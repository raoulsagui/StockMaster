package com.example.backend.module.entrepot.entity;

import com.example.backend.module.utilisateur.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité JPA représentant un entrepôt.
 *
 * Un entrepôt est le conteneur physique principal du système de stockage.
 * Il possède une capacité totale (en m² ou en unités selon le paramétrage),
 * et sa capacité utilisée est calculée à partir des zones qu'il contient.
 *
 * Relation avec Zone : un entrepôt peut contenir plusieurs zones (OneToMany).
 * On utilise CascadeType.ALL pour que la suppression d'un entrepôt
 * supprime aussi toutes ses zones (orphanRemoval = true confirme ça).
 *
 * Relation avec Utilisateur (responsable) : ManyToOne — plusieurs entrepôts
 * peuvent avoir le même responsable.
 */
@Entity
@Table(name = "entrepots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Entrepot {

    /**
     * Clé primaire auto-générée.
     * IDENTITY : la BDD gère l'incrémentation (compatible H2 et PostgreSQL).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom de l'entrepôt — doit être unique dans le système.
     * Ex : "Entrepôt Principal Paris", "Dépôt Sud Lyon"
     */
    @Column(nullable = false, unique = true)
    private String nom;

    /**
     * Adresse physique complète de l'entrepôt.
     * Ex : "15 Rue de la Logistique, 75001 Paris"
     */
    @Column(nullable = false)
    private String adresse;

    /**
     * Capacité totale de l'entrepôt, exprimée en mètres carrés.
     * Doit être un nombre positif.
     */
    @Column(nullable = false)
    private Double capaciteTotale;

    /**
     * Capacité actuellement utilisée, en mètres carrés.
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
     * Responsable de l'entrepôt.
     * Relation ManyToOne : plusieurs entrepôts peuvent avoir le même responsable.
     *
     * @ManyToOne LAZY → le responsable n'est chargé depuis la BDD que si on y accède.
     *            Meilleure performance que EAGER qui charge tout d'un coup.
     *
     * @JoinColumn → nom de la colonne FK dans la table "entrepots"
     * nullable = true → un entrepôt peut exister sans responsable assigné
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id", nullable = true)
    private Utilisateur responsable;

    /**
     * Liste des zones de stockage de cet entrepôt.
     *
     * @OneToMany mappedBy = "entrepot" → la relation est gérée côté Zone
     * cascade = ALL → toute opération sur l'entrepôt se propage aux zones
     * orphanRemoval = true → si une zone est retirée de la liste, elle est supprimée en BDD
     *
     * @Builder.Default → Lombok a besoin de ça pour initialiser la liste avec le Builder
     */
    @Builder.Default
    @OneToMany(mappedBy = "entrepot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<com.example.backend.module.zone.entity.Zone> zones = new ArrayList<>();

    /**
     * Date de création enregistrée automatiquement au premier INSERT.
     * updatable = false : ce champ ne peut pas être modifié après création.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    /**
     * Callback JPA : appelé AVANT chaque insertion en BDD.
     * Permet d'initialiser dateCreation sans avoir à le faire manuellement.
     */
    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }

    // -------------------------------------------------------
    // MÉTHODES MÉTIER
    // Ces méthodes encapsulent la logique propre à l'entrepôt.
    // -------------------------------------------------------

    /**
     * Calcule le taux d'occupation de l'entrepôt en pourcentage.
     * Retourne 0 si la capacité totale est nulle (évite la division par zéro).
     *
     * @return taux entre 0.0 et 100.0
     */
    public double getTauxOccupation() {
        if (capaciteTotale == null || capaciteTotale == 0) return 0.0;
        return (capaciteUtilisee / capaciteTotale) * 100.0;
    }

    /**
     * Capacité disponible restante.
     *
     * @return capacité libre en m²
     */
    public double getCapaciteDisponible() {
        return capaciteTotale - capaciteUtilisee;
    }
}
