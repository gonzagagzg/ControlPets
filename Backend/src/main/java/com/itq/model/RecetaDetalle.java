package com.itq.model;

import java.util.UUID;

public class RecetaDetalle {
    private UUID idDetalleReceta;
    private UUID idReceta;
    private UUID idProducto;
    private String dosis;
    private String frecuencia;
    private Integer duracionDias;

    public RecetaDetalle() {}

    public UUID getIdDetalleReceta() { return idDetalleReceta; }
    public void setIdDetalleReceta(UUID idDetalleReceta) { this.idDetalleReceta = idDetalleReceta; }
    public UUID getIdReceta() { return idReceta; }
    public void setIdReceta(UUID idReceta) { this.idReceta = idReceta; }
    public UUID getIdProducto() { return idProducto; }
    public void setIdProducto(UUID idProducto) { this.idProducto = idProducto; }
    public String getDosis() { return dosis; }
    public void setDosis(String dosis) { this.dosis = dosis; }
    public String getFrecuencia() { return frecuencia; }
    public void setFrecuencia(String frecuencia) { this.frecuencia = frecuencia; }
    public Integer getDuracionDias() { return duracionDias; }
    public void setDuracionDias(Integer duracionDias) { this.duracionDias = duracionDias; }
}
