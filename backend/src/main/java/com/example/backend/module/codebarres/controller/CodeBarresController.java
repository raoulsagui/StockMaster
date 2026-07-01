package com.example.backend.module.codebarres.controller;

import com.example.backend.module.codebarres.dto.QRCodeResponseDTO;
import com.example.backend.module.codebarres.entity.CodeBarresType;
import com.example.backend.module.codebarres.service.CodeBarresService;
import com.example.backend.module.produit.entity.Produit;
import com.example.backend.module.produit.repository.ProduitRepository;
import com.example.backend.module.zone.entity.Zone;
import com.example.backend.module.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/codebarres")
@RequiredArgsConstructor
public class CodeBarresController {

    private final CodeBarresService codeBarresService;
    private final ProduitRepository produitRepository;
    private final ZoneRepository zoneRepository;

    @GetMapping("/produit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER', 'AUDITEUR')")
    public ResponseEntity<QRCodeResponseDTO> genererQRCodeProduit(@PathVariable Long id) {
        return produitRepository.findById(id)
            .map(p -> {
                String contenu = codeBarresService.genererContenuProduit(id, p.getReference(), p.getNom());
                String qrBase64 = codeBarresService.genererQRCode(CodeBarresType.PRODUIT, id, contenu);
                
                QRCodeResponseDTO dto = new QRCodeResponseDTO(
                    id,
                    "PRODUIT",
                    p.getReference(),
                    p.getNom(),
                    p.getCodeBarres(),
                    qrBase64
                );
                return ResponseEntity.ok(dto);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/produit/scan")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER', 'AUDITEUR')")
    public ResponseEntity<QRCodeResponseDTO> scannerProduit(@RequestParam String code) {
        return produitRepository.findByCodeBarres(code)
            .map(p -> {
                String contenu = codeBarresService.genererContenuProduit(p.getId(), p.getReference(), p.getNom());
                String qrBase64 = codeBarresService.genererQRCode(CodeBarresType.PRODUIT, p.getId(), contenu);
                
                QRCodeResponseDTO dto = new QRCodeResponseDTO(
                    p.getId(),
                    "PRODUIT",
                    p.getReference(),
                    p.getNom(),
                    p.getCodeBarres(),
                    qrBase64
                );
                return ResponseEntity.ok(dto);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/zone/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER', 'AUDITEUR')")
    public ResponseEntity<QRCodeResponseDTO> genererQRCodeZone(@PathVariable Long id) {
        return zoneRepository.findById(id)
            .map(z -> {
                String contenu = codeBarresService.genererContenuEmplacement(z.getNom(), z.getEntrepot().getNom(), z.getType().name());
                String qrBase64 = codeBarresService.genererQRCode(CodeBarresType.EMPLACEMENT, id, contenu);

                QRCodeResponseDTO dto = new QRCodeResponseDTO(
                    id,
                    "EMPLACEMENT",
                    z.getNom(),
                    z.getEntrepot().getNom() + " - " + z.getType().name(),
                    z.getNom(),
                    qrBase64
                );
                return ResponseEntity.ok(dto);
            })
            .orElse(ResponseEntity.notFound().build());
    }
}