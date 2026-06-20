package com.example.backend.module.entree.repository;

import com.example.backend.module.entree.entity.Entree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EntreeRepository extends JpaRepository<Entree, Long> {

    List<Entree> findAllByOrderByDateCreationDesc();

    List<Entree> findByStatutOrderByDateCreationDesc(Entree.StatutEntree statut);

    List<Entree> findByEntrepotIdOrderByDateCreationDesc(Long entrepotId);

    List<Entree> findByProduitIdOrderByDateCreationDesc(Long produitId);

    /** Nombre d'entrées validées sur une période donnée */
    long countByStatutAndDateValidationBetween(
        Entree.StatutEntree statut,
        LocalDateTime debut,
        LocalDateTime fin
    );

    /** Nombre d'entrées validées sur une période pour des entrepôts donnés */
    @Query("SELECT COUNT(e) FROM Entree e WHERE e.statut = :statut " +
           "AND e.dateValidation BETWEEN :debut AND :fin " +
           "AND e.entrepot.id IN :entrepotIds")
    long countByStatutAndPeriodeAndEntrepots(
        @Param("statut") Entree.StatutEntree statut,
        @Param("debut") LocalDateTime debut,
        @Param("fin") LocalDateTime fin,
        @Param("entrepotIds") List<Long> entrepotIds
    );

    /**
     * Évolution quotidienne des entrées validées sur une période.
     * Retourne une liste de [date, count] pour le graphique.
     */
    @Query("SELECT CAST(e.dateValidation AS date), COUNT(e) FROM Entree e " +
           "WHERE e.statut = 'VALIDE' AND e.dateValidation BETWEEN :debut AND :fin " +
           "GROUP BY CAST(e.dateValidation AS date) ORDER BY CAST(e.dateValidation AS date)")
    List<Object[]> findEvolutionQuotidienne(
        @Param("debut") LocalDateTime debut,
        @Param("fin") LocalDateTime fin
    );

    @Query("SELECT CAST(e.dateValidation AS date), COUNT(e) FROM Entree e " +
           "WHERE e.statut = 'VALIDE' AND e.dateValidation BETWEEN :debut AND :fin " +
           "AND e.entrepot.id IN :entrepotIds " +
           "GROUP BY CAST(e.dateValidation AS date) ORDER BY CAST(e.dateValidation AS date)")
    List<Object[]> findEvolutionQuotidienneParEntrepots(
        @Param("debut") LocalDateTime debut,
        @Param("fin") LocalDateTime fin,
        @Param("entrepotIds") List<Long> entrepotIds
    );
}
