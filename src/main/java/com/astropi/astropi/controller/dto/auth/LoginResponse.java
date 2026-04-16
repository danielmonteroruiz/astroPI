package com.astropi.astropi.controller.dto.auth;

/**
 * DTO que devuelve el token JWT al cliente.
 */
public class LoginResponse {

    private String token;

    public LoginResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
