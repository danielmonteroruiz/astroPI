package com.astropi.astropi.service;

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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

        LocalDateTime ahora = LocalDateTime.now();
        String fecha = ahora.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        LocalDateTime inicioDia = ahora.toLocalDate().atStartOfDay();
        LocalDateTime finDia = ahora.toLocalDate().atTime(23, 59, 59);

        long contador = peticionRepository.countByFechaCreacionBetween(inicioDia, finDia) + 1;
        String secuencia = String.format("%04d", contador);

        return "P-" + fecha + "-" + secuencia;
    }

    public List<PeticionResponse> obtenerMisPeticiones(String username) {
        List<Peticion> peticiones = peticionRepository.findByUsuarioUsername(username);

        return peticiones.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<PeticionResponse> obtenerPeticionesUsuarioYGrupo(String username,
                                                                 EstadoPeticion estado,
                                                                 String servicio,
                                                                 String categoria,
                                                                 Long grupoId) {

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Specification<Peticion> filtros = crearFiltrosPeticiones(
                usuario,
                estado,
                servicio,
                categoria,
                grupoId
        );

        List<Peticion> peticiones = peticionRepository.findAll(
                filtros,
                Sort.by(Sort.Direction.DESC, "fechaCreacion")
        );

        return peticiones.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private Specification<Peticion> crearFiltrosPeticiones(Usuario usuario,
                                                           EstadoPeticion estado,
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
