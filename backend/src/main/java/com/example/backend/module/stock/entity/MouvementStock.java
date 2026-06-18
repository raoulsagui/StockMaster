package com.example.backend.module.stock.entity;

import com.example.backend.module.entrepot.entity.Entrepot;
import com.example.backend.module.produit.entity.Produit;
import com.example.backend.module.utilisateur.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entité représentant un mouvement de stock (historique).
 *
 * Chaque entrée, sortie ou ajustement de stock crée un enregistrement ici.
 * Cela permet de retracer l'historique complet des mouvements.
 *
 * Cette entité est en lecture seule après création (pas de modification).
 * Les modules 8 (entrées) et 9 (sorties) créeront des mouvements
 * qui seront enregistrés ici.
 */
@Entity
@Table(name = "mouvements_stock")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MouvementStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Type de mouvement effectué.
     * Défini par l'enum TypeMouvement ci-dessous.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeMouvement type;

    /** Le produit concerné par le mouvement */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    /** L'entrepôt où le mouvement a eu lieu */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrepot_id", nullable = false)
    private Entrepot entrepot;

    /**
     * Quantité déplacée (toujours positive).
     * Le sens est déterminé par le type de mouvement.
     */
    @Column(nullable = false)
    private Integer quantite;

    /**
     * Stock disponible après le mouvement.
     * Permet de reconstituer l'historique sans recalcul.
     */
    @Column(nullable = false)
    private Integer quantiteApres;

    /**
     * Référence externe optionnelle : numéro de bon, commande, transfert…
     * Permet de relier le mouvement à un document source.
     */
    private String reference;

    /** Note libre sur le mouvement */
    @Column(length = 500)
    private String note;

    /**
     * Utilisateur qui a effectué ou déclenché le mouvement.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    /** Date et heure exactes du mouvement — immuable après création */
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }

    // -------------------------------------------------------
    // ENUM : Types de mouvements
    // -------------------------------------------------------

    /**
     * Types de mouvements disponibles.
     * Chaque type indique si le stock augmente ou diminue.
     */
    public enum TypeMouvement {
        /** Entrée de marchandises (réception fournisseur) → stock + */
        ENTREE,

        /** Sortie de marchandises (livraison client) → stock - */
        SORTIE,

        /** Transfert sortant vers un autre entrepôt → stock - */
        TRANSFERT_SORTIE,

        /** Transfert entrant depuis un autre entrepôt → stock + */
        TRANSFERT_ENTREE,

        /** Ajustement manuel (inventaire, correction) → peut être + ou - */
        AJUSTEMENT,

        /** Réservation pour une commande → disponible - / réservé + */
        RESERVATION,

        /** Libération d'une réservation → disponible + / réservé - */
        LIBERATION_RESERVATION
    }
}
