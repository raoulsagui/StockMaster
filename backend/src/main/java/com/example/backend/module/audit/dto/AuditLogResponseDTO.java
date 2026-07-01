package com.example.backend.module.audit.dto;

import com.example.backend.module.audit.entity.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponseDTO {
    private Long id;
    private String entityType;
    private Long entityId;
    private AuditLog.ActionType action;
    private String utilisateurEmail;
    private String ancienneValeur;
    private String nouvelleValeur;
    private LocalDateTime dateAction;
}