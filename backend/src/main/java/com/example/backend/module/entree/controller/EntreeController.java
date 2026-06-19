package com.example.backend.module.entree.controller;

import com.example.backend.module.entree.dto.EntreeRequestDTO;
import com.example.backend.module.entree.dto.EntreeResponseDTO;
import com.example.backend.module.entree.service.EntreeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST — Module 8 : Entrées de stock.
 *
 * Politique d'accès (conforme à Rôles.md) :
 *   - Consulter          : ADMIN, GESTIONNAIRE, MAGASINIER, AUDITEUR
 *   - Créer              : ADMIN, GESTIONNAIRE, MAGASINIER
 *   - Valider / Annuler  : ADMIN, GESTIONNAIRE
 *
 * Routes :
 *   GET    /api/entrees             → liste tous les bons
 *   GET    /api/entrees/{id}        → détail d'un bon
 *   POST   /api/entrees             → créer un bon (BROUILLON)
 *   PATCH  /api/entrees/{id}/valider  → valider → met à jour le stock
 *   PATCH  /api/entrees/{id}/annuler  → annuler (BROUILLON uniquement)
 */
@RestController
@RequestMapping("/api/entrees")
@RequiredArgsConstructor
public class EntreeController {

    private final EntreeService entreeService;

    @GetMapping
    public ResponseEntity<List<EntreeResponseDTO>> findAll() {
        return ResponseEntity.ok(entreeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(entreeService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER')")
    public ResponseEntity<?> creer(@Valid @RequestBody EntreeRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(entreeService.creer(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/valider")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    public ResponseEntity<?> valider(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(entreeService.valider(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    public ResponseEntity<?> annuler(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(entreeService.annuler(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
