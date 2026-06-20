package com.example.backend.module.stock.controller;

import com.example.backend.module.stock.dto.MouvementStockResponseDTO;
import com.example.backend.module.stock.dto.StockResponseDTO;
import com.example.backend.module.stock.dto.StockSeuilRequestDTO;
import com.example.backend.module.stock.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST du module Stock (Module 7).
 *
 * Routes exposées :
 *   GET  /api/stocks                        → tous les stocks
 *   GET  /api/stocks/alertes                → stocks en stock faible
 *   GET  /api/stocks/entrepot/{id}          → stocks d'un entrepôt
 *   GET  /api/stocks/produit/{id}           → stocks d'un produit
 *   GET  /api/stocks/{id}                   → un stock précis
 *   GET  /api/stocks/{id}/mouvements        → historique paginé
 *   PATCH /api/stocks/{id}/seuils           → configurer min/max
 *
 * Politique d'accès :
 *   - Consultation : tous les rôles authentifiés
 *   - Configuration seuils : ADMIN et GESTIONNAIRE
 */
@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /**
     * GET /api/stocks
     * Retourne tous les stocks (toutes combinaisons produit/entrepôt).
     */
    @GetMapping
    public ResponseEntity<List<StockResponseDTO>> findAll() {
        return ResponseEntity.ok(stockService.findAll());
    }

    /**
     * GET /api/stocks/alertes
     * Retourne les stocks dont la quantité disponible est <= au seuil minimum.
     */
    @GetMapping("/alertes")
    public ResponseEntity<List<StockResponseDTO>> findStocksEnAlerte() {
        return ResponseEntity.ok(stockService.findStocksEnAlerte());
    }

    /**
     * GET /api/stocks/entrepot/{entrepotId}
     * Stocks d'un entrepôt spécifique.
     */
    @GetMapping("/entrepot/{entrepotId}")
    public ResponseEntity<List<StockResponseDTO>> findByEntrepot(
            @PathVariable Long entrepotId) {
        return ResponseEntity.ok(stockService.findByEntrepot(entrepotId));
    }

    /**
     * GET /api/stocks/produit/{produitId}
     * Stocks d'un produit dans tous les entrepôts.
     */
    @GetMapping("/produit/{produitId}")
    public ResponseEntity<List<StockResponseDTO>> findByProduit(
            @PathVariable Long produitId) {
        return ResponseEntity.ok(stockService.findByProduit(produitId));
    }

    /**
     * GET /api/stocks/{id}
     * Détail d'un stock spécifique.
     */
    @GetMapping("/{id}")
    public ResponseEntity<StockResponseDTO> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(stockService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/stocks/{id}/mouvements?page=0&size=20
     * Historique paginé des mouvements d'un stock.
     *
     * @param page Numéro de page (commence à 0)
     * @param size Nombre de lignes par page (défaut 20)
     */
    @GetMapping("/{id}/mouvements")
    public ResponseEntity<Page<MouvementStockResponseDTO>> getMouvements(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(stockService.getMouvements(id, page, size));
    }

    /**
     * GET /api/stocks/mouvements/derniers?limit=10
     * Les N derniers mouvements toutes entités confondues.
     * Utilisé par le tableau de bord.
     */
    @GetMapping("/mouvements/derniers")
    public ResponseEntity<List<MouvementStockResponseDTO>> getDerniersMouvements(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(stockService.getDerniersMouvements(limit));
    }

    /**
     * PATCH /api/stocks/{id}/seuils
     * Configure les seuils min/max d'un stock.
     * Réservé aux ADMIN et GESTIONNAIRE.
     */
    @PatchMapping("/{id}/seuils")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    public ResponseEntity<?> mettreAJourSeuils(
            @PathVariable Long id,
            @Valid @RequestBody StockSeuilRequestDTO dto) {
        try {
            return ResponseEntity.ok(stockService.mettreAJourSeuils(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
