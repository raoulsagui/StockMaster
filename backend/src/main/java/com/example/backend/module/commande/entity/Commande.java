package com.example.backend.module.commande.entity;

import com.example.backend.module.entrepot.entity.Entrepot;
import com.example.backend.module.fournisseur.entity.Fournisseur;
import com.example.backend.module.utilisateur.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant une commande fournisseur.
 *
 * Une commande fournisseur est un bon de commande passé auprès d'un fournisseur
 * pour réapprovisionner un entrepôt en produits.
 *
 * Cycle de vie :
 *   BROUILLON → commande créée, en cours de composition (lignes modifiables)
 *   VALIDEE   → commande transmise au fournisseur (non modifiable)
 *   LIVREE    → marchandises reçues, stock mis à jour via StockService
 *   ANNULEE   → commande abandonnée avant livraison
 *
 * Relations :
 *   - ManyToOne → Fournisseur : le fournisseur à qui la commande est adressée
 *   - ManyToOne → Entrepot   : l'entrepôt de destination de la livraison
 *   - ManyToOne → Utilisateur (createur) : qui a créé la commande
 *   - ManyToOne → Utilisateur (valideur) : qui a validé/livré la commande
 *   - OneToMany → LigneCommande : les lignes produit de la commande
 *
 * Référence générée : CMD-AAAA-NNNNN (ex: CMD-2026-00001)
 */
@Entity
@Table(name = "commandes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Référence unique générée automatiquement.
     * Format : CMD-AAAA-NNNNN
     */
    @Column(nullable = false, unique = true)
    private String reference;

    /**
     * Statut courant de la commande.
     * Valeur par défaut : BROUILLON.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutCommande statut = StatutCommande.BROUILLON;

    /**
     * Fournisseur auquel la commande est adressée.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id", nullable = false)
    private Fournisseur fournisseur;

    /**
     * Entrepôt de destination de la livraison.
     * C'est dans cet entrepôt que le stock sera ajouté lors de la réception.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrepot_id", nullable = false)
    private Entrepot entrepot;

    /**
     * Utilisateur ayant créé la commande.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createur_id", nullable = false)
    private Utilisateur createur;

    /**
     * Utilisateur ayant validé ou réceptionné la commande.
     * Null tant que la commande est en BROUILLON.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valideur_id")
    private Utilisateur valideur;

    /**
     * Date de livraison prévue communiquée au fournisseur.
     */
    private LocalDate dateLivraisonPrevue;

    /**
     * Date effective de réception de la livraison.
     * Renseignée lors du passage au statut LIVREE.
     */
    private LocalDateTime dateLivraisonEffective;

    /**
     * Date de validation de la commande.
     * Renseignée lors du passage au statut VALIDEE.
     */
    private LocalDateTime dateValidation;

    /**
     * Note ou commentaire global sur la commande.
     * Ex : "Urgence", "Commande groupée", conditions particulières.
     */
    @Column(length = 1000)
    private String note;

    /**
     * Lignes de la commande : une ligne par référence produit commandée.
     */
    @Builder.Default
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneCommande> lignes = new ArrayList<>();

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
     * Montant total de la commande : somme des sous-totaux de chaque ligne.
     * Retourne ZERO si la commande n'a pas de lignes ou si les prix ne sont pas renseignés.
     */
    public BigDecimal getMontantTotal() {
        return lignes.stream()
                .map(LigneCommande::getSousTotal)
                .filter(st -> st != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Nombre total d'unités commandées (somme des quantités).
     */
    public int getNombreUnites() {
        return lignes.stream()
                .mapToInt(LigneCommande::getQuantiteCommandee)
                .sum();
    }

    // -------------------------------------------------------
    // ENUMS
    // -------------------------------------------------------

    public enum StatutCommande {
        /** Commande créée, en cours de composition — modifiable */
        BROUILLON,

        /** Commande transmise au fournisseur — non modifiable */
        VALIDEE,

        /**
         * Marchandises reçues dans l'entrepôt.
         * Stock mis à jour via StockService.ajouterStock().
         */
        LIVREE,

        /** Commande abandonnée — aucun mouvement de stock */
        ANNULEE
    }
}
