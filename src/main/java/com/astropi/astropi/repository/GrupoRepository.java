package com.astropi.astropi.repository;

import com.astropi.astropi.model.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GrupoRepository extends JpaRepository<Grupo, Long> {

    Optional<Grupo> findByNombre(String nombre);
}