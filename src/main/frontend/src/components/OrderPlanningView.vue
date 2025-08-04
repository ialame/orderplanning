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
          @click="testBackend"
          class="flex items-center gap-2 bg-purple-600 text-white px-4 py-3 rounded-lg hover:bg-purple-700 transition-colors"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
          </svg>
          Test Backend
        </button>
      </div>
    </div>

    <!-- Backend Status -->
    <div class="bg-white rounded-lg shadow p-6 mb-6">
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-lg font-semibold text-gray-900">Backend Connection Status</h2>
          <p class="text-gray-600">Planning API and backend services</p>
        </div>
        <div :class="[
          'px-3 py-1 rounded-full text-sm font-medium',
          backendStatus === 'connected' ? 'bg-green-100 text-green-800' :
          backendStatus === 'error' ? 'bg-red-100 text-red-800' :
          'bg-yellow-100 text-yellow-800'
        ]">
          {{
            backendStatus === 'connected' ? '✅ Connected' :
              backendStatus === 'error' ? '❌ Error' :
                '⏳ Testing...'
          }}
        </div>
      </div>

      <div v-if="statusMessage" class="mt-4 p-3 bg-gray-50 rounded-lg">
        <p class="text-sm text-gray-700">{{ statusMessage }}</p>
      </div>

      <!-- API Endpoints Status -->
      <div class="mt-4 grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <h3 class="font-medium text-gray-900 mb-2">Planning API</h3>
          <ul class="text-sm text-gray-600 space-y-1">
            <li>{{ endpoints.planning }} /api/planning/generate</li>
            <li>{{ endpoints.debug }} /api/planning/debug-real</li>
            <li>{{ endpoints.stats }} /api/planning/stats</li>
            <li>{{ endpoints.planningView }} /api/planning/view-simple</li>
          </ul>
        </div>

        <div>
          <h3 class="font-medium text-gray-900 mb-2">Data API</h3>
          <ul class="text-sm text-gray-600 space-y-1">
            <li>{{ endpoints.orders }} /api/orders</li>
            <li>{{ endpoints.employees }} /api/employees</li>
            <li>{{ endpoints.planifications }} /api/planifications/planifications-avec-details</li>
          </ul>
        </div>
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

      <!-- Date Information from .env -->
      <div class="mb-6 p-4 bg-blue-50 rounded-lg border border-blue-200">
        <h3 class="text-sm font-semibold text-blue-900 mb-2">📅 Configured Date Range</h3>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
          <div>
            <span class="font-medium text-blue-800">Orders from:</span>
            <span class="text-blue-700 ml-2">{{ formatDisplayDate(dateConfig.orderStartDate) }}</span>
          </div>
          <div>
            <span class="font-medium text-blue-800">Processing period:</span>
            <span class="text-blue-700 ml-2">{{ formatDisplayDate(dateConfig.planningStartDate) }} → {{ formatDisplayDate(dateConfig.planningEndDate) }}</span>
          </div>
        </div>
        <p class="text-xs text-blue-600 mt-2">💡 Configure these dates in your .env file</p>
      </div>

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
      <svg v-if="message.type === 'success'" class="w-5 h-5 text-green-500" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
      </svg>
      <svg v-else-if="message.type === 'error'" class="w-5 h-5 text-red-500" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
      </svg>
      <svg v-else class="w-5 h-5 text-blue-500" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/>
      </svg>
      <div>
        <p class="font-medium">{{ message.text }}</p>
        <p v-if="message.details" class="text-sm opacity-75">{{ message.details }}</p>
      </div>
      <button @click="clearMessage" class="ml-auto">
        <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/>
        </svg>
      </button>
    </div>

    <!-- System Statistics -->
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
        <h2 class="text-lg font-semibold text-gray-900">Generated Plannings</h2>
        <p class="text-gray-600">Recent planning assignments for employees</p>
      </div>

      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Order</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Employee</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Date</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Time</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Duration</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Priority</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
          </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="planning in plannings.slice(0, 10)" :key="planning.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
              {{ planning.orderNumber || planning.orderId.slice(-6) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
              {{ planning.employeeName || `Emp ${planning.employeeId.slice(-4)}` }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
              {{ formatDate(planning.planningDate) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
              {{ formatTime(planning.startTime) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
              {{ planning.durationMinutes }}min
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
                <span :class="[
                  'inline-flex px-2 py-1 text-xs font-semibold rounded-full',
                  planning.priority === 'URGENT' ? 'bg-red-100 text-red-800' :
                  planning.priority === 'HIGH' ? 'bg-orange-100 text-orange-800' :
                  planning.priority === 'MEDIUM' ? 'bg-yellow-100 text-yellow-800' :
                  'bg-green-100 text-green-800'
                ]">
                  {{ planning.priority }}
                </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
                <span :class="[
                  'inline-flex px-2 py-1 text-xs font-semibold rounded-full',
                  planning.status === 'COMPLETED' ? 'bg-green-100 text-green-800' :
                  planning.status === 'IN_PROGRESS' ? 'bg-blue-100 text-blue-800' :
                  'bg-gray-100 text-gray-800'
                ]">
                  {{ planning.status }}
                </span>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <div v-if="plannings.length > 10" class="px-6 py-3 bg-gray-50 text-center">
        <p class="text-sm text-gray-600">Showing 10 of {{ plannings.length }} plannings</p>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { dateConfig, systemConfig, appConfig, mapPriorityCode, formatDisplayDate, debugLog } from '../config/appConfig'

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
  type: 'success' | 'error' | 'info' | 'warning'
}

// ========== EMITS ==========
const emit = defineEmits(['show-notification'])

// ========== STATE ==========
const loading = ref(false)
const backendStatus = ref<'testing' | 'connected' | 'error'>('testing')
const statusMessage = ref('')

const plannings = ref<Planning[]>([])
const message = ref<Message>({ text: '', type: 'info' })

const endpoints = ref({
  planning: '❓ Not tested',
  debug: '❓ Not tested',
  stats: '❓ Not tested',
  orders: '❓ Not tested',
  employees: '❓ Not tested',
  planningView: '❓ Not tested',
  planifications: '❓ Not tested'
})

const config = ref<PlanningConfig>({
  startDate: dateConfig.planningStartDate,
  endDate: dateConfig.planningEndDate,
  numberOfEmployees: systemConfig.defaultEmployees,
  timePerCard: systemConfig.defaultTimePerCard
})

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

const testBackend = async () => {
  backendStatus.value = 'testing'
  statusMessage.value = 'Testing backend connection...'

  const tests = [
    { name: 'debug', url: '/api/planning/debug-real' },
    { name: 'stats', url: '/api/planning/stats' },
    { name: 'planning', url: '/api/planning/generate' },
    { name: 'orders', url: '/api/orders' },
    { name: 'employees', url: '/api/employees' },
    { name: 'planningView', url: '/api/planning/view-simple' },
    { name: 'planifications', url: '/api/planifications/planifications-avec-details' }
  ]

  let successCount = 0

  for (const test of tests) {
    try {
      const response = await fetch(test.url, {
        method: test.name === 'planning' ? 'POST' : 'GET',
        headers: test.name === 'planning' ? { 'Content-Type': 'application/json' } : {},
        body: test.name === 'planning' ? JSON.stringify(config.value) : undefined
      })

      if (response.ok) {
        endpoints.value[test.name as keyof typeof endpoints.value] = '✅ Available'
        successCount++
      } else {
        endpoints.value[test.name as keyof typeof endpoints.value] = `❌ Error ${response.status}`
      }
    } catch (error) {
      endpoints.value[test.name as keyof typeof endpoints.value] = '❌ Connection failed'
    }
  }

  if (successCount > 0) {
    backendStatus.value = 'connected'
    statusMessage.value = `✅ Backend partially available (${successCount}/${tests.length} endpoints working)`
    showMessage({
      text: 'Backend connection test completed',
      details: `${successCount} out of ${tests.length} endpoints are working`,
      type: 'success'
    })
  } else {
    backendStatus.value = 'error'
    statusMessage.value = '❌ Backend not accessible. Please check if Spring Boot server is running on port 8080.'
    showMessage({
      text: 'Backend connection failed',
      details: 'Please start your Spring Boot server and try again',
      type: 'error'
    })
  }
}

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

    // Update stats
    stats.value = {
      totalOrders: data.totalOrders || 0,
      employeesUsed: data.employeesUsed || 0,
      planningsSaved: data.planningsSaved || 0,
      executionTimeMs: data.executionTimeMs || 0
    }

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
      details: error instanceof Error ? error.message : 'Unknown error occurred',
      type: 'error'
    })
  } finally {
    loading.value = false
  }
}

