package com.astropi.astropi.service;

import com.astropi.astropi.model.Rol;
import com.astropi.astropi.model.Usuario;
import com.astropi.astropi.repository.GrupoRepository;
import com.astropi.astropi.repository.IncidenciaRepository;
import com.astropi.astropi.repository.PeticionRepository;
import com.astropi.astropi.repository.RolRepository;
import com.astropi.astropi.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private IncidenciaRepository incidenciaRepository;

    @Mock
    private PeticionRepository peticionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void noDeberiaDesactivarSuPropioUsuario() {

        // Comprueba que un administrador no pueda bloquear su propia cuenta por error.
        Usuario admin = crearUsuario("admin", "SUPER_ADMIN", true);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> usuarioService.actualizarActivo(1L, false, "admin"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No puedes desactivar tu propio usuario");
    }

    @Test
    void noDeberiaQuitarseSuPropioRolSuperAdmin() {

        // Comprueba que un SUPER_ADMIN no pueda rebajarse a USER a si mismo.
        Usuario admin = crearUsuario("admin", "SUPER_ADMIN", true);
        Rol rolUser = crearRol("USER");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(rolRepository.findById(2L)).thenReturn(Optional.of(rolUser));

        assertThatThrownBy(() -> usuarioService.asignarRol(1L, 2L, "admin"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No puedes quitarte tu propio rol SUPER_ADMIN");
    }

    private Usuario crearUsuario(String username, String nombreRol, boolean activo) {

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setRol(crearRol(nombreRol));
        usuario.setActivo(activo);

        return usuario;
    }

    private Rol crearRol(String nombre) {

        Rol rol = new Rol();
        rol.setNombre(nombre);

        return rol;
    }
}
