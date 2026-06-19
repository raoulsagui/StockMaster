package com.example.backend.module.commande.repository;

import com.example.backend.module.commande.entity.LigneCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository JPA pour les lignes de commande.
 */
@Repository
public interface LigneCommandeRepository extends JpaRepository<LigneCommande, Long> {

    /**
     * Toutes les lignes d'une commande donnée, triées par nom de produit.
     */
    List<LigneCommande> findByCommandeIdOrderByProduitNomAsc(Long commandeId);

    /**
     * Ligne de commande pour un produit donné dans une commande.
     * Utilisé pour vérifier les doublons.
     */
    Optional<LigneCommande> findByCommandeIdAndProduitId(Long commandeId, Long produitId);

    /**
     * Vérifie si un produit est déjà dans une commande donnée.
     */
    boolean existsByCommandeIdAndProduitId(Long commandeId, Long produitId);
}
