
package com.astropi.astropi.controller.dto;

/**
 * DTO para crear una incidencia.
 */
public class IncidenciaRequest {

    private String titulo;
    private String descripcion;

    public String getTitulo(){
        return titulo;
    }

    public void setTitulo(String titulo){
        this.titulo = titulo;
    }
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

}
