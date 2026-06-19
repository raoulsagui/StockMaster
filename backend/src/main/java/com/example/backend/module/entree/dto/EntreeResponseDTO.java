package com.example.backend.module.entree.dto;

import com.example.backend.module.entree.entity.Entree;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de réponse pour un bon de réception.
 */
@Data
@Builder
public class EntreeResponseDTO {

    private Long   id;
    private String reference;
    private String statut;

    private Long   produitId;
    private String produitNom;
    private String produitReference;

    private Long   entrepotId;
    private String entrepotNom;

    private Long   fournisseurId;
    private String fournisseurNom;

    private Integer    quantite;
    private BigDecimal prixUnitaire;
    private String     note;

    private String createurNom;
    private String validateurNom;

    private LocalDateTime dateCreation;
    private LocalDateTime dateValidation;

    public static EntreeResponseDTO fromEntity(Entree e) {
        return EntreeResponseDTO.builder()
                .id(e.getId())
                .reference(e.getReference())
                .statut(e.getStatut().name())
                .produitId(e.getProduit().getId())
                .produitNom(e.getProduit().getNom())
                .produitReference(e.getProduit().getReference())
                .entrepotId(e.getEntrepot().getId())
                .entrepotNom(e.getEntrepot().getNom())
                .fournisseurId(e.getFournisseur() != null ? e.getFournisseur().getId() : null)
                .fournisseurNom(e.getFournisseur() != null ? e.getFournisseur().getNom() : null)
                .quantite(e.getQuantite())
                .prixUnitaire(e.getPrixUnitaire())
                .note(e.getNote())
                .createurNom(e.getCreateur() != null
                        ? e.getCreateur().getPrenom() + " " + e.getCreateur().getNom() : null)
                .validateurNom(e.getValidateur() != null
                        ? e.getValidateur().getPrenom() + " " + e.getValidateur().getNom() : null)
                .dateCreation(e.getDateCreation())
                .dateValidation(e.getDateValidation())
                .build();
    }
}
