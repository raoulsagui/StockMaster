package com.example.backend.module.alerte.service;

import com.example.backend.module.alerte.entity.Alerte;
import com.example.backend.module.utilisateur.entity.Role;
import com.example.backend.module.utilisateur.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service d'envoi d'emails pour les alertes.
 *
 * Les emails sont envoyés en ASYNC pour ne pas bloquer le scheduler.
 * Destinataires : tous les utilisateurs avec rôle ADMIN ou GESTIONNAIRE.
 * Format : HTML avec design professionnel (inline CSS).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlerteMailService {

    private final JavaMailSender mailSender;
    private final UtilisateurRepository utilisateurRepository;

    @Value("${app.mail.from}")
    private String expediteur;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    /**
     * Envoie un email HTML pour une alerte donnée.
     * Async : ne bloque pas le thread du scheduler.
     */
    @Async
    public void envoyerAlerteEmail(Alerte alerte) {
        // Récupère les destinataires : ADMIN + GESTIONNAIRE
        List<String> destinataires = utilisateurRepository.findAll().stream()
                .filter(u -> u.isActif()
                        && (u.getRole() == Role.ADMIN || u.getRole() == Role.GESTIONNAIRE))
                .map(u -> u.getEmail())
                .toList();

        if (destinataires.isEmpty()) {
            log.warn("Aucun destinataire trouvé pour l'alerte {}", alerte.getId());
            return;
        }

        String sujet   = buildSujet(alerte);
        String corps   = buildCorpsHtml(alerte);

        destinataires.forEach(email -> {
            try {
                var msg = mailSender.createMimeMessage();
                var helper = new MimeMessageHelper(msg, true, "UTF-8");
                helper.setFrom(expediteur);
                helper.setTo(email);
                helper.setSubject(sujet);
                helper.setText(corps, true);
                mailSender.send(msg);
                log.info("Email alerte [{}] envoyé à {}", alerte.getType(), email);
            } catch (Exception e) {
                log.error("Erreur envoi email alerte à {} : {}", email, e.getMessage());
            }
        });
    }

    // -------------------------------------------------------
    // CONSTRUCTION DU SUJET
    // -------------------------------------------------------
    private String buildSujet(Alerte alerte) {
        String emoji = switch (alerte.getSeverite()) {
            case CRITIQUE -> "🔴";
            case WARNING  -> "🟠";
            case INFO     -> "🔵";
        };
        return switch (alerte.getType()) {
            case STOCK_FAIBLE   -> emoji + " [StockMaster] Stock faible — " + alerte.getStock().getProduit().getNom();
            case PRODUIT_EXPIRE -> emoji + " [StockMaster] Produit expiré — " + alerte.getStock().getProduit().getNom();
            case ZONE_SATUREE   -> emoji + " [StockMaster] Zone saturée — " + alerte.getZone().getNom();
        };
    }

    // -------------------------------------------------------
    // CONSTRUCTION DU CORPS HTML
    // -------------------------------------------------------
    private String buildCorpsHtml(Alerte alerte) {
        String couleurBadge = switch (alerte.getSeverite()) {
            case CRITIQUE -> "#dc2626";
            case WARNING  -> "#d97706";
            case INFO     -> "#2563eb";
        };
        String labelSeverite = switch (alerte.getSeverite()) {
            case CRITIQUE -> "CRITIQUE";
            case WARNING  -> "AVERTISSEMENT";
            case INFO     -> "INFORMATION";
        };

        StringBuilder details = new StringBuilder();
        if (alerte.getStock() != null) {
            var stock = alerte.getStock();
            details.append(row("Produit",   stock.getProduit().getNom()))
                   .append(row("Référence", stock.getProduit().getReference()))
                   .append(row("Entrepôt",  stock.getEntrepot().getNom()))
                   .append(row("Quantité disponible", String.valueOf(stock.getQuantiteDisponible())))
                   .append(row("Seuil minimum",
                           alerte.getSeuil() != null ? String.valueOf(alerte.getSeuil().intValue()) : "—"));
        }
        if (alerte.getZone() != null) {
            var zone = alerte.getZone();
            details.append(row("Zone",      zone.getNom()))
                   .append(row("Entrepôt",  zone.getEntrepot().getNom()))
                   .append(row("Taux d'occupation",
                           alerte.getValeurActuelle() != null
                               ? String.format("%.1f %%", alerte.getValeurActuelle()) : "—"))
                   .append(row("Seuil critique",
                           alerte.getSeuil() != null
                               ? String.format("%.0f %%", alerte.getSeuil()) : "—"));
        }

        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head><meta charset="UTF-8"/></head>
            <body style="margin:0;padding:0;font-family:Arial,sans-serif;background:#f1f5f9;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f1f5f9;padding:32px 0;">
                <tr><td align="center">
                  <table width="600" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.08);">

                    <!-- HEADER -->
                    <tr>
                      <td style="background:%s;padding:24px 32px;">
                        <p style="margin:0;font-size:12px;color:rgba(255,255,255,.7);text-transform:uppercase;letter-spacing:1px;">STOCKMASTER — ALERTE AUTOMATIQUE</p>
                        <h1 style="margin:8px 0 0;font-size:22px;color:#ffffff;">%s</h1>
                        <span style="display:inline-block;margin-top:8px;padding:3px 10px;background:rgba(255,255,255,.2);border-radius:20px;font-size:11px;color:#fff;font-weight:bold;">%s</span>
                      </td>
                    </tr>

                    <!-- CORPS -->
                    <tr>
                      <td style="padding:28px 32px;">
                        <p style="margin:0 0 20px;font-size:15px;color:#374151;line-height:1.6;">%s</p>
                        <table width="100%%" cellpadding="0" cellspacing="0" style="border-radius:8px;overflow:hidden;border:1px solid #e5e7eb;">
                          %s
                        </table>
                        <p style="margin:20px 0 0;font-size:12px;color:#9ca3af;">Détectée le %s — Rendez-vous sur StockMaster pour prendre les mesures nécessaires.</p>
                      </td>
                    </tr>

                    <!-- FOOTER -->
                    <tr>
                      <td style="background:#f8fafc;padding:16px 32px;border-top:1px solid #e5e7eb;">
                        <p style="margin:0;font-size:11px;color:#9ca3af;text-align:center;">
                          Cet email a été généré automatiquement par StockMaster. Ne pas répondre.
                        </p>
                      </td>
                    </tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(
                couleurBadge,
                labelTitre(alerte),
                labelSeverite,
                alerte.getMessage(),
                details.toString(),
                alerte.getDateCreation() != null ? alerte.getDateCreation().format(FMT) : "—"
            );
    }

    private String labelTitre(Alerte alerte) {
        return switch (alerte.getType()) {
            case STOCK_FAIBLE   -> "Stock en dessous du seuil minimum";
            case PRODUIT_EXPIRE -> "Produit arrivé à expiration";
            case ZONE_SATUREE   -> "Zone de stockage saturée";
        };
    }

    private String row(String label, String valeur) {
        return """
            <tr>
              <td style="padding:10px 16px;font-size:13px;color:#6b7280;background:#f9fafb;width:45%%;border-bottom:1px solid #e5e7eb;">%s</td>
              <td style="padding:10px 16px;font-size:13px;font-weight:600;color:#111827;background:#ffffff;border-bottom:1px solid #e5e7eb;">%s</td>
            </tr>
            """.formatted(label, valeur);
    }
}
