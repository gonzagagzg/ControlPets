package com.itq.model;

import java.math.BigDecimal;
import java.util.UUID;

public class HistorialClinico {
    private UUID idHistorial;
    private UUID idEmpresa;
    private UUID idCita;
    private BigDecimal pesoKg;
    private BigDecimal temperaturaC;
    private String anamnesis;
    private String diagnostico;

    public HistorialClinico() {}

    public UUID getIdHistorial() { return idHistorial; }
    public void setIdHistorial(UUID idHistorial) { this.idHistorial = idHistorial; }
    public UUID getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(UUID idEmpresa) { this.idEmpresa = idEmpresa; }
    public UUID getIdCita() { return idCita; }
    public void setIdCita(UUID idCita) { this.idCita = idCita; }
    public BigDecimal getPesoKg() { return pesoKg; }
    public void setPesoKg(BigDecimal pesoKg) { this.pesoKg = pesoKg; }
    public BigDecimal getTemperaturaC() { return temperaturaC; }
    public void setTemperaturaC(BigDecimal temperaturaC) { this.temperaturaC = temperaturaC; }
    public String getAnamnesis() { return anamnesis; }
    public void setAnamnesis(String anamnesis) { this.anamnesis = anamnesis; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
}
