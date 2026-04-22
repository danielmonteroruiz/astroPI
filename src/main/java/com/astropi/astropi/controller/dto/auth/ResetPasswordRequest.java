package com.astropi.astropi.controller.dto.auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {

    @NotBlank(message = "El token es obligatorio")
    private String token;

    @NotBlank(message = "La nueva password es obligatoria")
    @Size(min = 6, max = 100, message = "La password debe tener entre 6 y 100 caracteres")
    private String password;

    @NotBlank(message = "La confirmacion de password es obligatoria")
    @Size(min = 6, max = 100, message = "La confirmacion debe tener entre 6 y 100 caracteres")
    private String confirmacionPassword;

    @AssertTrue(message = "Las passwords no coinciden")
    public boolean isPasswordCoincide() {
        return password != null && password.equals(confirmacionPassword);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmacionPassword() {
        return confirmacionPassword;
    }

    public void setConfirmacionPassword(String confirmacionPassword) {
        this.confirmacionPassword = confirmacionPassword;
    }
}
