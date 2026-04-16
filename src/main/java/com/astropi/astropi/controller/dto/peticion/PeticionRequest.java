package com.astropi.astropi.controller.dto.peticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear una peticion.
 */
public class PeticionRequest {

    @NotBlank(message = "El titulo es obligatorio")
    @Size(max = 150, message = "El titulo no puede superar 150 caracteres")
    private String titulo;

    @NotBlank(message = "La descripcion es obligatoria")
    @Size(max = 1000, message = "La descripcion no puede superar 1000 caracteres")
    private String descripcion;

    @NotBlank(message = "El servicio es obligatorio")
    @Size(max = 100, message = "El servicio no puede superar 100 caracteres")
    private String servicio;

    @NotBlank(message = "La categoria es obligatoria")
    @Size(max = 100, message = "La categoria no puede superar 100 caracteres")
    private String categoria;

    @NotNull(message = "El grupo es obligatorio")
    @Positive(message = "El grupo debe ser un id positivo")
    private Long grupoId;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getServicio() {
        return servicio;
    }

    public void setServicio(String servicio) {
        this.servicio = servicio;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Long getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(Long grupoId) {
        this.grupoId = grupoId;
    }
}
