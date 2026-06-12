package com.example.backend.module.utilisateur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO pour le changement de mot de passe.
 *
 * Utilisé dans deux cas :
 *   1. Première connexion / après reset → ancienMotDePasse peut être null
 *   2. Changement volontaire → ancienMotDePasse obligatoire
 *
 * POST /api/utilisateurs/{id}/changer-mot-de-passe
 */
@Data
public class ChangerMotDePasseRequestDTO {

    /**
     * Ancien mot de passe — peut être null si c'est un changement forcé
     * (première connexion ou après réinitialisation par l'admin).
     */
    private String ancienMotDePasse;

    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String nouveauMotDePasse;

    @NotBlank(message = "La confirmation est obligatoire")
    private String confirmerMotDePasse;
}
