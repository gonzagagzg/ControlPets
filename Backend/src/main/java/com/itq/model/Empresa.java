package com.itq.model;

import java.util.UUID;

public class Empresa {
    private UUID idEmpresa;
    private String ruc;
    private String razonSocial;
    private String direccion;
    private String correo;
    private String telefono;
    private Boolean activo;

    public Empresa() {}

    public UUID getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(UUID idEmpresa) { this.idEmpresa = idEmpresa; }
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public Boolean isActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
