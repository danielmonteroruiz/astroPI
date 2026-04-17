package com.astropi.astropi.repository;

import com.astropi.astropi.model.Peticion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para gestionar peticiones.
 */
@Repository
public interface PeticionRepository extends JpaRepository<Peticion, Long>, JpaSpecificationExecutor<Peticion> {

    List<Peticion> findByUsuarioUsername(String username);

    List<Peticion> findByUsuarioUsernameOrGrupoId(String username, Long grupoId);

    long countByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);

    boolean existsByGrupoId(Long grupoId);
}
