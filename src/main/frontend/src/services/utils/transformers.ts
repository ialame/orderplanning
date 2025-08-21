// ===============================================
// TRANSFORMATEURS DE DONNÉES API
// ===============================================
// Fichier: src/services/utils/transformers.ts

import type {
  OrderResponse,
  EmployeeResponse,
  PlanningResponse,
  Order,
  Employee,
  Planning
} from '../types'

// ========== HELPERS UTILITAIRES ==========

/**
 * Conversion sécurisée de chaîne vers nombre
 */
const safeParseInt = (value: any, defaultValue = 0): number => {
  if (typeof value === 'number') return value
  if (typeof value === 'string') {
    const parsed = parseInt(value, 10)
    return isNaN(parsed) ? defaultValue : parsed
  }
  return defaultValue
}

/**
 * Conversion sécurisée vers booléen
 */
const safeParseBoolean = (value: any, defaultValue = false): boolean => {
  if (typeof value === 'boolean') return value
  if (typeof value === 'number') return value === 1
  if (typeof value === 'string') {
    const lower = value.toLowerCase()
    return lower === 'true' || lower === '1' || lower === 'yes'
  }
  return defaultValue
}

/**
 * Nettoyage et validation d'email
 */
const cleanEmail = (email: any): string => {
  if (typeof email !== 'string') return ''
  const cleaned = email.trim().toLowerCase()
  return cleaned.includes('@') ? cleaned : ''
}

/**
 * Formatage des dates
 */
const formatDate = (date: any): string => {
  if (!date) return ''

  try {
    if (date instanceof Date) {
      return date.toISOString().split('T')[0]
    }

    if (typeof date === 'string') {
      // Si c'est déjà au format YYYY-MM-DD
      if (/^\d{4}-\d{2}-\d{2}$/.test(date)) {
        return date
      }

      // Essayer de parser
      const parsed = new Date(date)
      if (!isNaN(parsed.getTime())) {
        return parsed.toISOString().split('T')[0]
      }
    }

    return ''
  } catch (error) {
    console.warn('Date formatting error:', date, error)
    return ''
  }
}

/**
 * Formatage des heures (HH:MM)
 */
const formatTime = (time: any): string => {
  if (!time) return '09:00'

  try {
    if (typeof time === 'string') {
      // Si c'est déjà au format HH:MM
      if (/^\d{2}:\d{2}$/.test(time)) {
        return time
      }

      // Si c'est un datetime, extraire l'heure
      if (time.includes('T')) {
        return time.split('T')[1].substring(0, 5)
      }

      if (time.includes(' ')) {
        const timePart = time.split(' ')[1]
        return timePart.substring(0, 5)
      }
    }

    return '09:00'
  } catch (error) {
    console.warn('Time formatting error:', time, error)
    return '09:00'
  }
}

// ========== TRANSFORMATEURS ORDERS ==========

/**
 * Transforme les données brutes d'ordre vers le format frontend
 */
export const transformOrderFromApi = (apiData: any): Order => {
  return {
    id: apiData.id || '',
    orderNumber: apiData.orderNumber || apiData.num_commande || apiData.numeroCommande || 'Unknown',
    cardCount: safeParseInt(apiData.cardCount || apiData.nombre_cartes || apiData.nombreCartes),
    cardsWithName: safeParseInt(apiData.cardsWithName || apiData.cartes_avec_nom),
    percentageWithName: safeParseInt(apiData.percentageWithName || apiData.pourcentage_avec_nom),
    totalPrice: safeParseInt(apiData.totalPrice || apiData.prix_total || apiData.montant),
    priority: transformPriority(apiData.priority || apiData.priorite || apiData.delai_code),
    status: transformOrderStatus(apiData.status || apiData.statut),
    statusCode: safeParseInt(apiData.statusCode || apiData.status_code),
    creationDate: formatDate(apiData.creationDate || apiData.date_creation || apiData.date),
    deadline: formatDate(apiData.deadline || apiData.echeance || apiData.date_echeance),
    estimatedTimeMinutes: safeParseInt(apiData.estimatedTimeMinutes || apiData.duree_estimee_minutes || apiData.dureeEstimeeMinutes),
    orderQuality: transformQuality(apiData.orderQuality || apiData.qualite)
  }
}

/**
 * Transforme un tableau d'ordres depuis l'API
 */
export const transformOrdersFromApi = (apiData: any[]): Order[] => {
  if (!Array.isArray(apiData)) {
    console.warn('Orders data is not an array:', apiData)
    return []
  }

  return apiData.map(transformOrderFromApi).filter(order => order.id)
}

// ========== TRANSFORMATEURS EMPLOYEES ==========

/**
 * Transforme les données brutes d'employé vers le format frontend
 */
