// ===============================================
// CONFIGURATION DES ENDPOINTS API
// ===============================================
// Fichier: src/services/utils/endpoints.ts

// ========== CONFIGURATION BASE ==========

export const API_CONFIG = {
  BASE_URL: 'http://localhost:8080',
  TIMEOUT: 30000, // 30 secondes
  RETRY_ATTEMPTS: 3,
  RETRY_DELAY: 1000, // 1 seconde
} as const

// ========== ENDPOINTS STRUCTURÉS ==========

export const ENDPOINTS = {
  // ========== ORDERS ==========
  ORDERS: {
    BASE: '/api/orders',
    LIST: '/api/orders/frontend/orders',
    DETAIL: (id: string) => `/api/orders/frontend/orders/${id}`,
    CARDS: (id: string) => `/api/orders/frontend/orders/${id}/cards`,
    CREATE: '/api/orders',
    UPDATE: (id: string) => `/api/orders/${id}`,
    DELETE: (id: string) => `/api/orders/${id}`,
    STATS: '/api/orders/stats',

    // Endpoints alternatifs pour compatibilité
    ALT_LIST: '/api/commandes/frontend/commandes',
    ALT_FRONTEND: '/api/frontend/commandes'
  },

  // ========== EMPLOYEES ==========
  EMPLOYEES: {
    BASE: '/api/employees',
    LIST: '/api/employees',
    ACTIVE: '/api/employees/active',
    DETAIL: (id: string) => `/api/employees/${id}`,
    CREATE: '/api/employees',
    UPDATE: (id: string) => `/api/employees/${id}`,
    DELETE: (id: string) => `/api/employees/${id}`,
    DEBUG: '/api/employees/debug',
    INIT_TABLE: '/api/employees/init-table',
    CREATE_TEST: '/api/employees/create-test',
    PLANNING_DATA: '/api/employees/planning-data',

    // Endpoints français pour compatibilité
    FR_BASE: '/api/employes',
    FR_ACTIVE: '/api/employes/active',
    FR_LIST: '/api/employes/frontend/liste'
  },

  // ========== PLANNING ==========
  PLANNING: {
    BASE: '/api/planning',
    LIST: '/api/planning/view-simple',
    LIST_WITH_DATE: (date: string) => `/api/planning/view-simple?date=${date}`,
    GENERATE: '/api/planning/generate',
    GENERATE_SIMPLE: '/api/planning/generate-simple',
    WITH_DETAILS: '/api/planning/plannings-with-details',
    BY_EMPLOYEE: (employeeId: string, date?: string) =>
      `/api/planning/employee/${employeeId}${date ? `?date=${date}` : ''}`,
    BY_DATE: (date: string) => `/api/planning/date/${date}`,

    // Actions
    COMPLETE: (id: string) => `/api/planning/${id}/complete`,
    CANCEL: (id: string) => `/api/planning/${id}/cancel`,
    UPDATE: (id: string) => `/api/planning/${id}`,
    DELETE: (id: string) => `/api/planning/${id}`,

    // Utilitaires
    DEBUG: '/api/planning/debug-real',
    CLEANUP: '/api/planning/cleanup',
    STATS: '/api/planning/stats',

    // Endpoints français pour compatibilité
    FR_BASE: '/api/planifications',
    FR_LIST: '/api/planifications/liste',
    FR_GENERATE: '/api/planifications/generer'
  },

  // ========== SYSTEM ==========
  SYSTEM: {
    HEALTH: '/api/health',
    DEBUG: '/api/debug',
    INFO: '/api/info',
    STATUS: '/api/status'
  }
} as const

// ========== CONSTRUCTION D'URLS ==========

/**
 * Construit une URL complète à partir d'un endpoint
 */
