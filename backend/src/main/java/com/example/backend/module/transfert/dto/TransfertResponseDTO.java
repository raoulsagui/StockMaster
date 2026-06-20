package com.example.backend.module.transfert.dto;

import com.example.backend.module.transfert.entity.Transfert;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour un transfert.
 */
@Data
@Builder
public class TransfertResponseDTO {

    private Long   id;
    private String reference;
    private String statut;

    private Long   produitId;
    private String produitNom;
    private String produitReference;

    private Long   entrepotSourceId;
    private String entrepotSourceNom;

    private Long   entrepotDestinationId;
    private String entrepotDestinationNom;

    private Integer quantite;
    private String  note;

    private String createurNom;
    private String expediteurNom;
    private String recepteurNom;

    private LocalDateTime dateCreation;
    private LocalDateTime dateExpedition;
    private LocalDateTime dateReception;

    public static TransfertResponseDTO fromEntity(Transfert t) {
        return TransfertResponseDTO.builder()
                .id(t.getId())
                .reference(t.getReference())
                .statut(t.getStatut().name())
                .produitId(t.getProduit().getId())
                .produitNom(t.getProduit().getNom())
                .produitReference(t.getProduit().getReference())
                .entrepotSourceId(t.getEntrepotSource().getId())
                .entrepotSourceNom(t.getEntrepotSource().getNom())
                .entrepotDestinationId(t.getEntrepotDestination().getId())
                .entrepotDestinationNom(t.getEntrepotDestination().getNom())
                .quantite(t.getQuantite())
                .note(t.getNote())
                .createurNom(t.getCreateur() != null
                        ? t.getCreateur().getPrenom() + " " + t.getCreateur().getNom() : null)
                .expediteurNom(t.getExpediteur() != null
                        ? t.getExpediteur().getPrenom() + " " + t.getExpediteur().getNom() : null)
                .recepteurNom(t.getRecepteur() != null
                        ? t.getRecepteur().getPrenom() + " " + t.getRecepteur().getNom() : null)
                .dateCreation(t.getDateCreation())
                .dateExpedition(t.getDateExpedition())
                .dateReception(t.getDateReception())
                .build();
    }
}
