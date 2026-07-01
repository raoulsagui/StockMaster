package com.example.backend.module.alerte.dto;

import com.example.backend.module.alerte.entity.Alerte;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour une alerte.
 * Exposé via l'API REST au frontend.
 */
@Data
@Builder
public class AlerteResponseDTO {

    private Long id;
    private String type;
    private String severite;
    private String statut;
    private String message;

    // Contexte — stock concerné
    private String produitNom;
    private String produitReference;
    private String entrepotNom;

    // Contexte — zone concernée
    private String zoneNom;
    private String zoneEntrepotNom;

    // Valeurs numériques
    private Double valeurActuelle;
    private Double seuil;

    private boolean emailEnvoye;
    private LocalDateTime dateCreation;
    private LocalDateTime dateLecture;
    private LocalDateTime dateResolution;

    public static AlerteResponseDTO fromEntity(Alerte a) {
        var b = AlerteResponseDTO.builder()
                .id(a.getId())
                .type(a.getType().name())
                .severite(a.getSeverite().name())
                .statut(a.getStatut().name())
                .message(a.getMessage())
                .valeurActuelle(a.getValeurActuelle())
                .seuil(a.getSeuil())
                .emailEnvoye(a.isEmailEnvoye())
                .dateCreation(a.getDateCreation())
                .dateLecture(a.getDateLecture())
                .dateResolution(a.getDateResolution());

        if (a.getStock() != null) {
            b.produitNom(a.getStock().getProduit().getNom())
             .produitReference(a.getStock().getProduit().getReference())
             .entrepotNom(a.getStock().getEntrepot().getNom());
        }
        if (a.getZone() != null) {
            b.zoneNom(a.getZone().getNom())
             .zoneEntrepotNom(a.getZone().getEntrepot().getNom());
        }
        return b.build();
    }
}
