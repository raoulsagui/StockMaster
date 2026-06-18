package com.example.backend.module.stock.entity;

import com.example.backend.module.entrepot.entity.Entrepot;
import com.example.backend.module.produit.entity.Produit;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entité JPA représentant le stock d'un produit dans un entrepôt.
 *
 * Un Stock est l'intersection entre un Produit et un Entrepôt.
 * On a une ligne de stock par combinaison (produit, entrepôt).
 *
 * Exemple :
 *   Laptop Dell → Entrepôt Paris : 50 unités disponibles, 10 réservées
 *   Laptop Dell → Entrepôt Lyon  : 20 unités disponibles, 0 réservées
 *
 * Quantités gérées :
 *   - quantiteDisponible : unités libres, prêtes à être sorties
 *   - quantiteReservee   : réservées pour des commandes en attente
 *   - quantiteEnTransit  : en cours de transfert depuis/vers un autre entrepôt
 *
 * La quantité totale = disponible + réservée + en transit.
 *
 * Contrainte unique sur (produit, entrepôt) pour éviter les doublons.
 */
@Entity
@Table(
    name = "stocks",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_stock_produit_entrepot",
        columnNames = { "produit_id", "entrepot_id" }
    )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Le produit concerné par ce stock.
     * LAZY : on ne charge le produit que si on y accède.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    /**
     * L'entrepôt où se trouve ce stock.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrepot_id", nullable = false)
    private Entrepot entrepot;

    /**
     * Quantité disponible : unités libres, prêtes à sortir.
     * Mise à jour lors des entrées et sorties de stock.
     */
    @Builder.Default
    @Column(nullable = false)
    private Integer quantiteDisponible = 0;

    /**
     * Quantité réservée pour des commandes validées mais pas encore expédiées.
     * Déduite de la disponible lors d'une réservation.
     */
    @Builder.Default
    @Column(nullable = false)
    private Integer quantiteReservee = 0;

    /**
     * Quantité en cours de transfert (expédiée mais pas encore reçue).
     */
    @Builder.Default
    @Column(nullable = false)
    private Integer quantiteEnTransit = 0;

    /**
     * Seuil d'alerte stock faible.
     * Si quantiteDisponible <= stockMinimum, une alerte est déclenchée.
     * Null = pas d'alerte configurée.
     */
    private Integer stockMinimum;

    /**
     * Capacité maximale conseillée.
     * Sert d'indicateur de sur-stockage.
     * Null = pas de maximum configuré.
     */
    private Integer stockMaximum;

    /**
     * Date de la dernière mise à jour du stock.
     * Mise à jour automatiquement à chaque modification.
     */
    @Column(nullable = false)
    private LocalDateTime derniereMaj;

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.derniereMaj = LocalDateTime.now();
    }

    // -------------------------------------------------------
    // MÉTHODES MÉTIER
    // -------------------------------------------------------

    /**
     * Quantité totale physique dans l'entrepôt
     * (disponible + réservée, hors transit car pas encore là).
     */
    public int getQuantiteTotale() {
        return quantiteDisponible + quantiteReservee;
    }

    /**
     * Vérifie si le stock est en dessous du seuil minimum.
     * Utilisé par le module 12 (alertes automatiques).
     */
    public boolean estEnStockFaible() {
        return stockMinimum != null && quantiteDisponible <= stockMinimum;
    }

    /**
     * Vérifie si le stock dépasse le maximum configuré.
     */
    public boolean estEnSurStock() {
        return stockMaximum != null && getQuantiteTotale() > stockMaximum;
    }
}
