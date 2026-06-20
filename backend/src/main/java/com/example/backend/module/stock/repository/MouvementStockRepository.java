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

    /**
     * Derniers mouvements sur une liste d'entrepôts.
     * Utilisé par le tableau de bord des non-ADMIN.
     */
    Page<MouvementStock> findByEntrepotIdInOrderByDateCreationDesc(
        List<Long> entrepotIds, Pageable pageable
    );

    /**
     * Top N produits par volume de mouvements (toutes entités confondues).
     * Retourne [produitNom, totalMouvements] trié DESC.
     */
    @Query("SELECT m.produit.nom, COUNT(m) as total FROM MouvementStock m " +
           "WHERE m.type IN ('ENTREE', 'SORTIE') AND m.dateCreation >= :depuis " +
           "GROUP BY m.produit.nom ORDER BY total DESC")
    List<Object[]> findRotationProduits(
        @Param("depuis") java.time.LocalDateTime depuis,
        Pageable pageable
    );

    /**
     * Top N produits pour un périmètre d'entrepôts.
     */
    @Query("SELECT m.produit.nom, COUNT(m) as total FROM MouvementStock m " +
           "WHERE m.type IN ('ENTREE', 'SORTIE') AND m.dateCreation >= :depuis " +
           "AND m.entrepot.id IN :entrepotIds " +
           "GROUP BY m.produit.nom ORDER BY total DESC")
    List<Object[]> findRotationProduitsParEntrepots(
        @Param("depuis") java.time.LocalDateTime depuis,
        @Param("entrepotIds") List<Long> entrepotIds,
        Pageable pageable
    );
}
