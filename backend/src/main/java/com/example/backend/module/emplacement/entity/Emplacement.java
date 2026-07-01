package com.example.backend.module.emplacement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Un emplacement est la plus petite unité de localisation physique du stock.
 * Hiérarchie : Entrepôt → Zone → Rayon → Étagère → Emplacement
 *
 * Chaque emplacement possède une adresse complète générée automatiquement.
 * Exemple : ENT-001 / ZONE-A / RAYON-03 / ETAGERE-02 / EMP-12
 *
 * Un emplacement peut être :
 *   - LIBRE      : disponible pour accueillir du stock
 *   - OCCUPE     : contient du stock
 *   - RESERVE    : réservé pour une commande en attente
 *   - BLOQUE     : inutilisable (maintenance, dommage…)
 */
@Entity
@Table(
    name = "emplacements",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_emplacement_code_etagere",
        columnNames = {"code", "etagere_id"}
    )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Emplacement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Code de l'emplacement — unique au sein de l'étagère. Ex : "12", "A3" */
    @Column(nullable = false, length = 20)
    private String code;

    /** Adresse complète générée et stockée en base pour les recherches rapides */
    @Column(nullable = false, unique = true, length = 200)
    private String adresseComplete;

    /** Type de contenant : PALETTE, BAC, ETAGERE_OUVERTE, ARMOIRE, SOL */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeEmplacement type;

    /** Statut de disponibilité */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private StatutEmplacement statut = StatutEmplacement.LIBRE;

    /** Capacité maximale en unités (null = illimitée) */
    private Integer capaciteMax;

    /** Description ou contrainte particulière. Ex : "Fragile uniquement", "Froid -18°C" */
    @Column(length = 300)
    private String description;

    /** Étagère parente */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etagere_id", nullable = false)
    private Etagere etagere;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
        // Génère l'adresse complète au moment de la création
        if (this.adresseComplete == null) {
            this.adresseComplete = buildAdresseComplete();
        }
    }

    /**
     * Construit l'adresse complète à partir de la hiérarchie.
     * Ex : ENT-001 / ZONE-A / RAYON-03 / ETAGERE-02 / EMP-12
     */
    public String buildAdresseComplete() {
        Etagere et  = this.etagere;
        Rayon   r   = et.getRayon();
        var     z   = r.getZone();
        var     e   = z.getEntrepot();

        return String.format("%s / %s / RAYON-%s / ETAGERE-%s / EMP-%s",
                e.getNom(), z.getNom(), r.getCode(), et.getCode(), this.code);
    }

    /** Vérifie si cet emplacement est disponible */
    public boolean isDisponible() {
        return statut == StatutEmplacement.LIBRE;
    }

    // -------------------------------------------------------
    // ENUMS
    // -------------------------------------------------------

    public enum TypeEmplacement {
        PALETTE,
        BAC,
        ETAGERE_OUVERTE,
        ARMOIRE,
        SOL
    }

    public enum StatutEmplacement {
        /** Disponible pour accueillir du stock */
        LIBRE,
        /** Contient actuellement du stock */
        OCCUPE,
        /** Réservé pour une commande */
        RESERVE,
        /** Inutilisable (maintenance, dommage…) */
        BLOQUE
    }
}
