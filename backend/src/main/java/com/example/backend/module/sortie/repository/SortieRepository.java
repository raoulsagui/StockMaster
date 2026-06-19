package com.example.backend.module.sortie.repository;

import com.example.backend.module.sortie.entity.Sortie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SortieRepository extends JpaRepository<Sortie, Long> {

    List<Sortie> findAllByOrderByDateCreationDesc();

    List<Sortie> findByStatutOrderByDateCreationDesc(Sortie.StatutSortie statut);

    List<Sortie> findByEntrepotIdOrderByDateCreationDesc(Long entrepotId);

    List<Sortie> findByProduitIdOrderByDateCreationDesc(Long produitId);
}
