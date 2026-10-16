/**
 * Validaciones de formato reutilizables por todos los formularios.
 *
 * Adelantan al cliente el feedback que el backend ya aplica en servidor
 * (ver ClienteService, EmpresaService y UsuarioService) para que el usuario
 * no pueda escribir valores incongruentes: nombres con números, teléfonos con
 * letras o de más de 10 dígitos, correos mal formados, etc. El backend sigue
 * siendo la fuente de verdad; estos formatos son estrictamente iguales o más
 * restrictivos que sus validaciones.
 */

/* ------------------------------------------------------------- Patrones */

/** Letras (con acentos y ñ), espacios y separadores propios de un nombre. */
export const PATRON_NOMBRE = /^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ'’.,\- ]+$/

/** Usuario de acceso: letras, números, punto, guion y guion bajo (sin espacios). */
export const PATRON_USUARIO = /^[A-Za-z0-9._-]+$/

/** Código interno (ej. VET-001): letras, números y guiones. */
export const PATRON_CODIGO = /^[A-Za-z0-9-]+$/

/** Solo dígitos. */
export const PATRON_DIGITOS = /^\d+$/

/** Correo electrónico (mismo patrón que `ClienteService.PATRON_CORREO`). */
export const PATRON_CORREO = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/

/** Dominios admitidos al registrar veterinarias. */
export const PATRON_CORREO_VETERINARIA = /^[A-Za-z0-9+_.-]+@(gmail\.com|outlook\.com)$/

/** Ecuador: los teléfonos móviles tienen 10 dígitos; los fijos, 9. */
export const MAX_DIGITOS_TELEFONO = 10
export const MIN_DIGITOS_TELEFONO = 7

/* --------------------------------------------------------- Limpiadores */

/** Quita todo lo que no sea letra, espacio o separador de nombre. */
export function limpiarNombre(valor: string): string {
  return valor.replace(/[^A-Za-zÁÉÍÓÚÜÑáéíóúüñ'’.,\- ]/g, '')
}

/** Quita espacios y símbolos no válidos para un nombre de usuario. */
export function limpiarUsuario(valor: string): string {
  return valor.replace(/[^A-Za-z0-9._-]/g, '')
}

/** Quita todo lo que no sea letra, número o guion (códigos internos). */
export function limpiarCodigo(valor: string): string {
  return valor.replace(/[^A-Za-z0-9-]/g, '')
}

/** Deja únicamente dígitos. */
export function limpiarDigitos(valor: string): string {
  return valor.replace(/\D/g, '')
}

/** Deja solo dígitos y recorta a la longitud máxima de un teléfono. */
export function limpiarTelefono(valor: string): string {
  return limpiarDigitos(valor).slice(0, MAX_DIGITOS_TELEFONO)
}

/**
 * Normaliza un número decimal mientras se escribe: acepta coma o punto como
 * separador y deja como máximo 2 decimales (ej. "12,5" -> "12.5").
 */
export function limpiarDecimal(valor: string): string {
  const partes = valor
    .replace(/,/g, '.')
    .replace(/[^0-9.]/g, '')
    .split('.')
  const entero = partes[0] ?? ''
  if (partes.length === 1) return entero
  return `${entero || '0'}.${partes.slice(1).join('').slice(0, 2)}`
}

/* ----------------------------------------------------------- Formatos */

export type FormatoCampo =
  | 'nombre'
  | 'usuario'
  | 'codigo'
  | 'digitos'
  | 'telefono'
  | 'correo'
  | 'correoVeterinaria'
  | 'decimal'

export interface DefinicionFormato {
  /** Patrón que debe cumplir el valor al enviar. */
  patron: RegExp
  /** Mensaje mostrado cuando el valor no cumple el patrón. */
  mensaje: string
  /** `inputMode` sugerido para abrir el teclado adecuado en móvil. */
  inputMode?: 'text' | 'tel' | 'numeric' | 'decimal' | 'email'
  /** Longitud máxima de caracteres (se combina con `maxLength` del campo). */
  maxLength?: number
  /** Longitud mínima de caracteres. */
  minLength?: number
  /** Limpia el valor mientras se escribe (bloquea los caracteres prohibidos). */
  filtrar?: (valor: string) => string
}

export const FORMATOS: Record<FormatoCampo, DefinicionFormato> = {
  nombre: {
    patron: PATRON_NOMBRE,
    mensaje: 'Solo se permiten letras y espacios, sin números',
    filtrar: limpiarNombre,
  },
  usuario: {
    patron: PATRON_USUARIO,
    mensaje: 'Solo letras, números, punto, guion o guion bajo (sin espacios)',
    filtrar: limpiarUsuario,
  },
  codigo: {
    patron: PATRON_CODIGO,
    mensaje: 'Solo se permiten letras, números y guiones',
    filtrar: limpiarCodigo,
  },
  digitos: {
    patron: PATRON_DIGITOS,
    mensaje: 'Solo se permiten números',
    inputMode: 'numeric',
    filtrar: limpiarDigitos,
  },
  telefono: {
    patron: PATRON_DIGITOS,
    mensaje: 'El teléfono solo admite números',
    inputMode: 'tel',
    maxLength: MAX_DIGITOS_TELEFONO,
    minLength: MIN_DIGITOS_TELEFONO,
    filtrar: limpiarTelefono,
  },
  correo: {
    patron: PATRON_CORREO,
    mensaje: 'Ingresa un correo electrónico válido',
    inputMode: 'email',
  },
  correoVeterinaria: {
    patron: PATRON_CORREO_VETERINARIA,
    mensaje: 'Solo se permiten correos @gmail.com o @outlook.com',
    inputMode: 'email',
  },
  decimal: {
    patron: /^\d+(\.\d{0,2})?$/,
    mensaje: 'Ingresa un número válido (máximo 2 decimales)',
    inputMode: 'decimal',
    filtrar: limpiarDecimal,
  },
}
