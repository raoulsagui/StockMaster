package com.example.backend.module.commande.service;

import com.example.backend.module.commande.dto.CommandeRequestDTO;
import com.example.backend.module.commande.dto.CommandeResponseDTO;
import com.example.backend.module.commande.dto.LigneCommandeRequestDTO;
import com.example.backend.module.commande.dto.ReceptionRequestDTO;
import com.example.backend.module.commande.entity.Commande;
import com.example.backend.module.commande.entity.LigneCommande;
import com.example.backend.module.commande.repository.CommandeRepository;
import com.example.backend.module.commande.repository.LigneCommandeRepository;
import com.example.backend.module.entrepot.entity.Entrepot;
import com.example.backend.module.entrepot.repository.EntrepotRepository;
import com.example.backend.module.fournisseur.entity.Fournisseur;
import com.example.backend.module.fournisseur.repository.FournisseurRepository;
import com.example.backend.module.produit.entity.Produit;
import com.example.backend.module.produit.repository.ProduitRepository;
import com.example.backend.module.stock.entity.MouvementStock;
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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service central du module Commandes Fournisseur.
 *
 * Gère le cycle de vie complet d'une commande :
 *
 *   1. Création (BROUILLON)
 *      → Génère la référence séquentielle CMD-AAAA-NNNNN
 *      → Crée les lignes de commande
 *
 *   2. Validation (BROUILLON → VALIDEE)
 *      → Verrouille la commande (plus modifiable)
 *      → Enregistre le valideur et la date
 *
 *   3. Réception (VALIDEE → LIVREE)
 *      → Saisie des quantités réellement reçues par ligne
 *      → Appelle StockService.ajouterStock() pour chaque ligne avec quantiteRecue > 0
 *      → TypeMouvement = ENTREE pour une traçabilité complète dans l'historique
 *
 *   4. Annulation (BROUILLON ou VALIDEE → ANNULEE)
 *      → Aucun mouvement de stock
 *
 * Règles métier clés :
 *   - Une commande LIVREE ou ANNULEE est immuable
 *   - Seules les commandes en BROUILLON peuvent être modifiées
 *   - La réception est possible uniquement sur une commande VALIDEE
 *   - Un même produit ne peut figurer qu'une seule fois par commande
 */
@Service
@RequiredArgsConstructor
public class CommandeService {

    private final CommandeRepository        commandeRepository;
    private final LigneCommandeRepository   ligneCommandeRepository;
    private final FournisseurRepository     fournisseurRepository;
    private final EntrepotRepository        entrepotRepository;
    private final ProduitRepository         produitRepository;
    private final UtilisateurRepository     utilisateurRepository;
    private final StockService              stockService;

    // -------------------------------------------------------
    // CONSULTATION
    // -------------------------------------------------------

    /**
     * Liste paginée de toutes les commandes, triées par date décroissante.
     */
    public Page<CommandeResponseDTO> findAll(int page, int size) {
        return commandeRepository
                .findAllByOrderByDateCreationDesc(PageRequest.of(page, size))
                .map(CommandeResponseDTO::fromEntity);
    }

    /**
     * Commandes d'un fournisseur spécifique.
     */
    public List<CommandeResponseDTO> findByFournisseur(Long fournisseurId) {
        return commandeRepository
                .findByFournisseurIdOrderByDateCreationDesc(fournisseurId)
                .stream()
                .map(CommandeResponseDTO::fromEntity)
                .toList();
    }

    /**
     * Commandes destinées à un entrepôt spécifique.
     */
    public List<CommandeResponseDTO> findByEntrepot(Long entrepotId) {
        return commandeRepository
                .findByEntrepotIdOrderByDateCreationDesc(entrepotId)
                .stream()
                .map(CommandeResponseDTO::fromEntity)
                .toList();
    }

    /**
     * Détail d'une commande avec toutes ses lignes.
     */
    public CommandeResponseDTO findById(Long id) {
        return CommandeResponseDTO.fromEntityWithLignes(findOrThrow(id));
    }

