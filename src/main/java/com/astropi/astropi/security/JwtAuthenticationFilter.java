
package com.astropi.astropi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


/**
  Filtro JWT.
 Se ejecuta en cada petición.
 - Extrae el token del header
 - Lo valida
 - Autentíca al usuario en Spring Security
 */

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter  {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException{

        // 1. Obtener header Authorization
        final String authHeader = request.getHeader("Authorization");

        System.out.println("Auth header: " + authHeader);

        String username = null;
        String token = null;


        // 2. Validar formato Bearer
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            token = authHeader.substring(7);
            System.out.println("Token: " + token);

            username = jwtUtil.extractUsername(token);
            System.out.println("Username: " + username);
        }

        // 3. Si hay usuario y no está autenticado aún
        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 4. Validar token
            if(jwtUtil.validateToken(token, userDetails.getUsername())){

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 5. Autenticar usuario en el contexto
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 6. Continuar cadena de filtros
        filterChain.doFilter(request, response);

    }

}
