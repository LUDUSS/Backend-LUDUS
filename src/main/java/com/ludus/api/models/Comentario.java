package com.ludus.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comentarios")
public class Comentario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comentario", nullable = false, unique = true)
    private Integer id_comentario;

    // Relación con Resenia (Publicacion)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_resenia", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "comentarios"}) // Evitar recursión
    private Publicacion publicacion;

    // Relación con Usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario usuario;


    @Column(name = "texto_comentario", nullable = false, columnDefinition = "TEXT")
    private String texto_comentario;

    @Column(name = "fecha_comentario", nullable = false)
    private LocalDateTime fecha_comentario;

    public Comentario( Publicacion publicacion, Usuario usuario, String texto_comentario, LocalDateTime fecha_comentario) {
        this.publicacion = publicacion;
        this.usuario = usuario;
        this.texto_comentario = texto_comentario;
        this.fecha_comentario = fecha_comentario;
    }//fin constuctor

    public Comentario() {}// CONSTRUCTOR VACIO

    public Integer getId_comentario() {
        return id_comentario;
    }

    public void setId_comentario(Integer id_comentario) {
        this.id_comentario = id_comentario;
    }

    public Publicacion getPublicacion() {
        return publicacion;
    }

    public void setPublicacion(Publicacion publicacion) {
        this.publicacion = publicacion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTexto_comentario() {
        return texto_comentario;
    }

    public void setTexto_comentario(String texto_comentario) {
        this.texto_comentario = texto_comentario;
    }

    public LocalDateTime getFecha_comentario() {
        return fecha_comentario;
    }

    public void setFecha_comentario(LocalDateTime fecha_comentario) {
        this.fecha_comentario = fecha_comentario;
    }
}
