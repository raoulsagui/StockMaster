package com.example.backend.module.sortie.service;

import com.example.backend.module.sortie.dto.SortieRequestDTO;
import com.example.backend.module.sortie.dto.SortieResponseDTO;
import com.example.backend.module.sortie.entity.Sortie;
import com.example.backend.module.sortie.repository.SortieRepository;
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
 * Service du module Sorties (Module 9).
 *
 * Flux métier :
 *   1. creer()   → Crée un bon en BROUILLON (vérifie stock disponible)
 *   2. valider() → Passe en VALIDE + StockService.retirerStock()
 *   3. annuler() → Passe en ANNULE (uniquement depuis BROUILLON)
 */
@Service
@RequiredArgsConstructor
public class SortieService {

    private final SortieRepository      sortieRepository;
    private final ProduitRepository     produitRepository;
    private final EntrepotRepository    entrepotRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final StockService          stockService;

    // -------------------------------------------------------
    // CONSULTATION
    // -------------------------------------------------------

    public List<SortieResponseDTO> findAll() {
        return sortieRepository.findAllByOrderByDateCreationDesc()
                .stream().map(SortieResponseDTO::fromEntity).toList();
    }

    public List<SortieResponseDTO> findByStatut(String statut) {
        return sortieRepository
                .findByStatutOrderByDateCreationDesc(Sortie.StatutSortie.valueOf(statut))
                .stream().map(SortieResponseDTO::fromEntity).toList();
    }

    public SortieResponseDTO findById(Long id) {
        return SortieResponseDTO.fromEntity(findOrThrow(id));
    }

    // -------------------------------------------------------
    // CRÉATION
    // -------------------------------------------------------

    @Transactional
    public SortieResponseDTO creer(SortieRequestDTO dto) {
        var produit  = produitRepository.findById(dto.getProduitId())
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));
        var entrepot = entrepotRepository.findById(dto.getEntrepotId())
                .orElseThrow(() -> new RuntimeException("Entrepôt introuvable"));

        // Résolution du motif (défaut LIVRAISON si non fourni)
        Sortie.MotifSortie motif = Sortie.MotifSortie.LIVRAISON;
        if (dto.getMotif() != null && !dto.getMotif().isBlank()) {
            try {
                motif = Sortie.MotifSortie.valueOf(dto.getMotif());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Motif invalide : " + dto.getMotif());
            }
        }

        var sortie = Sortie.builder()
                .reference(genererReference())
                .statut(Sortie.StatutSortie.BROUILLON)
                .produit(produit)
                .entrepot(entrepot)
                .quantite(dto.getQuantite())
                .motif(motif)
                .destinataire(dto.getDestinataire())
                .note(dto.getNote())
                .createur(getUtilisateurConnecte())
                .build();

        return SortieResponseDTO.fromEntity(sortieRepository.save(sortie));
    }

    // -------------------------------------------------------
    // VALIDATION — retire du stock
    // -------------------------------------------------------

    @Transactional
    public SortieResponseDTO valider(Long id) {
        Sortie sortie = findOrThrow(id);

        if (sortie.getStatut() != Sortie.StatutSortie.BROUILLON) {
            throw new RuntimeException("Seul un bon en BROUILLON peut être validé");
        }

        // Décrémente le stock — lève RuntimeException si insuffisant
        stockService.retirerStock(
                sortie.getProduit().getId(),
                sortie.getEntrepot().getId(),
                sortie.getQuantite(),
                MouvementStock.TypeMouvement.SORTIE,
                sortie.getReference(),
                sortie.getNote()
        );

        sortie.setStatut(Sortie.StatutSortie.VALIDE);
        sortie.setValidateur(getUtilisateurConnecte());
        sortie.setDateValidation(LocalDateTime.now());

        return SortieResponseDTO.fromEntity(sortieRepository.save(sortie));
    }

    // -------------------------------------------------------
    // ANNULATION
    // -------------------------------------------------------

    @Transactional
    public SortieResponseDTO annuler(Long id) {
        Sortie sortie = findOrThrow(id);

        if (sortie.getStatut() != Sortie.StatutSortie.BROUILLON) {
            throw new RuntimeException("Seul un bon en BROUILLON peut être annulé");
        }

        sortie.setStatut(Sortie.StatutSortie.ANNULE);
        return SortieResponseDTO.fromEntity(sortieRepository.save(sortie));
    }

    // -------------------------------------------------------
    // HELPERS PRIVÉS
    // -------------------------------------------------------

    private Sortie findOrThrow(Long id) {
        return sortieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de sortie introuvable : id=" + id));
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
        String prefix = "SOR-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-";
        String suffix = Long.toString(ThreadLocalRandom.current().nextLong(10000, 99999), 36).toUpperCase();
        return prefix + suffix;
    }
}
