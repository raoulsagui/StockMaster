package com.example.backend.module.zone.repository;

import com.example.backend.module.zone.entity.Zone;
import com.example.backend.module.zone.entity.TypeZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA pour l'entité Zone.
 *
 * JpaRepository<Zone, Long> fournit les opérations CRUD de base.
 * Les méthodes ci-dessous sont générées par Spring Data JPA
 * à partir du nom de méthode (query derivation).
 */
@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {

    /**
     * Récupère toutes les zones d'un entrepôt donné.
     * Utilisé pour afficher les zones d'un entrepôt sur la page de détail.
     *
     * Spring Data génère : SELECT * FROM zones WHERE entrepot_id = ?
     *
     * @param entrepotId L'id de l'entrepôt parent
     * @return liste des zones appartenant à cet entrepôt
     */
    List<Zone> findByEntrepotId(Long entrepotId);

    /**
     * Vérifie si une zone avec ce nom existe déjà dans le même entrepôt.
     * Les noms de zones doivent être uniques au sein d'un entrepôt.
     *
     * Spring Data génère : SELECT COUNT(*) > 0 FROM zones WHERE nom = ? AND entrepot_id = ?
     *
     * @param nom       Le nom à vérifier
     * @param entrepotId L'id de l'entrepôt cible
     * @return true si une zone de même nom existe dans cet entrepôt
     */
    boolean existsByNomAndEntrepotId(String nom, Long entrepotId);

    /**
     * Vérifie si une autre zone (différente de l'id donné) porte le même nom
     * dans le même entrepôt. Utilisé lors de la modification.
     *
     * @param nom        Le nom à vérifier
     * @param entrepotId L'id de l'entrepôt
     * @param id         L'id de la zone en cours de modification (à exclure)
     * @return true si un conflit de nom existe
     */
    boolean existsByNomAndEntrepotIdAndIdNot(String nom, Long entrepotId, Long id);

    /**
     * Récupère les zones actives d'un entrepôt.
     * Utilisé lors des mouvements de stock pour proposer uniquement
     * les zones accessibles.
     *
     * @param entrepotId L'id de l'entrepôt
     * @return liste des zones actives de l'entrepôt
     */
    List<Zone> findByEntrepotIdAndActifTrue(Long entrepotId);

    /**
     * Compte le nombre de zones par entrepôt.
     * Utilisé dans les statistiques du tableau de bord.
     *
     * @param entrepotId L'id de l'entrepôt
     * @return nombre total de zones dans l'entrepôt
     */
    long countByEntrepotId(Long entrepotId);

    /**
     * Récupère toutes les zones d'un type donné dans un entrepôt.
     * Ex : toutes les zones RECEPTION de l'entrepôt X.
     *
     * @param entrepotId L'id de l'entrepôt
     * @param type       Le type fonctionnel de zone
     * @return liste des zones filtrées
     */
    List<Zone> findByEntrepotIdAndType(Long entrepotId, TypeZone type);

    /**
     * Somme des capacités allouées de toutes les zones d'un entrepôt.
     * Utilisé pour mettre à jour capaciteUtilisee de l'entrepôt parent.
     */
    @Query("SELECT COALESCE(SUM(z.capacite), 0) FROM Zone z WHERE z.entrepot.id = :entrepotId")
    Double sumCapaciteByEntrepotId(@Param("entrepotId") Long entrepotId);

    /**
     * Somme des capacités allouées en excluant une zone (pour la modification).
     */
    @Query("SELECT COALESCE(SUM(z.capacite), 0) FROM Zone z WHERE z.entrepot.id = :entrepotId AND z.id <> :excludeId")
    Double sumCapaciteByEntrepotIdExcluding(@Param("entrepotId") Long entrepotId, @Param("excludeId") Long excludeId);
}
