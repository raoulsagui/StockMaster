package com.example.backend.module.entrepot.entity;

import com.example.backend.module.utilisateur.entity.Utilisateur;
import com.example.backend.module.zone.entity.Zone;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entité JPA représentant un entrepôt.
 *
 * Un entrepôt est le conteneur physique principal du système de stockage.
 * Il possède une capacité totale et une capacité utilisée (en m³).
 * La capacité utilisée est mise à jour lors des mouvements de stock.
 *
 * Relation avec Zone     : un entrepôt peut contenir plusieurs zones (OneToMany).
 * Relation avec Utilisateur : ManyToOne pour le responsable.
 */
@Entity
@Table(name = "entrepots")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Entrepot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom de l'entrepôt — doit être unique dans le système.
     */
    @Column(nullable = false, unique = true)
    private String nom;

    /**
     * Adresse physique complète de l'entrepôt.
     */
    @Column(nullable = false)
    private String adresse;

    /**
     * Capacité totale de l'entrepôt en mètres carrés.
     */
    @Column(nullable = false)
    private Double capaciteTotale;

    /**
     * Capacité actuellement utilisée en mètres carrés.
     * Mise à jour lors des mouvements de stock.
     * Ne peut pas dépasser capaciteTotale.
     */
    @Builder.Default
    @Column(nullable = false)
    private Double capaciteUtilisee = 0.0;

    /**
     * Statut de l'entrepôt.
     * Un entrepôt inactif ne peut plus recevoir de nouvelles zones ou de stock.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean actif = true;

    /**
     * Responsable principal de l'entrepôt (optionnel).
     * C'est le gestionnaire en chef de cet entrepôt.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id", nullable = true)
    private Utilisateur responsable;

    /**
     * Membres assignés à cet entrepôt (gestionnaires et magasiniers).
     * Ces utilisateurs voient cet entrepôt dans leur dashboard.
     *
     * Table de liaison : entrepot_utilisateurs (entrepot_id, utilisateur_id)
     * Un utilisateur peut être assigné à plusieurs entrepôts.
     * Un entrepôt peut avoir plusieurs membres.
     */
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "entrepot_utilisateurs",
        joinColumns = @JoinColumn(name = "entrepot_id"),
        inverseJoinColumns = @JoinColumn(name = "utilisateur_id")
    )
    private Set<Utilisateur> membres = new HashSet<>();

    /**
     * Liste des zones de stockage de cet entrepôt.
     */
    @Builder.Default
    @OneToMany(mappedBy = "entrepot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Zone> zones = new ArrayList<>();

    /**
     * Date de création — initialisée automatiquement avant le premier INSERT.
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
     * Taux d'occupation de l'entrepôt en pourcentage.
     *
     * @return taux entre 0.0 et 100.0
     */
    public double getTauxOccupation() {
        if (capaciteTotale == null || capaciteTotale == 0) return 0.0;
        return (capaciteUtilisee / capaciteTotale) * 100.0;
    }

    /**
     * Capacité disponible restante en m³.
     *
     * @return capacité libre
     */
    public double getCapaciteDisponible() {
        return capaciteTotale - capaciteUtilisee;
    }
}
