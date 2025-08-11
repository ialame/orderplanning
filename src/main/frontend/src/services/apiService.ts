// ============= ENGLISH API SERVICE =============
// Service pour interagir avec l'API backend anglaise
// Traduction complète du service français existant

// ========== INTERFACES ==========

export interface OrderRequest {
  startDate: string
  endDate: string
  numberOfEmployees: number
  timePerCard: number
}

export interface OrderResponse {
  id: string
  orderNumber: string
  cardCount: number
  cardsWithName?: number
  percentageWithName?: number
  totalPrice?: number
  priority: 'URGENT' | 'HIGH' | 'MEDIUM' | 'LOW'
  status: 'PENDING' | 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED'
  statusCode: number
  creationDate?: string
  deadline?: string
  estimatedTimeMinutes: number
  orderQuality?: 'EXCELLENT' | 'GOOD' | 'AVERAGE' | 'POOR'
}

export interface CardResponse {
  id: string
  certificationId: string
  cardId: string
  name: string
  labelName: string
  barcode: string
  type: string
  annotation: string
  language: string
  withName: boolean
}

export interface EmployeeResponse {
  id: string
  firstName: string
  lastName: string
  email: string
  workHoursPerDay: number
  active: boolean
  fullName: string
}

export interface PlanningResponse {
  id: string
  orderId: string
  orderNumber: string
  employeeId: string
  employeeName?: string
  planningDate: string
  startTime: string
  endTime?: string
  durationMinutes: number
  priority: 'URGENT' | 'HIGH' | 'MEDIUM' | 'LOW'
  status: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
  cardCount?: number
  notes?: string
  completed: boolean
}

export interface PlanningGenerationResponse {
  success: boolean
  message: string
  algorithm: string
  executionTimeMs: number
  ordersAnalyzed: number
  planningsCreated: number
  planningsSaved: number
  employeesUsed: number
  plannings: PlanningResponse[]
  period: {
    start: string
    end: string
  }
  timestamp: number
  error?: string
}

export interface SystemDebugResponse {
  availableOrders: number
  activeEmployees: number
  availableColumns: string[]
  sampleOrders: Array<{
    id: string
    orderNumber: string
    delaiCode: string
    priority: string
    cardCount: number
  }>
  status: 'SUCCESS' | 'ERROR'
  message: string
  error?: string
}

export interface SystemStatsResponse {
  totalPlannings: number
  statusBreakdown: Record<string, number>
  lastUpdated: number
  error?: string
}

export interface ApiError extends Error {
  status?: number
  code?: string
}

// ========== API SERVICE CLASS ==========

