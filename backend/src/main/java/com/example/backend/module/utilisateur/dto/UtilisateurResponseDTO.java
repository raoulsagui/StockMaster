package com.example.backend.module.utilisateur.dto;

import com.example.backend.module.utilisateur.entity.Role;
import com.example.backend.module.utilisateur.entity.Utilisateur;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de réponse : ce qu'on renvoie au client pour un utilisateur.
 *
 * Points importants :
 *   - motDePasse est ABSENT : on ne renvoie jamais le hash du mot de passe
 *   - On expose uniquement ce dont le frontend a besoin
 *
 * La méthode statique fromEntity() est un "factory method" qui
 * convertit une entité Utilisateur en DTO de réponse.
 * C'est plus simple que MapStruct pour ce projet.
 */
@Data
@Builder
public class UtilisateurResponseDTO {

    private Long id;
    private String prenom;
    private String nom;
    private String email;
    private Role role;
    private boolean actif;
    /** Flag : l'utilisateur doit-il changer son mot de passe ? */
    private boolean doitChangerMotDePasse;
    private LocalDateTime dateCreation;

    /**
     * Convertit une entité Utilisateur en DTO de réponse.
     * Appelé dans le service après chaque opération.
     *
     * @param u L'entité Utilisateur récupérée de la BDD
     * @return  Le DTO prêt à être sérialisé en JSON
     */
    public static UtilisateurResponseDTO fromEntity(Utilisateur u) {
        return UtilisateurResponseDTO.builder()
                .id(u.getId())
                .prenom(u.getPrenom())
                .nom(u.getNom())
                .email(u.getEmail())
                .role(u.getRole())
                .actif(u.isActif())
                .doitChangerMotDePasse(u.isDoitChangerMotDePasse())
                .dateCreation(u.getDateCreation())
                .build();
    }
}
