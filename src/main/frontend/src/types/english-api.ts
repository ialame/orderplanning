// ============= TYPES TYPESCRIPT ANGLAIS =============
// Fichier: src/main/frontend/src/types/english-api.ts

// ========== CORE INTERFACES ==========

export interface Order {
  id?: string
  orderNumber: string
  cardCount: number
  cardsWithName?: number
  percentageWithName?: number
  totalPrice?: number
  priority: 'URGENT' | 'HIGH' | 'MEDIUM' | 'NORMAL' | 'LOW'
  status?: 'PENDING' | 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
  statusCode?: number
  creationDate?: string
  date?: string
  deadline?: string
  estimatedTimeMinutes: number
  cardNames?: string[]
  nameSample?: string
  orderQuality?: 'EXCELLENT' | 'GOOD' | 'AVERAGE' | 'POOR'
  cardsWithoutMissingData?: boolean
}

export interface OrderResponse {
  id: string
  orderNumber: string
  cardCount: number
  cardsWithName?: number
  percentageWithName?: number
  totalPrice?: number
  priority: string
  status: string
  statusCode: number
  creationDate?: string
  deadline?: string
  estimatedTimeMinutes: number
  orderQuality?: string
}

export interface Card {
  id: string
  certificationId?: string
  cardId?: string
  barcode?: string
  originalLanguage?: string
  cardName?: string
  cardLabel?: string
  translationLocale?: string
  edition?: number
  nameQuality?: 'WITH_NAME' | 'WITHOUT_NAME'
  type?: string
  annotation?: string
  certificationLanguage?: string
  language?: string
  duration: number
  amount?: number
  correspondenceStatus?: string
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

export interface CardsDetail {
  cardCount: number
  cardsWithName: number
  percentageWithName: number
  cardsSummary: Record<string, number>
  cardNames: string[]
  cardsDetails?: CardCertification[]
  globalQuality?: 'PERFECT' | 'EXCELLENT' | 'GOOD' | 'AVERAGE'
}

export interface CardCertification {
  certificationId: string
  cardId: string
  barcode: string
  originalLanguage: string
  cardName: string
  cardLabel: string
  translationLocale: string
  edition: number
  nameQuality: 'WITH_NAME' | 'WITHOUT_NAME'
}

// ========== EMPLOYEE INTERFACES ==========

export interface Employee {
  id?: string
  lastName: string
  firstName: string
  email: string
  workHoursPerDay: number
  active: boolean
  creationDate?: string
  fullName?: string
  available?: boolean
  currentLoad?: number
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

export interface EmployeeListItem {
  id: string
  name: string
  totalMinutes: number
  maxMinutes: number
  status: 'overloaded' | 'available' | 'full'
  taskCount: number
  cardCount: number
  completedTasks?: number
}

export interface EmployeeDetail {
  id: string
  name: string
  totalMinutes: number
  maxMinutes: number
  status: 'overloaded' | 'available' | 'full'
  tasks: Task[]
}

// ========== PLANNING INTERFACES ==========

export interface Planning {
  id?: string | number
  orderId?: string
  employeeId?: string
  scheduledDate?: string
  planningDate?: string
  startTime: string
  durationMinutes: number
  completed: boolean
  orderNumber?: string
  priority?: string
  employeeName?: string
  endTime?: string
  status?: string
}

export interface PlanningResponse {
  id: string
  orderId: string
  employeeId: string
  scheduledDate: string
  startTime: string
  endTime: string
  durationMinutes: number
  completed: boolean
  orderNumber: string
  priority: string
  employeeName: string
}

export interface Task {
  id: string
  orderNumber?: string
  priority: 'High' | 'Medium' | 'Low' | 'Normal'
  status: 'In Progress' | 'Scheduled' | 'Completed'
  startTime: string
  endTime: string
  scheduledStartTime?: string
  scheduledEndTime?: string
  duration: number
  calculatedDuration?: number
  amount: number
  cardCount: number
  totalCards?: number
  cards: Card[]
  expanded?: boolean
  completed?: boolean
  loadingCards?: boolean
}

// ========== DASHBOARD INTERFACES ==========

export interface DashboardStats {
  pendingOrders: number
  inProgressOrders: number
  completedOrders: number
  totalOrders: number
  activeEmployees?: number
  status: string
  timestamp?: string
  lastUpdate?: string
}

export interface SystemStatus {
  status: 'OK' | 'WARNING' | 'ERROR'
  message: string
  timestamp: string
  dataQuality?: {
    totalOrders: number
    ordersWithCards: number
    cardsWithNames: number
    percentageWithNames: number
  }
}

// ========== API RESPONSE WRAPPERS ==========

export interface ApiResponse<T> {
  success: boolean
  data?: T
  message?: string
  error?: string
  timestamp?: string
}

export interface PaginatedResponse<T> {
  items: T[]
  totalCount: number
  pageSize: number
  currentPage: number
  totalPages: number
  hasNextPage: boolean
  hasPreviousPage: boolean
}

export interface OrdersListResponse {
  orders: OrderResponse[]
  totalCount: number
  pendingCount: number
  scheduledCount: number
  completedCount: number
  filters?: {
    priority?: string
    status?: string
    dateFrom?: string
    dateTo?: string
  }
}

export interface EmployeePlanningResponse {
  employees: EmployeeListItem[]
  totalEmployees: number
  availableEmployees: number
  overloadedEmployees: number
  planningDate: string
}

// ========== ENUMS ==========

export enum OrderPriority {
  URGENT = 'URGENT',
  HIGH = 'HIGH',
  MEDIUM = 'MEDIUM',
  NORMAL = 'NORMAL',
  LOW = 'LOW'
}

export enum OrderStatus {
  PENDING = 'PENDING',
  SCHEDULED = 'SCHEDULED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

export enum EmployeeStatus {
  AVAILABLE = 'available',
  FULL = 'full',
  OVERLOADED = 'overloaded'
}

export enum CardQuality {
  EXCELLENT = 'EXCELLENT',
  GOOD = 'GOOD',
  AVERAGE = 'AVERAGE',
  POOR = 'POOR'
}

// ========== UTILITY TYPES ==========

export type OrderFilters = {
  priority?: OrderPriority
  status?: OrderStatus
  dateFrom?: string
  dateTo?: string
  searchTerm?: string
}

export type EmployeeFilters = {
  active?: boolean
  status?: EmployeeStatus
  searchTerm?: string
}

export type PlanningFilters = {
  employeeId?: string
  dateFrom?: string
  dateTo?: string
  completed?: boolean
}

// ========== API SERVICE INTERFACE ==========

export interface ApiServiceInterface {
  // Orders
  getOrders(filters?: OrderFilters): Promise<OrdersListResponse>
  getOrder(id: string): Promise<OrderResponse>
  getOrderCards(orderId: string): Promise<CardResponse[]>
  createOrder(order: Partial<Order>): Promise<OrderResponse>
  updateOrder(id: string, order: Partial<Order>): Promise<OrderResponse>
  deleteOrder(id: string): Promise<void>

