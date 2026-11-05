package com.ludus.api.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// POJO
@Entity
@Table(name = "interacciones")
public class Interaccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_interaccion", nullable = false, unique = true)
    private Integer id_interaccion;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "id_resenia", nullable = false)
    private Integer idResenia;

    @Column(name = "fechas_like", nullable = false)
    private LocalDateTime fecha_like;

    // Constructor vacío
    public Interaccion() {
    }

    public Interaccion(Integer id_interaccion, Integer idUsuario, Integer idResenia, LocalDateTime fecha_like) {
        this.id_interaccion = id_interaccion;
        this.idUsuario = idUsuario;
        this.idResenia = idResenia;
        this.fecha_like = fecha_like;
    }

    // Getters y Setters
    public Integer getId_usuario() {
        return idUsuario;
    }

    public void setId_usuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getId_resenia() {
        return idResenia;
    }

    public void setId_resenia(Integer idResenia) {
        this.idResenia = idResenia;
    }

    public Integer getIdResenia() {
        return idResenia;
    }

    public void setIdResenia(Integer idResenia) {
        this.idResenia = idResenia;
    }

    public Integer getId_interacciones() {
        return id_interaccion;
    }

    public Integer getId_interaccion() {
        return id_interaccion;
    }

    public void setId_interaccion(Integer id_interaccion) {
        this.id_interaccion = id_interaccion;
    }

    public LocalDateTime getFecha_like() {
        return fecha_like;
    }

    public void setFecha_like(LocalDateTime fecha_like) {
        this.fecha_like = fecha_like;
    }
}
