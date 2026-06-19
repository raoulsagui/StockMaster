package com.example.backend.module.inventaire.entity;

import com.example.backend.module.produit.entity.Produit;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entité représentant une ligne d'inventaire.
 *
 * Chaque ligne correspond à un produit inventorié dans le cadre
 * d'un inventaire physique. Elle stocke :
 *   - la quantité théorique (issue du stock système au moment de la création)
 *   - la quantité comptée (saisie par le magasinier lors du comptage)
 *   - l'écart calculé (compté - théorique)
 *
 * L'écart peut être :
 *   > 0 : écart positif  → plus de stock réel que prévu (gain)
 *   < 0 : écart négatif  → moins de stock réel que prévu (perte)
 *   = 0 : aucun écart    → quantités concordantes
 *
 * La ligne est créée avec comptee = false.
 * Elle passe à comptee = true quand le magasinier saisit la quantité réelle.
 */
@Entity
@Table(
    name = "lignes_inventaire",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_ligne_inventaire_produit",
        columnNames = { "inventaire_id", "produit_id" }
    )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneInventaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Inventaire auquel appartient cette ligne.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventaire_id", nullable = false)
    private Inventaire inventaire;

    /**
     * Produit concerné par cette ligne d'inventaire.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    /**
     * Quantité théorique : stock enregistré dans le système
     * au moment de la création de l'inventaire.
     * Valeur de référence pour le calcul de l'écart.
     */
    @Column(nullable = false)
    private Integer quantiteTheorique;

    /**
     * Quantité réellement comptée sur le terrain par le magasinier.
     * Null tant que la ligne n'a pas été comptée.
     */
    private Integer quantiteComptee;

    /**
     * Indique si cette ligne a été comptée (quantiteComptee saisie).
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean comptee = false;

    /**
     * Note ou commentaire sur cette ligne spécifique.
     * Ex : "produit endommagé", "conditionnement modifié"
     */
    @Column(length = 500)
    private String note;

    /**
     * Date de la dernière saisie sur cette ligne.
     */
    private LocalDateTime dateSaisie;

    // -------------------------------------------------------
    // MÉTHODE MÉTIER
    // -------------------------------------------------------

    /**
     * Calcule l'écart entre la quantité comptée et la quantité théorique.
     * Retourne 0 si la ligne n'a pas encore été comptée.
     *
     * @return écart = quantiteComptee - quantiteTheorique
     */
    public int getEcart() {
        if (!comptee || quantiteComptee == null) return 0;
        return quantiteComptee - quantiteTheorique;
    }
}
