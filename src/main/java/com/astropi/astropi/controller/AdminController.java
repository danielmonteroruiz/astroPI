package com.astropi.astropi.controller;

import com.astropi.astropi.controller.dto.admin.AdminUsuarioResponse;
import com.astropi.astropi.controller.dto.admin.AsignarGrupoRequest;
import com.astropi.astropi.controller.dto.admin.UsuarioActivoRequest;
import com.astropi.astropi.controller.dto.grupo.GrupoRequest;
import com.astropi.astropi.controller.dto.grupo.GrupoResponse;
import com.astropi.astropi.service.GrupoService;
import com.astropi.astropi.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private UsuarioService usuarioService;

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

    @GetMapping("/usuarios")
    public ResponseEntity<List<AdminUsuarioResponse>> obtenerUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerUsuariosAdmin());
    }

    @PutMapping("/usuarios/{id}/grupo")
    public ResponseEntity<AdminUsuarioResponse> asignarGrupo(
            @PathVariable Long id,
            @Valid @RequestBody AsignarGrupoRequest request) {

        AdminUsuarioResponse usuario = usuarioService.asignarGrupo(id, request.getGrupoId());

        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/usuarios/{id}/activo")
    public ResponseEntity<AdminUsuarioResponse> actualizarActivo(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioActivoRequest request) {

        AdminUsuarioResponse usuario = usuarioService.actualizarActivo(id, request.getActivo());

        return ResponseEntity.ok(usuario);
    }
}
