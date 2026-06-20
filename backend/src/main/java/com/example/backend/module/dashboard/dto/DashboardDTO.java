package com.example.backend.module.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO du tableau de bord.
 *
 * Contient les KPI et les derniers mouvements filtrés
 * selon le périmètre de l'utilisateur connecté.
 *
 * ADMIN          → données globales (tous les entrepôts)
 * GESTIONNAIRE   → entrepôts dont il est membre
 * MAGASINIER     → entrepôts dont il est membre
 * AUDITEUR       → entrepôts dont il est membre
 */
@Data
@Builder
public class DashboardDTO {

    /** Nombre d'entrepôts actifs dans le périmètre */
    private long entrepotsActifs;

    /** Nombre total d'entrepôts dans le périmètre */
    private long entrepotsTotal;

    /** Nombre de références produit suivies dans le périmètre */
    private long referencesEnStock;

    /** Total des unités disponibles dans le périmètre */
    private long totalUnitesDispo;

    /** Nombre de références en stock faible */
    private long stocksEnAlerte;

    /** Nombre d'utilisateurs actifs (ADMIN uniquement, sinon -1) */
    private long utilisateursActifs;

    /** Nombre d'entrées validées ce mois */
    private long entreesduMois;

    /** Nombre de sorties validées ce mois */
    private long sortiesDuMois;

    /** Derniers mouvements de stock (5 par défaut) */
    private List<MouvementResume> derniersMouvements;

    /** Stocks en alerte pour affichage rapide (5 max) */
    private List<AlerteStock> alertesRecentes;

    /** Données pour le graphique évolution du stock (30 derniers jours) */
    private List<PointGraphique> evolutionStock;

    /** Données pour le graphique rotation des produits (top 5) */
    private List<RotationProduit> rotationProduits;

    /** true si les données sont filtrées par entrepôt (non-ADMIN) */
    private boolean filtrePeriemtre;

    // -------------------------------------------------------
    // DTOs internes
    // -------------------------------------------------------

    @Data
    @Builder
    public static class MouvementResume {
        private Long   id;
        private String type;
        private String produitNom;
        private String entrepotNom;
        private int    quantite;
        private LocalDateTime dateCreation;
    }

    @Data
    @Builder
    public static class AlerteStock {
        private Long   stockId;
        private String produitNom;
        private String entrepotNom;
        private int    quantiteDisponible;
        private Integer stockMinimum;
    }

    /** Un point du graphique évolution : date + entrées + sorties */
    @Data
    @Builder
    public static class PointGraphique {
        private String date;    // "2026-06-01"
        private long   entrees;
        private long   sorties;
    }

    /** Un produit dans le graphique rotation */
    @Data
    @Builder
    public static class RotationProduit {
        private String produitNom;
        private long   totalMouvements;
    }
}
