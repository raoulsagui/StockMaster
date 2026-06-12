package com.example.backend.module.utilisateur.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Entité JPA représentant un utilisateur de l'application.
 *
 * On implémente UserDetails (interface de Spring Security) directement
 * sur l'entité pour simplifier l'intégration avec Spring Security.
 * Cela évite de créer un wrapper séparé.
 *
 * Annotations Lombok utilisées :
 *   @Data           → génère getters, setters, toString, equals, hashCode
 *   @Builder        → pattern Builder pour la construction d'objets
 *   @NoArgsConstructor → constructeur sans arguments (requis par JPA)
 *   @AllArgsConstructor → constructeur avec tous les arguments (pour @Builder)
 *
 * @Table(name = "utilisateurs") → nom explicite de la table en BDD
 */
@Entity
@Table(name = "utilisateurs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur implements UserDetails {

    /**
     * Clé primaire auto-générée par la BDD.
     * IDENTITY = la BDD s'occupe de l'incrémentation (compatible H2 et PostgreSQL).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String nom;

    /**
     * Email : identifiant de connexion, doit être unique.
     * unique = true → contrainte d'unicité en BDD.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Mot de passe hashé (BCrypt).
     * On ne stocke JAMAIS le mot de passe en clair.
     */
    @Column(nullable = false)
    private String motDePasse;

    /**
     * Rôle de l'utilisateur.
     * @Enumerated(EnumType.STRING) → stocké en texte ("ADMIN", "GESTIONNAIRE"…)
     * et non en entier (0, 1…). Plus lisible en BDD.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Statut du compte. Un compte inactif ne peut pas se connecter.
     */
    @Builder.Default
    private boolean actif = true;

    /**
     * Indique si l'utilisateur doit changer son mot de passe à la prochaine connexion.
     * Mis à true à la création du compte ou après une réinitialisation de mot de passe.
     * Remis à false une fois que l'utilisateur a défini son propre mot de passe.
     */
    @Builder.Default
    private boolean doitChangerMotDePasse = false;

    /**
     * Date de création du compte, remplie automatiquement
     * à la persistance grâce à @PrePersist.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    /**
     * Callback JPA : appelé AVANT l'insertion en BDD.
     * Permet d'initialiser dateCreation automatiquement.
     */
    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }

    // -------------------------------------------------------
    // IMPLÉMENTATION DE UserDetails (Spring Security)
    // Ces méthodes sont appelées par Spring Security pour
    // vérifier les droits et l'état du compte.
    // -------------------------------------------------------

    /**
     * Retourne les autorités (rôles) de l'utilisateur.
     * Spring Security attend le préfixe "ROLE_".
     * Ex : Role.ADMIN → "ROLE_ADMIN"
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Spring Security utilise getUsername() comme identifiant unique.
     * On retourne l'email car c'est lui qui sert à se connecter.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Mot de passe utilisé par Spring Security pour vérifier
     * l'authentification (comparaison avec BCrypt).
     */
    @Override
    public String getPassword() {
        return motDePasse;
    }

    /**
     * isEnabled() → Spring Security refuse la connexion si false.
     * On le branche sur notre champ "actif".
     */
    @Override
    public boolean isEnabled() {
        return actif;
    }

    // Les 3 méthodes suivantes gèrent l'expiration de compte,
    // de credentials, et le verrouillage. On les retourne toujours
    // true pour l'instant (pas gérés dans ce projet).
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}
