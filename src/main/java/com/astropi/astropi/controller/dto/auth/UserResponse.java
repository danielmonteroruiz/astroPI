package com.astropi.astropi.controller.dto.auth;

/**
 * DTO para devolver información del usuario autenticado.
 */
public class UserResponse {

    private String username;
    private String role;

    public UserResponse(String username, String role){

        this.username = username;
        this.role = role;
    }

    //GETER & SETER
    public String getUsername(){
        return username;
    }

    public String getRole(){
        return role;
    }
}