    /**
     * Statistiques globales pour le tableau de bord.
     */
    public CommandeStatsDTO getStats() {
        return CommandeStatsDTO.builder()
                .totalCommandes(commandeRepository.count())
                .brouillons(commandeRepository.countByStatut(Commande.StatutCommande.BROUILLON))
                .validees(commandeRepository.countByStatut(Commande.StatutCommande.VALIDEE))
                .livrees(commandeRepository.countByStatut(Commande.StatutCommande.LIVREE))
                .annulees(commandeRepository.countByStatut(Commande.StatutCommande.ANNULEE))
                .build();
    }

    // -------------------------------------------------------
    // CRÉATION
    // -------------------------------------------------------

    /**
     * Crée une nouvelle commande fournisseur en statut BROUILLON.
     *
     * Chaque ligne est validée pour s'assurer qu'il n'y a pas de doublon produit.
     * La référence est générée automatiquement au format CMD-AAAA-NNNNN.
     *
     * @throws RuntimeException si le fournisseur est inactif
     * @throws RuntimeException si un produit apparaît deux fois dans les lignes
     */
    @Transactional
    public CommandeResponseDTO creer(CommandeRequestDTO dto) {
        Fournisseur fournisseur = fournisseurRepository.findById(dto.getFournisseurId())
                .orElseThrow(() -> new RuntimeException(
                    "Fournisseur introuvable : id=" + dto.getFournisseurId()));

        if (!fournisseur.isActif()) {
            throw new RuntimeException(
                "Impossible de créer une commande pour un fournisseur inactif : " + fournisseur.getNom());
        }

        Entrepot entrepot = entrepotRepository.findById(dto.getEntrepotId())
                .orElseThrow(() -> new RuntimeException(
                    "Entrepôt introuvable : id=" + dto.getEntrepotId()));

        if (!entrepot.isActif()) {
            throw new RuntimeException(
                "Impossible de créer une commande vers un entrepôt inactif : " + entrepot.getNom());
        }

        // Vérifie l'absence de doublons produits dans les lignes
        verifierDoublonsProduits(dto.getLignes());

        Utilisateur createur = getUtilisateurConnecte();
        String reference = genererReference();

        Commande commande = Commande.builder()
                .reference(reference)
                .statut(Commande.StatutCommande.BROUILLON)
                .fournisseur(fournisseur)
                .entrepot(entrepot)
                .createur(createur)
                .dateLivraisonPrevue(dto.getDateLivraisonPrevue())
                .note(dto.getNote())
                .build();

        Commande saved = commandeRepository.save(commande);

        // Crée les lignes
        List<LigneCommande> lignes = construireLignes(dto.getLignes(), saved);
        ligneCommandeRepository.saveAll(lignes);
        saved.getLignes().addAll(lignes);

        return CommandeResponseDTO.fromEntityWithLignes(saved);
    }

    // -------------------------------------------------------
    // MODIFICATION (BROUILLON uniquement)
    // -------------------------------------------------------

    /**
     * Modifie l'en-tête d'une commande en BROUILLON.
     * Seules les métadonnées sont modifiables (fournisseur, entrepôt, date, note).
     * Les lignes sont remplacées intégralement.
     *
     * @throws RuntimeException si la commande n'est pas en BROUILLON
     */
    @Transactional
    public CommandeResponseDTO modifier(Long id, CommandeRequestDTO dto) {
        Commande commande = findOrThrow(id);

        if (commande.getStatut() != Commande.StatutCommande.BROUILLON) {
            throw new RuntimeException(
                "Seule une commande en BROUILLON peut être modifiée. "
                + "Statut actuel : " + commande.getStatut());
        }

        Fournisseur fournisseur = fournisseurRepository.findById(dto.getFournisseurId())
                .orElseThrow(() -> new RuntimeException(
                    "Fournisseur introuvable : id=" + dto.getFournisseurId()));

        Entrepot entrepot = entrepotRepository.findById(dto.getEntrepotId())
                .orElseThrow(() -> new RuntimeException(
                    "Entrepôt introuvable : id=" + dto.getEntrepotId()));

        verifierDoublonsProduits(dto.getLignes());

        // Mise à jour de l'en-tête
        commande.setFournisseur(fournisseur);
        commande.setEntrepot(entrepot);
        commande.setDateLivraisonPrevue(dto.getDateLivraisonPrevue());
        commande.setNote(dto.getNote());

        // Remplacement intégral des lignes (cascade orphanRemoval = true)
        commande.getLignes().clear();
        Commande saved = commandeRepository.save(commande);

        List<LigneCommande> nouvellesLignes = construireLignes(dto.getLignes(), saved);
        ligneCommandeRepository.saveAll(nouvellesLignes);
        saved.getLignes().addAll(nouvellesLignes);

        return CommandeResponseDTO.fromEntityWithLignes(saved);
    }

