package com.example.backend.module.emplacement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Une étagère est une subdivision d'un Rayon.
 * Hiérarchie : Entrepôt → Zone → Rayon → Étagère → Emplacement
 *
 * Exemple de code : ETAGERE-02
 */
@Entity
@Table(
    name = "etageres",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_etagere_code_rayon",
        columnNames = {"code", "rayon_id"}
    )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Etagere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Code de l'étagère — unique au sein du rayon. Ex : "01", "02" */
    @Column(nullable = false, length = 20)
    private String code;

    /** Libellé descriptif optionnel */
    @Column(length = 200)
    private String libelle;

    /** Nombre de niveaux (rangées) sur cette étagère */
    private Integer niveaux;

    @Builder.Default
    @Column(nullable = false)
    private boolean actif = true;

    /** Rayon parent */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rayon_id", nullable = false)
    private Rayon rayon;

    /** Emplacements de cette étagère */
    @Builder.Default
    @OneToMany(mappedBy = "etagere", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Emplacement> emplacements = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() { this.dateCreation = LocalDateTime.now(); }
}
