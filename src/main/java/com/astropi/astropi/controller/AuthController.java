package com.astropi.astropi.controller;

import com.astropi.astropi.controller.dto.auth.LoginRequest;
import com.astropi.astropi.controller.dto.auth.LoginResponse;
import com.astropi.astropi.controller.dto.auth.RegisterRequest;
import com.astropi.astropi.controller.dto.auth.UserResponse;
import com.astropi.astropi.model.Usuario;
import com.astropi.astropi.security.JwtUtil;
import com.astropi.astropi.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de autenticación.
 * Gestiona registro y login de usuarios.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioService usuarioService;


    /**
     * Endpoint de login.
     *
     * Flujo:
     * 1. Spring Security valida credenciales (UserDetailsService + BCrypt)
     * 2. Si son correctas → genera JWT
     * 3. Se devuelve el token al cliente
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request){
        //1. Autenticación
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        String token = jwtUtil.generateToken(userDetails.getUsername(),role);

        return new LoginResponse(token);

    }

    @PostMapping("/register")
    public UserResponse register(@RequestBody RegisterRequest request) {

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setNombre(request.getNombre());
        usuario.setApellidos(request.getApellidos());
        usuario.setEmail(request.getEmail());
        usuario.setDni(request.getDni());
        usuario.setPassword(request.getPassword());

        Usuario usuarioRegistrado = usuarioService.registrarUsuario(usuario);

        return new UserResponse(
                usuarioRegistrado.getUsername(),
                "ROLE_" + usuarioRegistrado.getRol().getNombre()
        );
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(Authentication authentication){

        // Username obtenido desde el token JWT.
        String username = authentication.getName();

        // Rol obtenido desde el token JWT.
        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority();
        return new UserResponse(username, role);
    }


}
