package com.astropi.astropi.service;

import com.astropi.astropi.controller.dto.GrupoResponse;
import com.astropi.astropi.model.Grupo;
import com.astropi.astropi.repository.GrupoRepository;
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

    public List<GrupoResponse> obtenerGrupos() {
        return grupoRepository.findAll().stream()
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

    private GrupoResponse mapToResponse(Grupo grupo) {
        return new GrupoResponse(
                grupo.getId(),
                grupo.getNombre()
        );
    }
}
