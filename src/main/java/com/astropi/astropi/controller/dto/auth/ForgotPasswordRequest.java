package com.astropi.astropi.controller.dto.auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

/**
 * DTO para solicitar recuperacion de password.
 */
public class ForgotPasswordRequest {

    @Size(max = 50, message = "El username no puede superar 50 caracteres")
    private String username;

    @Size(max = 150, message = "El email no puede superar 150 caracteres")
    private String email;

    @AssertTrue(message = "Debes indicar username o email")
    public boolean isTieneUsernameOEmail() {
        return (username != null && !username.isBlank()) || (email != null && !email.isBlank());
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
