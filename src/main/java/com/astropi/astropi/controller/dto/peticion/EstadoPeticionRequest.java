package com.astropi.astropi.controller.dto.peticion;

import com.astropi.astropi.model.EstadoPeticion;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para cambiar el estado de una peticion.
 */
public class EstadoPeticionRequest {

    @NotNull(message = "El estado es obligatorio")
    private EstadoPeticion estado;

    public EstadoPeticion getEstado() {
        return estado;
    }

    public void setEstado(EstadoPeticion estado) {
        this.estado = estado;
    }
}
