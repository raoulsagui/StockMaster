package com.example.backend.module.produit.dto;

import com.example.backend.module.produit.entity.Produit;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProduitResponseDTO {

    private Long id;
    private String reference;
    private String codeBarres;
    private String nom;
    private String description;
    private Long categorieId;
    private String categorieNom;
    private BigDecimal prixAchat;
    private BigDecimal prixVente;
    private Double poids;
    private Double volume;
    private boolean actif;
    private LocalDateTime dateCreation;

    public static ProduitResponseDTO fromEntity(Produit p) {
        return ProduitResponseDTO.builder()
                .id(p.getId())
                .reference(p.getReference())
                .codeBarres(p.getCodeBarres())
                .nom(p.getNom())
                .description(p.getDescription())
                .categorieId(p.getCategorie() != null ? p.getCategorie().getId() : null)
                .categorieNom(p.getCategorie() != null ? p.getCategorie().getNom() : null)
                .prixAchat(p.getPrixAchat())
                .prixVente(p.getPrixVente())
                .poids(p.getPoids())
                .volume(p.getVolume())
                .actif(p.isActif())
                .dateCreation(p.getDateCreation())
                .build();
    }
}
