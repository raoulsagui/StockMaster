package com.example.backend.module.stock.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * DTO pour la mise à jour des seuils min/max d'un stock.
 *
 * PATCH /api/stocks/{id}/seuils
 *
 * Séparé du stock principal car la mise à jour des seuils
 * est une action distincte (configuration) des mouvements de stock.
 */
@Data
public class StockSeuilRequestDTO {

    /**
     * Seuil minimum de stock.
     * Null = désactiver l'alerte stock faible.
     */
    @Min(value = 0, message = "Le stock minimum ne peut pas être négatif")
    private Integer stockMinimum;

    /**
     * Seuil maximum de stock.
     * Null = désactiver l'alerte sur-stock.
     */
    @Min(value = 0, message = "Le stock maximum ne peut pas être négatif")
    private Integer stockMaximum;
}
