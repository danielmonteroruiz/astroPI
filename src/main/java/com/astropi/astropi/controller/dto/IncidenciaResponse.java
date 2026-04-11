package com.astropi.astropi.controller.dto;

import java.time.LocalDateTime;

public class IncidenciaResponse {
    private Long id;
    private String codigoTicket; // I-2026XXXX-0001
    private String titulo;
    private String descripcion;
    private String servicio;
    private String categoria;
    private String estado;
    private String grupo;
    private String usuario;
    private LocalDateTime fechaCreacion;

    // GETTERS Y SETTERS 👇

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo; }
}
