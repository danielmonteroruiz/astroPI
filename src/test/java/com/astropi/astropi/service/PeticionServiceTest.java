package com.astropi.astropi.service;

import com.astropi.astropi.model.EstadoPeticion;
import com.astropi.astropi.model.Grupo;
import com.astropi.astropi.model.Peticion;
import com.astropi.astropi.model.Rol;
import com.astropi.astropi.model.Usuario;
import com.astropi.astropi.repository.GrupoRepository;
import com.astropi.astropi.repository.PeticionRepository;
import com.astropi.astropi.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeticionServiceTest {

    @Mock
    private PeticionRepository peticionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private GrupoRepository grupoRepository;

    @InjectMocks
    private PeticionService peticionService;

    @Test
    void noDeberiaReabrirPeticionCerrada() {

        // Comprueba que una peticion cerrada queda bloqueada y no puede volver a ABIERTA.
        Usuario usuario = crearUsuario("usuario1");
        Peticion peticion = new Peticion("Titulo", "Descripcion", usuario);
        peticion.setEstado(EstadoPeticion.CERRADA);

        when(usuarioRepository.findByUsername("usuario1")).thenReturn(Optional.of(usuario));
        when(peticionRepository.findById(1L)).thenReturn(Optional.of(peticion));

        assertThatThrownBy(() -> peticionService.actualizarEstado(1L, EstadoPeticion.ABIERTA, "usuario1"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Una peticion cerrada no puede reabrirse");
    }

    @Test
    void noDeberiaAceptarRangoDeFechasInvalido() {

        // Comprueba que el filtro rechaza una fechaDesde posterior a fechaHasta.
        Usuario usuario = crearUsuario("usuario1");

        when(usuarioRepository.findByUsername("usuario1")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> peticionService.obtenerPeticionesUsuarioYGrupo(
                "usuario1",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDate.of(2026, 4, 22),
                LocalDate.of(2026, 4, 21),
                0,
                10
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("fechaDesde no puede ser posterior a fechaHasta");
    }

    @Test
    void noDeberiaAceptarPaginaNegativa() {

        // Comprueba que el filtro rechaza un numero de pagina negativo.
        Usuario usuario = crearUsuario("usuario1");

        when(usuarioRepository.findByUsername("usuario1")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> peticionService.obtenerPeticionesUsuarioYGrupo(
                "usuario1",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                -1,
                10
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("page no puede ser negativo");
    }

    @Test
    void noDeberiaAceptarTamanoDePaginaFueraDeRango() {

        // Comprueba que el filtro rechaza un size fuera del rango permitido.
        Usuario usuario = crearUsuario("usuario1");

        when(usuarioRepository.findByUsername("usuario1")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> peticionService.obtenerPeticionesUsuarioYGrupo(
                "usuario1",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                101
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("size debe estar entre 1 y 100");
    }

    @Test
    void usuarioDemoNoDeberiaCrearPeticiones() {

        Usuario usuario = crearUsuarioDemo();
        when(usuarioRepository.findByUsername("demo")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> peticionService.crearPeticion(
                "Titulo",
                "Descripcion",
                "Accesos",
                "Alta de permisos",
                1L,
                "demo"
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("El usuario demo solo puede consultar peticiones");
    }

    private Usuario crearUsuario(String username) {

        Usuario usuario = new Usuario();
        usuario.setUsername(username);

        return usuario;
    }

    private Usuario crearUsuarioDemo() {

        Rol rol = new Rol();
        rol.setNombre("DEMO_READ_ONLY");

        Grupo grupo = new Grupo();
        grupo.setId(1L);
        grupo.setNombre("Demo");

        Usuario usuario = crearUsuario("demo");
        usuario.setRol(rol);
        usuario.setGrupo(grupo);

        return usuario;
    }
}
