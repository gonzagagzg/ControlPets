package com.itq.dto;

import java.util.UUID;

public class LoginResponse {

    private String token;
    private UUID idUsuario;
    private UUID idEmpresa;
    private Integer idRol;
    private String rol;
    private String nombres;
    private String usuario;

    public LoginResponse() {
    }

    public LoginResponse(
            String token,
            UUID idUsuario,
            UUID idEmpresa,
            Integer idRol,
            String rol,
            String nombres,
            String usuario
    ) {
        this.token = token;
        this.idUsuario = idUsuario;
        this.idEmpresa = idEmpresa;
        this.idRol = idRol;
        this.rol = rol;
        this.nombres = nombres;
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

    public UUID getIdUsuario() {
        return idUsuario;
    }

    public UUID getIdEmpresa() {
        return idEmpresa;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public String getRol() {
        return rol;
    }

    public String getNombres() {
        return nombres;
    }

    public String getUsuario() {
        return usuario;
    }
}