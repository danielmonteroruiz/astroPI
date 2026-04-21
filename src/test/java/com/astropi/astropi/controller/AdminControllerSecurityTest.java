package com.astropi.astropi.controller;

import com.astropi.astropi.config.CorsProperties;
import com.astropi.astropi.config.SecurityConfig;
import com.astropi.astropi.controller.dto.admin.PermisoResponse;
import com.astropi.astropi.controller.dto.grupo.GrupoResponse;
import com.astropi.astropi.security.CustomUserDetailsService;
import com.astropi.astropi.security.JwtAuthenticationEntryPoint;
import com.astropi.astropi.security.JwtAuthenticationFilter;
import com.astropi.astropi.security.JwtUtil;
import com.astropi.astropi.service.GrupoService;
import com.astropi.astropi.service.PermisoService;
import com.astropi.astropi.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class})
class AdminControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GrupoService grupoService;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private PermisoService permisoService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private CorsProperties corsProperties;

    @BeforeEach
    void setUp() {
        when(corsProperties.getAllowedOrigins()).thenReturn(List.of("http://localhost:3000"));
    }

    @Test
    void deberiaBloquearAccesoSinAutenticacionAAdminPermisos() throws Exception {

        // Comprueba que /admin/permisos devuelve 401 si no hay usuario autenticado.
        mockMvc.perform(get("/admin/permisos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("No autorizado"));
    }

    @Test
    void deberiaBloquearAccesoSinPermisoGestionarPermisos() throws Exception {

        // Comprueba que un usuario normal recibe 403 si intenta entrar en /admin/permisos.
        mockMvc.perform(get("/admin/permisos")
                        .with(user("usuario1").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deberiaPermitirAccesoConPermisoGestionarPermisos() throws Exception {

        // Comprueba que un usuario con GESTIONAR_PERMISOS puede listar permisos.
        when(permisoService.obtenerPermisos()).thenReturn(List.of(crearPermisoResponse()));

        mockMvc.perform(get("/admin/permisos")
                        .with(user("gestor").authorities(() -> "GESTIONAR_PERMISOS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("GESTIONAR_USUARIOS"));
    }

    @Test
    void deberiaBloquearAccesoSinPermisoGestionarUsuarios() throws Exception {

        // Comprueba que un usuario normal recibe 403 si intenta entrar en /admin/usuarios.
        mockMvc.perform(get("/admin/usuarios")
                        .with(user("usuario1").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deberiaPermitirAccesoConPermisoGestionarUsuarios() throws Exception {

        // Comprueba que un usuario con GESTIONAR_USUARIOS puede acceder al listado de usuarios.
        when(usuarioService.obtenerUsuariosAdmin()).thenReturn(List.of());

        mockMvc.perform(get("/admin/usuarios")
                        .with(user("gestor").authorities(() -> "GESTIONAR_USUARIOS")))
                .andExpect(status().isOk());
    }

    @Test
    void deberiaBloquearAccesoSinPermisoGestionarGrupos() throws Exception {

        // Comprueba que un usuario normal recibe 403 si intenta crear grupos en admin.
        mockMvc.perform(post("/admin/grupos")
                        .with(user("usuario1").roles("USER"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Desarrollo"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void deberiaPermitirAccesoConPermisoGestionarGrupos() throws Exception {

        // Comprueba que un usuario con GESTIONAR_GRUPOS puede crear grupos en admin.
        when(grupoService.crearGrupo("Desarrollo")).thenReturn(crearGrupoResponse());

        mockMvc.perform(post("/admin/grupos")
                        .with(user("gestor").authorities(() -> "GESTIONAR_GRUPOS"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Desarrollo"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Desarrollo"));
    }

    @Test
    void deberiaDevolverBadRequestSiFaltaNombreEnCrearPermiso() throws Exception {

        // Comprueba que crear un permiso sin nombre devuelve 400 con el error de validacion.
        mockMvc.perform(post("/admin/permisos")
                        .with(user("gestor").authorities(() -> "GESTIONAR_PERMISOS"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "descripcion": "Permite gestionar usuarios"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos invalidos"))
                .andExpect(jsonPath("$.campos.nombre").value("El nombre del permiso es obligatorio"));
    }

    @Test
    void deberiaDevolverBadRequestSiFaltaNombreEnCrearGrupo() throws Exception {

        // Comprueba que crear un grupo sin nombre devuelve 400 con el error de validacion.
        mockMvc.perform(post("/admin/grupos")
                        .with(user("gestor").authorities(() -> "GESTIONAR_GRUPOS"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos invalidos"))
                .andExpect(jsonPath("$.campos.nombre").value("El nombre del grupo es obligatorio"));
    }

    @Test
    void deberiaDevolverBadRequestSiJsonTieneCampoNoPermitidoEnCrearPermiso() throws Exception {

        // Comprueba que crear un permiso con un campo desconocido devuelve 400 controlado.
        mockMvc.perform(post("/admin/permisos")
                        .with(user("gestor").authorities(() -> "GESTIONAR_PERMISOS"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "GESTIONAR_USUARIOS",
                                  "descripcion": "Permite gestionar usuarios",
                                  "activo": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("JSON invalido"))
                .andExpect(jsonPath("$.mensaje").value("Campo no permitido: activo"));
    }

    @Test
    void deberiaDevolverBadRequestSiFaltaUsernameEnActualizarUsuario() throws Exception {

        // Comprueba que editar un usuario sin username devuelve 400 con el error de validacion.
        mockMvc.perform(put("/admin/usuarios/5")
                        .with(user("gestor").authorities(() -> "GESTIONAR_USUARIOS"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Dani",
                                  "apellidos": "Montero",
                                  "email": "dani@astropi.com",
                                  "dni": "12345678A"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos invalidos"))
                .andExpect(jsonPath("$.campos.username").value("El username es obligatorio"));
    }

    @Test
    void deberiaDevolverBadRequestSiEmailNoEsValidoEnActualizarUsuario() throws Exception {

        // Comprueba que editar un usuario con email invalido devuelve 400 controlado.
        mockMvc.perform(put("/admin/usuarios/5")
                        .with(user("gestor").authorities(() -> "GESTIONAR_USUARIOS"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "usuario1",
                                  "nombre": "Dani",
                                  "apellidos": "Montero",
                                  "email": "email-invalido",
                                  "dni": "12345678A"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos invalidos"))
                .andExpect(jsonPath("$.campos.email").value("El email debe tener un formato valido"));
    }

    @Test
    void deberiaDevolverBadRequestSiJsonTieneCampoNoPermitidoEnActualizarUsuario() throws Exception {

        // Comprueba que editar un usuario con un campo desconocido devuelve 400 controlado.
        mockMvc.perform(put("/admin/usuarios/5")
                        .with(user("gestor").authorities(() -> "GESTIONAR_USUARIOS"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "usuario1",
                                  "nombre": "Dani",
                                  "apellidos": "Montero",
                                  "email": "dani@astropi.com",
                                  "dni": "12345678A",
                                  "activo": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("JSON invalido"))
                .andExpect(jsonPath("$.mensaje").value("Campo no permitido: activo"));
    }

    @Test
    void deberiaDevolverBadRequestSiPasswordEsMuyCorta() throws Exception {

        // Comprueba que cambiar la password con menos de 6 caracteres devuelve 400.
        mockMvc.perform(put("/admin/usuarios/5/password")
                        .with(user("gestor").authorities(() -> "GESTIONAR_USUARIOS"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos invalidos"))
                .andExpect(jsonPath("$.campos.password").value("La password debe tener entre 6 y 100 caracteres"));
    }

    @Test
    void deberiaDevolverBadRequestSiJsonTieneCampoNoPermitidoEnCambiarPassword() throws Exception {

        // Comprueba que cambiar la password con un campo desconocido devuelve 400 controlado.
        mockMvc.perform(put("/admin/usuarios/5/password")
                        .with(user("gestor").authorities(() -> "GESTIONAR_USUARIOS"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "nuevaPassword123",
                                  "activo": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("JSON invalido"))
                .andExpect(jsonPath("$.mensaje").value("Campo no permitido: activo"));
    }

    private PermisoResponse crearPermisoResponse() {

        PermisoResponse response = new PermisoResponse();
        response.setId(1L);
        response.setNombre("GESTIONAR_USUARIOS");
        response.setDescripcion("Permite gestionar usuarios");

        return response;
    }

    private GrupoResponse crearGrupoResponse() {
        return new GrupoResponse(1L, "Desarrollo");
    }
}
