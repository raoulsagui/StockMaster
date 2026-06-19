package com.example.backend.module.transfert.entity;

import com.example.backend.module.entrepot.entity.Entrepot;
import com.example.backend.module.produit.entity.Produit;
import com.example.backend.module.utilisateur.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Module 10 — Transfert inter-entrepôts.
 *
 * Représente le déplacement de marchandises entre deux entrepôts.
 * Le flux de stock :
 *   - À l'expédition (EXPEDIE) : retirerStock(TRANSFERT_SORTIE) sur entrepôt source
 *   - À la réception (RECU)    : ajouterStock(TRANSFERT_ENTREE) sur entrepôt destination
 *
 * États :
 *   BROUILLON → créé, pas encore expédié
 *   EXPEDIE   → stock source déduit, en transit
 *   RECU      → stock destination crédité, transfert terminé
 *   ANNULE    → annulé (uniquement depuis BROUILLON)
 */
@Entity
@Table(name = "transferts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transfert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Référence unique du transfert.
     * Format : TRF-YYYYMM-XXXXX
     */
    @Column(nullable = false, unique = true)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutTransfert statut = StatutTransfert.BROUILLON;

    /** Produit transféré */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    /** Entrepôt source (expéditeur) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrepot_source_id", nullable = false)
    private Entrepot entrepotSource;

    /** Entrepôt destination (récepteur) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrepot_destination_id", nullable = false)
    private Entrepot entrepotDestination;

    /** Quantité à transférer */
    @Column(nullable = false)
    private Integer quantite;

    /** Note libre */
    @Column(length = 500)
    private String note;

    /** Utilisateur qui a créé le transfert */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createur_id")
    private Utilisateur createur;

    /** Utilisateur qui a expédié */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediteur_id")
    private Utilisateur expediteur;

    /** Utilisateur qui a réceptionné */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recepteur_id")
    private Utilisateur recepteur;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    /** Date d'expédition */
    private LocalDateTime dateExpedition;

    /** Date de réception */
    private LocalDateTime dateReception;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }

    // -------------------------------------------------------
    // ENUM
    // -------------------------------------------------------

    public enum StatutTransfert {
        BROUILLON,
        EXPEDIE,
        RECU,
        ANNULE
    }
}
