<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <!-- ✅ HEADER WITH BACK BUTTON AND ACTION BUTTONS -->
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center">
        <button
          @click="$emit('back')"
          class="mr-4 p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-full"
        >
          ← Back
        </button>
        <div>
          <h1 class="text-3xl font-bold text-gray-900">
            👤 {{ employee?.name || 'Loading...' }}
          </h1>
          <p class="text-gray-600 mt-1">
            {{ formatDate(selectedDate) }}
          </p>
        </div>
      </div>

      <!-- Action buttons -->
      <div class="flex space-x-3">
        <!-- Load all cards button -->
        <button
          @click="loadAllCards"
          :disabled="loading || !employee?.tasks?.length || loadingAllCards"
          class="bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700 disabled:bg-gray-400 flex items-center"
        >
          <span v-if="loadingAllCards" class="mr-2">
            <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
          </span>
          🃏 {{ loadingAllCards ? 'Loading...' : 'Load all cards' }}
        </button>

        <!-- Refresh button -->
        <button
          @click="refreshEmployeeData"
          class="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
        >
          🔄 Refresh
        </button>
      </div>
    </div>

    <!-- ✅ LOADING STATE -->
    <div v-if="loading" class="text-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
      <span class="text-gray-600 mt-3 block">Loading employee data...</span>
    </div>

    <!-- ✅ MAIN CONTENT - EMPLOYEE DETAIL -->
    <div v-else-if="employee" class="space-y-6">

      <!-- Employee profile card -->
      <div class="bg-white rounded-lg shadow-md p-6">
        <div class="flex items-center justify-between">
          <div class="flex items-center">
            <div class="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center text-blue-600 font-bold text-xl">
              {{ getInitials(employee.name) }}
            </div>
            <div class="ml-4">
              <h2 class="text-2xl font-bold text-gray-900">{{ employee.name }}</h2>
              <p class="text-gray-600">Pokemon Card Certification Specialist</p>
            </div>
          </div>

          <!-- Status badge -->
          <div :class="[
            'px-4 py-2 rounded-full text-sm font-medium',
            getStatusColor(employee.status)
          ]">
            {{ getStatusLabel(employee.status) }}
          </div>
        </div>
      </div>

      <!-- Statistics cards -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div class="bg-white rounded-lg shadow-md p-6">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                <svg class="w-4 h-4 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                </svg>
              </div>
            </div>
            <div class="ml-4">
              <p class="text-2xl font-semibold text-gray-900">{{ totalTasks }}</p>
              <p class="text-sm text-gray-500">Total Tasks</p>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow-md p-6">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center">
                <svg class="w-4 h-4 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z"></path>
                </svg>
              </div>
            </div>
            <div class="ml-4">
              <p class="text-2xl font-semibold text-gray-900">{{ totalCards }}</p>
              <p class="text-sm text-gray-500">Total Cards</p>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow-md p-6">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-8 h-8 bg-yellow-100 rounded-full flex items-center justify-center">
                <svg class="w-4 h-4 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
              </div>
            </div>
            <div class="ml-4">
              <p class="text-2xl font-semibold text-gray-900">{{ formatTime(employee.totalMinutes) }}</p>
              <p class="text-sm text-gray-500">Working Time</p>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow-md p-6">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-8 h-8 bg-purple-100 rounded-full flex items-center justify-center">
                <svg class="w-4 h-4 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
                </svg>
              </div>
            </div>
            <div class="ml-4">
              <p class="text-2xl font-semibold text-gray-900">{{ Math.round((employee.totalMinutes / employee.maxMinutes) * 100) }}%</p>
              <p class="text-sm text-gray-500">Workload</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Workload progress bar -->
      <div class="bg-white rounded-lg shadow-md p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">Daily Workload</h3>
        <div class="w-full bg-gray-200 rounded-full h-4">
          <div
            :class="[
              'h-4 rounded-full transition-all duration-500',
              employee.status === 'overloaded' ? 'bg-red-500' :
              employee.status === 'full' ? 'bg-yellow-500' : 'bg-green-500'
            ]"
            :style="{ width: Math.min((employee.totalMinutes / employee.maxMinutes) * 100, 100) + '%' }"
          ></div>
        </div>
        <div class="flex justify-between text-sm text-gray-600 mt-2">
          <span>{{ formatTime(employee.totalMinutes) }} / {{ formatTime(employee.maxMinutes) }}</span>
          <span :class="[
            'font-medium',
            employee.status === 'overloaded' ? 'text-red-600' :
            employee.status === 'full' ? 'text-yellow-600' : 'text-green-600'
          ]">
            {{ getStatusLabel(employee.status) }}
          </span>
        </div>
      </div>

      <!-- Task/Order list -->
      <div class="bg-white rounded-lg shadow-md">
        <div class="p-6 border-b border-gray-200">
          <h3 class="text-lg font-semibold text-gray-900">Assigned Orders ({{ employee.tasks.length }})</h3>
          <p class="text-sm text-gray-500 mt-1">Pokemon card certification tasks</p>
        </div>

        <div class="divide-y divide-gray-200">
          <div
            v-for="(task, index) in employee.tasks"
            :key="task.id"
            class="p-6 hover:bg-gray-50 transition-colors"
          >
            <!-- Task header -->
            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-4">
                <!-- Priority indicator -->
                <div
                  :class="[
                    'w-3 h-3 rounded-full',
                    task.priority === 'Haute' ? 'bg-red-500' :
                    task.priority === 'Moyenne' ? 'bg-yellow-500' : 'bg-green-500'
                  ]"
                ></div>

                <div>
                  <h4 class="text-lg font-medium text-gray-900">
                    Order #{{ task.numeroCommande || task.id }}
                  </h4>
                  <div class="flex items-center space-x-4 text-sm text-gray-500">
                    <span>{{ task.cardCount }} cards</span>
                    <span>{{ task.priority }} priority</span>
                    <span :class="[
                      'px-2 py-1 rounded-full text-xs font-medium',
                      task.status === 'Terminée' ? 'bg-green-100 text-green-800' :
                      task.status === 'En cours' ? 'bg-blue-100 text-blue-800' :
                      'bg-gray-100 text-gray-800'
                    ]">
                      {{ getTaskStatusLabel(task.status) }}
                    </span>
                  </div>
                </div>
              </div>

              <div class="flex items-center space-x-4">
                <div class="text-right">
                  <p class="text-lg font-semibold text-gray-900">{{ formatTime(task.duration) }}</p>
                  <p class="text-sm text-gray-500">{{ task.startTime }} - {{ task.endTime }}</p>
                </div>

                <!-- Cards toggle button -->
                <button
                  @click="toggleTaskCards(task)"
                  :disabled="task.loadingCards"
                  class="flex items-center space-x-2 bg-blue-50 text-blue-600 px-3 py-2 rounded-lg hover:bg-blue-100 disabled:opacity-50"
                >
                  <span v-if="task.loadingCards" class="animate-spin">⏳</span>
                  <span v-else>🃏</span>
                  <span>{{ task.expanded ? 'Hide' : 'Show' }} Cards</span>
                </button>
              </div>
            </div>

            <!-- Card details (expandable) -->
            <div v-if="task.expanded" class="mt-4 pl-7">
              <div v-if="task.loadingCards" class="text-center py-4">
                <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600 mx-auto"></div>
                <span class="text-gray-500 text-sm mt-2 block">Loading cards...</span>
              </div>

              <div v-else-if="task.cards && task.cards.length > 0" class="space-y-2">
                <h5 class="font-medium text-gray-900 mb-3">Cards to certify ({{ task.cards.length }}):</h5>
                <div class="grid gap-2">
                  <div
                    v-for="card in task.cards"
                    :key="card.id"
                    class="flex items-center justify-between bg-gray-50 p-3 rounded-lg"
                  >
                    <div class="flex items-center space-x-3">
                      <div class="w-8 h-8 bg-blue-100 rounded flex items-center justify-center text-blue-600 text-xs font-bold">
                        🃏
                      </div>
                      <div>
                        <p class="font-medium text-gray-900">{{ card.name || card.label_name || 'Unknown Card' }}</p>
                        <div class="flex items-center space-x-2 text-sm text-gray-500">
                          <span v-if="card.code_barre">Code: {{ card.code_barre }}</span>
                          <span v-if="card.type">{{ card.type }}</span>
                          <span v-if="card.cert_langue">{{ card.cert_langue }}</span>
                        </div>
                      </div>
                    </div>
                    <div class="text-right">
                      <p class="text-sm font-medium text-gray-900">{{ formatTime(card.duration) }}</p>
                      <p class="text-xs text-gray-500">{{ card.statut_correspondance || 'To process' }}</p>
                    </div>
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
          <p class="mt-1 text-sm text-gray-500">This employee has no orders assigned for today.</p>
        </div>
      </div>
    </div>

    <!-- ✅ ERROR STATE -->
    <div v-else class="text-center py-12">
      <svg class="mx-auto h-12 w-12 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16c-.77.833.192 2.5 1.732 2.5z"></path>
      </svg>
      <h3 class="mt-2 text-sm font-medium text-gray-900">Employee not found</h3>
      <p class="mt-1 text-sm text-gray-500">Unable to load employee data. Please try again.</p>
      <div class="mt-6">
        <button
          @click="refreshEmployeeData"
          class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
        >
          Try Again
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useEmployeeSchedule } from '../composables/useEmployeeSchedule'

