package com.astropi.astropi.controller;

import com.astropi.astropi.controller.dto.peticion.EstadoPeticionRequest;
import com.astropi.astropi.controller.dto.peticion.PeticionRequest;
import com.astropi.astropi.controller.dto.peticion.PeticionResponse;
import com.astropi.astropi.model.EstadoPeticion;
import com.astropi.astropi.model.Peticion;
import com.astropi.astropi.service.PeticionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de peticiones.
 */
@RestController
@RequestMapping("/peticiones")
public class PeticionController {

    @Autowired
    private PeticionService peticionService;

    @PostMapping
    public ResponseEntity<PeticionResponse> crearPeticion(@Valid @RequestBody PeticionRequest request,
                                                          Authentication authentication) {

        String username = authentication.getName();

        Peticion peticion = peticionService.crearPeticion(
                request.getTitulo(),
                request.getDescripcion(),
                request.getServicio(),
                request.getCategoria(),
                request.getGrupoId(),
                username
        );

        PeticionResponse response = peticionService.mapToResponse(peticion);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/mis-peticiones")
    public ResponseEntity<List<PeticionResponse>> obtenerMisPeticiones(Authentication authentication) {

        String username = authentication.getName();

        List<PeticionResponse> peticiones = peticionService.obtenerMisPeticiones(username);

        return ResponseEntity.ok(peticiones);
    }

    @GetMapping
    public ResponseEntity<List<PeticionResponse>> obtenerPeticiones(
            @RequestParam(required = false) EstadoPeticion estado,
            @RequestParam(required = false) String servicio,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Long grupoId,
            Authentication authentication) {

        String username = authentication.getName();

        List<PeticionResponse> peticiones =
                peticionService.obtenerPeticionesUsuarioYGrupo(
                        username,
                        estado,
                        servicio,
                        categoria,
                        grupoId
                );

        return ResponseEntity.ok(peticiones);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<PeticionResponse> actualizarEstado(@PathVariable Long id,
                                                             @Valid @RequestBody EstadoPeticionRequest request,
                                                             Authentication authentication) {

        String username = authentication.getName();

        PeticionResponse peticion = peticionService.actualizarEstado(
                id,
                request.getEstado(),
                username
        );

        return ResponseEntity.ok(peticion);
    }
}
