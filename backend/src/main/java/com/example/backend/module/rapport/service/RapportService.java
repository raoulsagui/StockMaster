package com.example.backend.module.rapport.service;

import com.example.backend.module.commande.entity.Commande;
import com.example.backend.module.commande.repository.CommandeRepository;
import com.example.backend.module.inventaire.entity.Inventaire;
import com.example.backend.module.inventaire.repository.InventaireRepository;
import com.example.backend.module.rapport.dto.RapportFournisseursDTO;
import com.example.backend.module.rapport.dto.RapportInventaireDTO;
import com.example.backend.module.rapport.dto.RapportMouvementsDTO;
import com.example.backend.module.stock.entity.MouvementStock;
import com.example.backend.module.stock.repository.MouvementStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service du module Rapports.
 *
 * Produit les données agrégées pour :
 *   - Rapport d'inventaire (par ID d'inventaire validé)
 *   - Rapport des mouvements (sur une période)
 *   - Rapport des fournisseurs (commandes sur une période)
 *
 * La génération des fichiers (PDF, Excel, CSV) est déléguée
 * aux classes utilitaires spécialisées :
 *   → RapportPdfGenerator
 *   → RapportExcelGenerator
 *   → RapportCsvGenerator
 */
@Service
@RequiredArgsConstructor
public class RapportService {

    private final InventaireRepository      inventaireRepository;
    private final MouvementStockRepository  mouvementRepository;
    private final CommandeRepository        commandeRepository;

    // -------------------------------------------------------
    // RAPPORT INVENTAIRE
    // -------------------------------------------------------

    /**
     * Construit le rapport complet d'un inventaire par son ID.
     * L'inventaire peut être dans n'importe quel statut.
     */
    public RapportInventaireDTO getRapportInventaire(Long inventaireId) {
        Inventaire inv = inventaireRepository.findById(inventaireId)
                .orElseThrow(() -> new RuntimeException("Inventaire introuvable : id=" + inventaireId));
        return RapportInventaireDTO.fromEntity(inv);
    }

    /**
     * Liste des inventaires filtrables (pour le sélecteur frontend).
     * Retourne uniquement les inventaires VALIDES par défaut.
     */
    public List<RapportInventaireDTO> listerInventaires(String statut) {
        List<Inventaire> inventaires = statut != null && !statut.isBlank()
                ? inventaireRepository.findByStatutOrderByDateCreationDesc(
                        Inventaire.StatutInventaire.valueOf(statut),
                        org.springframework.data.domain.Pageable.unpaged())
                  .getContent()
                : inventaireRepository.findAllByOrderByDateCreationDesc(
                        org.springframework.data.domain.Pageable.unpaged())
                  .getContent();

        return inventaires.stream()
                .map(RapportInventaireDTO::fromEntity)
                .toList();
    }

    // -------------------------------------------------------
    // RAPPORT MOUVEMENTS
    // -------------------------------------------------------

    /**
     * Rapport des mouvements de stock sur une période donnée.
     *
     * @param dateDebut  Début de la période (inclusif)
     * @param dateFin    Fin de la période (inclusif)
     * @param entrepotId Filtre optionnel par entrepôt (null = tous)
     * @param type       Filtre optionnel par type de mouvement (null = tous)
     */
    public RapportMouvementsDTO getRapportMouvements(
            LocalDateTime dateDebut, LocalDateTime dateFin,
            Long entrepotId, String type) {

        // Charge tous les mouvements non paginés pour le rapport
        List<MouvementStock> mouvements = mouvementRepository
                .findAllByOrderByDateCreationDesc(
                        org.springframework.data.domain.Pageable.unpaged())
                .getContent()
                .stream()
                .filter(m -> !m.getDateCreation().isBefore(dateDebut)
                          && !m.getDateCreation().isAfter(dateFin))
                .filter(m -> entrepotId == null || m.getEntrepot().getId().equals(entrepotId))
                .filter(m -> type == null || m.getType().name().equals(type))
                .toList();

        // Agrégations par type
        Map<String, Long> countParType = mouvements.stream()
                .collect(Collectors.groupingBy(m -> m.getType().name(), Collectors.counting()));

        Map<String, Long> quantiteParType = mouvements.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getType().name(),
                        Collectors.summingLong(m -> m.getQuantite().longValue())
                ));

        List<RapportMouvementsDTO.LigneMouvementDTO> lignes = mouvements.stream()
                .map(RapportMouvementsDTO::toLigne)
                .toList();

        return RapportMouvementsDTO.builder()
                .dateDebut(dateDebut)
                .dateFin(dateFin)
                .totalMouvements(mouvements.size())
                .countParType(countParType)
                .quantiteParType(quantiteParType)
                .mouvements(lignes)
                .build();
    }

    // -------------------------------------------------------
    // RAPPORT FOURNISSEURS
    // -------------------------------------------------------

    /**
     * Rapport des commandes fournisseurs sur une période donnée.
     * Agrège les commandes par fournisseur.
     */
    public RapportFournisseursDTO getRapportFournisseurs(
            LocalDateTime dateDebut, LocalDateTime dateFin) {

        List<Commande> commandes = commandeRepository
                .findAllByOrderByDateCreationDesc(
                        org.springframework.data.domain.Pageable.unpaged())
                .getContent()
                .stream()
                .filter(c -> !c.getDateCreation().isBefore(dateDebut)
                          && !c.getDateCreation().isAfter(dateFin))
                .toList();

        Map<String, Long> countParStatut = commandes.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getStatut().name(), Collectors.counting()));

        BigDecimal montantTotal = commandes.stream()
                .map(Commande::getMontantTotal)
                .filter(m -> m != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Groupement par fournisseur
        Map<Long, List<Commande>> parFournisseur = commandes.stream()
                .collect(Collectors.groupingBy(c -> c.getFournisseur().getId()));

        List<RapportFournisseursDTO.LigneFournisseurDTO> lignes = parFournisseur.values()
                .stream()
                .map(cmds -> {
                    Commande premier = cmds.get(0);
                    BigDecimal mt = cmds.stream()
                            .map(Commande::getMontantTotal)
                            .filter(m -> m != null)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return RapportFournisseursDTO.LigneFournisseurDTO.builder()
                            .fournisseurNom(premier.getFournisseur().getNom())
                            .fournisseurEmail(premier.getFournisseur().getEmail())
                            .nbCommandes(cmds.size())
                            .nbLivrees(cmds.stream().filter(c ->
                                    c.getStatut() == Commande.StatutCommande.LIVREE).count())
                            .nbAnnulees(cmds.stream().filter(c ->
                                    c.getStatut() == Commande.StatutCommande.ANNULEE).count())
                            .montantTotal(mt)
                            .commandes(cmds.stream()
                                    .map(RapportFournisseursDTO::toResume)
                                    .toList())
                            .build();
                })
                .sorted((a, b) -> b.getMontantTotal().compareTo(a.getMontantTotal()))
                .toList();

        return RapportFournisseursDTO.builder()
                .dateDebut(dateDebut)
                .dateFin(dateFin)
                .totalCommandes(commandes.size())
                .montantTotalHT(montantTotal)
                .countParStatut(countParStatut)
                .fournisseurs(lignes)
                .build();
    }
}
