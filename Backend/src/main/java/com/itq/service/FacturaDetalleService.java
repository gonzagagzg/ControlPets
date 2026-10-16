package com.itq.service;

import com.itq.dao.FacturaDetalleDAO;
import com.itq.model.FacturaDetalle;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FacturaDetalleService {
    private final FacturaDetalleDAO dao = new FacturaDetalleDAO();
    public List<FacturaDetalle> listar() throws SQLException { return dao.listar(); }
    public Optional<FacturaDetalle> buscarPorId(UUID idDetalle) throws SQLException { return dao.buscarPorId(idDetalle); }
    public FacturaDetalle crear(FacturaDetalle obj) throws SQLException { validar(obj); return dao.insertar(obj); }
    public boolean actualizar(FacturaDetalle obj) throws SQLException { validar(obj); return dao.actualizar(obj); }
    public boolean eliminar(UUID idDetalle) throws SQLException { return dao.eliminar(idDetalle); }
    private void validar(FacturaDetalle obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos del detalle de factura son obligatorios");
        if (obj.getIdFactura() == null) throw new IllegalArgumentException("La factura es obligatoria");
        if (obj.getIdProducto() == null) throw new IllegalArgumentException("El producto es obligatorio");
        if (obj.getIdIva() == null || obj.getIdIva() <= 0) throw new IllegalArgumentException("El IVA es obligatorio");
        if (obj.getCantidad() == null || obj.getCantidad() <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        if (obj.getPrecioUnitario() == null || obj.getPrecioUnitario().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("El precio unitario no puede ser negativo");
        if (obj.getSubtotal() == null || obj.getSubtotal().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("El subtotal no puede ser negativo");
    }
}
