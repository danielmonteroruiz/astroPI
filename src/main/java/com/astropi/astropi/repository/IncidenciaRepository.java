package com.astropi.astropi.repository;

import com.astropi.astropi.model.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Repositorio para gestionar incidencias.
 */
@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {
}
