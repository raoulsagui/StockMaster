package com.example.backend.module.utilisateur.dto;

import com.example.backend.module.utilisateur.entity.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO (Data Transfer Object) pour la création et modification d'un utilisateur.
 *
 * Pourquoi un DTO et pas directement l'entité ?
 *   - On ne veut pas exposer tous les champs de l'entité au client
 *   - On peut avoir des règles de validation différentes selon l'opération
 *   - Sécurité : évite le "mass assignment" (le client ne peut pas
 *     modifier des champs sensibles comme dateCreation, actif, etc.)
 *
 * Les annotations de validation (Bean Validation) déclenchent une erreur
 * 400 Bad Request si le client envoie des données invalides.
 * Elles sont activées par @Valid dans le controller.
 */
@Data
public class UtilisateurRequestDTO {

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String prenom;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email n'est pas valide")
    private String email;

    @NotNull(message = "Le rôle est obligatoire")
    private Role role;

    // motDePasse supprimé : généré automatiquement à la création et envoyé par email
    // actif est géré séparément via l'endpoint PATCH /utilisateurs/{id}/statut
}
