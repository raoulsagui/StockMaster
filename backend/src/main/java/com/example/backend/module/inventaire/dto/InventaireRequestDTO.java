package com.example.backend.module.inventaire.dto;

import com.example.backend.module.inventaire.entity.Inventaire;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de création d'un inventaire.
 *
 * POST /api/inventaires
 *
 * Pour un inventaire COMPLET : produitsIds peut être null ou vide —
 * le service chargera automatiquement tous les stocks de l'entrepôt.
 *
 * Pour un inventaire PARTIEL : produitsIds doit contenir au moins un ID.
 */
@Data
public class InventaireRequestDTO {

    @NotNull(message = "Le type d'inventaire est obligatoire")
    private Inventaire.TypeInventaire type;

    @NotNull(message = "L'entrepôt est obligatoire")
    private Long entrepotId;

    @NotNull(message = "La date prévue est obligatoire")
    @FutureOrPresent(message = "La date prévue ne peut pas être dans le passé")
    private LocalDate datePrevue;

    @Size(max = 1000, message = "La note ne peut pas dépasser 1000 caractères")
    private String note;

    /**
     * IDs des produits à inventorier.
     * Requis uniquement pour les inventaires PARTIELS.
     * Ignoré pour les inventaires COMPLETS (tous les produits de l'entrepôt).
     */
    private List<Long> produitsIds;
}
