package com.itq.servlet;

import com.itq.dto.ApiResponse;
import com.itq.model.Usuario;
import com.itq.security.Autorizacion;
import com.itq.service.UsuarioService;
import com.itq.util.HttpUtil;
import com.itq.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet("/api/usuarios/*")
public class UsuarioServlet extends HttpServlet {

    private final UsuarioService service =
            new UsuarioService();

    // =========================================================
    // GET
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            UUID idEmpresa =
                    obtenerIdEmpresa(
                            req
                    );

            boolean superUsuario =
                    esSuperUsuario(
                            req
                    );

            String raw =
                    valorId(
                            req
                    );

            // =================================================
            // GET /api/usuarios
            // =================================================

            if (raw == null) {

                Autorizacion.exigir(
                        req,
                        "USUARIOS",
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

            // =================================================
            // GET /api/usuarios/veterinarios-disponibles
            // =================================================

            if (raw.equalsIgnoreCase(
                    "veterinarios-disponibles"
            )) {

                Autorizacion.exigir(
                        req,
                        "USUARIOS",
                        "LISTAR"
                );

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Usuarios veterinarios disponibles",
                                service.listarVeterinariosDisponibles(
                                        idEmpresa,
                                        superUsuario
                                )
                        )
                );

                return;
            }

            // =================================================
            // GET /api/usuarios/empresa/{idEmpresa}
            // =================================================

