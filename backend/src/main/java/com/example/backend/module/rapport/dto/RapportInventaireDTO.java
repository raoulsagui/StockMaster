package com.example.backend.module.rapport.dto;

import com.example.backend.module.inventaire.entity.Inventaire;
import com.example.backend.module.inventaire.entity.LigneInventaire;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO agrégé pour le rapport d'inventaire.
 *
 * Contient les données complètes d'un inventaire validé,
 * prêtes à être sérialisées en JSON, Excel ou PDF.
 */
@Data
@Builder
public class RapportInventaireDTO {

    private Long id;
    private String reference;
    private String type;
    private String statut;
    private String entrepotNom;
    private String createurNom;
    private String valideurNom;
    private LocalDateTime dateValidation;
    private LocalDateTime dateCreation;
    private String note;

    // Métriques globales
    private int totalLignes;
    private long lignesAvecEcart;
    private long lignesSurplus;
    private long lignesManque;

    // Détail des lignes
    private List<LigneRapportDTO> lignes;

    @Data
    @Builder
    public static class LigneRapportDTO {
        private String produitReference;
        private String produitNom;
        private String produitCategorie;
        private Integer quantiteTheorique;
        private Integer quantiteComptee;
        private Integer ecart;
        private String sensEcart;
        private String note;
    }

    /**
     * Construit le DTO depuis une entité Inventaire complète.
     */
    public static RapportInventaireDTO fromEntity(Inventaire inv) {
        List<LigneRapportDTO> lignes = inv.getLignes().stream()
                .sorted((a, b) -> a.getProduit().getNom().compareTo(b.getProduit().getNom()))
                .map(l -> {
                    int ecart = l.getEcart();
                    return LigneRapportDTO.builder()
                            .produitReference(l.getProduit().getReference())
                            .produitNom(l.getProduit().getNom())
                            .produitCategorie(l.getProduit().getCategorie() != null
                                    ? l.getProduit().getCategorie().getNom() : "—")
                            .quantiteTheorique(l.getQuantiteTheorique())
                            .quantiteComptee(l.isComptee() ? l.getQuantiteComptee() : null)
                            .ecart(l.isComptee() ? ecart : null)
                            .sensEcart(ecart > 0 ? "SURPLUS" : ecart < 0 ? "MANQUE" : "CONFORME")
                            .note(l.getNote())
                            .build();
                })
                .toList();

        return RapportInventaireDTO.builder()
                .id(inv.getId())
                .reference(inv.getReference())
                .type(inv.getType().name())
                .statut(inv.getStatut().name())
                .entrepotNom(inv.getEntrepot().getNom())
                .createurNom(inv.getCreateur().getPrenom() + " " + inv.getCreateur().getNom())
                .valideurNom(inv.getValideur() != null
                        ? inv.getValideur().getPrenom() + " " + inv.getValideur().getNom() : null)
                .dateValidation(inv.getDateValidation())
                .dateCreation(inv.getDateCreation())
                .note(inv.getNote())
                .totalLignes(lignes.size())
                .lignesAvecEcart(lignes.stream().filter(l -> l.getEcart() != null && l.getEcart() != 0).count())
                .lignesSurplus(lignes.stream().filter(l -> l.getEcart() != null && l.getEcart() > 0).count())
                .lignesManque(lignes.stream().filter(l -> l.getEcart() != null && l.getEcart() < 0).count())
                .lignes(lignes)
                .build();
    }
}
