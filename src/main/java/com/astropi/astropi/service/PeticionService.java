package com.astropi.astropi.service;

import com.astropi.astropi.controller.dto.common.PagedResponse;
import com.astropi.astropi.controller.dto.peticion.PeticionResponse;
import com.astropi.astropi.model.EstadoPeticion;
import com.astropi.astropi.model.Grupo;
import com.astropi.astropi.model.Peticion;
import com.astropi.astropi.model.Usuario;
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
public class PeticionService {

    @Autowired
    private PeticionRepository peticionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GrupoRepository grupoRepository;

    public Peticion crearPeticion(String titulo, String descripcion,
                                  String servicio, String categoria,
                                  Long grupoId,
                                  String username) {

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grupo no encontrado"));

        Peticion peticion = new Peticion(titulo, descripcion, usuario);

        peticion.setServicio(servicio);
        peticion.setCategoria(categoria);
        peticion.setGrupo(grupo);
        peticion.setCodigoTicket(generarCodigoTicket());

        return peticionRepository.save(peticion);
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

    public List<PeticionResponse> obtenerMisPeticiones(String username) {
        List<Peticion> peticiones = peticionRepository.findByUsuarioUsername(username);

        return peticiones.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PagedResponse<PeticionResponse> obtenerPeticionesUsuarioYGrupo(String username,
                                                                          EstadoPeticion estado,
                                                                          String servicio,
                                                                          String categoria,
                                                                          Long grupoId,
                                                                          LocalDate fechaDesde,
                                                                          LocalDate fechaHasta,
                                                                          int page,
                                                                          int size) {

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

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

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "fechaCreacion")
        );

        Page<Peticion> peticiones = peticionRepository.findAll(
                filtros,
                pageable
        );

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

    public PeticionResponse actualizarEstado(Long peticionId, EstadoPeticion nuevoEstado, String username) {

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Peticion peticion = peticionRepository.findById(peticionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Peticion no encontrada"));

        if (!puedeGestionarPeticion(usuario, peticion)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes modificar esta peticion");
        }

        validarCambioEstado(peticion.getEstado(), nuevoEstado);
        peticion.setEstado(nuevoEstado);

        Peticion peticionActualizada = peticionRepository.save(peticion);

        return mapToResponse(peticionActualizada);
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

        if (peticion.getGrupo() != null) {
            response.setGrupo(peticion.getGrupo().getNombre());
        }

        return response;
    }

    private boolean puedeGestionarPeticion(Usuario usuario, Peticion peticion) {

        boolean esCreador = peticion.getUsuario() != null
                && peticion.getUsuario().getUsername().equals(usuario.getUsername());

        boolean mismoGrupo = usuario.getGrupo() != null
                && peticion.getGrupo() != null
                && peticion.getGrupo().getId().equals(usuario.getGrupo().getId());

        return esCreador || mismoGrupo;
    }

    private void validarCambioEstado(EstadoPeticion estadoActual, EstadoPeticion nuevoEstado) {

        if (estadoActual == EstadoPeticion.CERRADA && nuevoEstado != EstadoPeticion.CERRADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Una peticion cerrada no puede reabrirse");
        }
    }
}
