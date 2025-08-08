<template>
  <div class="order-planning-view p-6">
    <!-- Header -->
    <div class="flex justify-between items-center mb-6">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">🃏 Order Planning System</h1>
        <p class="text-gray-600 mt-1">Automatic distribution of Pokemon card orders to employees</p>
        <div class="flex items-center mt-2 space-x-4 text-sm text-gray-500">
          <span>📊 {{ stats.totalOrders || 0 }} orders analyzed</span>
          <span>👥 {{ stats.employeesUsed || 0 }} employees</span>
          <span>💾 {{ stats.planningsSaved || 0 }} plannings saved</span>
        </div>
      </div>
      <div class="flex gap-3">
        <button
          @click="generatePlanning"
          :disabled="loading"
          class="flex items-center gap-2 bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors shadow-lg"
        >
          <svg v-if="loading" class="animate-spin h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
          </svg>
          <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
          </svg>
          {{ loading ? 'Generating...' : 'Generate Planning' }}
        </button>
        <button
          @click="testBackend"
          class="flex items-center gap-2 bg-gray-600 text-white px-4 py-2 rounded-lg hover:bg-gray-700 transition-colors"
        >
          🔧 Test Backend
        </button>
      </div>
    </div>

    <!-- Configuration Section -->
    <div class="bg-white rounded-lg shadow p-6 mb-6">
      <h2 class="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path>
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
        </svg>
        Planning Configuration
      </h2>

      <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Start Date</label>
          <input
            type="date"
            v-model="config.startDate"
            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">End Date</label>
          <input
            type="date"
            v-model="config.endDate"
            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Number of Employees</label>
          <select
            v-model="config.numberOfEmployees"
            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="1">1 Employee</option>
            <option value="2">2 Employees</option>
            <option value="3">3 Employees</option>
            <option value="4">4 Employees (All)</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Time per Card (minutes)</label>
          <select
            v-model="config.timePerCard"
            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="2">2 minutes</option>
            <option value="3">3 minutes</option>
            <option value="5">5 minutes</option>
            <option value="10">10 minutes</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Statistics -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
      <div class="bg-white rounded-lg shadow p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">📊 System Stats</h3>
        <div class="space-y-3">
          <div class="flex justify-between">
            <span class="text-gray-600">Available Orders:</span>
            <span class="font-semibold">{{ systemStats.availableOrders || 0 }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-600">Active Employees:</span>
            <span class="font-semibold">{{ systemStats.activeEmployees || 0 }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-600">Total Plannings:</span>
            <span class="font-semibold">{{ systemStats.totalPlannings || 0 }}</span>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">⚡ Last Generation</h3>
        <div class="space-y-3">
          <div class="flex justify-between">
            <span class="text-gray-600">Orders Processed:</span>
            <span class="font-semibold">{{ stats.totalOrders || 0 }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-600">Employees Used:</span>
            <span class="font-semibold">{{ stats.employeesUsed || 0 }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-600">Execution Time:</span>
            <span class="font-semibold">{{ stats.executionTimeMs || 0 }}ms</span>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">🔧 Quick Actions</h3>
        <div class="space-y-2">
          <button
            @click="loadSampleData"
            class="w-full text-left px-3 py-2 bg-gray-50 hover:bg-gray-100 rounded-lg text-sm transition-colors"
          >
            🧪 Load Sample Data
          </button>
          <button
            @click="clearPlannings"
            class="w-full text-left px-3 py-2 bg-gray-50 hover:bg-gray-100 rounded-lg text-sm transition-colors"
          >
            🗑️ Clear All Plannings
          </button>
          <button
            @click="exportData"
            class="w-full text-left px-3 py-2 bg-gray-50 hover:bg-gray-100 rounded-lg text-sm transition-colors"
          >
            📤 Export Planning Data
          </button>
        </div>
      </div>
    </div>

    <!-- Generated Plannings -->
    <div v-if="plannings.length > 0" class="bg-white rounded-lg shadow overflow-hidden">
      <div class="px-6 py-4 border-b border-gray-200">
        <h2 class="text-lg font-semibold text-gray-900">📋 Generated Plannings ({{ plannings.length }})</h2>
        <p class="text-gray-600">Recent planning assignments for employees</p>
      </div>

      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Order</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Employee</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Time</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Duration</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Priority</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
          </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="planning in plannings.slice(0, 10)" :key="planning.id" class="hover:bg-gray-50 transition-colors">
            <!-- Order Number -->
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm font-medium text-gray-900">
                {{ planning.orderNumber || extractOrderNumber(planning.notes) || `ORD-${planning.orderId?.slice(-6) || 'XXX'}` }}
              </div>
            </td>

            <!-- Employee Name -->
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm text-gray-900">
                {{ planning.employeeName || `Employee ${planning.employeeId?.slice(-4) || 'XXXX'}` }}
              </div>
              <div class="text-xs text-gray-500">
                ID: {{ planning.employeeId?.slice(-4) || 'XXXX' }}
              </div>
            </td>

            <!-- Planning Date -->
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm text-gray-900">
                {{ formatDate(planning.planningDate) }}
              </div>
            </td>

            <!-- Start Time -->
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm text-gray-900">
                {{ formatTime(planning.startTime) }}
              </div>
            </td>

            <!-- Duration -->
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm text-gray-900">
                {{ planning.durationMinutes || planning.duration_minutes || 60 }}min
              </div>
            </td>

            <!-- Priority -->
            <td class="px-6 py-4 whitespace-nowrap">
                <span :class="[
                  'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
                  planning.priority === 'URGENT' ? 'bg-red-100 text-red-800' :
                  planning.priority === 'HIGH' ? 'bg-orange-100 text-orange-800' :
                  planning.priority === 'MEDIUM' ? 'bg-yellow-100 text-yellow-800' :
                  'bg-green-100 text-green-800'
                ]">
                  {{ planning.priority || 'MEDIUM' }}
                </span>
            </td>

            <!-- Status -->
            <td class="px-6 py-4 whitespace-nowrap">
                <span :class="[
                  'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
                  planning.status === 'COMPLETED' ? 'bg-green-100 text-green-800' :
                  planning.status === 'IN_PROGRESS' ? 'bg-blue-100 text-blue-800' :
                  'bg-gray-100 text-gray-800'
                ]">
                  {{ planning.status || 'SCHEDULED' }}
                </span>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <!-- Show More Button -->
      <div v-if="plannings.length > 10" class="px-6 py-4 border-t border-gray-200 bg-gray-50">
        <button
          class="text-sm text-blue-600 hover:text-blue-800 font-medium"
          @click="showAllPlannings = !showAllPlannings"
        >
          {{ showAllPlannings ? 'Show Less' : `Show All (${plannings.length} total)` }}
        </button>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else-if="!loading" class="text-center py-12 bg-white rounded-lg shadow border">
      <svg class="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V9a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"></path>
      </svg>
      <h3 class="text-lg font-medium text-gray-900 mb-2">No Planning Generated</h3>
      <p class="text-gray-500 mb-6">Click "Generate Planning" to start distributing Pokemon card orders to employees</p>
      <div class="space-y-2">
        <button
          @click="generatePlanning"
          class="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors mr-3"
        >
          Generate Your First Planning
        </button>
        <button
          @click="testBackend"
          class="bg-gray-600 text-white px-6 py-2 rounded-lg hover:bg-gray-700 transition-colors"
        >
          Test Backend Connection
        </button>
      </div>
    </div>

    <!-- Success/Error Messages -->
    <div v-if="message.text" :class="[
      'fixed bottom-4 right-4 p-4 rounded-lg shadow-lg flex items-center space-x-3 max-w-md z-50',
      message.type === 'success' ? 'bg-green-50 border border-green-200 text-green-800' :
      message.type === 'error' ? 'bg-red-50 border border-red-200 text-red-800' :
      message.type === 'warning' ? 'bg-yellow-50 border border-yellow-200 text-yellow-800' :
      'bg-blue-50 border border-blue-200 text-blue-800'
    ]">
      <svg class="w-5 h-5 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
        <path v-if="message.type === 'success'" fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
        <path v-else-if="message.type === 'error'" fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
        <path v-else fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/>
      </svg>
      <div class="flex-1">
        <p class="text-sm font-medium">{{ message.text }}</p>
        <p v-if="message.details" class="text-sm mt-1 opacity-80">{{ message.details }}</p>
      </div>
      <button @click="clearMessage" class="text-gray-400 hover:text-gray-600">
        <svg class="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { dateConfig, systemConfig } from '@/config/appConfig'

// ========== INTERFACES ==========
interface PlanningConfig {
  startDate: string
  endDate: string
  numberOfEmployees: number
  timePerCard: number
}

interface Planning {
  id: string
  orderId: string
  orderNumber?: string
  employeeId: string
  employeeName?: string
  planningDate: string
  startTime: string
  durationMinutes: number
  priority: 'URGENT' | 'HIGH' | 'MEDIUM' | 'LOW'
  status: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED'
  cardCount?: number
  notes?: string
}

interface Stats {
  totalOrders: number
  employeesUsed: number
  planningsSaved: number
  executionTimeMs: number
}

interface SystemStats {
  availableOrders: number
  activeEmployees: number
  totalPlannings: number
}

interface Message {
  text: string
  details?: string
  type: 'success' | 'error' | 'info' | 'warning'
}

// ========== EMITS ==========
const emit = defineEmits(['show-notification'])

// ========== STATE ==========
const loading = ref(false)
const showAllPlannings = ref(false)
const plannings = ref<Planning[]>([])
const message = ref<Message>({ text: '', type: 'info' })

const config = ref<PlanningConfig>({
  // ✅ Utilise VITE_PLANNING_START_DATE au lieu de valeur hardcodée
  startDate: dateConfig.planningStartDate,

  // ✅ Utilise VITE_PLANNING_END_DATE au lieu de valeur hardcodée
  endDate: dateConfig.planningEndDate,

  // ✅ Utilise VITE_DEFAULT_EMPLOYEES au lieu de valeur hardcodée
  numberOfEmployees: systemConfig.defaultEmployees,

  // ✅ Utilise VITE_DEFAULT_TIME_PER_CARD au lieu de valeur hardcodée
  timePerCard: systemConfig.defaultTimePerCard
})

// ========== MÉTHODE POUR DÉBUGGER LA CONFIGURATION ==========
const debugConfig = () => {
  console.log('🔧 Configuration actuelle:')
  console.log('  📅 Date début planification:', config.value.startDate)
  console.log('  📅 Date fin planification:', config.value.endDate)
  console.log('  👥 Nombre d\'employés:', config.value.numberOfEmployees)
  console.log('  ⏱️ Temps par carte:', config.value.timePerCard, 'minutes')

  console.log('🌍 Variables d\'environnement:')
  console.log('  VITE_PLANNING_START_DATE:', import.meta.env.VITE_PLANNING_START_DATE)
  console.log('  VITE_PLANNING_END_DATE:', import.meta.env.VITE_PLANNING_END_DATE)
  console.log('  VITE_DEFAULT_EMPLOYEES:', import.meta.env.VITE_DEFAULT_EMPLOYEES)
  console.log('  VITE_DEFAULT_TIME_PER_CARD:', import.meta.env.VITE_DEFAULT_TIME_PER_CARD)
}

const stats = ref<Stats>({
  totalOrders: 0,
  employeesUsed: 0,
  planningsSaved: 0,
  executionTimeMs: 0
})

const systemStats = ref<SystemStats>({
  availableOrders: 0,
  activeEmployees: 0,
  totalPlannings: 0
})

// ========== METHODS ==========
const generatePlanning = async () => {
  loading.value = true

  try {
    showMessage({
      text: 'Generating planning...',
      details: 'This may take a few moments',
      type: 'info'
    })

    const response = await fetch('/api/planning/generate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(config.value)
    })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    }

    const data = await response.json()

    console.log('🔍 Backend response:', data)

    // Update stats with corrected mapping
    stats.value = {
      totalOrders: data.totalOrders || data.ordersAnalyzed || data.ordersProcessed || 0,
      employeesUsed: data.employeesUsed || data.totalEmployees || data.employeeCount || 0,
      planningsSaved: data.planningsSaved || data.totalPlannings || data.planningsCreated || 0,
      executionTimeMs: data.executionTimeMs || data.executionTime || data.duration || 0
    }

    console.log('📊 Mapped stats:', stats.value)

    showMessage({
      text: 'Planning generated successfully!',
      details: `Processed ${stats.value.totalOrders} orders for ${stats.value.employeesUsed} employees`,
      type: 'success'
    })

    // Load the generated plannings
    await loadPlannings()

    emit('show-notification', {
      message: 'Planning generated successfully',
      details: `${stats.value.totalOrders} orders distributed among ${stats.value.employeesUsed} employees`,
      type: 'success'
    })

  } catch (error) {
    console.error('❌ Error generating planning:', error)
    showMessage({
      text: 'Failed to generate planning',
      details: error instanceof Error ? error.message : 'Unknown error occurred',
      type: 'error'
    })

    emit('show-notification', {
      message: 'Planning generation failed',
      details: error instanceof Error ? error.message : 'Unknown error',
      type: 'error'
    })
  } finally {
    loading.value = false
  }
}

