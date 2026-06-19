package com.example.backend.module.sortie.entity;

import com.example.backend.module.entrepot.entity.Entrepot;
import com.example.backend.module.produit.entity.Produit;
import com.example.backend.module.utilisateur.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Module 9 — Bon de sortie.
 *
 * Représente une sortie de marchandises depuis un entrepôt
 * (livraison client, transfert sortant, casse, etc.).
 * À la validation, le stock est automatiquement décrémenté
 * via StockService.retirerStock().
 *
 * États :
 *   BROUILLON → créé, pas encore validé
 *   VALIDE    → stock déduit, sortie définitive
 *   ANNULE    → annulée avant validation
 */
@Entity
@Table(name = "sorties")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sortie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Numéro de bon de sortie unique.
     * Format : SOR-YYYYMM-XXXXX
     */
    @Column(nullable = false, unique = true)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutSortie statut = StatutSortie.BROUILLON;

    /** Produit sorti */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    /** Entrepôt source */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrepot_id", nullable = false)
    private Entrepot entrepot;

    /** Quantité sortie */
    @Column(nullable = false)
    private Integer quantite;

    /** Motif de la sortie */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MotifSortie motif = MotifSortie.LIVRAISON;

    /** Destinataire ou référence client (optionnel) */
    @Column(length = 255)
    private String destinataire;

    /** Note libre */
    @Column(length = 500)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createur_id")
    private Utilisateur createur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validateur_id")
    private Utilisateur validateur;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateValidation;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }

    // -------------------------------------------------------
    // ENUMS
    // -------------------------------------------------------

    public enum StatutSortie {
        BROUILLON,
        VALIDE,
        ANNULE
    }

    public enum MotifSortie {
        LIVRAISON,
        RETOUR_FOURNISSEUR,
        CASSE,
        PERTE,
        AUTRE
    }
}
