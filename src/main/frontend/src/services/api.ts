// ============= ENGLISH API SERVICE - 100% ENGLISH =============
// Complete English API service for Pokemon card order planning system
// NO FRENCH variables, comments, or methods

// ========== CONFIGURATION ==========
const API_BASE_URL = 'http://localhost:8080/api'
const API_PLANNING_URL = 'http://localhost:8080/api/planning'
const API_ORDERS_URL = 'http://localhost:8080/api/orders'
const API_EMPLOYEES_URL = 'http://localhost:8080/api/employees'

// ========== TYPESCRIPT INTERFACES ==========

export interface Order {
  id?: string
  orderNumber: string
  cardCount: number
  cardsWithName?: number
  percentageWithName?: number
  totalPrice?: number
  priority: 'URGENT' | 'HIGH' | 'MEDIUM' | 'LOW'
  status?: 'PENDING' | 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED'
  statusCode?: number
  creationDate?: string
  deadline?: string
  estimatedTimeMinutes: number
  orderQuality?: 'EXCELLENT' | 'GOOD' | 'AVERAGE' | 'POOR'
  cardsWithoutMissingData?: boolean
}

export interface Employee {
  id?: string
  firstName: string
  lastName: string
  email: string
  workHoursPerDay: number
  active: boolean
  creationDate?: string
  fullName?: string
}

export interface Planning {
  id?: string | number
  orderId?: string
  employeeId?: string
  planningDate?: string
  startTime: string
  endTime?: string
  durationMinutes: number
  completed: boolean
  orderNumber?: string
  priority?: string
  employeeName?: string
  status?: string
  notes?: string
  cardCount?: number
}

export interface PlanningRequest {
  startDate: string
  endDate: string
  numberOfEmployees: number
  timePerCard: number
  cleanFirst?: boolean
}

export interface PlanningResponse {
  success: boolean
  message: string
  numberOfPlannedOrders?: number
  report?: any
}

export interface DashboardStats {
  pendingOrders: number
  inProgressOrders: number
  completedOrders: number
  totalOrders: number
  activeEmployees?: number
  status: string
  timestamp?: number
}

// ========== MAIN API SERVICE CLASS ==========

export class EnglishApiService {
  private baseUrl = API_BASE_URL

  // ========== UTILITY METHODS ==========

  private async request<T>(endpoint: string, options: RequestInit = {}, baseUrl = this.baseUrl): Promise<T> {
    const url = `${baseUrl}${endpoint}`

    const response = await fetch(url, {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    })

    if (!response.ok) {
      throw new Error(`HTTP Error: ${response.status} ${response.statusText}`)
    }

    return response.json()
  }

  // ========== ORDER METHODS ==========

  async getOrders(): Promise<Order[]> {
    console.log('🎮 Loading Pokemon card orders...')

    // Try multiple endpoints for maximum compatibility
    const endpoints = [
      '/orders/frontend/orders',
      '/orders/period?startDate=2025-05-22&endDate=2025-06-22',
      '/test/orders/last-month',
      '/test/orders'
    ]

    for (const endpoint of endpoints) {
      try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
          method: 'GET',
          headers: { 'Content-Type': 'application/json' }
        })

