package com.example.backend.module.entree.service;

import com.example.backend.module.entree.dto.EntreeRequestDTO;
import com.example.backend.module.entree.dto.EntreeResponseDTO;
import com.example.backend.module.entree.entity.Entree;
import com.example.backend.module.entree.repository.EntreeRepository;
import com.example.backend.module.entrepot.repository.EntrepotRepository;
import com.example.backend.module.fournisseur.repository.FournisseurRepository;
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
 * Service du module Entrées (Module 8).
 *
 * Flux métier :
 *   1. creer()   → Crée un bon en BROUILLON (vérifie produit/entrepôt actif)
 *   2. valider() → Passe en VALIDE + appelle StockService.ajouterStock()
 *   3. annuler() → Passe en ANNULE (uniquement depuis BROUILLON)
 */
@Service
@RequiredArgsConstructor
public class EntreeService {

    private final EntreeRepository      entreeRepository;
    private final ProduitRepository     produitRepository;
    private final EntrepotRepository    entrepotRepository;
    private final FournisseurRepository fournisseurRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final StockService          stockService;

    // -------------------------------------------------------
    // CONSULTATION
    // -------------------------------------------------------

    public List<EntreeResponseDTO> findAll() {
        return entreeRepository.findAllByOrderByDateCreationDesc()
                .stream().map(EntreeResponseDTO::fromEntity).toList();
    }

    public List<EntreeResponseDTO> findByStatut(String statut) {
        return entreeRepository
                .findByStatutOrderByDateCreationDesc(Entree.StatutEntree.valueOf(statut))
                .stream().map(EntreeResponseDTO::fromEntity).toList();
    }

    public EntreeResponseDTO findById(Long id) {
        return EntreeResponseDTO.fromEntity(findOrThrow(id));
    }

    // -------------------------------------------------------
    // CRÉATION
    // -------------------------------------------------------

    @Transactional
    public EntreeResponseDTO creer(EntreeRequestDTO dto) {
        var produit  = produitRepository.findById(dto.getProduitId())
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));
        var entrepot = entrepotRepository.findById(dto.getEntrepotId())
                .orElseThrow(() -> new RuntimeException("Entrepôt introuvable"));

        // Vérifications métier : produit et entrepôt doivent être actifs
        if (!produit.isActif()) {
            throw new RuntimeException("Le produit '" + produit.getNom() + "' est inactif et ne peut pas être réceptionné");
        }
        if (!entrepot.isActif()) {
            throw new RuntimeException("L'entrepôt '" + entrepot.getNom() + "' est inactif");
        }

        var entree = Entree.builder()
                .reference(genererReference())
                .statut(Entree.StatutEntree.BROUILLON)
                .produit(produit)
                .entrepot(entrepot)
                .quantite(dto.getQuantite())
                .prixUnitaire(dto.getPrixUnitaire())
                .note(dto.getNote())
                .createur(getUtilisateurConnecte())
                .build();

        if (dto.getFournisseurId() != null) {
            entree.setFournisseur(fournisseurRepository.findById(dto.getFournisseurId())
                    .orElseThrow(() -> new RuntimeException("Fournisseur introuvable")));
        }

        return EntreeResponseDTO.fromEntity(entreeRepository.save(entree));
    }

    // -------------------------------------------------------
    // VALIDATION — met à jour le stock
    // -------------------------------------------------------

    @Transactional
    public EntreeResponseDTO valider(Long id) {
        Entree entree = findOrThrow(id);

        if (entree.getStatut() != Entree.StatutEntree.BROUILLON) {
            throw new RuntimeException("Seul un bon en BROUILLON peut être validé");
        }

        // Mise à jour du stock
        stockService.ajouterStock(
                entree.getProduit().getId(),
                entree.getEntrepot().getId(),
                entree.getQuantite(),
                MouvementStock.TypeMouvement.ENTREE,
                entree.getReference(),
                entree.getNote()
        );

        entree.setStatut(Entree.StatutEntree.VALIDE);
        entree.setValidateur(getUtilisateurConnecte());
        entree.setDateValidation(LocalDateTime.now());

        return EntreeResponseDTO.fromEntity(entreeRepository.save(entree));
    }

    // -------------------------------------------------------
    // ANNULATION
    // -------------------------------------------------------

    @Transactional
    public EntreeResponseDTO annuler(Long id) {
        Entree entree = findOrThrow(id);

        if (entree.getStatut() != Entree.StatutEntree.BROUILLON) {
            throw new RuntimeException("Seul un bon en BROUILLON peut être annulé");
        }

        entree.setStatut(Entree.StatutEntree.ANNULE);
        return EntreeResponseDTO.fromEntity(entreeRepository.save(entree));
    }

    // -------------------------------------------------------
    // HELPERS PRIVÉS
    // -------------------------------------------------------

    private Entree findOrThrow(Long id) {
        return entreeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de réception introuvable : id=" + id));
    }

    private com.example.backend.module.utilisateur.entity.Utilisateur getUtilisateurConnecte() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            return utilisateurRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Génère une référence unique de type : BON-202606-A3F2B
     */
    private String genererReference() {
        String prefix = "BON-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-";
        String suffix = Long.toString(ThreadLocalRandom.current().nextLong(10000, 99999), 36).toUpperCase();
        return prefix + suffix;
    }
}
