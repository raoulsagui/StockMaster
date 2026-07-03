package com.example.backend.module.entrepot.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO de requête pour la création et la modification d'un entrepôt.
 *
 * Utilisé par :
 *   POST /api/entrepots         → création
 *   PUT  /api/entrepots/{id}    → modification complète
 *
 * Note : "actif" est géré séparément via PATCH /entrepots/{id}/statut.
 */
@Data
public class EntrepotRequestDTO {

    @NotBlank(message = "Le nom de l'entrepôt est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;

    @NotBlank(message = "L'adresse est obligatoire")
    @Size(min = 5, max = 255, message = "L'adresse doit contenir entre 5 et 255 caractères")
    private String adresse;

    @NotNull(message = "La capacité totale est obligatoire")
    @DecimalMin(value = "1.0", message = "La capacité totale doit être d'au moins 1 m³")
    private Double capaciteTotale;

    /**
     * ID du responsable (optionnel).
     */
    private Long responsableId;
}
