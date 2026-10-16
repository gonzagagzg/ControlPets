package com.itq.service;

import com.itq.dao.SriIvaDAO;
import com.itq.model.SriIva;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SriIvaService {
    private final SriIvaDAO dao = new SriIvaDAO();
    public List<SriIva> listar() throws SQLException { return dao.listar(); }
    public Optional<SriIva> buscarPorId(Integer idIva) throws SQLException { return dao.buscarPorId(idIva); }
    public SriIva crear(SriIva obj) throws SQLException { validar(obj); return dao.insertar(obj); }
    public boolean actualizar(SriIva obj) throws SQLException { validar(obj); return dao.actualizar(obj); }
    public boolean eliminar(Integer idIva) throws SQLException { return dao.eliminar(idIva); }
    private void validar(SriIva obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos del IVA son obligatorios");
        if (obj.getPorcentaje() == null || obj.getPorcentaje().compareTo(BigDecimal.ZERO) < 0 || obj.getPorcentaje().compareTo(new BigDecimal("100")) > 0) throw new IllegalArgumentException("El porcentaje de IVA debe estar entre 0 y 100");
        if (vacio(obj.getCodigoSri())) throw new IllegalArgumentException("El código SRI es obligatorio");
        obj.setCodigoSri(obj.getCodigoSri().trim());
    }
    private boolean vacio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
