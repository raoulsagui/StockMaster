package com.example.backend.module.alerte.service;

import com.example.backend.module.alerte.dto.AlerteResponseDTO;
import com.example.backend.module.alerte.entity.Alerte;
import com.example.backend.module.alerte.repository.AlerteRepository;
import com.example.backend.module.stock.entity.Stock;
import com.example.backend.module.stock.repository.StockRepository;
import com.example.backend.module.zone.entity.Zone;
import com.example.backend.module.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service principal du module Alertes.
 *
 * Responsabilités :
 *   1. Détecter les situations anormales (stock faible, zone saturée)
 *   2. Créer les alertes en base si elles n'existent pas déjà
 *   3. Déclencher l'envoi d'email pour chaque nouvelle alerte
 *   4. Exposer les données au controller (liste, stats, actions)
 *
 * Le scheduler {@link AlerteScheduler} appelle scannerToutesLesAlertes()
 * à intervalles réguliers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlerteService {

    private final AlerteRepository  alerteRepository;
    private final StockRepository   stockRepository;
    private final ZoneRepository    zoneRepository;
    private final AlerteMailService mailService;

    /** Seuil de saturation d'une zone — déclenche ZONE_SATUREE */
    private static final double SEUIL_ZONE_SATUREE_WARNING  = 80.0;
    private static final double SEUIL_ZONE_SATUREE_CRITIQUE = 95.0;

    private static final List<Alerte.StatutAlerte> STATUTS_ACTIFS =
            List.of(Alerte.StatutAlerte.NON_LUE, Alerte.StatutAlerte.LUE);

    // -------------------------------------------------------
    // SCAN GLOBAL — appelé par le Scheduler
    // -------------------------------------------------------

    /**
     * Point d'entrée principal du scan automatique.
     * Vérifie stocks faibles + zones saturées, crée les alertes manquantes.
     */
    @Transactional
    public void scannerToutesLesAlertes() {
        log.info("[AlerteScheduler] Démarrage du scan des alertes — {}", LocalDateTime.now());
        int nouvelles = 0;
        nouvelles += scannerStocksFaibles();
        nouvelles += scannerZonesSaturees();
        log.info("[AlerteScheduler] Scan terminé — {} nouvelle(s) alerte(s) créée(s)", nouvelles);
    }

    // -------------------------------------------------------
    // SCAN STOCKS FAIBLES
    // -------------------------------------------------------

    private int scannerStocksFaibles() {
        List<Stock> stocksEnAlerte = stockRepository.findStocksEnAlerte();
        int count = 0;

        for (Stock stock : stocksEnAlerte) {
            // Évite les doublons : ne recrée pas si alerte déjà active
            if (alerteRepository.existsByStockIdAndTypeAndStatutIn(
                    stock.getId(), Alerte.TypeAlerte.STOCK_FAIBLE, STATUTS_ACTIFS)) {
                continue;
            }

            double ratio = stock.getStockMinimum() > 0
                    ? (double) stock.getQuantiteDisponible() / stock.getStockMinimum() : 0;

            Alerte.Severite severite = ratio <= 0 ? Alerte.Severite.CRITIQUE
                    : ratio <= 0.5 ? Alerte.Severite.WARNING
                    : Alerte.Severite.INFO;

            String message = buildMessageStockFaible(stock);

            Alerte alerte = Alerte.builder()
                    .type(Alerte.TypeAlerte.STOCK_FAIBLE)
                    .severite(severite)
                    .message(message)
                    .stock(stock)
                    .valeurActuelle((double) stock.getQuantiteDisponible())
                    .seuil((double) stock.getStockMinimum())
                    .build();

            alerte = alerteRepository.save(alerte);
            mailService.envoyerAlerteEmail(alerte);
            alerte.setEmailEnvoye(true);
            alerteRepository.save(alerte);
            count++;

            log.warn("[STOCK_FAIBLE] {} — {} unités (seuil: {})",
                    stock.getProduit().getNom(),
                    stock.getQuantiteDisponible(),
                    stock.getStockMinimum());
        }
        return count;
    }

    // -------------------------------------------------------
    // SCAN ZONES SATURÉES
    // -------------------------------------------------------

    private int scannerZonesSaturees() {
        List<Zone> zones = zoneRepository.findAll().stream()
                .filter(z -> z.isActif() && z.getCapaciteTotale() != null && z.getCapaciteTotale() > 0)
                .filter(z -> z.getTauxOccupation() >= SEUIL_ZONE_SATUREE_WARNING)
                .toList();

        int count = 0;
        for (Zone zone : zones) {
            if (alerteRepository.existsByZoneIdAndTypeAndStatutIn(
                    zone.getId(), Alerte.TypeAlerte.ZONE_SATUREE, STATUTS_ACTIFS)) {
                continue;
            }

            double taux = zone.getTauxOccupation();
            Alerte.Severite severite = taux >= SEUIL_ZONE_SATUREE_CRITIQUE
                    ? Alerte.Severite.CRITIQUE : Alerte.Severite.WARNING;

            String message = String.format(
                    "La zone \"%s\" de l'entrepôt \"%s\" est occupée à %.1f%% (seuil : %.0f%%). Libérez de l'espace ou redistribuez le stock.",
                    zone.getNom(), zone.getEntrepot().getNom(), taux, SEUIL_ZONE_SATUREE_WARNING);

            Alerte alerte = Alerte.builder()
                    .type(Alerte.TypeAlerte.ZONE_SATUREE)
                    .severite(severite)
                    .message(message)
                    .zone(zone)
                    .valeurActuelle(taux)
                    .seuil(SEUIL_ZONE_SATUREE_WARNING)
                    .build();

            alerte = alerteRepository.save(alerte);
            mailService.envoyerAlerteEmail(alerte);
            alerte.setEmailEnvoye(true);
            alerteRepository.save(alerte);
            count++;

            log.warn("[ZONE_SATUREE] {} — {:.1f}%", zone.getNom(), taux);
        }
        return count;
    }

    // -------------------------------------------------------
    // API — Lecture
    // -------------------------------------------------------

    public List<AlerteResponseDTO> findAll() {
        return alerteRepository.findAllByOrderByDateCreationDesc()
                .stream().map(AlerteResponseDTO::fromEntity).toList();
    }

    public List<AlerteResponseDTO> findActives() {
        return alerteRepository.findActives()
                .stream().map(AlerteResponseDTO::fromEntity).toList();
    }

    public List<AlerteResponseDTO> findByStatut(String statut) {
        return alerteRepository.findByStatutOrderByDateCreationDesc(
                Alerte.StatutAlerte.valueOf(statut))
                .stream().map(AlerteResponseDTO::fromEntity).toList();
    }

    public long countNonLues() {
        return alerteRepository.countByStatut(Alerte.StatutAlerte.NON_LUE);
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total",       alerteRepository.count());
        stats.put("nonLues",     alerteRepository.countByStatut(Alerte.StatutAlerte.NON_LUE));
        stats.put("lues",        alerteRepository.countByStatut(Alerte.StatutAlerte.LUE));
        stats.put("resolues",    alerteRepository.countByStatut(Alerte.StatutAlerte.RESOLUE));

        Map<String, Long> parSeverite = new HashMap<>();
        alerteRepository.countBySeveriteActives().forEach(row ->
                parSeverite.put(row[0].toString(), (Long) row[1]));
        stats.put("parSeverite", parSeverite);
        return stats;
    }

    // -------------------------------------------------------
    // API — Actions
    // -------------------------------------------------------

    @Transactional
    public AlerteResponseDTO marquerLue(Long id) {
        Alerte a = findOrThrow(id);
        if (a.getStatut() == Alerte.StatutAlerte.NON_LUE) {
            a.setStatut(Alerte.StatutAlerte.LUE);
            a.setDateLecture(LocalDateTime.now());
            a = alerteRepository.save(a);
        }
        return AlerteResponseDTO.fromEntity(a);
    }

    @Transactional
    public AlerteResponseDTO marquerResolue(Long id) {
        Alerte a = findOrThrow(id);
        a.setStatut(Alerte.StatutAlerte.RESOLUE);
        a.setDateResolution(LocalDateTime.now());
        if (a.getDateLecture() == null) a.setDateLecture(LocalDateTime.now());
        return AlerteResponseDTO.fromEntity(alerteRepository.save(a));
    }

    @Transactional
    public void marquerToutesLues() {
        alerteRepository.findByStatutOrderByDateCreationDesc(Alerte.StatutAlerte.NON_LUE)
                .forEach(a -> {
                    a.setStatut(Alerte.StatutAlerte.LUE);
                    a.setDateLecture(LocalDateTime.now());
                    alerteRepository.save(a);
                });
    }

    @Transactional
    public AlerteResponseDTO forceDeclencher(String type) {
        // Déclenche manuellement un scan du type demandé (utile pour les tests)
        switch (type) {
            case "STOCK_FAIBLE" -> scannerStocksFaibles();
            case "ZONE_SATUREE" -> scannerZonesSaturees();
            default -> throw new RuntimeException("Type inconnu : " + type);
        }
        return null;
    }

    // -------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------

    private Alerte findOrThrow(Long id) {
        return alerteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alerte introuvable : id=" + id));
    }

    private String buildMessageStockFaible(Stock stock) {
        int qte   = stock.getQuantiteDisponible();
        int seuil = stock.getStockMinimum();
        if (qte <= 0) {
            return String.format(
                    "RUPTURE DE STOCK : \"%s\" est en rupture totale dans l'entrepôt \"%s\". Réapprovisionnement urgent requis.",
                    stock.getProduit().getNom(), stock.getEntrepot().getNom());
        }
        return String.format(
                "Stock faible : \"%s\" ne dispose plus que de %d unité(s) dans l'entrepôt \"%s\" (seuil minimum : %d unités). Pensez à réapprovisionner.",
                stock.getProduit().getNom(), qte, stock.getEntrepot().getNom(), seuil);
    }
}