export class ApiService {
  private readonly baseUrl = '/api/planning'
  private readonly headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }

  // ========== PLANNING METHODS ==========

  /**
   * Generate automatic planning using English API
   */
  async generatePlanning(request: OrderRequest): Promise<PlanningGenerationResponse> {
    try {
      console.log('🚀 [API] Generating planning:', request)

      const response = await fetch(`${this.baseUrl}/generate`, {
        method: 'POST',
        headers: this.headers,
        body: JSON.stringify(request)
      })

      if (!response.ok) {
        const errorText = await response.text()
        throw this.createApiError(`Planning generation failed: ${response.status}`, response.status, errorText)
      }

      const data = await response.json() as PlanningGenerationResponse
      console.log('✅ [API] Planning generated:', data)

      return data
    } catch (error) {
      console.error('❌ [API] Planning generation error:', error)
      throw this.handleError(error, 'Planning generation failed')
    }
  }

  /**
   * ✅ FIXED: ApiService getPlannings method
   * Replace this method in your src/services/apiService.ts file
   */
  async getPlannings(date?: string): Promise<PlanningResponse[]> {
    try {
      const url = date
        ? `/api/planning/view-simple?date=${date}`
        : `/api/planning/view-simple`

      console.log('📋 [API] Loading plannings from:', url)

      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        }
      })

      if (!response.ok) {
        throw this.createApiError(`Failed to load plannings: ${response.status}`, response.status)
      }

      const data = await response.json()
      console.log('✅ [API] Raw plannings data:', data)

      // ✅ VERIFY DATA FORMAT
      if (!Array.isArray(data)) {
        console.error('❌ Backend returned non-array data:', data)
        throw new Error('Invalid data format from backend')
      }

      // ✅ TRANSFORM DATA with proper mapping
      const transformedData = data.map((item: any): PlanningResponse => ({
        id: item.id || '',
        orderId: item.orderId || '',
        orderNumber: item.orderNumber || 'Unknown',
        employeeId: item.employeeId || '',
        employeeName: item.employeeName || 'Unknown Employee',
        planningDate: item.planningDate || '',
        startTime: item.startTime || '',
        endTime: item.endTime || '',
        durationMinutes: item.durationMinutes || 0,
        priority: item.priority || 'MEDIUM',
        status: item.status || 'SCHEDULED',
        cardCount: item.cardCount || 0,
        notes: item.notes || '',
        completed: Boolean(item.completed)
      }))

      console.log(`✅ [API] Transformed ${transformedData.length} plannings`)
      return transformedData

    } catch (error) {
      console.error('❌ [API] Load plannings error:', error)
      throw this.handleError(error, 'Failed to load plannings')
    }
  }
  /**
   * Get plannings with employee/order details (using JOINs)
   */
  async getPlanningsWithDetails(date?: string): Promise<PlanningResponse[]> {
    try {
      const url = date
        ? `${this.baseUrl}/view?date=${date}`
        : `${this.baseUrl}/view`

      console.log('📋 [API] Loading detailed plannings from:', url)

      const response = await fetch(url, {
        method: 'GET',
        headers: this.headers
      })

      if (!response.ok) {
        throw this.createApiError(`Failed to load detailed plannings: ${response.status}`, response.status)
      }

      const data = await response.json() as any[]
      console.log('✅ [API] Detailed plannings loaded:', data.length)

      return data.map(this.transformDetailedPlanningData)
    } catch (error) {
      console.error('❌ [API] Load detailed plannings error:', error)
      throw this.handleError(error, 'Failed to load detailed plannings')
    }
  }

  // ========== SYSTEM METHODS ==========

  /**
   * Get system debug information
   */
  async getSystemDebug(): Promise<SystemDebugResponse> {
    try {
      console.log('🔍 [API] Loading system debug info')

      const response = await fetch(`${this.baseUrl}/debug-real`, {
        method: 'GET',
        headers: this.headers
      })

      if (!response.ok) {
        throw this.createApiError(`Debug request failed: ${response.status}`, response.status)
      }

      const data = await response.json() as SystemDebugResponse
      console.log('✅ [API] System debug loaded:', data)

      return data
    } catch (error) {
      console.error('❌ [API] System debug error:', error)
      throw this.handleError(error, 'Failed to load system debug info')
    }
  }

  /**
   * Get system statistics
   */
  async getSystemStats(): Promise<SystemStatsResponse> {
    try {
      console.log('📊 [API] Loading system stats')

      const response = await fetch(`${this.baseUrl}/stats`, {
        method: 'GET',
        headers: this.headers
      })

      if (!response.ok) {
        throw this.createApiError(`Stats request failed: ${response.status}`, response.status)
      }

      const data = await response.json() as SystemStatsResponse
      console.log('✅ [API] System stats loaded:', data)

      return data
    } catch (error) {
      console.error('❌ [API] System stats error:', error)
      throw this.handleError(error, 'Failed to load system stats')
    }
  }

  /**
   * Test single planning save (for debugging)
   */
  async testSave(): Promise<any> {
    try {
      console.log('🧪 [API] Testing single save')

      const response = await fetch(`${this.baseUrl}/test-save`, {
        method: 'POST',
        headers: this.headers
      })

      if (!response.ok) {
        throw this.createApiError(`Test save failed: ${response.status}`, response.status)
      }

      const data = await response.json()
      console.log('✅ [API] Test save result:', data)

      return data
    } catch (error) {
      console.error('❌ [API] Test save error:', error)
      throw this.handleError(error, 'Test save failed')
    }
  }

  /**
   * Check planning table status
   */
  async checkTable(): Promise<any> {
    try {
      console.log('🔧 [API] Checking table status')

      const response = await fetch(`${this.baseUrl}/check-table`, {
        method: 'GET',
        headers: this.headers
      })

      if (!response.ok) {
        throw this.createApiError(`Table check failed: ${response.status}`, response.status)
      }

      const data = await response.json()
      console.log('✅ [API] Table status:', data)

      return data
    } catch (error) {
      console.error('❌ [API] Table check error:', error)
      throw this.handleError(error, 'Table check failed')
    }
  }

  /**
   * Cleanup old plannings
   */
  async cleanupPlannings(): Promise<any> {
    try {
      console.log('🗑️ [API] Cleaning up old plannings')

      const response = await fetch(`${this.baseUrl}/cleanup`, {
        method: 'DELETE',
        headers: this.headers
      })

      if (!response.ok) {
        throw this.createApiError(`Cleanup failed: ${response.status}`, response.status)
      }

      const data = await response.json()
      console.log('✅ [API] Cleanup result:', data)

      return data
    } catch (error) {
      console.error('❌ [API] Cleanup error:', error)
      throw this.handleError(error, 'Cleanup failed')
    }
  }

  // ========== UTILITY METHODS ==========

  /**
   * Get combined system information (debug + stats)
   */
  async getSystemInfo(): Promise<{
    debug: SystemDebugResponse
    stats: SystemStatsResponse
  }> {
    try {
      const [debug, stats] = await Promise.all([
        this.getSystemDebug(),
        this.getSystemStats()
      ])

      return { debug, stats }
    } catch (error) {
      console.error('❌ [API] System info error:', error)
      throw this.handleError(error, 'Failed to load system information')
    }
  }

  /**
   * Get planning summary for a date range
   */
  async getPlanningsSummary(startDate: string, endDate: string): Promise<{
    plannings: PlanningResponse[]
    totalCount: number
    statusBreakdown: Record<string, number>
    priorityBreakdown: Record<string, number>
  }> {
    try {
      const plannings = await this.getPlannings(startDate)

      // Filter by date range if endDate is different
      const filteredPlannings = plannings.filter(p =>
        p.planningDate >= startDate && p.planningDate <= endDate
      )

      // Calculate breakdowns
      const statusBreakdown: Record<string, number> = {}
      const priorityBreakdown: Record<string, number> = {}

      filteredPlannings.forEach(planning => {
        statusBreakdown[planning.status] = (statusBreakdown[planning.status] || 0) + 1
        priorityBreakdown[planning.priority] = (priorityBreakdown[planning.priority] || 0) + 1
      })

      return {
        plannings: filteredPlannings,
        totalCount: filteredPlannings.length,
        statusBreakdown,
        priorityBreakdown
      }
    } catch (error) {
      console.error('❌ [API] Planning summary error:', error)
      throw this.handleError(error, 'Failed to get planning summary')
    }
  }

  // ========== PRIVATE HELPERS ==========

  private transformPlanningData(item: any): PlanningResponse {
    return {
      id: item.id,
      orderId: item.orderId,
      orderNumber: this.extractOrderNumber(item.notes),
      employeeId: item.employeeId,
      employeeName: item.employeeName,
      planningDate: item.planningDate,
      startTime: item.startTime,
      endTime: item.endTime,
      durationMinutes: item.durationMinutes || item.estimated_duration_minutes,
      priority: item.priority,
      status: item.status,
      cardCount: item.cardCount,
      notes: item.notes,
      completed: Boolean(item.completed)
    }
  }

  private transformDetailedPlanningData(item: any): PlanningResponse {
    return {
      id: item.id,
      orderId: item.orderId,
      orderNumber: item.orderNumber,
      employeeId: item.employeeId,
      employeeName: item.employeeName,
      planningDate: item.planningDate,
      startTime: item.startTime,
      endTime: item.endTime,
      durationMinutes: item.durationMinutes,
      priority: item.priority,
      status: item.status,
      cardCount: item.cardCount,
      notes: item.notes,
      completed: Boolean(item.completed)
    }
  }

  private extractOrderNumber(notes?: string): string {
    if (!notes) return 'Unknown'
    const match = notes.match(/order (\w+)/)
    return match ? match[1] : 'Unknown'
  }

  private createApiError(message: string, status?: number, details?: string): ApiError {
    const error = new Error(details ? `${message}: ${details}` : message) as ApiError
    error.status = status
    error.name = 'ApiError'
    return error
  }

  private handleError(error: unknown, fallbackMessage: string): ApiError {
    if (error instanceof Error) {
      return error as ApiError
    }
    return this.createApiError(fallbackMessage, undefined, String(error))
  }
}

