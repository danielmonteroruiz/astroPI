package com.astropi.astropi.controller.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO para cambiar el rol asignado a un usuario desde administracion.
 */
public class AsignarRolRequest {

    @NotNull(message = "El id del rol es obligatorio")
    @Positive(message = "El id del rol debe ser positivo")
    private Long rolId;

    public Long getRolId() {
        return rolId;
    }

    public void setRolId(Long rolId) {
        this.rolId = rolId;
    }
}
