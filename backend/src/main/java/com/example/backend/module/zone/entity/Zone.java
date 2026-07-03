package com.example.backend.module.zone.entity;

import com.example.backend.module.entrepot.entity.Entrepot;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entité JPA représentant une zone de stockage à l'intérieur d'un entrepôt.
 *
 * Un entrepôt est découpé en zones selon leur fonction logistique :
 *   - RECEPTION  : zone de déchargement et contrôle des marchandises entrantes
 *   - STOCKAGE   : zone de conservation des produits
 *   - EXPEDITION : zone de préparation et chargement des commandes sortantes
 *
 * Chaque zone ne tracke que sa capacité utilisée.
 * La somme des capacités utilisées de toutes les zones ne doit pas dépasser
 * la capacité totale de l'entrepôt parent.
 */
@Entity
@Table(name = "zones")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Zone {

    /**
     * Clé primaire auto-générée.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom de la zone, unique au sein d'un même entrepôt.
     * Ex : "Zone A - Réception", "Allée 1 - Stockage froid"
     */
    @Column(nullable = false)
    private String nom;

    /**
     * Type fonctionnel de la zone.
     *
     * @Enumerated(EnumType.STRING) → stocké en texte ("RECEPTION", "STOCKAGE"…)
     * au lieu d'un entier, ce qui est plus lisible en base de données.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeZone type;

    /**
     * Description optionnelle de la zone.
     * Ex : "Zone réfrigérée pour produits frais", "Rayonnages haute densité"
     */
    @Column(length = 500)
    private String description;

    /**
     * Statut de la zone.
     * Une zone inactive n'accepte plus de nouveau stock.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean actif = true;

    /**
     * Capacité allouée à cette zone en m³.
     * La somme des capacités de toutes les zones d'un entrepôt
     * doit égaler la capacité totale de l'entrepôt.
     */
    @Column(nullable = false)
    private Double capacite;

    /**
     * Entrepôt parent auquel appartient cette zone.
     *
     * Côté "Many" de la relation OneToMany avec Entrepot.
     * @JoinColumn → colonne FK "entrepot_id" dans la table "zones"
     * nullable = false → une zone doit toujours appartenir à un entrepôt
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrepot_id", nullable = false)
    private Entrepot entrepot;

    /**
     * Date de création enregistrée automatiquement.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    /**
     * Callback JPA : initialise dateCreation avant le premier INSERT.
     */
    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }

    // -------------------------------------------------------
    // MÉTHODES MÉTIER
    // -------------------------------------------------------

    /**
     * Taux d'occupation de la zone = capacite / capaciteTotale entrepot.
     * Utilisé par AlerteService.
     */
    public double getTauxOccupation() {
        if (entrepot == null || entrepot.getCapaciteTotale() == null || entrepot.getCapaciteTotale() == 0) return 0.0;
        double cap = capacite != null ? capacite : 0.0;
        return Math.min((cap / entrepot.getCapaciteTotale()) * 100.0, 100.0);
    }

    public Double getCapaciteTotale() {
        return capacite;
    }
}
