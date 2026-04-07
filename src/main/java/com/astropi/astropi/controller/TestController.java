package com.astropi.astropi.controller;

import com.astropi.astropi.repository.UsuarioRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final UsuarioRepository usuarioRepository;

    public TestController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    //test to localhost:8080/test - Basic API
    @GetMapping("/test")
    public String test() {
        return "Usuarios en BD: " + usuarioRepository.count();
    }
}