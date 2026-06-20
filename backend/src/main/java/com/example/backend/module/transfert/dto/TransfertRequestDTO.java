package com.example.backend.module.transfert.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO de création d'un transfert inter-entrepôts.
 */
@Data
public class TransfertRequestDTO {

    @NotNull(message = "Le produit est obligatoire")
    private Long produitId;

    @NotNull(message = "L'entrepôt source est obligatoire")
    private Long entrepotSourceId;

    @NotNull(message = "L'entrepôt destination est obligatoire")
    private Long entrepotDestinationId;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être d'au moins 1")
    private Integer quantite;

    /** Note libre */
    private String note;
}
