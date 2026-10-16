package com.itq.servlet;

import com.itq.dto.ApiResponse;
import com.itq.security.Autorizacion;
import com.itq.service.FacturaDetalleService;
import com.itq.util.HttpUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet("/api/factura-detalles/*")
public class FacturaDetalleServlet extends HttpServlet {

    private final FacturaDetalleService service =
            new FacturaDetalleService();

    // =========================================================
    // CONSULTAR DETALLES
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            /*
             * Los detalles de factura pueden consultarse
             * únicamente si el usuario tiene permiso
             * para visualizar facturas.
             */
            Autorizacion.exigir(
                    req,
                    "FACTURAS",
                    "VER"
            );

            String raw =
                    valorId(req);

            // GET /api/factura-detalles
            if (raw == null) {

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Listado",
                                service.listar()
                        )
                );

                return;
            }

            // GET /api/factura-detalles/{id}
            UUID id =
                    UUID.fromString(raw);

            var encontrado =
                    service.buscarPorId(id);

            if (encontrado.isEmpty()) {

                HttpUtil.error(
                        resp,
                        404,
                        "Registro no encontrado"
                );

                return;
            }

            HttpUtil.json(
                    resp,
                    200,
                    ApiResponse.ok(
                            "Registro encontrado",
                            encontrado.get()
                    )
            );

        } catch (SecurityException e) {

            HttpUtil.error(
                    resp,
                    403,
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            HttpUtil.error(
                    resp,
                    400,
                    "Identificador inválido"
            );

        } catch (SQLException e) {

            HttpUtil.errorBaseDatos(resp, e);

        } catch (IllegalStateException e) {

            HttpUtil.errorInterno(resp, e);
        }
    }

    // =========================================================
    // CREAR DETALLE DIRECTAMENTE - BLOQUEADO
    // =========================================================

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        HttpUtil.error(
                resp,
                405,
                "Los detalles de factura únicamente pueden generarse mediante la emisión transaccional de la factura"
        );
    }

    // =========================================================
    // MODIFICAR DETALLE DIRECTAMENTE - BLOQUEADO
    // =========================================================

    @Override
    protected void doPut(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        HttpUtil.error(
                resp,
                405,
                "Los detalles de una factura emitida no pueden modificarse directamente"
        );
    }

    // =========================================================
    // ELIMINAR DETALLE DIRECTAMENTE - BLOQUEADO
    // =========================================================

    @Override
    protected void doDelete(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        HttpUtil.error(
                resp,
                405,
                "Los detalles de una factura emitida no pueden eliminarse directamente"
        );
    }

    // =========================================================
    // OBTENER ID DESDE URL
    // =========================================================

    private String valorId(
            HttpServletRequest req
    ) {

        String path =
                req.getPathInfo();

        if (path == null ||
                path.equals("/") ||
                path.isBlank()) {

            return null;
        }

        String valor =
                path.substring(1)
                        .split("/")[0];

        return valor.isBlank()
                ? null
                : valor;
    }
}