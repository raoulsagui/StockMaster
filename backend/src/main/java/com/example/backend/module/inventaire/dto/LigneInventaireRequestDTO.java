package com.example.backend.module.inventaire.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO de saisie d'une ligne d'inventaire.
 *
 * PUT /api/inventaires/{id}/lignes/{ligneId}
 *
 * Permet au magasinier de saisir la quantité réellement comptée
 * pour un produit donné.
 */
@Data
public class LigneInventaireRequestDTO {

    @NotNull(message = "La quantité comptée est obligatoire")
    @Min(value = 0, message = "La quantité comptée ne peut pas être négative")
    private Integer quantiteComptee;

    @Size(max = 500, message = "La note ne peut pas dépasser 500 caractères")
    private String note;
}
