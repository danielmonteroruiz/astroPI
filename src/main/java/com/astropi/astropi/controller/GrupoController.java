package com.astropi.astropi.controller;

import com.astropi.astropi.controller.dto.GrupoResponse;
import com.astropi.astropi.service.GrupoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador publico autenticado para consultar grupos.
 */
@RestController
@RequestMapping("/grupos")
public class GrupoController {

    @Autowired
    private GrupoService grupoService;

    @GetMapping
    public ResponseEntity<List<GrupoResponse>> obtenerGrupos() {
        return ResponseEntity.ok(grupoService.obtenerGrupos());
    }
}
