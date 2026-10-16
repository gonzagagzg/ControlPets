import type { ApiResponse } from '@/shared/types/api'
import { limpiarSesion, obtenerToken } from '@/shared/session/sesion'
import { useBloqueoEmpresa } from '@/shared/session/bloqueo'

/**
 * Cliente HTTP único contra la API de servlets.
 *
 * Responsabilidades:
 *  - Adjuntar `Authorization: Bearer <token>` a toda petición cuando hay
 *    sesión activa (el AuthFilter del backend lo exige en todo /api/* salvo
 *    /api/auth/login).
 *  - Desempaquetar el envoltorio {exito, mensaje, datos} para que las capas
 *    superiores trabajen directamente con los datos.
 *  - Normalizar los errores del backend (que llegan como
 *    {exito:false, mensaje:"..."} con status 400/404/409/500) en una
 *    excepción `ApiError` con mensaje legible.
 *  - Tratar el 204 de DELETE como respuesta vacía válida.
 *  - Reaccionar globalmente a 401 (token ausente/expirado: limpiar sesión y
 *    forzar vuelta a /acceso) y 403 (veterinaria desactivada u operación sin
 *    permiso: se deja el mensaje del backend para que la pantalla lo muestre).
 */

const BASE_URL = import.meta.env.VITE_API_URL ?? ''

export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
  ) {
    super(message)
    this.name = 'ApiError'
  }

  /** El backend usa 409 para violaciones de FK / UNIQUE (ver SqlErrorUtil). */
  get esConflicto() {
    return this.status === 409
  }

  get esNoEncontrado() {
    return this.status === 404
  }

  get esNoAutenticado() {
    return this.status === 401
  }

  /** 403: puede ser falta de permiso o veterinaria (empresa) desactivada. */
  get esProhibido() {
    return this.status === 403
  }
}

type ManejadorSesionExpirada = () => void
let manejadorSesionExpirada: ManejadorSesionExpirada | null = null

/** Registrado una vez desde el router para poder navegar a /acceso en un 401. */
export function alExpirarSesion(manejador: ManejadorSesionExpirada) {
  manejadorSesionExpirada = manejador
}

type QueryParams = Record<string, string | number | boolean | null | undefined>

function construirUrl(path: string, params?: QueryParams) {
  const url = `${BASE_URL}${path}`
  if (!params) return url

  const qs = new URLSearchParams()
  for (const [clave, valor] of Object.entries(params)) {
    if (valor !== undefined && valor !== null && valor !== '') {
      qs.append(clave, String(valor))
    }
  }
  const cadena = qs.toString()
  return cadena ? `${url}?${cadena}` : url
}

const MENSAJE_ERROR_INESPERADO =
  'Ocurrió un error inesperado. Intenta nuevamente o contacta al administrador.'

/**
 * Mensaje simple para cuando el servidor no envía uno apto para el usuario
 * final (se elige según el código HTTP).
 */
function mensajePorEstado(status: number): string {
  switch (status) {
    case 400:
      return 'La información enviada no es válida. Revisa los datos e inténtalo de nuevo.'
    case 401:
      return 'Tu sesión no es válida o expiró. Inicia sesión nuevamente.'
    case 403:
      return 'No tienes permisos para realizar esta acción.'
    case 404:
      return 'No se encontró la información solicitada.'
    case 409:
      return 'La operación no se pudo completar porque la información ya existe o está en uso.'
    case 503:
      return 'El servicio no está disponible en este momento. Intenta nuevamente en unos minutos.'
    default:
      return MENSAJE_ERROR_INESPERADO
  }
}

/** Detecta mensajes técnicos que no deben mostrarse al usuario final. */
function pareceTecnico(mensaje: string): boolean {
  const m = mensaje.toLowerCase()
  return (
    m.includes('exception') ||
    m.includes('sqlstate') ||
    m.includes('json') ||
    m.includes('duplicate key') ||
    m.includes('violates') ||
    m.includes('syntax error') ||
    m.includes('null value in column') ||
    m.includes('invalid uuid') ||
    m.includes('for input string') ||
    m.includes('cannot deserialize') ||
    m.includes('org.postgresql') ||
    m.includes('java.') ||
    m.includes('com.itq')
  )
}

