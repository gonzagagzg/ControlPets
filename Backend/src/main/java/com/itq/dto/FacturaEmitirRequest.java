package com.itq.dto;

import com.itq.model.FacturaDetalle;

import java.util.List;
import java.util.UUID;

public class FacturaEmitirRequest {

    private UUID idCliente;
    private List<FacturaDetalle> detalles;

    public FacturaEmitirRequest() {
    }

    public UUID getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(UUID idCliente) {
        this.idCliente = idCliente;
    }

    public List<FacturaDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<FacturaDetalle> detalles) {
        this.detalles = detalles;
    }
}