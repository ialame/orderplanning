export interface Order {
  id?: string
  orderNumber: string
  cardCount: number
  cardsWithName?: number
  percentageWithName?: number
  totalPrice?: number
  priority: 'URGENT' | 'HIGH' | 'MEDIUM' | 'NORMAL' | 'LOW'
  status?: 'PENDING' | 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED'
  statusCode?: number
  creationDate?: string
  date?: string
  deadline?: string
  estimatedTimeMinutes: number
  cardNames?: string[]
  nameSample?: string
  orderQuality?: 'EXCELLENT' | 'GOOD' | 'AVERAGE'
  cardsWithoutMissingData?: boolean
}

export interface Employee {
  id?: string
  lastName: string
  firstName: string
  email: string
  workHoursPerDay: number
  active: boolean
  creationDate?: string
}

export interface Planning {
  id?: string | number
  orderId?: string
  employeeId?: string
  plannedDate?: string
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
