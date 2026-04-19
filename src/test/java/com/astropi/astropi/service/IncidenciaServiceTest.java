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

        Usuario usuario = crearUsuario("usuario1");
        Incidencia incidencia = new Incidencia("Titulo", "Descripcion", usuario);
        incidencia.setEstado(EstadoIncidencia.CERRADA);

        when(usuarioRepository.findByUsername("usuario1")).thenReturn(Optional.of(usuario));
        when(incidenciaRepository.findById(1L)).thenReturn(Optional.of(incidencia));

        assertThatThrownBy(() -> incidenciaService.actualizarEstado(1L, EstadoIncidencia.ABIERTA, "usuario1"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Una incidencia cerrada no puede reabrirse");
    }

    private Usuario crearUsuario(String username) {

        Usuario usuario = new Usuario();
        usuario.setUsername(username);

        return usuario;
    }
}
