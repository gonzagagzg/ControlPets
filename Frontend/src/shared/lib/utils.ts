import { addDays, format, parseISO } from 'date-fns'
import { es } from 'date-fns/locale'

/** Concatena clases ignorando valores falsy. */
export function cn(...clases: (string | false | null | undefined)[]) {
  return clases.filter(Boolean).join(' ')
}

export function formatearFecha(valor: string | null | undefined) {
  if (!valor) return '—'
  try {
    return format(parseISO(valor), 'dd/MM/yyyy', { locale: es })
  } catch {
    return valor
  }
}

export function formatearFechaHora(valor: string | null | undefined) {
  if (!valor) return '—'
  try {
    return format(parseISO(valor), "dd/MM/yyyy HH:mm", { locale: es })
  } catch {
    return valor
  }
}

const formateadorMoneda = new Intl.NumberFormat('es-EC', {
  style: 'currency',
  currency: 'USD',
})

export function formatearMoneda(valor: number | null | undefined) {
  if (valor === null || valor === undefined) return '—'
  return formateadorMoneda.format(valor)
}

/**
 * Convierte un valor ISO con offset al formato que espera <input type="datetime-local">.
 * El input trabaja en hora local sin zona, así que se recorta a 'YYYY-MM-DDTHH:mm'.
 */
export function aInputDateTime(iso: string | null | undefined) {
  if (!iso) return ''
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return ''
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/** Inverso de `aInputDateTime`: produce ISO-8601 con offset, que es lo que parsea OffsetDateTime. */
export function desdeInputDateTime(valor: string): string {
  if (!valor) return ''
  return new Date(valor).toISOString()
}

/**
 * Fecha local de hoy en formato `YYYY-MM-DD`.
 *
 * Se calcula con la hora del usuario y no con UTC (a diferencia de
 * `toISOString().slice(0, 10)`): en zonas como Ecuador (UTC-5) por la noche el
 * día UTC ya es el de mañana y el mínimo de un `input` de fecha quedaría
 * corrido.
 */
export function hoyLocalISO(): string {
  return format(new Date(), 'yyyy-MM-dd')
}

/** Fecha local de mañana en formato `YYYY-MM-DD`. */
export function mananaLocalISO(): string {
  return format(addDays(new Date(), 1), 'yyyy-MM-dd')
}

/** Índice id -> registro, para resolver claves foráneas sin recorrer arreglos. */
export function indexarPor<T, K extends keyof T>(
  registros: T[] | undefined,
  campo: K,
): Map<string, T> {
  const mapa = new Map<string, T>()
  for (const r of registros ?? []) mapa.set(String(r[campo]), r)
  return mapa
}

export function calcularEdad(fechaNacimiento: string | null | undefined) {
  if (!fechaNacimiento) return '—'
  const nacimiento = parseISO(fechaNacimiento)
  const meses =
    (new Date().getFullYear() - nacimiento.getFullYear()) * 12 +
    (new Date().getMonth() - nacimiento.getMonth())
  if (meses < 0) return '—'
  if (meses < 12) return `${meses} ${meses === 1 ? 'mes' : 'meses'}`
  const anios = Math.floor(meses / 12)
  return `${anios} ${anios === 1 ? 'año' : 'años'}`
}
