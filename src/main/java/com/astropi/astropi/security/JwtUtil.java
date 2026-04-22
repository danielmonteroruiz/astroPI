package com.astropi.astropi.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Clase utilitaria para gestionar tokens JWT.
 */
@Component
public class JwtUtil {

    private final Key key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    /**
     * Generar un token JWT a partir del username.
     *
     * @param username usuario autenticado
     * @return token JWT firmado
     */
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username) //identifica al usuario
                .claim("role",role)
                .setIssuedAt(new Date()) //fecha de cracion
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs)) //Expira segun configuracion
                .signWith(key) //firma del token
                .compact();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public Date extractIssuedAt(String token) {
        return extractAllClaims(token).getIssuedAt();
    }

    /**
     * Extrae el username del token JWT.
     *
     * @param token JWT recibido
     * @return username contenido en el token
     */

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Valída el token JWT.
     * Comprueba:
     * - Que el username coincide
     * - Que no está expirado
     */
    public boolean validateToken(String token, String username) {

        final String extractedUsername = extractUsername(token);

        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    public boolean fueEmitidoAntesDeCambioCredenciales(String token, LocalDateTime credencialesActualizadasEn) {
        if (credencialesActualizadasEn == null) {
            return false;
        }

        Date issuedAt = extractIssuedAt(token);
        Date fechaCredenciales = Date.from(credencialesActualizadasEn.atZone(ZoneId.systemDefault()).toInstant());

        return issuedAt.before(fechaCredenciales);
    }

    /**
     * Comprueba si el token ha expirado.
     */

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrae la fecha de expiración del token.
     * Anterior a ahora:
     * Sí → está expirado
     * No → sigue válido
     */
    private Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    /**
     * Extrae todos los claims del token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
