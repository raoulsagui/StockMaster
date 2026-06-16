package com.example.backend.module.entrepot.repository;

import com.example.backend.module.entrepot.entity.Entrepot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA pour l'entité Entrepot.
 *
 * JpaRepository<Entrepot, Long> fournit d'emblée :
 *   - save(), findById(), findAll(), deleteById(), count()…
 *
 * Les méthodes supplémentaires ci-dessous sont générées automatiquement
 * par Spring Data JPA à partir du nom de méthode (query derivation).
 */
@Repository
public interface EntrepotRepository extends JpaRepository<Entrepot, Long> {

    /**
     * Vérifie si un entrepôt avec ce nom existe déjà.
     * Utilisé à la création pour garantir l'unicité du nom.
     *
     * Spring Data génère : SELECT COUNT(*) > 0 FROM entrepots WHERE nom = ?
     *
     * @param nom Le nom à vérifier
     * @return true si un entrepôt porte déjà ce nom
     */
    boolean existsByNom(String nom);

    /**
     * Vérifie si un autre entrepôt (différent de l'id donné) porte le même nom.
     * Utilisé lors de la modification pour autoriser de garder le même nom
     * tout en interdisant de prendre le nom d'un autre entrepôt.
     *
     * Spring Data génère : SELECT COUNT(*) > 0 FROM entrepots WHERE nom = ? AND id != ?
     *
     * @param nom Le nom à vérifier
     * @param id  L'id de l'entrepôt en cours de modification (à exclure)
     * @return true si un autre entrepôt porte ce nom
     */
    boolean existsByNomAndIdNot(String nom, Long id);

    /**
     * Récupère tous les entrepôts actifs.
     * Utilisé lors de la création d'une zone pour proposer uniquement
     * les entrepôts accessibles.
     *
     * @return liste des entrepôts dont actif = true
     */
    List<Entrepot> findByActifTrue();
}