/**
 * Devuelve el mensaje apto para el usuario final. Los mensajes del backend se
 * respetan salvo que parezcan técnicos; en ese caso se usa uno simple.
 */
function mensajeSeguro(status: number, mensajeBackend?: string | null): string {
  if (mensajeBackend && mensajeBackend.trim() && !pareceTecnico(mensajeBackend)) {
    return mensajeBackend
  }
  return mensajePorEstado(status)
}

async function solicitar<T>(
  method: string,
  path: string,
  opciones: { body?: unknown; params?: QueryParams } = {},
): Promise<T> {
  let respuesta: Response

  const token = obtenerToken()
  const headers: Record<string, string> = {}
  if (opciones.body !== undefined) headers['Content-Type'] = 'application/json'
  if (token) headers['Authorization'] = `Bearer ${token}`

  try {
    respuesta = await fetch(construirUrl(path, opciones.params), {
      method,
      headers: Object.keys(headers).length ? headers : undefined,
      body: opciones.body !== undefined ? JSON.stringify(opciones.body) : undefined,
    })
  } catch (causa) {
    console.error(`[API] ${method} ${path}: no se pudo contactar al servidor`, causa)
    throw new ApiError(0, 'No se pudo contactar al servidor. Revisa tu conexión e inténtalo de nuevo.')
  }

  // DELETE exitoso devuelve 204 sin cuerpo.
  if (respuesta.status === 204) return undefined as T

  const texto = await respuesta.text()
  let cuerpo: ApiResponse<T> | null = null

  if (texto) {
    try {
      cuerpo = JSON.parse(texto) as ApiResponse<T>
    } catch {
      console.error(
        `[API] ${method} ${path}: la respuesta del servidor no es JSON válido`,
        texto.slice(0, 500),
      )
      throw new ApiError(
        respuesta.status,
        'El servidor devolvió una respuesta inesperada. Intenta nuevamente.',
      )
    }
  }

  if (!respuesta.ok || (cuerpo && cuerpo.exito === false)) {
    const mensajeBackend = cuerpo?.mensaje
    // El detalle técnico queda en la consola del navegador (log); el usuario ve
    // siempre un mensaje simple y entendible.
    console.error(
      `[API] ${method} ${path} → HTTP ${respuesta.status}`,
      mensajeBackend ?? `Error ${respuesta.status}`,
    )

    const error = new ApiError(respuesta.status, mensajeSeguro(respuesta.status, mensajeBackend))

    // Sesión ausente/expirada: no tiene sentido seguir en la app, se limpia y
    // se manda al login. No se dispara si la petición era el propio login
    // (ahí un 401 es "credenciales inválidas", lo maneja la pantalla).
    if (error.esNoAutenticado && path !== '/api/auth/login') {
      limpiarSesion()
      manejadorSesionExpirada?.()
    }

    // 403 con la veterinaria desactivada: NO se cierra la sesión (el usuario
    // sigue siendo válido), se bloquea toda la app con un aviso hasta que
    // pague/reactiven la cuenta. Se distingue de un 403 "sin permiso" por el
    // mensaje que arma AuthFilter para ese caso puntual.
    if (error.esProhibido && /desactivad/i.test(error.message)) {
      useBloqueoEmpresa.getState().bloquear(error.message)
    }

    throw error
  }

  return (cuerpo?.datos ?? (undefined as T)) as T
}

export const api = {
  get: <T>(path: string, params?: QueryParams) => solicitar<T>('GET', path, { params }),
  post: <T>(path: string, body: unknown) => solicitar<T>('POST', path, { body }),
  put: <T>(path: string, body: unknown, params?: QueryParams) =>
    solicitar<T>('PUT', path, { body, params }),
  delete: (path: string, params?: QueryParams) => solicitar<void>('DELETE', path, { params }),
}
