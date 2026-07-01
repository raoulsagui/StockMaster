package com.example.backend.module.transfert.service;

import com.example.backend.module.transfert.dto.TransfertRequestDTO;
import com.example.backend.module.transfert.dto.TransfertResponseDTO;
import com.example.backend.module.transfert.entity.Transfert;
import com.example.backend.module.transfert.repository.TransfertRepository;
import com.example.backend.module.entrepot.repository.EntrepotRepository;
import com.example.backend.module.produit.repository.ProduitRepository;
import com.example.backend.module.stock.entity.MouvementStock;
import com.example.backend.module.stock.service.StockService;
import com.example.backend.module.utilisateur.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service du module Transferts (Module 10).
 *
 * Flux métier :
 *   1. creer()           → BROUILLON + réserve le stock source (disponible → réservé)
 *   2. expedier()        → EXPEDIE : retire du stock source (consume la réservation)
 *   3. receptionner()    → RECU   : ajoute au stock destination
 *   4. annuler()         → ANNULE : libère la réservation sur le stock source
 *
 * La réservation empêche qu'une autre opération (sortie, autre transfert)
 * utilise le même stock entre la création du brouillon et l'expédition.
 *
 * La transaction est atomique à chaque étape :
 * si StockService lève une exception, le statut n'est pas sauvegardé.
 */
@Service
@RequiredArgsConstructor
public class TransfertService {

    private final TransfertRepository   transfertRepository;
    private final ProduitRepository     produitRepository;
    private final EntrepotRepository    entrepotRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final StockService          stockService;

    // -------------------------------------------------------
    // CONSULTATION
    // -------------------------------------------------------

    public List<TransfertResponseDTO> findAll() {
        return transfertRepository.findAllByOrderByDateCreationDesc()
                .stream().map(TransfertResponseDTO::fromEntity).toList();
    }

    public List<TransfertResponseDTO> findByStatut(String statut) {
        return transfertRepository
                .findByStatutOrderByDateCreationDesc(Transfert.StatutTransfert.valueOf(statut))
                .stream().map(TransfertResponseDTO::fromEntity).toList();
    }

    public TransfertResponseDTO findById(Long id) {
        return TransfertResponseDTO.fromEntity(findOrThrow(id));
    }

    // -------------------------------------------------------
    // CRÉATION + RÉSERVATION
    // -------------------------------------------------------

    @Transactional
    public TransfertResponseDTO creer(TransfertRequestDTO dto) {
        var produit = produitRepository.findById(dto.getProduitId())
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));
        var source = entrepotRepository.findById(dto.getEntrepotSourceId())
                .orElseThrow(() -> new RuntimeException("Entrepôt source introuvable"));
        var destination = entrepotRepository.findById(dto.getEntrepotDestinationId())
                .orElseThrow(() -> new RuntimeException("Entrepôt destination introuvable"));

        // Vérifications métier
        if (source.getId().equals(destination.getId())) {
            throw new RuntimeException("L'entrepôt source et destination doivent être différents");
        }
        if (!produit.isActif()) {
            throw new RuntimeException("Le produit '" + produit.getNom() + "' est inactif");
        }
        if (!source.isActif()) {
            throw new RuntimeException("L'entrepôt source '" + source.getNom() + "' est inactif");
        }
        if (!destination.isActif()) {
            throw new RuntimeException("L'entrepôt destination '" + destination.getNom() + "' est inactif");
        }

        // Réserve le stock dans l'entrepôt source (passe de disponible → réservé)
        String reference = genererReference();
        stockService.reserverStock(
                produit.getId(), source.getId(), dto.getQuantite(), reference
        );

        var transfert = Transfert.builder()
                .reference(reference)
                .statut(Transfert.StatutTransfert.BROUILLON)
                .produit(produit)
                .entrepotSource(source)
                .entrepotDestination(destination)
                .quantite(dto.getQuantite())
                .note(dto.getNote())
                .createur(getUtilisateurConnecte())
                .build();

        return TransfertResponseDTO.fromEntity(transfertRepository.save(transfert));
    }

    // -------------------------------------------------------
    // EXPÉDITION — consume la réservation
    // -------------------------------------------------------

    @Transactional
    public TransfertResponseDTO expedier(Long id) {
        Transfert transfert = findOrThrow(id);

        if (transfert.getStatut() != Transfert.StatutTransfert.BROUILLON) {
            throw new RuntimeException("Seul un transfert en BROUILLON peut être expédié");
        }

        // La réservation a déjà réduit le disponible à la création du brouillon.
        // On consume juste la réservation (sans retoucher au disponible).
        stockService.consommerReservation(
                transfert.getProduit().getId(),
                transfert.getEntrepotSource().getId(),
                transfert.getQuantite(),
                transfert.getReference(),
                "Transfert expédié vers " + transfert.getEntrepotDestination().getNom()
        );

        transfert.setStatut(Transfert.StatutTransfert.EXPEDIE);
        transfert.setExpediteur(getUtilisateurConnecte());
        transfert.setDateExpedition(LocalDateTime.now());

        return TransfertResponseDTO.fromEntity(transfertRepository.save(transfert));
    }

    // -------------------------------------------------------
    // RÉCEPTION — crédite le stock destination
    // -------------------------------------------------------

    @Transactional
    public TransfertResponseDTO receptionner(Long id) {
        Transfert transfert = findOrThrow(id);

        if (transfert.getStatut() != Transfert.StatutTransfert.EXPEDIE) {
            throw new RuntimeException("Seul un transfert EXPÉDIÉ peut être réceptionné");
        }

        // Ajoute au stock destination
        stockService.ajouterStock(
                transfert.getProduit().getId(),
                transfert.getEntrepotDestination().getId(),
                transfert.getQuantite(),
                MouvementStock.TypeMouvement.TRANSFERT_ENTREE,
                transfert.getReference(),
                "Transfert depuis " + transfert.getEntrepotSource().getNom()
        );

        transfert.setStatut(Transfert.StatutTransfert.RECU);
        transfert.setRecepteur(getUtilisateurConnecte());
        transfert.setDateReception(LocalDateTime.now());

        return TransfertResponseDTO.fromEntity(transfertRepository.save(transfert));
    }

    // -------------------------------------------------------
    // ANNULATION — libère la réservation
    // -------------------------------------------------------

    @Transactional
    public TransfertResponseDTO annuler(Long id) {
        Transfert transfert = findOrThrow(id);

        if (transfert.getStatut() != Transfert.StatutTransfert.BROUILLON) {
            throw new RuntimeException("Seul un transfert en BROUILLON peut être annulé");
        }

        // Libère la réservation (passe de réservé → disponible)
        stockService.libererReservation(
                transfert.getProduit().getId(),
                transfert.getEntrepotSource().getId(),
                transfert.getQuantite(),
                transfert.getReference()
        );

        transfert.setStatut(Transfert.StatutTransfert.ANNULE);
        return TransfertResponseDTO.fromEntity(transfertRepository.save(transfert));
    }

    // -------------------------------------------------------
    // HELPERS PRIVÉS
    // -------------------------------------------------------

    private Transfert findOrThrow(Long id) {
        return transfertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfert introuvable : id=" + id));
    }

    private com.example.backend.module.utilisateur.entity.Utilisateur getUtilisateurConnecte() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            return utilisateurRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private String genererReference() {
        String prefix = "TRF-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-";
        String suffix = Long.toString(ThreadLocalRandom.current().nextLong(10000, 99999), 36).toUpperCase();
        return prefix + suffix;
    }
}
