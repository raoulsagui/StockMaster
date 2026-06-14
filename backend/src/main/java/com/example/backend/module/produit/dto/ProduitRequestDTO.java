package com.example.backend.module.produit.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProduitRequestDTO {

    @NotBlank(message = "La référence est obligatoire")
    @Size(max = 50, message = "La référence ne doit pas dépasser 50 caractères")
    private String reference;

    @Size(max = 50, message = "Le code-barres ne doit pas dépasser 50 caractères")
    private String codeBarres;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 200, message = "Le nom doit contenir entre 2 et 200 caractères")
    private String nom;

    private String description;

    private Long categorieId;

    @DecimalMin(value = "0.0", inclusive = true, message = "Le prix d'achat doit être positif")
    private BigDecimal prixAchat;

    @DecimalMin(value = "0.0", inclusive = true, message = "Le prix de vente doit être positif")
    private BigDecimal prixVente;

    @DecimalMin(value = "0.0", inclusive = true, message = "Le poids doit être positif")
    private Double poids;

    @DecimalMin(value = "0.0", inclusive = true, message = "Le volume doit être positif")
    private Double volume;
}
