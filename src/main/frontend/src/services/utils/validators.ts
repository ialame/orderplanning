// ===============================================
// VALIDATEURS DE RÉPONSES API
// ===============================================
// Fichier: src/services/utils/validators.ts

// ========== TYPES DE VALIDATION ==========

export interface ValidationResult {
  isValid: boolean
  errors: string[]
  warnings: string[]
  data?: any
}

export interface FieldValidation {
  field: string
  required: boolean
  type: 'string' | 'number' | 'boolean' | 'array' | 'object' | 'date' | 'email'
  minLength?: number
  maxLength?: number
  pattern?: RegExp
  validator?: (value: any) => boolean
}

// ========== HELPERS DE VALIDATION ==========

/**
 * Vérifie si une valeur existe et n'est pas null/undefined
 */
const exists = (value: any): boolean => {
  return value !== null && value !== undefined && value !== ''
}

/**
 * Vérifie le type d'une valeur
 */
const isOfType = (value: any, type: string): boolean => {
  switch (type) {
    case 'string':
      return typeof value === 'string'
    case 'number':
      return typeof value === 'number' && !isNaN(value)
    case 'boolean':
      return typeof value === 'boolean'
    case 'array':
      return Array.isArray(value)
    case 'object':
      return typeof value === 'object' && value !== null && !Array.isArray(value)
    case 'date':
      return value instanceof Date || (typeof value === 'string' && !isNaN(Date.parse(value)))
    case 'email':
      return typeof value === 'string' && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)
    default:
      return false
  }
}

/**
 * Valide un champ selon ses règles
 */
const validateField = (value: any, validation: FieldValidation): string[] => {
  const errors: string[] = []

  // Vérification de l'existence pour les champs requis
  if (validation.required && !exists(value)) {
    errors.push(`${validation.field} is required`)
    return errors // Si requis et manquant, on arrête là
  }

  // Si le champ n'existe pas mais n'est pas requis, c'est OK
  if (!exists(value) && !validation.required) {
    return errors
  }

  // Vérification du type
  if (!isOfType(value, validation.type)) {
    errors.push(`${validation.field} must be of type ${validation.type}`)
  }

  // Vérifications spécifiques aux chaînes
  if (validation.type === 'string' && typeof value === 'string') {
    if (validation.minLength && value.length < validation.minLength) {
      errors.push(`${validation.field} must be at least ${validation.minLength} characters`)
    }
    if (validation.maxLength && value.length > validation.maxLength) {
      errors.push(`${validation.field} must be at most ${validation.maxLength} characters`)
    }
    if (validation.pattern && !validation.pattern.test(value)) {
      errors.push(`${validation.field} format is invalid`)
    }
  }

  // Validateur personnalisé
  if (validation.validator && !validation.validator(value)) {
    errors.push(`${validation.field} failed custom validation`)
  }

  return errors
}

/**
 * Valide un objet selon un schéma de champs
 */
const validateObject = (data: any, schema: FieldValidation[]): ValidationResult => {
  const errors: string[] = []
  const warnings: string[] = []

  if (!data || typeof data !== 'object') {
    return {
      isValid: false,
      errors: ['Data must be an object'],
      warnings: []
    }
  }

  // Valider chaque champ
  for (const fieldValidation of schema) {
    const value = data[fieldValidation.field]
    const fieldErrors = validateField(value, fieldValidation)
    errors.push(...fieldErrors)
  }

  // Vérifier les champs inattendus (warnings)
  const expectedFields = schema.map(s => s.field)
  const actualFields = Object.keys(data)
  const unexpectedFields = actualFields.filter(field => !expectedFields.includes(field))

  if (unexpectedFields.length > 0) {
    warnings.push(`Unexpected fields: ${unexpectedFields.join(', ')}`)
  }

  return {
    isValid: errors.length === 0,
    errors,
    warnings,
    data
  }
}

// ========== SCHÉMAS DE VALIDATION ==========

/**
 * Schéma de validation pour les orders
 */
