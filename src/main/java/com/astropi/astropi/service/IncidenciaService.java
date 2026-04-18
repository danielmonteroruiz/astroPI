package com.astropi.astropi.service;

import com.astropi.astropi.model.Grupo;
import com.astropi.astropi.model.EstadoIncidencia;
import com.astropi.astropi.model.Incidencia;
import com.astropi.astropi.model.Usuario;
import com.astropi.astropi.repository.GrupoRepository;
import com.astropi.astropi.repository.IncidenciaRepository;
import com.astropi.astropi.repository.UsuarioRepository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.astropi.astropi.controller.dto.common.PagedResponse;
import com.astropi.astropi.controller.dto.incidencia.IncidenciaResponse;

/**
 * Lógica de negocio de incidencias.
 */

@Service
public class IncidenciaService {

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;


    @Autowired
    private GrupoRepository grupoRepository;

    public Incidencia crearIncidencia(String titulo, String descripcion,
                                      String servicio, String categoria,
                                      Long grupoId,
                                      String username){

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grupo no encontrado"));

        Incidencia incidencia = new Incidencia(titulo, descripcion, usuario);

        incidencia.setServicio(servicio);
        incidencia.setCategoria(categoria);
        incidencia.setGrupo(grupo);

        String codigo = generarCodigoTicket();
        incidencia.setCodigoTicket(codigo);

        return incidenciaRepository.save(incidencia);
    }
    private String generarCodigoTicket(){

        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int siguienteNumero = obtenerSiguienteNumeroTicket();
        String secuencia = String.format("%04d", siguienteNumero);

        return "I-" + fecha + "-" + secuencia;
    }

    private int obtenerSiguienteNumeroTicket() {
        return incidenciaRepository.findTopByOrderByIdDesc()
                .map(Incidencia::getCodigoTicket)
                .map(this::extraerNumeroTicket)
                .orElse(0) + 1;
    }

    private int extraerNumeroTicket(String codigoTicket) {

        if (codigoTicket == null || !codigoTicket.contains("-")) {
            return 0;
        }

        String[] partes = codigoTicket.split("-");
        String numero = partes[partes.length - 1];

        try {
            return Integer.parseInt(numero);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }


    public List<IncidenciaResponse> obtenerMisIncidencias(String username){
        List<Incidencia> incidencias = incidenciaRepository.findByUsuarioUsername(username);

        return incidencias.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public IncidenciaResponse mapToResponse(Incidencia incidencia){

        IncidenciaResponse response = new IncidenciaResponse();

        response.setId(incidencia.getId());
        response.setTitulo(incidencia.getTitulo());
        response.setDescripcion(incidencia.getDescripcion());
        response.setEstado(incidencia.getEstado().name());
        response.setFechaCreacion(incidencia.getFechaCreacion());
        response.setCodigoTicket(incidencia.getCodigoTicket());
        response.setServicio(incidencia.getServicio());
        response.setCategoria(incidencia.getCategoria());

        if (incidencia.getUsuario() != null){
            response.setUsuario(incidencia.getUsuario().getUsername());
        }

        if (incidencia.getGrupo() != null){
            response.setGrupo(incidencia.getGrupo().getNombre());
        }

        return response;
    }


    public PagedResponse<IncidenciaResponse> obtenerIncidenciasUsuarioYGrupo(String username,
                                                                             EstadoIncidencia estado,
                                                                             String servicio,
                                                                             String categoria,
                                                                             Long grupoId,
                                                                             LocalDate fechaDesde,
                                                                             LocalDate fechaHasta,
                                                                             int page,
                                                                             int size){

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        validarRangoFechas(fechaDesde, fechaHasta);
        validarPaginacion(page, size);

        Specification<Incidencia> filtros = crearFiltrosIncidencias(
                usuario,
                estado,
                servicio,
                categoria,
                grupoId,
                fechaDesde,
                fechaHasta
        );

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "fechaCreacion")
        );

        Page<Incidencia> incidencias = incidenciaRepository.findAll(
                filtros,
                pageable
        );

        List<IncidenciaResponse> content = incidencias.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return new PagedResponse<>(
                content,
                incidencias.getNumber(),
                incidencias.getSize(),
                incidencias.getTotalElements(),
                incidencias.getTotalPages()
        );
    }

    private Specification<Incidencia> crearFiltrosIncidencias(Usuario usuario,
                                                              EstadoIncidencia estado,
                                                              String servicio,
                                                              String categoria,
                                                              Long grupoId,
                                                              LocalDate fechaDesde,
                                                              LocalDate fechaHasta) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            Predicate esCreador = criteriaBuilder.equal(root.get("usuario").get("username"), usuario.getUsername());

            if (usuario.getGrupo() != null) {
                Predicate mismoGrupo = criteriaBuilder.equal(root.get("grupo").get("id"), usuario.getGrupo().getId());
                predicates.add(criteriaBuilder.or(esCreador, mismoGrupo));
            } else {
                predicates.add(esCreador);
            }

            if (estado != null) {
                predicates.add(criteriaBuilder.equal(root.get("estado"), estado));
            }

            if (tieneTexto(servicio)) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("servicio")),
                        servicio.toLowerCase()
                ));
            }

            if (tieneTexto(categoria)) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("categoria")),
                        categoria.toLowerCase()
                ));
            }

            if (grupoId != null) {
                predicates.add(criteriaBuilder.equal(root.get("grupo").get("id"), grupoId));
            }

            if (fechaDesde != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("fechaCreacion"),
                        fechaDesde.atStartOfDay()
                ));
            }

            if (fechaHasta != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("fechaCreacion"),
                        fechaHasta.atTime(23, 59, 59)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void validarRangoFechas(LocalDate fechaDesde, LocalDate fechaHasta) {

        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fechaDesde no puede ser posterior a fechaHasta");
        }
    }

    private void validarPaginacion(int page, int size) {

        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page no puede ser negativo");
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size debe estar entre 1 y 100");
        }
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    public IncidenciaResponse actualizarEstado(Long incidenciaId, EstadoIncidencia nuevoEstado, String username) {

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Incidencia incidencia = incidenciaRepository.findById(incidenciaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incidencia no encontrada"));

        if (!puedeGestionarIncidencia(usuario, incidencia)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes modificar esta incidencia");
        }

        validarCambioEstado(incidencia.getEstado(), nuevoEstado);
        incidencia.setEstado(nuevoEstado);

        Incidencia incidenciaActualizada = incidenciaRepository.save(incidencia);

        return mapToResponse(incidenciaActualizada);
    }

    private boolean puedeGestionarIncidencia(Usuario usuario, Incidencia incidencia) {

        boolean esCreador = incidencia.getUsuario() != null
                && incidencia.getUsuario().getUsername().equals(usuario.getUsername());

        boolean mismoGrupo = usuario.getGrupo() != null
                && incidencia.getGrupo() != null
                && incidencia.getGrupo().getId().equals(usuario.getGrupo().getId());

        return esCreador || mismoGrupo;
    }

    private void validarCambioEstado(EstadoIncidencia estadoActual, EstadoIncidencia nuevoEstado) {

        if (estadoActual == EstadoIncidencia.CERRADA && nuevoEstado != EstadoIncidencia.CERRADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Una incidencia cerrada no puede reabrirse");
        }
    }



}
