package com.example.backend.module.alerte.controller;

import com.example.backend.module.alerte.dto.AlerteResponseDTO;
import com.example.backend.module.alerte.service.AlerteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller REST du module Alertes.
 *
 * GET  /api/alertes                → toutes les alertes (tous rôles)
 * GET  /api/alertes/actives        → alertes non résolues
 * GET  /api/alertes/stats          → statistiques pour le tableau de bord
 * GET  /api/alertes/count-non-lues → badge navbar
 * PATCH /api/alertes/{id}/lue      → marquer une alerte comme lue
 * PATCH /api/alertes/{id}/resolue  → marquer une alerte comme résolue
 * PATCH /api/alertes/tout-lire     → marquer toutes les alertes comme lues
 * POST /api/alertes/scan           → déclencher un scan manuel (ADMIN)
 */
@RestController
@RequestMapping("/api/alertes")
@RequiredArgsConstructor
public class AlerteController {

    private final AlerteService alerteService;

    @GetMapping
    public ResponseEntity<List<AlerteResponseDTO>> findAll(
            @RequestParam(required = false) String statut) {
        if (statut != null && !statut.isBlank()) {
            return ResponseEntity.ok(alerteService.findByStatut(statut));
        }
        return ResponseEntity.ok(alerteService.findAll());
    }

    @GetMapping("/actives")
    public ResponseEntity<List<AlerteResponseDTO>> findActives() {
        return ResponseEntity.ok(alerteService.findActives());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(alerteService.getStats());
    }

    @GetMapping("/count-non-lues")
    public ResponseEntity<Map<String, Long>> countNonLues() {
        return ResponseEntity.ok(Map.of("count", alerteService.countNonLues()));
    }

    @PatchMapping("/{id}/lue")
    public ResponseEntity<AlerteResponseDTO> marquerLue(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(alerteService.marquerLue(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/resolue")
    public ResponseEntity<AlerteResponseDTO> marquerResolue(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(alerteService.marquerResolue(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/tout-lire")
    public ResponseEntity<Void> marquerToutesLues() {
        alerteService.marquerToutesLues();
        return ResponseEntity.ok().build();
    }

    /**
     * Scan manuel — déclenche immédiatement une vérification.
     * Réservé à l'ADMIN pour les tests et actions d'urgence.
     */
    @PostMapping("/scan")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> scanner(
            @RequestParam(defaultValue = "TOUS") String type) {
        if ("TOUS".equals(type)) {
            alerteService.scannerToutesLesAlertes();
        } else {
            alerteService.forceDeclencher(type);
        }
        return ResponseEntity.ok(Map.of("message", "Scan déclenché avec succès"));
    }
}
