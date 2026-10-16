package com.itq.dto;

public class ValidacionRucResponse {

    private String ruc;

    private boolean formatoValido;

    private boolean existeEnSri;

    private boolean registradoEnSistema;

    private boolean disponibleParaRegistro;

    private String mensaje;

    public ValidacionRucResponse() {
    }

    public ValidacionRucResponse(
            String ruc,
            boolean formatoValido,
            boolean existeEnSri,
            boolean registradoEnSistema,
            boolean disponibleParaRegistro,
            String mensaje
    ) {

        this.ruc = ruc;
        this.formatoValido = formatoValido;
        this.existeEnSri = existeEnSri;
        this.registradoEnSistema = registradoEnSistema;
        this.disponibleParaRegistro = disponibleParaRegistro;
        this.mensaje = mensaje;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public boolean isFormatoValido() {
        return formatoValido;
    }

    public void setFormatoValido(boolean formatoValido) {
        this.formatoValido = formatoValido;
    }

    public boolean isExisteEnSri() {
        return existeEnSri;
    }

    public void setExisteEnSri(boolean existeEnSri) {
        this.existeEnSri = existeEnSri;
    }

    public boolean isRegistradoEnSistema() {
        return registradoEnSistema;
    }

    public void setRegistradoEnSistema(boolean registradoEnSistema) {
        this.registradoEnSistema = registradoEnSistema;
    }

    public boolean isDisponibleParaRegistro() {
        return disponibleParaRegistro;
    }

    public void setDisponibleParaRegistro(
            boolean disponibleParaRegistro
    ) {
        this.disponibleParaRegistro = disponibleParaRegistro;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}