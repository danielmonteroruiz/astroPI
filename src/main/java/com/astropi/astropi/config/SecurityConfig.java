package com.astropi.astropi.config;

import com.astropi.astropi.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * Clase de configuración de seguridad de la aplicación.
 *
 * Define:
 * - Qué endpoints están protegidos o abiertos
 * - Tipo de autenticación (Basic Auth en este caso)
 * - Configuración de encriptación de contraseñas
 */


@Configuration
public class SecurityConfig {


    /**
     * Bean encargado de encriptar las contraseñas usando BCrypt
     */

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configuración principal de seguridad HTTP.
     */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                //Desactiva proteccion CSRF (para pruebas en postman)
                .csrf(csrf -> csrf.disable())

                //Config. de autorizacion endpoints
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll() //acceso pblico
                        .requestMatchers("/admin/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/user/**").hasAnyRole("USER","SUPER_ADMIN")
                        .anyRequest().authenticated() // el resto requiere autenticacion
                )
                // Configura autenticación básica (usuario/contraseña) temporal para pruebas
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }



    /**
     * Proporciona el AuthenticationManager necesario para gestionar la autenticación manual.
     *
     * Se encarga de coordinar el proceso de login usando UserDetailsService.
     */
    @Bean
    public AuthenticationManager authenticationManager (AuthenticationConfiguration authConfig) throws Exception{
        return authConfig.getAuthenticationManager();
    }



}
