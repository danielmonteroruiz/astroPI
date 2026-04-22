package com.astropi.astropi.repository;

import com.astropi.astropi.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @EntityGraph(attributePaths = {"rol", "rol.permisos", "grupo"})
    Optional<Usuario> findByUsername(String username);

    @Override
    @EntityGraph(attributePaths = {"rol", "rol.permisos", "grupo"})
    List<Usuario> findAll();

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByDni(String dni);

    boolean existsByGrupoId(Long grupoId);

    long countByRolNombreAndActivoTrue(String rolNombre);

    List<Usuario> findByGrupoIdAndActivoTrueOrderByUsernameAsc(Long grupoId);
}
