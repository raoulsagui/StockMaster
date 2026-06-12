package com.example.backend.module.utilisateur.service;

import com.example.backend.module.utilisateur.dto.LoginRequestDTO;
import com.example.backend.module.utilisateur.dto.LoginResponseDTO;
import com.example.backend.module.utilisateur.dto.UtilisateurResponseDTO;
import com.example.backend.module.utilisateur.entity.Utilisateur;
import com.example.backend.module.utilisateur.repository.UtilisateurRepository;
import com.example.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * Service d'authentification.
 *
 * Gère la connexion et retourne un token JWT.
 *
 * Flux de connexion :
 *   1. AuthController reçoit POST /api/auth/login avec email + mdp
 *   2. AuthService appelle authenticationManager.authenticate()
 *   3. Spring Security vérifie email + mdp via DaoAuthenticationProvider
 *   4. Si OK → on génère un token JWT via JwtService
 *   5. On retourne le token + les infos de l'utilisateur
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Authentifie un utilisateur et retourne un token JWT.
     *
     * @param dto Email + mot de passe saisis par l'utilisateur
     * @return    Token JWT + infos utilisateur
     * @throws org.springframework.security.core.AuthenticationException si identifiants invalides
     */
    public LoginResponseDTO login(LoginRequestDTO dto) {
        // Spring Security vérifie les credentials.
        // Lance BadCredentialsException si email/mdp incorrect.
        // Lance DisabledException si compte inactif (isEnabled() = false).
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                dto.getEmail(),
                dto.getMotDePasse()
            )
        );

        // À ce stade, l'authentification a réussi.
        // On charge l'utilisateur pour générer son token.
        Utilisateur utilisateur = utilisateurRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Génère le token JWT signé avec notre clé secrète
        String token = jwtService.genererToken(utilisateur);

        return LoginResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .utilisateur(UtilisateurResponseDTO.fromEntity(utilisateur))
                .build();
    }
}
