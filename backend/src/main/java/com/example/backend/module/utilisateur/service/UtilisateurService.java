package com.example.backend.module.utilisateur.service;

import com.example.backend.module.utilisateur.dto.UtilisateurRequestDTO;
import com.example.backend.module.utilisateur.dto.UtilisateurResponseDTO;
import com.example.backend.module.utilisateur.entity.Utilisateur;
import com.example.backend.module.utilisateur.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

/**
 * Service du module Utilisateur.
 *
 * La couche service contient la LOGIQUE MÉTIER :
 *   - Validation des règles (email unique, mot de passe min 8 car.)
 *   - Hashage du mot de passe avant sauvegarde
 *   - Conversion entité ↔ DTO
 *   - Transactions BDD (@Transactional)
 *
 * Le controller ne fait QUE recevoir la requête HTTP et appeler le service.
 * Le repository ne fait QUE les accès BDD.
 * C'est le pattern "Separation of Concerns" (séparation des responsabilités).
 */
@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // Caractères utilisés pour générer les mots de passe provisoires
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789@#!";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Génère un mot de passe provisoire aléatoire de 10 caractères.
     * Utilise SecureRandom (cryptographiquement sûr).
     */
    private String genererMotDePasseProvisoire() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * Récupère tous les utilisateurs.
     * stream().map() → convertit chaque entité en DTO de réponse.
     */
    public List<UtilisateurResponseDTO> findAll() {
        return utilisateurRepository.findAll()
                .stream()
                .map(UtilisateurResponseDTO::fromEntity)
                .toList();
    }

    /**
     * Récupère un utilisateur par son ID.
     * orElseThrow → lance une exception si non trouvé (géré dans ExceptionHandler).
     */
    public UtilisateurResponseDTO findById(Long id) {
        Utilisateur u = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : id=" + id));
        return UtilisateurResponseDTO.fromEntity(u);
    }

    /**
     * Crée un nouvel utilisateur.
     * Le mot de passe est généré automatiquement et envoyé par email.
     * L'utilisateur devra le changer à sa première connexion.
     */
    @Transactional
    public UtilisateurResponseDTO creer(UtilisateurRequestDTO dto) {
        // Règle métier : l'email doit être unique
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        // Génération du mot de passe provisoire
        String motDePasseProvisoire = genererMotDePasseProvisoire();

        // Construction de l'entité
        Utilisateur utilisateur = Utilisateur.builder()
                .prenom(dto.getPrenom())
                .nom(dto.getNom())
                .email(dto.getEmail())
                .motDePasse(passwordEncoder.encode(motDePasseProvisoire))
                .role(dto.getRole())
                .actif(true)
                // L'utilisateur devra changer son mot de passe à la première connexion
                .doitChangerMotDePasse(true)
                .build();

        Utilisateur saved = utilisateurRepository.save(utilisateur);

        // Envoi de l'email avec les identifiants (hors transaction pour ne pas bloquer)
        emailService.envoyerIdentifiantsCreation(saved.getEmail(), saved.getPrenom(), motDePasseProvisoire);

        return UtilisateurResponseDTO.fromEntity(saved);
    }

    /**
     * Modifie un utilisateur existant.
     * Si motDePasse est renseigné dans le DTO → on le met à jour.
     * Sinon → on conserve l'ancien mot de passe.
     */
    @Transactional
    public UtilisateurResponseDTO modifier(Long id, UtilisateurRequestDTO dto) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : id=" + id));

        // Vérifie que le nouvel email n'appartient pas à un autre utilisateur
        if (!utilisateur.getEmail().equals(dto.getEmail())
                && utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé par un autre compte");
        }

        // Mise à jour des champs
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setNom(dto.getNom());
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setRole(dto.getRole());
        // Le mot de passe n'est plus modifiable via ce formulaire.
        // Utiliser POST /reinitialiser-mdp ou POST /changer-mot-de-passe.

        Utilisateur saved = utilisateurRepository.save(utilisateur);
        return UtilisateurResponseDTO.fromEntity(saved);
    }

    /**
     * Active ou désactive un compte utilisateur.
     * PATCH /utilisateurs/{id}/statut
     */
    @Transactional
    public UtilisateurResponseDTO toggleStatut(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : id=" + id));

        // Inverse l'état actuel
        utilisateur.setActif(!utilisateur.isActif());

        Utilisateur saved = utilisateurRepository.save(utilisateur);
        return UtilisateurResponseDTO.fromEntity(saved);
    }

    /**
     * Réinitialise le mot de passe — génère un nouveau provisoire + envoie par email.
     */
    @Transactional
    public void reinitialiserMotDePasse(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : id=" + id));

        String motDePasseProvisoire = genererMotDePasseProvisoire();
        utilisateur.setMotDePasse(passwordEncoder.encode(motDePasseProvisoire));
        utilisateur.setDoitChangerMotDePasse(true);
        utilisateurRepository.save(utilisateur);

        emailService.envoyerReinitialisationMotDePasse(
                utilisateur.getEmail(), utilisateur.getPrenom(), motDePasseProvisoire);
    }

    /**
     * Permet à un utilisateur connecté de changer son propre mot de passe.
     * Remet doitChangerMotDePasse à false une fois effectué.
     *
     * @param id              ID de l'utilisateur
     * @param ancienMotDePasse Mot de passe actuel (vérifié pour sécurité)
     * @param nouveauMotDePasse Nouveau mot de passe
     */
    @Transactional
    public void changerMotDePasse(Long id, String ancienMotDePasse, String nouveauMotDePasse) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : id=" + id));

        // Si c'est un changement forcé (première connexion / reset),
        // on ne vérifie pas l'ancien mot de passe
        if (!utilisateur.isDoitChangerMotDePasse()) {
            // Changement volontaire : on vérifie l'ancien mot de passe
            if (!passwordEncoder.matches(ancienMotDePasse, utilisateur.getMotDePasse())) {
                throw new RuntimeException("L'ancien mot de passe est incorrect");
            }
        }

        if (nouveauMotDePasse == null || nouveauMotDePasse.length() < 8) {
            throw new RuntimeException("Le mot de passe doit contenir au moins 8 caractères");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
        // L'utilisateur a changé son mot de passe → plus obligatoire
        utilisateur.setDoitChangerMotDePasse(false);
        utilisateurRepository.save(utilisateur);
    }
}
