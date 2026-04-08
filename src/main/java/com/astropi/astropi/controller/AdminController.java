package com.astropi.astropi.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    //Endpoint prueba para admin
    @GetMapping("/test")
    public String testAdmin() {
        return "Acceso permitido a SUPER_ADMIN";
    }
}
