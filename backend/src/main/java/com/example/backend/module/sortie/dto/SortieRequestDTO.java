package com.example.backend.module.sortie.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO de création d'un bon de sortie.
 */
@Data
public class SortieRequestDTO {

    @NotNull(message = "Le produit est obligatoire")
    private Long produitId;

    @NotNull(message = "L'entrepôt est obligatoire")
    private Long entrepotId;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être d'au moins 1")
    private Integer quantite;

    /** LIVRAISON, RETOUR_FOURNISSEUR, CASSE, PERTE, AUTRE */
    private String motif;

    /** Destinataire ou référence client */
    private String destinataire;

    /** Note libre */
    private String note;
}
