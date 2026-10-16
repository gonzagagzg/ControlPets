package com.itq.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Panel estadístico global de la plataforma (solo SuperUsuario).
 *
 * Agrupa, en una sola respuesta, los indicadores que el SuperUsuario ve en
 * /app: totales agregados, una serie mensual (últimos 12 meses) y varios
 * desgloses (citas por estado, usuarios por rol, mascotas por especie,
 * empresas por estado y el ranking de veterinarias por facturación).
 *
 * Todos los conteos excluyen la Empresa Matriz (el hogar técnico del
 * SuperUsuario), que no es una veterinaria real.
 */
public class EstadisticasPlataforma {

    private Totales totales;
    private List<PuntoSerie> serieMensual;
    private List<Conteo> citasPorEstado;
    private List<Conteo> usuariosPorRol;
    private List<Conteo> mascotasPorEspecie;
    private List<Conteo> empresasPorEstado;
    private List<EmpresaRanking> topEmpresas;
    private String generadoEn;

    public Totales getTotales() {
        return totales;
    }

    public void setTotales(Totales totales) {
        this.totales = totales;
    }

    public List<PuntoSerie> getSerieMensual() {
        return serieMensual;
    }

    public void setSerieMensual(List<PuntoSerie> serieMensual) {
        this.serieMensual = serieMensual;
    }

    public List<Conteo> getCitasPorEstado() {
        return citasPorEstado;
    }

    public void setCitasPorEstado(List<Conteo> citasPorEstado) {
        this.citasPorEstado = citasPorEstado;
    }

    public List<Conteo> getUsuariosPorRol() {
        return usuariosPorRol;
    }

    public void setUsuariosPorRol(List<Conteo> usuariosPorRol) {
        this.usuariosPorRol = usuariosPorRol;
    }

    public List<Conteo> getMascotasPorEspecie() {
        return mascotasPorEspecie;
    }

    public void setMascotasPorEspecie(List<Conteo> mascotasPorEspecie) {
        this.mascotasPorEspecie = mascotasPorEspecie;
    }

    public List<Conteo> getEmpresasPorEstado() {
        return empresasPorEstado;
    }

    public void setEmpresasPorEstado(List<Conteo> empresasPorEstado) {
        this.empresasPorEstado = empresasPorEstado;
    }

    public List<EmpresaRanking> getTopEmpresas() {
        return topEmpresas;
    }

    public void setTopEmpresas(List<EmpresaRanking> topEmpresas) {
        this.topEmpresas = topEmpresas;
    }

    public String getGeneradoEn() {
        return generadoEn;
    }

    public void setGeneradoEn(String generadoEn) {
        this.generadoEn = generadoEn;
    }

    // =========================================================
    // INDICADORES GLOBALES
    // =========================================================

    public static class Totales {

        private long empresas;
        private long empresasActivas;
        private long empresasInactivas;
        private long usuarios;
        private long usuariosPendientes;
        private long clientes;
        private long mascotas;
        private long citas;
        private long citasPendientes;
        private long historiales;
        private long productos;
        private long productosBajoStock;
        private long facturas;
        private BigDecimal totalFacturado;
        private BigDecimal ticketPromedio;

        public long getEmpresas() {
            return empresas;
        }

        public void setEmpresas(long empresas) {
            this.empresas = empresas;
        }

        public long getEmpresasActivas() {
            return empresasActivas;
        }

        public void setEmpresasActivas(long empresasActivas) {
            this.empresasActivas = empresasActivas;
        }

        public long getEmpresasInactivas() {
            return empresasInactivas;
        }

        public void setEmpresasInactivas(long empresasInactivas) {
            this.empresasInactivas = empresasInactivas;
        }

        public long getUsuarios() {
            return usuarios;
        }

        public void setUsuarios(long usuarios) {
            this.usuarios = usuarios;
        }

        public long getUsuariosPendientes() {
            return usuariosPendientes;
        }

        public void setUsuariosPendientes(long usuariosPendientes) {
            this.usuariosPendientes = usuariosPendientes;
        }

        public long getClientes() {
            return clientes;
        }

        public void setClientes(long clientes) {
            this.clientes = clientes;
        }

        public long getMascotas() {
            return mascotas;
        }

