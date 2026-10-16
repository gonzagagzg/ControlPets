package com.itq.dto;

public class EmpresaConAdminRequest {

    private String ruc;
    private String razonSocial;
    private String direccion;
    private String correo;
    private String telefono;
    private Boolean activo;

    private String adminUsuario;
    private String adminNombres;
    private String adminContrasena;

    public EmpresaConAdminRequest() {
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getAdminUsuario() {
        return adminUsuario;
    }

    public void setAdminUsuario(String adminUsuario) {
        this.adminUsuario = adminUsuario;
    }

    public String getAdminNombres() {
        return adminNombres;
    }

    public void setAdminNombres(String adminNombres) {
        this.adminNombres = adminNombres;
    }

    public String getAdminContrasena() {
        return adminContrasena;
    }

    public void setAdminContrasena(String adminContrasena) {
        this.adminContrasena = adminContrasena;
    }
}