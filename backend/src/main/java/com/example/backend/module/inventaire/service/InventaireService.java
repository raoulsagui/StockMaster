package com.example.backend.module.inventaire.service;

import com.example.backend.module.entrepot.entity.Entrepot;
import com.example.backend.module.entrepot.repository.EntrepotRepository;
import com.example.backend.module.inventaire.dto.InventaireRequestDTO;
import com.example.backend.module.inventaire.dto.InventaireResponseDTO;
import com.example.backend.module.inventaire.dto.LigneInventaireRequestDTO;
import com.example.backend.module.inventaire.dto.LigneInventaireResponseDTO;
import com.example.backend.module.inventaire.entity.Inventaire;
import com.example.backend.module.inventaire.entity.LigneInventaire;
import com.example.backend.module.inventaire.repository.InventaireRepository;
import com.example.backend.module.inventaire.repository.LigneInventaireRepository;
import com.example.backend.module.produit.entity.Produit;
import com.example.backend.module.produit.repository.ProduitRepository;
import com.example.backend.module.stock.entity.MouvementStock;
import com.example.backend.module.stock.entity.Stock;
import com.example.backend.module.stock.repository.StockRepository;
import com.example.backend.module.stock.service.StockService;
import com.example.backend.module.utilisateur.entity.Utilisateur;
import com.example.backend.module.utilisateur.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service central du module Inventaire.
 *
 * Gère le cycle de vie complet d'un inventaire physique :
 *   1. Création (BROUILLON) avec génération des lignes
 *   2. Démarrage du comptage (EN_COURS)
 *   3. Saisie des quantités réelles par ligne
 *   4. Validation et application des ajustements de stock
 *
 * La validation appelle StockService pour ajuster les stocks
 * via des mouvements de type AJUSTEMENT.
 */
@Service
@RequiredArgsConstructor
public class InventaireService {

    private final InventaireRepository        inventaireRepository;
    private final LigneInventaireRepository   ligneRepository;
    private final StockRepository             stockRepository;
    private final StockService                stockService;
    private final EntrepotRepository          entrepotRepository;
    private final ProduitRepository           produitRepository;
    private final UtilisateurRepository       utilisateurRepository;

    // -------------------------------------------------------
    // CONSULTATION
    // -------------------------------------------------------

    /**
     * Liste paginée de tous les inventaires, triés par date décroissante.
     */
    public Page<InventaireResponseDTO> findAll(int page, int size) {
        return inventaireRepository
                .findAllByOrderByDateCreationDesc(PageRequest.of(page, size))
                .map(InventaireResponseDTO::fromEntity);
    }

    /**
     * Inventaires d'un entrepôt spécifique.
     */
    public List<InventaireResponseDTO> findByEntrepot(Long entrepotId) {
        return inventaireRepository
                .findByEntrepotIdOrderByDateCreationDesc(entrepotId)
                .stream()
                .map(InventaireResponseDTO::fromEntity)
                .toList();
    }

    /**
     * Détail d'un inventaire avec toutes ses lignes.
     */
    public InventaireResponseDTO findById(Long id) {
        return InventaireResponseDTO.fromEntityWithLignes(findOrThrow(id));
    }

    /**
     * Statistiques globales pour le tableau de bord.
     */
    public InventaireStatsDTO getStats() {
        return InventaireStatsDTO.builder()
                .totalInventaires(inventaireRepository.count())
                .enCours(inventaireRepository.countByStatut(Inventaire.StatutInventaire.EN_COURS))
                .brouillons(inventaireRepository.countByStatut(Inventaire.StatutInventaire.BROUILLON))
                .valides(inventaireRepository.countByStatut(Inventaire.StatutInventaire.VALIDE))
                .build();
    }

    // -------------------------------------------------------
    // CRÉATION
    // -------------------------------------------------------