        public void setMascotas(long mascotas) {
            this.mascotas = mascotas;
        }

        public long getCitas() {
            return citas;
        }

        public void setCitas(long citas) {
            this.citas = citas;
        }

        public long getCitasPendientes() {
            return citasPendientes;
        }

        public void setCitasPendientes(long citasPendientes) {
            this.citasPendientes = citasPendientes;
        }

        public long getHistoriales() {
            return historiales;
        }

        public void setHistoriales(long historiales) {
            this.historiales = historiales;
        }

        public long getProductos() {
            return productos;
        }

        public void setProductos(long productos) {
            this.productos = productos;
        }

        public long getProductosBajoStock() {
            return productosBajoStock;
        }

        public void setProductosBajoStock(long productosBajoStock) {
            this.productosBajoStock = productosBajoStock;
        }

        public long getFacturas() {
            return facturas;
        }

        public void setFacturas(long facturas) {
            this.facturas = facturas;
        }

        public BigDecimal getTotalFacturado() {
            return totalFacturado;
        }

        public void setTotalFacturado(BigDecimal totalFacturado) {
            this.totalFacturado = totalFacturado;
        }

        public BigDecimal getTicketPromedio() {
            return ticketPromedio;
        }

        public void setTicketPromedio(BigDecimal ticketPromedio) {
            this.ticketPromedio = ticketPromedio;
        }
    }

    // =========================================================
    // SERIE MENSUAL (ÚLTIMOS 12 MESES)
    // =========================================================

    public static class PuntoSerie {

        private String periodo;
        private BigDecimal facturado;
        private long facturas;
        private long citas;
        private long movimientos;

        public String getPeriodo() {
            return periodo;
        }

        public void setPeriodo(String periodo) {
            this.periodo = periodo;
        }

        public BigDecimal getFacturado() {
            return facturado;
        }

        public void setFacturado(BigDecimal facturado) {
            this.facturado = facturado;
        }

        public long getFacturas() {
            return facturas;
        }

        public void setFacturas(long facturas) {
            this.facturas = facturas;
        }

        public long getCitas() {
            return citas;
        }

        public void setCitas(long citas) {
            this.citas = citas;
        }

        public long getMovimientos() {
            return movimientos;
        }

        public void setMovimientos(long movimientos) {
            this.movimientos = movimientos;
        }
    }

    // =========================================================
    // DESGLOSES GENÉRICOS (etiqueta -> valor)
    // =========================================================

    public static class Conteo {

        private String etiqueta;
        private long valor;

        public Conteo() {
        }

        public Conteo(String etiqueta, long valor) {
            this.etiqueta = etiqueta;
            this.valor = valor;
        }

        public String getEtiqueta() {
            return etiqueta;
        }

        public void setEtiqueta(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        public long getValor() {
            return valor;
        }

        public void setValor(long valor) {
            this.valor = valor;
        }
    }

    // =========================================================
    // RANKING DE VETERINARIAS
    // =========================================================

    public static class EmpresaRanking {

        private String idEmpresa;
        private String razonSocial;
        private boolean activo;
        private BigDecimal facturado;
        private long facturas;
        private long clientes;
        private long mascotas;

        public String getIdEmpresa() {
            return idEmpresa;
        }

        public void setIdEmpresa(String idEmpresa) {
            this.idEmpresa = idEmpresa;
        }

        public String getRazonSocial() {
            return razonSocial;
        }

        public void setRazonSocial(String razonSocial) {
            this.razonSocial = razonSocial;
        }

        public boolean isActivo() {
            return activo;
        }

        public void setActivo(boolean activo) {
            this.activo = activo;
        }

        public BigDecimal getFacturado() {
            return facturado;
        }

        public void setFacturado(BigDecimal facturado) {
            this.facturado = facturado;
        }

        public long getFacturas() {
            return facturas;
        }

        public void setFacturas(long facturas) {
            this.facturas = facturas;
        }

        public long getClientes() {
            return clientes;
        }

        public void setClientes(long clientes) {
            this.clientes = clientes;
        }

        public long getMascotas() {
            return mascotas;
        }

        public void setMascotas(long mascotas) {
            this.mascotas = mascotas;
        }
    }
}
