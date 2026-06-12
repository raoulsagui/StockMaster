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
 * On crée un compte ADMIN par défaut si la BDD est vide.
 * Pratique en développement pour ne pas repartir de zéro à chaque redémarrage.
 *
 * Identifiants de dev :
 *   Email    : admin@stockmaster.fr
 *   Mot de passe : Admin@1234
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // On ne crée le compte que si aucun utilisateur n'existe
        if (utilisateurRepository.count() == 0) {

            Utilisateur admin = Utilisateur.builder()
                    .prenom("Admin")
                    .nom("StockMaster")
                    .email("admin@stockmaster.fr")
                    // Le mot de passe est hashé avec BCrypt
                    .motDePasse(passwordEncoder.encode("Admin@1234"))
                    .role(Role.ADMIN)
                    .actif(true)
                    .build();

            utilisateurRepository.save(admin);
            System.out.println("✅ Compte admin créé : admin@stockmaster.fr / Admin@1234");
        }
    }
}
