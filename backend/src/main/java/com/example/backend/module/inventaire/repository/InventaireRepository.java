package com.example.backend.module.inventaire.repository;

import com.example.backend.module.inventaire.entity.Inventaire;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository JPA pour le module Inventaires.
 */
@Repository
public interface InventaireRepository extends JpaRepository<Inventaire, Long> {

    /**
     * Trouve un inventaire par sa référence unique.
     */
    Optional<Inventaire> findByReference(String reference);

    /**
     * Vérifie si une référence existe déjà (pour l'unicité lors de la génération).
     */
    boolean existsByReference(String reference);

    /**
     * Tous les inventaires d'un entrepôt, triés par date de création décroissante.
     */
    List<Inventaire> findByEntrepotIdOrderByDateCreationDesc(Long entrepotId);

    /**
     * Inventaires filtrés par statut, paginés.
     * Utilisé par le tableau de bord et la liste avec filtres.
     */
    Page<Inventaire> findByStatutOrderByDateCreationDesc(
        Inventaire.StatutInventaire statut, Pageable pageable
    );

    /**
     * Tous les inventaires paginés, triés par date décroissante.
     */
    Page<Inventaire> findAllByOrderByDateCreationDesc(Pageable pageable);

    /**
     * Statistiques : compte les inventaires par statut.
     * Utilisé pour les KPI du tableau de bord.
     */
    long countByStatut(Inventaire.StatutInventaire statut);

    /**
     * Vérifie s'il existe un inventaire EN_COURS ou BROUILLON pour un entrepôt donné.
     * Un seul inventaire actif par entrepôt est autorisé à la fois.
     */
    @Query("""
        SELECT COUNT(i) > 0
        FROM Inventaire i
        WHERE i.entrepot.id = :entrepotId
          AND i.statut IN ('BROUILLON', 'EN_COURS')
    """)
    boolean existsInventaireActifPourEntrepot(@Param("entrepotId") Long entrepotId);

    /**
     * Dernier compteur d'inventaire pour l'année en cours.
     * Utilisé pour générer la référence séquentielle INV-AAAA-XXXXX.
     */
    @Query("""
        SELECT COUNT(i)
        FROM Inventaire i
        WHERE YEAR(i.dateCreation) = :annee
    """)
    long countByAnnee(@Param("annee") int annee);
}
