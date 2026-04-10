package com.astropi.astropi.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Clase utilitaria para gestionar tokens JWT.
 */
@Component
public class JwtUtil {

    //Clave secreta (EN PRODUCCION: ponerlo en variable de entorno)
    private final String SECRET = "astropi_super_secret_key_very_secure_2026_1234567890";

    //Generamos Key a partir de SECRET
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

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
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) //Expira en 24h
                .signWith(key) //firma del token
                .compact();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
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
