package com.example.backend.module.alerte.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler automatique des alertes — Spring Scheduler.
 *
 * Plannings configurés :
 *   - Scan complet : toutes les heures (cron = "0 0 * * * *")
 *   - Scan au démarrage : 30 secondes après le lancement (initialDelay)
 *
 * Pour activer le scheduling, @EnableScheduling est déclaré sur
 * BackendApplication.java (ou sur cette classe elle-même).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlerteScheduler {

    private final AlerteService alerteService;

    /**
     * Scan automatique toutes les heures.
     * Cron : secondes minutes heures jour mois jour-semaine
     * "0 0 * * * *" = au début de chaque heure (hh:00:00)
     */
    @Scheduled(cron = "0 0 * * * *")
    public void scanHoraire() {
        log.info("[Scheduler] Scan horaire des alertes démarré");
        alerteService.scannerToutesLesAlertes();
    }

    /**
     * Scan au démarrage de l'application — après 30 secondes
     * (laisse le temps à Hibernate de créer les tables).
     * Puis toutes les heures ensuite.
     */
    @Scheduled(initialDelay = 30_000, fixedRate = 3_600_000)
    public void scanInitial() {
        alerteService.scannerToutesLesAlertes();
    }
}
