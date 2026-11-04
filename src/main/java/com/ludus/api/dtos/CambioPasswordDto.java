package com.ludus.api.dtos;

public class CambioPasswordDto {

    private String passwordActual;
    private String passwordNueva;

    public CambioPasswordDto() {}// constructor vacío

    public String getPasswordActual() {
        return passwordActual;
    }// getPasswordActual

    public void setPasswordActual(String passwordActual) {
        this.passwordActual = passwordActual;
    }// setPasswordActual

    public String getPasswordNueva() {
        return passwordNueva;
    }// getPasswordNueva

    public void setPasswordNueva(String passwordNueva) {
        this.passwordNueva = passwordNueva;
    }// setPasswordNueva
}
