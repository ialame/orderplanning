<template>
  <div class="employee-detail-view">
    <!-- Back Button -->
    <div class="mb-6">
      <button
        @click="goBack"
        class="flex items-center gap-2 text-blue-600 hover:text-blue-800 transition-colors"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path>
        </svg>
        Back to Employee List
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
      <p class="text-gray-600">Loading employee details...</p>
    </div>

    <!-- Employee Found -->
    <div v-else-if="employee" class="space-y-6">
      <!-- Employee Header -->
      <div class="bg-white rounded-lg shadow-lg p-6 border-l-4 border-blue-500">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-4">
            <div class="w-16 h-16 bg-gradient-to-r from-blue-500 to-purple-600 rounded-full flex items-center justify-center text-white font-bold text-xl">
              {{ getInitials(employee.name) }}
            </div>
            <div>
              <h1 class="text-3xl font-bold text-gray-900">{{ employee.name }}</h1>
              <p class="text-gray-600">Employee Details & Assigned Orders</p>
              <div class="flex items-center mt-2 space-x-4 text-sm">
                <span class="text-blue-600">📧 {{ employee.email }}</span>
                <span class="text-green-600">⏰ {{ employee.workHoursPerDay }}h/day</span>
              </div>
            </div>
          </div>
          <div :class="[
            'px-4 py-2 rounded-full text-sm font-medium',
            employee.available ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
          ]">
            {{ employee.available ? '✅ Available' : '❌ Unavailable' }}
          </div>
        </div>
      </div>

      <!-- Quick Stats -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div class="bg-white p-6 rounded-lg shadow border-l-4 border-blue-500">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600">Total Tasks</p>
              <p class="text-2xl font-bold text-blue-600">{{ employee.tasks.length }}</p>
            </div>
            <div class="text-3xl text-blue-600">📋</div>
          </div>
        </div>

        <div class="bg-white p-6 rounded-lg shadow border-l-4 border-green-500">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600">Total Cards</p>
              <p class="text-2xl font-bold text-green-600">{{ totalCards }}</p>
            </div>
            <div class="text-3xl text-green-600">🃏</div>
          </div>
        </div>

        <div class="bg-white p-6 rounded-lg shadow border-l-4 border-yellow-500">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600">Total Time</p>
              <p class="text-2xl font-bold text-yellow-600">{{ formatTime(totalMinutes) }}</p>
            </div>
            <div class="text-3xl text-yellow-600">⏱️</div>
          </div>
        </div>

        <div class="bg-white p-6 rounded-lg shadow border-l-4 border-purple-500">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600">Workload</p>
              <p class="text-2xl font-bold text-purple-600">{{ workloadPercentage }}%</p>
            </div>
            <div class="text-3xl text-purple-600">📊</div>
          </div>
        </div>
      </div>

      <!-- Date Selector -->
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center justify-between">
          <h2 class="text-xl font-semibold text-gray-900">📅 Planning for</h2>
          <input
            type="date"
            v-model="selectedDate"
            @change="loadEmployeeDetails"
            class="border border-gray-300 rounded-lng px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
        </div>
      </div>

      <!-- Assigned Orders -->
      <div class="bg-white rounded-lg shadow overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-200">
          <h2 class="text-xl font-semibold text-gray-900">🎯 Assigned Orders ({{ employee.tasks.length }})</h2>
          <p class="text-gray-600 mt-1">Orders assigned to {{ employee.name }} for {{ formatDisplayDate(selectedDate) }}</p>
        </div>

        <!-- Orders List -->
        <div v-if="employee.tasks.length > 0" class="divide-y divide-gray-200">
          <div
            v-for="task in employee.tasks"
            :key="task.id"
            class="p-6 hover:bg-gray-50 transition-colors"
          >
            <div class="flex items-center justify-between">
              <div class="flex-1">
                <div class="flex items-center space-x-3">
                  <div :class="[
                    'w-3 h-3 rounded-full',
                    getPriorityColor(task.priority)
                  ]"></div>
                  <h3 class="text-lg font-semibold text-gray-900">Order {{ task.orderNumber }}</h3>
                  <span :class="[
                    'inline-flex px-2 py-1 text-xs font-semibold rounded-full',
                    getPriorityBadgeColor(task.priority)
                  ]">
                    {{ task.priority }}
                  </span>
                  <span :class="[
                    'inline-flex px-2 py-1 text-xs font-semibold rounded-full',
                    getStatusBadgeColor(task.status)
                  ]">
                    {{ task.status }}
                  </span>
                </div>

                <div class="mt-2 grid grid-cols-1 md:grid-cols-4 gap-4 text-sm text-gray-600">
                  <div>
                    <span class="font-medium">📅 Date:</span>
                    <span class="ml-1">{{ formatDisplayDate(task.planningDate) }}</span>
                  </div>
                  <div>
                    <span class="font-medium">⏰ Time:</span>
                    <span class="ml-1">{{ task.startTime }} - {{ task.endTime }}</span>
                  </div>
                  <div>
                    <span class="font-medium">🃏 Cards:</span>
                    <span class="ml-1">{{ task.cardCount }} cards</span>
                  </div>
                  <div>
                    <span class="font-medium">⏱️ Duration:</span>
                    <span class="ml-1">{{ formatTime(task.durationMinutes) }}</span>
                  </div>
                </div>
              </div>

              <div class="flex space-x-2">
                <button
                  @click="toggleOrderDetails(task)"
                  class="bg-blue-600 text-white px-3 py-1 rounded text-sm hover:bg-blue-700 transition-colors"
                >
                  {{ task.expanded ? 'Hide Cards' : 'View Cards' }}
                </button>
                <button
                  @click="updateOrderStatus(task)"
                  :class="[
                    'px-3 py-1 rounded text-sm transition-colors',
                    task.status === 'COMPLETED'
                      ? 'bg-gray-600 text-white hover:bg-gray-700'
                      : 'bg-green-600 text-white hover:bg-green-700'
                  ]"
                >
                  {{ task.status === 'COMPLETED' ? 'Reopen' : 'Complete' }}
                </button>
              </div>
            </div>

            <!-- Expanded Card Details -->
            <div v-if="task.expanded" class="mt-4 bg-gray-50 rounded-lg p-4">
              <h4 class="font-medium text-gray-900 mb-3">🃏 Cards in this order:</h4>

              <div v-if="task.loadingCards" class="text-center py-4">
                <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600 mx-auto mb-2"></div>
                <p class="text-sm text-gray-600">Loading cards...</p>
              </div>

              <div v-else-if="task.cards.length > 0" class="space-y-2">
                <div
                  v-for="card in task.cards"
                  :key="card.id"
                  class="flex items-center justify-between p-3 bg-white rounded border"
                >
                  <div class="flex-1">
                    <p class="font-medium text-gray-900">{{ card.name || card.label_name || 'Unknown Card' }}</p>
                    <div class="flex items-center space-x-2 text-sm text-gray-500">
                      <span v-if="card.code_barre">Code: {{ card.code_barre }}</span>
                      <span v-if="card.type">{{ card.type }}</span>
                      <span v-if="card.cert_langue">{{ card.cert_langue }}</span>
                    </div>
                  </div>
                  <div class="text-right">
                    <p class="text-sm font-medium text-gray-900">{{ formatTime(card.duration) }}</p>
                    <p class="text-xs text-gray-500">{{ card.statut_correspondance || 'To process' }}</p>
                  </div>
                </div>
              </div>

              <div v-else class="text-center py-4 text-gray-500">
                <p>No cards loaded yet</p>
                <button
                  @click="loadOrderCards(task)"
                  class="mt-2 text-blue-600 hover:text-blue-800 text-sm"
                >
                  Load cards for this order
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty state for tasks -->
        <div v-if="employee.tasks.length === 0" class="p-12 text-center">
          <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
          </svg>
          <h3 class="mt-2 text-sm font-medium text-gray-900">No orders assigned</h3>
          <p class="mt-1 text-sm text-gray-500">This employee has no orders assigned for the selected date.</p>
        </div>
      </div>
    </div>

    <!-- Error State -->
    <div v-else class="text-center py-12">
      <svg class="mx-auto h-12 w-12 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16c-.77.833.192 2.5 1.732 2.5z"></path>
      </svg>
      <h3 class="mt-2 text-sm font-medium text-gray-900">Employee not found</h3>
      <p class="mt-1 text-sm text-gray-500">Unable to load employee data. Please try again.</p>
      <button
        @click="loadEmployeeDetails"
        class="mt-4 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
      >
        Retry
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

