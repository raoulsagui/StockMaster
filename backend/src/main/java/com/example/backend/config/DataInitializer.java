package com.example.backend.config;

import com.example.backend.module.entrepot.entity.Entrepot;
import com.example.backend.module.entrepot.repository.EntrepotRepository;
import com.example.backend.module.utilisateur.entity.Role;
import com.example.backend.module.utilisateur.entity.Utilisateur;
import com.example.backend.module.utilisateur.repository.UtilisateurRepository;
import com.example.backend.module.zone.entity.TypeZone;
import com.example.backend.module.zone.entity.Zone;
import com.example.backend.module.zone.repository.ZoneRepository;
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
 *   admin@stockmaster.fr     / Admin@1234
 *   gestionnaire@stockmaster.fr / Gestionnaire@1234
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final EntrepotRepository entrepotRepository;
    private final ZoneRepository zoneRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // On ne crée les données que si aucun utilisateur n'existe
        if (utilisateurRepository.count() > 0) return;

        // -------------------------------------------------------
        // UTILISATEURS
        // -------------------------------------------------------
        Utilisateur admin = Utilisateur.builder()
                .prenom("Admin")
                .nom("StockMaster")
                .email("admin@stockmaster.fr")
                .motDePasse(passwordEncoder.encode("Admin@1234"))
                .role(Role.ADMIN)
                .actif(true)
                .build();
        admin = utilisateurRepository.save(admin);
        System.out.println("✅ Compte admin créé : admin@stockmaster.fr / Admin@1234");

        Utilisateur gestionnaire = Utilisateur.builder()
                .prenom("Jean")
                .nom("Dupont")
                .email("gestionnaire@stockmaster.fr")
                .motDePasse(passwordEncoder.encode("Gestionnaire@1234"))
                .role(Role.GESTIONNAIRE)
                .actif(true)
                .build();
        gestionnaire = utilisateurRepository.save(gestionnaire);
        System.out.println("✅ Compte gestionnaire créé : gestionnaire@stockmaster.fr / Gestionnaire@1234");

        // -------------------------------------------------------
        // ENTREPÔTS
        // -------------------------------------------------------

        // Entrepôt 1 — Principal Paris
        Entrepot entrepot1 = Entrepot.builder()
                .nom("Entrepôt Principal Paris")
                .adresse("15 Rue de la Logistique, 75001 Paris")
                .capaciteTotale(5000.0)
                .capaciteUtilisee(2150.0)
                .responsable(gestionnaire)
                .actif(true)
                .build();
        entrepot1 = entrepotRepository.save(entrepot1);
        System.out.println("✅ Entrepôt créé : " + entrepot1.getNom());

        // Entrepôt 2 — Dépôt Sud Lyon
        Entrepot entrepot2 = Entrepot.builder()
                .nom("Dépôt Sud Lyon")
                .adresse("8 Avenue Industrielle, 69007 Lyon")
                .capaciteTotale(2000.0)
                .capaciteUtilisee(300.0)
                .responsable(admin)
                .actif(true)
                .build();
        entrepot2 = entrepotRepository.save(entrepot2);
        System.out.println("✅ Entrepôt créé : " + entrepot2.getNom());

        // -------------------------------------------------------
        // ZONES — Entrepôt 1
        // -------------------------------------------------------
        zoneRepository.save(Zone.builder()
                .nom("Zone Réception A")
                .type(TypeZone.RECEPTION)
                .description("Zone de déchargement Nord — quais 1 à 4")
                .capaciteTotale(500.0)
                .capaciteUtilisee(120.0)
                .entrepot(entrepot1)
                .actif(true)
                .build());

        zoneRepository.save(Zone.builder()
                .nom("Allée Stockage B1")
                .type(TypeZone.STOCKAGE)
                .description("Rayonnages haute densité — produits secs")
                .capaciteTotale(3000.0)
                .capaciteUtilisee(1800.0)
                .entrepot(entrepot1)
                .actif(true)
                .build());

        zoneRepository.save(Zone.builder()
                .nom("Allée Stockage B2")
                .type(TypeZone.STOCKAGE)
                .description("Rayonnages — produits réfrigérés (2–8°C)")
                .capaciteTotale(800.0)
                .capaciteUtilisee(230.0)
                .entrepot(entrepot1)
                .actif(true)
                .build());

        zoneRepository.save(Zone.builder()
                .nom("Zone Expédition C")
                .type(TypeZone.EXPEDITION)
                .description("Préparation et chargement des commandes — quais 5 à 8")
                .capaciteTotale(700.0)
                .capaciteUtilisee(0.0)
                .entrepot(entrepot1)
                .actif(true)
                .build());

        // -------------------------------------------------------
        // ZONES — Entrepôt 2
        // -------------------------------------------------------
        zoneRepository.save(Zone.builder()
                .nom("Réception Lyon")
                .type(TypeZone.RECEPTION)
                .description("Zone d'entrée des marchandises")
                .capaciteTotale(200.0)
                .capaciteUtilisee(50.0)
                .entrepot(entrepot2)
                .actif(true)
                .build());

        zoneRepository.save(Zone.builder()
                .nom("Stock Central Lyon")
                .type(TypeZone.STOCKAGE)
                .description("Stockage général")
                .capaciteTotale(1600.0)
                .capaciteUtilisee(250.0)
                .entrepot(entrepot2)
                .actif(true)
                .build());

        zoneRepository.save(Zone.builder()
                .nom("Expédition Lyon")
                .type(TypeZone.EXPEDITION)
                .description("Zone de départ des commandes")
                .capaciteTotale(200.0)
                .capaciteUtilisee(0.0)
                .entrepot(entrepot2)
                .actif(true)
                .build());

        System.out.println("✅ Zones créées pour les deux entrepôts");
    }
}
