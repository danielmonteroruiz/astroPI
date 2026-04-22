package com.astropi.astropi.service;

import com.astropi.astropi.controller.dto.admin.PermisoResponse;
import com.astropi.astropi.controller.dto.admin.RolResponse;
import com.astropi.astropi.model.Permiso;
import com.astropi.astropi.model.Rol;
import com.astropi.astropi.repository.PermisoRepository;
import com.astropi.astropi.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

/**
 * Logica de negocio para permisos granulares.
 */
@Service
public class PermisoService {

    @Autowired
    private PermisoRepository permisoRepository;

    @Autowired
    private RolRepository rolRepository;

    @Transactional(readOnly = true)
    public List<PermisoResponse> obtenerPermisos() {
        return permisoRepository.findAll().stream()
                .sorted(Comparator.comparing(Permiso::getId))
                .map(this::mapToPermisoResponse)
                .toList();
    }

    @Transactional
    public PermisoResponse crearPermiso(String nombre, String descripcion) {

        permisoRepository.findByNombre(nombre)
                .ifPresent(permiso -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un permiso con ese nombre");
                });

        Permiso permiso = new Permiso();
        permiso.setNombre(nombre);
        permiso.setDescripcion(normalizarTextoOpcional(descripcion));

        Permiso permisoGuardado = permisoRepository.save(permiso);

        return mapToPermisoResponse(permisoGuardado);
    }

    @Transactional
    public PermisoResponse actualizarPermiso(Long permisoId, String nombre, String descripcion) {

        Permiso permiso = permisoRepository.findById(permisoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Permiso no encontrado"));

        permisoRepository.findByNombre(nombre)
                .filter(permisoExistente -> !permisoExistente.getId().equals(permisoId))
                .ifPresent(permisoExistente -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un permiso con ese nombre");
                });

        permiso.setNombre(nombre);
        permiso.setDescripcion(normalizarTextoOpcional(descripcion));

        return mapToPermisoResponse(permisoRepository.save(permiso));
    }

    @Transactional
    public void eliminarPermiso(Long permisoId) {

        Permiso permiso = permisoRepository.findById(permisoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Permiso no encontrado"));

        List<Rol> rolesConPermiso = rolRepository.findAll().stream()
                .filter(rol -> rol.getPermisos().contains(permiso))
                .toList();

        if (!rolesConPermiso.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar un permiso asignado a roles");
        }

        permisoRepository.delete(permiso);
    }

    @Transactional
    public RolResponse asignarPermisoARol(Long rolId, Long permisoId) {

        Rol rol = obtenerRol(rolId);
        Permiso permiso = obtenerPermiso(permisoId);

        rol.getPermisos().add(permiso);
        Rol rolActualizado = rolRepository.save(rol);

        return mapToRolResponse(rolActualizado);
    }

    @Transactional
    public RolResponse quitarPermisoARol(Long rolId, Long permisoId) {

        Rol rol = obtenerRol(rolId);
        Permiso permiso = obtenerPermiso(permisoId);

        rol.getPermisos().remove(permiso);
        Rol rolActualizado = rolRepository.save(rol);

        return mapToRolResponse(rolActualizado);
    }

    private Rol obtenerRol(Long rolId) {
        return rolRepository.findById(rolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado"));
    }

    private Permiso obtenerPermiso(Long permisoId) {
        return permisoRepository.findById(permisoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Permiso no encontrado"));
    }

    private PermisoResponse mapToPermisoResponse(Permiso permiso) {

        PermisoResponse response = new PermisoResponse();
        response.setId(permiso.getId());
        response.setNombre(permiso.getNombre());
        response.setDescripcion(permiso.getDescripcion());

        return response;
    }

    private RolResponse mapToRolResponse(Rol rol) {

        RolResponse response = new RolResponse();
        response.setId(rol.getId());
        response.setNombre(rol.getNombre());
        response.setPermisos(obtenerNombresPermisos(rol));

        return response;
    }

    private List<String> obtenerNombresPermisos(Rol rol) {
        return rol.getPermisos().stream()
                .map(Permiso::getNombre)
                .sorted()
                .toList();
    }

    private String normalizarTextoOpcional(String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor;
    }
}
