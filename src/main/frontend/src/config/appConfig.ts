/**
 * Pokemon Order Planning Application Configuration
 * Centralizes all configurable parameters via .env
 */

// ===============================================
// TYPES ET INTERFACES
// ===============================================

export interface DateConfig {
  orderStartDate: string    // Start date for orders to process
  planningStartDate: string // Processing start date
  planningEndDate: string   // Processing end date
}




export interface SystemConfig {
  apiBaseUrl: string
  defaultEmployees: number
  defaultTimePerCard: number
  defaultPageSize: number
  debugMode: boolean
}

export interface AppConfig {
  title: string
  version: string
  locale: string
}

export interface PriorityMapping {
  urgent: string[]
  high: string[]
  medium: string[]
  low: string[]
}

// ===============================================
// FONCTIONS UTILITAIRES
// ===============================================



/**
 * Parse une variable d'environnement en tableau
 */
const parseArrayEnv = (envVar: string | undefined, defaultValue: string[] = []): string[] => {
  if (!envVar) return defaultValue
  return envVar.split(',').map(item => item.trim()).filter(Boolean)
}

/**
 * Parse une variable d'environnement en nombre
 */
const parseNumberEnv = (envVar: string | undefined, defaultValue: number): number => {
  if (!envVar) return defaultValue
  const parsed = parseInt(envVar, 10)
  return isNaN(parsed) ? defaultValue : parsed
}

/**
 * Parse une variable d'environnement en booléen
 */
const parseBooleanEnv = (envVar: string | undefined, defaultValue: boolean): boolean => {
  if (!envVar) return defaultValue
  return envVar.toLowerCase() === 'true'
}

/**
 * Calcule une date par défaut en ajoutant des jours à une date de base
 */
const addDays = (dateStr: string, days: number): string => {
  const date = new Date(dateStr)
  date.setDate(date.getDate() + days)
  return date.toISOString().split('T')[0]
}

/**
 * Obtient la date d'aujourd'hui au format YYYY-MM-DD
 */
const getTodayString = (): string => {
  return new Date().toISOString().split('T')[0]
}

// ===============================================
// CONFIGURATION PRINCIPALE
// ===============================================

/**
 * Processing date configuration
 */

export const dateConfig: DateConfig = {
  // Commandes à partir du 1er juin 2025 (comme demandé)
  orderStartDate: import.meta.env.VITE_ORDER_START_DATE || '2025-06-01',

  // Dates de planification depuis .env
  planningStartDate: import.meta.env.VITE_PLANNING_START_DATE || getTodayString(),
  planningEndDate: import.meta.env.VITE_PLANNING_END_DATE ||
    addDays(import.meta.env.VITE_PLANNING_START_DATE || getTodayString(), 7)
}

/**
 * System configuration
 */
export const systemConfig: SystemConfig = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  defaultEmployees: parseNumberEnv(import.meta.env.VITE_DEFAULT_EMPLOYEES, 4),
  defaultTimePerCard: parseNumberEnv(import.meta.env.VITE_DEFAULT_TIME_PER_CARD, 3),
  defaultPageSize: parseNumberEnv(import.meta.env.VITE_DEFAULT_PAGE_SIZE, 20),
  debugMode: parseBooleanEnv(import.meta.env.VITE_DEBUG_MODE, true)
}

/**
 * Application configuration
 */
export const appConfig: AppConfig = {
  title: import.meta.env.VITE_APP_TITLE || 'Pokemon Order Planning System',
  version: import.meta.env.VITE_APP_VERSION || '1.0.0',
  locale: import.meta.env.VITE_DEFAULT_LOCALE || 'en'
}

/**
 * Priority mapping from database codes to standardized names
 */
export const priorityMapping: PriorityMapping = {
  urgent: parseArrayEnv(import.meta.env.VITE_PRIORITY_URGENT, ['X', 'URGENT']),
  high: parseArrayEnv(import.meta.env.VITE_PRIORITY_HIGH, ['F+', 'F', 'HIGH', 'HAUTE']),
  medium: parseArrayEnv(import.meta.env.VITE_PRIORITY_MEDIUM, ['MEDIUM', 'NORMALE']),
  low: parseArrayEnv(import.meta.env.VITE_PRIORITY_LOW, ['C', 'LOW', 'BASSE'])
}

