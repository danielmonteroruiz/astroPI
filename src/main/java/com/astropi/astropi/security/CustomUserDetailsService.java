package com.astropi.astropi.security;

import com.astropi.astropi.model.Usuario;
import com.astropi.astropi.repository.UsuarioRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de cargar los datos del usuario desde la base de datos
 * durante el proceso de autenticación
 */

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository){
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Metodo invocado por Spring Security durante el login.
     *
     * @param username nombre de usuario introducido en la autenticación
     * @return UserDetails con credenciales y roles del usuario
     * @throws UsernameNotFoundException si el usuario no existe en la BBDD
     */

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{

        //Buscar usuario en la DB
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        //Validar si el usuario esta activo
        if(Boolean.FALSE.equals(usuario.getActivo())) {
            throw new DisabledException("User is disabled");
        }

        //Construir objeto UserDetails que Spring utilizará para validar credenciales
        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword()) //Pass encriptada con Bcrypt
                .roles(usuario.getRol() != null ? usuario.getRol().getNombre() : "USER")
                .build();
    }

}
