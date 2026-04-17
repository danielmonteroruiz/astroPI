package com.astropi.astropi.controller.dto.admin;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para activar o desactivar usuarios desde administracion.
 */
public class UsuarioActivoRequest {

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
