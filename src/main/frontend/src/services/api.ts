import {
  Order,
  OrderResponse,
  OrdersListResponse,
  OrderFilters,
  Card,
  CardResponse,
  Employee,
  EmployeeResponse,
  EmployeeListItem,
  EmployeeDetail,
  EmployeePlanningResponse,
  EmployeeFilters,
  Planning,
  PlanningResponse,
  PlanningFilters,
  DashboardStats,
  SystemStatus,
  ApiResponse,
  ApiServiceInterface,
  OrderNotFoundError,
  EmployeeNotFoundError,
  PlanningExecutionError
} from '../types/english-api'

/**
 * English API Service for Pokemon Order Planning System
 * Complete translation from French version
 */
export class EnglishApiService implements ApiServiceInterface {
  private baseUrl = '/api'
  private frontendUrl = '/api/frontend'
  private planningUrl = '/api/planning-dp'
  private testUrl = '/api/test-planning'

  // ========== ORDER METHODS ==========

  /**
   * Retrieves the list of orders with optional filters
   */
  async getOrders(filters?: OrderFilters): Promise<OrdersListResponse> {
    try {
      console.log('🔍 Fetching orders with filters:', filters)

      const params = new URLSearchParams()
      if (filters?.priority) params.append('priority', filters.priority)
      if (filters?.status) params.append('status', filters.status)
      if (filters?.dateFrom) params.append('dateFrom', filters.dateFrom)
      if (filters?.dateTo) params.append('dateTo', filters.dateTo)
      if (filters?.searchTerm) params.append('search', filters.searchTerm)

      const url = `${this.frontendUrl}/orders${params.toString() ? '?' + params.toString() : ''}`
      const response = await fetch(url)

      if (!response.ok) {
        throw new Error(`Failed to fetch orders: ${response.status}`)
      }

      const data = await response.json()

      return {
        orders: data.orders || data.commandes || [],
        totalCount: data.totalCount || data.total || 0,
        pendingCount: data.pendingCount || data.enAttente || 0,
        scheduledCount: data.scheduledCount || data.planifiees || 0,
        completedCount: data.completedCount || data.terminees || 0,
        filters
      }
    } catch (error) {
      console.error('❌ Error fetching orders:', error)
      throw error
    }
  }

  /**
   * Retrieves a single order by ID
   */
  async getOrder(id: string): Promise<OrderResponse> {
    try {
      console.log('🔍 Fetching order:', id)

      const response = await fetch(`${this.frontendUrl}/orders/${id}`)

      if (!response.ok) {
        if (response.status === 404) {
          throw new OrderNotFoundError(id)
        }
        throw new Error(`Failed to fetch order: ${response.status}`)
      }

      const data = await response.json()
      return this.transformOrderToEnglish(data)
    } catch (error) {
      console.error('❌ Error fetching order:', error)
      throw error
    }
  }

  /**
   * Retrieves cards for a specific order
   */
  async getOrderCards(orderId: string): Promise<CardResponse[]> {
    try {
      console.log('🃏 Fetching cards for order:', orderId)

      const response = await fetch(`${this.frontendUrl}/orders/${orderId}/cards`)

      if (!response.ok) {
        throw new Error(`Failed to fetch cards: ${response.status}`)
      }

      const data = await response.json()

      if (data && data.cards && Array.isArray(data.cards)) {
        return data.cards.map((card: any) => this.transformCardToEnglish(card))
      }

      if (data && data.cartes && Array.isArray(data.cartes)) {
        return data.cartes.map((card: any) => this.transformCardToEnglish(card))
      }

      return []
    } catch (error) {
      console.error('❌ Error fetching order cards:', error)
      throw error
    }
  }

