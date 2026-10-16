package com.itq.service;

import com.itq.dao.RazaDAO;
import com.itq.model.Raza;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class RazaService {
    private final RazaDAO dao = new RazaDAO();
    public List<Raza> listar() throws SQLException { return dao.listar(); }
    public Optional<Raza> buscarPorId(Integer idRaza) throws SQLException { return dao.buscarPorId(idRaza); }
    public Raza crear(Raza obj) throws SQLException { validar(obj); return dao.insertar(obj); }
    public boolean actualizar(Raza obj) throws SQLException { validar(obj); return dao.actualizar(obj); }
    public boolean eliminar(Integer idRaza) throws SQLException { return dao.eliminar(idRaza); }
    private void validar(Raza obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos de la raza son obligatorios");
        if (obj.getIdEspecie() == null || obj.getIdEspecie() <= 0) throw new IllegalArgumentException("La especie es obligatoria");
        if (vacio(obj.getNombre())) throw new IllegalArgumentException("El nombre de la raza es obligatorio");
        obj.setNombre(obj.getNombre().trim());
    }
    private boolean vacio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
