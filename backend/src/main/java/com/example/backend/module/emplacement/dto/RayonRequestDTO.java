package com.example.backend.module.emplacement.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RayonRequestDTO {

    @NotBlank(message = "Le code du rayon est obligatoire")
    @Size(min = 1, max = 20, message = "Le code doit contenir entre 1 et 20 caractères")
    @Pattern(regexp = "^[A-Z0-9\\-]+$", message = "Le code ne peut contenir que des lettres majuscules, chiffres et tirets")
    private String code;

    @Size(max = 200, message = "Le libellé ne peut pas dépasser 200 caractères")
    private String libelle;

    @NotNull(message = "La zone est obligatoire")
    private Long zoneId;
}