  /**
   * Creates a new order
   */
  async createOrder(order: Partial<Order>): Promise<OrderResponse> {
    try {
      console.log('📝 Creating order:', order)

      const response = await fetch(`${this.frontendUrl}/orders`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(this.transformOrderToFrench(order))
      })

      if (!response.ok) {
        throw new Error(`Failed to create order: ${response.status}`)
      }

      const data = await response.json()
      return this.transformOrderToEnglish(data)
    } catch (error) {
      console.error('❌ Error creating order:', error)
      throw error
    }
  }

  /**
   * Updates an existing order
   */
  async updateOrder(id: string, order: Partial<Order>): Promise<OrderResponse> {
    try {
      console.log('📝 Updating order:', id, order)

      const response = await fetch(`${this.frontendUrl}/orders/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(this.transformOrderToFrench(order))
      })

      if (!response.ok) {
        if (response.status === 404) {
          throw new OrderNotFoundError(id)
        }
        throw new Error(`Failed to update order: ${response.status}`)
      }

      const data = await response.json()
      return this.transformOrderToEnglish(data)
    } catch (error) {
      console.error('❌ Error updating order:', error)
      throw error
    }
  }

  /**
   * Deletes an order
   */
  async deleteOrder(id: string): Promise<void> {
    try {
      console.log('🗑️ Deleting order:', id)

      const response = await fetch(`${this.frontendUrl}/orders/${id}`, {
        method: 'DELETE'
      })

      if (!response.ok) {
        if (response.status === 404) {
          throw new OrderNotFoundError(id)
        }
        throw new Error(`Failed to delete order: ${response.status}`)
      }
    } catch (error) {
      console.error('❌ Error deleting order:', error)
      throw error
    }
  }

  // ========== EMPLOYEE METHODS ==========

  /**
   * Retrieves the list of employees
   */
  async getEmployees(filters?: EmployeeFilters): Promise<EmployeeResponse[]> {
    try {
      console.log('👥 Fetching employees with filters:', filters)

      const params = new URLSearchParams()
      if (filters?.active !== undefined) params.append('active', filters.active.toString())
      if (filters?.status) params.append('status', filters.status)
      if (filters?.searchTerm) params.append('search', filters.searchTerm)

      const url = `${this.frontendUrl}/employees${params.toString() ? '?' + params.toString() : ''}`
      const response = await fetch(url)

      if (!response.ok) {
        throw new Error(`Failed to fetch employees: ${response.status}`)
      }

      const data = await response.json()
      const employees = Array.isArray(data) ? data : (data.employees || data.employes || [])

      return employees.map((employee: any) => this.transformEmployeeToEnglish(employee))
    } catch (error) {
      console.error('❌ Error fetching employees:', error)
      throw error
    }
  }

  /**
   * Retrieves a single employee by ID
   */
  async getEmployee(id: string): Promise<EmployeeResponse> {
    try {
      console.log('👤 Fetching employee:', id)

      const response = await fetch(`${this.frontendUrl}/employees/${id}`)

      if (!response.ok) {
        if (response.status === 404) {
          throw new EmployeeNotFoundError(id)
        }
        throw new Error(`Failed to fetch employee: ${response.status}`)
      }

      const data = await response.json()
      return this.transformEmployeeToEnglish(data)
    } catch (error) {
      console.error('❌ Error fetching employee:', error)
      throw error
    }
  }

  /**
   * Retrieves employee planning for a specific date
   */
  async getEmployeePlanning(date?: string): Promise<EmployeePlanningResponse> {
    try {
      const targetDate = date || new Date().toISOString().split('T')[0]
      console.log('📅 Fetching employee planning for:', targetDate)

      const response = await fetch(`${this.frontendUrl}/employee-planning?date=${targetDate}`)

      if (!response.ok) {
        throw new Error(`Failed to fetch employee planning: ${response.status}`)
      }

      const data = await response.json()

      return {
        employees: (data.employees || data.employes || []).map((emp: any) => this.transformEmployeeListItemToEnglish(emp)),
        totalEmployees: data.totalEmployees || data.totalEmployes || 0,
        availableEmployees: data.availableEmployees || data.employesDisponibles || 0,
        overloadedEmployees: data.overloadedEmployees || data.employesSurcharges || 0,
        planningDate: targetDate
      }
    } catch (error) {
      console.error('❌ Error fetching employee planning:', error)
      throw error
    }
  }

  /**
   * Retrieves detailed information for a specific employee
   */
  async getEmployeeDetails(id: string, date?: string): Promise<EmployeeDetail> {
    try {
      const targetDate = date || new Date().toISOString().split('T')[0]
      console.log('👤 Fetching employee details:', id, 'for date:', targetDate)

      const response = await fetch(`${this.frontendUrl}/employees/${id}/details?date=${targetDate}`)

      if (!response.ok) {
        if (response.status === 404) {
          throw new EmployeeNotFoundError(id)
        }
        throw new Error(`Failed to fetch employee details: ${response.status}`)
      }

      const data = await response.json()
      return this.transformEmployeeDetailToEnglish(data)
    } catch (error) {
      console.error('❌ Error fetching employee details:', error)
      throw error
    }
  }

  /**
   * Creates a new employee
   */
  async createEmployee(employee: Partial<Employee>): Promise<EmployeeResponse> {
    try {
      console.log('📝 Creating employee:', employee)

      const response = await fetch(`${this.frontendUrl}/employees`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(this.transformEmployeeToFrench(employee))
      })

      if (!response.ok) {
        throw new Error(`Failed to create employee: ${response.status}`)
      }

      const data = await response.json()
      return this.transformEmployeeToEnglish(data)
    } catch (error) {
      console.error('❌ Error creating employee:', error)
      throw error
    }
  }

  /**
   * Updates an existing employee
   */
  async updateEmployee(id: string, employee: Partial<Employee>): Promise<EmployeeResponse> {
    try {
      console.log('📝 Updating employee:', id, employee)

      const response = await fetch(`${this.frontendUrl}/employees/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(this.transformEmployeeToFrench(employee))
      })

      if (!response.ok) {
        if (response.status === 404) {
          throw new EmployeeNotFoundError(id)
        }
        throw new Error(`Failed to update employee: ${response.status}`)
      }

      const data = await response.json()
      return this.transformEmployeeToEnglish(data)
    } catch (error) {
      console.error('❌ Error updating employee:', error)
      throw error
    }
  }

  /**
   * Deletes an employee
   */
  async deleteEmployee(id: string): Promise<void> {
    try {
      console.log('🗑️ Deleting employee:', id)

      const response = await fetch(`${this.frontendUrl}/employees/${id}`, {
        method: 'DELETE'
      })

      if (!response.ok) {
        if (response.status === 404) {
          throw new EmployeeNotFoundError(id)
        }
        throw new Error(`Failed to delete employee: ${response.status}`)
      }
    } catch (error) {
      console.error('❌ Error deleting employee:', error)
      throw error
    }
  }

  // ========== PLANNING METHODS ==========

  /**
   * Retrieves planning entries with filters
   */
  async getPlannings(filters?: PlanningFilters): Promise<PlanningResponse[]> {
    try {
      console.log('📅 Fetching plannings with filters:', filters)

      const params = new URLSearchParams()
      if (filters?.employeeId) params.append('employeeId', filters.employeeId)
      if (filters?.dateFrom) params.append('dateFrom', filters.dateFrom)
      if (filters?.dateTo) params.append('dateTo', filters.dateTo)
      if (filters?.completed !== undefined) params.append('completed', filters.completed.toString())

      const url = `${this.frontendUrl}/plannings${params.toString() ? '?' + params.toString() : ''}`
      const response = await fetch(url)

      if (!response.ok) {
        throw new Error(`Failed to fetch plannings: ${response.status}`)
      }

      const data = await response.json()
      const plannings = Array.isArray(data) ? data : (data.plannings || data.planifications || [])

      return plannings.map((planning: any) => this.transformPlanningToEnglish(planning))
    } catch (error) {
      console.error('❌ Error fetching plannings:', error)
      throw error
    }
  }

  /**
   * Executes the dynamic programming planning algorithm
   */
  async executePlanning(day: number, month: number, year: number): Promise<ApiResponse<any>> {
    try {
      console.log('🚀 Executing planning for:', { day, month, year })

      const response = await fetch(`${this.planningUrl}/execute`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ day, month, year })
      })

      if (!response.ok) {
        throw new PlanningExecutionError(`HTTP ${response.status}`)
      }

      const data = await response.json()

      return {
        success: data.success || false,
        data: data.data || data,
        message: data.message || 'Planning executed successfully',
        timestamp: new Date().toISOString()
      }
    } catch (error) {
      console.error('❌ Error executing planning:', error)
      throw error
    }
  }

  /**
   * Performs a quick test of the planning system
   */
  async quickTest(): Promise<SystemStatus> {
    try {
      console.log('🧪 Running quick test')

      const response = await fetch(`${this.testUrl}/quick-test`)

      if (!response.ok) {
        throw new Error(`Failed to run quick test: ${response.status}`)
      }

      const data = await response.json()

      return {
        status: data.success ? 'OK' : 'ERROR',
        message: data.message || 'Quick test completed',
        timestamp: new Date().toISOString()
      }
    } catch (error) {
      console.error('❌ Error running quick test:', error)
      return {
        status: 'ERROR',
        message: error instanceof Error ? error.message : 'Unknown error',
        timestamp: new Date().toISOString()
      }
    }
  }

  /**
   * Gets system diagnostic information
   */
  async getDiagnostic(): Promise<SystemStatus> {
    try {
      console.log('🔍 Getting system diagnostic')

      const response = await fetch(`${this.testUrl}/diagnostic`)

      if (!response.ok) {
        throw new Error(`Failed to get diagnostic: ${response.status}`)
      }

      const data = await response.json()

      return {
        status: data.success ? 'OK' : 'WARNING',
        message: data.message || 'Diagnostic completed',
        timestamp: new Date().toISOString(),
        dataQuality: data.dataQuality || data.qualiteDonnees
      }
    } catch (error) {
      console.error('❌ Error getting diagnostic:', error)
      return {
        status: 'ERROR',
        message: error instanceof Error ? error.message : 'Unknown error',
        timestamp: new Date().toISOString()
      }
    }
  }

  // ========== DASHBOARD METHODS ==========

  /**
   * Gets dashboard statistics
   */
  async getDashboardStats(): Promise<DashboardStats> {
    try {
      console.log('📊 Fetching dashboard stats')

      const response = await fetch(`${this.baseUrl}/dashboard/stats`)

      if (!response.ok) {
        throw new Error(`Failed to fetch dashboard stats: ${response.status}`)
      }

      const data = await response.json()

      return {
        pendingOrders: data.pendingOrders || data.commandesEnAttente || 0,
        inProgressOrders: data.inProgressOrders || data.commandesEnCours || 0,
        completedOrders: data.completedOrders || data.commandesTerminees || 0,
        totalOrders: data.totalOrders || data.totalCommandes || 0,
        activeEmployees: data.activeEmployees || data.employesActifs || 0,
        status: data.status || 'OK',
        timestamp: new Date().toISOString(),
        lastUpdate: data.lastUpdate || data.derniereMAJ
      }
    } catch (error) {
      console.error('❌ Error fetching dashboard stats:', error)
      throw error
    }
  }

  /**
   * Gets current system status
   */
  async getSystemStatus(): Promise<SystemStatus> {
    try {
      console.log('⚡ Getting system status')

      const response = await fetch(`${this.baseUrl}/system/status`)

      if (!response.ok) {
        throw new Error(`Failed to get system status: ${response.status}`)
      }

      const data = await response.json()

      return {
        status: data.status || 'OK',
        message: data.message || 'System operational',
        timestamp: new Date().toISOString()
      }
    } catch (error) {
      console.error('❌ Error getting system status:', error)
      return {
        status: 'ERROR',
        message: error instanceof Error ? error.message : 'Unknown error',
        timestamp: new Date().toISOString()
      }
    }
  }

  // ========== TRANSFORMATION METHODS ==========

  private transformOrderToEnglish(frenchOrder: any): OrderResponse {
    return {
      id: frenchOrder.id,
      orderNumber: frenchOrder.numeroCommande || frenchOrder.orderNumber,
      cardCount: frenchOrder.nombreCartes || frenchOrder.cardCount || 0,
      cardsWithName: frenchOrder.nombreAvecNom || frenchOrder.cardsWithName,
      percentageWithName: frenchOrder.pourcentageAvecNom || frenchOrder.percentageWithName,
      totalPrice: frenchOrder.prixTotal || frenchOrder.totalPrice,
      priority: this.transformPriorityToEnglish(frenchOrder.priorite || frenchOrder.priority),
      status: this.transformStatusToEnglish(frenchOrder.statut || frenchOrder.status),
      statusCode: frenchOrder.status || frenchOrder.statusCode || 1,
      creationDate: frenchOrder.dateCreation || frenchOrder.creationDate,
      deadline: frenchOrder.dateLimite || frenchOrder.deadline,
      estimatedTimeMinutes: frenchOrder.tempsEstimeMinutes || frenchOrder.estimatedTimeMinutes || 120,
      orderQuality: this.transformQualityToEnglish(frenchOrder.qualiteCommande || frenchOrder.orderQuality)
    }
  }

  private transformOrderToFrench(englishOrder: any): any {
    return {
      numeroCommande: englishOrder.orderNumber,
      nombreCartes: englishOrder.cardCount,
      nombreAvecNom: englishOrder.cardsWithName,
      pourcentageAvecNom: englishOrder.percentageWithName,
      prixTotal: englishOrder.totalPrice,
      priorite: this.transformPriorityToFrench(englishOrder.priority),
      statut: this.transformStatusToFrench(englishOrder.status),
      dateCreation: englishOrder.creationDate,
      dateLimite: englishOrder.deadline,
      tempsEstimeMinutes: englishOrder.estimatedTimeMinutes,
      qualiteCommande: this.transformQualityToFrench(englishOrder.orderQuality)
    }
  }

  private transformCardToEnglish(frenchCard: any): CardResponse {
    return {
      id: frenchCard.carteId || frenchCard.id,
      certificationId: frenchCard.carteId || frenchCard.certificationId,
      cardId: frenchCard.cardId || frenchCard.carteId,
      name: frenchCard.nom || frenchCard.name || 'Unknown card',
      labelName: frenchCard.labelNom || frenchCard.labelName || 'Card to certify',
      barcode: frenchCard.codeBarre || frenchCard.barcode || 'N/A',
      type: frenchCard.type || 'Pokemon',
      annotation: frenchCard.annotation || '',
      language: frenchCard.langue || frenchCard.language || 'FR',
      withName: frenchCard.avecNom || frenchCard.withName || false
    }
  }

  private transformEmployeeToEnglish(frenchEmployee: any): EmployeeResponse {
    return {
      id: frenchEmployee.id,
      firstName: frenchEmployee.prenom || frenchEmployee.firstName,
      lastName: frenchEmployee.nom || frenchEmployee.lastName,
      email: frenchEmployee.email,
      workHoursPerDay: frenchEmployee.heuresTravailParJour || frenchEmployee.workHoursPerDay || 8,
      active: frenchEmployee.actif !== undefined ? frenchEmployee.actif : frenchEmployee.active !== false,
      fullName: frenchEmployee.nomComplet || frenchEmployee.fullName || `${frenchEmployee.prenom || frenchEmployee.firstName} ${frenchEmployee.nom || frenchEmployee.lastName}`
    }
  }

  private transformEmployeeToFrench(englishEmployee: any): any {
    return {
      prenom: englishEmployee.firstName,
      nom: englishEmployee.lastName,
      email: englishEmployee.email,
      heuresTravailParJour: englishEmployee.workHoursPerDay,
      actif: englishEmployee.active
    }
  }

  private transformEmployeeListItemToEnglish(frenchItem: any): EmployeeListItem {
    return {
      id: frenchItem.id,
      name: frenchItem.name || frenchItem.nom || frenchItem.nomComplet,
      totalMinutes: frenchItem.totalMinutes || frenchItem.minutesTotales || 0,
      maxMinutes: frenchItem.maxMinutes || frenchItem.minutesMax || 480,
      status: frenchItem.status || this.calculateEmployeeStatus(frenchItem.totalMinutes, frenchItem.maxMinutes),
      taskCount: frenchItem.taskCount || frenchItem.nombreTaches || 0,
      cardCount: frenchItem.cardCount || frenchItem.nombreCartes || 0,
      completedTasks: frenchItem.completedTasks || frenchItem.tachesTerminees
    }
  }

  private transformEmployeeDetailToEnglish(frenchDetail: any): EmployeeDetail {
    return {
      id: frenchDetail.id,
      name: frenchDetail.name || frenchDetail.nom,
      totalMinutes: frenchDetail.totalMinutes || frenchDetail.minutesTotales || 0,
      maxMinutes: frenchDetail.maxMinutes || frenchDetail.minutesMax || 480,
      status: frenchDetail.status || this.calculateEmployeeStatus(frenchDetail.totalMinutes, frenchDetail.maxMinutes),
      tasks: (frenchDetail.tasks || frenchDetail.taches || []).map((task: any) => this.transformTaskToEnglish(task))
    }
  }

  private transformTaskToEnglish(frenchTask: any): any {
    return {
      id: frenchTask.id,
      orderNumber: frenchTask.numeroCommande || frenchTask.orderNumber,
      priority: this.transformPriorityToEnglish(frenchTask.priorite || frenchTask.priority || 'NORMALE'),
      status: this.transformTaskStatusToEnglish(frenchTask.status || frenchTask.statut),
      startTime: frenchTask.heureDebut || frenchTask.startTime || '09:00',
      endTime: frenchTask.heureFin || frenchTask.endTime || '17:00',
      duration: frenchTask.dureeCalculee || frenchTask.duration || 120,
      amount: frenchTask.amount || 0,
      cardCount: frenchTask.nombreCartes || frenchTask.cardCount || 0,
      cards: (frenchTask.cards || frenchTask.cartes || []).map((card: any) => this.transformCardToEnglish(card)),
      expanded: frenchTask.expanded || false,
      completed: frenchTask.terminee || frenchTask.completed || false,
      loadingCards: frenchTask.loadingCards || false
    }
  }

  private transformPlanningToEnglish(frenchPlanning: any): PlanningResponse {
    return {
      id: frenchPlanning.id,
      orderId: frenchPlanning.commandeId || frenchPlanning.orderId,
      employeeId: frenchPlanning.employeId || frenchPlanning.employeeId,
      scheduledDate: frenchPlanning.datePlanifiee || frenchPlanning.scheduledDate,
      startTime: frenchPlanning.heureDebut || frenchPlanning.startTime,
      endTime: frenchPlanning.heureFin || frenchPlanning.endTime,
      durationMinutes: frenchPlanning.dureeMinutes || frenchPlanning.durationMinutes,
      completed: frenchPlanning.terminee || frenchPlanning.completed || false,
      orderNumber: frenchPlanning.numeroCommande || frenchPlanning.orderNumber,
      priority: this.transformPriorityToEnglish(frenchPlanning.priorite || frenchPlanning.priority),
      employeeName: frenchPlanning.employeNom || frenchPlanning.employeeName
    }
  }

  // Helper transformation methods
  private transformPriorityToEnglish(frenchPriority: string): string {
    const priorityMap: Record<string, string> = {
      'URGENTE': 'URGENT',
      'HAUTE': 'HIGH',
      'MOYENNE': 'MEDIUM',
      'NORMALE': 'NORMAL',
      'BASSE': 'LOW'
    }
    return priorityMap[frenchPriority] || frenchPriority || 'NORMAL'
  }

  private transformPriorityToFrench(englishPriority: string): string {
    const priorityMap: Record<string, string> = {
      'URGENT': 'URGENTE',
      'HIGH': 'HAUTE',
      'MEDIUM': 'MOYENNE',
      'NORMAL': 'NORMALE',
      'LOW': 'BASSE'
    }
    return priorityMap[englishPriority] || englishPriority || 'NORMALE'
  }

  private transformStatusToEnglish(frenchStatus: string): string {
    const statusMap: Record<string, string> = {
      'EN_ATTENTE': 'PENDING',
      'PLANIFIEE': 'SCHEDULED',
      'EN_COURS': 'IN_PROGRESS',
      'TERMINEE': 'COMPLETED',
      'ANNULEE': 'CANCELLED'
    }
    return statusMap[frenchStatus] || frenchStatus || 'PENDING'
  }

  private transformStatusToFrench(englishStatus: string): string {
    const statusMap: Record<string, string> = {
      'PENDING': 'EN_ATTENTE',
      'SCHEDULED': 'PLANIFIEE',
      'IN_PROGRESS': 'EN_COURS',
      'COMPLETED': 'TERMINEE',
      'CANCELLED': 'ANNULEE'
    }
    return statusMap[englishStatus] || englishStatus || 'EN_ATTENTE'
  }

  private transformTaskStatusToEnglish(frenchStatus: string): string {
    const statusMap: Record<string, string> = {
      'En cours': 'In Progress',
      'Planifiée': 'Scheduled',
      'Terminée': 'Completed'
    }
    return statusMap[frenchStatus] || frenchStatus || 'Scheduled'
  }

  private transformQualityToEnglish(frenchQuality: string): string {
    const qualityMap: Record<string, string> = {
      'EXCELLENTE': 'EXCELLENT',
      'BONNE': 'GOOD',
      'MOYENNE': 'AVERAGE',
      'FAIBLE': 'POOR',
      'PARFAITE': 'PERFECT'
    }
    return qualityMap[frenchQuality] || frenchQuality || 'GOOD'
  }

  private transformQualityToFrench(englishQuality: string): string {
    const qualityMap: Record<string, string> = {
      'EXCELLENT': 'EXCELLENTE',
      'GOOD': 'BONNE',
      'AVERAGE': 'MOYENNE',
      'POOR': 'FAIBLE',
      'PERFECT': 'PARFAITE'
    }
    return qualityMap[englishQuality] || englishQuality || 'BONNE'
  }

  private calculateEmployeeStatus(totalMinutes: number, maxMinutes: number): 'available' | 'full' | 'overloaded' {
    if (!totalMinutes || !maxMinutes) return 'available'

    const utilization = totalMinutes / maxMinutes

    if (utilization > 1.0) return 'overloaded'
    if (utilization >= 0.8) return 'full'
    return 'available'
  }

  // ========== UTILITY METHODS ==========

  /**
   * Formats duration from minutes to readable string
   */
  formatDuration(minutes: number): string {
    const hours = Math.floor(minutes / 60)
    const mins = minutes % 60
    return hours > 0 ? `${hours}h${mins.toString().padStart(2, '0')}` : `${mins}min`
  }

  /**
   * Calculates end time from start time and duration
   */
  calculateEndTime(startTime: string, durationMinutes: number): string {
    if (!startTime) return '00:00'

    const [hours, minutes] = startTime.split(':').map(Number)
    const totalMinutes = hours * 60 + minutes + durationMinutes

    const endHours = Math.floor(totalMinutes / 60) % 24
    const endMinutes = totalMinutes % 60

    return `${endHours.toString().padStart(2, '0')}:${endMinutes.toString().padStart(2, '0')}`
  }

  /**
   * Gets quality label from percentage
   */
  getQualityFromPercentage(percentage: number): string {
    if (percentage >= 95) return 'EXCELLENT'
    if (percentage >= 85) return 'GOOD'
    if (percentage >= 70) return 'AVERAGE'
    return 'POOR'
  }
}

// Export singleton instance
export default new EnglishApiService()
