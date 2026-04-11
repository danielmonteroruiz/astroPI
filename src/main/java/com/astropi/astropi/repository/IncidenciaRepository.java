package com.astropi.astropi.repository;

import com.astropi.astropi.model.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

import java.util.List;


/**
 * Repositorio para gestionar incidencias.
 */
@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    List<Incidencia> findByUsuarioUsername(String username);

    List<Incidencia> findByUsuarioUsernameOrUsuarioGrupoId(String username, Long grupoId);

    long countByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);

}
