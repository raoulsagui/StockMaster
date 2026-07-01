package com.example.backend.module.emplacement.controller;

import com.example.backend.module.emplacement.dto.*;
import com.example.backend.module.emplacement.service.EmplacementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller REST — Gestion des emplacements (Rayon / Étagère / Emplacement).
 *
 * Hiérarchie : Entrepôt → Zone → Rayon → Étagère → Emplacement
 *
 * ─── RAYONS ─────────────────────────────────────────────────────
 *   GET    /api/emplacements/rayons?zoneId=           → liste par zone
 *   GET    /api/emplacements/rayons/{id}              → détail
 *   POST   /api/emplacements/rayons                   → créer (ADMIN, GEST, MAGASINIER)
 *   PUT    /api/emplacements/rayons/{id}              → modifier
 *   PATCH  /api/emplacements/rayons/{id}/statut       → toggle actif (ADMIN, GEST)
 *
 * ─── ÉTAGÈRES ───────────────────────────────────────────────────
 *   GET    /api/emplacements/etageres?rayonId=        → liste par rayon
 *   GET    /api/emplacements/etageres/{id}            → détail
 *   POST   /api/emplacements/etageres                 → créer
 *   PUT    /api/emplacements/etageres/{id}            → modifier
 *   PATCH  /api/emplacements/etageres/{id}/statut     → toggle actif
 *
 * ─── EMPLACEMENTS ───────────────────────────────────────────────
 *   GET    /api/emplacements?etagereId=               → liste par étagère
 *   GET    /api/emplacements?entrepotId=              → liste par entrepôt
 *   GET    /api/emplacements?zoneId=                  → liste par zone
 *   GET    /api/emplacements/libres?entrepotId=       → emplacements libres
 *   GET    /api/emplacements/stats?entrepotId=        → statistiques
 *   GET    /api/emplacements/{id}                     → détail
 *   POST   /api/emplacements                          → créer
 *   PUT    /api/emplacements/{id}                     → modifier
 *   PATCH  /api/emplacements/{id}/statut              → changer statut
 */
@RestController
@RequestMapping("/api/emplacements")
@RequiredArgsConstructor
public class EmplacementController {

    private final EmplacementService service;

    // ================================================================
    // RAYONS
    // ================================================================

    @GetMapping("/rayons")
    public ResponseEntity<List<RayonResponseDTO>> getRayons(@RequestParam Long zoneId) {
        return ResponseEntity.ok(service.findRayonsByZone(zoneId));
    }

    @GetMapping("/rayons/{id}")
    public ResponseEntity<?> getRayonById(@PathVariable Long id) {
        try { return ResponseEntity.ok(service.findRayonById(id)); }
        catch (RuntimeException e) { return ResponseEntity.notFound().build(); }
    }

    @PostMapping("/rayons")
    @PreAuthorize("hasAnyRole('ADMIN','GESTIONNAIRE','MAGASINIER')")
    public ResponseEntity<?> creerRayon(@Valid @RequestBody RayonRequestDTO dto) {
        try { return ResponseEntity.status(HttpStatus.CREATED).body(service.creerRayon(dto)); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); }
    }

    @PutMapping("/rayons/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GESTIONNAIRE','MAGASINIER')")
    public ResponseEntity<?> modifierRayon(@PathVariable Long id, @Valid @RequestBody RayonRequestDTO dto) {
        try { return ResponseEntity.ok(service.modifierRayon(id, dto)); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); }
    }

    @PatchMapping("/rayons/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN','GESTIONNAIRE')")
    public ResponseEntity<?> toggleRayonStatut(@PathVariable Long id) {
        try { return ResponseEntity.ok(service.toggleRayonStatut(id)); }
        catch (RuntimeException e) { return ResponseEntity.notFound().build(); }
    }

    // ================================================================
    // ÉTAGÈRES
    // ================================================================

    @GetMapping("/etageres")
    public ResponseEntity<List<EtagereResponseDTO>> getEtageres(@RequestParam Long rayonId) {
        return ResponseEntity.ok(service.findEtageresByRayon(rayonId));
    }

    @GetMapping("/etageres/{id}")
    public ResponseEntity<?> getEtagereById(@PathVariable Long id) {
        try { return ResponseEntity.ok(service.findEtagereById(id)); }
        catch (RuntimeException e) { return ResponseEntity.notFound().build(); }
    }

    @PostMapping("/etageres")
    @PreAuthorize("hasAnyRole('ADMIN','GESTIONNAIRE','MAGASINIER')")
    public ResponseEntity<?> creerEtagere(@Valid @RequestBody EtagereRequestDTO dto) {
        try { return ResponseEntity.status(HttpStatus.CREATED).body(service.creerEtagere(dto)); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); }
    }

    @PutMapping("/etageres/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GESTIONNAIRE','MAGASINIER')")
    public ResponseEntity<?> modifierEtagere(@PathVariable Long id, @Valid @RequestBody EtagereRequestDTO dto) {
        try { return ResponseEntity.ok(service.modifierEtagere(id, dto)); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); }
    }

    @PatchMapping("/etageres/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN','GESTIONNAIRE')")
    public ResponseEntity<?> toggleEtagereStatut(@PathVariable Long id) {
        try { return ResponseEntity.ok(service.toggleEtagereStatut(id)); }
        catch (RuntimeException e) { return ResponseEntity.notFound().build(); }
    }

    // ================================================================
    // EMPLACEMENTS
    // ================================================================

    @GetMapping
    public ResponseEntity<List<EmplacementResponseDTO>> getEmplacements(
            @RequestParam(required = false) Long etagereId,
            @RequestParam(required = false) Long entrepotId,
            @RequestParam(required = false) Long zoneId) {
        if (etagereId != null)
            return ResponseEntity.ok(service.findEmplacementsByEtagere(etagereId));
        if (entrepotId != null)
            return ResponseEntity.ok(service.findEmplacementsByEntrepot(entrepotId));
        if (zoneId != null)
            return ResponseEntity.ok(service.findEmplacementsByZone(zoneId));
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/libres")
    public ResponseEntity<List<EmplacementResponseDTO>> getLibres(@RequestParam Long entrepotId) {
        return ResponseEntity.ok(service.findEmplacementsLibres(entrepotId));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(@RequestParam Long entrepotId) {
        return ResponseEntity.ok(service.getStatsEntrepot(entrepotId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try { return ResponseEntity.ok(service.findEmplacementById(id)); }
        catch (RuntimeException e) { return ResponseEntity.notFound().build(); }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GESTIONNAIRE','MAGASINIER')")
    public ResponseEntity<?> creer(@Valid @RequestBody EmplacementRequestDTO dto) {
        try { return ResponseEntity.status(HttpStatus.CREATED).body(service.creerEmplacement(dto)); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GESTIONNAIRE','MAGASINIER')")
    public ResponseEntity<?> modifier(@PathVariable Long id, @Valid @RequestBody EmplacementRequestDTO dto) {
        try { return ResponseEntity.ok(service.modifierEmplacement(id, dto)); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); }
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN','GESTIONNAIRE','MAGASINIER')")
    public ResponseEntity<?> changerStatut(
            @PathVariable Long id,
            @RequestParam String statut) {
        try { return ResponseEntity.ok(service.changerStatut(id, statut)); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); }
    }
}
