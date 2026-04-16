package com.astropi.astropi.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa una peticion en el sistema HelpDesk.
 */
@Entity
@Table(name = "peticiones")
public class Peticion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Codigo unico del ticket (P-YYYYMMDD-XXXX).
     */
    @Column(unique = true)
    private String codigoTicket;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Column(nullable = false, length = 100)
    private String servicio;

    @Column(nullable = false, length = 100)
    private String categoria;

    @Enumerated(EnumType.STRING)
    private EstadoPeticion estado;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    private LocalDateTime fechaCreacion;

    public Peticion() {
    }

    public Peticion(String titulo, String descripcion, Usuario usuario) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.usuario = usuario;
        this.estado = EstadoPeticion.ABIERTA;
        this.fechaCreacion = LocalDateTime.now();
    }

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

    public EstadoPeticion getEstado() {
        return estado;
    }

    public void setEstado(EstadoPeticion estado) {
        this.estado = estado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
