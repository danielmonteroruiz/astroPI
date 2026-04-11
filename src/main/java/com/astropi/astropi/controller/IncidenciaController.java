package com.astropi.astropi.controller;


import com.astropi.astropi.controller.dto.IncidenciaRequest;
import com.astropi.astropi.model.Incidencia;
import com.astropi.astropi.service.IncidenciaService;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import java.util.List;
import com.astropi.astropi.controller.dto.IncidenciaResponse;

import java.util.Locale;

/**
 * Controlador de incidencias.
 */
@RestController
@RequestMapping("/incidencias")
public class IncidenciaController {
    @Autowired
    private IncidenciaService incidenciaService;

    @PostMapping
    public ResponseEntity<IncidenciaResponse> crearIncidencia(@RequestBody IncidenciaRequest request,
                                                              Authentication authentication){

        String username = authentication.getName();

        Incidencia incidencia = incidenciaService.CrearIncidencia(
                request.getTitulo(),
                request.getDescripcion(),
                request.getServicio(),
                request.getCategoria(),
                request.getGrupoId(),
                username
        );

        IncidenciaResponse response = incidenciaService.mapToResponse(incidencia);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/mis-incidencias")
    public ResponseEntity<List<IncidenciaResponse>> obtenerMisIncidencias(Authentication authentication) {

        String username = authentication.getName();

        List<IncidenciaResponse> incidencias = incidenciaService.obtenerMisIncidencias(username);

        return ResponseEntity.ok(incidencias);
    }

    @GetMapping
    public ResponseEntity<List<IncidenciaResponse>> obtenerIncidencias(Authentication authentication){

        String username = authentication.getName();

        List<IncidenciaResponse> incidencias =
                incidenciaService.obtenerIncidenciasUsuarioYGrupo(username);

        return ResponseEntity.ok(incidencias);
    }

}
