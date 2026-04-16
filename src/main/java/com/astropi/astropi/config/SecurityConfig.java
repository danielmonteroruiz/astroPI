package com.astropi.astropi.config;

import com.astropi.astropi.security.JwtAuthenticationEntryPoint;
import com.astropi.astropi.security.JwtAuthenticationFilter;
import com.astropi.astropi.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
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
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

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
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                //Config. de autorizacion endpoints
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll() //acceso pblico
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/admin/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/user/**").hasAnyRole("USER","SUPER_ADMIN")
                        .requestMatchers("/incidencias/**").authenticated()
                        .requestMatchers("/peticiones/**").authenticated()
                        .requestMatchers("/grupos/**").authenticated()
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
    public AuthenticationManager authenticationManager(CustomUserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(provider);
    }



}
