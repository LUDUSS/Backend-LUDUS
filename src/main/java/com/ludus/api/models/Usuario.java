package com.ludus.api.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

//POJO
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "id_usuario", nullable= false , unique = true)
    private Integer id_usuario;
    @Column (name = "nombre", nullable = false)
    private String nombre;
    @Column (name = "correo", nullable = false)
    private String correo;
    @Column (name = "contrasenia", nullable = false)
    private String contrasenia;
    @Column (name = "telefono", nullable = false)
    private String telefono;
    @Column (name = "sobre_mi", nullable = false)
    private String sobre_mi;
    @Column (name = "fecha_registro", nullable = false)
    private LocalDateTime fecha_registro;

    // CONSTRUCTOR
    public Usuario(Integer id_usuario, String nombre, String correo, String contrasenia, String telefono, String sobre_mi, LocalDateTime fecha_registro) {
        this.id_usuario = id_usuario;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenia = contrasenia;
        this.telefono= telefono;
        this.sobre_mi = sobre_mi;
        this.fecha_registro = fecha_registro;
    }// fin CONSTRUCTOR

    public Usuario(){}// CONSTRUCTOR VACÍO

// MÉTODOS GET Y SET

    public Integer getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Integer id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getSobre_mi() {
        return sobre_mi;
    }

    public void setSobre_mi(String sobre_mi) {
        this.sobre_mi = sobre_mi;
    }

    public LocalDateTime getFecha_registro() {
        return fecha_registro;
    }

    public void setFecha_registro(LocalDateTime fecha_registro) {
        this.fecha_registro = fecha_registro;
    }

    // METODO TO STRING
    @Override
    public String toString() {
        return "Usuario{" +
                "id_usuario=" + id_usuario +
                ", nombre='" + nombre + '\'' +
                ", correo='" + correo + '\'' +
                ", contrasenia='" + contrasenia + '\'' +
                ", telefono='" + telefono + '\'' +
                ", sobre_mi='" + sobre_mi + '\'' +
                ", fecha_registro=" + fecha_registro +
                '}';
    } // FIN TO STRING
} // fin Usuario



