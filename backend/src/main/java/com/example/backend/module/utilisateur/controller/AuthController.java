package com.example.backend.module.utilisateur.controller;

import com.example.backend.module.utilisateur.dto.LoginRequestDTO;
import com.example.backend.module.utilisateur.dto.LoginResponseDTO;
import com.example.backend.module.utilisateur.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.*;

/**
 * Controller d'authentification.
 *
 * Routes publiques (pas de token requis) :
 *   POST /api/auth/login → connexion, retourne un token JWT
 *
 * @RestController   → combine @Controller + @ResponseBody
 *                    Chaque méthode retourne du JSON automatiquement
 * @RequestMapping   → préfixe commun à toutes les routes du controller
 * @CrossOrigin      → remplacé par la config CORS globale dans SecurityConfig
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Connexion utilisateur.
     *
     * POST /api/auth/login
     * Body : { "email": "...", "motDePasse": "..." }
     *
     * Réponses :
     *   200 OK    → { "token": "...", "type": "Bearer", "utilisateur": {...} }
     *   400       → Validation échouée (email vide, format invalide…)
     *   401       → Email ou mot de passe incorrect
     *   403       → Compte désactivé
     *
     * @Valid → déclenche la validation des annotations sur LoginRequestDTO
     * @RequestBody → lit le corps JSON de la requête et le convertit en DTO
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO dto) {
        try {
            LoginResponseDTO response = authService.login(dto);
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            // Spring Security lance cette exception si email/mdp invalide
            return ResponseEntity.status(401).body("Email ou mot de passe incorrect");

        } catch (DisabledException e) {
            // Spring Security lance cette exception si isEnabled() = false
            return ResponseEntity.status(403).body("Ce compte est désactivé");
        }
    }
}
