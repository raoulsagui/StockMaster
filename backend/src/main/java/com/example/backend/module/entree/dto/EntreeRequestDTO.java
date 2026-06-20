package com.example.backend.module.entree.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO de création d'un bon de réception.
 */
@Data
public class EntreeRequestDTO {

    @NotNull(message = "Le produit est obligatoire")
    private Long produitId;

    @NotNull(message = "L'entrepôt est obligatoire")
    private Long entrepotId;

    /** Fournisseur optionnel */
    private Long fournisseurId;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être d'au moins 1")
    private Integer quantite;

    /** Prix unitaire d'achat — optionnel */
    private BigDecimal prixUnitaire;

    /** Note libre */
    private String note;
}
