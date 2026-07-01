package com.example.backend.module.audit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType action;

    @Column(nullable = false)
    private String utilisateurEmail;

    @Column(columnDefinition = "TEXT")
    private String ancienneValeur;

    @Column(columnDefinition = "TEXT")
    private String nouvelleValeur;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateAction;

    @PrePersist
    public void prePersist() {
        this.dateAction = LocalDateTime.now();
    }

    public enum ActionType {
        CREER,
        MODIFIER,
        SUPPRIMER,
        DESACTIVER,
        REACTIVER,
        VALIDER,
        ANNULER
    }
}