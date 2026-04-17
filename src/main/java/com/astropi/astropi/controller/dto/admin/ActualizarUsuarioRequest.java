package com.astropi.astropi.controller.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para editar datos basicos de un usuario desde administracion.
 */
public class ActualizarUsuarioRequest {

    @NotBlank(message = "El username es obligatorio")
    @Size(max = 50, message = "El username no puede superar 50 caracteres")
    private String username;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombre;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 150, message = "Los apellidos no pueden superar 150 caracteres")
    private String apellidos;

    @Email(message = "El email debe tener un formato valido")
    @Size(max = 150, message = "El email no puede superar 150 caracteres")
    private String email;

    @NotBlank(message = "El dni es obligatorio")
    @Size(max = 20, message = "El dni no puede superar 20 caracteres")
    private String dni;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }
}
