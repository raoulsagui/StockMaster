package com.example.backend.module.fournisseur.controller;

import com.example.backend.module.fournisseur.dto.FournisseurRequestDTO;
import com.example.backend.module.fournisseur.dto.FournisseurResponseDTO;
import com.example.backend.module.fournisseur.service.FournisseurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST du module Fournisseurs.
 *
 * GET    /api/fournisseurs            → liste tous les fournisseurs
 * GET    /api/fournisseurs/actifs     → liste des fournisseurs actifs
 * GET    /api/fournisseurs/{id}       → détail d'un fournisseur
 * POST   /api/fournisseurs            → créer un fournisseur (ADMIN, GESTIONNAIRE)
 * PUT    /api/fournisseurs/{id}       → modifier un fournisseur (ADMIN, GESTIONNAIRE)
 * PATCH  /api/fournisseurs/{id}/statut → activer/désactiver (ADMIN)
 */
@RestController
@RequestMapping("/api/fournisseurs")
@RequiredArgsConstructor
public class FournisseurController {

    private final FournisseurService fournisseurService;

    @GetMapping
    public ResponseEntity<List<FournisseurResponseDTO>> findAll() {
        return ResponseEntity.ok(fournisseurService.findAll());
    }

    @GetMapping("/actifs")
    public ResponseEntity<List<FournisseurResponseDTO>> findActifs() {
        return ResponseEntity.ok(fournisseurService.findActifs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FournisseurResponseDTO> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(fournisseurService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER')")
    public ResponseEntity<?> creer(@Valid @RequestBody FournisseurRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(fournisseurService.creer(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER')")
    public ResponseEntity<?> modifier(@PathVariable Long id, @Valid @RequestBody FournisseurRequestDTO dto) {
        try {
            return ResponseEntity.ok(fournisseurService.modifier(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    public ResponseEntity<?> toggleStatut(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(fournisseurService.toggleStatut(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
