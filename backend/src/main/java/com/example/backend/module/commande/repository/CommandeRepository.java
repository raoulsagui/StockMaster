package com.example.backend.module.commande.repository;

import com.example.backend.module.commande.entity.Commande;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository JPA pour les commandes fournisseur.
 */
@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {

    /**
     * Trouve une commande par sa référence unique.
     */
    Optional<Commande> findByReference(String reference);

    /**
     * Vérifie si une référence existe (pour la génération séquentielle).
     */
    boolean existsByReference(String reference);

    /**
     * Liste paginée de toutes les commandes, triées par date décroissante.
     * Endpoint principal de la liste.
     */
    Page<Commande> findAllByOrderByDateCreationDesc(Pageable pageable);

    /**
     * Commandes d'un fournisseur donné, triées par date décroissante.
     */
    List<Commande> findByFournisseurIdOrderByDateCreationDesc(Long fournisseurId);

    /**
     * Commandes destinées à un entrepôt donné, triées par date décroissante.
     */
    List<Commande> findByEntrepotIdOrderByDateCreationDesc(Long entrepotId);

    /**
     * Commandes par statut, paginées.
     */
    Page<Commande> findByStatutOrderByDateCreationDesc(
        Commande.StatutCommande statut, Pageable pageable
    );

    /**
     * Compte les commandes par statut.
     * Utilisé pour les statistiques du tableau de bord.
     */
    long countByStatut(Commande.StatutCommande statut);

    /**
     * Compteur annuel pour la génération de la référence séquentielle.
     * Ex : 42 commandes en 2026 → prochain numéro = 43 → CMD-2026-00043
     */
    @Query("""
        SELECT COUNT(c)
        FROM Commande c
        WHERE YEAR(c.dateCreation) = :annee
    """)
    long countByAnnee(@Param("annee") int annee);
}
