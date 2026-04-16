package com.astropi.astropi.service;

import com.astropi.astropi.controller.dto.admin.AdminUsuarioResponse;
import com.astropi.astropi.model.Grupo;
import com.astropi.astropi.model.Rol;
import com.astropi.astropi.model.Usuario;
import com.astropi.astropi.repository.GrupoRepository;
import com.astropi.astropi.repository.RolRepository;
import com.astropi.astropi.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

/// // USER REGISTER

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private GrupoRepository grupoRepository;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          GrupoRepository grupoRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.grupoRepository = grupoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario SaveUser(Usuario usuario){

        if (usuarioRepository.findByUsername(usuario.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El username ya existe");
        }

        //encrypt password
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setActivo(true);

        //rol assign
        Rol rol = rolRepository.findByNombre("USER")
                .orElseThrow(()-> new RuntimeException("Rol USER not found"));
        usuario.setRol(rol);

        //Default Group Assign
        Grupo grupo = grupoRepository.findByNombre("Sistemas IT")
                .orElseThrow(()->new RuntimeException("Group not found"));
        usuario.setGrupo(grupo);

        return usuarioRepository.save(usuario);
    }

    public List<AdminUsuarioResponse> obtenerUsuariosAdmin() {
        return usuarioRepository.findAll().stream()
                .sorted(Comparator.comparing(Usuario::getId))
                .map(this::mapToAdminResponse)
                .toList();
    }

    private AdminUsuarioResponse mapToAdminResponse(Usuario usuario) {

        AdminUsuarioResponse response = new AdminUsuarioResponse();

        response.setId(usuario.getId());
        response.setUsername(usuario.getUsername());
        response.setNombre(usuario.getNombre());
        response.setApellidos(usuario.getApellidos());
        response.setEmail(usuario.getEmail());
        response.setDni(usuario.getDni());
        response.setActivo(usuario.getActivo());

        if (usuario.getRol() != null) {
            response.setRol(usuario.getRol().getNombre());
        }

        if (usuario.getGrupo() != null) {
            response.setGrupo(usuario.getGrupo().getNombre());
        }

        return response;
    }

}
