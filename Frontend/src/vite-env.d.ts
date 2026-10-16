/// <reference types="vite/client" />

interface ImportMetaEnv {
  /**
   * URL base de la API. Vacía en desarrollo: el proxy de Vite reenvía /api
   * al contexto de Tomcat. En producción se define al construir, p. ej.
   * VITE_API_URL=http://servidor:8080/VETERINARIAITQ
   */
  readonly VITE_API_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
