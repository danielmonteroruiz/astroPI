
package com.astropi.astropi.controller.dto;

/**
 * DTO para crear una incidencia.
 */
public class IncidenciaRequest {


    private String titulo;
    private String descripcion;
    private String servicio;
    private String categoria;
    private Long grupoId; // importante para asignación

    // getters y setters

    public String getTitulo(){
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getServicio() {
        return servicio;
    }
    public String getCategoria() {
        return categoria;
    }


    public void setTitulo(String titulo){
        this.titulo = titulo;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public void setServicio(String servicio){
        this.servicio = servicio;
    }

    public void setCategoria(String categoria){
        this.categoria = categoria;
    }


}
