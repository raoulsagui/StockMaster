package com.example.backend.module.commande.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de création d'une commande fournisseur.
 *
 * POST /api/commandes
 *
 * Contient les informations de l'en-tête (fournisseur, entrepôt, date)
 * et la liste des lignes produit à commander.
 * Au moins une ligne est obligatoire.
 */
@Data
public class CommandeRequestDTO {

    @NotNull(message = "Le fournisseur est obligatoire")
    private Long fournisseurId;

    @NotNull(message = "L'entrepôt de destination est obligatoire")
    private Long entrepotId;

    /** Date de livraison souhaitée — optionnelle */
    private LocalDate dateLivraisonPrevue;

    @Size(max = 1000, message = "La note ne peut pas dépasser 1000 caractères")
    private String note;

    /**
     * Lignes produit de la commande.
     * Au moins une ligne est requise.
     * @Valid déclenche la validation de chaque LigneCommandeRequestDTO.
     */
    @NotEmpty(message = "La commande doit contenir au moins une ligne")
    @Valid
    private List<LigneCommandeRequestDTO> lignes;
}
