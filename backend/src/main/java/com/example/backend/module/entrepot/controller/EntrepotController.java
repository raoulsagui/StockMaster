package com.example.backend.module.entrepot.controller;

import com.example.backend.module.entrepot.dto.EntrepotRequestDTO;
import com.example.backend.module.entrepot.dto.EntrepotResponseDTO;
import com.example.backend.module.entrepot.service.EntrepotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST du module Entrepôts.
 *
 * Routes exposées :
 *   GET    /api/entrepots                → liste tous les entrepôts (tous rôles)
 *   GET    /api/entrepots/actifs         → liste des entrepôts actifs (tous rôles)
 *   GET    /api/entrepots/{id}           → détail d'un entrepôt (tous rôles)
 *   POST   /api/entrepots                → créer un entrepôt (ADMIN, GESTIONNAIRE)
 *   PUT    /api/entrepots/{id}           → modifier un entrepôt (ADMIN, GESTIONNAIRE)
 *   PATCH  /api/entrepots/{id}/statut    → activer/désactiver (ADMIN)
 *
 * Politique d'accès :
 *   - Lecture : tous les utilisateurs authentifiés
 *   - Création/Modification : ADMIN et GESTIONNAIRE
 *   - Désactivation : ADMIN uniquement
 *
 * Les erreurs métier (nom dupliqué, entrepôt introuvable…) sont propagées
 * depuis le service sous forme de RuntimeException et gérées globalement
 * par Spring Boot (retourne 500 par défaut — à améliorer avec @ControllerAdvice).
 */
@RestController
@RequestMapping("/api/entrepots")
@RequiredArgsConstructor
public class EntrepotController {

    private final EntrepotService entrepotService;

    /**
     * GET /api/entrepots
     * Retourne la liste complète des entrepôts avec leurs métriques.
     * Accessible par tous les utilisateurs authentifiés.
     */
    @GetMapping
    public ResponseEntity<List<EntrepotResponseDTO>> findAll() {
        return ResponseEntity.ok(entrepotService.findAll());
    }

    /**
     * GET /api/entrepots/actifs
     * Retourne uniquement les entrepôts actifs.
     * Utilisé lors de la création d'une zone pour proposer les entrepôts disponibles.
     */
    @GetMapping("/actifs")
    public ResponseEntity<List<EntrepotResponseDTO>> findActifs() {
        return ResponseEntity.ok(entrepotService.findActifs());
    }

    /**
     * GET /api/entrepots/{id}
     * Retourne le détail d'un entrepôt par son ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EntrepotResponseDTO> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(entrepotService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/entrepots
     * Crée un nouvel entrepôt.
     *
     * @Valid       → déclenche la validation Bean Validation sur le DTO
     * @RequestBody → lit le corps JSON et le convertit en EntrepotRequestDTO
     *
     * Accès : ADMIN ou GESTIONNAIRE d'entrepôt.
     * Retourne 201 Created si succès, 400 si données invalides, 409 si nom dupliqué.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER')")
    public ResponseEntity<?> creer(@Valid @RequestBody EntrepotRequestDTO dto) {
        try {
            EntrepotResponseDTO created = entrepotService.creer(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            // Erreur métier (nom dupliqué, responsable inexistant…)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * PUT /api/entrepots/{id}
     * Modifie un entrepôt existant (remplacement complet des champs).
     *
     * Accès : ADMIN ou GESTIONNAIRE.
     * Retourne 200 OK si succès, 404 si non trouvé, 409 si conflit.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER')")
    public ResponseEntity<?> modifier(
            @PathVariable Long id,
            @Valid @RequestBody EntrepotRequestDTO dto
    ) {
        try {
            return ResponseEntity.ok(entrepotService.modifier(id, dto));
        } catch (RuntimeException e) {
            // On retourne le message d'erreur métier au client
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * PATCH /api/entrepots/{id}/statut
     * Active ou désactive un entrepôt.
     *
     * Accès : ADMIN uniquement.
     * Retourne 200 OK avec le nouvel état de l'entrepôt.
     */
    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    public ResponseEntity<?> toggleStatut(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(entrepotService.toggleStatut(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/entrepots/mes-entrepots
     * Retourne les entrepôts de l'utilisateur connecté.
     * ADMIN → tous, autres → uniquement leurs entrepôts assignés.
     */
    @GetMapping("/mes-entrepots")
    public ResponseEntity<List<EntrepotResponseDTO>> mesEntrepots() {
        return ResponseEntity.ok(entrepotService.findMesEntrepots());
    }

    /**
     * PATCH /api/entrepots/{id}/membres
     * Met à jour la liste des membres assignés à un entrepôt.
     * Accès : ADMIN uniquement.
     *
     * Body : [1, 2, 3] (liste d'IDs utilisateurs)
     */
    @PatchMapping("/{id}/membres")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> mettreAJourMembres(
            @PathVariable Long id,
            @RequestBody List<Long> membresIds) {
        try {
            return ResponseEntity.ok(entrepotService.mettreAJourMembres(id, membresIds));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
