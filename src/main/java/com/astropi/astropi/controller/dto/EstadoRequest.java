package com.astropi.astropi.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para cambiar el estado de una incidencia.
 */
public class EstadoRequest {

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(
            regexp = "ABIERTA|EN_PROCESO|PARADA|RESUELTA|CERRADA",
            message = "El estado debe ser ABIERTA, EN_PROCESO, PARADA, RESUELTA o CERRADA"
    )
    private String estado;

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
