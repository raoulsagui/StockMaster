package com.example.backend.module.stock.repository;

import com.example.backend.module.stock.entity.MouvementStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA pour l'historique des mouvements de stock.
 */
@Repository
public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {

    /**
     * Historique des mouvements d'un produit dans un entrepôt.
     * Pageable : on ne charge pas tout l'historique d'un coup.
     * Trié par date décroissante (le plus récent en premier).
     */
    Page<MouvementStock> findByProduitIdAndEntrepotIdOrderByDateCreationDesc(
        Long produitId, Long entrepotId, Pageable pageable
    );

    /**
     * Tous les mouvements d'un entrepôt, triés par date décroissante.
     * Pageable pour la pagination côté frontend.
     */
    Page<MouvementStock> findByEntrepotIdOrderByDateCreationDesc(
        Long entrepotId, Pageable pageable
    );

    /**
     * Tous les mouvements d'un produit (tous entrepôts confondus).
     */
    List<MouvementStock> findByProduitIdOrderByDateCreationDesc(Long produitId);

    /**
     * Les N derniers mouvements toutes entités confondues.
     * Utilisé par le tableau de bord pour afficher les derniers mouvements.
     */
    Page<MouvementStock> findAllByOrderByDateCreationDesc(Pageable pageable);
}
