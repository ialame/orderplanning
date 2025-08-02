<template>
  <div class="order-planning-view">
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
          @click="refreshData"
          :disabled="loading"
          class="flex items-center gap-2 bg-green-600 text-white px-4 py-3 rounded-lg hover:bg-green-700 disabled:opacity-50 transition-colors"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
          </svg>
          Refresh
        </button>
        <button
          @click="exportPlanning"
          class="flex items-center gap-2 bg-purple-600 text-white px-4 py-3 rounded-lg hover:bg-purple-700 transition-colors"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
          </svg>
          Export
        </button>
      </div>
    </div>

    <!-- Configuration Panel -->
    <div class="bg-white rounded-lg shadow-lg p-6 mb-6 border">
      <h2 class="text-xl font-semibold text-gray-900 mb-4 flex items-center">
        <svg class="w-6 h-6 mr-2 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
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

    <!-- Success/Error Messages -->
    <div v-if="message.text" :class="[
      'p-4 rounded-lg mb-6 flex items-center space-x-3',
      message.type === 'success' ? 'bg-green-50 border border-green-200 text-green-800' :
      message.type === 'error' ? 'bg-red-50 border border-red-200 text-red-800' :
      'bg-blue-50 border border-blue-200 text-blue-800'
    ]">
      <div class="flex-shrink-0">
        <span v-if="message.type === 'success'">✅</span>
        <span v-else-if="message.type === 'error'">❌</span>
        <span v-else>ℹ️</span>
      </div>
      <div class="flex-1">
        <p class="font-medium">{{ message.text }}</p>
        <p v-if="message.details" class="text-sm mt-1 opacity-90">{{ message.details }}</p>
      </div>
      <button @click="clearMessage" class="text-gray-400 hover:text-gray-600">
        ✕
      </button>
    </div>

    <!-- System Statistics -->
    <div v-if="systemStats" class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
      <div class="bg-gradient-to-r from-blue-500 to-blue-600 text-white p-6 rounded-lg shadow">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-blue-100 text-sm">Available Orders</p>
            <p class="text-2xl font-bold">{{ systemStats.availableOrders || 0 }}</p>
          </div>
          <div class="text-3xl opacity-80">📦</div>
        </div>
      </div>
      <div class="bg-gradient-to-r from-green-500 to-green-600 text-white p-6 rounded-lg shadow">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-green-100 text-sm">Active Employees</p>
            <p class="text-2xl font-bold">{{ systemStats.activeEmployees || 0 }}</p>
          </div>
          <div class="text-3xl opacity-80">👥</div>
        </div>
      </div>
      <div class="bg-gradient-to-r from-purple-500 to-purple-600 text-white p-6 rounded-lg shadow">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-purple-100 text-sm">Total Plannings</p>
            <p class="text-2xl font-bold">{{ systemStats.totalPlannings || 0 }}</p>
          </div>
          <div class="text-3xl opacity-80">📋</div>
        </div>
      </div>
      <div class="bg-gradient-to-r from-orange-500 to-orange-600 text-white p-6 rounded-lg shadow">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-orange-100 text-sm">Execution Time</p>
            <p class="text-2xl font-bold">{{ stats.executionTimeMs || 0 }}ms</p>
          </div>
          <div class="text-3xl opacity-80">⚡</div>
        </div>
      </div>
    </div>

    <!-- Planning Results -->
    <div v-if="plannings.length > 0" class="space-y-6">
      <h2 class="text-2xl font-bold text-gray-900 flex items-center">
        <svg class="w-6 h-6 mr-2 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
        </svg>
        Planning Results ({{ plannings.length }} items)
      </h2>

      <!-- Planning Cards -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div
          v-for="planning in plannings"
          :key="planning.id"
          class="bg-white rounded-lg shadow-lg border hover:shadow-xl transition-shadow"
        >
          <!-- Card Header -->
          <div class="p-4 border-b border-gray-200">
            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-3">
                <div :class="[
                  'w-3 h-3 rounded-full',
                  planning.priority === 'URGENT' ? 'bg-red-500' :
                  planning.priority === 'HIGH' ? 'bg-orange-500' :
                  planning.priority === 'MEDIUM' ? 'bg-yellow-500' :
                  'bg-green-500'
                ]"></div>
                <div>
                  <h3 class="font-semibold text-gray-900">Order #{{ planning.orderNumber }}</h3>
                  <p class="text-sm text-gray-500">{{ planning.employeeName || 'Employee ' + planning.employeeId.slice(-4) }}</p>
                </div>
              </div>
              <div class="text-right">
                <span :class="[
                  'px-2 py-1 rounded-full text-xs font-medium',
                  planning.priority === 'URGENT' ? 'bg-red-100 text-red-800' :
                  planning.priority === 'HIGH' ? 'bg-orange-100 text-orange-800' :
                  planning.priority === 'MEDIUM' ? 'bg-yellow-100 text-yellow-800' :
                  'bg-green-100 text-green-800'
                ]">
                  {{ planning.priority }}
                </span>
              </div>
            </div>
          </div>

          <!-- Card Content -->
          <div class="p-4">
            <div class="grid grid-cols-2 gap-4 text-sm">
              <div>
                <p class="text-gray-600">Planning Date</p>
                <p class="font-medium">{{ formatDate(planning.planningDate) }}</p>
              </div>
              <div>
                <p class="text-gray-600">Duration</p>
                <p class="font-medium">{{ planning.durationMinutes }} minutes</p>
              </div>
              <div>
                <p class="text-gray-600">Start Time</p>
                <p class="font-medium">{{ formatTime(planning.startTime) }}</p>
              </div>
              <div>
                <p class="text-gray-600">Cards Count</p>
                <p class="font-medium">{{ planning.cardCount || 1 }} cards</p>
              </div>
            </div>

            <!-- Status -->
            <div class="mt-4 pt-4 border-t border-gray-200">
              <div class="flex items-center justify-between">
                <span :class="[
                  'px-3 py-1 rounded-full text-sm font-medium',
                  planning.status === 'COMPLETED' ? 'bg-green-100 text-green-800' :
                  planning.status === 'IN_PROGRESS' ? 'bg-blue-100 text-blue-800' :
                  'bg-gray-100 text-gray-800'
                ]">
                  {{ planning.status }}
                </span>
                <div class="text-xs text-gray-500">
                  ID: {{ planning.id.slice(-8) }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else-if="!loading" class="text-center py-12 bg-white rounded-lg shadow border">
      <svg class="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V9a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"></path>
      </svg>
      <h3 class="text-lg font-medium text-gray-900 mb-2">No Planning Generated</h3>
      <p class="text-gray-500 mb-6">Click "Generate Planning" to start distributing Pokemon card orders to employees</p>
      <button
        @click="generatePlanning"
        class="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors"
      >
        Generate Your First Planning
      </button>
    </div>

    <!-- Debug Panel (development only) -->
    <div v-if="showDebug" class="mt-8 bg-gray-50 rounded-lg p-4">
      <h3 class="text-lg font-semibold mb-4">🔧 Debug Information</h3>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
        <div>
          <h4 class="font-medium mb-2">API Responses:</h4>
          <pre class="bg-white p-3 rounded border text-xs overflow-auto max-h-40">{{ JSON.stringify(debugInfo, null, 2) }}</pre>
        </div>
        <div>
          <h4 class="font-medium mb-2">Configuration:</h4>
          <pre class="bg-white p-3 rounded border text-xs">{{ JSON.stringify(config, null, 2) }}</pre>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'

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
  orderNumber: string
  employeeId: string
  employeeName?: string
  planningDate: string
  startTime: string
  durationMinutes: number
  priority: 'URGENT' | 'HIGH' | 'MEDIUM' | 'LOW'
  status: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED'
  cardCount?: number
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
  type: 'success' | 'error' | 'info'
}

