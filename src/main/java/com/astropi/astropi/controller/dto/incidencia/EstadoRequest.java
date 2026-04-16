package com.astropi.astropi.controller.dto.incidencia;

import com.astropi.astropi.model.EstadoIncidencia;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para cambiar el estado de una incidencia.
 */
public class EstadoRequest {

    @NotNull(message = "El estado es obligatorio")
    private EstadoIncidencia estado;

    public EstadoIncidencia getEstado() {
        return estado;
    }

    public void setEstado(EstadoIncidencia estado) {
        this.estado = estado;
    }
}
