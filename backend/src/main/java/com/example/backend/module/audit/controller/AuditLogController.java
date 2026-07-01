package com.example.backend.module.audit.controller;

import com.example.backend.module.audit.dto.AuditLogResponseDTO;
import com.example.backend.module.audit.entity.AuditLog;
import com.example.backend.module.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITEUR')")
    public ResponseEntity<List<AuditLogResponseDTO>> findAll() {
        return ResponseEntity.ok(auditLogService.findAll());
    }

    @GetMapping("/entite/{type}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITEUR')")
    public ResponseEntity<List<AuditLogResponseDTO>> findByEntityType(
            @PathVariable String type) {
        return ResponseEntity.ok(auditLogService.findByEntityType(type));
    }

    @GetMapping("/entite/{type}/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITEUR')")
    public ResponseEntity<List<AuditLogResponseDTO>> findByEntity(
            @PathVariable String type, @PathVariable Long id) {
        return ResponseEntity.ok(auditLogService.findByEntity(type, id));
    }
}