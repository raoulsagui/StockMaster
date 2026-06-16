package com.example.backend.module.utilisateur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO pour la modification du profil de l'utilisateur connecté.
 * Seuls le prénom et le nom sont modifiables.
 * L'email et le rôle ne sont pas exposés ici intentionnellement.
 */
@Data
public class ProfilRequestDTO {

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String prenom;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String nom;
}