// ========== INTERFACES ==========
interface Card {
  id: string
  translatable_id?: string
  name?: string
  label_name?: string
  code_barre?: string
  type?: string
  cert_langue?: string
  duration: number
  amount?: number
  statut_correspondance?: string
}

interface Task {
  id: string
  orderId: string
  orderNumber: string
  priority: 'URGENT' | 'HIGH' | 'MEDIUM' | 'LOW'
  status: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED'
  planningDate: string
  startTime: string
  endTime?: string
  durationMinutes: number
  cardCount: number
  cards: Card[]
  expanded?: boolean
  loadingCards?: boolean
}

interface Employee {
  id: string
  name: string
  email: string
  workHoursPerDay: number
  available: boolean
  active: boolean
  tasks: Task[]
}

// ========== ROUTER & ROUTE ==========
const route = useRoute()
const router = useRouter()

// ========== STATE ==========
const loading = ref(false)
const employee = ref<Employee | null>(null)
const selectedDate = ref(new Date().toISOString().split('T')[0])

// ========== COMPUTED ==========
const totalCards = computed(() =>
  employee.value?.tasks.reduce((total, task) => total + task.cardCount, 0) || 0
)

const totalMinutes = computed(() =>
  employee.value?.tasks.reduce((total, task) => total + task.durationMinutes, 0) || 0
)

