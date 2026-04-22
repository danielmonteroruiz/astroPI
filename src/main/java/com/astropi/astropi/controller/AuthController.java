package com.astropi.astropi.controller;

import com.astropi.astropi.controller.dto.auth.ForgotPasswordRequest;
import com.astropi.astropi.controller.dto.auth.LoginRequest;
import com.astropi.astropi.controller.dto.auth.LoginResponse;
import com.astropi.astropi.controller.dto.auth.RegisterRequest;
import com.astropi.astropi.controller.dto.auth.UserResponse;
import com.astropi.astropi.controller.dto.common.MensajeResponse;
import com.astropi.astropi.security.JwtUtil;
import com.astropi.astropi.service.PeticionService;
import com.astropi.astropi.service.UsuarioService;
import jakarta.validation.Valid;
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

    @Autowired
    private PeticionService peticionService;


    /**
     * Endpoint de login.
     *
     * Flujo:
     * 1. Spring Security valida credenciales (UserDetailsService + BCrypt)
     * 2. Si son correctas → genera JWT
     * 3. Se devuelve el token al cliente
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request){
        //1. Autenticación
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String role = obtenerRolPrincipal(userDetails);

        String token = jwtUtil.generateToken(userDetails.getUsername(),role);

        return new LoginResponse(token);

    }

    @PostMapping("/register")
    public MensajeResponse register(@Valid @RequestBody RegisterRequest request) {
        peticionService.crearSolicitudAltaUsuario(request);
        return new MensajeResponse("Solicitud de alta enviada al grupo Administradores");
    }

    @PostMapping("/forgot-password")
    public MensajeResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        peticionService.crearSolicitudRecuperacionPassword(request);
        return new MensajeResponse("Solicitud de recuperacion enviada al grupo Administradores");
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(Authentication authentication){
        return usuarioService.obtenerPerfilActual(authentication.getName());
    }

    private String obtenerRolPrincipal(UserDetails userDetails) {

        return userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .findFirst()
                .orElse("ROLE_USER");
    }

}
