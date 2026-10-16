package com.itq.service;

import com.itq.config.ConexionBD;
import com.itq.dao.EstadisticasDAO;
import com.itq.dto.EstadisticasPlataforma;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;

/**
 * Arma el panel estadístico global que consume el SuperUsuario en /app.
 * Solo lectura: delega cada agregación al {@link EstadisticasDAO}.
 */
public class EstadisticasService {

    /** Meses que abarca la serie temporal del panel. */
    private static final int MESES_SERIE = 12;

    /** Número máximo de veterinarias en el ranking por facturación. */
    private static final int LIMITE_RANKING = 10;

    private final EstadisticasDAO dao =
            new EstadisticasDAO();

    public EstadisticasPlataforma obtenerPanel()
            throws SQLException {

        try (Connection cn =
                     ConexionBD.obtenerConexion()) {

            EstadisticasPlataforma panel =
                    new EstadisticasPlataforma();

            panel.setTotales(
                    dao.totales(cn)
            );

            panel.setSerieMensual(
                    dao.serieMensual(cn, MESES_SERIE)
            );

            panel.setCitasPorEstado(
                    dao.citasPorEstado(cn)
            );

            panel.setUsuariosPorRol(
                    dao.usuariosPorRol(cn)
            );

            panel.setMascotasPorEspecie(
                    dao.mascotasPorEspecie(cn)
            );

            panel.setEmpresasPorEstado(
                    dao.empresasPorEstado(cn)
            );

            panel.setTopEmpresas(
                    dao.topEmpresas(cn, LIMITE_RANKING)
            );

            panel.setGeneradoEn(
                    OffsetDateTime.now().toString()
            );

            return panel;
        }
    }
}
