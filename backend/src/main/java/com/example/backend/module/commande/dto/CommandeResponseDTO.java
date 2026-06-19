package com.example.backend.module.commande.dto;

import com.example.backend.module.commande.entity.Commande;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de réponse pour une commande fournisseur.
 *
 * Retourné par tous les endpoints GET du module commandes.
 * Contient les informations dénormalisées (fournisseur, entrepôt, créateur)
 * et les métriques calculées (montant total, nombre d'unités).
 *
 * Deux factories :
 *   fromEntity()          → version liste (sans lignes, pour performances)
 *   fromEntityWithLignes() → version détail (avec toutes les lignes)
 */
@Data
@Builder
public class CommandeResponseDTO {

    private Long id;
    private String reference;
    private String statut;

    // --- Fournisseur ---
    private Long fournisseurId;
    private String fournisseurNom;
    private String fournisseurEmail;

    // --- Entrepôt de destination ---
    private Long entrepotId;
    private String entrepotNom;

    // --- Créateur ---
    private Long createurId;
    private String createurNom;

    // --- Valideur (null si BROUILLON) ---
    private Long valideurId;
    private String valideurNom;

    // --- Dates ---
    private LocalDate dateLivraisonPrevue;
    private LocalDateTime dateValidation;
    private LocalDateTime dateLivraisonEffective;
    private LocalDateTime dateCreation;

    private String note;

    // --- Métriques calculées ---
    /** Nombre de lignes dans la commande */
    private int nombreLignes;

    /** Nombre total d'unités commandées */
    private int nombreUnites;

    /** Montant total HT de la commande */
    private BigDecimal montantTotal;

    /** Lignes de la commande — incluses uniquement sur le détail */
    private List<LigneCommandeResponseDTO> lignes;

    // -------------------------------------------------------
    // FACTORIES
    // -------------------------------------------------------

    /**
     * Version liste — sans lignes pour alléger les réponses paginées.
     */
    public static CommandeResponseDTO fromEntity(Commande c) {
        return CommandeResponseDTO.builder()
                .id(c.getId())
                .reference(c.getReference())
                .statut(c.getStatut().name())
                // Fournisseur
                .fournisseurId(c.getFournisseur().getId())
                .fournisseurNom(c.getFournisseur().getNom())
                .fournisseurEmail(c.getFournisseur().getEmail())
                // Entrepôt
                .entrepotId(c.getEntrepot().getId())
                .entrepotNom(c.getEntrepot().getNom())
                // Créateur
                .createurId(c.getCreateur().getId())
                .createurNom(c.getCreateur().getPrenom() + " " + c.getCreateur().getNom())
                // Valideur
                .valideurId(c.getValideur() != null ? c.getValideur().getId() : null)
                .valideurNom(c.getValideur() != null
                    ? c.getValideur().getPrenom() + " " + c.getValideur().getNom()
                    : null)
                // Dates
                .dateLivraisonPrevue(c.getDateLivraisonPrevue())
                .dateValidation(c.getDateValidation())
                .dateLivraisonEffective(c.getDateLivraisonEffective())
                .dateCreation(c.getDateCreation())
                .note(c.getNote())
                // Métriques
                .nombreLignes(c.getLignes().size())
                .nombreUnites(c.getNombreUnites())
                .montantTotal(c.getMontantTotal())
                .build();
    }

    /**
     * Version détail — avec toutes les lignes, triées par nom de produit.
     */
    public static CommandeResponseDTO fromEntityWithLignes(Commande c) {
        CommandeResponseDTO dto = fromEntity(c);
        dto.setLignes(
            c.getLignes().stream()
             .sorted((a, b) -> a.getProduit().getNom().compareTo(b.getProduit().getNom()))
             .map(LigneCommandeResponseDTO::fromEntity)
             .toList()
        );
        return dto;
    }
}
