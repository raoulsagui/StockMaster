package com.example.backend.module.entrepot.dto;

import com.example.backend.module.entrepot.entity.Entrepot;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de réponse renvoyé au client pour un entrepôt.
 *
 * Contient toutes les informations nécessaires à l'affichage,
 * incluant les métriques calculées (taux d'occupation, capacité disponible).
 *
 * Le responsable est représenté par ses informations de base uniquement
 * (id + nom complet) pour éviter de renvoyer des données sensibles.
 *
 * La factory method statique fromEntity() gère la conversion entité → DTO.
 * C'est le même pattern que UtilisateurResponseDTO — cohérence du projet.
 */
@Data
@Builder
public class EntrepotResponseDTO {

    private Long id;
    private String nom;
    private String adresse;
    private Double capaciteTotale;
    private Double capaciteUtilisee;

    /**
     * Taux d'occupation calculé : (capaciteUtilisee / capaciteTotale) * 100.
     * Pré-calculé côté serveur pour simplifier l'affichage frontend.
     */
    private Double tauxOccupation;

    /**
     * Capacité libre restante en m².
     */
    private Double capaciteDisponible;

    /** Nombre de zones dans cet entrepôt (injecté par le service). */
    private long nombreZones;

    private boolean actif;
    private LocalDateTime dateCreation;

    /**
     * Informations de base sur le responsable.
     * Null si aucun responsable n'est assigné.
     */
    private ResponsableDTO responsable;

    /**
     * DTO interne léger représentant le responsable.
     * On n'expose que l'id et le nom complet — jamais le mot de passe ou l'email.
     */
    @Data
    @Builder
    public static class ResponsableDTO {
        private Long id;
        private String nomComplet;
    }

    /**
     * Factory method : convertit une entité Entrepot en DTO de réponse.
     *
     * Note : nombreZones n'est pas accessible directement via l'entité
     * (ça déclencherait un chargement de la collection lazy).
     * On le passe donc séparément depuis le service.
     *
     * @param entrepot    L'entité récupérée de la BDD
     * @param nombreZones Le nombre de zones calculé par le repository
     * @return Le DTO prêt à être sérialisé en JSON
     */
    public static EntrepotResponseDTO fromEntity(Entrepot entrepot, long nombreZones) {
        return EntrepotResponseDTO.builder()
                .id(entrepot.getId())
                .nom(entrepot.getNom())
                .adresse(entrepot.getAdresse())
                .capaciteTotale(entrepot.getCapaciteTotale())
                .capaciteUtilisee(entrepot.getCapaciteUtilisee())
                .tauxOccupation(Math.round(entrepot.getTauxOccupation() * 100.0) / 100.0)
                .capaciteDisponible(entrepot.getCapaciteDisponible())
                .nombreZones(nombreZones)
                .actif(entrepot.isActif())
                .dateCreation(entrepot.getDateCreation())
                // Responsable optionnel : construit le DTO seulement s'il existe
                .responsable(entrepot.getResponsable() != null
                        ? ResponsableDTO.builder()
                                .id(entrepot.getResponsable().getId())
                                .nomComplet(entrepot.getResponsable().getPrenom()
                                        + " " + entrepot.getResponsable().getNom())
                                .build()
                        : null)
                .build();
    }

    /**
     * Surcharge sans nombreZones (utile quand on n'a pas besoin de ce champ).
     * Le nombre de zones est mis à 0 par défaut.
     */
    public static EntrepotResponseDTO fromEntity(Entrepot entrepot) {
        return fromEntity(entrepot, 0L);
    }
}
