package com.example.backend.module.commande.entity;

import com.example.backend.module.produit.entity.Produit;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entité représentant une ligne de commande fournisseur.
 *
 * Chaque ligne correspond à un produit commandé dans la commande parente.
 * Elle stocke :
 *   - le produit commandé
 *   - la quantité commandée
 *   - le prix unitaire négocié pour cette commande (peut différer de prixAchat)
 *   - la quantité reçue (renseignée lors de la réception, peut être partielle)
 *
 * Contrainte : un même produit ne peut apparaître qu'une seule fois par commande.
 */
@Entity
@Table(
    name = "lignes_commande",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_ligne_commande_produit",
        columnNames = { "commande_id", "produit_id" }
    )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Commande parente.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    /**
     * Produit commandé.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    /**
     * Quantité commandée auprès du fournisseur.
     * Doit être strictement positive.
     */
    @Column(nullable = false)
    private Integer quantiteCommandee;

    /**
     * Quantité effectivement reçue lors de la réception.
     * Null avant réception. Peut différer de quantiteCommandee
     * (livraison partielle ou surplus).
     */
    private Integer quantiteRecue;

    /**
     * Prix unitaire négocié pour cette ligne (en €, HT).
     * Optionnel : peut être null si le prix n'est pas encore connu au moment de la commande.
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    /**
     * Note spécifique à cette ligne.
     * Ex : "Substitution acceptée", "Conditionnement différent"
     */
    @Column(length = 500)
    private String note;

    // -------------------------------------------------------
    // MÉTHODES MÉTIER
    // -------------------------------------------------------

    /**
     * Sous-total de la ligne = quantiteCommandee × prixUnitaire.
     * Retourne null si le prix unitaire n'est pas renseigné.
     */
    public BigDecimal getSousTotal() {
        if (prixUnitaire == null) return null;
        return prixUnitaire.multiply(BigDecimal.valueOf(quantiteCommandee));
    }

    /**
     * Indique si la ligne a été reçue (quantiteRecue renseignée).
     */
    public boolean isRecue() {
        return quantiteRecue != null;
    }

    /**
     * Écart de réception = quantiteRecue - quantiteCommandee.
     * Retourne 0 si pas encore reçue.
     * Positif = surplus, Négatif = livraison partielle.
     */
    public int getEcartReception() {
        if (quantiteRecue == null) return 0;
        return quantiteRecue - quantiteCommandee;
    }
}
