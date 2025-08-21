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