const loadPlannings = async () => {
  try {
    console.log('📋 Loading existing plannings from j_planning table...')

    // Try multiple endpoints for loading plannings
    const endpoints = [
      `/api/planning/view-simple?date=${config.value.startDate}`,
      `/api/planning/view?date=${config.value.startDate}`,
      `/api/planning/view`,
      `/api/planifications/planifications-avec-details`
    ]

    let response = null
    let usedEndpoint = ''

    for (const endpoint of endpoints) {
      try {
        console.log(`🔄 Trying planning endpoint: ${endpoint}`)
        response = await fetch(endpoint)

        if (response.ok) {
          usedEndpoint = endpoint
          console.log(`✅ Success with planning endpoint: ${endpoint}`)
          break
        } else {
          console.log(`❌ Failed planning endpoint ${endpoint}: HTTP ${response.status}`)
        }
      } catch (error) {
        console.log(`❌ Failed planning endpoint ${endpoint}:`, error.message)
        continue
      }
    }

    if (response && response.ok) {
      const data = await response.json()
      console.log('🔍 Raw planning data from backend:', data)

      // Transform the data to match our interface
      plannings.value = Array.isArray(data) ? data.map((item: any) => ({
        id: item.id,
        orderId: item.orderId || item.order_id,
        orderNumber: extractOrderNumber(item.notes) || item.orderNumber || item.order_number || `ORD-${item.orderId?.slice(-6)}`,
        employeeId: item.employeeId || item.employee_id,
        employeeName: item.employeeName || item.employee_name || `Employee ${item.employeeId?.slice(-4)}`,
        planningDate: item.planningDate || item.planning_date,
        startTime: item.startTime || item.start_time,
        durationMinutes: item.durationMinutes || item.duration_minutes || 0,
        priority: item.priority || 'MEDIUM',
        status: item.status || 'SCHEDULED',
        cardCount: item.cardCount || item.card_count
      })) : []

      console.log(`✅ Loaded ${plannings.value.length} plannings from ${usedEndpoint}`)

      if (plannings.value.length > 0) {
        showMessage({
          text: `Loaded ${plannings.value.length} existing plannings`,
          details: `Found plannings from ${usedEndpoint}`,
          type: 'success'
        })
      } else {
        showMessage({
          text: 'No existing plannings found',
          details: 'Database connected but no plannings found in j_planning table',
          type: 'info'
        })
      }

    } else {
      console.log('⚠️ No planning endpoints available - showing empty state')
      plannings.value = []
    }

  } catch (error) {
    console.error('❌ Error loading plannings:', error)
    plannings.value = []
    showMessage({
      text: 'Error loading plannings',
      details: error instanceof Error ? error.message : 'Unknown error',
      type: 'error'
    })
  }
}

