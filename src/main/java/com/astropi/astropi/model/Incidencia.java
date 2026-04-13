package com.astropi.astropi.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa una incidencia en el sistema (HelpDesk).
 */
@Entity
@Table(name = "incidencias")
public class Incidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * Código único del ticket (I-YYYYMMDD-XXXX).
     */
    @Column(unique = true)
    private String codigoTicket;

    /**
     * Título breve de la incidencia.
     */
    @Column(nullable = false, length = 150)
    private String titulo;

    /**
     * Descripción detallada del problema.
     */
    @Column(nullable = false, length = 1000)
    private String descripcion;

    /**
     * Estado actual de la incidencia.
     */
    @Enumerated(EnumType.STRING)
    private EstadoIncidencia estado;

    /**
     * Usuario que crea la incidencia.
     */
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String servicio;

    @Column(nullable = false, length = 100)
    private String categoria;

    @ManyToOne
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;
    /**
     * Fecha de creación automática.
     */
    private LocalDateTime fechaCreacion;

    // 🔧 Constructor vacío obligatorio (JPA)
    public Incidencia() {}
    /**
     * Constructor útil para crear incidencias.
     */
    public Incidencia(String titulo, String descripcion, Usuario usuario) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.usuario = usuario;
        this.estado = EstadoIncidencia.ABIERTA;
        this.fechaCreacion = LocalDateTime.now();
    }

    // 🔹 Getters y Setters

    public Long getId() {
        return id;
    }
    public String getCodigoTicket() {
        return codigoTicket;
    }

    public void setCodigoTicket(String codigoTicket) {
        this.codigoTicket = codigoTicket;
    }

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

    public EstadoIncidencia getEstado() {
        return estado;
    }

    public void setEstado(EstadoIncidencia estado) {
        this.estado = estado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
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

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

}
