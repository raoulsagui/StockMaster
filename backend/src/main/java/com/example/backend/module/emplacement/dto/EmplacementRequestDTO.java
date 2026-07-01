package com.example.backend.module.emplacement.dto;

import com.example.backend.module.emplacement.entity.Emplacement;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class EmplacementRequestDTO {

    @NotBlank(message = "Le code de l'emplacement est obligatoire")
    @Size(min = 1, max = 20)
    @Pattern(regexp = "^[A-Z0-9\\-]+$", message = "Le code ne peut contenir que des lettres majuscules, chiffres et tirets")
    private String code;

    @NotNull(message = "Le type est obligatoire")
    private Emplacement.TypeEmplacement type;

    @Min(value = 1, message = "La capacité doit être au moins 1")
    private Integer capaciteMax;

    @Size(max = 300)
    private String description;

    @NotNull(message = "L'étagère est obligatoire")
    private Long etagereId;
}
