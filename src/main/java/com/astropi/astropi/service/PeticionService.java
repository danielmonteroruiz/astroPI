package com.astropi.astropi.service;

import com.astropi.astropi.controller.dto.common.ComentarioResponse;
import com.astropi.astropi.controller.dto.common.PagedResponse;
import com.astropi.astropi.controller.dto.common.UsuarioAsignableResponse;
import com.astropi.astropi.controller.dto.peticion.PeticionResponse;
import com.astropi.astropi.model.ComentarioPeticion;
import com.astropi.astropi.model.EstadoPeticion;
import com.astropi.astropi.model.Grupo;
import com.astropi.astropi.model.Peticion;
import com.astropi.astropi.model.Usuario;
import com.astropi.astropi.repository.ComentarioPeticionRepository;
import com.astropi.astropi.repository.GrupoRepository;
import com.astropi.astropi.repository.PeticionRepository;
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
 * Logica de negocio de peticiones.
 */
@Service
@Transactional
public class PeticionService {

    @Autowired
    private PeticionRepository peticionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private ComentarioPeticionRepository comentarioPeticionRepository;

    public Peticion crearPeticion(String titulo, String descripcion,
                                  String servicio, String categoria,
                                  Long grupoId,
                                  String username) {

        Usuario usuario = obtenerUsuarioPorUsername(username);
        Grupo grupo = obtenerGrupoPorId(grupoId);

        Peticion peticion = new Peticion(titulo, descripcion, usuario);
        peticion.setServicio(servicio);
        peticion.setCategoria(categoria);
        peticion.setGrupo(grupo);
        peticion.setCodigoTicket(generarCodigoTicket());

        return peticionRepository.save(peticion);
    }

