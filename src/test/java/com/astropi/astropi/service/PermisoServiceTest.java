package com.astropi.astropi.service;

import com.astropi.astropi.controller.dto.admin.PermisoResponse;
import com.astropi.astropi.controller.dto.admin.RolResponse;
import com.astropi.astropi.model.Permiso;
import com.astropi.astropi.model.Rol;
import com.astropi.astropi.repository.PermisoRepository;
import com.astropi.astropi.repository.RolRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermisoServiceTest {

    @Mock
    private PermisoRepository permisoRepository;

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private PermisoService permisoService;

    @Test
    void noDeberiaCrearPermisoDuplicado() {

        // Comprueba que no se puedan crear dos permisos con el mismo nombre.
        Permiso permiso = crearPermiso(1L, "GESTIONAR_USUARIOS");
        when(permisoRepository.findByNombre("GESTIONAR_USUARIOS")).thenReturn(Optional.of(permiso));

        assertThatThrownBy(() -> permisoService.crearPermiso("GESTIONAR_USUARIOS", "Duplicado"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Ya existe un permiso con ese nombre");
    }

    @Test
    void noDeberiaEliminarPermisoAsignadoARoles() {

        // Comprueba que no se borre un permiso mientras algun rol lo siga usando.
        Permiso permiso = crearPermiso(1L, "GESTIONAR_USUARIOS");
        Rol rol = crearRol(1L, "SUPER_ADMIN", permiso);

        when(permisoRepository.findById(1L)).thenReturn(Optional.of(permiso));
        when(rolRepository.findAll()).thenReturn(List.of(rol));

        assertThatThrownBy(() -> permisoService.eliminarPermiso(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No se puede eliminar un permiso asignado a roles");
    }

    @Test
    void deberiaAsignarPermisoARol() {

        // Comprueba que la asignacion devuelve el rol actualizado con el permiso nuevo.
        Rol rol = crearRol(1L, "SUPER_ADMIN");
        Permiso permiso = crearPermiso(2L, "GESTIONAR_PERMISOS");

        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(permisoRepository.findById(2L)).thenReturn(Optional.of(permiso));
        when(rolRepository.save(rol)).thenReturn(rol);

        RolResponse response = permisoService.asignarPermisoARol(1L, 2L);

        assertThat(response.getPermisos()).containsExactly("GESTIONAR_PERMISOS");
    }

    @Test
    void deberiaQuitarPermisoARol() {

        // Comprueba que quitar un permiso deja el rol sin ese permiso en la respuesta.
        Permiso permiso = crearPermiso(2L, "GESTIONAR_PERMISOS");
        Rol rol = crearRol(1L, "SUPER_ADMIN", permiso);

        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(permisoRepository.findById(2L)).thenReturn(Optional.of(permiso));
        when(rolRepository.save(rol)).thenReturn(rol);

        RolResponse response = permisoService.quitarPermisoARol(1L, 2L);

        assertThat(response.getPermisos()).isEmpty();
    }

    private Permiso crearPermiso(Long id, String nombre) {

        Permiso permiso = new Permiso();
        permiso.setId(id);
        permiso.setNombre(nombre);

        return permiso;
    }

    private Rol crearRol(Long id, String nombre, Permiso... permisos) {

        Rol rol = new Rol();
        rol.setId(id);
        rol.setNombre(nombre);
        rol.setPermisos(new LinkedHashSet<>(List.of(permisos)));

        return rol;
    }
}