// ========== REACTIVE DATA ==========
const loading = ref(false)
const plannings = ref<Planning[]>([])
const stats = ref<Stats>({
  totalOrders: 0,
  employeesUsed: 0,
  planningsSaved: 0,
  executionTimeMs: 0
})
const systemStats = ref<SystemStats | null>(null)
const message = ref<Message | null>(null)
const debugInfo = ref<any>({})
const showDebug = ref(false) // Set to true for development

const config = ref<PlanningConfig>({
  startDate: '2025-06-01',
  endDate: '2025-07-04',
  numberOfEmployees: 4,
  timePerCard: 3
})

// ========== COMPUTED ==========
const formattedStats = computed(() => {
  return {
    totalOrders: stats.value.totalOrders.toLocaleString(),
    employeesUsed: stats.value.employeesUsed,
    planningsSaved: stats.value.planningsSaved.toLocaleString(),
    executionTime: `${stats.value.executionTimeMs}ms`
  }
})

// ========== METHODS ==========

/**
 * Generate automatic planning using English API
 */
const generatePlanning = async () => {
  loading.value = true
  clearMessage()

  try {
    console.log('🚀 Generating planning with config:', config.value)

    const requestData = {
      startDate: config.value.startDate,
      endDate: config.value.endDate,
      numberOfEmployees: parseInt(config.value.numberOfEmployees.toString()),
      timePerCard: parseInt(config.value.timePerCard.toString())
    }

    console.log('📤 Sending request to English API:', requestData)

    const response = await fetch('/api/planning/generate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(requestData)
    })

    if (!response.ok) {
      const errorText = await response.text()
      throw new Error(`HTTP ${response.status}: ${errorText}`)
    }

    const data = await response.json()
    console.log('✅ Planning data received:', data)

    debugInfo.value.generateResponse = data

    if (data.success) {
      // Update plannings (from the generation response)
      plannings.value = data.plannings || []

      // Update stats
      stats.value = {
        totalOrders: data.ordersAnalyzed || 0,
        employeesUsed: data.employeesUsed || 0,
        planningsSaved: data.planningsSaved || 0,
        executionTimeMs: data.executionTimeMs || 0
      }

      showMessage({
        text: `Planning generation successful!`,
        details: `${data.planningsSaved || 0} plannings created from ${data.ordersAnalyzed || 0} orders in ${data.executionTimeMs || 0}ms`,
        type: 'success'
      })

      // Refresh the planning view to get complete data
      await loadPlannings()

    } else {
      throw new Error(data.error || 'Planning generation failed')
    }

  } catch (error) {
    console.error('❌ Error generating planning:', error)

    debugInfo.value.generateError = error

    showMessage({
      text: 'Planning generation failed',
      details: error instanceof Error ? error.message : 'Unknown error occurred',
      type: 'error'
    })

    // Clear previous data on error
    plannings.value = []
    stats.value = { totalOrders: 0, employeesUsed: 0, planningsSaved: 0, executionTimeMs: 0 }

  } finally {
    loading.value = false
  }
}

