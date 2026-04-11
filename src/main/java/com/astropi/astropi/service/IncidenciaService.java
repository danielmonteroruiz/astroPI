package com.astropi.astropi.service;

import com.astropi.astropi.model.Incidencia;
import com.astropi.astropi.model.Usuario;
import com.astropi.astropi.repository.IncidenciaRepository;
import com.astropi.astropi.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    public Incidencia CrearIncidencia(String titulo, String descripcion, String username){
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Incidencia incidencia = new Incidencia(titulo, descripcion, usuario);

        return incidenciaRepository.save(incidencia);

    }

    public List<IncidenciaResponse> obtenerMisIncidencias(String username){
        List<Incidencia> incidencias = incidenciaRepository.findByUsuarioUsername(username);

        return incidencias.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private IncidenciaResponse mapToResponse(Incidencia incidencia){

        IncidenciaResponse response = new IncidenciaResponse();

        response.setId(incidencia.getId());
        response.setTitulo(incidencia.getTitulo());
        response.setDescripcion(incidencia.getDescripcion());
        response.setEstado(incidencia.getEstado().name());
        response.setFechaCreacion(incidencia.getFechaCreacion());

        if (incidencia.getUsuario() != null){
            response.setUsuario(incidencia.getUsuario().getUsername());

            if (incidencia.getUsuario().getGrupo() != null){
                response.setGrupo(incidencia.getUsuario().getGrupo().getNombre());
            }
        }

        return response;
    }

}
