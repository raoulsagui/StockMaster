package com.example.backend.module.stock.dto;

import com.example.backend.module.stock.entity.Stock;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour un stock.
 *
 * Retourné par GET /api/stocks et GET /api/stocks/{id}.
 * Contient toutes les informations nécessaires à l'affichage frontend,
 * incluant les infos du produit et de l'entrepôt associés.
 */
@Data
@Builder
public class StockResponseDTO {

    private Long id;

    // --- Informations du produit ---
    private Long produitId;
    private String produitReference;
    private String produitNom;
    private String produitCategorieNom;

    // --- Informations de l'entrepôt ---
    private Long entrepotId;
    private String entrepotNom;

    // --- Quantités ---
    private Integer quantiteDisponible;
    private Integer quantiteReservee;
    private Integer quantiteEnTransit;

    /** Quantité totale = disponible + réservée */
    private Integer quantiteTotale;

    // --- Seuils ---
    private Integer stockMinimum;
    private Integer stockMaximum;

    // --- Alertes calculées ---
    /** true si quantiteDisponible <= stockMinimum */
    private boolean enStockFaible;

    /** true si quantiteTotale > stockMaximum */
    private boolean enSurStock;

    private LocalDateTime derniereMaj;

    /**
     * Factory method : convertit l'entité Stock en DTO.
     */
    public static StockResponseDTO fromEntity(Stock s) {
        return StockResponseDTO.builder()
                .id(s.getId())
                // Produit
                .produitId(s.getProduit().getId())
                .produitReference(s.getProduit().getReference())
                .produitNom(s.getProduit().getNom())
                .produitCategorieNom(
                    s.getProduit().getCategorie() != null
                        ? s.getProduit().getCategorie().getNom()
                        : null
                )
                // Entrepôt
                .entrepotId(s.getEntrepot().getId())
                .entrepotNom(s.getEntrepot().getNom())
                // Quantités
                .quantiteDisponible(s.getQuantiteDisponible())
                .quantiteReservee(s.getQuantiteReservee())
                .quantiteEnTransit(s.getQuantiteEnTransit())
                .quantiteTotale(s.getQuantiteTotale())
                // Seuils
                .stockMinimum(s.getStockMinimum())
                .stockMaximum(s.getStockMaximum())
                // Alertes
                .enStockFaible(s.estEnStockFaible())
                .enSurStock(s.estEnSurStock())
                .derniereMaj(s.getDerniereMaj())
                .build();
    }
}
