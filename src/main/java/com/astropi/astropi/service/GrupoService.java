package com.astropi.astropi.service;

import com.astropi.astropi.controller.dto.grupo.GrupoResponse;
import com.astropi.astropi.model.Grupo;
import com.astropi.astropi.repository.GrupoRepository;
import com.astropi.astropi.repository.IncidenciaRepository;
import com.astropi.astropi.repository.PeticionRepository;
import com.astropi.astropi.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Logica de negocio de grupos.
 */
@Service
public class GrupoService {

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    @Autowired
    private PeticionRepository peticionRepository;

    public List<GrupoResponse> obtenerGrupos() {
        return grupoRepository.findAll().stream()
                .sorted((grupo1, grupo2) -> grupo1.getId().compareTo(grupo2.getId()))
                .map(this::mapToResponse)
                .toList();
    }

    public GrupoResponse crearGrupo(String nombre) {

        grupoRepository.findByNombre(nombre)
                .ifPresent(grupo -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un grupo con ese nombre");
                });

        Grupo grupo = new Grupo();
        grupo.setNombre(nombre);

        Grupo grupoGuardado = grupoRepository.save(grupo);

        return mapToResponse(grupoGuardado);
    }

    public GrupoResponse actualizarGrupo(Long grupoId, String nombre) {

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grupo no encontrado"));

        grupoRepository.findByNombre(nombre)
                .filter(grupoExistente -> !grupoExistente.getId().equals(grupoId))
                .ifPresent(grupoExistente -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un grupo con ese nombre");
                });

        grupo.setNombre(nombre);
        Grupo grupoActualizado = grupoRepository.save(grupo);

        return mapToResponse(grupoActualizado);
    }

    public void eliminarGrupo(Long grupoId) {

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grupo no encontrado"));

        if (usuarioRepository.existsByGrupoId(grupoId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar un grupo con usuarios asociados");
        }

        if (incidenciaRepository.existsByGrupoId(grupoId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar un grupo con incidencias asociadas");
        }

        if (peticionRepository.existsByGrupoId(grupoId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar un grupo con peticiones asociadas");
        }

        grupoRepository.delete(grupo);
    }

    private GrupoResponse mapToResponse(Grupo grupo) {
        return new GrupoResponse(
                grupo.getId(),
                grupo.getNombre()
        );
    }
}
