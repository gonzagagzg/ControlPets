package com.itq.service;

import com.itq.config.ConexionBD;
import com.itq.dao.EmpresaDAO;
import com.itq.dto.EmpresaConAdminRequest;
import com.itq.dto.ValidacionRucResponse;
import com.itq.model.Empresa;
import com.itq.util.PasswordUtil;
import com.itq.validation.EcuadorValidator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class EmpresaService {

    private final EmpresaDAO dao =
            new EmpresaDAO();

    private final RucSriService rucSriService =
            new RucSriService();

    private final ConfirmacionCuentaService confirmacionCuentaService =
            new ConfirmacionCuentaService();

    private static boolean SRI_VALIDATION_SKIP;

    static {
        com.itq.config.ApplicationInitializer.initialize();
        String valor = System.getenv("SRI_VALIDATION_SKIP");
        if (valor == null || valor.isBlank()) {
            valor = System.getProperty("SRI_VALIDATION_SKIP");
        }
        SRI_VALIDATION_SKIP = valor != null && valor.trim().equalsIgnoreCase("true");
    }

    private static final Pattern PATRON_CORREO =
            Pattern.compile(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
            );

    // =========================================================
    // LISTAR
    // =========================================================

    public List<Empresa> listar()
            throws SQLException {

        return dao.listar();
    }

    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    public Optional<Empresa> buscarPorId(
            UUID idEmpresa
    ) throws SQLException {

        if (idEmpresa == null) {

            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        return dao.buscarPorId(
                idEmpresa
        );
    }

    // =========================================================
    // VALIDAR RUC PARA EL FRONTEND
    // =========================================================

    public ValidacionRucResponse validarRuc(
            String ruc
    ) throws SQLException {

        if (vacio(ruc)) {

            return new ValidacionRucResponse(
                    null,
                    false,
                    false,
                    false,
                    false,
                    "El RUC es obligatorio"
            );
        }

        String rucLimpio =
                EcuadorValidator.limpiarNumero(
                        ruc
                );

        if (rucLimpio == null ||
                rucLimpio.length() != 13) {

            return new ValidacionRucResponse(
                    rucLimpio,
                    false,
                    false,
                    false,
                    false,
                    "El RUC debe contener 13 dígitos"
            );
        }

        if (!EcuadorValidator.rucValido(
                rucLimpio
        )) {

            return new ValidacionRucResponse(
                    rucLimpio,
                    false,
                    false,
                    false,
                    false,
                    "El RUC ecuatoriano no es válido"
            );
        }

        boolean registrado =
                dao.existeRuc(
                        rucLimpio
                );

        if (registrado) {

            return new ValidacionRucResponse(
                    rucLimpio,
                    true,
                    true,
                    true,
                    false,
                    "El RUC ya se encuentra registrado en el sistema"
            );
        }

        boolean existeEnSri = true;

        if (!SRI_VALIDATION_SKIP) {
            existeEnSri = rucSriService.existeEnSri(rucLimpio);
        }

        if (!existeEnSri) {

            return new ValidacionRucResponse(
                    rucLimpio,
                    true,
                    false,
                    false,
                    false,
                    "El RUC es válido, pero no consta registrado en el SRI"
            );
        }

        return new ValidacionRucResponse(
                rucLimpio,
                true,
                true,
                false,
                true,
                "RUC válido, registrado en el SRI y disponible"
        );
    }

    // =========================================================
    // CREAR EMPRESA
    // =========================================================

    public Empresa crear(
            Empresa obj
    ) throws SQLException {

        if (obj == null) {

            throw new IllegalArgumentException(
                    "Los datos de la empresa son obligatorios"
            );
        }

        if (obj.isActivo() == null) {
            obj.setActivo(true);
        }

        validar(obj);

        validarRucParaCreacion(
                obj.getRuc()
        );

        validarDuplicados(
                obj
        );

        return dao.insertar(
                obj
        );
    }

    // =========================================================
    // CREAR EMPRESA + ADMINISTRADOR
    // =========================================================

    public Empresa crearConAdmin(
            EmpresaConAdminRequest request
    ) throws SQLException {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Los datos de la empresa son obligatorios"
            );
        }

        Empresa empresa =
                new Empresa();

        empresa.setRuc(
                request.getRuc()
        );

        empresa.setRazonSocial(
                request.getRazonSocial()
        );

        empresa.setDireccion(
                request.getDireccion()
        );

        empresa.setCorreo(
                request.getCorreo()
        );

        empresa.setTelefono(
                request.getTelefono()
        );

        empresa.setActivo(
                request.getActivo()
        );

        if (empresa.isActivo() == null) {
            empresa.setActivo(true);
        }

        // Validación de estructura y datos básicos.
        validar(
                empresa
        );

        // Valida:
        // 1. que no esté en nuestra BD
        // 2. que exista oficialmente en SRI
        validarRucParaCreacion(
                empresa.getRuc()
        );

        // Evita correo/teléfono duplicado.
        validarDuplicados(
                empresa
        );

        validarAdmin(
                request
        );

        try (
                Connection cn =
                        ConexionBD.obtenerConexion()
        ) {

            cn.setAutoCommit(
                    false
            );

            try {

                UUID idEmpresa =
                        insertarEmpresa(
                                cn,
                                empresa
                        );

                empresa.setIdEmpresa(
                        idEmpresa
                );

                Integer idRolAdmin =
                        buscarRolAdministrador(
                                cn
                        );

                if (idRolAdmin == null) {

                    throw new IllegalArgumentException(
                            "No se encontró el rol Administrador Local"
                    );
                }

                String hash =
                        PasswordUtil.hash(
                                request
                                        .getAdminContrasena()
                                        .trim()
                        );

                /*
                 * Si la veterinaria tiene correo, el administrador queda
                 * 'pendiente' hasta confirmarlo; sin correo queda 'activo'.
                 * (empresa.getCorreo() ya viene normalizado por validar()).
                 */
                String correoNormalizado =
                        empresa.getCorreo();

                boolean confirmarCorreo =
                        correoNormalizado != null &&
                                !correoNormalizado.isBlank();

                UUID idUsuarioAdmin =
                        insertarUsuarioAdmin(
                                cn,
                                idEmpresa,
                                idRolAdmin,
                                request,
                                hash,
                                correoNormalizado,
                                confirmarCorreo
                        );

                if (confirmarCorreo) {

                    confirmacionCuentaService
                            .registrarYEnviar(
                                    cn,
                                    idUsuarioAdmin,
                                    correoNormalizado,
                                    request
                                            .getAdminNombres()
                                            .trim()
                            );
                }

                cn.commit();

                return empresa;

            } catch (Exception e) {

                try {
                    cn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(
                            rollbackError
                    );
                }

                if (e instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) e;
                }

                if (e instanceof SecurityException) {
                    throw (SecurityException) e;
                }

                if (e instanceof IllegalStateException) {
                    throw (IllegalStateException) e;
                }

                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }

                throw new SQLException(
                        "No se pudo registrar la veterinaria",
                        e
                );

            } finally {

                try {
                    cn.setAutoCommit(
                            true
                    );
                } catch (SQLException ignored) {
                }
            }
        }
    }

    // =========================================================
    // VALIDAR RUC PARA CREACIÓN
    // =========================================================

    private void validarRucParaCreacion(
            String ruc
    ) throws SQLException {

        if (dao.existeRuc(ruc)) {

            throw new IllegalArgumentException(
                    "Ya existe una veterinaria registrada con este RUC"
            );
        }

        boolean existeEnSri = true;

        if (!SRI_VALIDATION_SKIP) {
            existeEnSri = rucSriService.existeEnSri(ruc);
        }

        if (!existeEnSri) {

            throw new IllegalArgumentException(
                    "El RUC no consta registrado en el SRI"
            );
        }
    }

    // =========================================================
    // DUPLICADOS
    // =========================================================

    private void validarDuplicados(
            Empresa empresa
    ) throws SQLException {

        if (!vacio(
                empresa.getCorreo()
        )) {

            if (dao.existeCorreo(
                    empresa.getCorreo()
            )) {

                throw new IllegalArgumentException(
                        "Ya existe una veterinaria registrada con este correo electrónico"
                );
            }
        }

        if (!vacio(
                empresa.getTelefono()
        )) {

            if (dao.existeTelefono(
                    empresa.getTelefono()
            )) {

                throw new IllegalArgumentException(
                        "Ya existe una veterinaria registrada con este número de teléfono"
                );
            }
        }
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================

    public boolean actualizar(
            Empresa obj
    ) throws SQLException {

        if (obj == null ||
                obj.getIdEmpresa() == null) {

            throw new IllegalArgumentException(
                    "Los datos de la empresa son obligatorios"
            );
        }

        validar(
                obj
        );

        return dao.actualizar(
                obj
        );
    }

    // =========================================================
    // CAMBIAR CONTRASEÑA DEL ADMINISTRADOR LOCAL
    // =========================================================

    public boolean actualizarContrasenaAdministrador(
            UUID idEmpresa,
            String nuevaClave
    ) throws SQLException {

        if (idEmpresa == null) {

            throw new IllegalArgumentException(
                    "La veterinaria es obligatoria"
            );
        }

        if (vacio(nuevaClave)) {

            throw new IllegalArgumentException(
                    "La nueva contraseña es obligatoria"
            );
        }

        String claveLimpia =
                nuevaClave.trim();

        if (claveLimpia.length() < 6) {

            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 6 caracteres"
            );
        }

        if (dao.buscarPorId(idEmpresa).isEmpty()) {

            throw new IllegalArgumentException(
                    "La veterinaria no existe"
            );
        }

        String hash =
                PasswordUtil.hash(
                        claveLimpia
                );

        String sql = """
                UPDATE usuario
                SET clave_hash = ?
                WHERE id_usuario = (
                    SELECT u.id_usuario
                    FROM usuario u
                    INNER JOIN rol r
                        ON r.id_rol = u.id_rol
                    WHERE u.id_empresa = ?
                      AND UPPER(r.nombre) = UPPER(?)
                    LIMIT 1
                )
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    hash
            );

            ps.setObject(
                    2,
                    idEmpresa
            );

            ps.setString(
                    3,
                    "Administrador Local"
            );

            return ps.executeUpdate() == 1;
        }
    }

    // =========================================================
    // ACTIVAR
    // =========================================================

    public boolean activar(
            UUID idEmpresa
    ) throws SQLException {

        if (idEmpresa == null) {

            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        return dao.cambiarEstado(
                idEmpresa,
                true
        );
    }

    // =========================================================
    // DESACTIVAR
    // =========================================================

    public boolean desactivar(
            UUID idEmpresa
    ) throws SQLException {

        if (idEmpresa == null) {

            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        return dao.cambiarEstado(
                idEmpresa,
                false
        );
    }

    // =========================================================
    // ELIMINAR (LÓGICO)
    // =========================================================

    public boolean eliminar(
            UUID idEmpresa
    ) throws SQLException {

        if (idEmpresa == null) {

            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        return dao.softDelete(
                idEmpresa
        );
    }

    // =========================================================
    // VALIDAR DATOS EMPRESA
    // =========================================================

    private void validar(
            Empresa obj
    ) {

        if (vacio(
                obj.getRuc()
        )) {

            throw new IllegalArgumentException(
                    "El RUC es obligatorio"
            );
        }

        String rucLimpio =
                EcuadorValidator.limpiarNumero(
                        obj.getRuc()
                );

        if (rucLimpio == null ||
                rucLimpio.length() != 13) {

            throw new IllegalArgumentException(
                    "El RUC debe contener 13 dígitos"
            );
        }

        if (!EcuadorValidator.rucValido(
                rucLimpio
        )) {

            throw new IllegalArgumentException(
                    "El RUC ecuatoriano no es válido"
            );
        }

        obj.setRuc(
                rucLimpio
        );

        if (vacio(
                obj.getRazonSocial()
        )) {

            throw new IllegalArgumentException(
                    "La razón social es obligatoria"
            );
        }

        if (vacio(
                obj.getDireccion()
        )) {

            throw new IllegalArgumentException(
                    "La dirección es obligatoria"
            );
        }

        if (obj.isActivo() == null) {

            throw new IllegalArgumentException(
                    "El estado activo es obligatorio"
            );
        }

        obj.setRazonSocial(
                obj.getRazonSocial()
                        .trim()
        );

        obj.setDireccion(
                obj.getDireccion()
                        .trim()
        );

        // -----------------------------------------------------
        // CORREO
        // -----------------------------------------------------

        if (!vacio(
                obj.getCorreo()
        )) {

            String correo =
                    obj.getCorreo()
                            .trim()
                            .toLowerCase();

            if (!PATRON_CORREO
                    .matcher(correo)
                    .matches()) {

                throw new IllegalArgumentException(
                        "El correo electrónico no es válido"
                );
            }

            obj.setCorreo(
                    correo
            );

        } else {

            obj.setCorreo(
                    null
            );
        }

        // -----------------------------------------------------
        // TELÉFONO
        // -----------------------------------------------------

        if (!vacio(
                obj.getTelefono()
        )) {

            String telefono =
                    EcuadorValidator.limpiarNumero(
                            obj.getTelefono()
                    );

            if (telefono == null ||
                    telefono.length() < 7 ||
                    telefono.length() > 15) {

                throw new IllegalArgumentException(
                        "El teléfono no es válido"
                );
            }

            obj.setTelefono(
                    telefono
            );

        } else {

            obj.setTelefono(
                    null
            );
        }
    }

    // =========================================================
    // VALIDAR ADMIN
    // =========================================================

    private void validarAdmin(
            EmpresaConAdminRequest request
    ) {

        if (vacio(
                request.getAdminUsuario()
        )) {

            throw new IllegalArgumentException(
                    "El usuario administrador es obligatorio"
            );
        }

        if (vacio(
                request.getAdminNombres()
        )) {

            throw new IllegalArgumentException(
                    "Los nombres del administrador son obligatorios"
            );
        }

        if (vacio(
                request.getAdminContrasena()
        )) {

            throw new IllegalArgumentException(
                    "La contraseña del administrador es obligatoria"
            );
        }

        if (request
                .getAdminContrasena()
                .trim()
                .length() < 6) {

            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 6 caracteres"
            );
        }
    }

    // =========================================================
    // INSERTAR EMPRESA EN TRANSACCIÓN
    // =========================================================

    private UUID insertarEmpresa(
            Connection cn,
            Empresa empresa
    ) throws SQLException {

        String sql = """
                INSERT INTO empresa
                (
                    ruc,
                    razon_social,
                    direccion,
                    correo,
                    telefono,
                    activo
                )
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id_empresa
                """;

        try (
                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    empresa.getRuc()
            );

            ps.setString(
                    2,
                    empresa.getRazonSocial()
            );

            ps.setString(
                    3,
                    empresa.getDireccion()
            );

            ps.setString(
                    4,
                    empresa.getCorreo()
            );

            ps.setString(
                    5,
                    empresa.getTelefono()
            );

            ps.setBoolean(
                    6,
                    empresa.isActivo()
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                if (!rs.next()) {

                    throw new SQLException(
                            "No se generó la clave primaria de la empresa"
                    );
                }

                return rs.getObject(
                        "id_empresa",
                        UUID.class
                );
            }
        }
    }

    // =========================================================
    // BUSCAR ROL ADMINISTRADOR LOCAL
    // =========================================================

    private Integer buscarRolAdministrador(
            Connection cn
    ) throws SQLException {

        String sql = """
                SELECT id_rol
                FROM rol
                WHERE UPPER(nombre) = UPPER(?)
                """;

        try (
                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    "Administrador Local"
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                if (rs.next()) {

                    return (Integer)
                            rs.getObject(
                                    "id_rol"
                            );
                }

                return null;
            }
        }
    }

    // =========================================================
    // INSERTAR ADMIN
    // =========================================================

    private UUID insertarUsuarioAdmin(
            Connection cn,
            UUID idEmpresa,
            Integer idRol,
            EmpresaConAdminRequest request,
            String hash,
            String correo,
            boolean confirmarCorreo
    ) throws SQLException {

        String sql = """
                INSERT INTO usuario
                (
                    id_empresa,
                    id_rol,
                    usuario,
                    clave_hash,
                    nombres,
                    correo,
                    activo,
                    estado_cuenta
                )
                VALUES (?, ?, ?, ?, ?, ?, TRUE, ?)
                RETURNING id_usuario
                """;

        try (
                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setObject(
                    1,
                    idEmpresa
            );

            ps.setObject(
                    2,
                    idRol
            );

            ps.setString(
                    3,
                    request
                            .getAdminUsuario()
                            .trim()
            );

            ps.setString(
                    4,
                    hash
            );

            ps.setString(
                    5,
                    request
                            .getAdminNombres()
                            .trim()
            );

            /*
             * correo ya viene normalizado (o null) desde validar().
             */
            ps.setString(
                    6,
                    correo
            );

            ps.setString(
                    7,
                    confirmarCorreo
                            ? "pendiente"
                            : "activo"
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                if (!rs.next()) {

                    throw new SQLException(
                            "No se pudo crear el usuario administrador"
                    );
                }

                return rs.getObject(
                        "id_usuario",
                        UUID.class
                );
            }
        }
    }

    // =========================================================
    // UTILIDAD
    // =========================================================

    private boolean vacio(
            String valor
    ) {

        return valor == null ||
                valor.trim().isEmpty();
    }
}