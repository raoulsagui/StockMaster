package com.example.backend.module.commande.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * DTO de réception d'une commande fournisseur.
 *
 * PATCH /api/commandes/{id}/receptionner
 *
 * Lors de la réception, le magasinier saisit pour chaque ligne
 * la quantité réellement reçue (peut différer de la quantité commandée).
 * La réception déclenche l'ajout en stock via StockService.ajouterStock().
 */
@Data
public class ReceptionRequestDTO {

    /**
     * Quantités reçues par ligne de commande.
     * Au moins une ligne doit être fournie.
     */
    @NotEmpty(message = "Au moins une ligne de réception est requise")
    @Valid
    private List<LigneReceptionDTO> lignes;

    @Size(max = 1000, message = "La note ne peut pas dépasser 1000 caractères")
    private String note;

    // -------------------------------------------------------
    // DTO INTERNE
    // -------------------------------------------------------

    /**
     * Quantité reçue pour une ligne de commande donnée.
     */
    @Data
    public static class LigneReceptionDTO {

        @NotNull(message = "L'identifiant de la ligne est obligatoire")
        private Long ligneId;

        @NotNull(message = "La quantité reçue est obligatoire")
        @Min(value = 0, message = "La quantité reçue ne peut pas être négative")
        private Integer quantiteRecue;
    }
}
