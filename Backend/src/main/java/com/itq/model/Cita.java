package com.itq.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Cita {
    private UUID idCita;
    private UUID idEmpresa;
    private UUID idMascota;
    private UUID idVeterinario;
    private OffsetDateTime fechaHora;
    private String estado;

    public Cita() {}

    public UUID getIdCita() { return idCita; }
    public void setIdCita(UUID idCita) { this.idCita = idCita; }
    public UUID getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(UUID idEmpresa) { this.idEmpresa = idEmpresa; }
    public UUID getIdMascota() { return idMascota; }
    public void setIdMascota(UUID idMascota) { this.idMascota = idMascota; }
    public UUID getIdVeterinario() { return idVeterinario; }
    public void setIdVeterinario(UUID idVeterinario) { this.idVeterinario = idVeterinario; }
    public OffsetDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(OffsetDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