    // -------------------------------------------------------
    // VALIDATION
    // -------------------------------------------------------

    /**
     * Valide la commande : BROUILLON → VALIDEE.
     *
     * Une commande validée est considérée comme transmise au fournisseur.
     * Elle n'est plus modifiable. La réception peut ensuite être enregistrée.
     *
     * @throws RuntimeException si la commande n'est pas en BROUILLON
     * @throws RuntimeException si la commande ne contient aucune ligne
     */
    @Transactional
    public CommandeResponseDTO valider(Long id) {
        Commande commande = findOrThrow(id);

        if (commande.getStatut() != Commande.StatutCommande.BROUILLON) {
            throw new RuntimeException(
                "Seule une commande en BROUILLON peut être validée. "
                + "Statut actuel : " + commande.getStatut());
        }

        if (commande.getLignes().isEmpty()) {
            throw new RuntimeException(
                "Impossible de valider une commande sans lignes.");
        }

        Utilisateur valideur = getUtilisateurConnecte();
        commande.setStatut(Commande.StatutCommande.VALIDEE);
        commande.setValideur(valideur);
        commande.setDateValidation(LocalDateTime.now());

        return CommandeResponseDTO.fromEntityWithLignes(commandeRepository.save(commande));
    }

    // -------------------------------------------------------
    // RÉCEPTION
    // -------------------------------------------------------

    /**
     * Réceptionne la livraison : VALIDEE → LIVREE.
     *
     * Pour chaque ligne de réception fournie :
     *   - Met à jour quantiteRecue sur la LigneCommande
     *   - Si quantiteRecue > 0 : appelle StockService.ajouterStock()
     *     avec TypeMouvement.ENTREE pour alimenter le stock de l'entrepôt
     *
     * Note : la réception avec quantiteRecue = 0 est autorisée
     * (article non livré), aucun mouvement de stock n'est créé dans ce cas.
     *
     * @throws RuntimeException si la commande n'est pas en VALIDEE
     * @throws RuntimeException si une ligneId n'appartient pas à cette commande
     */
    @Transactional
    public CommandeResponseDTO receptionner(Long id, ReceptionRequestDTO dto) {
        Commande commande = findOrThrow(id);

        if (commande.getStatut() != Commande.StatutCommande.VALIDEE) {
            throw new RuntimeException(
                "Seule une commande VALIDEE peut être réceptionnée. "
                + "Statut actuel : " + commande.getStatut());
        }

        // Index des lignes par ID pour un accès O(1)
        Map<Long, LigneCommande> lignesParId = commande.getLignes().stream()
                .collect(Collectors.toMap(LigneCommande::getId, l -> l));

        String referenceCommande = "CMD-" + commande.getReference();

        for (ReceptionRequestDTO.LigneReceptionDTO ligneDto : dto.getLignes()) {
            LigneCommande ligne = lignesParId.get(ligneDto.getLigneId());

            if (ligne == null) {
                throw new RuntimeException(
                    "La ligne id=" + ligneDto.getLigneId()
                    + " n'appartient pas à la commande " + commande.getReference());
            }

            ligne.setQuantiteRecue(ligneDto.getQuantiteRecue());

            // Alimentation du stock uniquement si une quantité positive est reçue
            if (ligneDto.getQuantiteRecue() > 0) {
                String noteStock = "Réception commande " + commande.getReference()
                        + " — fournisseur : " + commande.getFournisseur().getNom()
                        + (dto.getNote() != null ? " — " + dto.getNote() : "");

                stockService.ajouterStock(
                    ligne.getProduit().getId(),
                    commande.getEntrepot().getId(),
                    ligneDto.getQuantiteRecue(),
                    MouvementStock.TypeMouvement.ENTREE,
                    referenceCommande,
                    noteStock
                );
            }
        }

        ligneCommandeRepository.saveAll(commande.getLignes());

        commande.setStatut(Commande.StatutCommande.LIVREE);
        commande.setDateLivraisonEffective(LocalDateTime.now());

        return CommandeResponseDTO.fromEntityWithLignes(commandeRepository.save(commande));
    }

