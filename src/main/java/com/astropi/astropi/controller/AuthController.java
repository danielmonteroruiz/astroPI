package com.astropi.astropi.controller;

import com.astropi.astropi.model.Usuario;
import com.astropi.astropi.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    /// // CONTROL DE PETICION DE USUARIO PARA EL REGISTRO
// Endpoint para registrar un nuevo usuario en el sistema.
// Recibe los datos en formato JSON, los convierte en un objeto Usuario
    @PostMapping("/register")
    public Usuario register(@RequestBody Usuario usuario){
        return usuarioService.SaveUser(usuario);
    }
}