    /**
     * Crée un nouvel inventaire en statut BROUILLON.
     *
     * Pour un inventaire COMPLET :
     *   - Charge tous les stocks de l'entrepôt
     *   - Crée une ligne par produit avec la quantité théorique actuelle
     *
     * Pour un inventaire PARTIEL :
     *   - Crée uniquement les lignes pour les produits sélectionnés
     *
     * @throws RuntimeException si un inventaire actif existe déjà pour cet entrepôt
     * @throws RuntimeException si un inventaire PARTIEL ne contient aucun produit
     */
    @Transactional
    public InventaireResponseDTO creer(InventaireRequestDTO dto) {
        // Validation : un seul inventaire actif par entrepôt
        if (inventaireRepository.existsInventaireActifPourEntrepot(dto.getEntrepotId())) {
            throw new RuntimeException(
                "Un inventaire est déjà en cours ou en brouillon pour cet entrepôt. "
                + "Veuillez le clôturer ou l'annuler avant d'en créer un nouveau."
            );
        }

        // Validation : inventaire PARTIEL doit avoir des produits
        if (dto.getType() == Inventaire.TypeInventaire.PARTIEL
                && (dto.getProduitsIds() == null || dto.getProduitsIds().isEmpty())) {
            throw new RuntimeException(
                "Un inventaire partiel doit contenir au moins un produit."
            );
        }

        Entrepot entrepot = entrepotRepository.findById(dto.getEntrepotId())
                .orElseThrow(() -> new RuntimeException("Entrepôt introuvable : id=" + dto.getEntrepotId()));

        Utilisateur createur = getUtilisateurConnecte();

        // Génère la référence séquentielle
        String reference = genererReference();

        // Construit l'entité inventaire
        Inventaire inventaire = Inventaire.builder()
                .reference(reference)
                .type(dto.getType())
                .statut(Inventaire.StatutInventaire.BROUILLON)
                .entrepot(entrepot)
                .createur(createur)
                .note(dto.getNote())
                .build();

        // Sauvegarde l'inventaire pour obtenir un ID avant de lier les lignes
        Inventaire saved = inventaireRepository.save(inventaire);

        // Génère les lignes selon le type
        List<Stock> stocks = chargerStocksRelevants(dto, entrepot.getId());

        if (stocks.isEmpty()) {
            throw new RuntimeException(
                "Aucun stock trouvé pour cet entrepôt. "
                + "Assurez-vous qu'il contient des produits avant de créer un inventaire."
            );
        }

        List<LigneInventaire> lignes = stocks.stream()
                .map(stock -> LigneInventaire.builder()
                        .inventaire(saved)
                        .produit(stock.getProduit())
                        .quantiteTheorique(stock.getQuantiteDisponible())
                        .comptee(false)
                        .build())
                .toList();

        ligneRepository.saveAll(lignes);
        saved.getLignes().addAll(lignes);

        return InventaireResponseDTO.fromEntityWithLignes(saved);
    }

    // -------------------------------------------------------
    // DÉMARRAGE
    // -------------------------------------------------------

    /**
     * Passe l'inventaire en statut EN_COURS.
     * Seul un inventaire en BROUILLON peut être démarré.
     */
    @Transactional
    public InventaireResponseDTO demarrer(Long id) {
        Inventaire inventaire = findOrThrow(id);

        if (inventaire.getStatut() != Inventaire.StatutInventaire.BROUILLON) {
            throw new RuntimeException(
                "Seul un inventaire en BROUILLON peut être démarré. "
                + "Statut actuel : " + inventaire.getStatut()
            );
        }

        inventaire.setStatut(Inventaire.StatutInventaire.EN_COURS);
        return InventaireResponseDTO.fromEntityWithLignes(inventaireRepository.save(inventaire));
    }

    // -------------------------------------------------------
    // SAISIE DES QUANTITÉS
    // -------------------------------------------------------

    /**
     * Saisit ou met à jour la quantité réelle comptée pour une ligne.
     *
     * La ligne doit appartenir à un inventaire EN_COURS.
     * La saisie peut être faite plusieurs fois (correction possible).
     */
    @Transactional
    public LigneInventaireResponseDTO saisirQuantite(Long inventaireId, Long ligneId,
                                                      LigneInventaireRequestDTO dto) {
        Inventaire inventaire = findOrThrow(inventaireId);

        if (inventaire.getStatut() != Inventaire.StatutInventaire.EN_COURS) {
            throw new RuntimeException(
                "La saisie n'est possible que sur un inventaire EN_COURS. "
                + "Statut actuel : " + inventaire.getStatut()
            );
        }

        LigneInventaire ligne = ligneRepository.findById(ligneId)
                .orElseThrow(() -> new RuntimeException("Ligne d'inventaire introuvable : id=" + ligneId));

        if (!ligne.getInventaire().getId().equals(inventaireId)) {
            throw new RuntimeException("Cette ligne n'appartient pas à cet inventaire.");
        }

        ligne.setQuantiteComptee(dto.getQuantiteComptee());
        ligne.setNote(dto.getNote());
        ligne.setComptee(true);
        ligne.setDateSaisie(LocalDateTime.now());

        return LigneInventaireResponseDTO.fromEntity(ligneRepository.save(ligne));
    }

    // -------------------------------------------------------
    // VALIDATION
    // -------------------------------------------------------