export const transformEmployeeFromApi = (apiData: any): Employee => {
  const firstName = apiData.firstName || apiData.prenom || apiData.first_name || ''
  const lastName = apiData.lastName || apiData.nom || apiData.last_name || ''

  return {
    id: apiData.id || '',
    firstName,
    lastName,
    fullName: apiData.fullName || apiData.nomComplet || `${firstName} ${lastName}`.trim(),
    email: cleanEmail(apiData.email),
    workHoursPerDay: safeParseInt(apiData.workHoursPerDay || apiData.heuresTravailParJour || apiData.work_hours_per_day, 8),
    active: safeParseBoolean(apiData.active || apiData.actif, true),
    creationDate: formatDate(apiData.creationDate || apiData.dateCreation || apiData.date_creation),
    available: safeParseBoolean(apiData.available || apiData.disponible, true),
    currentLoad: safeParseInt(apiData.currentLoad || apiData.charge_actuelle)
  }
}

/**
 * Transforme un tableau d'employés depuis l'API
 */
export const transformEmployeesFromApi = (apiData: any[]): Employee[] => {
  if (!Array.isArray(apiData)) {
    console.warn('Employees data is not an array:', apiData)
    return []
  }

  return apiData.map(transformEmployeeFromApi).filter(employee => employee.id)
}

// ========== TRANSFORMATEURS PLANNING ==========

/**
 * Transforme les données brutes de planning vers le format frontend
 */
export const transformPlanningFromApi = (apiData: any): Planning => {
  return {
    id: apiData.id || '',
    orderId: apiData.orderId || apiData.order_id || '',
    orderNumber: apiData.orderNumber || apiData.numeroCommande || apiData.num_commande || 'Unknown',
    employeeId: apiData.employeeId || apiData.employee_id || '',
    employeeName: apiData.employeeName || apiData.nomEmploye || apiData.employee_name || 'Unknown Employee',
    planningDate: formatDate(apiData.planningDate || apiData.datePlanifiee || apiData.planning_date),
    startTime: formatTime(apiData.startTime || apiData.heureDebut || apiData.start_time),
    endTime: formatTime(apiData.endTime || apiData.heureFin || apiData.end_time),
    durationMinutes: safeParseInt(apiData.durationMinutes || apiData.dureeMinutes || apiData.estimated_duration_minutes),
    priority: transformPriority(apiData.priority || apiData.priorite),
    status: transformPlanningStatus(apiData.status || apiData.statut),
    cardCount: safeParseInt(apiData.cardCount || apiData.nombreCartes || apiData.card_count, 1),
    notes: apiData.notes || apiData.commentaires || '',
    completed: safeParseBoolean(apiData.completed || apiData.terminee),
    progressPercentage: safeParseInt(apiData.progressPercentage || apiData.pourcentageProgres || apiData.progress_percentage)
  }
}

/**
 * Transforme un tableau de plannings depuis l'API
 */
export const transformPlanningsFromApi = (apiData: any[]): Planning[] => {
  if (!Array.isArray(apiData)) {
    console.warn('Planning data is not an array:', apiData)
    return []
  }

  return apiData.map(transformPlanningFromApi).filter(planning => planning.id)
}

// ========== TRANSFORMATEURS DE PRIORITÉ ==========

/**
 * Normalise les priorités vers un format standard
 */
export const transformPriority = (priority: any): 'URGENT' | 'HIGH' | 'MEDIUM' | 'LOW' => {
  if (!priority) return 'MEDIUM'

  const p = String(priority).toUpperCase()

  // Mapping des différents formats
  const priorityMap: Record<string, 'URGENT' | 'HIGH' | 'MEDIUM' | 'LOW'> = {
    // Français
    'URGENT': 'URGENT',
    'URGENTE': 'URGENT',
    'HAUTE': 'HIGH',
    'ÉLEVÉE': 'HIGH',
    'MOYENNE': 'MEDIUM',
    'NORMALE': 'MEDIUM',
    'BASSE': 'LOW',
    'FAIBLE': 'LOW',

    // Anglais
    'HIGH': 'HIGH',
    'MEDIUM': 'MEDIUM',
    'NORMAL': 'MEDIUM',
    'LOW': 'LOW',

    // Codes numériques
    '1': 'URGENT',
    '2': 'HIGH',
    '3': 'MEDIUM',
    '4': 'LOW',

    // Codes délai
    'SAME_DAY': 'URGENT',
    'NEXT_DAY': 'HIGH',
    'WEEK': 'MEDIUM',
    'MONTH': 'LOW'
  }

  return priorityMap[p] || 'MEDIUM'
}

// ========== TRANSFORMATEURS DE STATUT ==========

/**
 * Normalise les statuts d'ordre
 */
