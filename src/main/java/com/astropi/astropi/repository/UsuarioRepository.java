package com.astropi.astropi.repository;

import com.astropi.astropi.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByDni(String dni);

    boolean existsByGrupoId(Long grupoId);

    long countByRolNombreAndActivoTrue(String rolNombre);
}
