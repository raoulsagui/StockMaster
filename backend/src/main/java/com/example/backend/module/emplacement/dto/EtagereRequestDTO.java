package com.example.backend.module.emplacement.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class EtagereRequestDTO {

    @NotBlank(message = "Le code de l'étagère est obligatoire")
    @Size(min = 1, max = 20)
    @Pattern(regexp = "^[A-Z0-9\\-]+$", message = "Le code ne peut contenir que des lettres majuscules, chiffres et tirets")
    private String code;

    @Size(max = 200)
    private String libelle;

    @Min(value = 1, message = "Le nombre de niveaux doit être au moins 1")
    @Max(value = 50, message = "Le nombre de niveaux ne peut pas dépasser 50")
    private Integer niveaux;

    @NotNull(message = "Le rayon est obligatoire")
    private Long rayonId;
}
