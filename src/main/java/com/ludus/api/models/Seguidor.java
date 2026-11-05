package com.ludus.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seguidores")
public class Seguidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seguimiento")
    private Integer idSeguimiento;

    // El que da el seguir
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_seguidor", referencedColumnName = "id_usuario", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario seguidor;

    // El que recibe el seguimiento
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_seguido", referencedColumnName = "id_usuario", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario seguido;

    @Column(name = "fecha_seguimiento", nullable = false)
    private LocalDateTime fechaSeguimiento;

    public Seguidor() {}

    // Getters y Setters
    public Integer getIdSeguimiento() { return idSeguimiento; }
    public void setIdSeguimiento(Integer idSeguimiento) { this.idSeguimiento = idSeguimiento; }
    public Usuario getSeguidor() { return seguidor; }
    public void setSeguidor(Usuario seguidor) { this.seguidor = seguidor; }
    public Usuario getSeguido() { return seguido; }
    public void setSeguido(Usuario seguido) { this.seguido = seguido; }
    public LocalDateTime getFechaSeguimiento() { return fechaSeguimiento; }
    public void setFechaSeguimiento(LocalDateTime fechaSeguimiento) { this.fechaSeguimiento = fechaSeguimiento; }
}