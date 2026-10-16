package com.itq.service;

import com.itq.dao.RecetaDAO;
import com.itq.dao.RecetaDetalleDAO;
import com.itq.model.RecetaDetalle;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RecetaDetalleService {
    private final RecetaDetalleDAO dao = new RecetaDetalleDAO();
    private final RecetaDAO recetaDAO = new RecetaDAO();

    public List<RecetaDetalle> listar(UUID idEmpresa, boolean superUsuario) throws SQLException {
        if (superUsuario) return dao.listar();
        validarEmpresaSesion(idEmpresa);
        return dao.listarPorEmpresa(idEmpresa);
    }

    public Optional<RecetaDetalle> buscarPorId(UUID idDetalleReceta, UUID idEmpresa, boolean superUsuario) throws SQLException {
        if (superUsuario) return dao.buscarPorId(idDetalleReceta);
        validarEmpresaSesion(idEmpresa);
        return dao.buscarPorIdYEmpresa(idDetalleReceta, idEmpresa);
    }

    public RecetaDetalle crear(RecetaDetalle obj, UUID idEmpresa, boolean superUsuario) throws SQLException {
        if (obj == null) throw new IllegalArgumentException("Los datos del detalle de receta son obligatorios");

        if (superUsuario) {
            validar(obj);
            if (recetaDAO.buscarPorId(obj.getIdReceta()).isEmpty()) {
                throw new IllegalArgumentException("La receta no existe");
            }
            return dao.insertar(obj);
        }

        validarEmpresaSesion(idEmpresa);
        validar(obj);
        validarRecetaDeEmpresa(obj.getIdReceta(), idEmpresa);
        return dao.insertar(obj);
    }

    public boolean actualizar(RecetaDetalle obj, UUID idEmpresa, boolean superUsuario) throws SQLException {
        if (obj == null || obj.getIdDetalleReceta() == null) {
            throw new IllegalArgumentException("Los datos del detalle de receta son obligatorios");
        }

        if (superUsuario) {
            validar(obj);
            return dao.actualizar(obj);
        }

        validarEmpresaSesion(idEmpresa);

        if (dao.buscarPorIdYEmpresa(obj.getIdDetalleReceta(), idEmpresa).isEmpty()) return false;

        validar(obj);
        validarRecetaDeEmpresa(obj.getIdReceta(), idEmpresa);
        return dao.actualizarPorEmpresa(obj, idEmpresa);
    }

    public boolean eliminar(UUID idDetalleReceta, UUID idEmpresa, boolean superUsuario) throws SQLException {
        if (superUsuario) return dao.eliminar(idDetalleReceta);
        validarEmpresaSesion(idEmpresa);
        return dao.eliminarPorEmpresa(idDetalleReceta, idEmpresa);
    }

    private void validarRecetaDeEmpresa(UUID idReceta, UUID idEmpresa) throws SQLException {
        if (idReceta == null) throw new IllegalArgumentException("La receta es obligatoria");
        if (recetaDAO.buscarPorIdYEmpresa(idReceta, idEmpresa).isEmpty()) {
            throw new IllegalArgumentException("La receta no pertenece a la empresa indicada");
        }
    }

    private void validarEmpresaSesion(UUID idEmpresa) {
        if (idEmpresa == null) {
            throw new SecurityException("El usuario no tiene una empresa asignada");
        }
    }

    private void validar(RecetaDetalle obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos del detalle de receta son obligatorios");
        if (obj.getIdReceta() == null) throw new IllegalArgumentException("La receta es obligatoria");
        if (obj.getIdProducto() == null) throw new IllegalArgumentException("El producto es obligatorio");
        if (vacio(obj.getDosis())) throw new IllegalArgumentException("La dosis es obligatoria");
        if (vacio(obj.getFrecuencia())) throw new IllegalArgumentException("La frecuencia es obligatoria");
        if (obj.getDuracionDias() == null || obj.getDuracionDias() <= 0) throw new IllegalArgumentException("La duración debe ser mayor que cero");
        obj.setDosis(obj.getDosis().trim());
        obj.setFrecuencia(obj.getFrecuencia().trim());
    }
    private boolean vacio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
