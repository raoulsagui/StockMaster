package com.example.backend.module.utilisateur.entity;

/**
 * Enumération des rôles disponibles dans l'application.
 *
 * Spring Security utilise le préfixe "ROLE_" par convention.
 * On le gère dans SecurityConfig via hasRole() ou hasAuthority().
 *
 * Rôles définis dans le CDC :
 *   ADMIN        → accès complet
 *   GESTIONNAIRE → gestion des entrepôts
 *   MAGASINIER   → saisie des mouvements de stock
 *   AUDITEUR     → lecture seule
 */
public enum Role {
    ADMIN,
    GESTIONNAIRE,
    MAGASINIER,
    AUDITEUR
}
