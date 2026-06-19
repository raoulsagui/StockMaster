package com.example.backend.module.inventaire.entity;

import com.example.backend.module.entrepot.entity.Entrepot;
import com.example.backend.module.utilisateur.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un inventaire physique d'un entrepôt.
 *
 * Un inventaire est l'opération de comptabilisation réelle des marchandises
 * présentes dans un entrepôt, afin de les comparer avec les quantités
 * théoriques enregistrées dans le système.
 *
 * Cycle de vie :
 *   BROUILLON  → inventaire créé mais pas encore commencé
 *   EN_COURS   → comptage en cours (lignes peuvent être modifiées)
 *   VALIDE     → inventaire clôturé, ajustements de stock appliqués
 *   ANNULE     → inventaire abandonné (aucun ajustement appliqué)
 *
 * Types d'inventaire :
 *   COMPLET  → tous les produits de l'entrepôt sont comptabilisés
 *   PARTIEL  → seul un sous-ensemble de produits est sélectionné
 */
@Entity
@Table(name = "inventaires")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Référence unique générée automatiquement.
     * Format : INV-AAAA-XXXXX (ex: INV-2026-00001)
     */
    @Column(nullable = false, unique = true)
    private String reference;

    /**
     * Type d'inventaire : COMPLET ou PARTIEL.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeInventaire type;

    /**
     * Statut courant de l'inventaire.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutInventaire statut = StatutInventaire.BROUILLON;

    /**
     * Entrepôt sur lequel porte l'inventaire.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrepot_id", nullable = false)
    private Entrepot entrepot;

    /**
     * Utilisateur ayant créé l'inventaire.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createur_id", nullable = false)
    private Utilisateur createur;

    /**
     * Utilisateur ayant validé l'inventaire.
     * Null tant que le statut n'est pas VALIDE.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valideur_id")
    private Utilisateur valideur;

    /**
     * Date planifiée de l'inventaire.
     */
    @Column(nullable = false)
    private LocalDate datePrevue;

    /**
     * Date de validation effective.
     * Renseignée lors du passage au statut VALIDE.
     */
    private LocalDateTime dateValidation;

    /**
     * Note ou commentaire global sur l'inventaire.
     */
    @Column(length = 1000)
    private String note;

    /**
     * Lignes de l'inventaire : une ligne par produit comptabilisé.
     */
    @Builder.Default
    @OneToMany(mappedBy = "inventaire", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneInventaire> lignes = new ArrayList<>();

    /**
     * Date de création de l'enregistrement.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }

    // -------------------------------------------------------
    // MÉTHODES MÉTIER
    // -------------------------------------------------------

    /**
     * Nombre total de lignes avec un écart (positif ou négatif).
     */
    public long getNombreLignesAvecEcart() {
        return lignes.stream()
                .filter(l -> l.getEcart() != 0)
                .count();
    }

    /**
     * Nombre de lignes avec écart positif (plus de stock réel que théorique).
     */
    public long getNombreLignesEcartPositif() {
        return lignes.stream()
                .filter(l -> l.getEcart() > 0)
                .count();
    }

    /**
     * Nombre de lignes avec écart négatif (moins de stock réel que théorique).
     */
    public long getNombreLignesEcartNegatif() {
        return lignes.stream()
                .filter(l -> l.getEcart() < 0)
                .count();
    }

    /**
     * Indique si tous les produits de l'inventaire ont été comptés.
     */
    public boolean estComplet() {
        return lignes.stream().allMatch(LigneInventaire::isComptee);
    }

    // -------------------------------------------------------
    // ENUMS
    // -------------------------------------------------------

    public enum TypeInventaire {
        /** Tous les produits de l'entrepôt sont inventoriés */
        COMPLET,
        /** Seulement certains produits sélectionnés */
        PARTIEL
    }

    public enum StatutInventaire {
        /** Inventaire créé, comptage non démarré */
        BROUILLON,
        /** Comptage en cours */
        EN_COURS,
        /** Inventaire clôturé, ajustements appliqués */
        VALIDE,
        /** Inventaire abandonné */
        ANNULE
    }
}
