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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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

        LocalDateTime ahora = LocalDateTime.now();

        // Fecha en formato YYYYMMDD
        String fecha = ahora.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Inicio y fin del día
        LocalDateTime inicioDia = ahora.toLocalDate().atStartOfDay();
        LocalDateTime finDia = ahora.toLocalDate().atTime(23, 59, 59);

        // Contador del día
        long contador = incidenciaRepository
                .countByFechaCreacionBetween(inicioDia, finDia) + 1;

        // Formato 0001, 0002...
        String secuencia = String.format("%04d", contador);

        return "I-" + fecha + "-" + secuencia;
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


    public List<IncidenciaResponse> obtenerIncidenciasUsuarioYGrupo(String username,
                                                                    EstadoIncidencia estado,
                                                                    String servicio,
                                                                    String categoria,
                                                                    Long grupoId){

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Specification<Incidencia> filtros = crearFiltrosIncidencias(
                usuario,
                estado,
                servicio,
                categoria,
                grupoId
        );

        List<Incidencia> incidencias = incidenciaRepository.findAll(
                filtros,
                Sort.by(Sort.Direction.DESC, "fechaCreacion")
        );

        return incidencias.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private Specification<Incidencia> crearFiltrosIncidencias(Usuario usuario,
                                                              EstadoIncidencia estado,
                                                              String servicio,
                                                              String categoria,
                                                              Long grupoId) {

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

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
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
