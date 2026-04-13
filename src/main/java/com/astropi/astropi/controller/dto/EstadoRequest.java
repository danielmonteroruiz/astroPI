package com.astropi.astropi.controller.dto;

/**
 * DTO para cambiar el estado de una incidencia.
 */
public class EstadoRequest {

    private String estado;

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
