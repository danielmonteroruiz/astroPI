package com.astropi.astropi.security;

import com.astropi.astropi.model.Permiso;
import com.astropi.astropi.model.Usuario;
import com.astropi.astropi.repository.UsuarioRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio encargado de cargar los datos del usuario desde la base de datos
 * durante el proceso de autenticacion.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Metodo invocado por Spring Security durante el login.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new DisabledException("User is disabled");
        }

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(obtenerAuthorities(usuario))
                .build();
    }

    private List<GrantedAuthority> obtenerAuthorities(Usuario usuario) {

        List<GrantedAuthority> authorities = new ArrayList<>();

        String rol = usuario.getRol() != null ? usuario.getRol().getNombre() : "USER";
        authorities.add(new SimpleGrantedAuthority("ROLE_" + rol));

        if (usuario.getRol() != null && usuario.getRol().getPermisos() != null) {
            usuario.getRol().getPermisos().stream()
                    .map(Permiso::getNombre)
                    .sorted()
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }

        return authorities;
    }
}
