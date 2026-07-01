package com.example.backend.module.emplacement.repository;

import com.example.backend.module.emplacement.entity.Etagere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtagereRepository extends JpaRepository<Etagere, Long> {

    List<Etagere> findByRayonIdOrderByCode(Long rayonId);
    List<Etagere> findByRayonIdAndActifTrueOrderByCode(Long rayonId);
    boolean existsByCodeAndRayonId(String code, Long rayonId);
    boolean existsByCodeAndRayonIdAndIdNot(String code, Long rayonId, Long id);
    long countByRayonId(Long rayonId);
}
