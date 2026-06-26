package com.example.backend.module.stock.service;

import com.example.backend.module.entrepot.entity.Entrepot;
import com.example.backend.module.entrepot.repository.EntrepotRepository;
import com.example.backend.module.produit.entity.Produit;
import com.example.backend.module.produit.repository.ProduitRepository;
import com.example.backend.module.stock.dto.MouvementStockResponseDTO;
import com.example.backend.module.stock.dto.StockResponseDTO;
import com.example.backend.module.stock.dto.StockSeuilRequestDTO;
import com.example.backend.module.stock.entity.MouvementStock;
import com.example.backend.module.stock.entity.Stock;
import com.example.backend.module.stock.repository.MouvementStockRepository;
import com.example.backend.module.stock.repository.StockRepository;
import com.example.backend.module.utilisateur.entity.Utilisateur;
import com.example.backend.module.utilisateur.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service central du module Stock.
 *
 * Ce service est utilisé par :
 *   - StockController (consultation + configuration des seuils)
 *   - EntreeService (module 8) : appelle ajouterStock()
 *   - SortieService (module 9) : appelle retirerStock()
 *   - TransfertService (module 10) : appelle reserverStock() / libererReservation()
 *
 * Toutes les modifications de stock créent un mouvement dans MouvementStock
 * pour la traçabilité complète (module 15).
 */
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final MouvementStockRepository mouvementRepository;
    private final ProduitRepository produitRepository;
    private final EntrepotRepository entrepotRepository;
    private final UtilisateurRepository utilisateurRepository;

    // -------------------------------------------------------
    // CONSULTATION
    // -------------------------------------------------------

    /** Tous les stocks (toutes combinaisons produit/entrepôt) */
    public List<StockResponseDTO> findAll() {
        return stockRepository.findAll()
                .stream()
                .map(StockResponseDTO::fromEntity)
                .toList();
    }

    /** Stocks d'un entrepôt spécifique */
    public List<StockResponseDTO> findByEntrepot(Long entrepotId) {
        return stockRepository.findByEntrepotId(entrepotId)
                .stream()
                .map(StockResponseDTO::fromEntity)
                .toList();
    }

    /** Stocks d'un produit dans tous les entrepôts */
    public List<StockResponseDTO> findByProduit(Long produitId) {
        return stockRepository.findByProduitId(produitId)
                .stream()
                .map(StockResponseDTO::fromEntity)
                .toList();
    }

    /** Un stock spécifique par son ID */
    public StockResponseDTO findById(Long id) {
        return StockResponseDTO.fromEntity(findStockOrThrow(id));
    }

    /** Stocks en alerte (en dessous du seuil minimum) */
    public List<StockResponseDTO> findStocksEnAlerte() {
        return stockRepository.findStocksEnAlerte()
                .stream()
                .map(StockResponseDTO::fromEntity)
                .toList();
    }

    // -------------------------------------------------------
    // HISTORIQUE DES MOUVEMENTS
    // -------------------------------------------------------

    /**
     * Historique paginé des mouvements pour un stock donné.
     *
     * @param stockId L'ID du stock
     * @param page    Numéro de page (0-indexed)
     * @param size    Nombre d'éléments par page
     */
    public Page<MouvementStockResponseDTO> getMouvements(Long stockId, int page, int size) {
        Stock stock = findStockOrThrow(stockId);
        return mouvementRepository
            .findByProduitIdAndEntrepotIdOrderByDateCreationDesc(
                stock.getProduit().getId(),
                stock.getEntrepot().getId(),
                PageRequest.of(page, size)
            )
            .map(MouvementStockResponseDTO::fromEntity);
    }

    /**
     * Les N derniers mouvements toutes entités confondues.
     * Utilisé par le tableau de bord.
     */
    public List<MouvementStockResponseDTO> getDerniersMouvements(int limit) {
        return mouvementRepository
            .findAllByOrderByDateCreationDesc(PageRequest.of(0, limit))
            .stream()
            .map(MouvementStockResponseDTO::fromEntity)
            .toList();
    }

    // -------------------------------------------------------
    // CONFIGURATION DES SEUILS
    // -------------------------------------------------------

    /**
     * Met à jour les seuils min/max d'un stock.
     * PATCH /api/stocks/{id}/seuils
     */
    @Transactional
    public StockResponseDTO mettreAJourSeuils(Long id, StockSeuilRequestDTO dto) {
        Stock stock = findStockOrThrow(id);

        // Validation : si les deux sont définis, min < max
        if (dto.getStockMinimum() != null && dto.getStockMaximum() != null
                && dto.getStockMinimum() > dto.getStockMaximum()) {
            throw new RuntimeException("Le stock minimum ne peut pas être supérieur au stock maximum");
        }

        stock.setStockMinimum(dto.getStockMinimum());
        stock.setStockMaximum(dto.getStockMaximum());
        return StockResponseDTO.fromEntity(stockRepository.save(stock));
    }

    // -------------------------------------------------------
    // MÉTHODES APPELÉES PAR LES AUTRES MODULES
    // -------------------------------------------------------

    /**
     * Vérifie qu'un stock suffisant existe pour (produit, entrepôt).
     * Utilisé par TransfertService avant la création d'un brouillon.
     *
     * @throws RuntimeException si le stock est absent ou insuffisant
     */
    public void verifierStockSuffisant(Long produitId, Long entrepotId, int quantite) {
        Stock stock = stockRepository
            .findByProduitIdAndEntrepotId(produitId, entrepotId)
            .orElseThrow(() -> new RuntimeException(
                "Ce produit n'est pas en stock dans l'entrepôt source"));
        if (stock.getQuantiteDisponible() < quantite)
            throw new RuntimeException(
                "Stock insuffisant dans l'entrepôt source : disponible="
                + stock.getQuantiteDisponible() + ", demandé=" + quantite);
    }

    /**
     * Ajoute une quantité au stock (utilisé par le module 8 : entrées).
     * Crée automatiquement une ligne de stock si elle n'existe pas encore.
     *
     * @param produitId   ID du produit
     * @param entrepotId  ID de l'entrepôt
     * @param quantite    Quantité à ajouter (doit être > 0)
     * @param type        Type de mouvement (ENTREE, TRANSFERT_ENTREE…)
     * @param reference   Référence du document source (ex: BON-2026-001)
     * @param note        Note optionnelle
     * @return Le stock mis à jour
     */
    @Transactional
    public Stock ajouterStock(Long produitId, Long entrepotId, int quantite,
                              MouvementStock.TypeMouvement type,
                              String reference, String note) {
        if (quantite <= 0)
            throw new RuntimeException("La quantité doit être supérieure à 0");

        // Charge ou crée la ligne de stock
        Stock stock = stockRepository
            .findByProduitIdAndEntrepotId(produitId, entrepotId)
            .orElseGet(() -> creerNouveauStock(produitId, entrepotId));

        stock.setQuantiteDisponible(stock.getQuantiteDisponible() + quantite);

        Stock saved = stockRepository.save(stock);
        enregistrerMouvement(saved, type, quantite, reference, note);
        return saved;
    }

    /**
     * Retire une quantité du stock disponible (utilisé par le module 9 : sorties).
     *
     * @throws RuntimeException si stock insuffisant
     */
    @Transactional
    public Stock retirerStock(Long produitId, Long entrepotId, int quantite,
                              MouvementStock.TypeMouvement type,
                              String reference, String note) {
        if (quantite <= 0)
            throw new RuntimeException("La quantité doit être supérieure à 0");

        Stock stock = stockRepository
            .findByProduitIdAndEntrepotId(produitId, entrepotId)
            .orElseThrow(() -> new RuntimeException(
                "Aucun stock trouvé pour ce produit dans cet entrepôt"));

        if (stock.getQuantiteDisponible() < quantite)
            throw new RuntimeException(
                "Stock insuffisant : disponible=" + stock.getQuantiteDisponible()
                + ", demandé=" + quantite);

        stock.setQuantiteDisponible(stock.getQuantiteDisponible() - quantite);

        Stock saved = stockRepository.save(stock);
        enregistrerMouvement(saved, type, quantite, reference, note);
        return saved;
    }

    /**
     * Réserve une quantité du stock disponible.
     * Utilisé par TransfertService.creer() pour verrouiller le stock
     * dès la création du brouillon (empêche une autre opération d'utiliser
     * le même stock entre-temps).
     *
     * La quantité passe de disponible → réservée.
     *
     * @param produitId  ID du produit
     * @param entrepotId ID de l'entrepôt
     * @param quantite   Quantité à réserver
     * @param reference  Référence du document (ex: TRF-202606-XXXXX)
     * @throws RuntimeException si stock insuffisant
     */
    @Transactional
    public Stock reserverStock(Long produitId, Long entrepotId, int quantite,
                               String reference) {
        if (quantite <= 0)
            throw new RuntimeException("La quantité doit être supérieure à 0");

        Stock stock = stockRepository
            .findByProduitIdAndEntrepotId(produitId, entrepotId)
            .orElseThrow(() -> new RuntimeException(
                "Aucun stock trouvé pour ce produit dans cet entrepôt"));

        if (stock.getQuantiteDisponible() < quantite)
            throw new RuntimeException(
                "Stock insuffisant pour la réservation : disponible="
                + stock.getQuantiteDisponible() + ", demandé=" + quantite);

        stock.setQuantiteDisponible(stock.getQuantiteDisponible() - quantite);
        stock.setQuantiteReservee(stock.getQuantiteReservee() + quantite);

        Stock saved = stockRepository.save(stock);
        enregistrerMouvement(saved, MouvementStock.TypeMouvement.RESERVATION,
                quantite, reference, "Réservation pour transfert " + reference);
        return saved;
    }

    /**
     * Libère une réservation (annulation d'un transfert en brouillon).
     * La quantité passe de réservée → disponible.
     */
    @Transactional
    public Stock libererReservation(Long produitId, Long entrepotId, int quantite,
                                    String reference) {
        if (quantite <= 0)
            throw new RuntimeException("La quantité doit être supérieure à 0");

        Stock stock = stockRepository
            .findByProduitIdAndEntrepotId(produitId, entrepotId)
            .orElseThrow(() -> new RuntimeException(
                "Aucun stock trouvé pour ce produit dans cet entrepôt"));

        if (stock.getQuantiteReservee() < quantite)
            throw new RuntimeException(
                "Réservation insuffisante : réservée=" + stock.getQuantiteReservee()
                + ", à libérer=" + quantite);

        stock.setQuantiteDisponible(stock.getQuantiteDisponible() + quantite);
        stock.setQuantiteReservee(stock.getQuantiteReservee() - quantite);

        Stock saved = stockRepository.save(stock);
        enregistrerMouvement(saved, MouvementStock.TypeMouvement.LIBERATION_RESERVATION,
                quantite, reference, "Libération réservation transfert " + reference);
        return saved;
    }

    /**
     * Consomme une réservation lors de l'expédition d'un transfert.
     * Ne fait que réduire la quantiteReservee (la disponible a déjà été
     * réduite au moment de la réservation).
     *
     * Génère un mouvement TRANSFERT_SORTIE pour la traçabilité.
     */
    @Transactional
    public Stock consommerReservation(Long produitId, Long entrepotId, int quantite,
                                       String reference, String note) {
        if (quantite <= 0)
            throw new RuntimeException("La quantité doit être supérieure à 0");

        Stock stock = stockRepository
            .findByProduitIdAndEntrepotId(produitId, entrepotId)
            .orElseThrow(() -> new RuntimeException(
                "Aucun stock trouvé pour ce produit dans cet entrepôt"));

        if (stock.getQuantiteReservee() < quantite)
            throw new RuntimeException(
                "Réservation insuffisante : réservée=" + stock.getQuantiteReservee()
                + ", à consommer=" + quantite);

        // On ne touche pas à quantiteDisponible (déjà réduit par la réservation)
        stock.setQuantiteReservee(stock.getQuantiteReservee() - quantite);

        Stock saved = stockRepository.save(stock);
        enregistrerMouvement(saved, MouvementStock.TypeMouvement.TRANSFERT_SORTIE,
                quantite, reference, note);
        return saved;
    }
    // -------------------------------------------------------
    // MÉTHODES PRIVÉES
    // -------------------------------------------------------

    /** Crée une nouvelle ligne de stock pour une combinaison produit/entrepôt */
    private Stock creerNouveauStock(Long produitId, Long entrepotId) {
        Produit produit = produitRepository.findById(produitId)
            .orElseThrow(() -> new RuntimeException("Produit introuvable : id=" + produitId));
        Entrepot entrepot = entrepotRepository.findById(entrepotId)
            .orElseThrow(() -> new RuntimeException("Entrepôt introuvable : id=" + entrepotId));

        return Stock.builder()
            .produit(produit)
            .entrepot(entrepot)
            .quantiteDisponible(0)
            .quantiteReservee(0)
            .quantiteEnTransit(0)
            .build();
    }

    /**
     * Enregistre un mouvement de stock pour la traçabilité.
     * Appelé après chaque modification du stock.
     */
    private void enregistrerMouvement(Stock stock, MouvementStock.TypeMouvement type,
                                      int quantite, String reference, String note) {
        // Récupère l'utilisateur connecté depuis le contexte Spring Security
        Utilisateur utilisateur = null;
        try {
            String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
            utilisateur = utilisateurRepository.findByEmail(email).orElse(null);
        } catch (Exception ignored) {
            // Si pas d'utilisateur connecté (ex: batch), on laisse null
        }

        MouvementStock mouvement = MouvementStock.builder()
            .type(type)
            .produit(stock.getProduit())
            .entrepot(stock.getEntrepot())
            .quantite(quantite)
            .quantiteApres(stock.getQuantiteDisponible())
            .reference(reference)
            .note(note)
            .utilisateur(utilisateur)
            .build();

        mouvementRepository.save(mouvement);
    }

    private Stock findStockOrThrow(Long id) {
        return stockRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Stock introuvable : id=" + id));
    }
}
