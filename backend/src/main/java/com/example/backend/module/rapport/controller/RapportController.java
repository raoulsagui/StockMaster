package com.example.backend.module.rapport.controller;

import com.example.backend.module.rapport.dto.RapportFournisseursDTO;
import com.example.backend.module.rapport.dto.RapportInventaireDTO;
import com.example.backend.module.rapport.dto.RapportMouvementsDTO;
import com.example.backend.module.rapport.service.RapportCsvGenerator;
import com.example.backend.module.rapport.service.RapportExcelGenerator;
import com.example.backend.module.rapport.service.RapportPdfGenerator;
import com.example.backend.module.rapport.service.RapportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller REST du module Rapports.
 *
 * Routes :
 *
 *   — Données JSON (pour affichage frontend) —
 *   GET /api/rapports/inventaires                        → liste des inventaires
 *   GET /api/rapports/inventaires/{id}                   → rapport d'un inventaire
 *   GET /api/rapports/mouvements?dateDebut&dateFin       → rapport mouvements
 *   GET /api/rapports/fournisseurs?dateDebut&dateFin     → rapport fournisseurs
 *
 *   — Exports fichiers —
 *   GET /api/rapports/inventaires/{id}/export?format=pdf|excel|csv
 *   GET /api/rapports/mouvements/export?format=…&dateDebut&dateFin
 *   GET /api/rapports/fournisseurs/export?format=…&dateDebut&dateFin
 *
 * Tous les rôles authentifiés peuvent accéder (déjà configuré dans SecurityConfig).
 */
@RestController
@RequestMapping("/api/rapports")
@RequiredArgsConstructor
public class RapportController {

    private final RapportService       rapportService;
    private final RapportPdfGenerator  pdfGenerator;
    private final RapportExcelGenerator excelGenerator;
    private final RapportCsvGenerator  csvGenerator;

    // -------------------------------------------------------
    // DONNÉES JSON
    // -------------------------------------------------------

    @GetMapping("/inventaires")
    public ResponseEntity<List<RapportInventaireDTO>> listerInventaires(
            @RequestParam(required = false) String statut) {
        return ResponseEntity.ok(rapportService.listerInventaires(statut));
    }

    @GetMapping("/inventaires/{id}")
    public ResponseEntity<RapportInventaireDTO> getRapportInventaire(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(rapportService.getRapportInventaire(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/mouvements")
    public ResponseEntity<RapportMouvementsDTO> getRapportMouvements(
            @RequestParam(required = false) Long entrepotId,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(rapportService.getRapportMouvements(entrepotId, type));
    }

    @GetMapping("/fournisseurs")
    public ResponseEntity<RapportFournisseursDTO> getRapportFournisseurs() {
        return ResponseEntity.ok(rapportService.getRapportFournisseurs());
    }

    // -------------------------------------------------------
    // EXPORTS FICHIERS
    // -------------------------------------------------------

    @GetMapping("/inventaires/{id}/export")
    public ResponseEntity<byte[]> exportInventaire(
            @PathVariable Long id,
            @RequestParam(defaultValue = "pdf") String format) {
        RapportInventaireDTO dto = rapportService.getRapportInventaire(id);
        String nom = "inventaire-" + dto.getReference();
        return buildResponse(format,
                pdfGenerator.genererInventaire(dto),
                excelGenerator.genererInventaire(dto),
                csvGenerator.genererInventaire(dto),
                nom);
    }

    @GetMapping("/mouvements/export")
    public ResponseEntity<byte[]> exportMouvements(
            @RequestParam(required = false) Long entrepotId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "pdf") String format) {
        RapportMouvementsDTO dto = rapportService.getRapportMouvements(entrepotId, type);
        return buildResponse(format,
                pdfGenerator.genererMouvements(dto),
                excelGenerator.genererMouvements(dto),
                csvGenerator.genererMouvements(dto),
                "rapport-mouvements");
    }

    @GetMapping("/fournisseurs/export")
    public ResponseEntity<byte[]> exportFournisseurs(
            @RequestParam(defaultValue = "pdf") String format) {
        RapportFournisseursDTO dto = rapportService.getRapportFournisseurs();
        return buildResponse(format,
                pdfGenerator.genererFournisseurs(dto),
                excelGenerator.genererFournisseurs(dto),
                csvGenerator.genererFournisseurs(dto),
                "rapport-fournisseurs");
    }

    // -------------------------------------------------------
    // HELPER — construit la ResponseEntity selon le format
    // -------------------------------------------------------
    private ResponseEntity<byte[]> buildResponse(String format,
                                                  byte[] pdf, byte[] excel, byte[] csv,
                                                  String nomFichier) {
        return switch (format.toLowerCase()) {
            case "excel", "xlsx" -> ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + nomFichier + ".xlsx\"")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excel);
            case "csv" -> ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + nomFichier + ".csv\"")
                    .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .body(csv);
            default -> ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + nomFichier + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        };
    }
}
