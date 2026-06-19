package com.example.backend.module.entree.repository;

import com.example.backend.module.entree.entity.Entree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntreeRepository extends JpaRepository<Entree, Long> {

    List<Entree> findAllByOrderByDateCreationDesc();

    List<Entree> findByStatutOrderByDateCreationDesc(Entree.StatutEntree statut);

    List<Entree> findByEntrepotIdOrderByDateCreationDesc(Long entrepotId);

    List<Entree> findByProduitIdOrderByDateCreationDesc(Long produitId);
}
