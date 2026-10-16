package com.itq.service;

import com.itq.dao.EspecieDAO;
import com.itq.model.Especie;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EspecieService {
    private final EspecieDAO dao = new EspecieDAO();
    public List<Especie> listar() throws SQLException { return dao.listar(); }
    public Optional<Especie> buscarPorId(Integer idEspecie) throws SQLException { return dao.buscarPorId(idEspecie); }
    public Especie crear(Especie obj) throws SQLException { validar(obj); return dao.insertar(obj); }
    public boolean actualizar(Especie obj) throws SQLException { validar(obj); return dao.actualizar(obj); }
    public boolean eliminar(Integer idEspecie) throws SQLException { return dao.eliminar(idEspecie); }
    private void validar(Especie obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos de la especie son obligatorios");
        if (vacio(obj.getNombre())) throw new IllegalArgumentException("El nombre de la especie es obligatorio");
        obj.setNombre(obj.getNombre().trim());
    }
    private boolean vacio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
