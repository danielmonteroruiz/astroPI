package com.astropi.astropi.controller;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
public class UserController {

    //Endpoint de prueba para usuario basico
    @GetMapping("/test")
    public String testUser(){
        return "Acceso permitido a USER";
    }

}
