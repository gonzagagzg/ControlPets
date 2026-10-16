package com.itq.model;

import java.util.UUID;

public class Cliente {

    private UUID idCliente;
    private UUID idEmpresa;
    private String tipoDocumento;
    private String identificacion;
    private String nombres;
    private String direccion;
    private String correo;
    private String telefono;

    public Cliente() {
    }

    public UUID getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(
            UUID idCliente
    ) {
        this.idCliente = idCliente;
    }

    public UUID getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(
            UUID idEmpresa
    ) {
        this.idEmpresa = idEmpresa;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(
            String tipoDocumento
    ) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(
            String identificacion
    ) {
        this.identificacion = identificacion;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(
            String nombres
    ) {
        this.nombres = nombres;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(
            String direccion
    ) {
        this.direccion = direccion;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(
            String correo
    ) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(
            String telefono
    ) {
        this.telefono = telefono;
    }
}