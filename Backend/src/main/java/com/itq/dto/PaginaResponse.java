package com.itq.dto;

import java.util.List;

public class PaginaResponse<T> {

    private List<T> contenido;
    private long totalRegistros;
    private int pagina;
    private int tamanio;
    private int totalPaginas;

    public PaginaResponse() {
    }

    public PaginaResponse(
            List<T> contenido,
            long totalRegistros,
            int pagina,
            int tamanio
    ) {
        this.contenido = contenido;
        this.totalRegistros = totalRegistros;
        this.pagina = pagina;
        this.tamanio = tamanio;

        this.totalPaginas =
                tamanio <= 0
                        ? 0
                        : (int) Math.ceil(
                        (double) totalRegistros
                                / tamanio
                );
    }

    public List<T> getContenido() {
        return contenido;
    }

    public void setContenido(List<T> contenido) {
        this.contenido = contenido;
    }

    public long getTotalRegistros() {
        return totalRegistros;
    }

    public void setTotalRegistros(long totalRegistros) {
        this.totalRegistros = totalRegistros;
    }

    public int getPagina() {
        return pagina;
    }

    public void setPagina(int pagina) {
        this.pagina = pagina;
    }

    public int getTamanio() {
        return tamanio;
    }

    public void setTamanio(int tamanio) {
        this.tamanio = tamanio;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public void setTotalPaginas(int totalPaginas) {
        this.totalPaginas = totalPaginas;
    }
}