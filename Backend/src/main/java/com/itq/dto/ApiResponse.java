package com.itq.dto;

public class ApiResponse<T> {
    private final boolean exito;
    private final String mensaje;
    private final T datos;

    public ApiResponse(boolean exito, String mensaje, T datos) {
        this.exito = exito; this.mensaje = mensaje; this.datos = datos;
    }
    public static <T> ApiResponse<T> ok(String mensaje, T datos) { return new ApiResponse<>(true, mensaje, datos); }
    public static <T> ApiResponse<T> error(String mensaje) { return new ApiResponse<>(false, mensaje, null); }
    public boolean isExito() { return exito; }
    public String getMensaje() { return mensaje; }
    public T getDatos() { return datos; }
}
