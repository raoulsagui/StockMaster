package com.example.backend.module.rapport.service;

import com.example.backend.module.rapport.dto.RapportFournisseursDTO;
import com.example.backend.module.rapport.dto.RapportInventaireDTO;
import com.example.backend.module.rapport.dto.RapportMouvementsDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Générateur Excel (.xlsx) — utilise Apache POI.
 * Chaque méthode produit un byte[] prêt à être streamé en HTTP.
 */
@Component
public class RapportExcelGenerator {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // -------------------------------------------------------
    // RAPPORT INVENTAIRE — EXCEL
    // -------------------------------------------------------
    public byte[] genererInventaire(RapportInventaireDTO dto) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Inventaire " + dto.getReference());
            sheet.setColumnWidth(0, 4000); sheet.setColumnWidth(1, 7000);
            sheet.setColumnWidth(2, 5000); sheet.setColumnWidth(3, 3000);
            sheet.setColumnWidth(4, 3000); sheet.setColumnWidth(5, 3000);
            sheet.setColumnWidth(6, 3500); sheet.setColumnWidth(7, 6000);

            CellStyle styleTitre = creerStyleTitre(wb);
            CellStyle styleMeta  = creerStyleMeta(wb);
            CellStyle styleTh    = creerStyleHeader(wb);
            CellStyle styleTd    = creerStyleCell(wb, false);
            CellStyle styleAlt   = creerStyleCell(wb, true);
            CellStyle styleOk    = creerStyleColore(wb, new byte[]{(byte)22,(byte)163,(byte)74});
            CellStyle styleAlert = creerStyleColore(wb, new byte[]{(byte)220,(byte)38,(byte)38});

            int r = 0;
            Row rowTitre = sheet.createRow(r++);
            Cell cTitre = rowTitre.createCell(0);
            cTitre.setCellValue("Rapport d'inventaire — " + dto.getReference());
            cTitre.setCellStyle(styleTitre);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

            Row rowMeta = sheet.createRow(r++);
            rowMeta.createCell(0).setCellValue("Entrepôt : " + dto.getEntrepotNom());
            rowMeta.getCell(0).setCellStyle(styleMeta);
            rowMeta.createCell(2).setCellValue("Type : " + dto.getType());
            rowMeta.getCell(2).setCellStyle(styleMeta);
            rowMeta.createCell(4).setCellValue("Statut : " + dto.getStatut());
            rowMeta.getCell(4).setCellStyle(styleMeta);

            Row rowMeta2 = sheet.createRow(r++);
            rowMeta2.createCell(0).setCellValue("Créé par : " + dto.getCreateurNom());
            rowMeta2.getCell(0).setCellStyle(styleMeta);
            if (dto.getValideurNom() != null) {
                rowMeta2.createCell(2).setCellValue("Validé par : " + dto.getValideurNom());
                rowMeta2.getCell(2).setCellStyle(styleMeta);
            }
            if (dto.getDateValidation() != null) {
                rowMeta2.createCell(4).setCellValue("Date : " + dto.getDateValidation().format(FMT));
                rowMeta2.getCell(4).setCellStyle(styleMeta);
            }
            r++;

            String[] headers = {"Référence","Produit","Catégorie","Théorique","Compté","Écart","Sens","Note"};
            Row rowH = sheet.createRow(r++);
            for (int i = 0; i < headers.length; i++) {
                Cell c = rowH.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(styleTh);
            }