export const ORDER_SCHEMA: FieldValidation[] = [
  { field: 'id', required: true, type: 'string', minLength: 1 },
  { field: 'orderNumber', required: true, type: 'string', minLength: 1 },
  { field: 'cardCount', required: true, type: 'number', validator: (v) => v >= 0 },
  { field: 'cardsWithName', required: false, type: 'number', validator: (v) => v >= 0 },
  { field: 'percentageWithName', required: false, type: 'number', validator: (v) => v >= 0 && v <= 100 },
  { field: 'totalPrice', required: false, type: 'number', validator: (v) => v >= 0 },
  {
    field: 'priority',
    required: false,
    type: 'string',
    validator: (v) => ['URGENT', 'HIGH', 'MEDIUM', 'LOW'].includes(v)
  },
  {
    field: 'status',
    required: false,
    type: 'string',
    validator: (v) => ['PENDING', 'SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'].includes(v)
  },
  { field: 'statusCode', required: false, type: 'number' },
  { field: 'creationDate', required: false, type: 'date' },
  { field: 'deadline', required: false, type: 'date' },
  { field: 'estimatedTimeMinutes', required: false, type: 'number', validator: (v) => v >= 0 }
]

/**
 * Schéma de validation pour les employees
 */
export const EMPLOYEE_SCHEMA: FieldValidation[] = [
  { field: 'id', required: true, type: 'string', minLength: 1 },
  { field: 'firstName', required: true, type: 'string', minLength: 1, maxLength: 100 },
  { field: 'lastName', required: true, type: 'string', minLength: 1, maxLength: 100 },
  { field: 'fullName', required: false, type: 'string' },
  { field: 'email', required: false, type: 'email' },
  { field: 'workHoursPerDay', required: false, type: 'number', validator: (v) => v > 0 && v <= 24 },
  { field: 'active', required: false, type: 'boolean' },
  { field: 'creationDate', required: false, type: 'date' },
  { field: 'available', required: false, type: 'boolean' },
  { field: 'currentLoad', required: false, type: 'number', validator: (v) => v >= 0 }
]

/**
 * Schéma de validation pour les plannings
 */
export const PLANNING_SCHEMA: FieldValidation[] = [
  { field: 'id', required: true, type: 'string', minLength: 1 },
  { field: 'orderId', required: false, type: 'string' },
  { field: 'orderNumber', required: false, type: 'string' },
  { field: 'employeeId', required: false, type: 'string' },
  { field: 'employeeName', required: false, type: 'string' },
  { field: 'planningDate', required: false, type: 'date' },
  { field: 'startTime', required: true, type: 'string', pattern: /^\d{2}:\d{2}$/ },
  { field: 'endTime', required: false, type: 'string', pattern: /^\d{2}:\d{2}$/ },
  { field: 'durationMinutes', required: true, type: 'number', validator: (v) => v > 0 },
  {
    field: 'priority',
    required: false,
    type: 'string',
    validator: (v) => ['URGENT', 'HIGH', 'MEDIUM', 'LOW'].includes(v)
  },
  {
    field: 'status',
    required: false,
    type: 'string',
    validator: (v) => ['SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'].includes(v)
  },
  { field: 'cardCount', required: false, type: 'number', validator: (v) => v >= 0 },
  { field: 'notes', required: false, type: 'string' },
  { field: 'completed', required: false, type: 'boolean' },
  { field: 'progressPercentage', required: false, type: 'number', validator: (v) => v >= 0 && v <= 100 }
]

// ========== VALIDATEURS SPÉCIALISÉS ==========

/**
 * Valide une réponse d'API pour les orders
 */
export const validateOrderResponse = (data: any): ValidationResult => {
  // Vérifier si c'est un tableau ou un objet unique
  if (Array.isArray(data)) {
    const results = data.map((item, index) => {
      const result = validateObject(item, ORDER_SCHEMA)
      if (!result.isValid) {
        result.errors = result.errors.map(error => `Item ${index}: ${error}`)
      }
      return result
    })

    const allErrors = results.flatMap(r => r.errors)
    const allWarnings = results.flatMap(r => r.warnings)

    return {
      isValid: allErrors.length === 0,
      errors: allErrors,
      warnings: allWarnings,
      data
    }
  } else {
    return validateObject(data, ORDER_SCHEMA)
  }
}

/**
 * Valide une réponse d'API pour les employees
 */
export const validateEmployeeResponse = (data: any): ValidationResult => {
  if (Array.isArray(data)) {
    const results = data.map((item, index) => {
      const result = validateObject(item, EMPLOYEE_SCHEMA)
      if (!result.isValid) {
        result.errors = result.errors.map(error => `Employee ${index}: ${error}`)
      }
      return result
    })

    const allErrors = results.flatMap(r => r.errors)
    const allWarnings = results.flatMap(r => r.warnings)

    return {
      isValid: allErrors.length === 0,
      errors: allErrors,
      warnings: allWarnings,
      data
    }
  } else {
    return validateObject(data, EMPLOYEE_SCHEMA)
  }
}

