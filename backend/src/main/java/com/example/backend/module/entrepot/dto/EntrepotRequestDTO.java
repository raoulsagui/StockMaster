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
 * Les annotations Bean Validation déclenchent une réponse 400 Bad Request
 * automatiquement si le client envoie des données invalides.
 * Elles sont activées par @Valid dans le controller.
 *
 * Note : le champ "actif" n'est pas ici — il est géré séparément
 * via PATCH /entrepots/{id}/statut (cohérence avec le module utilisateur).
 */
@Data
public class EntrepotRequestDTO {

    /**
     * Nom de l'entrepôt.
     * Doit être unique (vérifié côté service).
     */
    @NotBlank(message = "Le nom de l'entrepôt est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;

    /**
     * Adresse physique complète.
     */
    @NotBlank(message = "L'adresse est obligatoire")
    @Size(min = 5, max = 255, message = "L'adresse doit contenir entre 5 et 255 caractères")
    private String adresse;

    /**
     * Capacité totale en mètres carrés.
     * Doit être un nombre strictement positif.
     */
    @NotNull(message = "La capacité totale est obligatoire")
    @DecimalMin(value = "1.0", message = "La capacité totale doit être d'au moins 1 m²")
    private Double capaciteTotale;

    /**
     * Capacité actuellement utilisée.
     * Optionnel à la création (défaut = 0).
     * Ne peut pas dépasser la capacité totale (vérifié côté service).
     */
    @DecimalMin(value = "0.0", message = "La capacité utilisée ne peut pas être négative")
    private Double capaciteUtilisee = 0.0;

    /**
     * ID du responsable de l'entrepôt (référence vers un Utilisateur).
     * Optionnel : un entrepôt peut exister sans responsable assigné.
     */
    private Long responsableId;
}