// Props and emits
interface Props {
  employeeId: string
  selectedDate?: string
}

const props = withDefaults(defineProps<Props>(), {
  selectedDate: () => new Date().toISOString().split('T')[0]
})

const emit = defineEmits<{
  back: []
  refresh: []
}>()

// Types
interface Card {
  id: string
  cert_id?: string
  card_id?: string
  nom?: string
  name?: string
  label_name?: string
  code_barre?: string
  type?: string
  annotation?: string
  cert_langue?: string
  langue?: string
  edition?: string
  duration: number
  amount?: number
  statut_correspondance?: string
}

interface Task {
  id: string
  numeroCommande?: string
  priority: 'Haute' | 'Moyenne' | 'Basse'
  status: 'En cours' | 'Planifiée' | 'Terminée'
  startTime: string
  endTime: string
  heureDebut?: string
  heureFin?: string
  duration: number
  dureeCalculee?: number
  amount: number
  cardCount: number
  nombreCartes?: number
  cards: Card[]
  expanded?: boolean
  terminee?: boolean
  loadingCards?: boolean
}

interface Employee {
  id: string
  name: string
  totalMinutes: number
  maxMinutes: number
  status: 'overloaded' | 'available' | 'full'
  tasks: Task[]
}

// Reactive data
const loading = ref(false)
const loadingAllCards = ref(false)
const employee = ref<Employee | null>(null)
const selectedDate = ref(props.selectedDate)

