package com.example.backend.module.inventaire.dto;

import com.example.backend.module.inventaire.entity.LigneInventaire;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour une ligne d'inventaire.
 *
 * Expose les informations du produit (dénormalisées) pour éviter
 * un appel supplémentaire côté frontend.
 */
@Data
@Builder
public class LigneInventaireResponseDTO {

    private Long id;

    // --- Produit ---
    private Long produitId;
    private String produitReference;
    private String produitNom;
    private String produitCategorieNom;

    // --- Comptage ---
    private Integer quantiteTheorique;
    private Integer quantiteComptee;
    private boolean comptee;

    /**
     * Écart calculé : quantiteComptee - quantiteTheorique.
     * 0 si la ligne n'est pas encore comptée.
     */
    private Integer ecart;

    /**
     * Sens de l'écart pour l'affichage frontend.
     * "POSITIF", "NEGATIF", "AUCUN"
     */
    private String sensEcart;

    private String note;
    private LocalDateTime dateSaisie;

    /**
     * Factory method : convertit l'entité en DTO.
     */
    public static LigneInventaireResponseDTO fromEntity(LigneInventaire l) {
        int ecart = l.getEcart();
        String sensEcart = ecart > 0 ? "POSITIF" : ecart < 0 ? "NEGATIF" : "AUCUN";

        return LigneInventaireResponseDTO.builder()
                .id(l.getId())
                .produitId(l.getProduit().getId())
                .produitReference(l.getProduit().getReference())
                .produitNom(l.getProduit().getNom())
                .produitCategorieNom(
                    l.getProduit().getCategorie() != null
                        ? l.getProduit().getCategorie().getNom()
                        : null
                )
                .quantiteTheorique(l.getQuantiteTheorique())
                .quantiteComptee(l.getQuantiteComptee())
                .comptee(l.isComptee())
                .ecart(ecart)
                .sensEcart(sensEcart)
                .note(l.getNote())
                .dateSaisie(l.getDateSaisie())
                .build();
    }
}
