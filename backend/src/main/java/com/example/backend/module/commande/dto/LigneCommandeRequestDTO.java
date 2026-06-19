package com.example.backend.module.commande.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO d'une ligne de commande fournisseur.
 *
 * Utilisé dans CommandeRequestDTO.lignes[]
 * et pour la réception (avec quantiteRecue).
 */
@Data
public class LigneCommandeRequestDTO {

    @NotNull(message = "Le produit est obligatoire")
    private Long produitId;

    @NotNull(message = "La quantité commandée est obligatoire")
    @Min(value = 1, message = "La quantité commandée doit être d'au moins 1")
    private Integer quantiteCommandee;

    /**
     * Prix unitaire HT négocié.
     * Optionnel : peut être saisi après la création de la commande.
     */
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix unitaire doit être positif")
    private BigDecimal prixUnitaire;

    @Size(max = 500, message = "La note ne peut pas dépasser 500 caractères")
    private String note;
}
