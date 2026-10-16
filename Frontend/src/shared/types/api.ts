/**
 * Tipos espejo de los modelos Java del backend (com.itq.model).
 *
 * Convenciones que respeta el backend:
 *  - Gson serializa los campos en camelCase, igual que en Java.
 *  - `UUID`           -> string
 *  - `Integer`        -> number   (llaves SERIAL de catálogos)
 *  - `BigDecimal`     -> number   (Gson lo emite como número JSON)
 *  - `LocalDate`      -> string 'YYYY-MM-DD'
 *  - `OffsetDateTime` -> string ISO-8601 con offset
 *
 * IMPORTANTE: no confundir llaves. Los catálogos usan number, las
 * entidades de negocio usan string (UUID). Los alias de abajo lo hacen explícito.
 */

export type Uuid = string
export type IsoDate = string // 'YYYY-MM-DD'
export type IsoDateTime = string // ISO-8601 con offset

/** Envoltorio uniforme que devuelven todos los servlets. */
export interface ApiResponse<T> {
  exito: boolean
  mensaje: string
  datos: T
}

/* ---------------------------------------------------------------- catálogos */

export interface SriIva {
  idIva: number
  porcentaje: number
  codigoSri: string | null
}

export interface Especie {
  idEspecie: number
  nombre: string
}

export interface Raza {
  idRaza: number
  idEspecie: number
  nombre: string
}

export interface Vacuna {
  idVacuna: number
  nombre: string
}

export interface Categoria {
  idCategoria: number
  idEmpresa: Uuid
  nombre: string
}

/* ------------------------------------------------------- seguridad / acceso */

export interface LoginRequest {
  usuario: string
  clave: string
}

export interface LoginResponse {
  token: string
  idUsuario: Uuid
  idEmpresa: Uuid | null
  idRol: number
  rol: string
  nombres: string
  usuario: string
}

/** Body de POST /api/auth/notificar-bloqueo (endpoint público). */
export interface NotificarBloqueoRequest {
  usuario: string
}

export interface RecuperarPasswordRequest {
  correo: string
}

export interface RestablecerPasswordRequest {
  token: string
  nuevaClave: string
}

/** Body de POST /api/auth/confirmar-cuenta (endpoint público). */
export interface ConfirmarCuentaRequest {
  token: string
}

export interface CambiarClaveAdminRequest {
  nuevaClave: string
}

/** GET /api/empresas/validar-ruc?ruc=... (dto.ValidacionRucResponse) */
export interface ValidacionRuc {
  ruc: string
  formatoValido: boolean
  existeEnSri: boolean
  registradoEnSistema: boolean
  disponibleParaRegistro: boolean
  mensaje?: string | null
}

/** Respuesta paginada genérica (ver PaginaResponse<T> en el backend). */
export interface PaginaResponse<T> {
  contenido: T[]
  totalRegistros: number
  pagina: number
  tamanio: number
  totalPaginas: number
}

export interface ResumenEmpresa {
  idEmpresa: Uuid
  ruc: string
  razonSocial: string
  direccion: string
  activo: boolean
  totalUsuarios: number
  totalVeterinarios: number
  totalClientes: number
  totalMascotas: number
  totalCitas: number
  totalHistoriales: number
  totalProductos: number
  totalRecetas: number
  totalFacturas: number
  totalMovimientosInventario: number
  totalFacturado: number
  productosBajoStock: number
}

/* ------------------------------------------- panel estadístico (SuperUsuario) */

/** GET /api/estadisticas (dto.EstadisticasPlataforma) — solo SuperUsuario. */
export interface EstadisticasTotales {
  empresas: number
  empresasActivas: number
  empresasInactivas: number
  usuarios: number
  usuariosPendientes: number
  clientes: number
  mascotas: number
  citas: number
  citasPendientes: number
  historiales: number
  productos: number
  productosBajoStock: number
  facturas: number
  totalFacturado: number
  ticketPromedio: number
}

/** Un mes de la serie temporal: `periodo` en formato 'YYYY-MM'. */
export interface EstadisticaPuntoSerie {
  periodo: string
  facturado: number
  facturas: number
  citas: number
  movimientos: number
}

/** Desglose genérico etiqueta/valor (citas por estado, por rol, etc.). */
export interface EstadisticaConteo {
  etiqueta: string
  valor: number
}

export interface EstadisticaEmpresaRanking {
  idEmpresa: Uuid
  razonSocial: string
  activo: boolean
  facturado: number
  facturas: number
  clientes: number
  mascotas: number
}

export interface EstadisticasPlataforma {
  totales: EstadisticasTotales
  serieMensual: EstadisticaPuntoSerie[]
  citasPorEstado: EstadisticaConteo[]
  usuariosPorRol: EstadisticaConteo[]
  mascotasPorEspecie: EstadisticaConteo[]
  empresasPorEstado: EstadisticaConteo[]
  topEmpresas: EstadisticaEmpresaRanking[]
  generadoEn: string
}

export interface Rol {
  idRol: number
  nombre: string
}

export interface Permiso {
  idPermiso: number
  modulo: string
  accion: string
}

/** Llave compuesta: el servlet la recibe por query string, no por path. */
export interface RolPermiso {
  idRol: number
  idPermiso: number
}

export interface Empresa {
  idEmpresa: Uuid
  ruc: string
  razonSocial: string
  direccion: string
  correo?: string | null
  telefono?: string | null
  activo: boolean
}

