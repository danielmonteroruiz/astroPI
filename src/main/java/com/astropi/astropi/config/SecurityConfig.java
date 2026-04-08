package com.astropi.astropi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


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
     * Configuración principal de seguridad HTTP.
     *
     * @param http objeto de configuración de Spring Security
     * @return cadena de filtros de seguridad
     */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                //Desactiva proteccion CSRF (para pruebas en postman)
                .csrf(csrf -> csrf.disable())

                //Config. de autorizacion endpoints
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/register").permitAll() //acceso publico
                        .anyRequest().authenticated() // el resto requiere autenticacion
                )
                // Configura autenticación básica (usuario/contraseña)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    /**
     * Bean encargado de encriptar las contraseñas usando BCrypt.
     *
     * Spring lo utiliza automáticamente al validar credenciales.
     */

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    /**
     * Proporciona el AuthenticationManager necesario para gestionar la autenticación.
     *
     * Se encarga de coordinar el proceso de login usando UserDetailsService.
     */
    @Bean
    public AuthenticationManager authenticationManager (AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }

}
