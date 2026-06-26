package com.example.backend.module.rapport.service;

import com.example.backend.module.rapport.dto.RapportFournisseursDTO;
import com.example.backend.module.rapport.dto.RapportInventaireDTO;
import com.example.backend.module.rapport.dto.RapportMouvementsDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Générateur PDF pour les rapports StockMaster.
 * Utilise OpenPDF (fork libre d'iText 4).
 *
 * Chaque méthode construit un document PDF en mémoire
 * et retourne un tableau d'octets prêt à être streamé en réponse HTTP.
 */
@Component
public class RapportPdfGenerator {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Font FONT_TITRE    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.decode("#1e293b"));
    private static final Font FONT_SOUS     = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.decode("#334155"));
    private static final Font FONT_META     = FontFactory.getFont(FontFactory.HELVETICA, 9,  Color.decode("#64748b"));
    private static final Font FONT_TH       = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
    private static final Font FONT_TD       = FontFactory.getFont(FontFactory.HELVETICA, 8,  Color.decode("#1e293b"));
    private static final Font FONT_TD_ALERT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.decode("#dc2626"));
    private static final Font FONT_TD_OK    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.decode("#16a34a"));

    private static final Color COLOR_HEADER = Color.decode("#1e40af");
    private static final Color COLOR_ALT    = Color.decode("#f8fafc");