// ========== SINGLETON INSTANCE ==========
export const apiService = new EnglishApiService()

// ========== CONVENIENCE FUNCTIONS ==========

/**
 * Quick function to generate planning
 */
export const generatePlanning = (config: OrderRequest) =>
  apiService.generatePlanning(config)

/**
 * Quick function to get today's plannings
 */
export const getTodaysPlannings = () =>
  apiService.getPlannings(new Date().toISOString().split('T')[0])

/**
 * Quick function to get system status
 */
export const getSystemStatus = () =>
  apiService.getSystemInfo()

/**
 * Format planning for display
 */
export const formatPlanningForDisplay = (planning: PlanningResponse) => ({
  ...planning,
  formattedDate: new Date(planning.planningDate).toLocaleDateString('en-US', {
    weekday: 'short',
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  }),
  formattedStartTime: new Date(planning.startTime).toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit'
  }),
  formattedDuration: `${planning.durationMinutes} min`,
  priorityColor: {
    'URGENT': 'red',
    'HIGH': 'orange',
    'MEDIUM': 'yellow',
    'LOW': 'green'
  }[planning.priority] || 'gray',
  statusColor: {
    'SCHEDULED': 'blue',
    'IN_PROGRESS': 'orange',
    'COMPLETED': 'green',
    'CANCELLED': 'red'
  }[planning.status] || 'gray'
})

// ========== ERROR HANDLING UTILITIES ==========

export const isApiError = (error: unknown): error is ApiError => {
  return error instanceof Error && error.name === 'ApiError'
}

export const getErrorMessage = (error: unknown): string => {
  if (isApiError(error)) {
    return error.message
  }
  if (error instanceof Error) {
    return error.message
  }
  return 'An unknown error occurred'
}

export const getErrorDetails = (error: unknown): { message: string; status?: number; details?: string } => {
  if (isApiError(error)) {
    return {
      message: error.message,
      status: error.status,
      details: error.code
    }
  }
  if (error instanceof Error) {
    return {
      message: error.message
    }
  }
  return {
    message: 'An unknown error occurred',
    details: String(error)
  }
}