// ===============================================
// FONCTIONS D'AIDE
// ===============================================

/**
 * Gets the complete configuration
 */
export const getFullConfig = () => {
  return {
    dates: dateConfig,
    system: systemConfig,
    app: appConfig,
    priorities: priorityMapping
  }
}

/**
 * Maps a priority code to a standardized name
 */
export const mapPriorityCode = (code: string): 'URGENT' | 'HIGH' | 'MEDIUM' | 'LOW' => {
  const upperCode = code.toUpperCase()

  if (priorityMapping.urgent.includes(upperCode)) return 'URGENT'
  if (priorityMapping.high.includes(upperCode)) return 'HIGH'
  if (priorityMapping.low.includes(upperCode)) return 'LOW'

  return 'MEDIUM' // Default
}

/**
 * Formats a date for display
 */
export const formatDisplayDate = (dateStr: string): string => {
  try {
    return new Date(dateStr).toLocaleDateString(appConfig.locale === 'fr' ? 'fr-FR' : 'en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    })
  } catch {
    return dateStr
  }
}

/**
 * Checks if an order date is within the processing range
 */
export const isOrderInDateRange = (orderDate: string): boolean => {
  const order = new Date(orderDate)
  const start = new Date(dateConfig.orderStartDate)
  const end = new Date(dateConfig.planningEndDate)

  return order >= start && order <= end
}

/**
 * Debug log (only if enabled)
 */
export const debugLog = (message: string, ...args: any[]) => {
  if (systemConfig.debugMode) {
    console.log(`[Config] ${message}`, ...args)
  }
}

// ===============================================
// INITIALISATION ET VALIDATION
// ===============================================

/**
 * Validates configuration at startup
 */
export const validateConfig = (): { valid: boolean; errors: string[] } => {
  const errors: string[] = []

  // Check dates
  const orderStart = new Date(dateConfig.orderStartDate)
  const planningStart = new Date(dateConfig.planningStartDate)
  const planningEnd = new Date(dateConfig.planningEndDate)

  if (isNaN(orderStart.getTime())) {
    errors.push(`Invalid ORDER_START_DATE: ${dateConfig.orderStartDate}`)
  }

  if (isNaN(planningStart.getTime())) {
    errors.push(`Invalid PLANNING_START_DATE: ${dateConfig.planningStartDate}`)
  }

  if (isNaN(planningEnd.getTime())) {
    errors.push(`Invalid PLANNING_END_DATE: ${dateConfig.planningEndDate}`)
  }

  if (planningEnd <= planningStart) {
    errors.push('PLANNING_END_DATE must be after PLANNING_START_DATE')
  }

  // Check numeric values
  if (systemConfig.defaultEmployees < 1 || systemConfig.defaultEmployees > 50) {
    errors.push('DEFAULT_EMPLOYEES must be between 1 and 50')
  }

  if (systemConfig.defaultTimePerCard < 1 || systemConfig.defaultTimePerCard > 60) {
    errors.push('DEFAULT_TIME_PER_CARD must be between 1 and 60 minutes')
  }

  return {
    valid: errors.length === 0,
    errors
  }
}

// ===============================================
// LOG DE DÉMARRAGE
// ===============================================

// Validate and display configuration at startup
const validation = validateConfig()

if (validation.valid) {
  debugLog('Configuration loaded successfully:', getFullConfig())
  debugLog(`📅 Orders from: ${formatDisplayDate(dateConfig.orderStartDate)}`)
  debugLog(`🚀 Planning from: ${formatDisplayDate(dateConfig.planningStartDate)} to ${formatDisplayDate(dateConfig.planningEndDate)}`)
} else {
  console.error('❌ Configuration validation failed:', validation.errors)
}

// Export default configuration as well
export default {
  dates: dateConfig,
  system: systemConfig,
  app: appConfig,
  priorities: priorityMapping,
  mapPriorityCode,
  formatDisplayDate,
  isOrderInDateRange,
  debugLog,
  validateConfig
}