    /**
     * Valide l'inventaire et applique les ajustements de stock.
     *
     * Pour chaque ligne avec un écart :
     *   - Écart positif → ajouterStock() avec type AJUSTEMENT
     *   - Écart négatif → retirerStock() avec type AJUSTEMENT
     *
     * Une fois validé, le statut passe à VALIDE et les ajustements
     * sont traçables dans l'historique des mouvements de stock.
     *
     * @throws RuntimeException si l'inventaire n'est pas EN_COURS
     * @throws RuntimeException si toutes les lignes n'ont pas été comptées
     */
    @Transactional
    public InventaireResponseDTO valider(Long id) {
        Inventaire inventaire = findOrThrow(id);

        if (inventaire.getStatut() != Inventaire.StatutInventaire.EN_COURS) {
            throw new RuntimeException(
                "Seul un inventaire EN_COURS peut être validé. "
                + "Statut actuel : " + inventaire.getStatut()
            );
        }

        if (!inventaire.estComplet()) {
            long nonComptes = ligneRepository.findByInventaireIdAndCompteeFalse(id).size();
            throw new RuntimeException(
                "Impossible de valider : " + nonComptes
                + " ligne(s) n'ont pas encore été comptées."
            );
        }

        // Applique les ajustements de stock pour chaque ligne avec écart
        for (LigneInventaire ligne : inventaire.getLignes()) {
            int ecart = ligne.getEcart();
            if (ecart == 0) continue;

            String reference = "INV-" + inventaire.getReference();
            String note = "Ajustement inventaire " + inventaire.getReference()
                        + " — théorique: " + ligne.getQuantiteTheorique()
                        + ", compté: " + ligne.getQuantiteComptee();

            if (ecart > 0) {
                // Surplus : on ajoute au stock
                stockService.ajouterStock(
                    ligne.getProduit().getId(),
                    inventaire.getEntrepot().getId(),
                    ecart,
                    MouvementStock.TypeMouvement.AJUSTEMENT,
                    reference,
                    note
                );
            } else {
                // Manque : on retire du stock (ecart est négatif, on prend la valeur abs)
                stockService.retirerStock(
                    ligne.getProduit().getId(),
                    inventaire.getEntrepot().getId(),
                    Math.abs(ecart),
                    MouvementStock.TypeMouvement.AJUSTEMENT,
                    reference,
                    note
                );
            }
        }

        // Clôture l'inventaire
        Utilisateur valideur = getUtilisateurConnecte();
        inventaire.setStatut(Inventaire.StatutInventaire.VALIDE);
        inventaire.setValideur(valideur);
        inventaire.setDateValidation(LocalDateTime.now());

        return InventaireResponseDTO.fromEntityWithLignes(inventaireRepository.save(inventaire));
    }

    // -------------------------------------------------------
    // ANNULATION
    // -------------------------------------------------------

    /**
     * Annule un inventaire en BROUILLON ou EN_COURS.
     * Aucun ajustement de stock n'est appliqué.
     */
    @Transactional
    public InventaireResponseDTO annuler(Long id) {
        Inventaire inventaire = findOrThrow(id);

        if (inventaire.getStatut() == Inventaire.StatutInventaire.VALIDE) {
            throw new RuntimeException("Un inventaire validé ne peut pas être annulé.");
        }

        if (inventaire.getStatut() == Inventaire.StatutInventaire.ANNULE) {
            throw new RuntimeException("Cet inventaire est déjà annulé.");
        }

        inventaire.setStatut(Inventaire.StatutInventaire.ANNULE);
        return InventaireResponseDTO.fromEntityWithLignes(inventaireRepository.save(inventaire));
    }

    // -------------------------------------------------------
    // MÉTHODES PRIVÉES
    // -------------------------------------------------------

    /**
     * Charge les stocks pertinents selon le type d'inventaire.
     */
    private List<Stock> chargerStocksRelevants(InventaireRequestDTO dto, Long entrepotId) {
        if (dto.getType() == Inventaire.TypeInventaire.COMPLET) {
            return stockRepository.findByEntrepotId(entrepotId);
        } else {
            // PARTIEL : uniquement les produits sélectionnés
            return dto.getProduitsIds().stream()
                    .map(produitId -> stockRepository
                            .findByProduitIdAndEntrepotId(produitId, entrepotId)
                            .orElseThrow(() -> new RuntimeException(
                                "Aucun stock trouvé pour le produit id=" + produitId
                                + " dans cet entrepôt."
                            ))
                    )
                    .toList();
        }
    }

    /**
     * Génère une référence unique au format INV-AAAA-NNNNN.
     * Ex : INV-2026-00001, INV-2026-00002, ...
     */
    private String genererReference() {
        int annee = LocalDate.now().getYear();
        long compteur = inventaireRepository.countByAnnee(annee) + 1;
        return String.format("INV-%d-%05d", annee, compteur);
    }

    /**
     * Récupère l'utilisateur connecté depuis le contexte Spring Security.
     */
    private Utilisateur getUtilisateurConnecte() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable"));
    }

    private Inventaire findOrThrow(Long id) {
        return inventaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventaire introuvable : id=" + id));
    }

    // -------------------------------------------------------
    // DTO INTERNE STATS
    // -------------------------------------------------------

    @lombok.Data
    @lombok.Builder
    public static class InventaireStatsDTO {
        private long totalInventaires;
        private long enCours;
        private long brouillons;
        private long valides;
    }
}
