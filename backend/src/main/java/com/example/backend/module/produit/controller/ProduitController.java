package com.example.backend.module.produit.controller;

import com.example.backend.module.produit.dto.ProduitRequestDTO;
import com.example.backend.module.produit.dto.ProduitResponseDTO;
import com.example.backend.module.produit.service.ProduitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produits")
@RequiredArgsConstructor
public class ProduitController {

    private final ProduitService produitService;

    @GetMapping
    public ResponseEntity<List<ProduitResponseDTO>> findAll() {
        return ResponseEntity.ok(produitService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProduitResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER')")
    public ResponseEntity<ProduitResponseDTO> creer(@Valid @RequestBody ProduitRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produitService.creer(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER')")
    public ResponseEntity<ProduitResponseDTO> modifier(@PathVariable Long id, @Valid @RequestBody ProduitRequestDTO dto) {
        return ResponseEntity.ok(produitService.modifier(id, dto));
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    public ResponseEntity<ProduitResponseDTO> toggleActif(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.toggleActif(id));
    }
}
