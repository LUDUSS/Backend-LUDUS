
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
    private Integer id_seguimiento;

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
    private Integer id_seguidor;

    @Column(name = "id_seguido", nullable = false)
    private Integer id_seguido;

    @Column(name = "fecha_seguimiento", nullable = false)
    private LocalDateTime fecha_seguimiento;

    // Constructor vacío
    public Seguidor() {
    }

    // Constructor temporal, antes de las claves foráneas
    public Seguidor(Integer id_seguidor, Integer id_seguido, LocalDateTime fecha_seguimiento) {
        this.id_seguidor = id_seguidor;
        this.id_seguido = id_seguido;
        this.fecha_seguimiento = fecha_seguimiento;
    }

    // Getters y Setters
    public Integer getId_seguimiento() {
        return id_seguimiento;
    }

    public void setId_seguimiento(Integer id_seguimiento) {
        this.id_seguimiento = id_seguimiento;
    }

    public Integer getId_seguidor() {
        return id_seguidor;
    }

    public void setId_seguidor(Integer id_seguidor) {
        this.id_seguidor = id_seguidor;
    }

    public Integer getId_seguido() {
        return id_seguido;
    }

    public void setId_seguido(Integer id_seguido) {
        this.id_seguido = id_seguido;
    }

    public LocalDateTime getFecha_seguimiento() {
        return fecha_seguimiento;
    }

    public void setFecha_seguimiento(LocalDateTime fecha_seguimiento) {
        this.fecha_seguimiento = fecha_seguimiento;
    }

    // toString
    @Override
    public String toString() {
        return "Seguidor{" +
                "id_seguimiento=" + id_seguimiento +
                ", id_seguidor=" + id_seguidor +
                ", id_seguido=" + id_seguido +
                ", fecha_seguimiento=" + fecha_seguimiento +
                '}';
    }
}// class Seguidor
