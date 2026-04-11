package com.astropi.astropi.service;

import com.astropi.astropi.model.Grupo;
import com.astropi.astropi.model.Incidencia;
import com.astropi.astropi.model.Usuario;
import com.astropi.astropi.repository.GrupoRepository;
import com.astropi.astropi.repository.IncidenciaRepository;
import com.astropi.astropi.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.astropi.astropi.controller.dto.IncidenciaResponse;

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

    /// ///
    public Incidencia CrearIncidencia(String titulo, String descripcion,
                                      String servicio, String categoria,
                                      Long grupoId,
                                      String username){

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        Incidencia incidencia = new Incidencia(titulo, descripcion, usuario);

        incidencia.setServicio(servicio);
        incidencia.setCategoria(categoria);
        incidencia.setGrupo(grupo);

        String codigo = generarCodigoTicket();
        incidencia.setCodigoTicket(codigo);

        return incidenciaRepository.save(incidencia);
    }
    /// ///
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


    /// ///
    public List<IncidenciaResponse> obtenerMisIncidencias(String username){
        List<Incidencia> incidencias = incidenciaRepository.findByUsuarioUsername(username);

        return incidencias.stream()
                .map(this::mapToResponse)
                .toList();
    }
    /// ///
    public IncidenciaResponse mapToResponse(Incidencia incidencia){

        IncidenciaResponse response = new IncidenciaResponse();

        response.setId(incidencia.getId());
        response.setTitulo(incidencia.getTitulo());
        response.setDescripcion(incidencia.getDescripcion());
        response.setEstado(incidencia.getEstado().name());
        response.setFechaCreacion(incidencia.getFechaCreacion());
        response.setCodigoTicket(incidencia.getCodigoTicket());

        if (incidencia.getUsuario() != null){
            response.setUsuario(incidencia.getUsuario().getUsername());

            if (incidencia.getUsuario().getGrupo() != null){
                response.setGrupo(incidencia.getUsuario().getGrupo().getNombre());
            }
        }

        return response;
    }


    /// ///
    public List<IncidenciaResponse> obtenerIncidenciasUsuarioYGrupo(String username){

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Long grupoId = usuario.getGrupo().getId();

        List<Incidencia> incidencias = incidenciaRepository
                .findByUsuarioUsernameOrUsuarioGrupoId(username, grupoId);

        return incidencias.stream()
                .map(this::mapToResponse)
                .toList();
    }



}
