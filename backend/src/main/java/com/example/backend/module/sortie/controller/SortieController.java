package com.example.backend.module.sortie.controller;

import com.example.backend.module.sortie.dto.SortieRequestDTO;
import com.example.backend.module.sortie.dto.SortieResponseDTO;
import com.example.backend.module.sortie.service.SortieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST — Module 9 : Sorties de stock.
 *
 * Politique d'accès :
 *   - Consulter          : ADMIN, GESTIONNAIRE, MAGASINIER, AUDITEUR
 *   - Créer              : ADMIN, GESTIONNAIRE, MAGASINIER
 *   - Valider / Annuler  : ADMIN, GESTIONNAIRE
 *
 * Routes :
 *   GET    /api/sorties              → liste tous les bons
 *   GET    /api/sorties/{id}         → détail
 *   POST   /api/sorties              → créer (BROUILLON)
 *   PATCH  /api/sorties/{id}/valider → valider → déduit le stock
 *   PATCH  /api/sorties/{id}/annuler → annuler
 */
@RestController
@RequestMapping("/api/sorties")
@RequiredArgsConstructor
public class SortieController {

    private final SortieService sortieService;

    @GetMapping
    public ResponseEntity<List<SortieResponseDTO>> findAll() {
        return ResponseEntity.ok(sortieService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(sortieService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER')")
    public ResponseEntity<?> creer(@Valid @RequestBody SortieRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(sortieService.creer(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/valider")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    public ResponseEntity<?> valider(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(sortieService.valider(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    public ResponseEntity<?> annuler(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(sortieService.annuler(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
