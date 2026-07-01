package com.example.backend.module.audit.service;

import com.example.backend.module.audit.dto.AuditLogResponseDTO;
import com.example.backend.module.audit.entity.AuditLog;
import com.example.backend.module.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public List<AuditLogResponseDTO> findAll() {
        return auditLogRepository.findAll().stream()
            .map(this::toDTO)
            .toList();
    }

    public List<AuditLogResponseDTO> findByEntityType(String entityType) {
        return auditLogRepository.findByEntityTypeOrderByDateActionDesc(entityType).stream()
            .map(this::toDTO)
            .toList();
    }

    public List<AuditLogResponseDTO> findByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByDateActionDesc(entityType, entityId).stream()
            .map(this::toDTO)
            .toList();
    }

    public AuditLogResponseDTO creer(AuditLog.ActionType action, String entityType, Long entityId,
                                     String utilisateurEmail, String ancienneValeur, String nouvelleValeur) {
        AuditLog log = AuditLog.builder()
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .utilisateurEmail(utilisateurEmail)
            .ancienneValeur(ancienneValeur)
            .nouvelleValeur(nouvelleValeur)
            .build();
        return toDTO(auditLogRepository.save(log));
    }

    private AuditLogResponseDTO toDTO(AuditLog log) {
        return new AuditLogResponseDTO(
            log.getId(),
            log.getEntityType(),
            log.getEntityId(),
            log.getAction(),
            log.getUtilisateurEmail(),
            log.getAncienneValeur(),
            log.getNouvelleValeur(),
            log.getDateAction()
        );
    }
}