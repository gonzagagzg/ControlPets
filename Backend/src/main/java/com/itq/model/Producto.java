package com.itq.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class Producto {
    private UUID idProducto;
    private UUID idEmpresa;
    private Integer idCategoria;
    private Integer idIva;
    private String nombre;
    private BigDecimal precioUnitario;
    private Integer stockActual;
    private Integer stockMinimo;
    private LocalDate fechaCaducidad;

    public Producto() {}

    public UUID getIdProducto() { return idProducto; }
    public void setIdProducto(UUID idProducto) { this.idProducto = idProducto; }
    public UUID getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(UUID idEmpresa) { this.idEmpresa = idEmpresa; }
    public Integer getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }
    public Integer getIdIva() { return idIva; }
    public void setIdIva(Integer idIva) { this.idIva = idIva; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public Integer getStockActual() { return stockActual; }
    public void setStockActual(Integer stockActual) { this.stockActual = stockActual; }
    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }
    public LocalDate getFechaCaducidad() { return fechaCaducidad; }
    public void setFechaCaducidad(LocalDate fechaCaducidad) { this.fechaCaducidad = fechaCaducidad; }
}
