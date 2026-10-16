package com.itq.model;

import java.time.LocalDate;
import java.util.UUID;

public class MascotaVacuna {
    private UUID idMascotaVacuna;
    private UUID idEmpresa;
    private UUID idMascota;
    private Integer idVacuna;
    private LocalDate fechaAplicacion;

    public MascotaVacuna() {}

    public UUID getIdMascotaVacuna() { return idMascotaVacuna; }
    public void setIdMascotaVacuna(UUID idMascotaVacuna) { this.idMascotaVacuna = idMascotaVacuna; }
    public UUID getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(UUID idEmpresa) { this.idEmpresa = idEmpresa; }
    public UUID getIdMascota() { return idMascota; }
    public void setIdMascota(UUID idMascota) { this.idMascota = idMascota; }
    public Integer getIdVacuna() { return idVacuna; }
    public void setIdVacuna(Integer idVacuna) { this.idVacuna = idVacuna; }
    public LocalDate getFechaAplicacion() { return fechaAplicacion; }
    public void setFechaAplicacion(LocalDate fechaAplicacion) { this.fechaAplicacion = fechaAplicacion; }
}
