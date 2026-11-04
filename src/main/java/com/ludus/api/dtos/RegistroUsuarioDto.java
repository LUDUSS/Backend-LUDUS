package com.ludus.api.dtos;

public class RegistroUsuarioDto {
    private String nombre;
    private String correo;
    private String contrasenia;
    private String telefono;

    public RegistroUsuarioDto(String nombre, String correo, String contrasenia, String telefono) {
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenia = contrasenia;
        this.telefono = telefono;
    }// constructor

    public RegistroUsuarioDto(){}// constructor vacío

    public String getNombre() {
        return nombre;
    }// getNombre

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }// setNombre

    public String getCorreo() {
        return correo;
    }// getCorreo

    public void setCorreo(String correo) {
        this.correo = correo;
    }// setCorreo

    public String getContrasenia() {
        return contrasenia;
    }// getContrasenia

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }// setContrasenia

    public String getTelefono() {
        return telefono;
    }// getTelefono

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }// setTelefono
}
