package com.ludus.api.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comentarios")
public class Comentario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comentario", nullable = false, unique = true)
    private Integer id_comentario;

    @Column(name = "id_resenia", nullable = false)
    private Integer id_resenia;

    @Column(name = "id_usuario", nullable = false)
    private Integer id_usuario;

    @Column(name = "texto_comentario", nullable = false, columnDefinition = "TEXT")
    private String texto_comentario;

    @Column(name = "fecha_comentario", nullable = false)
    private LocalDateTime fecha_comentario;

    public Comentario() {
    }

    public Integer getId_comentario() {
        return id_comentario;
    }

    public Integer getId_resenia() {
        return id_resenia;
    }

    public void setId_resenia(Integer id_resenia) {
        this.id_resenia = id_resenia;
    }

    public Integer getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Integer id_usuario) {
        this.id_usuario = id_usuario;
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
