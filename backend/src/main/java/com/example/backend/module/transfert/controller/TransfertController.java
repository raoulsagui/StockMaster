package com.example.backend.module.transfert.controller;

import com.example.backend.module.transfert.dto.TransfertRequestDTO;
import com.example.backend.module.transfert.dto.TransfertResponseDTO;
import com.example.backend.module.transfert.service.TransfertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST — Module 10 : Transferts inter-entrepôts.
 *
 * Politique d'accès :
 *   - Consulter              : ADMIN, GESTIONNAIRE, MAGASINIER, AUDITEUR
 *   - Créer / Expédier       : ADMIN, GESTIONNAIRE, MAGASINIER
 *   - Réceptionner / Annuler : ADMIN, GESTIONNAIRE, MAGASINIER
 *
 * Routes :
 *   GET    /api/transferts                    → liste tous
 *   GET    /api/transferts/{id}               → détail
 *   POST   /api/transferts                    → créer (BROUILLON)
 *   PATCH  /api/transferts/{id}/expedier      → expédier → retire stock source
 *   PATCH  /api/transferts/{id}/receptionner  → réceptionner → crédite stock destination
 *   PATCH  /api/transferts/{id}/annuler       → annuler (BROUILLON uniquement)
 */
@RestController
@RequestMapping("/api/transferts")
@RequiredArgsConstructor
public class TransfertController {

    private final TransfertService transfertService;

    @GetMapping
    public ResponseEntity<List<TransfertResponseDTO>> findAll() {
        return ResponseEntity.ok(transfertService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(transfertService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER')")
    public ResponseEntity<?> creer(@Valid @RequestBody TransfertRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(transfertService.creer(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/expedier")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER')")
    public ResponseEntity<?> expedier(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(transfertService.expedier(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/receptionner")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER')")
    public ResponseEntity<?> receptionner(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(transfertService.receptionner(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER')")
    public ResponseEntity<?> annuler(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(transfertService.annuler(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
