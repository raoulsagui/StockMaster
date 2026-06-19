package com.example.backend.module.inventaire.repository;

import com.example.backend.module.inventaire.entity.LigneInventaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository JPA pour les lignes d'inventaire.
 */
@Repository
public interface LigneInventaireRepository extends JpaRepository<LigneInventaire, Long> {

    /**
     * Toutes les lignes d'un inventaire donné.
     */
    List<LigneInventaire> findByInventaireIdOrderByProduitNomAsc(Long inventaireId);

    /**
     * Ligne d'inventaire pour un produit donné dans un inventaire.
     */
    Optional<LigneInventaire> findByInventaireIdAndProduitId(Long inventaireId, Long produitId);

    /**
     * Lignes non encore comptées d'un inventaire (pour le suivi de progression).
     */
    List<LigneInventaire> findByInventaireIdAndCompteeFalse(Long inventaireId);

    /**
     * Lignes avec un écart (quantite comptée ≠ théorique).
     * Utilisé pour la vue "résultat" avant validation.
     */
    @Query("""
        SELECT l FROM LigneInventaire l
        WHERE l.inventaire.id = :inventaireId
          AND l.comptee = true
          AND l.quantiteComptee <> l.quantiteTheorique
        ORDER BY l.produit.nom ASC
    """)
    List<LigneInventaire> findLignesAvecEcart(@Param("inventaireId") Long inventaireId);

    /**
     * Lignes avec écart positif (surplus).
     */
    @Query("""
        SELECT l FROM LigneInventaire l
        WHERE l.inventaire.id = :inventaireId
          AND l.comptee = true
          AND l.quantiteComptee > l.quantiteTheorique
        ORDER BY l.produit.nom ASC
    """)
    List<LigneInventaire> findLignesEcartPositif(@Param("inventaireId") Long inventaireId);

    /**
     * Lignes avec écart négatif (manque).
     */
    @Query("""
        SELECT l FROM LigneInventaire l
        WHERE l.inventaire.id = :inventaireId
          AND l.comptee = true
          AND l.quantiteComptee < l.quantiteTheorique
        ORDER BY l.produit.nom ASC
    """)
    List<LigneInventaire> findLignesEcartNegatif(@Param("inventaireId") Long inventaireId);

    /**
     * Vérifie si un produit est déjà dans un inventaire donné.
     */
    boolean existsByInventaireIdAndProduitId(Long inventaireId, Long produitId);
}
