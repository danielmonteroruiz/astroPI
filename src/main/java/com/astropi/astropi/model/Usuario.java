package com.astropi.astropi.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
        name = "usuarios",
        indexes = {
                @Index(name = "idx_usuarios_grupo_id", columnList = "grupo_id"),
                @Index(name = "idx_usuarios_rol_id_activo", columnList = "rol_id, activo")
        }
)
@Data

public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 150)
    private String apellidos;

    @Column(length = 150, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 20)
    private String dni;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "credenciales_actualizadas_en", nullable = false)
    private java.time.LocalDateTime credencialesActualizadasEn;

    @ManyToOne
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @ManyToOne
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;
}
