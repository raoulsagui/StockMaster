package com.example.backend.module.utilisateur.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO de réponse après une connexion réussie.
 * Contient le token JWT et les infos de l'utilisateur connecté.
 *
 * Le frontend stockera ce token dans Pinia et l'enverra
 * dans le header "Authorization: Bearer <token>" à chaque requête.
 */
@Data
@Builder
public class LoginResponseDTO {

    /** Token JWT à stocker côté client */
    private String token;

    /** Type du token — toujours "Bearer" dans notre cas */
    private String type;

    /** Infos de l'utilisateur connecté pour affichage dans l'UI */
    private UtilisateurResponseDTO utilisateur;
}
