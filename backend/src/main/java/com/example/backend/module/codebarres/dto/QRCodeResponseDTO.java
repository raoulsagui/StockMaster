package com.example.backend.module.codebarres.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeResponseDTO {
    private Long id;
    private String type;
    private String reference;
    private String nom;
    private String codeBarres;
    private String qrCodeBase64;
}