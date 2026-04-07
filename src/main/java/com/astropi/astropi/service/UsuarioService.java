package com.astropi.astropi.service;

import com.astropi.astropi.model.Grupo;
import com.astropi.astropi.model.Rol;
import com.astropi.astropi.model.Usuario;
import com.astropi.astropi.repository.GrupoRepository;
import com.astropi.astropi.repository.RolRepository;
import com.astropi.astropi.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

        //encrypt password
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

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

}
