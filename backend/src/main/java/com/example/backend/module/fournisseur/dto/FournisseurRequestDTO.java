package com.example.backend.module.fournisseur.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO de requête pour la création et la modification d'un fournisseur.
 */
@Data
public class FournisseurRequestDTO {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;

    @NotBlank(message = "L'adresse est obligatoire")
    @Size(min = 5, max = 255, message = "L'adresse doit contenir entre 5 et 255 caractères")
    private String adresse;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Size(min = 6, max = 20, message = "Le téléphone doit contenir entre 6 et 20 caractères")
    private String telephone;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email n'est pas valide")
    private String email;

    @NotBlank(message = "Le contact principal est obligatoire")
    @Size(min = 2, max = 100, message = "Le contact doit contenir entre 2 et 100 caractères")
    private String contactPrincipal;
}
