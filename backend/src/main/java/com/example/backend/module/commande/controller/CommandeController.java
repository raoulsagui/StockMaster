package com.example.backend.module.commande.controller;

import com.example.backend.module.commande.dto.CommandeRequestDTO;
import com.example.backend.module.commande.dto.CommandeResponseDTO;
import com.example.backend.module.commande.dto.ReceptionRequestDTO;
import com.example.backend.module.commande.service.CommandeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST du module Commandes Fournisseur.
 *
 * Routes exposées :
 *
 *   GET    /api/commandes                          → liste paginée         (tous rôles)
 *   GET    /api/commandes/stats                    → KPI tableau de bord   (tous rôles)
 *   GET    /api/commandes/fournisseur/{id}         → par fournisseur       (tous rôles)
 *   GET    /api/commandes/entrepot/{id}            → par entrepôt          (tous rôles)
 *   GET    /api/commandes/{id}                     → détail + lignes       (tous rôles)
 *   POST   /api/commandes                          → créer                 (ADMIN, GESTIONNAIRE, MAGASINIER)
 *   PUT    /api/commandes/{id}                     → modifier (BROUILLON)  (ADMIN, GESTIONNAIRE, MAGASINIER)
 *   PATCH  /api/commandes/{id}/valider             → valider               (ADMIN, GESTIONNAIRE)
 *   PATCH  /api/commandes/{id}/receptionner        → réceptionner          (ADMIN, GESTIONNAIRE, MAGASINIER)
 *   PATCH  /api/commandes/{id}/annuler             → annuler               (ADMIN, GESTIONNAIRE)
 *
 * Politique d'accès :
 *   - Consultation              : tous les rôles authentifiés
 *   - Création / Modification   : ADMIN + GESTIONNAIRE + MAGASINIER
 *   - Validation / Annulation   : ADMIN + GESTIONNAIRE (opérations engageantes)
 *   - Réception                 : ADMIN + GESTIONNAIRE + MAGASINIER (acte terrain)
 */
@RestController
@RequestMapping("/api/commandes")
@RequiredArgsConstructor
public class CommandeController {

    private final CommandeService commandeService;

    // -------------------------------------------------------
    // CONSULTATION
    // -------------------------------------------------------

    /**
     * GET /api/commandes?page=0&size=20
     * Liste paginée de toutes les commandes, triées par date décroissante.
     */
    @GetMapping
    public ResponseEntity<Page<CommandeResponseDTO>> findAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(commandeService.findAll(page, size));
    }

    /**
     * GET /api/commandes/stats
     * Statistiques globales (total, brouillons, validées, livrées, annulées).
     */
    @GetMapping("/stats")
    public ResponseEntity<CommandeService.CommandeStatsDTO> getStats() {
        return ResponseEntity.ok(commandeService.getStats());
    }

    /**
     * GET /api/commandes/fournisseur/{fournisseurId}
     * Toutes les commandes d'un fournisseur donné.
     */
    @GetMapping("/fournisseur/{fournisseurId}")
    public ResponseEntity<List<CommandeResponseDTO>> findByFournisseur(
            @PathVariable Long fournisseurId) {
        return ResponseEntity.ok(commandeService.findByFournisseur(fournisseurId));
    }

    /**
     * GET /api/commandes/entrepot/{entrepotId}
     * Toutes les commandes destinées à un entrepôt donné.
     */
    @GetMapping("/entrepot/{entrepotId}")
    public ResponseEntity<List<CommandeResponseDTO>> findByEntrepot(
            @PathVariable Long entrepotId) {
        return ResponseEntity.ok(commandeService.findByEntrepot(entrepotId));
    }

    /**
     * GET /api/commandes/{id}
     * Détail complet d'une commande avec toutes ses lignes.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CommandeResponseDTO> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(commandeService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // -------------------------------------------------------
    // CRÉATION / MODIFICATION
    // -------------------------------------------------------

    /**
     * POST /api/commandes
     * Crée une nouvelle commande fournisseur en statut BROUILLON.
     * Les lignes sont créées en même temps que la commande.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER')")
    public ResponseEntity<?> creer(@Valid @RequestBody CommandeRequestDTO dto) {
        try {
            CommandeResponseDTO created = commandeService.creer(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * PUT /api/commandes/{id}
     * Modifie une commande en BROUILLON (en-tête + remplacement des lignes).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER')")
    public ResponseEntity<?> modifier(
            @PathVariable Long id,
            @Valid @RequestBody CommandeRequestDTO dto) {
        try {
            return ResponseEntity.ok(commandeService.modifier(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // -------------------------------------------------------
    // CYCLE DE VIE
    // -------------------------------------------------------

    /**
     * PATCH /api/commandes/{id}/valider
     * Valide la commande : BROUILLON → VALIDEE.
     * Réservé aux ADMIN et GESTIONNAIRE.
     */
    @PatchMapping("/{id}/valider")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    public ResponseEntity<?> valider(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(commandeService.valider(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PATCH /api/commandes/{id}/receptionner
     * Réceptionne la livraison : VALIDEE → LIVREE.
     * Met à jour le stock pour chaque ligne reçue.
     * Accessible au MAGASINIER (acte terrain de réception physique).
     */
    @PatchMapping("/{id}/receptionner")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER')")
    public ResponseEntity<?> receptionner(
            @PathVariable Long id,
            @Valid @RequestBody ReceptionRequestDTO dto) {
        try {
            return ResponseEntity.ok(commandeService.receptionner(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PATCH /api/commandes/{id}/annuler
     * Annule la commande sans générer de mouvement de stock.
     * Réservé aux ADMIN et GESTIONNAIRE.
     */
    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    public ResponseEntity<?> annuler(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(commandeService.annuler(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
