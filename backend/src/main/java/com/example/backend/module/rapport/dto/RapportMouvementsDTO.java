package com.example.backend.module.rapport.dto;

import com.example.backend.module.stock.entity.MouvementStock;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO agrégé pour le rapport des mouvements de stock.
 *
 * Contient la liste des mouvements filtrés par période
 * et des statistiques agrégées par type de mouvement.
 */
@Data
@Builder
public class RapportMouvementsDTO {

    // Période du rapport
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    // Statistiques globales
    private long totalMouvements;
    private Map<String, Long> countParType;   // ex: {ENTREE: 42, SORTIE: 18, ...}
    private Map<String, Long> quantiteParType; // somme des quantités par type

    // Détail des mouvements
    private List<LigneMouvementDTO> mouvements;

    @Data
    @Builder
    public static class LigneMouvementDTO {
        private Long id;
        private String type;
        private String produitReference;
        private String produitNom;
        private String entrepotNom;
        private Integer quantite;
        private Integer quantiteApres;
        private String reference;
        private String note;
        private String utilisateurNom;
        private LocalDateTime dateCreation;
    }

    /**
     * Convertit une entité MouvementStock en ligne de rapport.
     */
    public static LigneMouvementDTO toLigne(MouvementStock m) {
        return LigneMouvementDTO.builder()
                .id(m.getId())
                .type(m.getType().name())
                .produitReference(m.getProduit().getReference())
                .produitNom(m.getProduit().getNom())
                .entrepotNom(m.getEntrepot().getNom())
                .quantite(m.getQuantite())
                .quantiteApres(m.getQuantiteApres())
                .reference(m.getReference())
                .note(m.getNote())
                .utilisateurNom(m.getUtilisateur() != null
                        ? m.getUtilisateur().getPrenom() + " " + m.getUtilisateur().getNom()
                        : "—")
                .dateCreation(m.getDateCreation())
                .build();
    }
}