const workloadPercentage = computed(() => {
  if (!employee.value) return 0
  const dailyMinutes = employee.value.workHoursPerDay * 60
  return Math.round((totalMinutes.value / dailyMinutes) * 100)
})

// ========== METHODS ==========

const goBack = () => {
  router.push('/employees')
}

const getInitials = (name: string): string => {
  if (!name) return 'XX'
  return name.split(' ').map(part => part.charAt(0)).join('').toUpperCase().slice(0, 2)
}

const formatTime = (minutes: number): string => {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return hours > 0 ? `${hours}h ${mins}m` : `${mins}m`
}

const formatDisplayDate = (dateStr: string): string => {
  try {
    return new Date(dateStr).toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    })
  } catch {
    return dateStr
  }
}

const getPriorityColor = (priority: string): string => {
  switch (priority) {
    case 'URGENT': return 'bg-red-500'
    case 'HIGH': return 'bg-orange-500'
    case 'MEDIUM': return 'bg-yellow-500'
    case 'LOW': return 'bg-green-500'
    default: return 'bg-gray-500'
  }
}

const getPriorityBadgeColor = (priority: string): string => {
  switch (priority) {
    case 'URGENT': return 'bg-red-100 text-red-800'
    case 'HIGH': return 'bg-orange-100 text-orange-800'
    case 'MEDIUM': return 'bg-yellow-100 text-yellow-800'
    case 'LOW': return 'bg-green-100 text-green-800'
    default: return 'bg-gray-100 text-gray-800'
  }
}

const getStatusBadgeColor = (status: string): string => {
  switch (status) {
    case 'COMPLETED': return 'bg-green-100 text-green-800'
    case 'IN_PROGRESS': return 'bg-blue-100 text-blue-800'
    case 'SCHEDULED': return 'bg-gray-100 text-gray-800'
    default: return 'bg-gray-100 text-gray-800'
  }
}

const loadEmployeeDetails = async () => {
  const employeeId = route.params.id as string
  if (!employeeId) {
    console.error('No employee ID provided')
    return
  }

  loading.value = true

  try {
    console.log(`👤 Loading details for employee: ${employeeId}`)

    // Load employee basic info
    const employeeResponse = await fetch(`/api/employees/${employeeId}`)
    if (!employeeResponse.ok) {
      throw new Error(`Failed to load employee: ${employeeResponse.status}`)
    }

    const employeeData = await employeeResponse.json()

    // Load employee's assigned orders/plannings
    const planningsResponse = await fetch(`/api/planning/employee/${employeeId}?date=${selectedDate.value}`)
    let planningsData = []

    if (planningsResponse.ok) {
      planningsData = await planningsResponse.json()
      console.log(`📋 Found ${planningsData.length} plannings for employee`)
    } else {
      console.warn('Could not load plannings, trying alternative endpoint')

      // Try alternative endpoint
      try {
        const altResponse = await fetch(`/api/planning/view-simple?date=${selectedDate.value}`)
        if (altResponse.ok) {
          const allPlannings = await altResponse.json()
          planningsData = allPlannings.filter((p: any) => p.employeeId === employeeId)
          console.log(`📋 Found ${planningsData.length} plannings (filtered from all)`)
        }
      } catch (error) {
        console.warn('Alternative endpoint also failed:', error)
      }
    }

    // Transform plannings to tasks
    const transformedTasks: Task[] = planningsData.map((planning: any) => ({
      id: planning.id,
      orderId: planning.orderId || planning.order_id,
      orderNumber: planning.orderNumber || planning.order_number || `ORD-${planning.orderId?.slice(-6)}`,
      priority: mapPriority(planning.priority),
      status: mapStatus(planning.status),
      planningDate: planning.planningDate || planning.planning_date,
      startTime: planning.startTime || planning.start_time || '09:00',
      endTime: calculateEndTime(planning.startTime || '09:00', planning.durationMinutes || 60),
      durationMinutes: planning.durationMinutes || planning.duration_minutes || 60,
      cardCount: planning.cardCount || planning.card_count || 1,
      cards: [],
      expanded: false,
      loadingCards: false
    }))

    // Set employee data
    employee.value = {
      id: employeeData.id,
      name: employeeData.fullName || `${employeeData.firstName} ${employeeData.lastName}`,
      email: employeeData.email,
      workHoursPerDay: employeeData.workHoursPerDay || 8,
      available: employeeData.available !== false,
      active: employeeData.active !== false,
      tasks: transformedTasks
    }

    console.log(`✅ Employee details loaded: ${employee.value.name} with ${employee.value.tasks.length} tasks`)

  } catch (error) {
    console.error('❌ Error loading employee details:', error)
    employee.value = null
  } finally {
    loading.value = false
  }
}