            if (raw.equalsIgnoreCase(
                    "empresa"
            )) {

                Autorizacion.exigir(
                        req,
                        "USUARIOS",
                        "LISTAR"
                );

                if (!superUsuario) {

                    throw new SecurityException(
                            "Esta operación requiere SuperUsuario"
                    );
                }

                String idEmpresaRaw =
                        segundoValor(
                                req
                        );

                if (idEmpresaRaw == null) {

                    HttpUtil.error(
                            resp,
                            400,
                            "La empresa es obligatoria"
                    );

                    return;
                }

                UUID empresaSeleccionada =
                        UUID.fromString(
                                idEmpresaRaw
                        );

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Usuarios de la empresa",
                                service.listarPorEmpresaSuperUsuario(
                                        empresaSeleccionada,
                                        true
                                )
                        )
                );

                return;
            }

            // =================================================
            // GET /api/usuarios/{id}
            // =================================================

            Autorizacion.exigir(
                    req,
                    "USUARIOS",
                    "VER"
            );

            UUID id =
                    UUID.fromString(
                            raw
                    );

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

            HttpUtil.errorValidacion(resp, e);

        } catch (SQLException e) {

            HttpUtil.errorBaseDatos(resp, e);

        } catch (IllegalStateException e) {

            HttpUtil.errorInterno(resp, e);
        }
    }

    // =========================================================
    // POST
    // =========================================================

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            Autorizacion.exigir(
                    req,
                    "USUARIOS",
                    "CREAR"
            );

            UUID idEmpresa =
                    obtenerIdEmpresa(
                            req
                    );

            boolean superUsuario =
                    esSuperUsuario(
                            req
                    );

            Usuario obj =
                    JsonUtil.gson()
                            .fromJson(
                                    req.getReader(),
                                    Usuario.class
                            );

            HttpUtil.json(
                    resp,
                    201,
                    ApiResponse.ok(
                            "Registro creado",
                            service.crear(
                                    obj,
                                    idEmpresa,
                                    superUsuario
                            )
                    )
            );

        } catch (SecurityException e) {

            HttpUtil.error(
                    resp,
                    403,
                    e.getMessage()
            );

        } catch (SQLException e) {

            HttpUtil.errorBaseDatos(resp, e);

        } catch (IllegalStateException e) {

            HttpUtil.errorInterno(resp, e);

        } catch (Exception e) {

            HttpUtil.errorSolicitud(resp, e);
        }
    }

    // =========================================================
    // PUT
    // =========================================================

    @Override
    protected void doPut(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            Autorizacion.exigir(
                    req,
                    "USUARIOS",
                    "EDITAR"
            );

            UUID idEmpresa =
                    obtenerIdEmpresa(
                            req
                    );

            boolean superUsuario =
                    esSuperUsuario(
                            req
                    );

            String raw =
                    valorId(
                            req
                    );

            if (raw == null) {

                HttpUtil.error(
                        resp,
                        400,
                        "Identificador obligatorio"
                );

                return;
            }

            String accion =
                    segundoValor(
                            req
                    );

            UUID id =
                    UUID.fromString(
                            raw
                    );

            // =================================================
            // PUT /api/usuarios/{id}/bloquear
            // =================================================

            if (accion != null &&
                    accion.equalsIgnoreCase("bloquear")) {

                java.util.Map<String, String> body =
                        JsonUtil.gson()
                                .fromJson(
                                        req.getReader(),
                                        java.util.Map.class
                                );

                String tipoBloqueo =
                        body != null
                                ? body.get("tipoBloqueo")
                                : null;

                if (!service.bloquear(
                        id,
                        tipoBloqueo,
                        idEmpresa,
                        superUsuario
                )) {

                    HttpUtil.error(
                            resp,
                            404,
                            "Usuario no encontrado"
                    );

                    return;
                }

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Usuario bloqueado",
                                null
                        )
                );

                return;
            }

            // =================================================
            // PUT /api/usuarios/{id}/desbloquear
            // =================================================

            if (accion != null &&
                    accion.equalsIgnoreCase("desbloquear")) {

                if (!service.desbloquear(
                        id,
                        idEmpresa,
                        superUsuario
                )) {

                    HttpUtil.error(
                            resp,
                            404,
                            "Usuario no encontrado"
                    );

                    return;
                }

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Usuario desbloqueado",
                                null
                        )
                );

                return;
            }

            // =================================================
            // PUT /api/usuarios/{id}
            // =================================================

            Usuario obj =
                    JsonUtil.gson()
                            .fromJson(
                                    req.getReader(),
                                    Usuario.class
                            );

            if (obj == null) {

                HttpUtil.error(
                        resp,
                        400,
                        "Los datos del usuario son obligatorios"
                );

                return;
            }

            obj.setIdUsuario(
                    id
            );

            if (!service.actualizar(
                    obj,
                    idEmpresa,
                    superUsuario,
                    obtenerIdUsuario(
                            req
                    )
            )) {

                HttpUtil.error(
                        resp,
                        404,
                        "Registro no encontrado"
                );

                return;
            }

            obj.setClaveHash(
                    null
            );

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

        } catch (SQLException e) {

            HttpUtil.errorBaseDatos(resp, e);

        } catch (IllegalStateException e) {

            HttpUtil.errorInterno(resp, e);

        } catch (Exception e) {

            HttpUtil.errorSolicitud(resp, e);
        }
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Override
    protected void doDelete(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            Autorizacion.exigir(
                    req,
                    "USUARIOS",
                    "ELIMINAR"
            );

            UUID idEmpresa =
                    obtenerIdEmpresa(
                            req
                    );

            boolean superUsuario =
                    esSuperUsuario(
                            req
                    );

            String raw =
                    valorId(
                            req
                    );

            if (raw == null) {

                HttpUtil.error(
                        resp,
                        400,
                        "Identificador obligatorio"
                );

                return;
            }

            UUID id =
                    UUID.fromString(
                            raw
                    );

            if (!service.eliminar(
                    id,
                    idEmpresa,
                    superUsuario,
                    obtenerIdUsuario(
                            req
                    )
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

        } catch (SQLException e) {

            HttpUtil.errorBaseDatos(resp, e);

        } catch (IllegalStateException e) {

            HttpUtil.errorInterno(resp, e);

        } catch (Exception e) {

            HttpUtil.error(
                    resp,
                    400,
                    "Identificador inválido"
            );
        }
    }

    // =========================================================
    // EMPRESA DEL JWT
    // =========================================================

    private UUID obtenerIdEmpresa(
            HttpServletRequest req
    ) {

        return (UUID)
                req.getAttribute(
                        "idEmpresa"
                );
    }

    // =========================================================
    // USUARIO DEL JWT
    // =========================================================

    private UUID obtenerIdUsuario(
            HttpServletRequest req
    ) {

        return (UUID)
                req.getAttribute(
                        "idUsuario"
                );
    }

    // =========================================================
    // SUPERUSUARIO
    // =========================================================

    private boolean esSuperUsuario(
            HttpServletRequest req
    ) {

        String rol =
                (String)
                        req.getAttribute(
                                "rol"
                        );

        return rol != null &&
                rol.equalsIgnoreCase(
                        "SuperUsuario"
                );
    }

    // =========================================================
    // PRIMER VALOR DEL PATH
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

        String[] partes =
                path.substring(1)
                        .split("/");

        if (partes.length == 0 ||
                partes[0].isBlank()) {

            return null;
        }

        return partes[0];
    }

    // =========================================================
    // SEGUNDO VALOR DEL PATH
    // =========================================================

    private String segundoValor(
            HttpServletRequest req
    ) {

        String path =
                req.getPathInfo();

        if (path == null ||
                path.equals("/") ||
                path.isBlank()) {

            return null;
        }

        String[] partes =
                path.substring(1)
                        .split("/");

        if (partes.length < 2 ||
                partes[1].isBlank()) {

            return null;
        }

        return partes[1];
    }
}