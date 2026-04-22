package com.astropi.astropi.controller;

import com.astropi.astropi.controller.dto.common.AsignarTicketRequest;
import com.astropi.astropi.controller.dto.common.ComentarioRequest;
import com.astropi.astropi.controller.dto.common.ComentarioResponse;
import com.astropi.astropi.controller.dto.common.PagedResponse;
import com.astropi.astropi.controller.dto.common.UsuarioAsignableResponse;
import com.astropi.astropi.controller.dto.incidencia.EstadoIncidenciaRequest;
import com.astropi.astropi.controller.dto.incidencia.IncidenciaRequest;
import com.astropi.astropi.model.EstadoIncidencia;
import com.astropi.astropi.model.Incidencia;
import com.astropi.astropi.service.IncidenciaService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.util.List;
import com.astropi.astropi.controller.dto.incidencia.IncidenciaResponse;
import com.astropi.astropi.controller.dto.common.MensajeResponse;

/**
 * Controlador de incidencias.
 */
@RestController
@RequestMapping("/incidencias")
public class IncidenciaController {
    @Autowired
    private IncidenciaService incidenciaService;

    @PostMapping
    public ResponseEntity<IncidenciaResponse> crearIncidencia(@Valid @RequestBody IncidenciaRequest request,
                                                              Authentication authentication){

        String username = authentication.getName();

        Incidencia incidencia = incidenciaService.crearIncidencia(
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
    public ResponseEntity<PagedResponse<IncidenciaResponse>> obtenerIncidencias(
            @RequestParam(required = false) EstadoIncidencia estado,
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
            Authentication authentication){

        String username = authentication.getName();

        PagedResponse<IncidenciaResponse> incidencias =
                incidenciaService.obtenerIncidenciasUsuarioYGrupo(
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

        return ResponseEntity.ok(incidencias);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<IncidenciaResponse> actualizarEstado(@PathVariable Long id,
                                                               @Valid @RequestBody EstadoIncidenciaRequest request,
                                                               Authentication authentication) {

        String username = authentication.getName();

        IncidenciaResponse incidencia = incidenciaService.actualizarEstado(
                id,
                request.getEstado(),
                username
        );

        return ResponseEntity.ok(incidencia);
    }

    @PutMapping("/{id}/asignacion")
    public ResponseEntity<IncidenciaResponse> asignarIncidencia(@PathVariable Long id,
                                                                @Valid @RequestBody AsignarTicketRequest request,
                                                                Authentication authentication) {

        String username = authentication.getName();

        IncidenciaResponse incidencia = incidenciaService.asignarIncidencia(
                id,
                request.getUsernameAsignado(),
                username
        );

        return ResponseEntity.ok(incidencia);
    }

    @GetMapping("/asignables")
    public ResponseEntity<List<UsuarioAsignableResponse>> obtenerUsuariosAsignables(Authentication authentication) {
        return ResponseEntity.ok(incidenciaService.obtenerUsuariosAsignables(authentication.getName()));
    }

    @PostMapping("/{id}/comentarios")
    public ResponseEntity<ComentarioResponse> agregarComentario(@PathVariable Long id,
                                                                @Valid @RequestBody ComentarioRequest request,
                                                                Authentication authentication) {

        ComentarioResponse comentario = incidenciaService.agregarComentario(
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

        ComentarioResponse comentario = incidenciaService.actualizarComentario(
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

        incidenciaService.eliminarComentario(id, comentarioId, authentication.getName());
        return ResponseEntity.ok(new MensajeResponse("Comentario borrado correctamente"));
    }

}
