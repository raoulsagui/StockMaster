package com.example.backend.module.rapport.service;

import com.example.backend.module.rapport.dto.RapportFournisseursDTO;
import com.example.backend.module.rapport.dto.RapportInventaireDTO;
import com.example.backend.module.rapport.dto.RapportMouvementsDTO;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Générateur CSV — RFC 4180, séparateur virgule, encoding UTF-8 avec BOM.
 * Le BOM garantit l'ouverture correcte dans Excel Windows.
 */
@Component
public class RapportCsvGenerator {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String SEP = ";";  // point-virgule pour compatibilité Excel FR
    private static final String NL  = "\r\n";
    private static final byte[] BOM = {(byte)0xEF, (byte)0xBB, (byte)0xBF};

    // -------------------------------------------------------
    // RAPPORT INVENTAIRE — CSV
    // -------------------------------------------------------
    public byte[] genererInventaire(RapportInventaireDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("Référence").append(SEP).append(dto.getReference()).append(NL);
        sb.append("Entrepôt").append(SEP).append(dto.getEntrepotNom()).append(NL);
        sb.append("Type").append(SEP).append(dto.getType()).append(NL);
        sb.append("Statut").append(SEP).append(dto.getStatut()).append(NL);
        sb.append("Créé par").append(SEP).append(dto.getCreateurNom()).append(NL);
        if (dto.getValideurNom() != null)
            sb.append("Validé par").append(SEP).append(dto.getValideurNom()).append(NL);
        if (dto.getDateValidation() != null)
            sb.append("Date validation").append(SEP).append(dto.getDateValidation().format(FMT)).append(NL);
        sb.append(NL);
        sb.append("Référence produit").append(SEP)
          .append("Produit").append(SEP)
          .append("Catégorie").append(SEP)
          .append("Théorique").append(SEP)
          .append("Compté").append(SEP)
          .append("Écart").append(SEP)
          .append("Sens").append(SEP)
          .append("Note").append(NL);

        for (RapportInventaireDTO.LigneRapportDTO l : dto.getLignes()) {
            sb.append(csv(l.getProduitReference())).append(SEP)
              .append(csv(l.getProduitNom())).append(SEP)
              .append(csv(l.getProduitCategorie())).append(SEP)
              .append(l.getQuantiteTheorique() != null ? l.getQuantiteTheorique() : "").append(SEP)
              .append(l.getQuantiteComptee() != null ? l.getQuantiteComptee() : "").append(SEP)
              .append(l.getEcart() != null ? l.getEcart() : "").append(SEP)
              .append(csv(l.getSensEcart())).append(SEP)
              .append(csv(l.getNote())).append(NL);
        }
        return concat(BOM, sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    // -------------------------------------------------------
    // RAPPORT MOUVEMENTS — CSV
    // -------------------------------------------------------
    public byte[] genererMouvements(RapportMouvementsDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("Type").append(SEP)
          .append("Référence").append(SEP)
          .append("Produit").append(SEP)
          .append("Entrepôt").append(SEP)
          .append("Quantité").append(SEP)
          .append("Stock après").append(SEP)
          .append("Document").append(SEP)
          .append("Note").append(SEP)
          .append("Opérateur").append(SEP)
          .append("Date").append(NL);

        for (RapportMouvementsDTO.LigneMouvementDTO m : dto.getMouvements()) {
            sb.append(csv(m.getType())).append(SEP)
              .append(csv(m.getReference())).append(SEP)
              .append(csv(m.getProduitNom())).append(SEP)
              .append(csv(m.getEntrepotNom())).append(SEP)
              .append(m.getQuantite()).append(SEP)
              .append(m.getQuantiteApres()).append(SEP)
              .append(csv(m.getReference())).append(SEP)
              .append(csv(m.getNote())).append(SEP)
              .append(csv(m.getUtilisateurNom())).append(SEP)
              .append(m.getDateCreation() != null ? m.getDateCreation().format(FMT) : "").append(NL);
        }
        return concat(BOM, sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    // -------------------------------------------------------
    // RAPPORT FOURNISSEURS — CSV
    // -------------------------------------------------------
    public byte[] genererFournisseurs(RapportFournisseursDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("Fournisseur").append(SEP)
          .append("Email").append(SEP)
          .append("Nb commandes").append(SEP)
          .append("Livrées").append(SEP)
          .append("Annulées").append(SEP)
          .append("Montant HT (€)").append(NL);

        for (RapportFournisseursDTO.LigneFournisseurDTO f : dto.getFournisseurs()) {
            sb.append(csv(f.getFournisseurNom())).append(SEP)
              .append(csv(f.getFournisseurEmail())).append(SEP)
              .append(f.getNbCommandes()).append(SEP)
              .append(f.getNbLivrees()).append(SEP)
              .append(f.getNbAnnulees()).append(SEP)
              .append(f.getMontantTotal() != null ? f.getMontantTotal().toPlainString() : "0").append(NL);
        }
        return concat(BOM, sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    // -------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------
    /** Échappe les guillemets et encadre si la valeur contient SEP ou guillemets */
    private String csv(String v) {
        if (v == null) return "";
        String escaped = v.replace("\"", "\"\"");
        return (escaped.contains(SEP) || escaped.contains("\"") || escaped.contains("\n"))
                ? "\"" + escaped + "\"" : escaped;
    }

    private byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
