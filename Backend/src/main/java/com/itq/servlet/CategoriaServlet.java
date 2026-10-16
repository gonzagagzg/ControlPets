package com.itq.servlet;

import com.itq.dto.ApiResponse;
import com.itq.model.Categoria;
import com.itq.security.Autorizacion;
import com.itq.service.CategoriaService;
import com.itq.util.HttpUtil;
import com.itq.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet("/api/categorias/*")
public class CategoriaServlet extends HttpServlet {

    private final CategoriaService service =
            new CategoriaService();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            UUID idEmpresa =
                    obtenerIdEmpresa(req);

            boolean superUsuario =
                    esSuperUsuario(req);

            String raw =
                    valorId(req);

            // GET /api/categorias
            if (raw == null) {

                Autorizacion.exigir(
                        req,
                        "CATEGORIAS",
                        "LISTAR"
                );

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Listado",
                                service.listar(
                                        idEmpresa,
                                        superUsuario
                                )
                        )
                );

                return;
            }

            // GET /api/categorias/{id}
            Autorizacion.exigir(
                    req,
                    "CATEGORIAS",
                    "VER"
            );

            Integer id =
                    Integer.valueOf(raw);

            var encontrado =
                    service.buscarPorId(
                            id,
                            idEmpresa,
                            superUsuario
                    );

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

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            Autorizacion.exigir(
                    req,
                    "CATEGORIAS",
                    "CREAR"
            );

            UUID idEmpresa =
                    obtenerIdEmpresa(req);

            boolean superUsuario =
                    esSuperUsuario(req);

            Categoria obj =
                    JsonUtil.gson()
                            .fromJson(
                                    req.getReader(),
                                    Categoria.class
                            );

            Categoria creada =
                    service.crear(
                            obj,
                            idEmpresa,
                            superUsuario
                    );

            HttpUtil.json(
                    resp,
                    201,
                    ApiResponse.ok(
                            "Registro creado",
                            creada
                    )
            );

        } catch (SecurityException e) {

            HttpUtil.error(
                    resp,
                    403,
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            HttpUtil.errorValidacion(resp, e);

        } catch (SQLException e) {

            HttpUtil.errorBaseDatos(resp, e);

        } catch (IllegalStateException e) {

            HttpUtil.errorInterno(resp, e);

        } catch (Exception e) {

            HttpUtil.errorSolicitud(resp, e);
        }
    }

    @Override
    protected void doPut(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            Autorizacion.exigir(
                    req,
                    "CATEGORIAS",
                    "EDITAR"
            );

            UUID idEmpresa =
                    obtenerIdEmpresa(req);

            boolean superUsuario =
                    esSuperUsuario(req);

            String raw =
                    valorId(req);

            if (raw == null) {

                HttpUtil.error(
                        resp,
                        400,
                        "Identificador obligatorio"
                );

                return;
            }

            Integer id =
                    Integer.valueOf(raw);

            Categoria obj =
                    JsonUtil.gson()
                            .fromJson(
                                    req.getReader(),
                                    Categoria.class
                            );

            if (obj == null) {

                HttpUtil.error(
                        resp,
                        400,
                        "Los datos de la categoría son obligatorios"
                );

                return;
            }

            obj.setIdCategoria(id);

            if (!service.actualizar(
                    obj,
                    idEmpresa,
                    superUsuario
            )) {

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
                            "Registro actualizado",
                            obj
                    )
            );

        } catch (SecurityException e) {

            HttpUtil.error(
                    resp,
                    403,
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            HttpUtil.errorValidacion(resp, e);

        } catch (SQLException e) {

            HttpUtil.errorBaseDatos(resp, e);

        } catch (IllegalStateException e) {

            HttpUtil.errorInterno(resp, e);
        }
    }

    @Override
    protected void doDelete(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            Autorizacion.exigir(
                    req,
                    "CATEGORIAS",
                    "ELIMINAR"
            );

            UUID idEmpresa =
                    obtenerIdEmpresa(req);

            boolean superUsuario =
                    esSuperUsuario(req);

            String raw =
                    valorId(req);

            if (raw == null) {

                HttpUtil.error(
                        resp,
                        400,
                        "Identificador obligatorio"
                );

                return;
            }

            Integer id =
                    Integer.valueOf(raw);

            if (!service.eliminar(
                    id,
                    idEmpresa,
                    superUsuario
            )) {

                HttpUtil.error(
                        resp,
                        404,
                        "Registro no encontrado"
                );

                return;
            }

            resp.setStatus(
                    HttpServletResponse.SC_NO_CONTENT
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

    private UUID obtenerIdEmpresa(
            HttpServletRequest req
    ) {

        return (UUID)
                req.getAttribute("idEmpresa");
    }

    private boolean esSuperUsuario(
            HttpServletRequest req
    ) {

        String rol =
                (String)
                        req.getAttribute("rol");

        return rol != null &&
                rol.equalsIgnoreCase(
                        "SuperUsuario"
                );
    }

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