const loadPlannings = async () => {
  try {
    console.log('📋 Loading plannings for date:', config.value.startDate)

    const response = await fetch(`/api/planning/view-simple?date=${config.value.startDate}`)

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }

    const data = await response.json()
    console.log('🔍 Plannings for', config.value.startDate, ':', data.length)

    // Filter to be sure (in case backend returns everything)
    const filteredData = data.filter((p: any) => p.planningDate === config.value.startDate || p.planning_date === config.value.startDate)

    plannings.value = filteredData.map((item: any) => ({
      id: item.id,
      orderId: item.orderId || item.order_id,
      orderNumber: extractOrderNumber(item.notes) || item.orderNumber || `ORD-${item.orderId?.slice(-6)}`,
      employeeId: item.employeeId || item.employee_id,
      employeeName: item.employeeName || item.employee_name || `Employee ${item.employeeId?.slice(-4)}`,
      planningDate: item.planningDate || item.planning_date,
      startTime: item.startTime || item.start_time,
      durationMinutes: item.durationMinutes || item.duration_minutes || 60,
      priority: item.priority || 'MEDIUM',
      status: item.status || 'SCHEDULED',
      cardCount: item.cardCount || item.card_count || 0,
      notes: item.notes
    }))

    console.log(`✅ Loaded ${plannings.value.length} plannings for ${config.value.startDate}`)

  } catch (error) {
    console.error('❌ Error loading plannings:', error)
    plannings.value = []
  }
}

