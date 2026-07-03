/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Base URL of the backend API. Defaults to "/api" (proxied by Vite in dev). */
  readonly VITE_API_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
