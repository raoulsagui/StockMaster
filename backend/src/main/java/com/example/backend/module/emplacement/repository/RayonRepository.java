package com.example.backend.module.emplacement.repository;

import com.example.backend.module.emplacement.entity.Rayon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RayonRepository extends JpaRepository<Rayon, Long> {

    List<Rayon> findByZoneIdOrderByCode(Long zoneId);
    List<Rayon> findByZoneIdAndActifTrueOrderByCode(Long zoneId);
    boolean existsByCodeAndZoneId(String code, Long zoneId);
    boolean existsByCodeAndZoneIdAndIdNot(String code, Long zoneId, Long id);
    long countByZoneId(Long zoneId);
}
