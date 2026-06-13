package com.example.backend.module.zone.dto;

import com.example.backend.module.zone.entity.TypeZone;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO de requête pour la création et la modification d'une zone.
 *
 * Utilisé par :
 *   POST /api/zones            → création d'une zone
 *   PUT  /api/zones/{id}       → modification d'une zone
 *
 * Le champ "actif" est géré séparément via PATCH /zones/{id}/statut.
 */
@Data
public class ZoneRequestDTO {

    /**
     * Nom de la zone — doit être unique au sein du même entrepôt.
     * Ex : "Zone A1 - Palettes", "Réception Nord"
     */
    @NotBlank(message = "Le nom de la zone est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;

    /**
     * Type fonctionnel de la zone.
     * Doit correspondre à l'une des valeurs de l'enum TypeZone.
     */
    @NotNull(message = "Le type de zone est obligatoire")
    private TypeZone type;

    /**
     * Description optionnelle (usage, contraintes, équipements…).
     */
    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;

    /**
     * Capacité totale de la zone en mètres carrés.
     * Le service vérifie que la somme des capacités ne dépasse pas l'entrepôt.
     */
    @NotNull(message = "La capacité totale est obligatoire")
    @DecimalMin(value = "1.0", message = "La capacité totale doit être d'au moins 1 m²")
    private Double capaciteTotale;

    /**
     * Capacité actuellement utilisée.
     * Optionnel à la création (défaut = 0).
     */
    @DecimalMin(value = "0.0", message = "La capacité utilisée ne peut pas être négative")
    private Double capaciteUtilisee = 0.0;

    /**
     * ID de l'entrepôt auquel cette zone appartient.
     * Obligatoire : une zone doit toujours être rattachée à un entrepôt.
     */
    @NotNull(message = "L'entrepôt est obligatoire")
    private Long entrepotId;
}