export const buildUrl = (endpoint: string, baseUrl = API_CONFIG.BASE_URL): string => {
  // Éviter la double slash
  const cleanBase = baseUrl.replace(/\/$/, '')
  const cleanEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`
  return `${cleanBase}${cleanEndpoint}`
}

/**
 * Construit une URL avec des paramètres de requête
 */
export const buildUrlWithParams = (
  endpoint: string,
  params: Record<string, any> = {},
  baseUrl = API_CONFIG.BASE_URL
): string => {
  const url = buildUrl(endpoint, baseUrl)

  // Filtrer les paramètres vides
  const validParams = Object.entries(params)
    .filter(([_, value]) => value !== null && value !== undefined && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)

  if (validParams.length === 0) return url

  const separator = url.includes('?') ? '&' : '?'
  return `${url}${separator}${validParams.join('&')}`
}

// ========== HELPERS POUR ENDPOINTS COMMUNS ==========

export const orderEndpoints = {
  list: () => buildUrl(ENDPOINTS.ORDERS.LIST),
  detail: (id: string) => buildUrl(ENDPOINTS.ORDERS.DETAIL(id)),
  cards: (id: string) => buildUrl(ENDPOINTS.ORDERS.CARDS(id)),
  withFilters: (filters: {
    priority?: string
    status?: string
    dateFrom?: string
    dateTo?: string
  }) => buildUrlWithParams(ENDPOINTS.ORDERS.LIST, filters)
}

export const employeeEndpoints = {
  list: () => buildUrl(ENDPOINTS.EMPLOYEES.ACTIVE),
  detail: (id: string) => buildUrl(ENDPOINTS.EMPLOYEES.DETAIL(id)),
  create: () => buildUrl(ENDPOINTS.EMPLOYEES.CREATE),
  planningData: (date?: string) =>
    buildUrlWithParams(ENDPOINTS.EMPLOYEES.PLANNING_DATA, { date })
}

export const planningEndpoints = {
  list: () => buildUrl(ENDPOINTS.PLANNING.LIST),
  listByDate: (date: string) => buildUrl(ENDPOINTS.PLANNING.LIST_WITH_DATE(date)),
  generate: () => buildUrl(ENDPOINTS.PLANNING.GENERATE),
  byEmployee: (employeeId: string, date?: string) =>
    buildUrl(ENDPOINTS.PLANNING.BY_EMPLOYEE(employeeId, date)),
  debug: () => buildUrl(ENDPOINTS.PLANNING.DEBUG)
}

// ========== FALLBACK ENDPOINTS ==========

/**
 * Liste d'endpoints alternatifs pour chaque type de données
 * Utile pour la résilience et la compatibilité
 */
export const FALLBACK_ENDPOINTS = {
  ORDERS: [
    ENDPOINTS.ORDERS.LIST,
    ENDPOINTS.ORDERS.ALT_LIST,
    ENDPOINTS.ORDERS.ALT_FRONTEND
  ],

  EMPLOYEES: [
    ENDPOINTS.EMPLOYEES.ACTIVE,
    ENDPOINTS.EMPLOYEES.LIST,
    ENDPOINTS.EMPLOYEES.FR_ACTIVE
  ],

  PLANNING: [
    ENDPOINTS.PLANNING.LIST,
    ENDPOINTS.PLANNING.WITH_DETAILS,
    ENDPOINTS.PLANNING.FR_LIST
  ]
} as const

// ========== CONFIGURATION PAR ENVIRONNEMENT ==========

export const getApiConfig = (environment: 'development' | 'production' | 'test' = 'development') => {
  const configs = {
    development: {
      ...API_CONFIG,
      BASE_URL: 'http://localhost:8080'
    },
    production: {
      ...API_CONFIG,
      BASE_URL: process.env.VITE_API_BASE_URL || 'https://your-api.com'
    },
    test: {
      ...API_CONFIG,
      BASE_URL: 'http://localhost:3001',
      TIMEOUT: 5000
    }
  }

  return configs[environment]
}

// ========== EXPORT PAR DÉFAUT ==========

export default {
  ENDPOINTS,
  API_CONFIG,
  buildUrl,
  buildUrlWithParams,
  orderEndpoints,
  employeeEndpoints,
  planningEndpoints,
  FALLBACK_ENDPOINTS,
  getApiConfig
}