const refreshData = async () => {
  await Promise.all([
    loadSystemStats(),
    loadPlannings()
  ])
}

const loadSystemStats = async () => {
  try {
    const [debugResponse, statsResponse] = await Promise.all([
      fetch('/api/planning/debug-real'),
      fetch('/api/planning/stats')
    ])

    if (debugResponse.ok) {
      const debugData = await debugResponse.json()
      systemStats.value.availableOrders = debugData.availableOrders || 0
      systemStats.value.activeEmployees = debugData.activeEmployees || 0
    }

    if (statsResponse.ok) {
      const statsData = await statsResponse.json()
      systemStats.value.totalPlannings = statsData.totalPlannings || 0
    }

  } catch (error) {
    console.error('❌ Error loading system stats:', error)
    // Set sample values
    systemStats.value = {
      availableOrders: 15,
      activeEmployees: 4,
      totalPlannings: 23
    }
  }
}

const loadSampleData = () => {
  plannings.value = [
    {
      id: 'sample-001',
      orderId: 'ord-sample-001',
      orderNumber: 'SAMPLE-001',
      employeeId: 'emp-001',
      employeeName: 'Alice Johnson',
      planningDate: config.value.startDate,
      startTime: '09:00',
      durationMinutes: 45,
      priority: 'URGENT',
      status: 'SCHEDULED'
    },
    {
      id: 'sample-002',
      orderId: 'ord-sample-002',
      orderNumber: 'SAMPLE-002',
      employeeId: 'emp-002',
      employeeName: 'Bob Smith',
      planningDate: config.value.startDate,
      startTime: '10:00',
      durationMinutes: 30,
      priority: 'HIGH',
      status: 'IN_PROGRESS'
    },
    {
      id: 'sample-003',
      orderId: 'ord-sample-003',
      orderNumber: 'SAMPLE-003',
      employeeId: 'emp-003',
      employeeName: 'Carol Williams',
      planningDate: config.value.startDate,
      startTime: '11:00',
      durationMinutes: 60,
      priority: 'MEDIUM',
      status: 'COMPLETED'
    }
  ]

  stats.value = {
    totalOrders: 15,
    employeesUsed: 3,
    planningsSaved: 15,
    executionTimeMs: 250
  }

  showMessage({
    text: 'Sample data loaded',
    details: 'Showing demo planning data for testing',
    type: 'info'
  })
}

const clearPlannings = async () => {
  if (confirm('Are you sure you want to clear all plannings?')) {
    plannings.value = []
    stats.value = { totalOrders: 0, employeesUsed: 0, planningsSaved: 0, executionTimeMs: 0 }
    showMessage({
      text: 'All plannings cleared',
      type: 'info'
    })
  }
}

const exportData = () => {
  const dataToExport = {
    config: config.value,
    stats: stats.value,
    plannings: plannings.value,
    systemStats: systemStats.value,
    exportedAt: new Date().toISOString()
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
    text: 'Planning data exported',
    type: 'success'
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
    return new Date(`1970-01-01T${timeStr}`).toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return timeStr
  }
}

const extractOrderNumber = (notes?: string): string => {
  if (!notes) return 'Unknown'
  const match = notes.match(/order (\w+)/)
  return match ? match[1] : 'Unknown'
}

// ========== LIFECYCLE ==========
onMounted(async () => {
  console.log('📋 OrderPlanningView mounted - Loading data automatically...')

  // Automatically test backend and load all data on mount
  await testBackend()
  await loadSystemStats()
  await loadPlannings()
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

/* Custom scrollbar for tables */
.overflow-x-auto::-webkit-scrollbar {
  height: 8px;
}

.overflow-x-auto::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.overflow-x-auto::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.overflow-x-auto::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}
</style>
