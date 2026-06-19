package com.example.backend.module.commande.dto;

import com.example.backend.module.commande.entity.LigneCommande;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO de réponse pour une ligne de commande.
 *
 * Expose les informations du produit dénormalisées pour éviter
 * un appel supplémentaire côté frontend.
 */
@Data
@Builder
public class LigneCommandeResponseDTO {

    private Long id;

    // --- Produit ---
    private Long produitId;
    private String produitReference;
    private String produitNom;
    private String produitCategorieNom;

    // --- Commande ---
    private Integer quantiteCommandee;
    private Integer quantiteRecue;
    private BigDecimal prixUnitaire;
    private BigDecimal sousTotal;

    /** true si quantiteRecue a été saisie */
    private boolean recue;

    /**
     * Écart de réception = quantiteRecue - quantiteCommandee.
     * 0 si pas encore reçue. Positif = surplus, négatif = manque.
     */
    private Integer ecartReception;

    /** Sens de l'écart : "SURPLUS", "MANQUE", "CONFORME" */
    private String sensEcart;

    private String note;

    public static LigneCommandeResponseDTO fromEntity(LigneCommande l) {
        int ecart = l.getEcartReception();
        String sensEcart = ecart > 0 ? "SURPLUS" : ecart < 0 ? "MANQUE" : "CONFORME";

        return LigneCommandeResponseDTO.builder()
                .id(l.getId())
                .produitId(l.getProduit().getId())
                .produitReference(l.getProduit().getReference())
                .produitNom(l.getProduit().getNom())
                .produitCategorieNom(
                    l.getProduit().getCategorie() != null
                        ? l.getProduit().getCategorie().getNom()
                        : null
                )
                .quantiteCommandee(l.getQuantiteCommandee())
                .quantiteRecue(l.getQuantiteRecue())
                .prixUnitaire(l.getPrixUnitaire())
                .sousTotal(l.getSousTotal())
                .recue(l.isRecue())
                .ecartReception(ecart)
                .sensEcart(sensEcart)
                .note(l.getNote())
                .build();
    }
}
