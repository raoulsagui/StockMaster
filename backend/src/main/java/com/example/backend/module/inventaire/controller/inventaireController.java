package com.example.backend.module.inventaire.controller;

import com.example.backend.module.inventaire.dto.InventaireRequestDTO;
import com.example.backend.module.inventaire.dto.InventaireResponseDTO;
import com.example.backend.module.inventaire.dto.LigneInventaireRequestDTO;
import com.example.backend.module.inventaire.dto.LigneInventaireResponseDTO;
import com.example.backend.module.inventaire.service.InventaireService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST du module Inventaires.
 *
 * Routes exposées :
 *
 *   GET    /api/inventaires                            → liste paginée (tous rôles)
 *   GET    /api/inventaires/stats                      → KPI du tableau de bord (tous rôles)
 *   GET    /api/inventaires/entrepot/{id}              → inventaires d'un entrepôt (tous rôles)
 *   GET    /api/inventaires/{id}                       → détail avec lignes (tous rôles)
 *   POST   /api/inventaires                            → créer (ADMIN, GESTIONNAIRE, MAGASINIER)
 *   PATCH  /api/inventaires/{id}/demarrer              → démarrer (ADMIN, GESTIONNAIRE, MAGASINIER)
 *   PUT    /api/inventaires/{id}/lignes/{ligneId}      → saisir quantité (ADMIN, GESTIONNAIRE, MAGASINIER)
 *   PATCH  /api/inventaires/{id}/valider               → valider et appliquer ajustements (ADMIN, GESTIONNAIRE)
 *   PATCH  /api/inventaires/{id}/annuler               → annuler (ADMIN, GESTIONNAIRE)
 *
 * Politique d'accès :
 *   - Consultation : tous les rôles authentifiés
 *   - Création, démarrage, saisie : ADMIN + GESTIONNAIRE + MAGASINIER
 *   - Validation, annulation : ADMIN + GESTIONNAIRE (opérations sensibles)
 */
@RestController
@RequestMapping("/api/inventaires")
@RequiredArgsConstructor
public class InventaireController {

    private final InventaireService inventaireService;

    // -------------------------------------------------------
    // CONSULTATION
    // -------------------------------------------------------

    /**
     * GET /api/inventaires?page=0&size=20
     * Liste paginée de tous les inventaires, triés par date décroissante.
     */
    @GetMapping
    public ResponseEntity<Page<InventaireResponseDTO>> findAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(inventaireService.findAll(page, size));
    }

    /**
     * GET /api/inventaires/stats
     * Statistiques globales (nombre par statut).
     */
    @GetMapping("/stats")
    public ResponseEntity<InventaireService.InventaireStatsDTO> getStats() {
        return ResponseEntity.ok(inventaireService.getStats());
    }

    /**
     * GET /api/inventaires/entrepot/{entrepotId}
     * Tous les inventaires d'un entrepôt spécifique.
     */
    @GetMapping("/entrepot/{entrepotId}")
    public ResponseEntity<List<InventaireResponseDTO>> findByEntrepot(
            @PathVariable Long entrepotId) {
        return ResponseEntity.ok(inventaireService.findByEntrepot(entrepotId));
    }

    /**
     * GET /api/inventaires/{id}
     * Détail complet d'un inventaire avec toutes ses lignes.
     */
    @GetMapping("/{id}")
    public ResponseEntity<InventaireResponseDTO> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(inventaireService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // -------------------------------------------------------
    // CRÉATION
    // -------------------------------------------------------

    /**
     * POST /api/inventaires
     * Crée un nouvel inventaire (BROUILLON) avec génération automatique des lignes.
     *
     * Pour COMPLET : toutes les lignes de stock de l'entrepôt sont importées.
     * Pour PARTIEL : seules les lignes des produits sélectionnés sont créées.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER')")
    public ResponseEntity<?> creer(@Valid @RequestBody InventaireRequestDTO dto) {
        try {
            InventaireResponseDTO created = inventaireService.creer(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // -------------------------------------------------------
    // CYCLE DE VIE
    // -------------------------------------------------------

    /**
     * PATCH /api/inventaires/{id}/demarrer
     * Passe l'inventaire de BROUILLON à EN_COURS.
     */
    @PatchMapping("/{id}/demarrer")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER')")
    public ResponseEntity<?> demarrer(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(inventaireService.demarrer(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PUT /api/inventaires/{inventaireId}/lignes/{ligneId}
     * Saisit la quantité réelle comptée pour une ligne d'inventaire.
     * Accessible au MAGASINIER pour le comptage terrain.
     */
    @PutMapping("/{inventaireId}/lignes/{ligneId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER')")
    public ResponseEntity<?> saisirQuantite(
            @PathVariable Long inventaireId,
            @PathVariable Long ligneId,
            @Valid @RequestBody LigneInventaireRequestDTO dto) {
        try {
            LigneInventaireResponseDTO updated = inventaireService.saisirQuantite(inventaireId, ligneId, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PATCH /api/inventaires/{id}/valider
     * Valide l'inventaire et applique les ajustements de stock.
     * Réservé aux ADMIN et GESTIONNAIRE (opération irréversible).
     */
    @PatchMapping("/{id}/valider")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    public ResponseEntity<?> valider(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(inventaireService.valider(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PATCH /api/inventaires/{id}/annuler
     * Annule l'inventaire sans appliquer d'ajustements.
     * Réservé aux ADMIN et GESTIONNAIRE.
     */
    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    public ResponseEntity<?> annuler(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(inventaireService.annuler(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
