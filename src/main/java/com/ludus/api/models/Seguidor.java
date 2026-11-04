
package com.ludus.api.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// POJO
@Entity
@Table(name = "seguidores")
public class Seguidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seguimiento", nullable = false, unique = true)
    private Integer idSeguimiento;

    // Clave foránea hacia Usuarios (el que da el seguir)
    // Descomentar cuando exista Usuario.java y la tabla usuarios en MySQL
    //@ManyToOne
    //@JoinColumn(name = "id_seguidor", referencedColumnName = "id_usuario", nullable = false)
    //private Usuario seguidor;

    // Clave foránea hacia Usuarios (el que recibe el seguido)
    // Descomentar cuando exista Usuario.java y la tabla usuarios en MySQL
    //@ManyToOne
    //@JoinColumn(name = "id_seguido", referencedColumnName = "id_usuario", nullable = false)
    //private Usuario seguido;

    // Temporal, antes de las claves foráneas, para probar que funcione
    @Column(name = "id_seguidor", nullable = false)
    private Integer idSeguidor;

    @Column(name = "id_seguido", nullable = false)
    private Integer idSeguido;

    @Column(name = "fecha_seguimiento", nullable = false)
    private LocalDateTime fechaSeguimiento;

    // Constructor vacío
    public Seguidor() {
    }

    // Constructor temporal, antes de las claves foráneas
    public Seguidor(Integer idSeguidor, Integer idSeguido, LocalDateTime fechaSeguimiento) {
        this.idSeguidor = idSeguidor;
        this.idSeguido = idSeguido;
        this.fechaSeguimiento = fechaSeguimiento;
    }

    // Getters y Setters
    public Integer getIdSeguimiento() {
        return idSeguimiento;
    }

    public void setIdSeguimiento(Integer idSeguimiento) {
        this.idSeguimiento = idSeguimiento;
    }

    public Integer getIdSeguidor() {
        return idSeguidor;
    }

    public void setIdSeguidor(Integer idSeguidor) {
        this.idSeguidor = idSeguidor;
    }

    public Integer getIdSeguido() {
        return idSeguido;
    }

    public void setIdSeguido(Integer idSeguido) {
        this.idSeguido = idSeguido;
    }

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
                ", id_seguidor=" + idSeguidor +
                ", id_seguido=" + idSeguido +
                ", fecha_seguimiento=" + fechaSeguimiento +
                '}';
    }
}// class Seguidor
