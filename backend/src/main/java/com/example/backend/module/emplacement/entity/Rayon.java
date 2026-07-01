package com.example.backend.module.emplacement.entity;

import com.example.backend.module.zone.entity.Zone;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Un rayon est une subdivision physique d'une Zone.
 * Hiérarchie : Entrepôt → Zone → Rayon → Étagère → Emplacement
 *
 * Exemple de code : RAYON-03
 */
@Entity
@Table(
    name = "rayons",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_rayon_code_zone",
        columnNames = {"code", "zone_id"}
    )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rayon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Code du rayon — unique au sein de la zone. Ex : "03", "A", "B2" */
    @Column(nullable = false, length = 20)
    private String code;

    /** Libellé descriptif optionnel. Ex : "Rayon produits froids" */
    @Column(length = 200)
    private String libelle;

    @Builder.Default
    @Column(nullable = false)
    private boolean actif = true;

    /** Zone parent */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    /** Étagères de ce rayon */
    @Builder.Default
    @OneToMany(mappedBy = "rayon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Etagere> etageres = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() { this.dateCreation = LocalDateTime.now(); }

    /** Adresse partielle : ZONE-A / RAYON-03 */
    public String getAdressePartielle() {
        return zone.getNom() + " / RAYON-" + code;
    }
}
