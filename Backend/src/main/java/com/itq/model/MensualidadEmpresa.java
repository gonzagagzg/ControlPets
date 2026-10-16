package com.itq.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class MensualidadEmpresa {

    private UUID idMensualidad;
    private UUID idEmpresa;
    private String periodo;
    private BigDecimal valor;
    private LocalDate fechaVencimiento;
    private LocalDate fechaPago;
    private String estado;
    private String observacion;
    private Boolean activo;

    public MensualidadEmpresa() {
    }

    public UUID getIdMensualidad() {
        return idMensualidad;
    }

    public void setIdMensualidad(
            UUID idMensualidad
    ) {
        this.idMensualidad = idMensualidad;
    }

    public UUID getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(
            UUID idEmpresa
    ) {
        this.idEmpresa = idEmpresa;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(
            String periodo
    ) {
        this.periodo = periodo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(
            BigDecimal valor
    ) {
        this.valor = valor;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(
            LocalDate fechaVencimiento
    ) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(
            LocalDate fechaPago
    ) {
        this.fechaPago = fechaPago;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(
            String estado
    ) {
        this.estado = estado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(
            String observacion
    ) {
        this.observacion = observacion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(
            Boolean activo
    ) {
        this.activo = activo;
    }
}