    // -------------------------------------------------------
    // ANNULATION
    // -------------------------------------------------------

    /**
     * Annule une commande en BROUILLON ou VALIDEE.
     * Aucun mouvement de stock n'est généré.
     *
     * @throws RuntimeException si la commande est déjà LIVREE ou ANNULEE
     */
    @Transactional
    public CommandeResponseDTO annuler(Long id) {
        Commande commande = findOrThrow(id);

        if (commande.getStatut() == Commande.StatutCommande.LIVREE) {
            throw new RuntimeException(
                "Une commande déjà livrée ne peut pas être annulée.");
        }

        if (commande.getStatut() == Commande.StatutCommande.ANNULEE) {
            throw new RuntimeException("Cette commande est déjà annulée.");
        }

        commande.setStatut(Commande.StatutCommande.ANNULEE);
        return CommandeResponseDTO.fromEntityWithLignes(commandeRepository.save(commande));
    }

    // -------------------------------------------------------
    // MÉTHODES PRIVÉES
    // -------------------------------------------------------

    /**
     * Construit les entités LigneCommande à partir des DTOs.
     * Charge chaque produit depuis la base.
     */
    private List<LigneCommande> construireLignes(List<LigneCommandeRequestDTO> lignesDto,
                                                  Commande commande) {
        return lignesDto.stream().map(dto -> {
            Produit produit = produitRepository.findById(dto.getProduitId())
                    .orElseThrow(() -> new RuntimeException(
                        "Produit introuvable : id=" + dto.getProduitId()));

            if (!produit.isActif()) {
                throw new RuntimeException(
                    "Impossible de commander un produit inactif : " + produit.getNom());
            }

            return LigneCommande.builder()
                    .commande(commande)
                    .produit(produit)
                    .quantiteCommandee(dto.getQuantiteCommandee())
                    .prixUnitaire(dto.getPrixUnitaire())
                    .note(dto.getNote())
                    .build();
        }).toList();
    }

    /**
     * Vérifie qu'aucun produit n'est dupliqué dans la liste de lignes.
     *
     * @throws RuntimeException si un produit apparaît plus d'une fois
     */
    private void verifierDoublonsProduits(List<LigneCommandeRequestDTO> lignes) {
        long produitDistincts = lignes.stream()
                .map(LigneCommandeRequestDTO::getProduitId)
                .distinct()
                .count();

        if (produitDistincts < lignes.size()) {
            throw new RuntimeException(
                "Une commande ne peut pas contenir deux fois le même produit. "
                + "Regroupez les quantités sur une seule ligne.");
        }
    }

    /**
     * Génère une référence unique au format CMD-AAAA-NNNNN.
     * Ex : CMD-2026-00001
     */
    private String genererReference() {
        int annee = LocalDate.now().getYear();
        long compteur = commandeRepository.countByAnnee(annee) + 1;
        return String.format("CMD-%d-%05d", annee, compteur);
    }

    /**
     * Récupère l'utilisateur authentifié depuis le contexte Spring Security.
     */
    private Utilisateur getUtilisateurConnecte() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable"));
    }

    private Commande findOrThrow(Long id) {
        return commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable : id=" + id));
    }

    // -------------------------------------------------------
    // DTO STATS INTERNE
    // -------------------------------------------------------

    @lombok.Data
    @lombok.Builder
    public static class CommandeStatsDTO {
        private long totalCommandes;
        private long brouillons;
        private long validees;
        private long livrees;
        private long annulees;
    }
}
