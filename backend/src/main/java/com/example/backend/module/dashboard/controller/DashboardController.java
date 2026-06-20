package com.example.backend.module.dashboard.controller;

import com.example.backend.module.dashboard.dto.DashboardDTO;
import com.example.backend.module.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller du tableau de bord.
 *
 * GET /api/dashboard → retourne les KPI filtrés selon l'utilisateur connecté.
 * Accessible par tous les rôles authentifiés.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardDTO> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }
}
