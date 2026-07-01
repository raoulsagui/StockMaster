package com.example.backend.module.alerte.repository;

import com.example.backend.module.alerte.entity.Alerte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlerteRepository extends JpaRepository<Alerte, Long> {

    /** Toutes les alertes triées par date décroissante */
    List<Alerte> findAllByOrderByDateCreationDesc();

    /** Alertes par statut */
    List<Alerte> findByStatutOrderByDateCreationDesc(Alerte.StatutAlerte statut);

    /** Alertes non lues uniquement (pour le badge dans la navbar) */
    long countByStatut(Alerte.StatutAlerte statut);

    /**
     * Vérifie si une alerte active (NON_LUE ou LUE) existe déjà
     * pour ce stock et ce type — évite les doublons.
     */
    boolean existsByStockIdAndTypeAndStatutIn(
            Long stockId,
            Alerte.TypeAlerte type,
            List<Alerte.StatutAlerte> statuts);

    /**
     * Vérifie si une alerte active existe déjà pour cette zone.
     */
    boolean existsByZoneIdAndTypeAndStatutIn(
            Long zoneId,
            Alerte.TypeAlerte type,
            List<Alerte.StatutAlerte> statuts);

    /** Alertes actives (non résolues) pour le dashboard */
    @Query("SELECT a FROM Alerte a WHERE a.statut != 'RESOLUE' ORDER BY a.dateCreation DESC")
    List<Alerte> findActives();

    /** Compte des alertes par sévérité et statut non résolu */
    @Query("SELECT a.severite, COUNT(a) FROM Alerte a WHERE a.statut != 'RESOLUE' GROUP BY a.severite")
    List<Object[]> countBySeveriteActives();
}
