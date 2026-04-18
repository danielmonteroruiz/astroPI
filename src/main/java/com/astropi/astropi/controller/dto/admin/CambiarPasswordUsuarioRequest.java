package com.astropi.astropi.controller.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para cambiar la password de un usuario desde administracion.
 */
public class CambiarPasswordUsuarioRequest {

    @NotBlank(message = "La password es obligatoria")
    @Size(min = 6, max = 100, message = "La password debe tener entre 6 y 100 caracteres")
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