    public List<PeticionResponse> obtenerMisPeticiones(String username) {
        List<Peticion> peticiones = peticionRepository.findByUsuarioUsername(username);

        return peticiones.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<PeticionResponse> obtenerPeticionesUsuarioYGrupo(String username,
                                                                          EstadoPeticion estado,
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

        Specification<Peticion> filtros = crearFiltrosPeticiones(
                usuario,
                estado,
                servicio,
                categoria,
                grupoId,
                fechaDesde,
                fechaHasta
        );

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaCreacion"));
        Page<Peticion> peticiones = peticionRepository.findAll(filtros, pageable);

        List<PeticionResponse> content = peticiones.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return new PagedResponse<>(
                content,
                peticiones.getNumber(),
                peticiones.getSize(),
                peticiones.getTotalElements(),
                peticiones.getTotalPages()
        );
    }

    public PeticionResponse actualizarEstado(Long peticionId, EstadoPeticion nuevoEstado, String username) {
        Usuario usuario = obtenerUsuarioPorUsername(username);
        Peticion peticion = obtenerPeticionPorId(peticionId);

        validarGestionTicket(usuario, peticion);
        validarCambioEstado(peticion.getEstado(), nuevoEstado);

        peticion.setEstado(nuevoEstado);
        return mapToResponse(peticionRepository.save(peticion));
    }

    public PeticionResponse asignarPeticion(Long peticionId, String usernameAsignado, String username) {
        Usuario usuario = obtenerUsuarioPorUsername(username);
        Peticion peticion = obtenerPeticionPorId(peticionId);

        validarGestionTicket(usuario, peticion);

        if (usernameAsignado == null || usernameAsignado.isBlank()) {
            peticion.setUsuarioAsignado(null);
            return mapToResponse(peticionRepository.save(peticion));
        }

        Usuario usuarioAsignado = obtenerUsuarioPorUsername(usernameAsignado);
        validarUsuarioAsignable(peticion, usuarioAsignado);

        peticion.setUsuarioAsignado(usuarioAsignado);
        return mapToResponse(peticionRepository.save(peticion));
    }

    public ComentarioResponse agregarComentario(Long peticionId, String contenido, String username) {
        Usuario usuario = obtenerUsuarioPorUsername(username);
        Peticion peticion = obtenerPeticionPorId(peticionId);

        validarGestionTicket(usuario, peticion);

        ComentarioPeticion comentario = new ComentarioPeticion();
        comentario.setPeticion(peticion);
        comentario.setAutor(usuario);
        comentario.setContenido(contenido.trim());
        comentario.setFechaCreacion(LocalDateTime.now());

        ComentarioPeticion comentarioGuardado = comentarioPeticionRepository.save(comentario);
        peticion.getComentarios().add(comentarioGuardado);

        return mapComentarioResponse(comentarioGuardado);
    }

    public ComentarioResponse actualizarComentario(Long peticionId, Long comentarioId, String contenido, String username) {
        Usuario usuario = obtenerUsuarioPorUsername(username);
        Peticion peticion = obtenerPeticionPorId(peticionId);
        ComentarioPeticion comentario = comentarioPeticionRepository.findById(comentarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentario no encontrado"));

        validarComentarioDePeticion(peticionId, comentario);
        validarAutorComentario(usuario, comentario.getAutor());
        validarGestionTicket(usuario, peticion);

        comentario.setContenido(contenido.trim());

        return mapComentarioResponse(comentarioPeticionRepository.save(comentario));
    }

    public void eliminarComentario(Long peticionId, Long comentarioId, String username) {
        Usuario usuario = obtenerUsuarioPorUsername(username);
        Peticion peticion = obtenerPeticionPorId(peticionId);
        ComentarioPeticion comentario = comentarioPeticionRepository.findById(comentarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentario no encontrado"));

        validarComentarioDePeticion(peticionId, comentario);
        validarAutorComentario(usuario, comentario.getAutor());
        validarGestionTicket(usuario, peticion);

        comentarioPeticionRepository.delete(comentario);
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

    public PeticionResponse mapToResponse(Peticion peticion) {
        PeticionResponse response = new PeticionResponse();

        response.setId(peticion.getId());
        response.setCodigoTicket(peticion.getCodigoTicket());
        response.setTitulo(peticion.getTitulo());
        response.setDescripcion(peticion.getDescripcion());
        response.setServicio(peticion.getServicio());
        response.setCategoria(peticion.getCategoria());
        response.setEstado(peticion.getEstado().name());
        response.setFechaCreacion(peticion.getFechaCreacion());

        if (peticion.getUsuario() != null) {
            response.setUsuario(peticion.getUsuario().getUsername());
        }

        if (peticion.getUsuarioAsignado() != null) {
            response.setUsuarioAsignado(peticion.getUsuarioAsignado().getUsername());
        }

        if (peticion.getGrupo() != null) {
            response.setGrupo(peticion.getGrupo().getNombre());
        }

        response.setComentarios(
                peticion.getComentarios().stream()
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

    private Peticion obtenerPeticionPorId(Long peticionId) {
        return peticionRepository.findById(peticionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Peticion no encontrada"));
    }

    private String generarCodigoTicket() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int siguienteNumero = obtenerSiguienteNumeroTicket();
        String secuencia = String.format("%04d", siguienteNumero);

        return "P-" + fecha + "-" + secuencia;
    }

    private int obtenerSiguienteNumeroTicket() {
        return peticionRepository.findTopByOrderByIdDesc()
                .map(Peticion::getCodigoTicket)
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

    private Specification<Peticion> crearFiltrosPeticiones(Usuario usuario,
                                                           EstadoPeticion estado,
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

    private void validarGestionTicket(Usuario usuario, Peticion peticion) {
        if (!puedeGestionarPeticion(usuario, peticion)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes modificar esta peticion");
        }
    }

    private boolean puedeGestionarPeticion(Usuario usuario, Peticion peticion) {
        boolean esCreador = peticion.getUsuario() != null
                && peticion.getUsuario().getUsername().equals(usuario.getUsername());

        boolean mismoGrupo = usuario.getGrupo() != null
                && peticion.getGrupo() != null
                && peticion.getGrupo().getId().equals(usuario.getGrupo().getId());

        return esCreador || mismoGrupo;
    }

    private void validarUsuarioAsignable(Peticion peticion, Usuario usuarioAsignado) {
        if (!Boolean.TRUE.equals(usuarioAsignado.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo puedes asignar peticiones a usuarios activos");
        }

        if (peticion.getGrupo() == null || usuarioAsignado.getGrupo() == null
                || !peticion.getGrupo().getId().equals(usuarioAsignado.getGrupo().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario asignado debe pertenecer al mismo grupo");
        }
    }

    private void validarCambioEstado(EstadoPeticion estadoActual, EstadoPeticion nuevoEstado) {
        if (estadoActual == EstadoPeticion.CERRADA && nuevoEstado != EstadoPeticion.CERRADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Una peticion cerrada no puede reabrirse");
        }
    }

    private void validarComentarioDePeticion(Long peticionId, ComentarioPeticion comentario) {
        if (comentario.getPeticion() == null || !comentario.getPeticion().getId().equals(peticionId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El comentario no pertenece a esta peticion");
        }
    }

    private void validarAutorComentario(Usuario usuarioActual, Usuario autorComentario) {
        if (autorComentario == null || !autorComentario.getUsername().equals(usuarioActual.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el autor puede modificar o borrar su comentario");
        }
    }

    private ComentarioResponse mapComentarioResponse(ComentarioPeticion comentario) {
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
