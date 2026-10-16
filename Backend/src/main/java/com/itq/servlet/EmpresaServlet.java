package com.itq.servlet;

import com.itq.dto.ApiResponse;
import com.itq.dto.EmpresaConAdminRequest;
import com.itq.model.Empresa;
import com.itq.security.Autorizacion;
import com.itq.service.EmpresaService;
import com.itq.service.SuperUsuarioService;
import com.itq.util.HttpUtil;
import com.itq.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet("/api/empresas/*")
public class EmpresaServlet extends HttpServlet {

    private final EmpresaService service =
            new EmpresaService();

    private final SuperUsuarioService superUsuarioService =
            new SuperUsuarioService();

    // =========================================================
    // GET
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            exigirSuperUsuario(req);

            String[] partes =
                    partesRuta(req);

            // =================================================
            // GET /api/empresas/validar-ruc?ruc=XXXXXXXXXXXXX
            // =================================================

            if (partes.length == 1 &&
                    partes[0].equalsIgnoreCase("validar-ruc")) {

                Autorizacion.exigir(
                        req,
                        "EMPRESAS",
                        "CREAR"
                );

                String ruc =
                        req.getParameter("ruc");

                if (ruc == null ||
                        ruc.trim().isEmpty()) {

                    HttpUtil.error(
                            resp,
                            400,
                            "El parámetro RUC es obligatorio"
                    );

                    return;
                }

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Validación de RUC",
                                service.validarRuc(
                                        ruc
                                )
                        )
                );

                return;
            }

            // =================================================
            // GET /api/empresas
            // =================================================

            if (partes.length == 0) {

                Autorizacion.exigir(
                        req,
                        "EMPRESAS",
                        "LISTAR"
                );

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

            // =================================================
            // GET /api/empresas/{id}/resumen
            // =================================================

            if (partes.length == 2 &&
                    partes[1].equalsIgnoreCase("resumen")) {

                Autorizacion.exigir(
                        req,
                        "EMPRESAS",
                        "VER"
                );

                UUID idEmpresa =
                        UUID.fromString(
                                partes[0]
                        );

                var resumen =
                        superUsuarioService
                                .obtenerResumen(
                                        idEmpresa
                                );

                if (resumen.isEmpty()) {

                    HttpUtil.error(
                            resp,
                            404,
                            "Empresa no encontrada"
                    );

                    return;
                }

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Resumen de empresa",
                                resumen.get()
                        )
                );

                return;
            }

            // =================================================
            // GET /api/empresas/{id}
            // =================================================

            if (partes.length == 1) {

                Autorizacion.exigir(
                        req,
                        "EMPRESAS",
                        "VER"
                );

                UUID idEmpresa =
                        UUID.fromString(
                                partes[0]
                        );

                var encontrado =
                        service.buscarPorId(
                                idEmpresa
                        );

                if (encontrado.isEmpty()) {

                    HttpUtil.error(
                            resp,
                            404,
                            "Empresa no encontrada"
                    );

                    return;
                }

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Empresa encontrada",
                                encontrado.get()
                        )
                );

                return;
            }

            // =================================================
            // RUTA NO ENCONTRADA
            // =================================================

            HttpUtil.error(
                    resp,
                    404,
                    "Ruta no encontrada"
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

            HttpUtil.errorNoDisponible(resp, e);

        } catch (Exception e) {

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

            exigirSuperUsuario(req);

            Autorizacion.exigir(
                    req,
                    "EMPRESAS",
                    "CREAR"
            );

            EmpresaConAdminRequest request =
                    JsonUtil.gson()
                            .fromJson(
                                    req.getReader(),
                                    EmpresaConAdminRequest.class
                            );

            if (request == null) {

                HttpUtil.error(
                        resp,
                        400,
                        "Los datos de la empresa son obligatorios"
                );

                return;
            }

            Empresa creada =
                    service.crearConAdmin(
                            request
                    );

            HttpUtil.json(
                    resp,
                    201,
                    ApiResponse.ok(
                            "Empresa creada",
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

            String mensaje =
                    e.getMessage();

            // =================================================
            // RUC DUPLICADO
            // =================================================

            if (mensaje != null &&
                    (
                            mensaje.contains("empresa_ruc_key") ||
                                    mensaje.contains("ux_empresa_ruc")
                    )) {

                HttpUtil.error(
                        resp,
                        400,
                        "Ya existe una veterinaria registrada con este RUC"
                );

                return;
            }

            // =================================================
            // CORREO DUPLICADO
            // =================================================

            if (mensaje != null &&
                    (
                            mensaje.contains("uk_empresa_correo") ||
                                    mensaje.contains(
                                            "ux_empresa_correo_normalizado"
                                    )
                    )) {

                HttpUtil.error(
                        resp,
                        400,
                        "Ya existe una veterinaria registrada con este correo"
                );

                return;
            }

            // =================================================
            // TELÉFONO DUPLICADO
            // =================================================

            if (mensaje != null &&
                    (
                            mensaje.contains("uk_empresa_telefono") ||
                                    mensaje.contains(
                                            "ux_empresa_telefono_normalizado"
                                    )
                    )) {

                HttpUtil.error(
                        resp,
                        400,
                        "Ya existe una veterinaria registrada con este teléfono"
                );

                return;
            }

            HttpUtil.errorBaseDatos(resp, e);

        } catch (IllegalStateException e) {

            HttpUtil.errorNoDisponible(resp, e);

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

            exigirSuperUsuario(req);

            String[] partes =
                    partesRuta(req);

            if (partes.length == 0) {

                HttpUtil.error(
                        resp,
                        400,
                        "Identificador obligatorio"
                );

                return;
            }

            UUID idEmpresa =
                    UUID.fromString(
                            partes[0]
                    );

            // =================================================
            // PUT /api/empresas/{id}/activar
            // =================================================

            if (partes.length == 2 &&
                    partes[1].equalsIgnoreCase("activar")) {

                Autorizacion.exigir(
                        req,
                        "EMPRESAS",
                        "ACTIVAR"
                );

                if (!service.activar(
                        idEmpresa
                )) {

                    HttpUtil.error(
                            resp,
                            404,
                            "Empresa no encontrada"
                    );

                    return;
                }

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Empresa activada",
                                null
                        )
                );

                return;
            }

            // =================================================
            // PUT /api/empresas/{id}/desactivar
            // =================================================

            if (partes.length == 2 &&
                    partes[1].equalsIgnoreCase("desactivar")) {

                Autorizacion.exigir(
                        req,
                        "EMPRESAS",
                        "DESACTIVAR"
                );

                if (!service.desactivar(
                        idEmpresa
                )) {

                    HttpUtil.error(
                            resp,
                            404,
                            "Empresa no encontrada"
                    );

                    return;
                }

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Empresa desactivada",
                                null
                        )
                );

                return;
            }

            // =================================================
            // PUT /api/empresas/{id}/contrasena-admin
            // =================================================

            if (partes.length == 2 &&
                    partes[1].equalsIgnoreCase(
                            "contrasena-admin"
                    )) {

                Autorizacion.exigir(
                        req,
                        "EMPRESAS",
                        "EDITAR"
                );

                CambiarContrasenaAdminRequest request =
                        JsonUtil.gson()
                                .fromJson(
                                        req.getReader(),
                                        CambiarContrasenaAdminRequest.class
                                );

                if (request == null) {

                    HttpUtil.error(
                            resp,
                            400,
                            "Los datos para cambiar la contraseña son obligatorios"
                    );

                    return;
                }

                if (request.getNuevaClave() == null ||
                        request.getNuevaClave()
                                .trim()
                                .isEmpty()) {

                    HttpUtil.error(
                            resp,
                            400,
                            "La nueva contraseña es obligatoria"
                    );

                    return;
                }

                boolean actualizado =
                        service
                                .actualizarContrasenaAdministrador(
                                        idEmpresa,
                                        request.getNuevaClave()
                                );

                if (!actualizado) {

                    HttpUtil.error(
                            resp,
                            404,
                            "No se encontró el Administrador Local de esta veterinaria"
                    );

                    return;
                }

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Contraseña del Administrador Local actualizada correctamente",
                                null
                        )
                );

                return;
            }

            // =================================================
            // PUT /api/empresas/{id}
            // =================================================

            if (partes.length == 1) {

                Autorizacion.exigir(
                        req,
                        "EMPRESAS",
                        "EDITAR"
                );

                Empresa obj =
                        JsonUtil.gson()
                                .fromJson(
                                        req.getReader(),
                                        Empresa.class
                                );

                if (obj == null) {

                    HttpUtil.error(
                            resp,
                            400,
                            "Los datos de la empresa son obligatorios"
                    );

                    return;
                }

                obj.setIdEmpresa(
                        idEmpresa
                );

                if (!service.actualizar(
                        obj
                )) {

                    HttpUtil.error(
                            resp,
                            404,
                            "Empresa no encontrada"
                    );

                    return;
                }

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Empresa actualizada",
                                obj
                        )
                );

                return;
            }

            // =================================================
            // RUTA NO ENCONTRADA
            // =================================================

            HttpUtil.error(
                    resp,
                    404,
                    "Ruta no encontrada"
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

            String mensaje =
                    e.getMessage();

            // =================================================
            // RUC DUPLICADO
            // =================================================

            if (mensaje != null &&
                    (
                            mensaje.contains("empresa_ruc_key") ||
                                    mensaje.contains("ux_empresa_ruc")
                    )) {

                HttpUtil.error(
                        resp,
                        400,
                        "Ya existe una empresa registrada con este RUC"
                );

                return;
            }

            // =================================================
            // CORREO DUPLICADO
            // =================================================

            if (mensaje != null &&
                    (
                            mensaje.contains("uk_empresa_correo") ||
                                    mensaje.contains(
                                            "ux_empresa_correo_normalizado"
                                    )
                    )) {

                HttpUtil.error(
                        resp,
                        400,
                        "Ya existe una empresa registrada con este correo"
                );

                return;
            }

            // =================================================
            // TELÉFONO DUPLICADO
            // =================================================

            if (mensaje != null &&
                    (
                            mensaje.contains("uk_empresa_telefono") ||
                                    mensaje.contains(
                                            "ux_empresa_telefono_normalizado"
                                    )
                    )) {

                HttpUtil.error(
                        resp,
                        400,
                        "Ya existe una empresa registrada con este teléfono"
                );

                return;
            }

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

            exigirSuperUsuario(req);

            Autorizacion.exigir(
                    req,
                    "EMPRESAS",
                    "ELIMINAR"
            );

            String[] partes =
                    partesRuta(req);

            if (partes.length != 1) {

                HttpUtil.error(
                        resp,
                        400,
                        "Identificador de la veterinaria obligatorio"
                );

                return;
            }

            UUID idEmpresa =
                    UUID.fromString(
                            partes[0]
                    );

            if (!service.eliminar(
                    idEmpresa
            )) {

                HttpUtil.error(
                        resp,
                        404,
                        "Veterinaria no encontrada o ya eliminada"
                );

                return;
            }

            HttpUtil.json(
                    resp,
                    200,
                    ApiResponse.ok(
                            "Veterinaria eliminada correctamente",
                            null
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

        } catch (Exception e) {

            HttpUtil.errorInterno(resp, e);
        }
    }

    // =========================================================
    // SOLO SUPERUSUARIO
    // =========================================================

    private void exigirSuperUsuario(
            HttpServletRequest req
    ) {

        String rol =
                (String)
                        req.getAttribute(
                                "rol"
                        );

        if (rol == null ||
                !rol.equalsIgnoreCase(
                        "SuperUsuario"
                )) {

            throw new SecurityException(
                    "Esta operación requiere SuperUsuario"
            );
        }
    }

    // =========================================================
    // PARSEO DE RUTA
    //
    // /                          -> []
    // /validar-ruc               -> [validar-ruc]
    // /UUID                      -> [UUID]
    // /UUID/resumen              -> [UUID, resumen]
    // /UUID/activar              -> [UUID, activar]
    // /UUID/desactivar           -> [UUID, desactivar]
    // /UUID/contrasena-admin     -> [UUID, contrasena-admin]
    // DELETE /UUID                -> soft delete de la veterinaria
    // =========================================================

    private String[] partesRuta(
            HttpServletRequest req
    ) {

        String path =
                req.getPathInfo();

        if (path == null ||
                path.equals("/") ||
                path.isBlank()) {

            return new String[0];
        }

        String limpio =
                path.trim();

        if (limpio.startsWith("/")) {

            limpio =
                    limpio.substring(1);
        }

        if (limpio.endsWith("/")) {

            limpio =
                    limpio.substring(
                            0,
                            limpio.length() - 1
                    );
        }

        if (limpio.isBlank()) {

            return new String[0];
        }

        return limpio.split("/");
    }

    // =========================================================
    // DTO INTERNO - CAMBIO DE CONTRASEÑA ADMIN
    // =========================================================

    private static class CambiarContrasenaAdminRequest {

        private String nuevaClave;

        public String getNuevaClave() {
            return nuevaClave;
        }

        public void setNuevaClave(
                String nuevaClave
        ) {
            this.nuevaClave =
                    nuevaClave;
        }
    }
}