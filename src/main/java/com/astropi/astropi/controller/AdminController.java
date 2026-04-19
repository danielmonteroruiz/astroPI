package com.astropi.astropi.controller;

import com.astropi.astropi.controller.dto.admin.ActualizarUsuarioRequest;
import com.astropi.astropi.controller.dto.admin.AdminUsuarioResponse;
import com.astropi.astropi.controller.dto.admin.AsignarGrupoRequest;
import com.astropi.astropi.controller.dto.admin.AsignarPermisoRequest;
import com.astropi.astropi.controller.dto.admin.AsignarRolRequest;
import com.astropi.astropi.controller.dto.admin.CambiarPasswordUsuarioRequest;
import com.astropi.astropi.controller.dto.admin.PermisoRequest;
import com.astropi.astropi.controller.dto.admin.PermisoResponse;
import com.astropi.astropi.controller.dto.admin.RolResponse;
import com.astropi.astropi.controller.dto.admin.UsuarioActivoRequest;
import com.astropi.astropi.controller.dto.common.MensajeResponse;
import com.astropi.astropi.controller.dto.grupo.GrupoRequest;
import com.astropi.astropi.controller.dto.grupo.GrupoResponse;
import com.astropi.astropi.service.GrupoService;
import com.astropi.astropi.service.PermisoService;
import com.astropi.astropi.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PermisoService permisoService;

    @PostMapping("/grupos")
    public ResponseEntity<GrupoResponse> crearGrupo(@Valid @RequestBody GrupoRequest request) {
        GrupoResponse grupo = grupoService.crearGrupo(request.getNombre());

        return ResponseEntity.ok(grupo);
    }

    @PutMapping("/grupos/{id}")
    public ResponseEntity<GrupoResponse> actualizarGrupo(
            @PathVariable Long id,
            @Valid @RequestBody GrupoRequest request) {

        GrupoResponse grupo = grupoService.actualizarGrupo(id, request.getNombre());

        return ResponseEntity.ok(grupo);
    }

    @DeleteMapping("/grupos/{id}")
    public ResponseEntity<MensajeResponse> eliminarGrupo(@PathVariable Long id) {

        grupoService.eliminarGrupo(id);

        return ResponseEntity.ok(new MensajeResponse("Grupo borrado correctamente"));
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<AdminUsuarioResponse>> obtenerUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerUsuariosAdmin());
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RolResponse>> obtenerRoles() {
        return ResponseEntity.ok(usuarioService.obtenerRolesAdmin());
    }

    @GetMapping("/permisos")
    public ResponseEntity<List<PermisoResponse>> obtenerPermisos() {
        return ResponseEntity.ok(permisoService.obtenerPermisos());
    }

    @PostMapping("/permisos")
    public ResponseEntity<PermisoResponse> crearPermiso(@Valid @RequestBody PermisoRequest request) {
        PermisoResponse permiso = permisoService.crearPermiso(
                request.getNombre(),
                request.getDescripcion()
        );

        return ResponseEntity.ok(permiso);
    }

    @DeleteMapping("/permisos/{id}")
    public ResponseEntity<MensajeResponse> eliminarPermiso(@PathVariable Long id) {

        permisoService.eliminarPermiso(id);

        return ResponseEntity.ok(new MensajeResponse("Permiso borrado correctamente"));
    }

    @PutMapping("/roles/{id}/permisos")
    public ResponseEntity<RolResponse> asignarPermisoARol(
            @PathVariable Long id,
            @Valid @RequestBody AsignarPermisoRequest request) {

        RolResponse rol = permisoService.asignarPermisoARol(id, request.getPermisoId());

        return ResponseEntity.ok(rol);
    }

    @DeleteMapping("/roles/{id}/permisos/{permisoId}")
    public ResponseEntity<RolResponse> quitarPermisoARol(
            @PathVariable Long id,
            @PathVariable Long permisoId) {

        RolResponse rol = permisoService.quitarPermisoARol(id, permisoId);

        return ResponseEntity.ok(rol);
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<AdminUsuarioResponse> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioRequest request) {

        AdminUsuarioResponse usuario = usuarioService.actualizarUsuario(
                id,
                request.getUsername(),
                request.getNombre(),
                request.getApellidos(),
                request.getEmail(),
                request.getDni()
        );

        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/usuarios/{id}/grupo")
    public ResponseEntity<AdminUsuarioResponse> asignarGrupo(
            @PathVariable Long id,
            @Valid @RequestBody AsignarGrupoRequest request) {

        AdminUsuarioResponse usuario = usuarioService.asignarGrupo(id, request.getGrupoId());

        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/usuarios/{id}/rol")
    public ResponseEntity<AdminUsuarioResponse> asignarRol(
            @PathVariable Long id,
            @Valid @RequestBody AsignarRolRequest request,
            Authentication authentication) {

        AdminUsuarioResponse usuario = usuarioService.asignarRol(
                id,
                request.getRolId(),
                authentication.getName()
        );

        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/usuarios/{id}/activo")
    public ResponseEntity<AdminUsuarioResponse> actualizarActivo(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioActivoRequest request,
            Authentication authentication) {

        AdminUsuarioResponse usuario = usuarioService.actualizarActivo(
                id,
                request.getActivo(),
                authentication.getName()
        );

        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/usuarios/{id}/password")
    public ResponseEntity<AdminUsuarioResponse> cambiarPassword(
            @PathVariable Long id,
            @Valid @RequestBody CambiarPasswordUsuarioRequest request) {

        AdminUsuarioResponse usuario = usuarioService.cambiarPassword(
                id,
                request.getPassword()
        );

        return ResponseEntity.ok(usuario);
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<MensajeResponse> eliminarUsuario(
            @PathVariable Long id,
            Authentication authentication) {

        usuarioService.eliminarUsuario(id, authentication.getName());

        return ResponseEntity.ok(new MensajeResponse("Usuario borrado correctamente"));
    }
}