export const transformOrderStatus = (status: any): 'PENDING' | 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' => {
  if (!status) return 'PENDING'

  const s = String(status).toUpperCase()

  const statusMap: Record<string, 'PENDING' | 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'> = {
    // Français
    'EN_ATTENTE': 'PENDING',
    'ATTENTE': 'PENDING',
    'PLANIFIÉ': 'SCHEDULED',
    'PLANIFIEE': 'SCHEDULED',
    'EN_COURS': 'IN_PROGRESS',
    'COURS': 'IN_PROGRESS',
    'TERMINÉ': 'COMPLETED',
    'TERMINEE': 'COMPLETED',
    'ANNULÉ': 'CANCELLED',
    'ANNULEE': 'CANCELLED',

    // Anglais
    'PENDING': 'PENDING',
    'SCHEDULED': 'SCHEDULED',
    'IN_PROGRESS': 'IN_PROGRESS',
    'COMPLETED': 'COMPLETED',
    'CANCELLED': 'CANCELLED',

    // Codes numériques
    '1': 'PENDING',
    '2': 'SCHEDULED',
    '3': 'IN_PROGRESS',
    '4': 'COMPLETED',
    '5': 'CANCELLED'
  }

  return statusMap[s] || 'PENDING'
}

/**
 * Normalise les statuts de planning
 */
export const transformPlanningStatus = (status: any): 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' => {
  if (!status) return 'SCHEDULED'

  const s = String(status).toUpperCase()

  const statusMap: Record<string, 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'> = {
    // Français
    'PLANIFIÉ': 'SCHEDULED',
    'PLANIFIEE': 'SCHEDULED',
    'EN_COURS': 'IN_PROGRESS',
    'COURS': 'IN_PROGRESS',
    'TERMINÉ': 'COMPLETED',
    'TERMINEE': 'COMPLETED',
    'ANNULÉ': 'CANCELLED',
    'ANNULEE': 'CANCELLED',

    // Anglais
    'SCHEDULED': 'SCHEDULED',
    'IN_PROGRESS': 'IN_PROGRESS',
    'COMPLETED': 'COMPLETED',
    'CANCELLED': 'CANCELLED'
  }

  return statusMap[s] || 'SCHEDULED'
}

/**
 * Normalise la qualité
 */
export const transformQuality = (quality: any): 'EXCELLENT' | 'GOOD' | 'AVERAGE' | 'POOR' => {
  if (!quality) return 'AVERAGE'

  const q = String(quality).toUpperCase()

  const qualityMap: Record<string, 'EXCELLENT' | 'GOOD' | 'AVERAGE' | 'POOR'> = {
    'EXCELLENT': 'EXCELLENT',
    'EXCELLENTE': 'EXCELLENT',
    'GOOD': 'GOOD',
    'BIEN': 'GOOD',
    'BONNE': 'GOOD',
    'AVERAGE': 'AVERAGE',
    'MOYENNE': 'AVERAGE',
    'POOR': 'POOR',
    'MAUVAISE': 'POOR',
    'FAIBLE': 'POOR'
  }

  return qualityMap[q] || 'AVERAGE'
}

// ========== TRANSFORMATEURS POUR L'ENVOI ==========

/**
 * Transforme un employé du format frontend vers l'API
 */
export const transformEmployeeForApi = (employee: Partial<Employee>): Record<string, any> => {
  return {
    firstName: employee.firstName || '',
    lastName: employee.lastName || '',
    email: employee.email || '',
    workHoursPerDay: employee.workHoursPerDay || 8,
    active: employee.active !== false // Default true
  }
}

/**
 * Transforme une configuration de planning pour l'API
 */
export const transformPlanningConfigForApi = (config: any): Record<string, any> => {
  return {
    startDate: formatDate(config.startDate) || formatDate(new Date()),
    timePerCard: safeParseInt(config.timePerCard, 3),
    cleanFirst: safeParseBoolean(config.cleanFirst, false),
    numberOfEmployees: safeParseInt(config.numberOfEmployees),
    endDate: formatDate(config.endDate)
  }
}

// ========== EXPORT PAR DÉFAUT ==========

export default {
  // Orders
  transformOrderFromApi,
  transformOrdersFromApi,

  // Employees
  transformEmployeeFromApi,
  transformEmployeesFromApi,
  transformEmployeeForApi,

  // Planning
  transformPlanningFromApi,
  transformPlanningsFromApi,
  transformPlanningConfigForApi,

  // Status & Priority
  transformPriority,
  transformOrderStatus,
  transformPlanningStatus,
  transformQuality,

  // Utilities
  safeParseInt,
  safeParseBoolean,
  cleanEmail,
  formatDate,
  formatTime
}
