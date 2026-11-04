package com.ludus.api.models;


import jakarta.persistence.*;

import java.time.LocalDateTime;

//POJO
@Entity
@Table(
        name = "interacciones",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_usuario_resenia",
                        columnNames = {"id_usuario", "id_resenia"}
                )
        }
)

public class Interaccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_interaccion", nullable = false, unique = true)
    private Integer id_interaccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_resenia", nullable = false)
    private Publicacion publicacion;

    @Column(name = "fecha_like", nullable = false)
    private LocalDateTime fecha_like;

    public Interaccion(
            Usuario usuario,
            Publicacion publicacion,
            LocalDateTime fecha_like) {

        this.usuario = usuario;
        this.publicacion = publicacion;
        this.fecha_like = fecha_like;
    }// constructor

    // Constructos vació
    public Interaccion() {

    } // Constructor vació

    public Usuario getUsuario() {
        return usuario;
    }// getUsuario

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }// setUsuario

    public Publicacion getPublicacion() {
        return publicacion;
    }// getPublicacion

    public void setPublicacion(Publicacion publicacion) {
        this.publicacion = publicacion;
    }// setPublicacion

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
} // class Interacion
