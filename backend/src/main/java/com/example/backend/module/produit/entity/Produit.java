package com.example.backend.module.produit.entity;

import com.example.backend.module.categorie.entity.Categorie;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "produits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;

    @Column(unique = true)
    private String codeBarres;

    @Column(nullable = false)
    private String nom;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @Column(precision = 10, scale = 2)
    private BigDecimal prixAchat;

    @Column(precision = 10, scale = 2)
    private BigDecimal prixVente;

    private Double poids;
    private Double volume;

    @Builder.Default
    private boolean actif = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }
}