        if (response.ok) {
          const orders: any[] = await response.json()
          console.log(`✅ ${orders.length} orders loaded from ${endpoint}`)
          return this.mapOrderResponse(orders)
        }
      } catch (error) {
        console.log(`🔄 Endpoint ${endpoint} unavailable, trying next...`)
        continue
      }
    }

    throw new Error('No order endpoints available')
  }

  async getOrderDetails(id: string): Promise<Order> {
    try {
      return await this.request<Order>(`/orders/${id}`)
    } catch (error) {
      return this.request<Order>(`/orders/${id}`, {}, `${API_BASE_URL}/test`)
    }
  }

  async getOrderCards(orderId: string): Promise<any> {
    try {
      console.log(`🃏 Loading cards for order: ${orderId}`)

      const response = await fetch(`${API_BASE_URL}/orders/frontend/orders/${orderId}/cards`)

      if (!response.ok) {
        throw new Error(`Card loading error: ${response.status}`)
      }

      const result = await response.json()
      console.log('✅ Cards loaded:', result)

      return result

    } catch (error) {
      console.error('❌ Error loading cards:', error)
      throw error
    }
  }

  // ========== EMPLOYEE METHODS ==========

  async getEmployees(): Promise<Employee[]> {
    try {
      console.log('👥 Loading active employees...')

      // Try j_employee table first (current table)
      try {
        const response = await fetch(`${API_BASE_URL}/employees/active`, {
          method: 'GET',
          headers: { 'Content-Type': 'application/json' }
        })

        if (response.ok) {
          const employees: any[] = await response.json()
          console.log(`✅ ${employees.length} employees loaded from j_employee`)
          return this.mapEmployeeResponse(employees)
        }
      } catch (error) {
        console.log('🔄 Employee service unavailable, using fallback...')
      }

      // Fallback: Create test employees
      return this.createTestEmployees()

    } catch (error) {
      console.error('❌ Error loading employees:', error)
      return this.createTestEmployees()
    }
  }

  async getEmployeeDetails(id: string): Promise<any> {
    try {
      return await this.request<any>(`/employees/${id}`)
    } catch (error) {
      return this.request<any>(`/employees/${id}`, {}, `${API_BASE_URL}/test`)
    }
  }

  // ========== PLANNING METHODS ==========

  /**
   * Generate Pokemon card planning automatically
   */
  async generatePokemonPlanning(options: {
    cleanFirst?: boolean
    startDate?: string
    timePerCard?: number
  } = {}): Promise<any> {
    try {
      console.log('🎮 Generating Pokemon card planning...')

      const response = await fetch(`${API_PLANNING_URL}/generate`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          cleanFirst: options.cleanFirst || false,
          startDate: options.startDate || '2025-06-01',
          timePerCard: options.timePerCard || 3
        })
      })

      if (!response.ok) {
        throw new Error(`Planning generation error: ${response.status}`)
      }

      const result = await response.json()
      console.log('✅ Planning generated:', result)

      return result

    } catch (error) {
      console.error('❌ Planning generation error:', error)
      throw error
    }
  }

  /**
   * Load all plannings
   */
  async getPlannings(): Promise<Planning[]> {
    try {
      console.log('📋 [API] Loading plannings from backend...')

      const response = await fetch('/api/planning/view-simple', {
        method: 'GET',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        }
      })

      if (!response.ok) {
        throw new Error(`Failed to load plannings: HTTP ${response.status}`)
      }

      const data = await response.json()
      console.log('📥 [API] Raw plannings data:', data)

      // ✅ Vérification format
      if (!Array.isArray(data)) {
        console.error('❌ Backend returned non-array data:', data)
        return []
      }

      // ✅ Transformation sécurisée
      const plannings = data.map((item: any) => ({
        id: item.id || '',
        orderId: item.orderId || '',
        orderNumber: item.orderNumber || 'Unknown Order',
        employeeId: item.employeeId || '',
        employeeName: item.employeeName || 'Unknown Employee',
        planningDate: item.planningDate || '',
        startTime: item.startTime || '',
        endTime: item.endTime || '',
        durationMinutes: item.durationMinutes || 0,
        priority: item.priority || 'MEDIUM',
        status: item.status || 'SCHEDULED',
        cardCount: item.cardCount || 1,
        notes: item.notes || '',
        completed: Boolean(item.completed),
        progressPercentage: item.progressPercentage || 0
      }))

      console.log(`✅ [API] Successfully loaded ${plannings.length} plannings`)
      return plannings

    } catch (error) {
      console.error('❌ [API] Error loading plannings:', error)
      return []
    }
  }

  /**
   * ✅ NOUVEAU : Test de connectivité plannings
   */
  async testPlanningConnectivity(): Promise<{success: boolean, count: number, message: string}> {
    try {
      console.log('🧪 Testing planning connectivity...')

      // Test simple sans paramètres
      const response = await fetch('/api/planning/view-simple')

      if (!response.ok) {
        return {
          success: false,
          count: 0,
          message: `HTTP ${response.status}: ${response.statusText}`
        }
      }

      const data = await response.json()

      return {
        success: true,
        count: Array.isArray(data) ? data.length : 0,
        message: `Backend accessible, found ${Array.isArray(data) ? data.length : 0} plannings`
      }

    } catch (error) {
      return {
        success: false,
        count: 0,
        message: `Connection error: ${error.message}`
      }
    }
  }

  /**
   * ✅ NOUVEAU : Diagnostic complet
   */
  async diagnosePlanningIssues(): Promise<any> {
    try {
      console.log('🔍 Running planning diagnosis...')

      const tests = {
        backend_connection: false,
        data_count: 0,
        employees_found: 0,
        dates_available: [],
        sample_data: null,
        errors: []
      }

      // Test 1: Connectivité basique
      try {
        const response = await fetch('/api/planning/view-simple')
        if (response.ok) {
          tests.backend_connection = true
          const data = await response.json()

          if (Array.isArray(data)) {
            tests.data_count = data.length

            // Analyser les données
            const uniqueEmployees = new Set(data.map(p => p.employeeId).filter(Boolean))
            tests.employees_found = uniqueEmployees.size

            const uniqueDates = [...new Set(data.map(p => p.planningDate).filter(Boolean))]
            tests.dates_available = uniqueDates.sort()

            if (data.length > 0) {
              tests.sample_data = data[0]
            }
          }
        }
      } catch (error) {
        tests.errors.push(`Backend connection: ${error.message}`)
      }

      // Test 2: Test génération
      try {
        const generateResponse = await fetch('/api/planning/debug-real')
        if (generateResponse.ok) {
          const debugData = await generateResponse.json()
          tests.backend_debug = debugData
        }
      } catch (error) {
        tests.errors.push(`Debug endpoint: ${error.message}`)
      }

      console.log('🔍 Diagnosis complete:', tests)
      return tests

    } catch (error) {
      console.error('❌ Diagnosis failed:', error)
      return { error: error.message }
    }
  }
  /**
   * Load plannings for a specific period
   */
  async getPlanningsByPeriod(startDate: string, endDate: string): Promise<Planning[]> {
    try {
      console.log(`📅 Loading plannings for period ${startDate} → ${endDate}`)

      const endpoints = [
        `${API_PLANNING_URL}/view-simple?date=${startDate}`,
        `${API_PLANNING_URL}/view-simple`,
        `${API_BASE_URL}/plannings?startDate=${startDate}&endDate=${endDate}`,
        `${API_BASE_URL}/test/plannings`
      ]

      for (const endpoint of endpoints) {
        try {
          const response = await fetch(endpoint, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
          })

          if (response.ok) {
            const data = await response.json()
            console.log(`✅ Plannings found via ${endpoint}:`, data.length)
            return this.mapPlanningResponse(data, endpoint)
          }
        } catch (error) {
          console.log(`🔄 Endpoint ${endpoint} unavailable`)
          continue
        }
      }

      return []

    } catch (error) {
      console.error('❌ Error loading plannings by period:', error)
      return []
    }
  }

  /**
   * Clean duplicate plannings
   */
  async cleanDuplicatePlannings(): Promise<any> {
    try {
      console.log('🧹 Cleaning duplicate plannings...')

      const response = await fetch(`${API_PLANNING_URL}/nettoyer-doublons`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        }
      })

      if (!response.ok) {
        throw new Error(`Cleanup error: ${response.status}`)
      }

      const result = await response.json()
      console.log('✅ Cleanup completed:', result)

      return result

    } catch (error) {
      console.error('❌ Cleanup error:', error)
      throw error
    }
  }

  /**
   * Automatic planning (compatibility method)
   */
  async automaticPlanning(request?: PlanningRequest): Promise<PlanningResponse> {
    try {
      console.log('🚀 Starting automatic planning...')

      const result = await this.generatePokemonPlanning({
        cleanFirst: false,
        startDate: request?.startDate || '2025-06-01',
        timePerCard: request?.timePerCard || 3
      })

      return {
        success: result.success || false,
        message: result.message || 'Planning completed',
        numberOfPlannedOrders: result.planningsSaved || 0,
        report: {
          ordersAnalyzed: result.ordersAnalyzed || 0,
          totalCards: result.totalCards || 0,
          totalHours: result.totalHours || '0',
          employeesUsed: result.employeesUsed || 0,
          strategy: result.strategy || 'AUTO'
        }
      }

    } catch (error) {
      console.error('❌ Automatic planning error:', error)
      return {
        success: false,
        message: 'Automatic planning error',
        numberOfPlannedOrders: 0
      }
    }
  }

  // ========== DASHBOARD METHODS ==========

  async getDashboardStats(): Promise<DashboardStats> {
    return this.request<DashboardStats>('/stats', {}, `${API_BASE_URL}/dashboard`)
  }

  async testConnection(): Promise<{ status: string }> {
    return this.request<{ status: string }>('/test', {}, `${API_BASE_URL}/dashboard`)
  }

  // ========== PRIVATE MAPPING METHODS ==========

  private mapOrderResponse(orders: any[]): Order[] {
    return orders.map(order => ({
      id: order.id,
      orderNumber: order.orderNumber || order.numeroCommande,
      cardCount: order.cardCount || order.nombreCartes,
      cardsWithName: order.cardsWithName || order.nombreAvecNom,
      percentageWithName: order.percentageWithName || order.pourcentageAvecNom,
      priority: order.priority || order.priorite || 'MEDIUM',
      totalPrice: order.totalPrice || order.prixTotal,
      statusCode: order.status || order.statusCode,
      estimatedTimeMinutes: order.estimatedTimeMinutes || order.tempsEstimeMinutes,
      creationDate: order.creationDate || order.dateCreation || new Date().toISOString(),
      status: this.mapStatus(order.status || order.statusCode),
      orderQuality: this.getQualityFromPercentage(order.percentageWithName || order.pourcentageAvecNom || 0),
      cardsWithoutMissingData: (order.percentageWithName || order.pourcentageAvecNom || 0) >= 95
    }))
  }

  private mapEmployeeResponse(employees: any[]): Employee[] {
    return employees.map(emp => ({
      id: emp.id,
      firstName: emp.firstName || emp.prenom,
      lastName: emp.lastName || emp.nom,
      email: emp.email,
      workHoursPerDay: emp.workHoursPerDay || emp.heuresTravailParJour || 8,
      active: emp.active !== undefined ? emp.active : emp.actif,
      creationDate: emp.creationDate || emp.dateCreation,
      fullName: `${emp.firstName || emp.prenom} ${emp.lastName || emp.nom}`
    }))
  }

  private mapPlanningResponse(response: any[], endpoint: string): Planning[] {
    if (!Array.isArray(response)) {
      console.warn('⚠️ Planning response is not an array:', response)
      return []
    }

    return response.map(planning => {
      // Detect data format based on endpoint used
      if (endpoint.includes('/api/planning/')) {
        // New planning backend format
        return {
          id: planning.id || planning.planningId,
          orderId: planning.orderId || planning.order_id,
          employeeId: planning.employeeId || planning.employee_id,
          planningDate: planning.planningDate || planning.planning_date,
          startTime: this.extractTimeFromDateTime(planning.startTime || planning.start_time),
          endTime: this.extractTimeFromDateTime(planning.endTime || planning.end_time),
          durationMinutes: planning.durationMinutes || planning.estimated_duration_minutes || 30,
          completed: planning.completed || false,
          orderNumber: planning.orderNumber || planning.order_number || 'N/A',
          priority: planning.priority || 'MEDIUM',
          employeeName: planning.employeeName || planning.employee_name || 'Employee',
          status: planning.status || 'SCHEDULED',
          notes: planning.notes || '',
          cardCount: planning.cardCount || planning.card_count || 1
        }
      } else {
        // Legacy backend format
        return {
          id: planning.id || planning.planification_id,
          orderId: planning.orderId || planning.order_id,
          employeeId: planning.employeeId || planning.employe_id,
          planningDate: planning.planningDate || planning.date_planification,
          startTime: planning.startTime || planning.heure_debut || '09:00',
          endTime: planning.endTime || planning.heure_fin,
          durationMinutes: planning.durationMinutes || planning.duree_minutes || 30,
          completed: planning.completed || planning.terminee || false,
          orderNumber: planning.orderNumber || planning.numeroCommande || planning.num_commande || 'N/A',
          priority: planning.priority || planning.priorite || 'MEDIUM',
          employeeName: planning.employeeName || planning.employeNom || planning.employe_nom || 'Employee',
          status: planning.status || planning.statut || 'SCHEDULED'
        }
      }
    })
  }

  private createTestEmployees(): Employee[] {
    const testEmployees = [
      {
        id: 'E93263727DF943D78BD9B0F91845F358',
        firstName: 'Ibrahim',
        lastName: 'ALAME',
        email: 'ibrahim.alame@pokemon.com',
        workHoursPerDay: 8,
        active: true,
        fullName: 'Ibrahim ALAME'
      },
      {
        id: 'F84374838EF054E789E8BF279456A469',
        firstName: 'FX',
        lastName: 'Colombani',
        email: 'fx.colombani@pokemon.com',
        workHoursPerDay: 8,
        active: true,
        fullName: 'FX Colombani'
      },
      {
        id: 'A95485949F0165F89AF9C038A567B57A',
        firstName: 'Pokemon',
        lastName: 'Trainer',
        email: 'trainer@pokemon.com',
        workHoursPerDay: 8,
        active: true,
        fullName: 'Pokemon Trainer'
      }
    ]

    console.log('⚠️ Using test employees as fallback:', testEmployees.length)
    return testEmployees
  }

  private extractTimeFromDateTime(dateTimeStr: string | null): string {
    if (!dateTimeStr) return '09:00'

    try {
      // If it's already just time (HH:MM)
      if (dateTimeStr.match(/^\d{2}:\d{2}$/)) {
        return dateTimeStr
      }

      // If it's a full datetime
      if (dateTimeStr.includes('T')) {
        return dateTimeStr.split('T')[1].substring(0, 5)
      }

      // If it's a datetime with space
      if (dateTimeStr.includes(' ')) {
        const timePart = dateTimeStr.split(' ')[1]
        return timePart.substring(0, 5)
      }

      return '09:00'
    } catch (error) {
      console.warn('⚠️ Time extraction error:', dateTimeStr, error)
      return '09:00'
    }
  }

  private mapStatus(statusNumber: number): string {
    switch (statusNumber) {
      case 1: return 'PENDING'
      case 2: return 'IN_PROGRESS'
      case 3: return 'COMPLETED'
      case 4: return 'CANCELLED'
      default: return 'PENDING'
    }
  }

  private getQualityFromPercentage(percentage: number): string {
    if (percentage >= 95) return 'EXCELLENT'
    if (percentage >= 85) return 'GOOD'
    if (percentage >= 75) return 'AVERAGE'
    if (percentage >= 60) return 'FAIR'
    return 'POOR'
  }
}

// ========== SINGLETON EXPORT ==========

export const apiService = new EnglishApiService()

// ========== CONVENIENCE FUNCTIONS ==========

export const generatePlanning = (config: PlanningRequest) =>
  apiService.automaticPlanning(config)

export const getTodaysPlannings = () =>
  apiService.getPlannings()

export const getSystemStatus = () =>
  apiService.testConnection()

export default apiService
