package com.example.backend.module.alerte.entity;

import com.example.backend.module.produit.entity.Produit;
import com.example.backend.module.stock.entity.Stock;
import com.example.backend.module.zone.entity.Zone;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entité représentant une alerte automatique générée par le système.
 *
 * Trois types d'alertes sont gérés :
 *   - STOCK_FAIBLE    : quantité disponible ≤ seuil minimum configuré
 *   - PRODUIT_EXPIRE  : date de péremption dépassée ou imminente (à venir)
 *   - ZONE_SATUREE    : taux d'occupation d'une zone ≥ seuil critique
 *
 * Cycle de vie d'une alerte :
 *   NON_LUE → LUE → RESOLUE
 *
 * Le système évite les doublons : une alerte du même type sur le même
 * objet ne sera pas recréée si elle est déjà NON_LUE ou LUE.
 */
@Entity
@Table(name = "alertes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alerte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Type fonctionnel de l'alerte */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeAlerte type;

    /** Niveau de criticité */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severite severite;

    /** Statut de lecture/résolution */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private StatutAlerte statut = StatutAlerte.NON_LUE;

    /** Message descriptif de l'alerte */
    @Column(nullable = false, length = 500)
    private String message;

    /**
     * Stock concerné (pour STOCK_FAIBLE).
     * Null pour les autres types.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    /**
     * Zone concernée (pour ZONE_SATUREE).
     * Null pour les autres types.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    /**
     * Valeur numérique au moment du déclenchement.
     * Ex : quantité disponible pour STOCK_FAIBLE, taux % pour ZONE_SATUREE.
     */
    private Double valeurActuelle;

    /**
     * Seuil configuré qui a déclenché l'alerte.
     * Ex : stockMinimum, seuil de saturation.
     */
    private Double seuil;

    /** Un email a déjà été envoyé pour cette alerte */
    @Builder.Default
    @Column(nullable = false)
    private boolean emailEnvoye = false;

    /** Date de création de l'alerte */
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    /** Date de lecture */
    private LocalDateTime dateLecture;

    /** Date de résolution */
    private LocalDateTime dateResolution;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }

    // -------------------------------------------------------
    // ENUMS
    // -------------------------------------------------------

    public enum TypeAlerte {
        STOCK_FAIBLE,
        PRODUIT_EXPIRE,
        ZONE_SATUREE
    }

    public enum Severite {
        /** Information — à surveiller */
        INFO,
        /** Avertissement — action recommandée */
        WARNING,
        /** Critique — action immédiate requise */
        CRITIQUE
    }

    public enum StatutAlerte {
        NON_LUE,
        LUE,
        RESOLUE
    }
}
