package com.example.backend.module.stock.dto;

import com.example.backend.module.stock.entity.MouvementStock;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour un mouvement de stock (historique).
 *
 * Retourné par GET /api/stocks/{id}/mouvements
 */
@Data
@Builder
public class MouvementStockResponseDTO {

    private Long id;
    private String type;          // ENTREE, SORTIE, TRANSFERT_SORTIE, etc.
    private String produitNom;
    private String entrepotNom;
    private Integer quantite;
    private Integer quantiteApres;
    private String reference;     // ex: BON-2026-001
    private String note;
    private String utilisateurNom;// prénom + nom de qui a fait le mouvement
    private LocalDateTime dateCreation;

    public static MouvementStockResponseDTO fromEntity(MouvementStock m) {
        return MouvementStockResponseDTO.builder()
                .id(m.getId())
                .type(m.getType().name())
                .produitNom(m.getProduit().getNom())
                .entrepotNom(m.getEntrepot().getNom())
                .quantite(m.getQuantite())
                .quantiteApres(m.getQuantiteApres())
                .reference(m.getReference())
                .note(m.getNote())
                .utilisateurNom(m.getUtilisateur() != null
                    ? m.getUtilisateur().getPrenom() + " " + m.getUtilisateur().getNom()
                    : null)
                .dateCreation(m.getDateCreation())
                .build();
    }
}