/** Body de POST /api/empresas — crea la veterinaria y su usuario administrador en una transacción. */
export interface EmpresaConAdmin extends Empresa {
  adminUsuario: string
  adminNombres: string
  adminContrasena: string
}

export interface Usuario {
  idUsuario: Uuid
  idEmpresa: Uuid
  idRol: number
  /**
   * Nombre del rol resuelto por el backend. Viene en los listados porque
   * GET /api/roles omite los roles reservados a quien no es SuperUsuario.
   */
  nombreRol?: string | null
  usuario: string
  /** El backend expone este campo; nunca lo mostramos en la UI. */
  claveHash?: string
  nombres: string
  /** Correo de la cuenta (opcional). Se usa para la confirmación por enlace. */
  correo?: string | null
  activo: boolean
  tipobloqueo: 'pago' | 'tecnico' | null
  notificacion: 'pago' | 'tecnico' | null
  /**
   * Confirmación del correo de la cuenta: 'pendiente' hasta abrir el enlace
   * enviado por correo; 'activo' cuando ya se confirmó (o la veterinaria se
   * creó sin correo). Es independiente de `activo` (bloqueo administrativo).
   */
  estadoCuenta?: 'pendiente' | 'activo' | null
}

export interface Veterinario {
  idVeterinario: Uuid
  idUsuario: Uuid
  idEmpresa: Uuid
  codigoVeterinario: string | null
  especialidad: string | null
}

export const ESTADOS_MENSUALIDAD = ['PENDIENTE', 'PAGADA', 'VENCIDA'] as const
export type EstadoMensualidad = (typeof ESTADOS_MENSUALIDAD)[number]

export interface MensualidadEmpresa {
  idMensualidad: Uuid
  idEmpresa: Uuid
  periodo: string
  valor: number
  fechaVencimiento: IsoDate
  fechaPago: IsoDate | null
  estado: EstadoMensualidad | string
  observacion: string | null
}

/* ----------------------------------------------------------------- clínica */

export const TIPOS_DOCUMENTO = ['CEDULA', 'RUC'] as const
export type TipoDocumento = (typeof TIPOS_DOCUMENTO)[number]

export interface Cliente {
  idCliente: Uuid
  idEmpresa: Uuid
  tipoDocumento: TipoDocumento | string
  identificacion: string
  nombres: string
  direccion?: string | null
  correo?: string | null
  telefono?: string | null
}

export interface Mascota {
  idMascota: Uuid
  idEmpresa: Uuid
  idCliente: Uuid
  idRaza: number
  nombre: string
  fechaNacimiento: IsoDate | null
}

export interface MascotaVacuna {
  idMascotaVacuna: Uuid
  idEmpresa: Uuid
  idMascota: Uuid
  idVacuna: number
  fechaAplicacion: IsoDate
}

export const ESTADOS_CITA = ['Pendiente', 'Atendida', 'Cancelada'] as const
export type EstadoCita = (typeof ESTADOS_CITA)[number]

export interface Cita {
  idCita: Uuid
  idEmpresa: Uuid
  idMascota: Uuid
  idVeterinario: Uuid
  fechaHora: IsoDateTime
  estado: EstadoCita | string
}

export interface HistorialClinico {
  idHistorial: Uuid
  idEmpresa: Uuid
  idCita: Uuid
  pesoKg: number | null
  temperaturaC: number | null
  anamnesis: string | null
  diagnostico: string | null
}

export interface Receta {
  idReceta: Uuid
  idEmpresa: Uuid
  idHistorial: Uuid
  indicacionesGenerales: string | null
}

export interface RecetaDetalle {
  idDetalleReceta: Uuid
  idReceta: Uuid
  idProducto: Uuid
  dosis: string
  frecuencia: string
  duracionDias: number
}

/* ------------------------------------------------- inventario / facturación */

export interface Producto {
  idProducto: Uuid
  idEmpresa: Uuid
  idCategoria: number
  idIva: number
  nombre: string
  precioUnitario: number
  stockActual: number
  stockMinimo: number
  fechaCaducidad: IsoDate | null
}

export const ESTADOS_FACTURA = ['Emitida', 'Anulada'] as const
export type EstadoFactura = (typeof ESTADOS_FACTURA)[number]

export interface Factura {
  idFactura: Uuid
  idEmpresa: Uuid
  idCliente: Uuid
  idUsuario: Uuid
  total: number
  estado: EstadoFactura | string
  fecha: IsoDateTime
}

export interface FacturaDetalle {
  idDetalle: Uuid
  idFactura: Uuid
  idProducto: Uuid
  idIva: number
  cantidad: number
  precioUnitario: number
  subtotal: number
}

/** Body de POST /api/facturas/emitir — emisión transaccional (cabecera + detalle). */
export interface FacturaEmitirRequest {
  idCliente: Uuid
  detalles: Array<Pick<FacturaDetalle, 'idProducto' | 'idIva' | 'cantidad' | 'precioUnitario' | 'subtotal'>>
}

export const TIPOS_MOVIMIENTO = ['Ingreso', 'Egreso', 'Venta', 'Ajuste'] as const
export type TipoMovimiento = (typeof TIPOS_MOVIMIENTO)[number]

export interface MovimientoInventario {
  idMovimiento: Uuid
  idEmpresa: Uuid
  idProducto: Uuid
  idFactura: Uuid | null
  tipo: TipoMovimiento | string
  cantidad: number
  fecha: IsoDateTime
}

/* --------------------------------------------------------------- utilidades */

/** Payload de creación: la llave primaria la genera la BD. */
export type NuevoRegistro<T, K extends keyof T> = Omit<T, K>
