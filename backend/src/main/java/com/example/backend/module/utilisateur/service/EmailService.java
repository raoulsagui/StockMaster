package com.example.backend.module.utilisateur.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * Service d'envoi d'emails.
 *
 * En dev : utilise Mailpit (serveur SMTP local qui intercepte les mails).
 * En prod : remplacer la config SMTP dans application.properties.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    /**
     * Envoie les identifiants de connexion à un nouvel utilisateur.
     *
     * @param destinataire Email du nouvel utilisateur
     * @param prenom       Prénom pour personnaliser le message
     * @param motDePasse   Mot de passe provisoire généré
     */
    public void envoyerIdentifiantsCreation(String destinataire, String prenom, String motDePasse) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(destinataire);
            helper.setSubject("Bienvenue sur StockMaster — Vos identifiants de connexion");

            String html = """
                    <div style="font-family: Inter, Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 24px;">
                        <div style="background: #1e40af; padding: 24px; border-radius: 12px 12px 0 0; text-align: center;">
                            <h1 style="color: white; margin: 0; font-size: 24px;">StockMaster</h1>
                            <p style="color: #bfdbfe; margin: 8px 0 0;">Gestion des stocks</p>
                        </div>
                        <div style="background: white; padding: 32px; border: 1px solid #e5e7eb; border-top: none; border-radius: 0 0 12px 12px;">
                            <h2 style="color: #111827; margin: 0 0 16px;">Bonjour %s,</h2>
                            <p style="color: #6b7280; line-height: 1.6;">
                                Un compte a été créé pour vous sur StockMaster.
                                Voici vos identifiants de connexion :
                            </p>
                            <div style="background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 8px; padding: 20px; margin: 24px 0;">
                                <p style="margin: 0 0 8px; color: #374151;">
                                    <strong>Email :</strong> %s
                                </p>
                                <p style="margin: 0; color: #374151;">
                                    <strong>Mot de passe provisoire :</strong>
                                    <span style="font-family: monospace; background: #dbeafe; color: #1d4ed8; padding: 2px 8px; border-radius: 4px;">%s</span>
                                </p>
                            </div>
                            <div style="background: #fef3c7; border: 1px solid #fcd34d; border-radius: 8px; padding: 16px; margin-bottom: 24px;">
                                <p style="margin: 0; color: #92400e; font-size: 14px;">
                                    ⚠️ Vous devrez changer ce mot de passe à votre première connexion.
                                </p>
                            </div>
                            <a href="http://localhost:5173/login"
                               style="display: inline-block; background: #1d4ed8; color: white; padding: 12px 24px;
                                      border-radius: 8px; text-decoration: none; font-weight: 600;">
                                Se connecter
                            </a>
                        </div>
                        <p style="text-align: center; color: #9ca3af; font-size: 12px; margin-top: 16px;">
                            Cet email a été envoyé automatiquement, merci de ne pas y répondre.
                        </p>
                    </div>
                    """.formatted(prenom, destinataire, motDePasse);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            // On ne bloque pas la création si l'email échoue
            System.err.println("⚠️ Échec envoi email à " + destinataire + " : " + e.getMessage());
        }
    }

    /**
     * Envoie le nouveau mot de passe provisoire après réinitialisation.
     */
    public void envoyerReinitialisationMotDePasse(String destinataire, String prenom, String motDePasse) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(destinataire);
            helper.setSubject("StockMaster — Réinitialisation de votre mot de passe");

            String html = """
                    <div style="font-family: Inter, Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 24px;">
                        <div style="background: #1e40af; padding: 24px; border-radius: 12px 12px 0 0; text-align: center;">
                            <h1 style="color: white; margin: 0; font-size: 24px;">StockMaster</h1>
                        </div>
                        <div style="background: white; padding: 32px; border: 1px solid #e5e7eb; border-top: none; border-radius: 0 0 12px 12px;">
                            <h2 style="color: #111827; margin: 0 0 16px;">Bonjour %s,</h2>
                            <p style="color: #6b7280; line-height: 1.6;">
                                Votre mot de passe a été réinitialisé par un administrateur.
                            </p>
                            <div style="background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 8px; padding: 20px; margin: 24px 0;">
                                <p style="margin: 0; color: #374151;">
                                    <strong>Nouveau mot de passe provisoire :</strong>
                                    <span style="font-family: monospace; background: #dbeafe; color: #1d4ed8; padding: 2px 8px; border-radius: 4px;">%s</span>
                                </p>
                            </div>
                            <div style="background: #fef3c7; border: 1px solid #fcd34d; border-radius: 8px; padding: 16px; margin-bottom: 24px;">
                                <p style="margin: 0; color: #92400e; font-size: 14px;">
                                    ⚠️ Vous devrez changer ce mot de passe à votre prochaine connexion.
                                </p>
                            </div>
                            <a href="http://localhost:5173/login"
                               style="display: inline-block; background: #1d4ed8; color: white; padding: 12px 24px;
                                      border-radius: 8px; text-decoration: none; font-weight: 600;">
                                Se connecter
                            </a>
                        </div>
                    </div>
                    """.formatted(prenom, motDePasse);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("⚠️ Échec envoi email à " + destinataire + " : " + e.getMessage());
        }
    }
}
