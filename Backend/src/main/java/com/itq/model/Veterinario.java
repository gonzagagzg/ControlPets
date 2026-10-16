package com.itq.model;

import java.util.UUID;

public class Veterinario {

    private UUID idVeterinario;
    private UUID idUsuario;
    private UUID idEmpresa;

    private String codigoVeterinario;
    private String especialidad;

    private String usuario;
    private String nombres;

    public Veterinario() {
    }

    public UUID getIdVeterinario() {
        return idVeterinario;
    }

    public void setIdVeterinario(UUID idVeterinario) {
        this.idVeterinario = idVeterinario;
    }

    public UUID getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(UUID idUsuario) {
        this.idUsuario = idUsuario;
    }

    public UUID getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(UUID idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getCodigoVeterinario() {
        return codigoVeterinario;
    }

    public void setCodigoVeterinario(String codigoVeterinario) {
        this.codigoVeterinario = codigoVeterinario;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }
}