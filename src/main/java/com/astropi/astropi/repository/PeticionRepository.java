package com.astropi.astropi.repository;

import com.astropi.astropi.model.Peticion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar peticiones.
 */
@Repository
public interface PeticionRepository extends JpaRepository<Peticion, Long>, JpaSpecificationExecutor<Peticion> {

    List<Peticion> findByUsuarioUsername(String username);

    List<Peticion> findByUsuarioUsernameOrGrupoId(String username, Long grupoId);

    Optional<Peticion> findTopByOrderByIdDesc();

    boolean existsByGrupoId(Long grupoId);

    boolean existsByUsuarioId(Long usuarioId);
}
