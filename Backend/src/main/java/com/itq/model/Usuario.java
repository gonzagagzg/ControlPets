package com.itq.model;

import java.util.UUID;

public class Usuario {

    private UUID idUsuario;
    private UUID idEmpresa;
    private Integer idRol;
    /**
     * Nombre del rol resuelto en el servidor (`idRol` -> `rol.nombre`). Se
     * rellena en los listados (ver UsuarioService.conNombreRol) para que la UI
     * pueda mostrar el rol aunque GET /api/roles lo omita por ser reservado.
     */
    private String nombreRol;
    private String usuario;
    private String claveHash;
    private String nombres;
    private Boolean activo;
    private String tipobloqueo;
    private String notificacion;
    private String correo;
    /**
     * Estado de confirmación del correo de la cuenta:
     * 'pendiente' (aún no confirmó el enlace) o 'activo' (confirmado, o
     * creada sin correo). Independiente de {@link #activo}, que es el
     * bloqueo/desbloqueo administrativo.
     */
    private String estadoCuenta;

    public Usuario() {}

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

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getClaveHash() {
        return claveHash;
    }

    public void setClaveHash(String claveHash) {
        this.claveHash = claveHash;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public Boolean isActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getTipobloqueo() {
        return tipobloqueo;
    }

    public void setTipobloqueo(String tipobloqueo) {
        this.tipobloqueo = tipobloqueo;
    }

    public String getNotificacion() {
        return notificacion;
    }

    public void setNotificacion(String notificacion) {
        this.notificacion = notificacion;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getEstadoCuenta() {
        return estadoCuenta;
    }

    public void setEstadoCuenta(String estadoCuenta) {
        this.estadoCuenta = estadoCuenta;
    }
}