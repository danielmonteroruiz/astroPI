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
    private final String SECRET = "clave_super_Segura_1234567879";

    //Generamos Key a partir de SECRET
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    /**
     * Generar un token JWT a partir del username.
     * @param username usuario autenticado
     * @return token JWT firmado
     */
    public String generateToken(String username){
        return Jwts.builder()
                .setSubject(username) //identifica al usuario
                .setIssuedAt(new Date()) //fecha de cracion
                .setExpiration(new Date(System.currentTimeMillis() +86400000)) //Expira en 24h
                .signWith(key) //firma del token
                .compact();
    }

/**
 * Extrae el username del token JWT.
 * @param token JWT recibido
 * @return username contenido en el token
 */

public String extractUsername (String token){
    return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
}

}
