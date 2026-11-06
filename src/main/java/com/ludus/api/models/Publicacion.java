package com.ludus.api.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

//POJO
@Entity
@Table(name = "resenias", uniqueConstraints = @UniqueConstraint(
        name = "uk_resenias_usuario_juego", columnNames = {"id_usuario", "id_juego"}))
public class Publicacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resenia", nullable = false, unique = true)
    private Integer id_resenia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_juego", nullable = false)
    private Juego juego;

    @Column (name = "calificacion", nullable = false)
    private Double calificacion;
    @Column (name = "texto_resenia", nullable = false, columnDefinition = "TEXT")
    private String texto_resenia;
    @Column (name = "fecha_publicacion", nullable = false)
    private LocalDateTime fecha_publicacion;

    // CONSTRUCTOR
    public Publicacion(Integer id_resenia, Usuario usuario, Juego juego, Double calificacion, String texto_resenia, LocalDateTime fecha_publicacion){
        this.id_resenia = id_resenia;
        this.usuario = usuario;
        this.juego = juego;
        this.calificacion = calificacion;
        this.texto_resenia = texto_resenia;
        this.fecha_publicacion = fecha_publicacion;
    }// fin CONSTRUCTOR

    public Publicacion(){}// CONSTRUCTOR VACIO

    // METODOS GET Y SET

    public Integer getId_resenia() {
        return id_resenia;
    }// FIN getId_resenia

    public void setId_resenia(Integer id_resenia) {
        this.id_resenia = id_resenia;
    }//FIN setId_resenia

    public Usuario getUsuario() {
        return usuario;
    }// getUsuario

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }// setUsuario

    public Juego getJuego() {
        return juego;
    }// getJuego

    public void setJuego(Juego juego) {
        this.juego = juego;
    }// setJuego

    public Double getCalificacion() {
        return calificacion;
    }//fin getCalificacion

    public void setCalificacion(Double calificacion) {
        this.calificacion = calificacion;
    }//fin setCalificacion

    public String getTexto_resenia() {
        return texto_resenia;
    }// fin getteXTO

    public void setTexto_resenia(String texto_resenia) {
        this.texto_resenia = texto_resenia;
    }// fin setTexto

    public LocalDateTime getFecha_publicacion() {
        return fecha_publicacion;
    }// fin getFecha

    public void setFecha_publicacion(LocalDateTime fecha_publicacion) {
        this.fecha_publicacion = fecha_publicacion;
    }// fin setFecha

    // METODO TO STRING
    @Override
    public String toString() {
        return "Publicacion{" +
                "id_resenia=" + id_resenia +
                ", id_usuario=" + (usuario != null ? usuario.getId_usuario() : null) +
                ", id_juego=" + (juego != null ? juego.getId_juego() : null) +
                ", calificacion=" + calificacion +
                ", texto_resenia='" + texto_resenia + '\'' +
                ", fecha_publicacion=" + fecha_publicacion +
                '}';
    }// FIN TO STRING
}// fin cosntructor vacio




