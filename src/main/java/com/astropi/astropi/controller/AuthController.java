package com.astropi.astropi.controller;

import com.astropi.astropi.controller.dto.LoginRequest;
import com.astropi.astropi.controller.dto.LoginResponse;
import com.astropi.astropi.controller.dto.UserResponse;
import com.astropi.astropi.model.Usuario;
import com.astropi.astropi.security.JwtUtil;
import com.astropi.astropi.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

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
        //DEBUG
        System.out.println(userDetails.getAuthorities());

        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        String token = jwtUtil.generateToken(userDetails.getUsername(),role);

        return new LoginResponse(token);

    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(Authentication authentication){

        // DEBUG
        System.out.println("Authentication: " + authentication);

        //username desde el token
        String username = authentication.getName();

        //Rol desde el token
        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority();
        return new UserResponse(username, role);
    }


}
