package com.astropi.astropi.controller.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO para cambiar el grupo asignado a un usuario desde administracion.
 */
public class AsignarGrupoRequest {

    @NotNull(message = "El id del grupo es obligatorio")
    @Positive(message = "El id del grupo debe ser positivo")
    private Long grupoId;

    public Long getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(Long grupoId) {
        this.grupoId = grupoId;
    }
}
