package com.astropi.astropi.controller.dto.grupo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear o actualizar grupos.
 */
public class GrupoRequest {

    @NotBlank(message = "El nombre del grupo es obligatorio")
    @Size(max = 100, message = "El nombre del grupo no puede superar 100 caracteres")
    private String nombre;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
