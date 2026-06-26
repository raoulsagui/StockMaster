package com.example.backend.module.rapport.dto;

import com.example.backend.module.commande.entity.Commande;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO agrégé pour le rapport des fournisseurs.
 *
 * Synthétise les commandes par fournisseur sur une période donnée :
 * nombre de commandes, montant total, statuts.
 */
@Data
@Builder
public class RapportFournisseursDTO {

    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    // Statistiques globales
    private long totalCommandes;
    private BigDecimal montantTotalHT;
    private Map<String, Long> countParStatut;

    // Détail par fournisseur
    private List<LigneFournisseurDTO> fournisseurs;

    @Data
    @Builder
    public static class LigneFournisseurDTO {
        private String fournisseurNom;
        private String fournisseurEmail;
        private long nbCommandes;
        private long nbLivrees;
        private long nbAnnulees;
        private BigDecimal montantTotal;
        private List<CommandeResumeeDTO> commandes;
    }

    @Data
    @Builder
    public static class CommandeResumeeDTO {
        private String reference;
        private String statut;
        private String entrepotNom;
        private BigDecimal montantTotal;
        private LocalDateTime dateCreation;
        private LocalDateTime dateLivraisonEffective;
    }

    /**
     * Convertit une entité Commande en résumé.
     */
    public static CommandeResumeeDTO toResume(Commande c) {
        return CommandeResumeeDTO.builder()
                .reference(c.getReference())
                .statut(c.getStatut().name())
                .entrepotNom(c.getEntrepot().getNom())
                .montantTotal(c.getMontantTotal())
                .dateCreation(c.getDateCreation())
                .dateLivraisonEffective(c.getDateLivraisonEffective())
                .build();
    }
}
