package com.example.backend.module.sortie.repository;

import com.example.backend.module.sortie.entity.Sortie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SortieRepository extends JpaRepository<Sortie, Long> {

    List<Sortie> findAllByOrderByDateCreationDesc();

    List<Sortie> findByStatutOrderByDateCreationDesc(Sortie.StatutSortie statut);

    List<Sortie> findByEntrepotIdOrderByDateCreationDesc(Long entrepotId);

    List<Sortie> findByProduitIdOrderByDateCreationDesc(Long produitId);

    /** Nombre de sorties validées sur une période donnée */
    long countByStatutAndDateValidationBetween(
        Sortie.StatutSortie statut,
        LocalDateTime debut,
        LocalDateTime fin
    );

    /** Nombre de sorties validées sur une période pour des entrepôts donnés */
    @Query("SELECT COUNT(s) FROM Sortie s WHERE s.statut = :statut " +
           "AND s.dateValidation BETWEEN :debut AND :fin " +
           "AND s.entrepot.id IN :entrepotIds")
    long countByStatutAndPeriodeAndEntrepots(
        @Param("statut") Sortie.StatutSortie statut,
        @Param("debut") LocalDateTime debut,
        @Param("fin") LocalDateTime fin,
        @Param("entrepotIds") List<Long> entrepotIds
    );
}
