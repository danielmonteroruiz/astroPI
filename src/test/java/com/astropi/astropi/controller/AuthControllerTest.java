package com.astropi.astropi.controller;

import com.astropi.astropi.config.CorsProperties;
import com.astropi.astropi.config.SecurityConfig;
import com.astropi.astropi.security.CustomUserDetailsService;
import com.astropi.astropi.security.JwtAuthenticationEntryPoint;
import com.astropi.astropi.security.JwtAuthenticationFilter;
import com.astropi.astropi.security.JwtUtil;
import com.astropi.astropi.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private CorsProperties corsProperties;

    @BeforeEach
    void setUp() {
        when(corsProperties.getAllowedOrigins()).thenReturn(List.of("http://localhost:3000"));
    }

    @Test
    void deberiaDevolverUnauthorizedSiLoginTieneCredencialesInvalidas() throws Exception {

        // Comprueba que el login devuelve 401 si las credenciales no son correctas.
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciales invalidas"));

        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "usuario1",
                                  "password": "passwordIncorrecta"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales invalidas"))
                .andExpect(jsonPath("$.mensaje").value("Username o password incorrectos"));
    }

    @Test
    void deberiaDevolverForbiddenSiLoginEsDeUsuarioDesactivado() throws Exception {

        // Comprueba que el login devuelve 403 si el usuario esta desactivado.
        when(authenticationManager.authenticate(any()))
                .thenThrow(new DisabledException("Usuario desactivado"));

        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "usuario1",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Usuario desactivado"))
                .andExpect(jsonPath("$.mensaje").value("El usuario no esta activo"));
    }

    @Test
    void deberiaDevolverUnauthorizedSiAuthMeNoTieneToken() throws Exception {

        // Comprueba que /auth/me devuelve 401 si no hay autenticacion.
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("No autorizado"));
    }

    @Test
    void deberiaPermitirAuthMeConUsuarioAutenticado() throws Exception {

        // Comprueba que /auth/me devuelve username y rol cuando el usuario esta autenticado.
        mockMvc.perform(get("/auth/me")
                        .with(user("usuario1").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("usuario1"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    void deberiaDevolverTokenSiLoginEsCorrecto() throws Exception {

        // Comprueba que el login devuelve un token cuando las credenciales son correctas.
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                User.withUsername("usuario1").password("password123").roles("USER").build(),
                null,
                User.withUsername("usuario1").password("password123").roles("USER").build().getAuthorities()
        );

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken("usuario1", "ROLE_USER")).thenReturn("token-prueba");

        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "usuario1",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-prueba"));
    }

    @Test
    void deberiaDevolverBadRequestSiFaltaUsernameEnLogin() throws Exception {

        // Comprueba que el login devuelve 400 si falta el username.
        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos invalidos"))
                .andExpect(jsonPath("$.campos.username").value("El username es obligatorio"));
    }

    @Test
    void deberiaDevolverBadRequestSiRegisterTieneEmailInvalido() throws Exception {

        // Comprueba que el registro devuelve 400 si el email no tiene formato valido.
        mockMvc.perform(post("/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "usuario1",
                                  "nombre": "Dani",
                                  "apellidos": "Montero",
                                  "email": "email-invalido",
                                  "dni": "12345678A",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos invalidos"))
                .andExpect(jsonPath("$.campos.email").value("El email debe tener un formato valido"));
    }

    @Test
    void deberiaDevolverBadRequestSiRegisterTieneCampoNoPermitido() throws Exception {

        // Comprueba que el registro devuelve 400 si llega un campo no permitido.
        mockMvc.perform(post("/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "usuario1",
                                  "nombre": "Dani",
                                  "apellidos": "Montero",
                                  "email": "dani@astropi.com",
                                  "dni": "12345678A",
                                  "password": "password123",
                                  "activo": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("JSON invalido"))
                .andExpect(jsonPath("$.mensaje").value("Campo no permitido: activo"));
    }
}
