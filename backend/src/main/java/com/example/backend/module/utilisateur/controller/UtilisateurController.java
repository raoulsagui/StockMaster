package com.example.backend.module.utilisateur.controller;

import com.example.backend.module.utilisateur.dto.ChangerMotDePasseRequestDTO;
import com.example.backend.module.utilisateur.dto.UtilisateurRequestDTO;
import com.example.backend.module.utilisateur.dto.UtilisateurResponseDTO;
import com.example.backend.module.utilisateur.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST du module Utilisateurs.
 *
 * Routes protégées (token JWT requis) :
 *   GET    /api/utilisateurs              → liste tous les utilisateurs
 *   GET    /api/utilisateurs/{id}         → détail d'un utilisateur
 *   POST   /api/utilisateurs              → créer un utilisateur (ADMIN)
 *   PUT    /api/utilisateurs/{id}         → modifier un utilisateur (ADMIN)
 *   PATCH  /api/utilisateurs/{id}/statut  → activer/désactiver (ADMIN)
 *   POST   /api/utilisateurs/{id}/reinitialiser-mdp → reset mdp (ADMIN)
 *
 * @PreAuthorize("hasRole('ADMIN')") → seul l'ADMIN peut faire cette action.
 * Activé par @EnableMethodSecurity dans SecurityConfig.
 *
 * ResponseEntity<T> → permet de contrôler le code HTTP de la réponse
 *   ResponseEntity.ok(data)         → 200 OK avec body
 *   ResponseEntity.status(201).body → 201 Created avec body
 *   ResponseEntity.noContent()      → 204 No Content
 */
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    /**
     * GET /api/utilisateurs
     * Tous les rôles peuvent voir la liste des utilisateurs.
     */
    @GetMapping
    public ResponseEntity<List<UtilisateurResponseDTO>> findAll() {
        return ResponseEntity.ok(utilisateurService.findAll());
    }

    /**
     * GET /api/utilisateurs/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.findById(id));
    }

    /**
     * POST /api/utilisateurs
     * Création réservée aux ADMIN.
     * @Valid → valide les annotations du DTO avant d'entrer dans la méthode
     * @ResponseStatus(201) → retourne 201 Created (convention REST pour création)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponseDTO> creer(@Valid @RequestBody UtilisateurRequestDTO dto) {
        UtilisateurResponseDTO created = utilisateurService.creer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/utilisateurs/{id}
     * Modification réservée aux ADMIN.
     * PUT = remplacement complet (on envoie toutes les données).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponseDTO> modifier(
            @PathVariable Long id,
            @Valid @RequestBody UtilisateurRequestDTO dto
    ) {
        return ResponseEntity.ok(utilisateurService.modifier(id, dto));
    }

    /**
     * PATCH /api/utilisateurs/{id}/statut
     * Active ou désactive un compte.
     * PATCH = modification partielle (pas besoin d'envoyer tout le body).
     */
    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponseDTO> toggleStatut(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.toggleStatut(id));
    }

    /**
     * POST /api/utilisateurs/{id}/reinitialiser-mdp
     * Réinitialise le mot de passe (envoi d'email à venir).
     */
    @PostMapping("/{id}/reinitialiser-mdp")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reinitialiserMotDePasse(@PathVariable Long id) {
        utilisateurService.reinitialiserMotDePasse(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/utilisateurs/{id}/changer-mot-de-passe
     * Permet à un utilisateur de changer son propre mot de passe.
     * Accessible par tous les rôles (chaque user gère son propre compte).
     * Après ce changement, doitChangerMotDePasse passe à false.
     */
    @PostMapping("/{id}/changer-mot-de-passe")
    public ResponseEntity<?> changerMotDePasse(
            @PathVariable Long id,
            @Valid @RequestBody ChangerMotDePasseRequestDTO dto
    ) {
        // Vérification côté controller que les deux mots de passe correspondent
        if (!dto.getNouveauMotDePasse().equals(dto.getConfirmerMotDePasse())) {
            return ResponseEntity.badRequest().body("Les mots de passe ne correspondent pas");
        }
        try {
            utilisateurService.changerMotDePasse(id, dto.getAncienMotDePasse(), dto.getNouveauMotDePasse());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
