import type { ColumnDef } from '@tanstack/react-table'
import type { CrudHooks } from '@/shared/api/crud'
import type { FormatoCampo } from '@/shared/lib/validacion'

export type TipoCampo =
  | 'texto'
  | 'numero'
  | 'decimal'
  | 'fecha'
  | 'fechaHora'
  | 'textarea'
  | 'select'
  | 'booleano'
  | 'contrasena'

export interface OpcionSelect {
  valor: string | number
  etiqueta: string
}

export interface CampoFormulario<T> {
  nombre: keyof T & string
  etiqueta: string
  tipo: TipoCampo
  requerido?: boolean
  /** Solo se puede definir al crear; se bloquea al editar (FKs estructurales). */
  soloCreacion?: boolean
  /** Opciones para `tipo: 'select'`. Las llaves numéricas se convierten a number. */
  opciones?: OpcionSelect[]
  /** El select devuelve number en vez de string (llaves SERIAL). */
  opcionesNumericas?: boolean
  placeholder?: string
  ayuda?: string
  maxLength?: number
  /**
   * Formato esperado del valor (nombres sin números, teléfonos de 10 dígitos,
   * correos, códigos…). Bloquea caracteres inválidos mientras se escribe y
   * valida el patrón al enviar. Ver `FORMATOS` en `@/shared/lib/validacion`.
   */
  formato?: FormatoCampo
  min?: number
  max?: number
  /** Solo para `tipo: 'fecha'`: no permite elegir una fecha posterior a hoy (ej. fecha de nacimiento). */
  maxHoy?: boolean
  /**
   * Solo para `tipo: 'fecha'` o `'fechaHora'`: fija la fecha mínima.
   * Al crear es hoy (`'hoy'`) o mañana (`'manana'`); al editar es siempre la
   * fecha original del registro, para no poder retrocederla (y no bloquear el
   * guardado de registros cuya fecha ya pasó). Ej.: agendar citas (`'hoy'`) o
   * la caducidad de un producto (`'manana'`).
   */
  minFecha?: 'hoy' | 'manana'
  /** Ocupa las dos columnas de la rejilla. */
  anchoCompleto?: boolean
}

export interface ConfiguracionCrud<T> {
  /** Plural, para títulos: "Clientes". */
  titulo: string
  /** Singular, para el modal: "cliente". */
  singular: string
  descripcion?: string
  api: CrudHooks<T>
  campoId: keyof T & string
  campos: CampoFormulario<T>[]
  columnas: ColumnDef<T, unknown>[]
  /**
   * Si el registro lleva `idEmpresa`, se inyecta automáticamente desde la
   * sesión al crear y se filtra el listado por empresa activa.
   */
  multiEmpresa?: boolean
  /** Valores fijos añadidos al payload de creación. */
  valoresBase?: Partial<T>
  /**
   * `true` mientras terminan de cargar catálogos auxiliares que las columnas
   * necesitan (p. ej. el nombre del rol o de la categoría). Sin esto, la tabla
   * se dibuja en cuanto llega la lista principal y las columnas con relaciones
   * salen vacías hasta que resuelven sus propias consultas.
   */
  cargando?: boolean
  /**
   * Devuelve el motivo por el que una fila no se puede editar/eliminar, o
   * `null` si sí se puede. Permite deshabilitar las acciones por fila (p. ej.
   * un Administrador Local no puede gestionar su propia cuenta) sin exponer
   * al usuario a un error 403 del backend.
   */
  bloqueoGestion?: (registro: T) => string | null
}
