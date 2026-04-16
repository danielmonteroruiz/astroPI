package com.astropi.astropi.controller;

import com.astropi.astropi.controller.dto.GrupoRequest;
import com.astropi.astropi.controller.dto.GrupoResponse;
import com.astropi.astropi.service.GrupoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private GrupoService grupoService;

    //Endpoint prueba para admin
    @GetMapping("/test")
    public String testAdmin() {
        return "Acceso permitido a SUPER_ADMIN";
    }

    @PostMapping("/grupos")
    public ResponseEntity<GrupoResponse> crearGrupo(@Valid @RequestBody GrupoRequest request) {
        GrupoResponse grupo = grupoService.crearGrupo(request.getNombre());

        return ResponseEntity.ok(grupo);
    }
}