const testBackend = async () => {
  try {
    const response = await fetch('/api/planning/debug-real')
    if (response.ok) {
      showMessage({
        text: 'Backend connection successful',
        type: 'success'
      })
    }
  } catch (error) {
    showMessage({
      text: 'Backend connection failed',
      type: 'error'
    })
  }
}

const loadSampleData = () => {
  showMessage({
    text: 'Sample data feature coming soon',
    type: 'info'
  })
}

const clearPlannings = () => {
  plannings.value = []
  stats.value = { totalOrders: 0, employeesUsed: 0, planningsSaved: 0, executionTimeMs: 0 }
  showMessage({
    text: 'All plannings cleared',
    type: 'info'
  })
}

const exportData = () => {
  showMessage({
    text: 'Export feature coming soon',
    type: 'info'
  })
}

// ========== UTILITY FUNCTIONS ==========
const showMessage = (msg: Message) => {
  message.value = msg
  if (msg.type === 'success' || msg.type === 'info') {
    setTimeout(() => clearMessage(), 5000)
  }
}

const clearMessage = () => {
  message.value = { text: '', type: 'info' }
}

const formatDate = (dateStr: string): string => {
  if (!dateStr) return 'N/A'

  try {
    let date: Date

    if (dateStr.includes('T')) {
      date = new Date(dateStr)
    } else if (dateStr.includes('-')) {
      date = new Date(dateStr + 'T00:00:00')
    } else {
      date = new Date(dateStr)
    }

    if (isNaN(date.getTime())) return 'Invalid Date'

    return date.toLocaleDateString('en-US', {
      weekday: 'short',
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  } catch (error) {
    return 'Invalid Date'
  }
}

const formatTime = (timeStr: string | Date): string => {
  if (!timeStr) return 'N/A'

  try {
    let date: Date

    if (timeStr instanceof Date) {
      date = timeStr
    } else if (typeof timeStr === 'string') {
      if (timeStr.includes('T')) {
        date = new Date(timeStr)
      } else if (timeStr.includes(':')) {
        date = new Date(`1970-01-01T${timeStr}`)
      } else {
        date = new Date(timeStr)
      }
    } else {
      return 'N/A'
    }

    if (isNaN(date.getTime())) return 'Invalid Time'

    return date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    })
  } catch (error) {
    return 'Invalid Time'
  }
}

const extractOrderNumber = (notes?: string): string => {
  if (!notes || typeof notes !== 'string') return ''

  const match = notes.match(/order\s+([A-Z0-9]+)/i)
  return match ? match[1] : ''
}

// ========== LIFECYCLE ==========
onMounted(async () => {
  console.log('📋 OrderPlanningView mounted')
  await testBackend()
  await loadPlannings()
})
</script>

<style scoped>
.order-planning-view {
  max-width: 1400px;
  margin: 0 auto;
}
</style>
