package com.itq.model;

import java.math.BigDecimal;

public class SriIva {
    private Integer idIva;
    private BigDecimal porcentaje;
    private String codigoSri;

    public SriIva() {}

    public Integer getIdIva() { return idIva; }
    public void setIdIva(Integer idIva) { this.idIva = idIva; }
    public BigDecimal getPorcentaje() { return porcentaje; }
    public void setPorcentaje(BigDecimal porcentaje) { this.porcentaje = porcentaje; }
    public String getCodigoSri() { return codigoSri; }
    public void setCodigoSri(String codigoSri) { this.codigoSri = codigoSri; }
}
