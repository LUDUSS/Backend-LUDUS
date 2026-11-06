package com.ludus.api.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

//POJO
@Entity
@Table(name = "interacciones", uniqueConstraints = @UniqueConstraint(
        name = "uk_interacciones_usuario_resenia", columnNames = {"id_usuario", "id_resenia"}))

public class Interaccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_interaccion", nullable = false, unique = true)
    private Integer id_interaccion;


    @Column(name = "fecha_like", nullable = false)
    private LocalDateTime fecha_like;

    // Clave foránea hacia Usuarios
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private Usuario usuario;

    // Clave foránea hacia Reseñas
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_resenia", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private Publicacion publicacion;

    // Constructos vació
    public Interaccion(){

    } // Constructor vació

    public Integer getId_interaccion() { return id_interaccion; }
    public void setId_interaccion(Integer id_interaccion) { this.id_interaccion = id_interaccion; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Publicacion getPublicacion() { return publicacion; }
    public void setPublicacion(Publicacion publicacion) { this.publicacion = publicacion; }
    public LocalDateTime getFecha_like() { return fecha_like; }
    public void setFecha_like(LocalDateTime fecha_like) { this.fecha_like = fecha_like; }

} // class Interacion
