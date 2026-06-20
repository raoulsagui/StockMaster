package com.example.backend.module.dashboard.service;

import com.example.backend.module.dashboard.dto.DashboardDTO;
import com.example.backend.module.entree.entity.Entree;
import com.example.backend.module.entree.repository.EntreeRepository;
import com.example.backend.module.entrepot.entity.Entrepot;
import com.example.backend.module.entrepot.repository.EntrepotRepository;
import com.example.backend.module.sortie.entity.Sortie;
import com.example.backend.module.sortie.repository.SortieRepository;
import com.example.backend.module.stock.entity.MouvementStock;
import com.example.backend.module.stock.entity.Stock;
import com.example.backend.module.stock.repository.MouvementStockRepository;
import com.example.backend.module.stock.repository.StockRepository;
import com.example.backend.module.utilisateur.entity.Role;
import com.example.backend.module.utilisateur.entity.Utilisateur;
import com.example.backend.module.utilisateur.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service du tableau de bord.
 *
 * Calcule les KPI en tenant compte du périmètre de l'utilisateur :
 *   - ADMIN          → toutes les données
 *   - autres rôles   → données filtrées sur leurs entrepôts assignés
 *
 * Si un utilisateur n'a aucun entrepôt assigné, le dashboard
 * affiche des zéros (pas de données à montrer).
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EntrepotRepository       entrepotRepository;
    private final StockRepository          stockRepository;
    private final MouvementStockRepository mouvementRepository;
    private final UtilisateurRepository    utilisateurRepository;
    private final EntreeRepository         entreeRepository;
    private final SortieRepository         sortieRepository;

    public DashboardDTO getDashboard() {
        Utilisateur connecte = getUtilisateurConnecte();
        boolean estAdmin = connecte != null && connecte.getRole() == Role.ADMIN;

        // -------------------------------------------------------
        // ENTREPÔTS DU PÉRIMÈTRE
        // -------------------------------------------------------
        List<Entrepot> entrepots;
        if (estAdmin) {
            entrepots = entrepotRepository.findAll();
        } else if (connecte != null) {
            entrepots = entrepotRepository.findByMembresId(connecte.getId());
        } else {
            entrepots = List.of();
        }

        List<Long> entrepotIds = entrepots.stream().map(Entrepot::getId).toList();

        // -------------------------------------------------------
        // STOCKS DU PÉRIMÈTRE
        // -------------------------------------------------------
        List<Stock> stocks;
        if (estAdmin) {
            stocks = stockRepository.findAll();
        } else if (!entrepotIds.isEmpty()) {
            stocks = stockRepository.findByEntrepotIdIn(entrepotIds);
        } else {
            stocks = List.of();
        }

        long entrepotsActifs = entrepots.stream().filter(Entrepot::isActif).count();
        long referencesEnStock = stocks.size();
        long totalUnitesDispo = stocks.stream()
                .mapToLong(s -> s.getQuantiteDisponible() != null ? s.getQuantiteDisponible() : 0)
                .sum();
        long stocksEnAlerte = stocks.stream().filter(Stock::estEnStockFaible).count();

        // -------------------------------------------------------
        // DERNIERS MOUVEMENTS
        // -------------------------------------------------------
        List<DashboardDTO.MouvementResume> mouvements;
        if (estAdmin) {
            mouvements = mouvementRepository
                    .findAllByOrderByDateCreationDesc(PageRequest.of(0, 5))
                    .stream()
                    .map(this::toMouvementResume)
                    .toList();
        } else if (!entrepotIds.isEmpty()) {
            mouvements = mouvementRepository
                    .findByEntrepotIdInOrderByDateCreationDesc(entrepotIds, PageRequest.of(0, 5))
                    .stream()
                    .map(this::toMouvementResume)
                    .toList();
        } else {
            mouvements = List.of();
        }

        // -------------------------------------------------------
        // ALERTES RÉCENTES (5 max)
        // -------------------------------------------------------
        List<DashboardDTO.AlerteStock> alertes = stocks.stream()
                .filter(Stock::estEnStockFaible)
                .limit(5)
                .map(s -> DashboardDTO.AlerteStock.builder()
                        .stockId(s.getId())
                        .produitNom(s.getProduit().getNom())
                        .entrepotNom(s.getEntrepot().getNom())
                        .quantiteDisponible(s.getQuantiteDisponible())
                        .stockMinimum(s.getStockMinimum())
                        .build())
                .toList();

        // -------------------------------------------------------
        // ENTRÉES / SORTIES DU MOIS
        // -------------------------------------------------------
        LocalDateTime debutMois = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMois   = LocalDateTime.now();

        long entreesduMois;
        long sortiesDuMois;

        if (estAdmin) {
            entreesduMois = entreeRepository.countByStatutAndDateValidationBetween(
                    Entree.StatutEntree.VALIDE, debutMois, finMois);
            sortiesDuMois = sortieRepository.countByStatutAndDateValidationBetween(
                    Sortie.StatutSortie.VALIDE, debutMois, finMois);
        } else if (!entrepotIds.isEmpty()) {
            entreesduMois = entreeRepository.countByStatutAndPeriodeAndEntrepots(
                    Entree.StatutEntree.VALIDE, debutMois, finMois, entrepotIds);
            sortiesDuMois = sortieRepository.countByStatutAndPeriodeAndEntrepots(
                    Sortie.StatutSortie.VALIDE, debutMois, finMois, entrepotIds);
        } else {
            entreesduMois = 0;
            sortiesDuMois = 0;
        }

        // -------------------------------------------------------
        // UTILISATEURS ACTIFS (ADMIN uniquement)
        // -------------------------------------------------------
        long utilisateursActifs = estAdmin
                ? utilisateurRepository.findAll().stream().filter(Utilisateur::isActif).count()
                : -1L;

        // -------------------------------------------------------
        // GRAPHIQUE 1 : ÉVOLUTION DU STOCK (30 derniers jours)
        // On construit une liste de points date → {entrees, sorties}
        // -------------------------------------------------------
        LocalDateTime debut30j = LocalDate.now().minusDays(29).atStartOfDay();

        List<Object[]> rawEntrees;
        List<Object[]> rawSorties;

        if (estAdmin) {
            rawEntrees = entreeRepository.findEvolutionQuotidienne(debut30j, finMois);
            rawSorties = sortieRepository.findEvolutionQuotidienne(debut30j, finMois);
        } else if (!entrepotIds.isEmpty()) {
            rawEntrees = entreeRepository.findEvolutionQuotidienneParEntrepots(debut30j, finMois, entrepotIds);
            rawSorties = sortieRepository.findEvolutionQuotidienneParEntrepots(debut30j, finMois, entrepotIds);
        } else {
            rawEntrees = List.of();
            rawSorties = List.of();
        }

        // Fusion par date sur les 30 derniers jours
        java.util.Map<String, long[]> evolutionMap = new java.util.LinkedHashMap<>();
        for (int i = 29; i >= 0; i--) {
            String d = LocalDate.now().minusDays(i).toString();
            evolutionMap.put(d, new long[]{0, 0});
        }
        for (Object[] row : rawEntrees) {
            String d = row[0].toString().substring(0, 10);
            if (evolutionMap.containsKey(d)) evolutionMap.get(d)[0] = ((Number) row[1]).longValue();
        }
        for (Object[] row : rawSorties) {
            String d = row[0].toString().substring(0, 10);
            if (evolutionMap.containsKey(d)) evolutionMap.get(d)[1] = ((Number) row[1]).longValue();
        }

        List<DashboardDTO.PointGraphique> evolutionStock = evolutionMap.entrySet().stream()
                .map(e -> DashboardDTO.PointGraphique.builder()
                        .date(e.getKey())
                        .entrees(e.getValue()[0])
                        .sorties(e.getValue()[1])
                        .build())
                .toList();

        // -------------------------------------------------------
        // GRAPHIQUE 2 : ROTATION DES PRODUITS (top 5, 30 derniers jours)
        // -------------------------------------------------------
        List<Object[]> rawRotation;
        if (estAdmin) {
            rawRotation = mouvementRepository.findRotationProduits(debut30j, PageRequest.of(0, 5));
        } else if (!entrepotIds.isEmpty()) {
            rawRotation = mouvementRepository.findRotationProduitsParEntrepots(debut30j, entrepotIds, PageRequest.of(0, 5));
        } else {
            rawRotation = List.of();
        }

        List<DashboardDTO.RotationProduit> rotationProduits = rawRotation.stream()
                .map(row -> DashboardDTO.RotationProduit.builder()
                        .produitNom((String) row[0])
                        .totalMouvements(((Number) row[1]).longValue())
                        .build())
                .toList();

        return DashboardDTO.builder()
                .entrepotsActifs(entrepotsActifs)
                .entrepotsTotal(entrepots.size())
                .referencesEnStock(referencesEnStock)
                .totalUnitesDispo(totalUnitesDispo)
                .stocksEnAlerte(stocksEnAlerte)
                .entreesduMois(entreesduMois)
                .sortiesDuMois(sortiesDuMois)
                .utilisateursActifs(utilisateursActifs)
                .derniersMouvements(mouvements)
                .alertesRecentes(alertes)
                .filtrePeriemtre(!estAdmin)
                .build();
    }

    // -------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------

    private DashboardDTO.MouvementResume toMouvementResume(MouvementStock m) {
        return DashboardDTO.MouvementResume.builder()
                .id(m.getId())
                .type(m.getType().name())
                .produitNom(m.getProduit().getNom())
                .entrepotNom(m.getEntrepot().getNom())
                .quantite(m.getQuantite())
                .dateCreation(m.getDateCreation())
                .build();
    }

    private Utilisateur getUtilisateurConnecte() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            return utilisateurRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
