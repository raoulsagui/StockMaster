package com.example.backend.module.zone.controller;

import com.example.backend.module.zone.dto.ZoneRequestDTO;
import com.example.backend.module.zone.dto.ZoneResponseDTO;
import com.example.backend.module.zone.service.ZoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST du module Zones de stockage.
 *
 * Routes exposées :
 *   GET    /api/zones                          → liste toutes les zones (tous rôles)
 *   GET    /api/zones/{id}                     → détail d'une zone (tous rôles)
 *   GET    /api/zones/entrepot/{entrepotId}    → zones d'un entrepôt (tous rôles)
 *   POST   /api/zones                          → créer une zone (ADMIN, GESTIONNAIRE)
 *   PUT    /api/zones/{id}                     → modifier une zone (ADMIN, GESTIONNAIRE)
 *   PATCH  /api/zones/{id}/statut              → activer/désactiver (ADMIN)
 *
 * Politique d'accès identique au module Entrepôts.
 */
@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneService zoneService;

    /**
     * GET /api/zones
     * Retourne la liste de toutes les zones avec leurs métriques d'occupation.
     */
    @GetMapping
    public ResponseEntity<List<ZoneResponseDTO>> findAll() {
        return ResponseEntity.ok(zoneService.findAll());
    }

    /**
     * GET /api/zones/{id}
     * Retourne le détail d'une zone par son ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ZoneResponseDTO> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(zoneService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/zones/entrepot/{entrepotId}
     * Retourne toutes les zones appartenant à un entrepôt donné.
     * Endpoint dédié pour afficher les zones sur la page de détail d'un entrepôt.
     *
     * @param entrepotId L'id de l'entrepôt dont on veut les zones
     */
    @GetMapping("/entrepot/{entrepotId}")
    public ResponseEntity<?> findByEntrepot(@PathVariable Long entrepotId) {
        try {
            return ResponseEntity.ok(zoneService.findByEntrepot(entrepotId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/zones
     * Crée une nouvelle zone dans un entrepôt.
     *
     * Retourne 201 Created si succès.
     * Accès : ADMIN ou GESTIONNAIRE.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER')")
    public ResponseEntity<?> creer(@Valid @RequestBody ZoneRequestDTO dto) {
        try {
            ZoneResponseDTO created = zoneService.creer(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * PUT /api/zones/{id}
     * Modifie une zone existante.
     * Accès : ADMIN ou GESTIONNAIRE.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER')")
    public ResponseEntity<?> modifier(
            @PathVariable Long id,
            @Valid @RequestBody ZoneRequestDTO dto
    ) {
        try {
            return ResponseEntity.ok(zoneService.modifier(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * PATCH /api/zones/{id}/statut
     * Active ou désactive une zone.
     * Accès : ADMIN uniquement.
     */
    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    public ResponseEntity<?> toggleStatut(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(zoneService.toggleStatut(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
