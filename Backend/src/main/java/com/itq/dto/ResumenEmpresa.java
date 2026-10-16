package com.itq.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class ResumenEmpresa {

    private UUID idEmpresa;
    private String ruc;
    private String razonSocial;
    private String direccion;
    private Boolean activo;

    private long totalUsuarios;
    private long totalVeterinarios;
    private long totalClientes;
    private long totalMascotas;
    private long totalCitas;
    private long totalHistoriales;
    private long totalProductos;
    private long totalRecetas;
    private long totalFacturas;
    private long totalMovimientosInventario;

    private BigDecimal totalFacturado;
    private long productosBajoStock;

    public ResumenEmpresa() {
    }

    public UUID getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(UUID idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public long getTotalUsuarios() {
        return totalUsuarios;
    }

    public void setTotalUsuarios(long totalUsuarios) {
        this.totalUsuarios = totalUsuarios;
    }

    public long getTotalVeterinarios() {
        return totalVeterinarios;
    }

    public void setTotalVeterinarios(long totalVeterinarios) {
        this.totalVeterinarios = totalVeterinarios;
    }

    public long getTotalClientes() {
        return totalClientes;
    }

    public void setTotalClientes(long totalClientes) {
        this.totalClientes = totalClientes;
    }

    public long getTotalMascotas() {
        return totalMascotas;
    }

    public void setTotalMascotas(long totalMascotas) {
        this.totalMascotas = totalMascotas;
    }

    public long getTotalCitas() {
        return totalCitas;
    }

    public void setTotalCitas(long totalCitas) {
        this.totalCitas = totalCitas;
    }

    public long getTotalHistoriales() {
        return totalHistoriales;
    }

    public void setTotalHistoriales(long totalHistoriales) {
        this.totalHistoriales = totalHistoriales;
    }

    public long getTotalProductos() {
        return totalProductos;
    }

    public void setTotalProductos(long totalProductos) {
        this.totalProductos = totalProductos;
    }

    public long getTotalRecetas() {
        return totalRecetas;
    }

    public void setTotalRecetas(long totalRecetas) {
        this.totalRecetas = totalRecetas;
    }

    public long getTotalFacturas() {
        return totalFacturas;
    }

    public void setTotalFacturas(long totalFacturas) {
        this.totalFacturas = totalFacturas;
    }

    public long getTotalMovimientosInventario() {
        return totalMovimientosInventario;
    }

    public void setTotalMovimientosInventario(long totalMovimientosInventario) {
        this.totalMovimientosInventario = totalMovimientosInventario;
    }

    public BigDecimal getTotalFacturado() {
        return totalFacturado;
    }

    public void setTotalFacturado(BigDecimal totalFacturado) {
        this.totalFacturado = totalFacturado;
    }

    public long getProductosBajoStock() {
        return productosBajoStock;
    }

    public void setProductosBajoStock(long productosBajoStock) {
        this.productosBajoStock = productosBajoStock;
    }
}