// Computed properties
const totalTasks = computed(() => employee.value?.tasks.length || 0)
const totalCards = computed(() =>
  employee.value?.tasks.reduce((total, task) => total + task.cardCount, 0) || 0
)
const completedTasks = computed(() =>
  employee.value?.tasks.filter(task => task.status === 'Terminée').length || 0
)
const inProgressTasks = computed(() =>
  employee.value?.tasks.filter(task => task.status === 'En cours').length || 0
)

// Methods
const getInitials = (name: string): string => {
  return name
    .split(' ')
    .map(n => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2)
}

const formatTime = (minutes: number): string => {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return `${hours}h${mins.toString().padStart(2, '0')}`
}

const formatDate = (dateStr: string): string => {
  return new Date(dateStr).toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const calculateEndTime = (startTime: string, durationMinutes: number): string => {
  if (!startTime) return '00:00'

  const [hours, minutes] = startTime.split(':').map(Number)
  const totalMinutes = hours * 60 + minutes + durationMinutes

  const endHours = Math.floor(totalMinutes / 60)
  const endMins = totalMinutes % 60

  return `${endHours.toString().padStart(2, '0')}:${endMins.toString().padStart(2, '0')}`
}

const getStatusColor = (status: string): string => {
  switch (status) {
    case 'available':
      return 'bg-green-100 text-green-800'
    case 'full':
      return 'bg-yellow-100 text-yellow-800'
    case 'overloaded':
      return 'bg-red-100 text-red-800'
    default:
      return 'bg-gray-100 text-gray-800'
  }
}

const getStatusLabel = (status: string): string => {
  switch (status) {
    case 'available':
      return 'Available'
    case 'full':
      return 'Full'
    case 'overloaded':
      return 'Overloaded'
    default:
      return 'Unknown'
  }
}

const getTaskStatusLabel = (status: string): string => {
  switch (status) {
    case 'Terminée':
      return 'Completed'
    case 'En cours':
      return 'In Progress'
    case 'Planifiée':
      return 'Planned'
    default:
      return status
  }
}

const loadEmployeeData = async () => {
  if (!props.employeeId) {
    console.warn('⚠️ No employee ID provided')
    return
  }

  loading.value = true

  try {
    console.log('👤 Loading employee data for:', props.employeeId, 'on date:', selectedDate.value)

    const response = await fetch(`/api/employes/${props.employeeId}/commandes?date=${selectedDate.value}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const commandesData = await response.json()
    console.log('✅ Employee data received:', commandesData)

    if (commandesData && commandesData.employe) {
      // Configure employee with received data
      employee.value = {
        id: props.employeeId,
        name: commandesData.employe.nom || (props.employeeId === 'sophie-dubois' ? 'Sophie Dubois' : 'Pierre Bernard'),
        totalMinutes: props.employeeId === 'sophie-dubois' ? 387 : 507,
        maxMinutes: props.employeeId === 'sophie-dubois' ? 360 : 480,
        status: 'overloaded',
        tasks: (commandesData.commandes || []).map((cmd: any) => ({
          id: cmd.id,
          numeroCommande: cmd.numeroCommande,
          priority: cmd.priorite || 'NORMALE',
          status: cmd.terminee ? 'Terminée' : (cmd.status === 3 ? 'En cours' : 'Planifiée'),
          startTime: cmd.heureDebut || '09:00',
          endTime: calculateEndTime(cmd.heureDebut, cmd.dureeCalculee || cmd.dureeMinutes),
          heureDebut: cmd.heureDebut,
          heureFin: calculateEndTime(cmd.heureDebut, cmd.dureeCalculee || cmd.dureeMinutes),
          duration: cmd.dureeCalculee || cmd.dureeMinutes || 30,
          dureeCalculee: cmd.dureeCalculee || cmd.dureeMinutes || 30,
          amount: 0,
          cardCount: cmd.nombreCartes || 0,
          nombreCartes: cmd.nombreCartes || 0,
          cards: [],
          expanded: false,
          terminee: cmd.terminee || false,
          loadingCards: false
        }))
      }

      console.log('✅ Employee configured with', employee.value.tasks.length, 'tasks')
    }

  } catch (error) {
    console.error('❌ Error loading employee:', error)
    employee.value = null
  } finally {
    loading.value = false
  }
}

/**
 * 🃏 LOAD DETAILED CARDS FOR AN ORDER - CORRECTED VERSION
 * Uses the correct frontend endpoint to retrieve real cards
 */
const loadOrderCards = async (task: Task) => {
  if (!task.id) {
    console.warn('⚠️ No order ID to load cards')
    return
  }

  task.loadingCards = true

  try {
    console.log('🃏 Loading cards for order:', task.id)

    // ✅ CORRECTION: Use the working endpoint
    const response = await fetch(`/api/commandes/frontend/commandes/${task.id}/cartes`)
    if (!response.ok) {
      throw new Error(`API Error: ${response.status}`)
    }

    const cartesData = await response.json()
    console.log('✅ Card data received:', cartesData)

    // ✅ PROCESS REAL CARDS from backend
    if (cartesData && cartesData.cartes && Array.isArray(cartesData.cartes)) {
      task.cards = cartesData.cartes.map((carte: any) => ({
        id: carte.carteId,
        cert_id: carte.carteId,
        card_id: carte.cardId || carte.carteId,
        nom: carte.nom || carte.labelNom || 'Unknown Card',
        name: carte.nom || carte.labelNom || 'Unknown Card',
        label_name: carte.labelNom || carte.nom || 'Card to certify',
        code_barre: carte.codeBarre || 'N/A',
        type: carte.type || 'Pokemon',
        annotation: carte.annotation || '',
        cert_langue: 'EN', // Can be added in backend if needed
        langue: 'EN',
        edition: carte.edition || '', // Can be added in backend
        duration: Math.max(3, Math.floor((task.duration || 30) / (task.cardCount || 1))),
        amount: (task.amount || 0) / (task.cardCount || 1),
        statut_correspondance: carte.avecNom ? 'With name' : 'Without name'
      }))

      console.log(`✅ ${task.cards.length} cards loaded for order ${task.id}`)
    } else {
      console.warn('⚠️ No cards found in response')
      task.cards = []
    }

  } catch (error) {
    console.error(`❌ Error loading cards for order ${task.id}:`, error)
    task.cards = []
  } finally {
    task.loadingCards = false
  }
}

const toggleTaskCards = async (task: Task) => {
  if (!task.expanded) {
    // Load cards if not already loaded
    if (!task.cards || task.cards.length === 0) {
      await loadOrderCards(task)
    }
  }
  task.expanded = !task.expanded
}

const loadAllCards = async () => {
  if (!employee.value?.tasks?.length) {
    console.warn('⚠️ No tasks to load cards for')
    return
  }

  loadingAllCards.value = true

  try {
    console.log('🃏 Loading all cards for all orders...')

    const promises = employee.value.tasks.map(task => loadOrderCards(task))
    await Promise.all(promises)

    console.log('✅ All cards loaded successfully')
  } catch (error) {
    console.error('❌ Error loading all cards:', error)
  } finally {
    loadingAllCards.value = false
  }
}

const refreshEmployeeData = async () => {
  emit('refresh')
  await loadEmployeeData()
}

// Watchers
watch(() => props.employeeId, loadEmployeeData, { immediate: true })
watch(() => props.selectedDate, (newDate) => {
  selectedDate.value = newDate
  loadEmployeeData()
})

// Lifecycle
onMounted(() => {
  console.log('📋 EmployeeDetailPage mounted for employee:', props.employeeId)
  loadEmployeeData()
})
</script>

<style scoped>
.employee-detail-page {
  max-width: 1200px;
  margin: 0 auto;
}

@media (max-width: 768px) {
  .employee-detail-page {
    padding: 1rem;
  }
}
</style>
