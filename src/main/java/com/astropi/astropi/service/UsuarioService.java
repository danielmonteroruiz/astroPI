package com.astropi.astropi.service;

import com.astropi.astropi.controller.dto.admin.AdminUsuarioResponse;
import com.astropi.astropi.controller.dto.auth.UserResponse;
import com.astropi.astropi.controller.dto.admin.RolResponse;
import com.astropi.astropi.model.Grupo;
import com.astropi.astropi.model.Permiso;
import com.astropi.astropi.model.Rol;
import com.astropi.astropi.model.Usuario;
import com.astropi.astropi.repository.GrupoRepository;
import com.astropi.astropi.repository.IncidenciaRepository;
import com.astropi.astropi.repository.PeticionRepository;
import com.astropi.astropi.repository.RolRepository;
import com.astropi.astropi.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class UsuarioService {

    private static final Set<String> ROLES_PERMITIDOS = Set.of("SUPER_ADMIN", "USER");

    @Autowired
    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private GrupoRepository grupoRepository;
    @Autowired
    private IncidenciaRepository incidenciaRepository;
    @Autowired
    private PeticionRepository peticionRepository;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          GrupoRepository grupoRepository,
                          IncidenciaRepository incidenciaRepository,
                          PeticionRepository peticionRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.grupoRepository = grupoRepository;
        this.incidenciaRepository = incidenciaRepository;
        this.peticionRepository = peticionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrarUsuario(Usuario usuario){

        validarUsernameDisponible(usuario.getUsername(), null);
        validarEmailDisponible(usuario.getEmail(), null);
        validarDniDisponible(usuario.getDni(), null);

        //encrypt password
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setActivo(true);
        usuario.setEmail(normalizarTextoOpcional(usuario.getEmail()));

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

    public AdminUsuarioResponse crearUsuarioAdmin(String username,
                                                  String nombre,
                                                  String apellidos,
                                                  String email,
                                                  String dni,
                                                  String password,
                                                  Long rolId,
                                                  Long grupoId,
                                                  Boolean activo) {

        validarUsernameDisponible(username, null);
        validarEmailDisponible(email, null);
        validarDniDisponible(dni, null);

        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado"));

        if (!esRolPermitido(rol)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no permitido");
        }

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grupo no encontrado"));

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setEmail(normalizarTextoOpcional(email));
        usuario.setDni(dni);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(rol);
        usuario.setGrupo(grupo);
        usuario.setActivo(activo);

        return mapToAdminResponse(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public UserResponse obtenerPerfilActual(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        UserResponse response = new UserResponse();
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

    @Transactional(readOnly = true)
    public List<AdminUsuarioResponse> obtenerUsuariosAdmin() {
        return usuarioRepository.findAll().stream()
                .sorted(Comparator.comparing(Usuario::getId))
                .map(this::mapToAdminResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RolResponse> obtenerRolesAdmin() {
        return rolRepository.findAll().stream()
                .filter(this::esRolPermitido)
                .sorted(Comparator.comparing(Rol::getId))
                .map(this::mapToRolResponse)
                .toList();
    }

    public AdminUsuarioResponse actualizarUsuario(Long usuarioId,
                                                  String username,
                                                  String nombre,
                                                  String apellidos,
                                                  String email,
                                                  String dni) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        validarUsernameDisponible(username, usuarioId);
        validarEmailDisponible(email, usuarioId);
        validarDniDisponible(dni, usuarioId);

        usuario.setUsername(username);
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setEmail(normalizarTextoOpcional(email));
        usuario.setDni(dni);

        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        return mapToAdminResponse(usuarioActualizado);
    }

    public AdminUsuarioResponse asignarGrupo(Long usuarioId, Long grupoId) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grupo no encontrado"));

        usuario.setGrupo(grupo);
        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        return mapToAdminResponse(usuarioActualizado);
    }

    public AdminUsuarioResponse asignarRol(Long usuarioId, Long rolId, String usernameAdmin) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado"));

        if (!esRolPermitido(rol)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no permitido");
        }

        validarCambioRolSeguro(usuario, rol, usernameAdmin);

        usuario.setRol(rol);
        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        return mapToAdminResponse(usuarioActualizado);
    }

    public AdminUsuarioResponse actualizarActivo(Long usuarioId, Boolean activo, String usernameAdmin) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        validarCambioActivoSeguro(usuario, activo, usernameAdmin);

        usuario.setActivo(activo);
        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        return mapToAdminResponse(usuarioActualizado);
    }

    public AdminUsuarioResponse cambiarPassword(Long usuarioId, String password) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        usuario.setPassword(passwordEncoder.encode(password));
        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        return mapToAdminResponse(usuarioActualizado);
    }

    public void eliminarUsuario(Long usuarioId, String usernameAdmin) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        validarEliminacionUsuario(usuario, usernameAdmin);

        usuarioRepository.delete(usuario);
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
            response.setPermisos(usuario.getRol().getPermisos().stream()
                    .map(Permiso::getNombre)
                    .sorted()
                    .toList());
        }

        if (usuario.getGrupo() != null) {
            response.setGrupo(usuario.getGrupo().getNombre());
        }

        return response;
    }

    private boolean esRolPermitido(Rol rol) {
        return rol != null && ROLES_PERMITIDOS.contains(rol.getNombre());
    }

    private void validarUsernameDisponible(String username, Long usuarioId) {
        usuarioRepository.findByUsername(username)
                .filter(usuarioExistente -> esOtroUsuario(usuarioExistente, usuarioId))
                .ifPresent(usuarioExistente -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "El username ya existe");
                });
    }

    private void validarEmailDisponible(String email, Long usuarioId) {

        String emailNormalizado = normalizarTextoOpcional(email);

        if (emailNormalizado == null) {
            return;
        }

        usuarioRepository.findByEmail(emailNormalizado)
                .filter(usuarioExistente -> esOtroUsuario(usuarioExistente, usuarioId))
                .ifPresent(usuarioExistente -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya existe");
                });
    }

    private void validarDniDisponible(String dni, Long usuarioId) {
        usuarioRepository.findByDni(dni)
                .filter(usuarioExistente -> esOtroUsuario(usuarioExistente, usuarioId))
                .ifPresent(usuarioExistente -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "El dni ya existe");
                });
    }

    private boolean esOtroUsuario(Usuario usuarioExistente, Long usuarioId) {
        return usuarioId == null || !usuarioExistente.getId().equals(usuarioId);
    }

    private String normalizarTextoOpcional(String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor;
    }

    private void validarCambioRolSeguro(Usuario usuario, Rol nuevoRol, String usernameAdmin) {

        boolean esMismoUsuario = usuario.getUsername().equals(usernameAdmin);
        boolean quitaSuperAdmin = esSuperAdmin(usuario) && !"SUPER_ADMIN".equals(nuevoRol.getNombre());

        if (esMismoUsuario && quitaSuperAdmin) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes quitarte tu propio rol SUPER_ADMIN");
        }

        if (quitaSuperAdmin && esUltimoSuperAdminActivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe existir al menos un SUPER_ADMIN activo");
        }
    }

    private void validarCambioActivoSeguro(Usuario usuario, Boolean activo, String usernameAdmin) {

        if (Boolean.TRUE.equals(activo)) {
            return;
        }

        if (usuario.getUsername().equals(usernameAdmin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes desactivar tu propio usuario");
        }

        if (esSuperAdmin(usuario) && esUltimoSuperAdminActivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe existir al menos un SUPER_ADMIN activo");
        }
    }

    private boolean esSuperAdmin(Usuario usuario) {
        return usuario.getRol() != null && "SUPER_ADMIN".equals(usuario.getRol().getNombre());
    }

    private boolean esUltimoSuperAdminActivo() {
        return usuarioRepository.countByRolNombreAndActivoTrue("SUPER_ADMIN") <= 1;
    }

    private void validarEliminacionUsuario(Usuario usuario, String usernameAdmin) {

        if (usuario.getUsername().equals(usernameAdmin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes eliminar tu propio usuario");
        }

        if (esSuperAdmin(usuario) && Boolean.TRUE.equals(usuario.getActivo()) && esUltimoSuperAdminActivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe existir al menos un SUPER_ADMIN activo");
        }

        if (incidenciaRepository.existsByUsuarioId(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar un usuario con incidencias asociadas");
        }

        if (peticionRepository.existsByUsuarioId(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar un usuario con peticiones asociadas");
        }
    }

    private RolResponse mapToRolResponse(Rol rol) {

        RolResponse response = new RolResponse();

        response.setId(rol.getId());
        response.setNombre(rol.getNombre());
        response.setPermisos(rol.getPermisos().stream()
                .map(Permiso::getNombre)
                .sorted()
                .toList());

        return response;
    }

}
