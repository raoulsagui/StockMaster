package com.example.backend.module.sortie.dto;

import com.example.backend.module.sortie.entity.Sortie;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour un bon de sortie.
 */
@Data
@Builder
public class SortieResponseDTO {

    private Long   id;
    private String reference;
    private String statut;
    private String motif;

    private Long   produitId;
    private String produitNom;
    private String produitReference;

    private Long   entrepotId;
    private String entrepotNom;

    private Integer quantite;
    private String  destinataire;
    private String  note;

    private String createurNom;
    private String validateurNom;

    private LocalDateTime dateCreation;
    private LocalDateTime dateValidation;

    public static SortieResponseDTO fromEntity(Sortie s) {
        return SortieResponseDTO.builder()
                .id(s.getId())
                .reference(s.getReference())
                .statut(s.getStatut().name())
                .motif(s.getMotif().name())
                .produitId(s.getProduit().getId())
                .produitNom(s.getProduit().getNom())
                .produitReference(s.getProduit().getReference())
                .entrepotId(s.getEntrepot().getId())
                .entrepotNom(s.getEntrepot().getNom())
                .quantite(s.getQuantite())
                .destinataire(s.getDestinataire())
                .note(s.getNote())
                .createurNom(s.getCreateur() != null
                        ? s.getCreateur().getPrenom() + " " + s.getCreateur().getNom() : null)
                .validateurNom(s.getValidateur() != null
                        ? s.getValidateur().getPrenom() + " " + s.getValidateur().getNom() : null)
                .dateCreation(s.getDateCreation())
                .dateValidation(s.getDateValidation())
                .build();
    }
}
