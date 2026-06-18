package com.example.backend.module.fournisseur.repository;

import com.example.backend.module.fournisseur.entity.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {

    boolean existsByNom(String nom);
    boolean existsByNomAndIdNot(String nom, Long id);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);

    List<Fournisseur> findByActifTrue();
}
