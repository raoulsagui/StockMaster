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
 * Chaque zone a sa propre capacité et son taux d'occupation.
 * La somme des capacités des zones ne doit pas dépasser la capacité de l'entrepôt.
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
     * Capacité totale de la zone en mètres cubes (m³).
     * Représente la surface physique disponible.
     * Null = capacité non définie (zone sans limite mesurée).
     */
    private Double capaciteTotale;

    /**
     * Capacité actuellement utilisée en mètres cubes (m³).
     * Mise à jour lors des mouvements de stock.
     */
    @Builder.Default
    private Double capaciteUtilisee = 0.0;

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
     * Taux d'occupation de la zone en pourcentage (0–100).
     * Retourne 0 si la capacité totale n'est pas définie ou est nulle.
     */
    public double getTauxOccupation() {
        if (capaciteTotale == null || capaciteTotale == 0) return 0.0;
        double utilise = capaciteUtilisee != null ? capaciteUtilisee : 0.0;
        return Math.min((utilise / capaciteTotale) * 100.0, 100.0);
    }

    /**
     * Capacité disponible restante en m³.
     * Retourne null si la capacité totale n'est pas définie.
     */
    public Double getCapaciteDisponible() {
        if (capaciteTotale == null) return null;
        double utilise = capaciteUtilisee != null ? capaciteUtilisee : 0.0;
        return Math.max(capaciteTotale - utilise, 0.0);
    }
}
