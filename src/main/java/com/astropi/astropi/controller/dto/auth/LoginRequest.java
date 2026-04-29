package com.astropi.astropi.controller.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para recibir credenciales de login.
 */
public class LoginRequest {

    @NotBlank(message = "El username es obligatorio")
    @Size(max = 50, message = "El username no puede superar 50 caracteres")
    private String username;

    @NotBlank(message = "La password es obligatoria")
    @Size(min = 4, max = 100, message = "La password debe tener entre 4 y 100 caracteres")
    private String password;

    public String getUsername(){
        return username;
    }

    public void setUsername(String username){
        this.username=username;
    }

    public String getPassword(){
        return password;
    }

    public void setPassword(String password){
        this.password = password;
    }
}