/**
 * Load existing plannings from the database
 */
const loadPlannings = async () => {
  try {
    console.log('📋 Loading existing plannings...')

    // Load plannings for the configured start date
    const response = await fetch(`/api/planning/view-simple?date=${config.value.startDate}`)

    if (!response.ok) {
      throw new Error(`Failed to load plannings: ${response.status}`)
    }

    const data = await response.json()
    console.log('✅ Plannings loaded:', data.length)

    debugInfo.value.loadResponse = data

    // Transform the data to match our interface
    plannings.value = data.map((item: any) => ({
      id: item.id,
      orderId: item.orderId,
      orderNumber: extractOrderNumber(item.notes),
      employeeId: item.employeeId,
      employeeName: item.employeeName || `Employee ${item.employeeId.slice(-4)}`,
      planningDate: item.planningDate,
      startTime: item.startTime,
      durationMinutes: item.durationMinutes,
      priority: item.priority,
      status: item.status,
      cardCount: item.cardCount
    }))

  } catch (error) {
    console.error('❌ Error loading plannings:', error)
    showMessage({
      text: 'Failed to load plannings',
      details: error instanceof Error ? error.message : 'Unknown error',
      type: 'error'
    })
  }
}

/**
 * Load system statistics
 */
const loadSystemStats = async () => {
  try {
    // Load debug info
    const debugResponse = await fetch('/api/planning/debug-real')
    const debugData = await debugResponse.json()

    // Load statistics
    const statsResponse = await fetch('/api/planning/stats')
    const statsData = await statsResponse.json()

    systemStats.value = {
      availableOrders: debugData.availableOrders || 0,
      activeEmployees: debugData.activeEmployees || 0,
      totalPlannings: statsData.totalPlannings || 0
    }

    debugInfo.value.systemStats = { debugData, statsData }

  } catch (error) {
    console.error('❌ Error loading system stats:', error)
  }
}

