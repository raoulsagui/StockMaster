package com.example.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service responsable de la gestion des JWT (JSON Web Tokens).
 *
 * Un JWT est composé de 3 parties séparées par des points :
 *   HEADER.PAYLOAD.SIGNATURE
 *
 *   - Header  : algorithme de signature (HS256)
 *   - Payload : données (claims) : email, rôle, expiration
 *   - Signature : HMAC-SHA256(header + payload + secret)
 *
 * Ce service :
 *   1. Génère un token à la connexion
 *   2. Extrait les informations d'un token reçu
 *   3. Valide qu'un token est légitime et non expiré
 */
@Service
public class JwtService {

    /**
     * Clé secrète lue depuis application.properties.
     * Elle sert à signer et vérifier les tokens.
     * En production : stocker dans une variable d'environnement.
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Durée de validité du token en millisecondes (24h par défaut).
     * Lue depuis application.properties.
     */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Génère un token JWT pour un utilisateur.
     * Le token contient l'email (subject) et le rôle (claim custom).
     *
     * @param userDetails L'utilisateur pour lequel générer le token
     * @return Le token JWT sous forme de chaîne "xxxxx.yyyyy.zzzzz"
     */
    public String genererToken(UserDetails userDetails) {
        // Claims supplémentaires qu'on intègre dans le payload
        Map<String, Object> claimsSupplementaires = new HashMap<>();
        claimsSupplementaires.put("roles", userDetails.getAuthorities().toString());

        return Jwts.builder()
                // Subject = identifiant unique de l'utilisateur (son email)
                .subject(userDetails.getUsername())
                // Claims supplémentaires (rôles)
                .claims(claimsSupplementaires)
                // Date d'émission du token
                .issuedAt(new Date(System.currentTimeMillis()))
                // Date d'expiration
                .expiration(new Date(System.currentTimeMillis() + expiration))
                // Signature avec notre clé secrète (algorithme HS256)
                .signWith(getCleSecrete())
                .compact();
    }

    /**
     * Extrait l'email (subject) d'un token.
     * Utilisé dans le filtre JWT pour identifier l'utilisateur.
     */
    public String extraireEmail(String token) {
        return extraireClaim(token, Claims::getSubject);
    }

    /**
     * Vérifie si un token est valide pour un utilisateur donné.
     * Contrôle :
     *   1. L'email dans le token correspond à l'utilisateur
     *   2. Le token n'est pas expiré
     */
    public boolean estValide(String token, UserDetails userDetails) {
        final String email = extraireEmail(token);
        return email.equals(userDetails.getUsername()) && !estExpire(token);
    }

    /**
     * Méthode générique pour extraire n'importe quel claim.
     * Utilise une Function comme extracteur pour rester flexible.
     */
    public <T> T extraireClaim(String token, Function<Claims, T> extracteur) {
        final Claims claims = extraireTousClaims(token);
        return extracteur.apply(claims);
    }

    /**
     * Parse et retourne tous les claims du token.
     * Lance une exception si le token est invalide ou la signature incorrecte.
     */
    private Claims extraireTousClaims(String token) {
        return Jwts.parser()
                .verifyWith(getCleSecrete())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Vérifie si la date d'expiration est passée */
    private boolean estExpire(String token) {
        return extraireClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Convertit la clé secrète (String) en objet SecretKey pour JJWT.
     * HMAC-SHA256 nécessite une clé d'au moins 256 bits.
     */
    private SecretKey getCleSecrete() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
