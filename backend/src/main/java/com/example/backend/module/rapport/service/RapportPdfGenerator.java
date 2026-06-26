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
 * Générateur PDF — utilise OpenPDF (fork libre d'iText 4).
 * Chaque méthode produit un byte[] prêt à être streamé en HTTP.
 */
@Component
public class RapportPdfGenerator {

    private static final DateTimeFormatter FMT      = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Font FONT_TITRE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(30,  41,  59));
    private static final Font FONT_SOUS  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(51,  65,  85));
    private static final Font FONT_META  = FontFactory.getFont(FontFactory.HELVETICA,       9,  new Color(100, 116, 139));
    private static final Font FONT_TH    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  8,  Color.WHITE);
    private static final Font FONT_TD    = FontFactory.getFont(FontFactory.HELVETICA,        8,  new Color(30,  41,  59));
    private static final Font FONT_OK    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  8,  new Color(22,  163,  74));
    private static final Font FONT_ALERT = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  8,  new Color(220, 38,  38));

    private static final Color HDR  = new Color(30,  64, 175);
    private static final Color ALT  = new Color(248, 250, 252);
    private static final Color WHITE = Color.WHITE;

    // -------------------------------------------------------
    // RAPPORT INVENTAIRE — PDF
    // -------------------------------------------------------
    public byte[] genererInventaire(RapportInventaireDTO dto) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 30, 30, 40, 30);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            // En-tête
            doc.add(new Paragraph("StockMaster — Rapport d'inventaire", FONT_TITRE));
            doc.add(new Paragraph("Référence : " + dto.getReference()
                + "   |   Entrepôt : " + dto.getEntrepotNom()
                + "   |   Type : " + dto.getType(), FONT_META));
            doc.add(new Paragraph("Créé par : " + dto.getCreateurNom()
                + (dto.getValideurNom() != null ? "   |   Validé par : " + dto.getValideurNom() : "")
                + (dto.getDateValidation() != null ? "   |   Date : " + dto.getDateValidation().format(FMT) : ""),
                FONT_META));
            doc.add(new Paragraph("Lignes : " + dto.getTotalLignes()
                + "   Avec écart : " + dto.getLignesAvecEcart()
                + "   Surplus : " + dto.getLignesSurplus()
                + "   Manque : " + dto.getLignesManque(), FONT_META));
            doc.add(Chunk.NEWLINE);

            // Tableau des lignes
            PdfPTable table = new PdfPTable(new float[]{3f, 4f, 2.5f, 1.5f, 1.5f, 1.5f, 1.5f, 3f});
            table.setWidthPercentage(100);
            addTh(table, "Référence");
            addTh(table, "Produit");
            addTh(table, "Catégorie");
            addTh(table, "Théorique");
            addTh(table, "Compté");
            addTh(table, "Écart");
            addTh(table, "Sens");
            addTh(table, "Note");

            boolean alt = false;
            for (RapportInventaireDTO.LigneRapportDTO l : dto.getLignes()) {
                Color bg = alt ? ALT : WHITE;
                addTd(table, l.getProduitReference(), bg, FONT_TD);
                addTd(table, l.getProduitNom(),        bg, FONT_TD);
                addTd(table, l.getProduitCategorie(),  bg, FONT_TD);
                addTdRight(table, l.getQuantiteTheorique() != null ? String.valueOf(l.getQuantiteTheorique()) : "—", bg, FONT_TD);
                addTdRight(table, l.getQuantiteComptee()  != null ? String.valueOf(l.getQuantiteComptee())  : "—", bg, FONT_TD);
                int ecart = l.getEcart() != null ? l.getEcart() : 0;
                Font fe = ecart > 0 ? FONT_OK : ecart < 0 ? FONT_ALERT : FONT_TD;
                addTdRight(table, l.getEcart() != null ? (ecart > 0 ? "+" + ecart : String.valueOf(ecart)) : "—", bg, fe);
                addTd(table, l.getSensEcart() != null ? l.getSensEcart() : "—", bg, fe);
                addTd(table, l.getNote() != null ? l.getNote() : "—", bg, FONT_META);
                alt = !alt;
            }
            doc.add(table);
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF inventaire", e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    // -------------------------------------------------------
    // RAPPORT MOUVEMENTS — PDF
    // -------------------------------------------------------
    public byte[] genererMouvements(RapportMouvementsDTO dto) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 30, 30, 40, 30);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            doc.add(new Paragraph("StockMaster — Rapport des mouvements de stock", FONT_TITRE));
            doc.add(new Paragraph("Période : " + dto.getDateDebut().format(FMT)
                + " → " + dto.getDateFin().format(FMT)
                + "   |   Total : " + dto.getTotalMouvements() + " mouvement(s)", FONT_META));
            doc.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(new float[]{1.5f, 3f, 4f, 3f, 1.5f, 1.5f, 2.5f, 2f, 3f});
            table.setWidthPercentage(100);
            addTh(table, "Type");
            addTh(table, "Référence");
            addTh(table, "Produit");
            addTh(table, "Entrepôt");
            addTh(table, "Qté");
            addTh(table, "Après");
            addTh(table, "Document");
            addTh(table, "Opérateur");
            addTh(table, "Date");

            boolean alt = false;
            for (RapportMouvementsDTO.LigneMouvementDTO m : dto.getMouvements()) {
                Color bg = alt ? ALT : WHITE;
                boolean entree = m.getType().contains("ENTREE") || m.getType().equals("AJUSTEMENT");
                Font ft = entree ? FONT_OK : FONT_ALERT;
                addTd(table, m.getType(),           bg, ft);
                addTd(table, nvl(m.getReference()), bg, FONT_TD);
                addTd(table, m.getProduitNom(),      bg, FONT_TD);
                addTd(table, m.getEntrepotNom(),     bg, FONT_TD);
                addTdRight(table, String.valueOf(m.getQuantite()),     bg, FONT_TD);
                addTdRight(table, String.valueOf(m.getQuantiteApres()), bg, FONT_TD);
                addTd(table, nvl(m.getReference()),       bg, FONT_META);
                addTd(table, nvl(m.getUtilisateurNom()),  bg, FONT_META);
                addTd(table, m.getDateCreation() != null ? m.getDateCreation().format(FMT) : "—", bg, FONT_META);
                alt = !alt;
            }
            doc.add(table);
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF mouvements", e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    // -------------------------------------------------------
    // RAPPORT FOURNISSEURS — PDF
    // -------------------------------------------------------
    public byte[] genererFournisseurs(RapportFournisseursDTO dto) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 30, 30, 40, 30);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            doc.add(new Paragraph("StockMaster — Rapport des fournisseurs", FONT_TITRE));
            doc.add(new Paragraph("Période : " + dto.getDateDebut().format(FMT)
                + " → " + dto.getDateFin().format(FMT)
                + "   |   Commandes : " + dto.getTotalCommandes()
                + "   |   Montant HT : " + dto.getMontantTotalHT() + " €", FONT_META));
            doc.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(new float[]{3f, 3f, 1.5f, 1.5f, 1.5f, 2.5f});
            table.setWidthPercentage(100);
            addTh(table, "Fournisseur");
            addTh(table, "Email");
            addTh(table, "Commandes");
            addTh(table, "Livrées");
            addTh(table, "Annulées");
            addTh(table, "Montant HT");

            boolean alt = false;
            for (RapportFournisseursDTO.LigneFournisseurDTO f : dto.getFournisseurs()) {
                Color bg = alt ? ALT : WHITE;
                addTd(table, f.getFournisseurNom(),   bg, FONT_SOUS);
                addTd(table, f.getFournisseurEmail(),  bg, FONT_META);
                addTdRight(table, String.valueOf(f.getNbCommandes()), bg, FONT_TD);
                addTdRight(table, String.valueOf(f.getNbLivrees()),   bg, FONT_OK);
                addTdRight(table, String.valueOf(f.getNbAnnulees()),  bg, f.getNbAnnulees() > 0 ? FONT_ALERT : FONT_TD);
                addTdRight(table, f.getMontantTotal() + " €",         bg, FONT_TD);
                alt = !alt;
            }
            doc.add(table);
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF fournisseurs", e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    // -------------------------------------------------------
    // HELPERS INTERNES
    // -------------------------------------------------------
    private void addTh(PdfPTable t, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, FONT_TH));
        c.setBackgroundColor(HDR);
        c.setPadding(5);
        c.setBorder(Rectangle.NO_BORDER);
        t.addCell(c);
    }

    private void addTd(PdfPTable t, String text, Color bg, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "—", font));
        c.setBackgroundColor(bg);
        c.setPadding(4);
        c.setBorderColor(new Color(226, 232, 240));
        c.setBorderWidth(0.5f);
        t.addCell(c);
    }

    private void addTdRight(PdfPTable t, String text, Color bg, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "—", font));
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c.setBackgroundColor(bg);
        c.setPadding(4);
        c.setBorderColor(new Color(226, 232, 240));
        c.setBorderWidth(0.5f);
        t.addCell(c);
    }

    private String nvl(String s) { return s != null ? s : "—"; }
}