/**
 * Refresh all data
 */
const refreshData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadPlannings(),
      loadSystemStats()
    ])
    showMessage({
      text: 'Data refreshed successfully',
      type: 'success'
    })
  } catch (error) {
    showMessage({
      text: 'Failed to refresh data',
      details: error instanceof Error ? error.message : 'Unknown error',
      type: 'error'
    })
  } finally {
    loading.value = false
  }
}

/**
 * Export planning data
 */
const exportPlanning = async () => {
  try {
    const dataToExport = {
      config: config.value,
      stats: stats.value,
      plannings: plannings.value,
      generatedAt: new Date().toISOString()
    }

    const blob = new Blob([JSON.stringify(dataToExport, null, 2)], {
      type: 'application/json'
    })

    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `pokemon-planning-${config.value.startDate}.json`
    document.body.appendChild(a)
    a.click()
    URL.revokeObjectURL(url)
    document.body.removeChild(a)

    showMessage({
      text: 'Planning exported successfully',
      type: 'success'
    })

  } catch (error) {
    console.error('❌ Export error:', error)
    showMessage({
      text: 'Export failed',
      details: error instanceof Error ? error.message : 'Unknown error',
      type: 'error'
    })
  }
}

// ========== UTILITY FUNCTIONS ==========

const showMessage = (msg: Message) => {
  message.value = msg
  // Auto-clear success messages after 5 seconds
  if (msg.type === 'success') {
    setTimeout(() => {
      if (message.value && message.value.type === 'success') {
        clearMessage()
      }
    }, 5000)
  }
}

const clearMessage = () => {
  message.value = null
}

const formatDate = (dateStr: string): string => {
  try {
    return new Date(dateStr).toLocaleDateString('en-US', {
      weekday: 'short',
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  } catch {
    return dateStr
  }
}

const formatTime = (timeStr: string): string => {
  try {
    return new Date(timeStr).toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return timeStr
  }
}

const extractOrderNumber = (notes: string): string => {
  const match = notes?.match(/order (\w+)/)
  return match ? match[1] : 'Unknown'
}

// ========== LIFECYCLE ==========
onMounted(async () => {
  console.log('📋 OrderPlanningView mounted')
  await Promise.all([
    loadSystemStats(),
    loadPlannings()
  ])
})
</script>

<style scoped>
.order-planning-view {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
}

@media (max-width: 768px) {
  .order-planning-view {
    padding: 1rem;
  }
}

/* Custom scrollbar for debug panel */
pre::-webkit-scrollbar {
  width: 4px;
}

pre::-webkit-scrollbar-track {
  background: #f1f1f1;
}

pre::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

pre::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}
</style>
