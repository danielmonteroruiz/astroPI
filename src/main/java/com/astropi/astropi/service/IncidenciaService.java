package com.astropi.astropi.service;

import com.astropi.astropi.controller.dto.common.ComentarioResponse;
import com.astropi.astropi.controller.dto.common.PagedResponse;
import com.astropi.astropi.controller.dto.common.UsuarioAsignableResponse;
import com.astropi.astropi.controller.dto.incidencia.IncidenciaResponse;
import com.astropi.astropi.model.ComentarioIncidencia;
import com.astropi.astropi.model.EstadoIncidencia;
import com.astropi.astropi.model.Grupo;
import com.astropi.astropi.model.Incidencia;
import com.astropi.astropi.model.Usuario;
import com.astropi.astropi.repository.ComentarioIncidenciaRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Logica de negocio de incidencias.
 */
@Service
@Transactional
public class IncidenciaService {

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private ComentarioIncidenciaRepository comentarioIncidenciaRepository;

    public Incidencia crearIncidencia(String titulo, String descripcion,
                                      String servicio, String categoria,
                                      Long grupoId,
                                      String username) {

        Usuario usuario = obtenerUsuarioPorUsername(username);
        Grupo grupo = obtenerGrupoPorId(grupoId);

        Incidencia incidencia = new Incidencia(titulo, descripcion, usuario);
        incidencia.setServicio(servicio);
        incidencia.setCategoria(categoria);
        incidencia.setGrupo(grupo);
        incidencia.setCodigoTicket(generarCodigoTicket());

        return incidenciaRepository.save(incidencia);
    }