const toggleOrderDetails = async (task: Task) => {
  task.expanded = !task.expanded

  if (task.expanded && task.cards.length === 0) {
    await loadOrderCards(task)
  }
}

const loadOrderCards = async (task: Task) => {
  task.loadingCards = true

  try {
    const response = await fetch(`/api/orders/${task.orderId}/cards`)

    if (response.ok) {
      const cardsData = await response.json()
      task.cards = Array.isArray(cardsData.cards) ? cardsData.cards.map((card: any) => ({
        id: card.id,
        name: card.name,
        label_name: card.label_name,
        code_barre: card.barcode || card.code_barre,
        type: card.type,
        cert_langue: card.language || card.cert_langue,
        duration: Math.floor(task.durationMinutes / task.cardCount),
        statut_correspondance: card.status || 'TO_PROCESS'
      })) : []

      console.log(`🃏 Loaded ${task.cards.length} cards for order ${task.orderNumber}`)
    } else {
      // Create fallback card data
      task.cards = Array.from({ length: task.cardCount }, (_, index) => ({
        id: `card-${task.id}-${index}`,
        name: `Card ${index + 1}`,
        label_name: `Pokemon Card ${index + 1}`,
        code_barre: `CODE-${index + 1}`,
        type: 'Pokemon',
        cert_langue: 'EN',
        duration: Math.floor(task.durationMinutes / task.cardCount),
        statut_correspondance: 'TO_PROCESS'
      }))

      console.warn(`⚠️ Could not load cards, created ${task.cards.length} fallback cards`)
    }

  } catch (error) {
    console.error('❌ Error loading order cards:', error)
    task.cards = []
  } finally {
    task.loadingCards = false
  }
}

const updateOrderStatus = (task: Task) => {
  const newStatus = task.status === 'COMPLETED' ? 'IN_PROGRESS' : 'COMPLETED'
  task.status = newStatus

  console.log(`✅ Updated order ${task.orderNumber} status to ${newStatus}`)
  // Here you could make an API call to persist the status change
}

// ========== UTILITY FUNCTIONS ==========

const mapPriority = (priority: any): 'URGENT' | 'HIGH' | 'MEDIUM' | 'LOW' => {
  if (!priority) return 'MEDIUM'
  const p = String(priority).toUpperCase()
  if (p.includes('URGENT') || p === 'X') return 'URGENT'
  if (p.includes('HIGH') || p === 'F+' || p === 'F') return 'HIGH'
  if (p.includes('LOW') || p === 'C') return 'LOW'
  return 'MEDIUM'
}

const mapStatus = (status: any): 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' => {
  if (!status) return 'SCHEDULED'
  const s = String(status).toUpperCase()
  if (s.includes('PROGRESS') || s.includes('COURS')) return 'IN_PROGRESS'
  if (s.includes('COMPLETED') || s.includes('TERMINE')) return 'COMPLETED'
  return 'SCHEDULED'
}

const calculateEndTime = (startTime: string, durationMinutes: number): string => {
  try {
    const [hours, minutes] = startTime.split(':').map(Number)
    const startMinutes = hours * 60 + minutes
    const endMinutes = startMinutes + durationMinutes
    const endHours = Math.floor(endMinutes / 60) % 24
    const endMins = endMinutes % 60
    return `${endHours.toString().padStart(2, '0')}:${endMins.toString().padStart(2, '0')}`
  } catch {
    return '17:00'
  }
}

// ========== LIFECYCLE ==========
onMounted(async () => {
  console.log('👤 EmployeeDetailView mounted')
  await loadEmployeeDetails()
})
</script>

<style scoped>
.employee-detail-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

@media (max-width: 768px) {
  .employee-detail-view {
    padding: 1rem;
  }
}

/* Smooth transitions */
.transition-colors {
  transition-property: color, background-color, border-color;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 150ms;
}
</style>
