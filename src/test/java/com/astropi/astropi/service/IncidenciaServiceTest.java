package com.astropi.astropi.service;

import com.astropi.astropi.model.EstadoIncidencia;
import com.astropi.astropi.model.Incidencia;
import com.astropi.astropi.model.Usuario;
import com.astropi.astropi.repository.GrupoRepository;
import com.astropi.astropi.repository.IncidenciaRepository;
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
class IncidenciaServiceTest {

    @Mock
    private IncidenciaRepository incidenciaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private GrupoRepository grupoRepository;

    @InjectMocks
    private IncidenciaService incidenciaService;

    @Test
    void noDeberiaReabrirIncidenciaCerrada() {

        // Comprueba que una incidencia cerrada queda bloqueada y no puede volver a ABIERTA.
        Usuario usuario = crearUsuario("usuario1");
        Incidencia incidencia = new Incidencia("Titulo", "Descripcion", usuario);
        incidencia.setEstado(EstadoIncidencia.CERRADA);

        when(usuarioRepository.findByUsername("usuario1")).thenReturn(Optional.of(usuario));
        when(incidenciaRepository.findById(1L)).thenReturn(Optional.of(incidencia));

        assertThatThrownBy(() -> incidenciaService.actualizarEstado(1L, EstadoIncidencia.ABIERTA, "usuario1"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Una incidencia cerrada no puede reabrirse");
    }

    @Test
    void noDeberiaAceptarRangoDeFechasInvalido() {

        // Comprueba que el filtro rechaza una fechaDesde posterior a fechaHasta.
        Usuario usuario = crearUsuario("usuario1");

        when(usuarioRepository.findByUsername("usuario1")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> incidenciaService.obtenerIncidenciasUsuarioYGrupo(
                "usuario1",
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

        assertThatThrownBy(() -> incidenciaService.obtenerIncidenciasUsuarioYGrupo(
                "usuario1",
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

        assertThatThrownBy(() -> incidenciaService.obtenerIncidenciasUsuarioYGrupo(
                "usuario1",
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

    private Usuario crearUsuario(String username) {

        Usuario usuario = new Usuario();
        usuario.setUsername(username);

        return usuario;
    }
}
