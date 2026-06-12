package com.example.backend.module.utilisateur.repository;

import com.example.backend.module.utilisateur.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository JPA pour l'entité Utilisateur.
 *
 * JpaRepository<Utilisateur, Long> nous donne gratuitement :
 *   - save(utilisateur)          → INSERT ou UPDATE
 *   - findById(id)               → SELECT par id
 *   - findAll()                  → SELECT tous
 *   - deleteById(id)             → DELETE par id
 *   - count()                    → COUNT(*)
 *   ... et bien d'autres.
 *
 * On ajoute uniquement les méthodes dont on a besoin en plus.
 * Spring Data JPA génère l'implémentation automatiquement
 * à partir du nom de la méthode (query derivation).
 */
@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    /**
     * Cherche un utilisateur par son email.
     * Utilisé par Spring Security lors de l'authentification
     * pour charger l'utilisateur à partir de son email.
     *
     * Spring Data génère : SELECT * FROM utilisateurs WHERE email = ?
     */
    Optional<Utilisateur> findByEmail(String email);

    /**
     * Vérifie si un email est déjà utilisé.
     * Utilisé lors de la création pour éviter les doublons.
     *
     * Spring Data génère : SELECT COUNT(*) > 0 FROM utilisateurs WHERE email = ?
     */
    boolean existsByEmail(String email);
}