  // Employees
  getEmployees(filters?: EmployeeFilters): Promise<EmployeeResponse[]>
  getEmployee(id: string): Promise<EmployeeResponse>
  getEmployeePlanning(date?: string): Promise<EmployeePlanningResponse>
  getEmployeeDetails(id: string, date?: string): Promise<EmployeeDetail>
  createEmployee(employee: Partial<Employee>): Promise<EmployeeResponse>
  updateEmployee(id: string, employee: Partial<Employee>): Promise<EmployeeResponse>
  deleteEmployee(id: string): Promise<void>

  // Planning
  getPlannings(filters?: PlanningFilters): Promise<PlanningResponse[]>
  executePlanning(day: number, month: number, year: number): Promise<ApiResponse<any>>
  quickTest(): Promise<SystemStatus>
  getDiagnostic(): Promise<SystemStatus>

  // Dashboard
  getDashboardStats(): Promise<DashboardStats>
  getSystemStatus(): Promise<SystemStatus>
}

// ========== ERROR TYPES ==========

export interface ApiError {
  code: string
  message: string
  details?: Record<string, any>
  timestamp: string
}

export class OrderNotFoundError extends Error {
  constructor(orderId: string) {
    super(`Order with ID ${orderId} not found`)
    this.name = 'OrderNotFoundError'
  }
}

export class EmployeeNotFoundError extends Error {
  constructor(employeeId: string) {
    super(`Employee with ID ${employeeId} not found`)
    this.name = 'EmployeeNotFoundError'
  }
}

export class PlanningExecutionError extends Error {
  constructor(message: string) {
    super(`Planning execution failed: ${message}`)
    this.name = 'PlanningExecutionError'
  }
}
