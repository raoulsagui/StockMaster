package com.example.backend.module.entree.entity;

import com.example.backend.module.entrepot.entity.Entrepot;
import com.example.backend.module.fournisseur.entity.Fournisseur;
import com.example.backend.module.produit.entity.Produit;
import com.example.backend.module.utilisateur.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Module 8 — Bon de réception (entrée de stock).
 *
 * Représente la réception physique de marchandises d'un fournisseur
 * dans un entrepôt. À la validation, le stock correspondant est
 * automatiquement mis à jour via StockService.ajouterStock().
 *
 * États possibles :
 *   BROUILLON  → créé mais pas encore validé
 *   VALIDE     → stock mis à jour, entrée définitive
 *   ANNULE     → annulée avant validation
 */
@Entity
@Table(name = "entrees")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Entree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Numéro de bon de réception unique.
     * Format : BON-YYYYMM-XXXXX (généré automatiquement).
     */
    @Column(nullable = false, unique = true)
    private String reference;

    /** État du bon de réception */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutEntree statut = StatutEntree.BROUILLON;

    /** Produit reçu */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    /** Entrepôt de destination */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrepot_id", nullable = false)
    private Entrepot entrepot;

    /** Fournisseur source (optionnel — peut être une entrée interne) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    /** Quantité réceptionnée */
    @Column(nullable = false)
    private Integer quantite;

    /** Prix unitaire d'achat lors de cette réception */
    @Column(precision = 10, scale = 2)
    private java.math.BigDecimal prixUnitaire;

    /** Note libre sur la réception (état des marchandises, remarques…) */
    @Column(length = 500)
    private String note;

    /** Utilisateur qui a créé le bon */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createur_id")
    private Utilisateur createur;

    /** Utilisateur qui a validé le bon (null si pas encore validé) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validateur_id")
    private Utilisateur validateur;

    /** Date/heure de création */
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    /** Date/heure de validation (null si pas encore validé) */
    private LocalDateTime dateValidation;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }

    // -------------------------------------------------------
    // ENUM
    // -------------------------------------------------------

    public enum StatutEntree {
        BROUILLON,
        VALIDE,
        ANNULE
    }
}
