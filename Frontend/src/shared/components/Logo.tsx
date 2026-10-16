/**
 * Logotipos de ControlPets.
 *
 * Los tres recursos viven en `public/` y son PNG con transparencia:
 *  - `LogoControlPets.png`      solo el isotipo (funciona sobre claro y oscuro).
 *  - `LogoControlPetsfull.png`  isotipo + letras (pensado para fondos claros).
 *  - `LogoControlPetsletras.png` solo letras (pensado para fondos claros).
 *
 * Para las letras de la marca se prefiere `<LogoTexto />` / `<MarcaControlPets />`:
 * se renderizan con la tipografía y la paleta del sistema, así el nombre se ve
 * nítido a cualquier tamaño y destaca sobre el isotipo. Los PNG con letras
 * siguen disponibles para piezas donde se necesite el logotipo original.
 */

import { cn } from '@/shared/lib/utils'

interface PropsLogo {
  className?: string
  /** Texto alternativo; usar "" cuando el isotipo acompaña al nombre visible. */
  alt?: string
}

/** Solo el isotipo. Se ve bien sobre fondos claros y oscuros. */
export function LogoIcono({ className, alt = 'ControlPets' }: PropsLogo) {
  return <img src="/LogoControlPets.png" alt={alt} className={className} />
}

/** Isotipo + letras. Para fondos claros. */
export function LogoCompleto({ className, alt = 'ControlPets' }: PropsLogo) {
  return <img src="/LogoControlPetsfull.png" alt={alt} className={className} />
}

/** Solo las letras. Para fondos claros. */
export function LogoLetras({ className, alt = 'ControlPets' }: PropsLogo) {
  return <img src="/LogoControlPetsletras.png" alt={alt} className={className} />
}

/* --------------------------------------------------------- Marca ControlPets */

type TamanoMarca = 'sm' | 'md' | 'lg'

/** `claro` = sobre fondos oscuros (sidebar, portadas teal); `oscuro` = sobre fondos claros. */
type VarianteMarca = 'claro' | 'oscuro'

const TAMANOS: Record<TamanoMarca, { marco: string; icono: string; texto: string }> = {
  sm: { marco: 'h-8 w-8 rounded-lg', icono: 'h-5 w-5', texto: 'text-base' },
  md: {
    marco: 'h-9 w-9 rounded-lg sm:h-11 sm:w-11 sm:rounded-xl',
    icono: 'h-6 w-6 sm:h-7 sm:w-7',
    texto: 'text-lg sm:text-xl',
  },
  lg: {
    marco: 'h-11 w-11 rounded-xl sm:h-14 sm:w-14 sm:rounded-2xl',
    icono: 'h-7 w-7 sm:h-8 sm:w-8',
    texto: 'text-xl sm:text-2xl',
  },
}

const VARIANTES: Record<VarianteMarca, { base: string; acento: string; marco: string }> = {
  claro: {
    base: 'text-white',
    acento: 'text-acento-300',
    marco: 'bg-white/15 ring-1 ring-white/20',
  },
  oscuro: {
    base: 'text-slate-900',
    acento: 'text-acento-500',
    marco: 'bg-white shadow-suave ring-1 ring-brand-100',
  },
}

/**
 * Nombre de la marca: "Control" en tinta/blanco + "Pets" en el azul de apoyo,
 * con peso alto y tracking cerrado. Pensado para colocarse junto al isotipo.
 *
 * `claro`  → fondos oscuros (sidebar teal, panel de acceso, hero).
 * `oscuro` → fondos claros (tarjetas, login móvil).
 */
export function LogoTexto({
  tamano = 'md',
  variante = 'claro',
  className,
}: {
  tamano?: TamanoMarca
  variante?: VarianteMarca
  className?: string
}) {
  return (
    <span
      className={cn(
        'font-extrabold leading-none tracking-tight',
        TAMANOS[tamano].texto,
        VARIANTES[variante].base,
        className,
      )}
    >
      Control
      <span className={VARIANTES[variante].acento}>Pets</span>
    </span>
  )
}

/**
 * Lockup completo de marca: isotipo enmarcado + nombre de la aplicación.
 * Es la forma recomendada de mostrar la marca en navbar, sidebar, login y pie.
 */
export function MarcaControlPets({
  tamano = 'md',
  variante = 'claro',
  className,
}: {
  tamano?: TamanoMarca
  variante?: VarianteMarca
  className?: string
}) {
  const t = TAMANOS[tamano]
  const v = VARIANTES[variante]
  return (
    <span className={cn('inline-flex items-center gap-3', className)}>
      <span
        className={cn('flex shrink-0 items-center justify-center', t.marco, v.marco)}
        aria-hidden="true"
      >
        <LogoIcono alt="" className={t.icono} />
      </span>
      <LogoTexto tamano={tamano} variante={variante} />
    </span>
  )
}
