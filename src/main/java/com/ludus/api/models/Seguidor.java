
package com.ludus.api.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// POJO
@Entity
@Table(
        name = "seguidores",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_seguidor_seguido",
                        columnNames = {"id_seguidor", "id_seguido"}
                )
        }
)
public class Seguidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seguimiento", nullable = false, unique = true)
    private Integer idSeguimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_seguidor",
            referencedColumnName = "id_usuario",
            nullable = false
    )
    private Usuario seguidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_seguido",
            referencedColumnName = "id_usuario",
            nullable = false
    )
    private Usuario seguido;

    @Column(name = "fecha_seguimiento", nullable = false)
    private LocalDateTime fechaSeguimiento;

    public Seguidor(Usuario seguidor, Usuario seguido, LocalDateTime fechaSeguimiento) {
        this.seguidor = seguidor;
        this.seguido = seguido;
        this.fechaSeguimiento = fechaSeguimiento;
    }// constructor

    // Constructor vacío
    public Seguidor() {
    }

    // Getters y Setters
    public Integer getIdSeguimiento() {
        return idSeguimiento;
    }

    public void setIdSeguimiento(Integer idSeguimiento) {
        this.idSeguimiento = idSeguimiento;
    }

    public Usuario getSeguidor() {
        return seguidor;
    }// getSeguidor

    public void setSeguidor(Usuario seguidor) {
        this.seguidor = seguidor;
    }// setSeguidor

    public Usuario getSeguido() {
        return seguido;
    }// getSeguido

    public void setSeguido(Usuario seguido) {
        this.seguido = seguido;
    }// setSeguido

    public LocalDateTime getFechaSeguimiento() {
        return fechaSeguimiento;
    }

    public void setFechaSeguimiento(LocalDateTime fechaSeguimiento) {
        this.fechaSeguimiento = fechaSeguimiento;
    }

    // toString
    @Override
    public String toString() {
        return "Seguidor{" +
                "id_seguimiento=" + idSeguimiento +
                ", id_seguidor=" +
                (seguidor != null ? seguidor.getId_usuario() : null) +
                ", id_seguido=" +
                (seguido != null ? seguido.getId_usuario() : null) +
                ", fecha_seguimiento=" + fechaSeguimiento +
                '}';
    }
}// class Seguidor
