package com.astropi.astropi.controller.dto.common;

import jakarta.validation.constraints.Size;

/**
 * Request para asignar o desasignar un ticket a un usuario.
 */
public class AsignarTicketRequest {

    @Size(max = 50, message = "El username asignado no puede superar los 50 caracteres")
    private String usernameAsignado;

    public String getUsernameAsignado() {
        return usernameAsignado;
    }

    public void setUsernameAsignado(String usernameAsignado) {
        this.usernameAsignado = usernameAsignado;
    }
}
