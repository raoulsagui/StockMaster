package com.example.backend.module.produit.repository;

import com.example.backend.module.produit.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {
    boolean existsByReference(String reference);
    boolean existsByReferenceAndIdNot(String reference, Long id);
    boolean existsByCodeBarres(String codeBarres);
    boolean existsByCodeBarresAndIdNot(String codeBarres, Long id);
}
