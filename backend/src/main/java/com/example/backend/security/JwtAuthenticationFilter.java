package com.example.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre JWT — Intercepte chaque requête HTTP pour vérifier le token.
 *
 * Ce filtre s'exécute UNE FOIS par requête (OncePerRequestFilter).
 * Il est placé dans la chaîne de filtres Spring Security AVANT
 * UsernamePasswordAuthenticationFilter.
 *
 * Fonctionnement :
 *   1. Lit le header "Authorization: Bearer <token>"
 *   2. Extrait et valide le token JWT
 *   3. Charge l'utilisateur depuis la BDD
 *   4. Si valide → authentifie l'utilisateur dans le SecurityContext
 *   5. Continue la chaîne de filtres (filterChain.doFilter)
 *
 * Si le token est absent ou invalide, on passe sans authentifier.
 * Spring Security refusera alors l'accès aux routes protégées (401).
 */
@Component
@RequiredArgsConstructor // Lombok : génère un constructeur avec les champs final
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Récupère le header Authorization
        final String authHeader = request.getHeader("Authorization");

        // Si le header est absent ou ne commence pas par "Bearer " → on passe
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extrait le token (après "Bearer ")
        // "Bearer xxxxx.yyyyy.zzzzz" → "xxxxx.yyyyy.zzzzz"
        final String token = authHeader.substring(7);

        try {
            // 3. Extrait l'email depuis le token
            final String email = jwtService.extraireEmail(token);

            // 4. Si on a un email et qu'il n'y a pas déjà d'authentification
            //    dans le contexte (évite de re-authentifier à chaque filtre)
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Charge l'utilisateur depuis la BDD via son email
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // 5. Valide le token : bon utilisateur + non expiré
                if (jwtService.estValide(token, userDetails)) {
                    // Crée l'objet d'authentification Spring Security
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null, // credentials : null car on utilise JWT (pas de mdp ici)
                                    userDetails.getAuthorities()
                            );

                    // Ajoute les détails de la requête (IP, session…)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 6. Enregistre l'authentification dans le contexte de sécurité
                    // À partir d'ici, Spring Security considère l'utilisateur connecté
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token invalide (expiré, signature incorrecte…) → on ignore
            // Spring Security refusera l'accès aux routes protégées
        }

        // 7. Continue vers le prochain filtre / controller
        filterChain.doFilter(request, response);
    }
}
