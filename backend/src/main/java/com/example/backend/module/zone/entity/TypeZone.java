package com.example.backend.module.zone.entity;

/**
 * Enumération des types fonctionnels de zones de stockage.
 *
 * Un entrepôt peut être découpé en trois types de zones
 * selon le flux logistique des marchandises :
 *
 *   RECEPTION  → Zone d'entrée : déchargement, contrôle qualité, étiquetage
 *   STOCKAGE   → Zone principale : conservation des produits en attente
 *   EXPEDITION → Zone de sortie : préparation des commandes, chargement
 *
 * Ces types sont stockés en base sous forme de texte ("RECEPTION" etc.)
 * grâce à @Enumerated(EnumType.STRING) sur l'entité Zone.
 */
public enum TypeZone {

    /** Zone de réception et contrôle des marchandises entrantes */
    RECEPTION,

    /** Zone de stockage et conservation des produits */
    STOCKAGE,

    /** Zone de préparation et expédition des commandes */
    EXPEDITION
}
