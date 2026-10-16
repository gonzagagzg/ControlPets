package com.itq.model;

import java.math.BigDecimal;
import java.util.UUID;

public class FacturaDetalle {
    private UUID idDetalle;
    private UUID idFactura;
    private UUID idProducto;
    private Integer idIva;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public FacturaDetalle() {}

    public UUID getIdDetalle() { return idDetalle; }
    public void setIdDetalle(UUID idDetalle) { this.idDetalle = idDetalle; }
    public UUID getIdFactura() { return idFactura; }
    public void setIdFactura(UUID idFactura) { this.idFactura = idFactura; }
    public UUID getIdProducto() { return idProducto; }
    public void setIdProducto(UUID idProducto) { this.idProducto = idProducto; }
    public Integer getIdIva() { return idIva; }
    public void setIdIva(Integer idIva) { this.idIva = idIva; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
