package com.example.backend.security;

import com.example.backend.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuration centrale de Spring Security.
 *
 * @EnableWebSecurity       → active la sécurité web Spring
 * @EnableMethodSecurity    → active @PreAuthorize sur les méthodes
 *                            (ex: @PreAuthorize("hasRole('ADMIN')"))
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    // On injecte l'implémentation séparée au lieu de la définir ici
    // → brise la dépendance circulaire avec JwtAuthenticationFilter
    private final UserDetailsServiceImpl userDetailsService;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * Chaîne de filtres de sécurité principale.
     *
     * On configure :
     *   - CORS : autorise les requêtes depuis Vue.js (localhost:5173)
     *   - CSRF : désactivé (inutile avec JWT stateless)
     *   - Routes publiques : /api/auth/**, /h2-console/**
     *   - Routes protégées : tout le reste nécessite un token valide
     *   - Stateless : pas de session HTTP (JWT gère l'état)
     *   - Notre filtre JWT est ajouté AVANT le filtre standard
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CORS : délègue à notre bean corsConfigurationSource()
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // CSRF désactivé : avec JWT on n'a pas besoin de protection CSRF
            // car les tokens ne sont pas dans les cookies (pas vulnérables au CSRF)
            .csrf(AbstractHttpConfigurer::disable)

            // Règles d'autorisation des routes
            .authorizeHttpRequests(auth -> auth
                // Routes publiques (pas besoin d'être connecté)
                .requestMatchers("/api/auth/**").permitAll()

                // --- Changer son propre mot de passe + profil : tous les utilisateurs connectés ---
                .requestMatchers("/api/utilisateurs/*/changer-mot-de-passe").authenticated()
                .requestMatchers("/api/utilisateurs/profil/**").authenticated()

                // --- Utilisateurs : ADMIN uniquement (création, modif, toggle statut, reset mdp) ---
                .requestMatchers("/api/utilisateurs/**").hasRole("ADMIN")

                // --- Entrepôts / Zones ---
                // Lecture : tous les rôles authentifiés
                .requestMatchers(org.springframework.http.HttpMethod.GET,
                        "/api/entrepots/**", "/api/zones/**")
                        .hasAnyRole("ADMIN", "GESTIONNAIRE", "MAGASINIER", "AUDITEUR")
                // Écriture (création, modif) : ADMIN + GESTIONNAIRE + MAGASINIER
                // Désactivation (PATCH statut) : ADMIN + GESTIONNAIRE (géré au niveau du controller)
                .requestMatchers("/api/entrepots/**", "/api/zones/**")
                        .hasAnyRole("ADMIN", "GESTIONNAIRE", "MAGASINIER")

                // --- Produits / Catégories / Fournisseurs ---
                // Lecture : tous les rôles authentifiés
                .requestMatchers(org.springframework.http.HttpMethod.GET,
                        "/api/produits/**", "/api/categories/**", "/api/fournisseurs/**")
                        .hasAnyRole("ADMIN", "GESTIONNAIRE", "MAGASINIER", "AUDITEUR")
                // Écriture (création, modif) : ADMIN + GESTIONNAIRE + MAGASINIER
                // Suppression / toggle statut : ADMIN + GESTIONNAIRE (géré au niveau du controller)
                .requestMatchers("/api/produits/**", "/api/categories/**", "/api/fournisseurs/**")
                        .hasAnyRole("ADMIN", "GESTIONNAIRE", "MAGASINIER")

                // --- Entrées / Sorties / Transferts : ADMIN + GESTIONNAIRE + MAGASINIER ---
                .requestMatchers("/api/entrees/**", "/api/sorties/**", "/api/transferts/**")
                        .hasAnyRole("ADMIN", "GESTIONNAIRE", "MAGASINIER")

                // --- Inventaires : ADMIN + GESTIONNAIRE + MAGASINIER ---
                // Lecture : tous les rôles
                .requestMatchers(org.springframework.http.HttpMethod.GET,
                        "/api/inventaires/**")
                        .hasAnyRole("ADMIN", "GESTIONNAIRE", "MAGASINIER", "AUDITEUR")
                // Écriture (création, démarrage, saisie) : ADMIN + GESTIONNAIRE + MAGASINIER
                // Validation / annulation : ADMIN + GESTIONNAIRE (géré au niveau du controller)
                .requestMatchers("/api/inventaires/**")
                        .hasAnyRole("ADMIN", "GESTIONNAIRE", "MAGASINIER")

                // --- Commandes fournisseur ---
                // Lecture : tous les rôles authentifiés
                .requestMatchers(org.springframework.http.HttpMethod.GET,
                        "/api/commandes/**")
                        .hasAnyRole("ADMIN", "GESTIONNAIRE", "MAGASINIER", "AUDITEUR")
                // Écriture (création, modif, réception) : ADMIN + GESTIONNAIRE + MAGASINIER
                // Validation / annulation : ADMIN + GESTIONNAIRE (géré au niveau du controller)
                .requestMatchers("/api/commandes/**")
                        .hasAnyRole("ADMIN", "GESTIONNAIRE", "MAGASINIER")

<<<<<<< HEAD
                // --- Emplacements : ADMIN + GESTIONNAIRE + MAGASINIER ---
                .requestMatchers("/api/emplacements/**")
                        .hasAnyRole("ADMIN", "GESTIONNAIRE", "MAGASINIER")

                // --- Stocks / Alertes / Rapports : tous les rôles authentifiés ---
                .requestMatchers("/api/stocks/**", "/api/alertes/**", "/api/rapports/**")
=======
                // --- Stocks / Mouvements / Alertes / Rapports : tous les rôles ---
                .requestMatchers("/api/stocks/**", "/api/alertes/**",
                        "/api/rapports/**", "/api/emplacements/**",
                        "/api/dashboard/**")
>>>>>>> origin/raoulbranch
                        .hasAnyRole("ADMIN", "GESTIONNAIRE", "MAGASINIER", "AUDITEUR")

                // Toutes les autres routes nécessitent d'être authentifié
                .anyRequest().authenticated()
            )

            // Pas de session HTTP : Spring Security ne crée pas de session
            // L'authentification est portée par le token JWT à chaque requête
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Enregistre notre provider d'authentification (email + BCrypt)
            .authenticationProvider(authenticationProvider())

            // Notre filtre JWT s'exécute AVANT le filtre standard
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configuration CORS.
     * Autorise le frontend Vue.js à appeler notre API depuis le navigateur.
     * Sans ça : erreur "CORS policy: No 'Access-Control-Allow-Origin' header"
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Origine autorisée : notre frontend Vue.js
        config.setAllowedOrigins(List.of(allowedOrigins));

        // Méthodes HTTP autorisées
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Headers autorisés dans les requêtes entrantes
        // Authorization : pour envoyer le token JWT
        // Content-Type  : pour envoyer du JSON
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // Autorise l'envoi de cookies / credentials (pas utilisé ici mais bonne pratique)
        config.setAllowCredentials(true);

        // Applique cette config à toutes les routes de l'API
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * AuthenticationProvider : gère l'authentification email + mot de passe.
     *
     * Spring Boot 4 / Spring Security 6.4+ breaking change :
     * DaoAuthenticationProvider exige maintenant le UserDetailsService
     * passé directement au constructeur (plus de setUserDetailsService()).
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * BCrypt : algorithme de hashage des mots de passe.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager : point d'entrée pour déclencher une authentification.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
