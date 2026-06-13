package com.example.backend.module.zone.dto;

import com.example.backend.module.zone.entity.Zone;
import com.example.backend.module.zone.entity.TypeZone;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de réponse renvoyé au client pour une zone de stockage.
 *
 * Contient les informations de la zone ainsi que :
 *   - Les informations de base de l'entrepôt parent (id + nom)
 *   - Les métriques calculées (taux d'occupation, capacité disponible)
 *
 * Même pattern que EntrepotResponseDTO : factory method statique fromEntity().
 */
@Data
@Builder
public class ZoneResponseDTO {

    private Long id;
    private String nom;
    private TypeZone type;
    private String description;
    private Double capaciteTotale;
    private Double capaciteUtilisee;

    /**
     * Taux d'occupation calculé : (capaciteUtilisee / capaciteTotale) * 100.
     * Arrondi à 2 décimales.
     */
    private Double tauxOccupation;

    /**
     * Capacité disponible restante en m².
     */
    private Double capaciteDisponible;

    private boolean actif;
    private LocalDateTime dateCreation;

    /**
     * Informations de base sur l'entrepôt parent.
     * On n'expose que ce dont le frontend a besoin.
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
     * Cette méthode doit être appelée dans un contexte transactionnel
     * (ou via un JOIN FETCH dans le repository si nécessaire).
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
                .capaciteTotale(zone.getCapaciteTotale())
                .capaciteUtilisee(zone.getCapaciteUtilisee())
                .tauxOccupation(Math.round(zone.getTauxOccupation() * 100.0) / 100.0)
                .capaciteDisponible(zone.getCapaciteDisponible())
                .actif(zone.isActif())
                .dateCreation(zone.getDateCreation())
                // Entrepôt parent : on construit le DTO interne si présent
                .entrepot(zone.getEntrepot() != null
                        ? EntrepotInfoDTO.builder()
                                .id(zone.getEntrepot().getId())
                                .nom(zone.getEntrepot().getNom())
                                .build()
                        : null)
                .build();
    }
}
