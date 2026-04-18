package com.astropi.astropi.controller.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO para asignar o quitar permisos a un rol.
 */
public class AsignarPermisoRequest {

    @NotNull(message = "El permisoId es obligatorio")
    @Positive(message = "El permisoId debe ser positivo")
    private Long permisoId;

    public Long getPermisoId() {
        return permisoId;
    }

    public void setPermisoId(Long permisoId) {
        this.permisoId = permisoId;
    }
}
