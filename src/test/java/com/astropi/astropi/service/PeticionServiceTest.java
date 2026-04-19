package com.astropi.astropi.service;

import com.astropi.astropi.model.EstadoPeticion;
import com.astropi.astropi.model.Peticion;
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

    private Usuario crearUsuario(String username) {

        Usuario usuario = new Usuario();
        usuario.setUsername(username);

        return usuario;
    }
}
