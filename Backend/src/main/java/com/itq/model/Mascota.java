package com.itq.model;

import java.time.LocalDate;
import java.util.UUID;

public class Mascota {
    private UUID idMascota;
    private UUID idEmpresa;
    private UUID idCliente;
    private Integer idRaza;
    private String nombre;
    private LocalDate fechaNacimiento;

    public Mascota() {}

    public UUID getIdMascota() { return idMascota; }
    public void setIdMascota(UUID idMascota) { this.idMascota = idMascota; }
    public UUID getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(UUID idEmpresa) { this.idEmpresa = idEmpresa; }
    public UUID getIdCliente() { return idCliente; }
    public void setIdCliente(UUID idCliente) { this.idCliente = idCliente; }
    public Integer getIdRaza() { return idRaza; }
    public void setIdRaza(Integer idRaza) { this.idRaza = idRaza; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
}
