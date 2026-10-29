package com.ludus.api.models;


import jakarta.persistence.*;

import java.time.LocalDateTime;

//POJO
@Entity
@Table(name = "interacciones")

public class Interaccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_interaccion", nullable = false, unique = true)
    private Integer id_interaccion;

    @Column(name = "fechas_like", nullable = false)
    private LocalDateTime fecha_like;

    // Clave foránea hacia Usuarios
    //@ManyToOne
    //@JoinColumn(name = "id_usuario", nullable = false)
    //private Usuario usuario;

    // Clave foránea hacia Reseñas
    //@ManyToOne
    //@JoinColumn(name = "id_resenia", nullable = false)
    //private Resenia resenia;

    // Constructos vació
    public Interaccion(){

    } // Constructor vació

    // Contructor temporar, antes de las claves foraneas, para probar que funcione
    public Interaccion(Integer id_interaccion, LocalDateTime fecha_like) {
        this.id_interaccion = id_interaccion;
        this.fecha_like = fecha_like;
    }// Constructor Iteraccion

    //Constructor para cuando ya contemos con las KF
    //public Interaccion(Usuario usuario, Resenia resenia, LocalDateTime fechaLike) {
     //   this.usuario = usuario;
    //  this.resenia = resenia;
    //  this.fechaLike = fechaLike;
    //}  Constructor

    // Getters y Setters

    //public Usuario getUsuario() {return usuario;}

    //public void setUsuario(Usuario usuario) {this.usuario = usuario;}

    // public Resenia getResenia() {return resenia;}

   // public void setResenia(Resenia resenia) {this.resenia = resenia;}


    public Integer getId_interacciones() {
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
