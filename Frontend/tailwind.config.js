/** @type {import('tailwindcss').Config} */

/**
 * Identidad visual ControlPets (DEVs ITQ)
 * ---------------------------------------
 *  Marca / primario — navbar, sidebar, encabezados, botones, éxito .... #189D8B
 *  Secundario / enlaces — links y elementos de apoyo ................... #3BA3ED
 *  Superficies de fondo — página y estructura ................ #E6F4F1 / #E1EBEB
 *  Alerta / acciones destructivas ...................................... #FF6B6B
 *  Texto — cuerpo y titulares ............................... #1F2937 / #111111
 *
 * La paleta se resuelve aquí (y no componente por componente) para que todo
 * el aplicativo herede la identidad de marca de forma consistente.
 *
 * Notas de mantenimiento:
 *  - `brand`    → teal de marca. Se usa para superficies oscuras, encabezados,
 *                 botones primarios y estados de éxito.
 *  - `acento`   → azul de apoyo. Enlaces, elementos secundarios e íconos de
 *                 apoyo. Antes llamado `gold` (ver historial del proyecto).
 *  - `slate`    → neutros de la marca: escalas bajas = superficies menta
 *                 (#E6F4F1 / #E1EBEB), escalas altas = tinta (#1F2937 /
 *                 #111111). Sobrescribimos el gris por defecto de Tailwind
 *                 para que los textos y bordes ya existentes tomen la tinta
 *                 de marca sin tocar cada componente.
 *  - `emerald`  → estados de éxito (mismo teal de marca).
 *  - `red`      → alertas y acciones destructivas (#FF6B6B).
 *  - `amber`    → avisos (stock mínimo, advertencias).
 */

// Teal de marca — anclado en #189D8B (600).
const brand = {
  50: '#E6F4F1',
  100: '#CDEAE4',
  200: '#A3D9CF',
  300: '#72C5B7',
  400: '#44B0A0',
  500: '#22A492',
  600: '#189D8B',
  700: '#137F71',
  800: '#0F685D',
  900: '#0B5049',
  950: '#073A35',
}

// Azul de apoyo — anclado en #3BA3ED (500).
const acento = {
  50: '#EAF5FD',
  100: '#D3EAFB',
  200: '#A9D5F7',
  300: '#7CBFF2',
  400: '#58B0EF',
  500: '#3BA3ED',
  600: '#2A8AD1',
  700: '#1F70AC',
  800: '#1A5A89',
  900: '#164A70',
}

// Alerta / destructivo — anclado en #FF6B6B (500).
const peligro = {
  50: '#FFEFF0',
  100: '#FFE0E2',
  200: '#FFC5C8',
  300: '#FFA3A7',
  400: '#FF8589',
  500: '#FF6B6B',
  600: '#E8514F',
  700: '#C63A3C',
  800: '#A02C31',
  900: '#7F2730',
}

// Avisos — ámbar cálido que convive con el coral de alertas.
const aviso = {
  50: '#FFF8EB',
  100: '#FFEFD0',
  200: '#FFDFA6',
  300: '#FFCB73',
  400: '#FBB03B',
  500: '#F5A524',
  600: '#D98314',
  700: '#B26B08',
  800: '#8F5206',
  900: '#6B3D05',
}

// Neutros de marca: superficies menta (bajos) + tinta (altos).
const neutro = {
  50: '#E6F4F1',
  100: '#E1EBEB',
  200: '#D3E2E0',
  300: '#B8CDCB',
  400: '#9CA3AF',
  500: '#6B7280',
  600: '#4B5563',
  700: '#374151',
  800: '#1F2937',
  900: '#111111',
  950: '#0B0F14',
}

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        brand,
        acento,
        emerald: brand,
        red: peligro,
        amber: aviso,
        slate: neutro,
        gray: neutro,
      },
      fontFamily: {
        sans: ['"Plus Jakarta Sans"', 'Inter', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        suave: '0 1px 2px 0 rgb(7 58 53 / 0.05), 0 1px 3px 0 rgb(7 58 53 / 0.08)',
        marca: '0 10px 30px -12px rgb(24 157 139 / 0.45)',
        flotante: '0 24px 48px -20px rgb(7 58 53 / 0.35)',
      },
      // Curva "expo-out": rápida al inicio y muy suave al final. Da el aire
      // profesional (Linear/Stripe) a las entradas y los hovers.
      transitionTimingFunction: {
        suave: 'cubic-bezier(0.16, 1, 0.3, 1)',
      },
      keyframes: {
        // Entrada de contenido: sube y aparece. Base del "reveal".
        entrada: {
          '0%': { opacity: '0', transform: 'translateY(12px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        aparecer: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        // Modales y tarjetas emergentes: escala sutil + fade.
        escala: {
          '0%': { opacity: '0', transform: 'scale(0.96)' },
          '100%': { opacity: '1', transform: 'scale(1)' },
        },
        // Drawer móvil.
        'deslizar-izq': {
          '0%': { transform: 'translateX(-100%)' },
          '100%': { transform: 'translateX(0)' },
        },
        'deslizar-der': {
          '0%': { opacity: '0', transform: 'translateX(18px)' },
          '100%': { opacity: '1', transform: 'translateX(0)' },
        },
        // Hojas inferiores y menús desplegables.
        'deslizar-arriba': {
          '0%': { opacity: '0', transform: 'translateY(12px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        // Burbujas decorativas del hero.
        flotar: {
          '0%, 100%': { transform: 'translateY(0) translateX(0)' },
          '50%': { transform: 'translateY(-14px) translateX(6px)' },
        },
        // Esqueletos de carga.
        brillo: {
          '100%': { transform: 'translateX(100%)' },
        },
        'pulso-suave': {
          '0%, 100%': { opacity: '1' },
          '50%': { opacity: '0.5' },
        },
        // Anillo que late: llama la atención sin ser agresivo (alertas/badges).
        'latido-anillo': {
          '0%': { boxShadow: '0 0 0 0 rgb(245 165 36 / 0.55)' },
          '70%': { boxShadow: '0 0 0 8px rgb(245 165 36 / 0)' },
          '100%': { boxShadow: '0 0 0 0 rgb(245 165 36 / 0)' },
        },
      },
      animation: {
        entrada: 'entrada 0.45s cubic-bezier(0.16, 1, 0.3, 1) both',
        aparecer: 'aparecer 0.25s ease-out both',
        escala: 'escala 0.22s cubic-bezier(0.16, 1, 0.3, 1) both',
        'deslizar-izq': 'deslizar-izq 0.3s cubic-bezier(0.16, 1, 0.3, 1) both',
        'deslizar-der': 'deslizar-der 0.32s cubic-bezier(0.16, 1, 0.3, 1) both',
        'deslizar-arriba': 'deslizar-arriba 0.28s cubic-bezier(0.16, 1, 0.3, 1) both',
        flotar: 'flotar 8s ease-in-out infinite',
        'pulso-suave': 'pulso-suave 2.4s ease-in-out infinite',
        'latido-anillo': 'latido-anillo 2s cubic-bezier(0.16, 1, 0.3, 1) infinite',
      },
    },
  },
  plugins: [],
}
