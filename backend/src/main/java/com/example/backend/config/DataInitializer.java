package com.example.backend.config;

import com.example.backend.module.utilisateur.entity.Role;
import com.example.backend.module.utilisateur.entity.Utilisateur;
import com.example.backend.module.utilisateur.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initialisation des données de démarrage.
 *
 * CommandLineRunner : Spring Boot exécute cette classe au démarrage,
 * après que le contexte Spring soit complètement initialisé.
 *
 * Crée les données de démonstration si la BDD est vide :
 *   - 1 compte ADMIN + 1 compte GESTIONNAIRE
 *   - 2 entrepôts avec zones de stockage
 *
 * Identifiants de dev :
 *   admin@stockmaster.fr        / Admin@1234
 *   gestionnaire@stockmaster.fr / Gestionnaire@1234
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // On ne crée les comptes que si aucun utilisateur n'existe
        if (utilisateurRepository.count() > 0) return;

        // -------------------------------------------------------
        // COMPTES PAR DÉFAUT
        // -------------------------------------------------------
        Utilisateur admin = Utilisateur.builder()
                .prenom("Admin")
                .nom("StockMaster")
                .email("admin@stockmaster.fr")
                .motDePasse(passwordEncoder.encode("Admin@1234"))
                .role(Role.ADMIN)
                .actif(true)
                .build();
        utilisateurRepository.save(admin);
        System.out.println("✅ Compte admin créé : admin@stockmaster.fr / Admin@1234");

        Utilisateur gestionnaire = Utilisateur.builder()
                .prenom("Jean")
                .nom("Dupont")
                .email("gestionnaire@stockmaster.fr")
                .motDePasse(passwordEncoder.encode("Gestionnaire@1234"))
                .role(Role.GESTIONNAIRE)
                .actif(true)
                .build();
        utilisateurRepository.save(gestionnaire);
        System.out.println("✅ Compte gestionnaire créé : gestionnaire@stockmaster.fr / Gestionnaire@1234");
    }
}