    public List<IncidenciaResponse> obtenerMisIncidencias(String username) {
        List<Incidencia> incidencias = incidenciaRepository.findByUsuarioUsername(username);

        return incidencias.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<IncidenciaResponse> obtenerIncidenciasUsuarioYGrupo(String username,
                                                                             EstadoIncidencia estado,
                                                                             String servicio,
                                                                             String categoria,
                                                                             Long grupoId,
                                                                             LocalDate fechaDesde,
                                                                             LocalDate fechaHasta,
                                                                             int page,
                                                                             int size) {

        Usuario usuario = obtenerUsuarioPorUsername(username);

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

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaCreacion"));
        Page<Incidencia> incidencias = incidenciaRepository.findAll(filtros, pageable);

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

    public IncidenciaResponse actualizarEstado(Long incidenciaId, EstadoIncidencia nuevoEstado, String username) {
        Usuario usuario = obtenerUsuarioPorUsername(username);
        Incidencia incidencia = obtenerIncidenciaPorId(incidenciaId);

        validarGestionTicket(usuario, incidencia);
        validarCambioEstado(incidencia.getEstado(), nuevoEstado);

        incidencia.setEstado(nuevoEstado);
        return mapToResponse(incidenciaRepository.save(incidencia));
    }

    public IncidenciaResponse asignarIncidencia(Long incidenciaId, String usernameAsignado, String username) {
        Usuario usuario = obtenerUsuarioPorUsername(username);
        Incidencia incidencia = obtenerIncidenciaPorId(incidenciaId);

        validarGestionTicket(usuario, incidencia);

        if (usernameAsignado == null || usernameAsignado.isBlank()) {
            incidencia.setUsuarioAsignado(null);
            return mapToResponse(incidenciaRepository.save(incidencia));
        }

        Usuario usuarioAsignado = obtenerUsuarioPorUsername(usernameAsignado);
        validarUsuarioAsignable(incidencia, usuarioAsignado);

        incidencia.setUsuarioAsignado(usuarioAsignado);
        return mapToResponse(incidenciaRepository.save(incidencia));
    }

    public ComentarioResponse agregarComentario(Long incidenciaId, String contenido, String username) {
        Usuario usuario = obtenerUsuarioPorUsername(username);
        Incidencia incidencia = obtenerIncidenciaPorId(incidenciaId);

        validarGestionTicket(usuario, incidencia);

        ComentarioIncidencia comentario = new ComentarioIncidencia();
        comentario.setIncidencia(incidencia);
        comentario.setAutor(usuario);
        comentario.setContenido(contenido.trim());
        comentario.setFechaCreacion(LocalDateTime.now());

        ComentarioIncidencia comentarioGuardado = comentarioIncidenciaRepository.save(comentario);
        incidencia.getComentarios().add(comentarioGuardado);

        return mapComentarioResponse(comentarioGuardado);
    }

    public ComentarioResponse actualizarComentario(Long incidenciaId, Long comentarioId, String contenido, String username) {
        Usuario usuario = obtenerUsuarioPorUsername(username);
        Incidencia incidencia = obtenerIncidenciaPorId(incidenciaId);
        ComentarioIncidencia comentario = comentarioIncidenciaRepository.findById(comentarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentario no encontrado"));

        validarComentarioDeIncidencia(incidenciaId, comentario);
        validarAutorComentario(usuario, comentario.getAutor());
        validarGestionTicket(usuario, incidencia);

        comentario.setContenido(contenido.trim());

        return mapComentarioResponse(comentarioIncidenciaRepository.save(comentario));
    }

    public void eliminarComentario(Long incidenciaId, Long comentarioId, String username) {
        Usuario usuario = obtenerUsuarioPorUsername(username);
        Incidencia incidencia = obtenerIncidenciaPorId(incidenciaId);
        ComentarioIncidencia comentario = comentarioIncidenciaRepository.findById(comentarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentario no encontrado"));

        validarComentarioDeIncidencia(incidenciaId, comentario);
        validarAutorComentario(usuario, comentario.getAutor());
        validarGestionTicket(usuario, incidencia);

        comentarioIncidenciaRepository.delete(comentario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioAsignableResponse> obtenerUsuariosAsignables(String username) {
        Usuario usuario = obtenerUsuarioPorUsername(username);

        if (usuario.getGrupo() == null) {
            return List.of();
        }

        return usuarioRepository.findByGrupoIdAndActivoTrueOrderByUsernameAsc(usuario.getGrupo().getId()).stream()
                .map(this::mapUsuarioAsignable)
                .toList();
    }

    public IncidenciaResponse mapToResponse(Incidencia incidencia) {
        IncidenciaResponse response = new IncidenciaResponse();

        response.setId(incidencia.getId());
        response.setTitulo(incidencia.getTitulo());
        response.setDescripcion(incidencia.getDescripcion());
        response.setEstado(incidencia.getEstado().name());
        response.setFechaCreacion(incidencia.getFechaCreacion());
        response.setCodigoTicket(incidencia.getCodigoTicket());
        response.setServicio(incidencia.getServicio());
        response.setCategoria(incidencia.getCategoria());

        if (incidencia.getUsuario() != null) {
            response.setUsuario(incidencia.getUsuario().getUsername());
        }

        if (incidencia.getUsuarioAsignado() != null) {
            response.setUsuarioAsignado(incidencia.getUsuarioAsignado().getUsername());
        }

        if (incidencia.getGrupo() != null) {
            response.setGrupo(incidencia.getGrupo().getNombre());
        }

        response.setComentarios(
                incidencia.getComentarios().stream()
                        .map(this::mapComentarioResponse)
                        .toList()
        );

        return response;
    }

    private Usuario obtenerUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private Grupo obtenerGrupoPorId(Long grupoId) {
        return grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grupo no encontrado"));
    }

    private Incidencia obtenerIncidenciaPorId(Long incidenciaId) {
        return incidenciaRepository.findById(incidenciaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incidencia no encontrada"));
    }

    private String generarCodigoTicket() {
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

    private Specification<Incidencia> crearFiltrosIncidencias(Usuario usuario,
                                                              EstadoIncidencia estado,
                                                              String servicio,
                                                              String categoria,
                                                              Long grupoId,
                                                              LocalDate fechaDesde,
                                                              LocalDate fechaHasta) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!esSuperAdmin(usuario)) {
                Predicate esCreador = criteriaBuilder.equal(root.get("usuario").get("username"), usuario.getUsername());

                if (usuario.getGrupo() != null) {
                    Predicate mismoGrupo = criteriaBuilder.equal(root.get("grupo").get("id"), usuario.getGrupo().getId());
                    predicates.add(criteriaBuilder.or(esCreador, mismoGrupo));
                } else {
                    predicates.add(esCreador);
                }
            }

            if (estado != null) {
                predicates.add(criteriaBuilder.equal(root.get("estado"), estado));
            }

            if (tieneTexto(servicio)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("servicio")), servicio.toLowerCase()));
            }

            if (tieneTexto(categoria)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("categoria")), categoria.toLowerCase()));
            }

            if (grupoId != null) {
                predicates.add(criteriaBuilder.equal(root.get("grupo").get("id"), grupoId));
            }

            if (fechaDesde != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("fechaCreacion"), fechaDesde.atStartOfDay()));
            }

            if (fechaHasta != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("fechaCreacion"), fechaHasta.atTime(23, 59, 59)));
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

    private void validarGestionTicket(Usuario usuario, Incidencia incidencia) {
        if (!puedeGestionarIncidencia(usuario, incidencia)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes modificar esta incidencia");
        }
    }

    private boolean puedeGestionarIncidencia(Usuario usuario, Incidencia incidencia) {
        if (esSuperAdmin(usuario)) {
            return true;
        }

        boolean esCreador = incidencia.getUsuario() != null
                && incidencia.getUsuario().getUsername().equals(usuario.getUsername());

        boolean mismoGrupo = usuario.getGrupo() != null
                && incidencia.getGrupo() != null
                && incidencia.getGrupo().getId().equals(usuario.getGrupo().getId());

        return esCreador || mismoGrupo;
    }

    private void validarUsuarioAsignable(Incidencia incidencia, Usuario usuarioAsignado) {
        if (!Boolean.TRUE.equals(usuarioAsignado.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo puedes asignar incidencias a usuarios activos");
        }

        if (incidencia.getGrupo() == null || usuarioAsignado.getGrupo() == null
                || !incidencia.getGrupo().getId().equals(usuarioAsignado.getGrupo().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario asignado debe pertenecer al mismo grupo");
        }
    }

    private boolean esSuperAdmin(Usuario usuario) {
        return usuario.getRol() != null && "SUPER_ADMIN".equals(usuario.getRol().getNombre());
    }

    private void validarCambioEstado(EstadoIncidencia estadoActual, EstadoIncidencia nuevoEstado) {
        if (estadoActual == EstadoIncidencia.CERRADA && nuevoEstado != EstadoIncidencia.CERRADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Una incidencia cerrada no puede reabrirse");
        }
    }

    private void validarComentarioDeIncidencia(Long incidenciaId, ComentarioIncidencia comentario) {
        if (comentario.getIncidencia() == null || !comentario.getIncidencia().getId().equals(incidenciaId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El comentario no pertenece a esta incidencia");
        }
    }

    private void validarAutorComentario(Usuario usuarioActual, Usuario autorComentario) {
        if (autorComentario == null || !autorComentario.getUsername().equals(usuarioActual.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el autor puede modificar o borrar su comentario");
        }
    }

    private ComentarioResponse mapComentarioResponse(ComentarioIncidencia comentario) {
        ComentarioResponse response = new ComentarioResponse();
        response.setId(comentario.getId());
        response.setContenido(comentario.getContenido());
        response.setFechaCreacion(comentario.getFechaCreacion());

        if (comentario.getAutor() != null) {
            response.setAutor(comentario.getAutor().getUsername());
        }

        return response;
    }

    private UsuarioAsignableResponse mapUsuarioAsignable(Usuario usuario) {
        UsuarioAsignableResponse response = new UsuarioAsignableResponse();
        response.setId(usuario.getId());
        response.setUsername(usuario.getUsername());
        response.setNombreCompleto(usuario.getNombre() + " " + usuario.getApellidos());
        return response;
    }
}
