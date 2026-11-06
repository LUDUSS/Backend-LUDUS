package com.ludus.api.models;

import jakarta.persistence.*;

@Entity
@Table(name = "juegos")
public class Juego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_juego", nullable = false, unique = true)
    private Integer id_juego;
    @Column(name = "id_api", nullable = false, unique = true)
    private Long id_api;
    @Column(name = "nombre_juego", nullable = false, columnDefinition = "TEXT")
    private String nombre_juego;
    @Column(name = "categoria", columnDefinition = "TEXT")
    private String categoria;
    @Column(name = "plataforma", columnDefinition = "TEXT")
    private String plataforma;
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    @Column(name = "imagen_url", columnDefinition = "TEXT")
    private String imagen_url;

    public Juego(Integer id_juego, Integer id_api, String nombre_juego, String categoria, String plataforma, String descripcion, String imagen_url) {
        this.id_juego = id_juego;
        this.id_api = id_api == null ? null : id_api.longValue();
        this.nombre_juego = nombre_juego;
        this.categoria = categoria;
        this.plataforma = plataforma;
        this.descripcion = descripcion;
        this.imagen_url = imagen_url;
    }// constructor

    public Juego() {}// constructor vacío

    public Integer getId_juego() {
        return id_juego;
    }// getId_juego

    public Long getId_api() {
        return id_api;
    }// getId_api

    public void setId_api(Long id_api) {
        this.id_api = id_api;
    }// setId_api

    public String getNombre_juego() {
        return nombre_juego;
    }// getNombre_juego

    public void setNombre_juego(String nombre_juego) {
        this.nombre_juego = nombre_juego;
    }// setNombre_juego

    public String getCategoria() {
        return categoria;
    }// getCategoria

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }// setCategoria

    public String getPlataforma() {
        return plataforma;
    }// getPlataforma

    public void setPlataforma(String plataforma) {
        this.plataforma = plataforma;
    }// setPlataforma

    public String getDescripcion() {
        return descripcion;
    }// getDescripción

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }// setDescripción

    public String getImagen_url() {
        return imagen_url;
    }// getImagen_url

    public void setImagen_url(String imagen_url) {
        this.imagen_url = imagen_url;
    }// setImagen_url

    @Override
    public String toString() {
        return "Juego{" +
                "id_juego=" + id_juego +
                ", id_api=" + id_api +
                ", nombre_juego='" + nombre_juego + '\'' +
                ", categoria='" + categoria + '\'' +
                ", plataforma='" + plataforma + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", imagen_url='" + imagen_url + '\'' +
                '}';
    }// toString
}
