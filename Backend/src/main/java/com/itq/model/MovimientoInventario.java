package com.itq.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class MovimientoInventario {
    private UUID idMovimiento;
    private UUID idEmpresa;
    private UUID idProducto;
    private UUID idFactura;
    private String tipo;
    private Integer cantidad;
    private OffsetDateTime fecha;

    public MovimientoInventario() {}

    public UUID getIdMovimiento() { return idMovimiento; }
    public void setIdMovimiento(UUID idMovimiento) { this.idMovimiento = idMovimiento; }
    public UUID getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(UUID idEmpresa) { this.idEmpresa = idEmpresa; }
    public UUID getIdProducto() { return idProducto; }
    public void setIdProducto(UUID idProducto) { this.idProducto = idProducto; }
    public UUID getIdFactura() { return idFactura; }
    public void setIdFactura(UUID idFactura) { this.idFactura = idFactura; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public OffsetDateTime getFecha() { return fecha; }
    public void setFecha(OffsetDateTime fecha) { this.fecha = fecha; }
}