/**
 * Valide une réponse d'API pour les plannings
 */
export const validatePlanningResponse = (data: any): ValidationResult => {
  if (Array.isArray(data)) {
    const results = data.map((item, index) => {
      const result = validateObject(item, PLANNING_SCHEMA)
      if (!result.isValid) {
        result.errors = result.errors.map(error => `Planning ${index}: ${error}`)
      }
      return result
    })

    const allErrors = results.flatMap(r => r.errors)
    const allWarnings = results.flatMap(r => r.warnings)

    return {
      isValid: allErrors.length === 0,
      errors: allErrors,
      warnings: allWarnings,
      data
    }
  } else {
    return validateObject(data, PLANNING_SCHEMA)
  }
}

// ========== VALIDATEURS DE RÉPONSE HTTP ==========

/**
 * Valide une réponse HTTP basique
 */
export const validateHttpResponse = (response: Response): ValidationResult => {
  const errors: string[] = []
  const warnings: string[] = []

  // Vérifier le status code
  if (!response.ok) {
    errors.push(`HTTP error: ${response.status} ${response.statusText}`)
  }

  // Vérifier les headers
  const contentType = response.headers.get('content-type')
  if (contentType && !contentType.includes('application/json')) {
    warnings.push(`Unexpected content type: ${contentType}`)
  }

  return {
    isValid: errors.length === 0,
    errors,
    warnings
  }
}

/**
 * Valide qu'une réponse contient des données JSON valides
 */
export const validateJsonResponse = async (response: Response): Promise<ValidationResult> => {
  const httpValidation = validateHttpResponse(response)

  if (!httpValidation.isValid) {
    return httpValidation
  }

  try {
    const data = await response.json()

    return {
      isValid: true,
      errors: [],
      warnings: httpValidation.warnings,
      data
    }
  } catch (error) {
    return {
      isValid: false,
      errors: [`Invalid JSON response: ${error.message}`],
      warnings: httpValidation.warnings
    }
  }
}

// ========== VALIDATEURS DE CONFIGURATION ==========

/**
 * Valide une configuration de génération de planning
 */
export const validatePlanningConfig = (config: any): ValidationResult => {
  const schema: FieldValidation[] = [
    { field: 'startDate', required: false, type: 'date' },
    { field: 'endDate', required: false, type: 'date' },
    { field: 'timePerCard', required: false, type: 'number', validator: (v) => v > 0 && v <= 60 },
    { field: 'numberOfEmployees', required: false, type: 'number', validator: (v) => v > 0 },
    { field: 'cleanFirst', required: false, type: 'boolean' }
  ]

  const result = validateObject(config, schema)

  // Validation métier supplémentaire
  if (config.startDate && config.endDate) {
    const start = new Date(config.startDate)
    const end = new Date(config.endDate)

    if (start >= end) {
      result.errors.push('startDate must be before endDate')
      result.isValid = false
    }
  }

  return result
}

/**
 * Valide les données d'un nouvel employé
 */
export const validateEmployeeCreation = (employee: any): ValidationResult => {
  const schema: FieldValidation[] = [
    { field: 'firstName', required: true, type: 'string', minLength: 1, maxLength: 100 },
    { field: 'lastName', required: true, type: 'string', minLength: 1, maxLength: 100 },
    { field: 'email', required: false, type: 'email' },
    { field: 'workHoursPerDay', required: false, type: 'number', validator: (v) => v > 0 && v <= 24 },
    { field: 'active', required: false, type: 'boolean' }
  ]

  return validateObject(employee, schema)
}

// ========== EXPORT PAR DÉFAUT ==========

export default {
  // Validation générale
  validateObject,
  validateField,

  // Validateurs spécialisés
  validateOrderResponse,
  validateEmployeeResponse,
  validatePlanningResponse,

  // Validateurs HTTP
  validateHttpResponse,
  validateJsonResponse,

  // Validateurs de configuration
  validatePlanningConfig,
  validateEmployeeCreation,

  // Schémas
  ORDER_SCHEMA,
  EMPLOYEE_SCHEMA,
  PLANNING_SCHEMA,

  // Helpers
  exists,
  isOfType
}
