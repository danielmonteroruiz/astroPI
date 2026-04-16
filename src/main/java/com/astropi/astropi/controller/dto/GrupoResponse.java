package com.astropi.astropi.controller.dto;

public class GrupoResponse {

    private Long id;
    private String nombre;

    public GrupoResponse(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
