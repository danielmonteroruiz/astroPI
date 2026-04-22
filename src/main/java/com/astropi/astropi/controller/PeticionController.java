package com.astropi.astropi.controller;

import com.astropi.astropi.controller.dto.common.AsignarTicketRequest;
import com.astropi.astropi.controller.dto.common.ComentarioRequest;
import com.astropi.astropi.controller.dto.common.ComentarioResponse;
import com.astropi.astropi.controller.dto.common.PagedResponse;
import com.astropi.astropi.controller.dto.common.UsuarioAsignableResponse;
import com.astropi.astropi.controller.dto.peticion.EstadoPeticionRequest;
import com.astropi.astropi.controller.dto.peticion.PeticionRequest;
import com.astropi.astropi.controller.dto.peticion.PeticionResponse;
import com.astropi.astropi.model.EstadoPeticion;
import com.astropi.astropi.model.Peticion;
import com.astropi.astropi.service.PeticionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import com.astropi.astropi.controller.dto.common.MensajeResponse;

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
    public ResponseEntity<PagedResponse<PeticionResponse>> obtenerPeticiones(
            @RequestParam(required = false) EstadoPeticion estado,
            @RequestParam(required = false) String servicio,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String codigoTicket,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String categoriaTexto,
            @RequestParam(required = false) Long grupoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        String username = authentication.getName();

        PagedResponse<PeticionResponse> peticiones =
                peticionService.obtenerPeticionesUsuarioYGrupo(
                        username,
                        estado,
                        servicio,
                        categoria,
                        codigoTicket,
                        titulo,
                        categoriaTexto,
                        grupoId,
                        fechaDesde,
                        fechaHasta,
                        page,
                        size
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

    @PutMapping("/{id}/asignacion")
    public ResponseEntity<PeticionResponse> asignarPeticion(@PathVariable Long id,
                                                            @Valid @RequestBody AsignarTicketRequest request,
                                                            Authentication authentication) {

        PeticionResponse peticion = peticionService.asignarPeticion(
                id,
                request.getUsernameAsignado(),
                authentication.getName()
        );

        return ResponseEntity.ok(peticion);
    }

    @GetMapping("/asignables")
    public ResponseEntity<List<UsuarioAsignableResponse>> obtenerUsuariosAsignables(Authentication authentication) {
        return ResponseEntity.ok(peticionService.obtenerUsuariosAsignables(authentication.getName()));
    }

    @PostMapping("/{id}/comentarios")
    public ResponseEntity<ComentarioResponse> agregarComentario(@PathVariable Long id,
                                                                @Valid @RequestBody ComentarioRequest request,
                                                                Authentication authentication) {

        ComentarioResponse comentario = peticionService.agregarComentario(
                id,
                request.getContenido(),
                authentication.getName()
        );

        return ResponseEntity.ok(comentario);
    }

    @PutMapping("/{id}/comentarios/{comentarioId}")
    public ResponseEntity<ComentarioResponse> actualizarComentario(@PathVariable Long id,
                                                                   @PathVariable Long comentarioId,
                                                                   @Valid @RequestBody ComentarioRequest request,
                                                                   Authentication authentication) {

        ComentarioResponse comentario = peticionService.actualizarComentario(
                id,
                comentarioId,
                request.getContenido(),
                authentication.getName()
        );

        return ResponseEntity.ok(comentario);
    }

    @DeleteMapping("/{id}/comentarios/{comentarioId}")
    public ResponseEntity<MensajeResponse> eliminarComentario(@PathVariable Long id,
                                                              @PathVariable Long comentarioId,
                                                              Authentication authentication) {

        peticionService.eliminarComentario(id, comentarioId, authentication.getName());
        return ResponseEntity.ok(new MensajeResponse("Comentario borrado correctamente"));
    }
}
