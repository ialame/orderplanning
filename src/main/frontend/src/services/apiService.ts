// ===============================================
// APISERVICE CORRIGÉ - src/services/apiService.ts
// ===============================================

import type {
  OrderResponse,
  EmployeeResponse,
  PlanningResponse,
  PlanningGenerationResponse,
  SystemDebugResponse
} from './types'

class ApiService {
  private readonly BASE_URL = 'http://localhost:8080'
  private readonly DEFAULT_TIMEOUT = 10000 // 10 secondes

  /**
   * ✅ MÉTHODE PRIVÉE - Fetch avec timeout et gestion d'erreur
   */
  private async fetchWithTimeout(
    url: string,
    options: RequestInit = {},
    timeout = this.DEFAULT_TIMEOUT
  ): Promise<Response> {

    // ✅ CONTROLLER D'ABORT POUR TIMEOUT
    const controller = new AbortController()
    const timeoutId = setTimeout(() => {
      console.log(`⏰ [API] Request timeout after ${timeout}ms for ${url}`)
      controller.abort()
    }, timeout)

    try {
      // ✅ CONFIGURATION PAR DÉFAUT
      const defaultOptions: RequestInit = {
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json',
          ...options.headers
        },
        signal: controller.signal,
        ...options
      }

      console.log(`📡 [API] Fetching: ${url}`)
      const response = await fetch(url, defaultOptions)

      // Nettoyer le timeout si la requête se termine
      clearTimeout(timeoutId)

      console.log(`📥 [API] Response: ${response.status} in ${url}`)
      return response

    } catch (error) {
      clearTimeout(timeoutId)

      if (error.name === 'AbortError') {
        throw new Error(`Request timed out after ${timeout/1000} seconds`)
      }

      throw error
    }
  }

  /**
   * ✅ MÉTHODE PRIVÉE - Parser JSON avec gestion d'erreur
   */
  private async parseJsonResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
      let errorMessage = `HTTP ${response.status}: ${response.statusText}`

      try {
        const errorData = await response.json()
        if (errorData.message) {
          errorMessage = errorData.message
        }
      } catch {
        // Si on ne peut pas parser l'erreur JSON, garder le message HTTP
      }

      throw new Error(errorMessage)
    }

    const contentType = response.headers.get('content-type')
    if (!contentType || !contentType.includes('application/json')) {
      throw new Error(`Invalid content type: ${contentType}`)
    }

    try {
      return await response.json()
    } catch (error) {
      throw new Error('Invalid JSON response')
    }
  }

  // ========== EMPLOYEES API ==========

  /**
   * ✅ GET EMPLOYEES - Version corrigée
   */
  async getEmployees(): Promise<EmployeeResponse[]> {
    try {
      console.log('👥 [API] Getting employees...')

      const response = await this.fetchWithTimeout('/api/employees/active')
      const data = await this.parseJsonResponse<EmployeeResponse[]>(response)

      if (!Array.isArray(data)) {
        console.error('❌ [API] Response is not an array:', data)
        return []
      }

      console.log(`✅ [API] Successfully loaded ${data.length} employees`)
      return data

    } catch (error) {
      console.error('❌ [API] getEmployees error:', error)

      // ✅ FALLBACK - Essayer d'autres endpoints
      return this.getEmployeesWithFallback()
    }
  }

  /**
   * ✅ FALLBACK EMPLOYEES - Essayer plusieurs endpoints
   */
  private async getEmployeesWithFallback(): Promise<EmployeeResponse[]> {
    const endpoints = [
      '/api/employees',
      '/api/employes/active',
      '/api/employes'
    ]

    for (const endpoint of endpoints) {
      try {
        console.log(`🔄 [API] Trying fallback endpoint: ${endpoint}`)

        const response = await this.fetchWithTimeout(endpoint, {}, 8000)

        if (response.ok) {
          const data = await this.parseJsonResponse<EmployeeResponse[]>(response)

          if (Array.isArray(data) && data.length > 0) {
            console.log(`✅ [API] Fallback success with ${endpoint}: ${data.length} employees`)
            return data
          }
        }

      } catch (error) {
        console.log(`❌ [API] Fallback failed for ${endpoint}:`, error.message)
        continue
      }
    }

    console.warn('⚠️ [API] All employee endpoints failed')
    return []
  }

  /**
   * ✅ CREATE EMPLOYEE - Version corrigée
   */
  async createEmployee(employee: Partial<EmployeeResponse>): Promise<any> {
    try {
      console.log('👤 [API] Creating employee:', employee)

      // ✅ VALIDATION
      if (!employee.firstName || !employee.lastName) {
        throw new Error('First name and last name are required')
      }

      // ✅ DONNÉES NETTOYÉES
      const cleanData = {
        firstName: employee.firstName.trim(),
        lastName: employee.lastName.trim(),
        email: employee.email?.trim() || '',
        workHoursPerDay: employee.workHoursPerDay || 8,
        active: employee.active !== false
      }

      const response = await this.fetchWithTimeout('/api/employees', {
        method: 'POST',
        body: JSON.stringify(cleanData)
      })

      const result = await this.parseJsonResponse(response)
      console.log('✅ [API] Employee created successfully:', result)

      return result

    } catch (error) {
      console.error('❌ [API] createEmployee error:', error)
      throw error
    }
  }

  // ========== ORDERS API ==========

  /**
   * ✅ GET ORDERS - Version corrigée
   */
  async getOrders(): Promise<OrderResponse[]> {
    try {
      console.log('📋 [API] Getting orders...')

      const response = await this.fetchWithTimeout('/api/orders/frontend/orders')
      const data = await this.parseJsonResponse<OrderResponse[]>(response)

      if (!Array.isArray(data)) {
        console.error('❌ [API] Orders response is not an array:', data)
        return []
      }

      console.log(`✅ [API] Successfully loaded ${data.length} orders`)
      return data

    } catch (error) {
      console.error('❌ [API] getOrders error:', error)
      return this.getOrdersWithFallback()
    }
  }

  /**
   * ✅ FALLBACK ORDERS
   */
  private async getOrdersWithFallback(): Promise<OrderResponse[]> {
    const endpoints = [
      '/api/commandes/frontend/commandes',
      '/api/frontend/commandes',
      '/api/orders'
    ]

    for (const endpoint of endpoints) {
      try {
        const response = await this.fetchWithTimeout(endpoint, {}, 8000)

        if (response.ok) {
          const data = await this.parseJsonResponse<OrderResponse[]>(response)

          if (Array.isArray(data)) {
            console.log(`✅ [API] Orders fallback success with ${endpoint}`)
            return data
          }
        }

      } catch (error) {
        continue
      }
    }

    return []
  }

  /**
   * ✅ GET ORDER CARDS
   */
  async getOrderCards(orderId: string): Promise<any[]> {
    try {
      console.log(`🃏 [API] Getting cards for order: ${orderId}`)

      const response = await this.fetchWithTimeout(`/api/orders/frontend/orders/${orderId}/cards`)
      const data = await this.parseJsonResponse<any[]>(response)

      console.log(`✅ [API] Loaded ${data?.length || 0} cards`)
      return Array.isArray(data) ? data : []

    } catch (error) {
      console.error('❌ [API] getOrderCards error:', error)
      return []
    }
  }

  // ========== PLANNING API ==========

  /**
   * ✅ GET PLANNINGS - Version corrigée
   */
  async getPlannings(date?: string): Promise<PlanningResponse[]> {
    try {
      console.log('📅 [API] Getting plannings for date:', date || 'all')

      const url = date
        ? `/api/planning/view-simple?date=${date}`
        : '/api/planning/view-simple'

      const response = await this.fetchWithTimeout(url)
      const data = await this.parseJsonResponse<PlanningResponse[]>(response)

      if (!Array.isArray(data)) {
        console.error('❌ [API] Plannings response is not an array:', data)
        return []
      }

      console.log(`✅ [API] Successfully loaded ${data.length} plannings`)
      return data

    } catch (error) {
      console.error('❌ [API] getPlannings error:', error)
      return []
    }
  }

  /**
   * ✅ GENERATE PLANNING - Version corrigée
   */
  async generatePlanning(config: any = {}): Promise<PlanningGenerationResponse> {
    try {
      console.log('🚀 [API] Generating planning with config:', config)

      const requestData = {
        startDate: config.startDate || '2025-06-01',
        timePerCard: config.timePerCard || 3,
        cleanFirst: config.cleanFirst || false
      }

      const response = await this.fetchWithTimeout('/api/planning/generate', {
        method: 'POST',
        body: JSON.stringify(requestData)
      }, 30000) // 30 secondes pour la génération

      const result = await this.parseJsonResponse<PlanningGenerationResponse>(response)
      console.log('✅ [API] Planning generated successfully:', result)

      return result

    } catch (error) {
      console.error('❌ [API] generatePlanning error:', error)
      throw error
    }
  }

  // ========== SYSTEM API ==========

  /**
   * ✅ TEST CONNECTION - Version corrigée
   */
  async testConnection(): Promise<{ success: boolean; message: string }> {
    try {
      console.log('🧪 [API] Testing connection...')

      const response = await this.fetchWithTimeout('/api/employees/debug', {}, 5000)

      const success = response.ok
      let message = success
        ? `Backend accessible (HTTP ${response.status})`
        : `Backend error (HTTP ${response.status})`

      if (success) {
        try {
          const data = await response.json()
          if (data.employee_count !== undefined) {
            message += ` - ${data.employee_count} employees in database`
          }
        } catch {
          // Ignore JSON parsing errors for connection test
        }
      }

      console.log(`🧪 [API] Connection test result: ${success ? 'SUCCESS' : 'FAILED'}`)
      return { success, message }

    } catch (error) {
      console.error('🧪 [API] Connection test error:', error)

      let message = 'Connection failed'
      if (error.message.includes('timeout')) {
        message = 'Connection timeout - backend may be slow'
      } else if (error.message.includes('fetch')) {
        message = 'Network error - check if backend is running'
      }

      return { success: false, message }
    }
  }

  /**
   * ✅ GET SYSTEM INFO
   */
  async getSystemInfo(): Promise<SystemDebugResponse> {
    try {
      const response = await this.fetchWithTimeout('/api/planning/debug-real')
      return await this.parseJsonResponse<SystemDebugResponse>(response)

    } catch (error) {
      console.error('❌ [API] getSystemInfo error:', error)

      return {
        availableOrders: 0,
        activeEmployees: 0,
        availableColumns: [],
        sampleOrders: [],
        status: 'ERROR',
        message: error.message,
        error: error.message
      }
    }
  }
}

// ========== SINGLETON EXPORT ==========
export const apiService = new ApiService()

// ========== CONVENIENCE FUNCTIONS ==========
export const generatePlanning = (config?: any) => apiService.generatePlanning(config)
export const getTodaysPlannings = () => apiService.getPlannings()
export const getSystemStatus = () => apiService.getSystemInfo()

export default apiService
