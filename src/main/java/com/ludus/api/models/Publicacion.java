package com.ludus.api.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

//POJO
@Entity
@Table(name = "resenias")
public class Publicacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "id_resenia", nullable= false , unique = true)
    private Integer id_resenia;
    @Column (name = "calificacion", nullable = false)
    private Double calificacion;
    @Column (name = "texto_resenia", nullable = false)
    private String texto_resenia;
    @Column (name = "fecha_publicacion", nullable = false)
    private LocalDateTime fecha_publicacion;

    // CONSTRUCTOR
    public Publicacion(Integer id_resenia, Double calificacion, String texto_resenia, LocalDateTime fecha_publicacion){
        this.id_resenia = id_resenia;
        this.calificacion = calificacion;
        this.texto_resenia = texto_resenia;
        this.fecha_publicacion = fecha_publicacion;
    }// fin CONSTRUCTOR

    public Publicacion(){}// CONSTRUCTOR VACIO

    // METODOS GET Y SET

    public Integer getId_resenia() {
        return id_resenia;
    }// FIN getId_resenia

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
                ", calificacion=" + calificacion +
                ", texto_resenia='" + texto_resenia + '\'' +
                ", fecha_publicacion=" + fecha_publicacion +
                '}';
    }// FIN TO STRING
}// fin cosntructor vacio




