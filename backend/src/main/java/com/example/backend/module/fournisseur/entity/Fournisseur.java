package com.example.backend.module.fournisseur.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entité JPA représentant un fournisseur.
 *
 * Un fournisseur est un partenaire commercial qui livre des produits.
 * Il est lié aux commandes d'approvisionnement (module commandes).
 */
@Entity
@Table(name = "fournisseurs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom de la société fournisseur — unique dans le système. */
    @Column(nullable = false, unique = true)
    private String nom;

    /** Adresse physique ou postale du fournisseur. */
    @Column(nullable = false)
    private String adresse;

    /** Numéro de téléphone principal. */
    @Column(nullable = false)
    private String telephone;

    /** Adresse email principale. */
    @Column(nullable = false, unique = true)
    private String email;

    /** Nom de la personne de contact principal chez le fournisseur. */
    @Column(nullable = false)
    private String contactPrincipal;

    /** Statut du fournisseur. Un fournisseur inactif ne peut plus être sélectionné. */
    @Builder.Default
    @Column(nullable = false)
    private boolean actif = true;

    /** Date de création — initialisée automatiquement avant le premier INSERT. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }
}
