package com.astropi.astropi.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa una peticion en el sistema HelpDesk.
 */
@Entity
@Table(
        name = "peticiones",
        indexes = {
                @Index(name = "idx_peticiones_usuario_id", columnList = "usuario_id"),
                @Index(name = "idx_peticiones_usuario_asignado_id", columnList = "usuario_asignado_id"),
                @Index(name = "idx_peticiones_grupo_id", columnList = "grupo_id"),
                @Index(name = "idx_peticiones_estado", columnList = "estado"),
                @Index(name = "idx_peticiones_fecha_creacion", columnList = "fecha_creacion")
        }
)
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
    @JoinColumn(name = "usuario_asignado_id")
    private Usuario usuarioAsignado;

    @ManyToOne
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @OneToMany(mappedBy = "peticion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaCreacion ASC")
    private java.util.List<ComentarioPeticion> comentarios = new java.util.ArrayList<>();

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

    public Usuario getUsuarioAsignado() {
        return usuarioAsignado;
    }

    public void setUsuarioAsignado(Usuario usuarioAsignado) {
        this.usuarioAsignado = usuarioAsignado;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public java.util.List<ComentarioPeticion> getComentarios() {
        return comentarios;
    }

    public void setComentarios(java.util.List<ComentarioPeticion> comentarios) {
        this.comentarios = comentarios;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
