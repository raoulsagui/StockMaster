package com.example.backend.module.zone.dto;

import com.example.backend.module.zone.entity.Zone;
import com.example.backend.module.zone.entity.TypeZone;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de réponse renvoyé au client pour une zone de stockage.
 *
 * Contient les informations de la zone ainsi que les informations
 * de base de l'entrepôt parent (id + nom).
 *
 * Le taux d'occupation est calculé à partir de capaciteUtilisee / capaciteTotale.
 */
@Data
@Builder
public class ZoneResponseDTO {

    private Long id;
    private String nom;
    private TypeZone type;
    private String description;
    private boolean actif;
    private LocalDateTime dateCreation;

    // --- Capacités ---
    /** Capacité totale de la zone en m³ (null si non définie) */
    private Double capaciteTotale;

    /** Capacité actuellement occupée en m³ */
    private Double capaciteUtilisee;

    /** Capacité libre = total - utilisée (null si capaciteTotale non définie) */
    private Double capaciteDisponible;

    /**
     * Taux d'occupation en pourcentage (0–100).
     * 0 si capaciteTotale non définie ou nulle.
     */
    private Double tauxOccupation;

    /**
     * Informations de base sur l'entrepôt parent.
     */
    private EntrepotInfoDTO entrepot;

    /**
     * DTO interne léger pour les informations de l'entrepôt parent.
     */
    @Data
    @Builder
    public static class EntrepotInfoDTO {
        private Long id;
        private String nom;
    }

    /**
     * Factory method : convertit une entité Zone en DTO de réponse.
     *
     * Attention : l'entité Zone.entrepot est chargée en LAZY.
     * Cette méthode doit être appelée dans un contexte transactionnel.
     *
     * @param zone L'entité Zone récupérée de la BDD
     * @return Le DTO prêt à être sérialisé en JSON
     */
    public static ZoneResponseDTO fromEntity(Zone zone) {
        return ZoneResponseDTO.builder()
                .id(zone.getId())
                .nom(zone.getNom())
                .type(zone.getType())
                .description(zone.getDescription())
                .actif(zone.isActif())
                .dateCreation(zone.getDateCreation())
                // Capacités
                .capaciteTotale(zone.getCapaciteTotale())
                .capaciteUtilisee(zone.getCapaciteUtilisee())
                .capaciteDisponible(zone.getCapaciteDisponible())
                .tauxOccupation(Math.round(zone.getTauxOccupation() * 10.0) / 10.0)
                // Entrepôt parent
                .entrepot(zone.getEntrepot() != null
                        ? EntrepotInfoDTO.builder()
                                .id(zone.getEntrepot().getId())
                                .nom(zone.getEntrepot().getNom())
                                .build()
                        : null)
                .build();
    }
}