            boolean alt = false;
            for (RapportInventaireDTO.LigneRapportDTO l : dto.getLignes()) {
                Row row = sheet.createRow(r++);
                CellStyle bg = alt ? styleAlt : styleTd;
                row.createCell(0).setCellValue(l.getProduitReference()); row.getCell(0).setCellStyle(bg);
                row.createCell(1).setCellValue(l.getProduitNom());       row.getCell(1).setCellStyle(bg);
                row.createCell(2).setCellValue(l.getProduitCategorie()); row.getCell(2).setCellStyle(bg);
                if (l.getQuantiteTheorique() != null) { row.createCell(3).setCellValue(l.getQuantiteTheorique()); row.getCell(3).setCellStyle(bg); }
                if (l.getQuantiteComptee()  != null) { row.createCell(4).setCellValue(l.getQuantiteComptee());   row.getCell(4).setCellStyle(bg); }
                if (l.getEcart() != null) {
                    row.createCell(5).setCellValue(l.getEcart());
                    row.getCell(5).setCellStyle(l.getEcart() > 0 ? styleOk : l.getEcart() < 0 ? styleAlert : bg);
                }
                row.createCell(6).setCellValue(l.getSensEcart() != null ? l.getSensEcart() : "");
                row.getCell(6).setCellStyle(l.getEcart() != null && l.getEcart() > 0 ? styleOk : l.getEcart() != null && l.getEcart() < 0 ? styleAlert : bg);
                row.createCell(7).setCellValue(l.getNote() != null ? l.getNote() : "");
                row.getCell(7).setCellStyle(bg);
                alt = !alt;
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération Excel inventaire", e);
        }
    }

    // -------------------------------------------------------
    // RAPPORT MOUVEMENTS — EXCEL
    // -------------------------------------------------------
    public byte[] genererMouvements(RapportMouvementsDTO dto) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Mouvements");
            int[] widths = {4000,4000,7000,5000,3000,3000,5000,5000,6000};
            for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i]);

            CellStyle styleTitre = creerStyleTitre(wb);
            CellStyle styleMeta  = creerStyleMeta(wb);
            CellStyle styleTh    = creerStyleHeader(wb);
            CellStyle styleTd    = creerStyleCell(wb, false);
            CellStyle styleAlt   = creerStyleCell(wb, true);
            CellStyle styleOk    = creerStyleColore(wb, new byte[]{(byte)22,(byte)163,(byte)74});
            CellStyle styleAlert = creerStyleColore(wb, new byte[]{(byte)220,(byte)38,(byte)38});

            int r = 0;
            Row rowT = sheet.createRow(r++);
            Cell ct = rowT.createCell(0);
            ct.setCellValue("Rapport des mouvements de stock");
            ct.setCellStyle(styleTitre);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            Row rowM = sheet.createRow(r++);
            rowM.createCell(0).setCellValue("Période : " + dto.getDateDebut().format(FMT) + " → " + dto.getDateFin().format(FMT));
            rowM.getCell(0).setCellStyle(styleMeta);
            rowM.createCell(5).setCellValue("Total : " + dto.getTotalMouvements());
            rowM.getCell(5).setCellStyle(styleMeta);
            r++;

            String[] headers = {"Type","Référence","Produit","Entrepôt","Qté","Après","Document","Opérateur","Date"};
            Row rowH = sheet.createRow(r++);
            for (int i = 0; i < headers.length; i++) {
                Cell c = rowH.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(styleTh);
            }

            boolean alt = false;
            for (RapportMouvementsDTO.LigneMouvementDTO m : dto.getMouvements()) {
                Row row = sheet.createRow(r++);
                CellStyle bg = alt ? styleAlt : styleTd;
                boolean entree = m.getType().contains("ENTREE") || m.getType().equals("AJUSTEMENT");
                row.createCell(0).setCellValue(m.getType());           row.getCell(0).setCellStyle(entree ? styleOk : styleAlert);
                row.createCell(1).setCellValue(nvl(m.getReference())); row.getCell(1).setCellStyle(bg);
                row.createCell(2).setCellValue(m.getProduitNom());     row.getCell(2).setCellStyle(bg);
                row.createCell(3).setCellValue(m.getEntrepotNom());    row.getCell(3).setCellStyle(bg);
                row.createCell(4).setCellValue(m.getQuantite());       row.getCell(4).setCellStyle(bg);
                row.createCell(5).setCellValue(m.getQuantiteApres());  row.getCell(5).setCellStyle(bg);
                row.createCell(6).setCellValue(nvl(m.getReference())); row.getCell(6).setCellStyle(bg);
                row.createCell(7).setCellValue(nvl(m.getUtilisateurNom())); row.getCell(7).setCellStyle(bg);
                row.createCell(8).setCellValue(m.getDateCreation() != null ? m.getDateCreation().format(FMT) : ""); row.getCell(8).setCellStyle(bg);
                alt = !alt;
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération Excel mouvements", e);
        }
    }

    // -------------------------------------------------------
    // RAPPORT FOURNISSEURS — EXCEL
    // -------------------------------------------------------
    public byte[] genererFournisseurs(RapportFournisseursDTO dto) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Fournisseurs");
            int[] widths = {6000,6000,3500,3500,3500,4500};
            for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i]);

            CellStyle styleTitre = creerStyleTitre(wb);
            CellStyle styleMeta  = creerStyleMeta(wb);
            CellStyle styleTh    = creerStyleHeader(wb);
            CellStyle styleTd    = creerStyleCell(wb, false);
            CellStyle styleAlt   = creerStyleCell(wb, true);
            CellStyle styleOk    = creerStyleColore(wb, new byte[]{(byte)22,(byte)163,(byte)74});
            CellStyle styleAlert = creerStyleColore(wb, new byte[]{(byte)220,(byte)38,(byte)38});

            int r = 0;
            Row rowT = sheet.createRow(r++);
            Cell ct = rowT.createCell(0);
            ct.setCellValue("Rapport des fournisseurs");
            ct.setCellStyle(styleTitre);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            Row rowM = sheet.createRow(r++);
            rowM.createCell(0).setCellValue("Période : " + dto.getDateDebut().format(FMT) + " → " + dto.getDateFin().format(FMT));
            rowM.getCell(0).setCellStyle(styleMeta);
            rowM.createCell(3).setCellValue("Total HT : " + dto.getMontantTotalHT() + " €");
            rowM.getCell(3).setCellStyle(styleMeta);
            r++;

            String[] headers = {"Fournisseur","Email","Commandes","Livrées","Annulées","Montant HT"};
            Row rowH = sheet.createRow(r++);
            for (int i = 0; i < headers.length; i++) {
                Cell c = rowH.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(styleTh);
            }

            boolean alt = false;
            for (RapportFournisseursDTO.LigneFournisseurDTO f : dto.getFournisseurs()) {
                Row row = sheet.createRow(r++);
                CellStyle bg = alt ? styleAlt : styleTd;
                row.createCell(0).setCellValue(f.getFournisseurNom());   row.getCell(0).setCellStyle(bg);
                row.createCell(1).setCellValue(f.getFournisseurEmail()); row.getCell(1).setCellStyle(bg);
                row.createCell(2).setCellValue(f.getNbCommandes());      row.getCell(2).setCellStyle(bg);
                row.createCell(3).setCellValue(f.getNbLivrees());        row.getCell(3).setCellStyle(styleOk);
                row.createCell(4).setCellValue(f.getNbAnnulees());       row.getCell(4).setCellStyle(f.getNbAnnulees() > 0 ? styleAlert : bg);
                row.createCell(5).setCellValue(f.getMontantTotal() != null ? f.getMontantTotal().doubleValue() : 0); row.getCell(5).setCellStyle(bg);
                alt = !alt;
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération Excel fournisseurs", e);
        }
    }

    // -------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------
    private CellStyle creerStyleTitre(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); f.setFontHeightInPoints((short)14);
        s.setFont(f);
        return s;
    }
    private CellStyle creerStyleMeta(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setColor(IndexedColors.GREY_50_PERCENT.getIndex()); f.setFontHeightInPoints((short)9);
        s.setFont(f);
        return s;
    }
    private CellStyle creerStyleHeader(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setBorderBottom(BorderStyle.THIN);
        return s;
    }
    private CellStyle creerStyleCell(Workbook wb, boolean alt) {
        CellStyle s = wb.createCellStyle();
        if (alt) { s.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex()); s.setFillPattern(FillPatternType.SOLID_FOREGROUND); }
        s.setBorderBottom(BorderStyle.THIN); s.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return s;
    }
    private CellStyle creerStyleColore(Workbook wb, byte[] rgb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true);
        // On utilise les couleurs standard disponibles dans POI
        f.setColor(rgb[0] > 0 ? IndexedColors.GREEN.getIndex() : IndexedColors.RED.getIndex());
        s.setFont(f);
        return s;
    }
    private String nvl(String s) { return s != null ? s : ""; }
}
