package com.astropi.astropi.service;

import com.astropi.astropi.model.Grupo;
import com.astropi.astropi.repository.GrupoRepository;
import com.astropi.astropi.repository.IncidenciaRepository;
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
class GrupoServiceTest {

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private IncidenciaRepository incidenciaRepository;

    @Mock
    private PeticionRepository peticionRepository;

    @InjectMocks
    private GrupoService grupoService;

    @Test
    void noDeberiaEliminarGrupoInexistente() {

        // Comprueba que no se pueda borrar un grupo si el id no existe.
        when(grupoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> grupoService.eliminarGrupo(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Grupo no encontrado");
    }

    @Test
    void noDeberiaEliminarGrupoConUsuariosAsociados() {

        // Comprueba que no se borre un grupo si aun tiene usuarios asignados.
        Grupo grupo = crearGrupo(2L, "Soporte");

        when(grupoRepository.findById(2L)).thenReturn(Optional.of(grupo));
        when(usuarioRepository.existsByGrupoId(2L)).thenReturn(true);

        assertThatThrownBy(() -> grupoService.eliminarGrupo(2L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No se puede eliminar un grupo con usuarios asociados");
    }

    @Test
    void noDeberiaEliminarGrupoConIncidenciasAsociadas() {

        // Comprueba que no se borre un grupo si aun tiene incidencias relacionadas.
        Grupo grupo = crearGrupo(3L, "Desarrollo");

        when(grupoRepository.findById(3L)).thenReturn(Optional.of(grupo));
        when(usuarioRepository.existsByGrupoId(3L)).thenReturn(false);
        when(incidenciaRepository.existsByGrupoId(3L)).thenReturn(true);

        assertThatThrownBy(() -> grupoService.eliminarGrupo(3L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No se puede eliminar un grupo con incidencias asociadas");
    }

    @Test
    void noDeberiaEliminarGrupoConPeticionesAsociadas() {

        // Comprueba que no se borre un grupo si aun tiene peticiones relacionadas.
        Grupo grupo = crearGrupo(4L, "Contabilidad");

        when(grupoRepository.findById(4L)).thenReturn(Optional.of(grupo));
        when(usuarioRepository.existsByGrupoId(4L)).thenReturn(false);
        when(incidenciaRepository.existsByGrupoId(4L)).thenReturn(false);
        when(peticionRepository.existsByGrupoId(4L)).thenReturn(true);

        assertThatThrownBy(() -> grupoService.eliminarGrupo(4L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No se puede eliminar un grupo con peticiones asociadas");
    }

    private Grupo crearGrupo(Long id, String nombre) {

        Grupo grupo = new Grupo();
        grupo.setId(id);
        grupo.setNombre(nombre);

        return grupo;
    }
}
