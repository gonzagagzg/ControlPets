import { useEffect, useRef, useState, type ReactNode } from 'react'
import { cn } from '@/shared/lib/utils'

/**
 * Revela su contenido con una animación de entrada cuando entra en el
 * viewport (IntersectionObserver). Se anima una sola vez y respeta
 * `prefers-reduced-motion` (ver index.css). Pensado para secciones y
 * listas que quedan por debajo del pliegue.
 */
export function Revelar({
  children,
  className,
  retraso = 0,
}: {
  children: ReactNode
  className?: string
  /** Retardo en ms para escalonar varios elementos entre sí. */
  retraso?: number
}) {
  const ref = useRef<HTMLDivElement>(null)
  const [visible, setVisible] = useState(false)

  useEffect(() => {
    const el = ref.current
    if (!el) return

    if (typeof IntersectionObserver === 'undefined') {
      setVisible(true)
      return
    }

    const observador = new IntersectionObserver(
      (entradas) => {
        if (entradas.some((entrada) => entrada.isIntersecting)) {
          setVisible(true)
          observador.disconnect()
        }
      },
      { threshold: 0.12, rootMargin: '0px 0px -40px 0px' },
    )

    observador.observe(el)
    return () => observador.disconnect()
  }, [])

  return (
    <div
      ref={ref}
      className={cn(visible ? 'animate-entrada' : 'opacity-0', className)}
      style={{ animationDelay: `${retraso}ms` }}
    >
      {children}
    </div>
  )
}
