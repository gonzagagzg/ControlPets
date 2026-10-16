package com.itq.model;

import java.util.UUID;

public class Receta {
    private UUID idReceta;
    private UUID idEmpresa;
    private UUID idHistorial;
    private String indicacionesGenerales;

    public Receta() {}

    public UUID getIdReceta() { return idReceta; }
    public void setIdReceta(UUID idReceta) { this.idReceta = idReceta; }
    public UUID getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(UUID idEmpresa) { this.idEmpresa = idEmpresa; }
    public UUID getIdHistorial() { return idHistorial; }
    public void setIdHistorial(UUID idHistorial) { this.idHistorial = idHistorial; }
    public String getIndicacionesGenerales() { return indicacionesGenerales; }
    public void setIndicacionesGenerales(String indicacionesGenerales) { this.indicacionesGenerales = indicacionesGenerales; }
}
