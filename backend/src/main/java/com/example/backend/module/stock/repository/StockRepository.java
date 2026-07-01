package com.example.backend.module.stock.repository;

import com.example.backend.module.stock.entity.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository JPA pour l'entité Stock.
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    /**
     * Trouve le stock d'un produit dans un entrepôt spécifique.
     * Utilisé pour mettre à jour le stock lors d'une entrée ou sortie.
     *
     * Lock PESSIMISTIC_WRITE : évite les lectures périmées en concurrence.
     * La ligne est verrouillée jusqu'à la fin de la transaction.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.produit.id = :produitId AND s.entrepot.id = :entrepotId")
    Optional<Stock> findByProduitIdAndEntrepotId(Long produitId, Long entrepotId);

    /**
     * Vérifie si un stock existe pour une combinaison produit/entrepôt.
     */
    boolean existsByProduitIdAndEntrepotId(Long produitId, Long entrepotId);

    /**
     * Tous les stocks d'un entrepôt donné.
     * Utilisé sur la page de détail d'un entrepôt.
     */
    List<Stock> findByEntrepotId(Long entrepotId);

    /**
     * Stocks sur une liste d'entrepôts.
     * Utilisé par le tableau de bord des non-ADMIN.
     */
    List<Stock> findByEntrepotIdIn(List<Long> entrepotIds);

    /**
     * Tous les stocks d'un produit donné (dans tous les entrepôts).
     * Utile pour voir la répartition d'un produit.
     */
    List<Stock> findByProduitId(Long produitId);

    /**
     * Stocks en dessous du seuil minimum (stock faible).
     * Utilisé par le module 12 (alertes).
     *
     * La requête JPQL compare quantiteDisponible avec stockMinimum.
     * Les lignes sans seuil configuré (stockMinimum = null) sont ignorées.
     */
    @Query("SELECT s FROM Stock s WHERE s.stockMinimum IS NOT NULL AND s.quantiteDisponible <= s.stockMinimum")
    List<Stock> findStocksEnAlerte();

    /**
     * Nombre de produits en stock dans un entrepôt.
     * Utilisé pour les KPI du tableau de bord.
     */
    long countByEntrepotId(Long entrepotId);
}
