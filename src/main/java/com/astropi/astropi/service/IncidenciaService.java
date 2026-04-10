package com.astropi.astropi.service;

import com.astropi.astropi.model.Incidencia;
import com.astropi.astropi.model.Usuario;
import com.astropi.astropi.repository.IncidenciaRepository;
import com.astropi.astropi.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
