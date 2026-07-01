package com.example.backend.module.emplacement.repository;

import com.example.backend.module.emplacement.entity.Emplacement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmplacementRepository extends JpaRepository<Emplacement, Long> {

    List<Emplacement> findByEtagereIdOrderByCode(Long etagereId);

    /** Emplacements libres d'une étagère */
    List<Emplacement> findByEtagereIdAndStatut(Long etagereId, Emplacement.StatutEmplacement statut);

    boolean existsByCodeAndEtagereId(String code, Long etagereId);
    boolean existsByCodeAndEtagereIdAndIdNot(String code, Long etagereId, Long id);

    Optional<Emplacement> findByAdresseComplete(String adresseComplete);

    /** Tous les emplacements d'un entrepôt (via la hiérarchie complète) */
    @Query("""
        SELECT e FROM Emplacement e
        JOIN e.etagere et
        JOIN et.rayon r
        JOIN r.zone z
        WHERE z.entrepot.id = :entrepotId
        ORDER BY z.nom, r.code, et.code, e.code
        """)
    List<Emplacement> findByEntrepotId(@Param("entrepotId") Long entrepotId);

    /** Emplacements d'une zone */
    @Query("""
        SELECT e FROM Emplacement e
        JOIN e.etagere et
        JOIN et.rayon r
        WHERE r.zone.id = :zoneId
        ORDER BY r.code, et.code, e.code
        """)
    List<Emplacement> findByZoneId(@Param("zoneId") Long zoneId);

    /** Emplacements libres d'un entrepôt */
    @Query("""
        SELECT e FROM Emplacement e
        JOIN e.etagere et
        JOIN et.rayon r
        JOIN r.zone z
        WHERE z.entrepot.id = :entrepotId
        AND e.statut = 'LIBRE'
        ORDER BY z.nom, r.code, et.code, e.code
        """)
    List<Emplacement> findLibresByEntrepotId(@Param("entrepotId") Long entrepotId);

    /** Statistiques par statut pour un entrepôt */
    @Query("""
        SELECT e.statut, COUNT(e) FROM Emplacement e
        JOIN e.etagere et
        JOIN et.rayon r
        JOIN r.zone z
        WHERE z.entrepot.id = :entrepotId
        GROUP BY e.statut
        """)
    List<Object[]> countByStatutForEntrepot(@Param("entrepotId") Long entrepotId);

    long countByEtagereId(Long etagereId);
}
