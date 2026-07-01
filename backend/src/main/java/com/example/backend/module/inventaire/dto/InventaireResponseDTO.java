package com.example.backend.module.inventaire.dto;

import com.example.backend.module.inventaire.entity.Inventaire;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de réponse pour un inventaire.
 *
 * Retourné par tous les endpoints GET du module inventaire.
 * Contient les métriques calculées (progression, écarts) prêtes
 * à l'emploi pour le frontend.
 */
@Data
@Builder
public class InventaireResponseDTO {

    private Long id;
    private String reference;
    private String type;
    private String statut;

    // --- Entrepôt ---
    private Long entrepotId;
    private String entrepotNom;

    // --- Créateur ---
    private Long createurId;
    private String createurNom;

    // --- Valideur (null si pas encore validé) ---
    private Long valideurId;
    private String valideurNom;

    // --- Dates ---
    private LocalDateTime dateValidation;
    private LocalDateTime dateCreation;

    private String note;

    // --- Métriques calculées ---
    /** Nombre total de lignes dans l'inventaire */
    private int nombreLignes;

    /** Nombre de lignes déjà comptées */
    private int nombreLignesComptees;

    /** Pourcentage de progression du comptage (0–100) */
    private int pourcentageProgression;

    /** Nombre de lignes avec un écart (positif ou négatif) */
    private long nombreLignesAvecEcart;

    /** Nombre de lignes en surplus (écart positif) */
    private long nombreLignesEcartPositif;

    /** Nombre de lignes en manque (écart négatif) */
    private long nombreLignesEcartNegatif;

    /** Lignes de l'inventaire (incluses uniquement sur le détail, null en liste) */
    private List<LigneInventaireResponseDTO> lignes;

    /**
     * Factory method — version liste (sans lignes pour alléger les réponses).
     */
    public static InventaireResponseDTO fromEntity(Inventaire inv) {
        int total = inv.getLignes().size();
        int comptes = (int) inv.getLignes().stream().filter(l -> l.isComptee()).count();
        int progression = total == 0 ? 0 : (int) Math.round((double) comptes / total * 100);

        return InventaireResponseDTO.builder()
                .id(inv.getId())
                .reference(inv.getReference())
                .type(inv.getType().name())
                .statut(inv.getStatut().name())
                // Entrepôt
                .entrepotId(inv.getEntrepot().getId())
                .entrepotNom(inv.getEntrepot().getNom())
                // Créateur
                .createurId(inv.getCreateur().getId())
                .createurNom(inv.getCreateur().getPrenom() + " " + inv.getCreateur().getNom())
                // Valideur
                .valideurId(inv.getValideur() != null ? inv.getValideur().getId() : null)
                .valideurNom(inv.getValideur() != null
                    ? inv.getValideur().getPrenom() + " " + inv.getValideur().getNom()
                    : null)
                // Dates
                .dateValidation(inv.getDateValidation())
                .dateCreation(inv.getDateCreation())
                .note(inv.getNote())
                // Métriques
                .nombreLignes(total)
                .nombreLignesComptees(comptes)
                .pourcentageProgression(progression)
                .nombreLignesAvecEcart(inv.getNombreLignesAvecEcart())
                .nombreLignesEcartPositif(inv.getNombreLignesEcartPositif())
                .nombreLignesEcartNegatif(inv.getNombreLignesEcartNegatif())
                .build();
    }

    /**
     * Factory method — version détail (avec lignes).
     */
    public static InventaireResponseDTO fromEntityWithLignes(Inventaire inv) {
        InventaireResponseDTO dto = fromEntity(inv);
        dto.setLignes(
            inv.getLignes().stream()
               .sorted((a, b) -> a.getProduit().getNom().compareTo(b.getProduit().getNom()))
               .map(LigneInventaireResponseDTO::fromEntity)
               .toList()
        );
        return dto;
    }
}
