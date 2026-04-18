package com.astropi.astropi.repository;

import com.astropi.astropi.model.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


/**
 * Repositorio para gestionar incidencias.
 */
@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Long>, JpaSpecificationExecutor<Incidencia> {

    List<Incidencia> findByUsuarioUsername(String username);

    List<Incidencia> findByUsuarioUsernameOrGrupoId(String username, Long grupoId);

    Optional<Incidencia> findTopByOrderByIdDesc();

    boolean existsByGrupoId(Long grupoId);

    boolean existsByUsuarioId(Long usuarioId);

}
