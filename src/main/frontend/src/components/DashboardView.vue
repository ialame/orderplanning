<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h2 class="text-3xl font-bold text-gray-900">📊 Dashboard</h2>
        <p class="text-gray-600 mt-1">Overview of your card order management system</p>
      </div>
      <button
        @click="refreshData"
        :disabled="loading"
        class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors flex items-center gap-2"
      >
        <span :class="loading ? 'animate-spin' : ''">🔄</span>
        {{ loading ? 'Loading...' : 'Refresh' }}
      </button>
    </div>

    <!-- Welcome Message -->
    <div class="bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 rounded-lg p-6">
      <h3 class="text-lg font-semibold text-blue-900 mb-2">
        🎉 Welcome to OrderPlanning!
      </h3>
      <p class="text-blue-700">
        This application helps you manage Pokemon card orders with intelligent automatic planning.
        Processing time: <strong>{{ PROCESSING_TIME_PER_CARD }} minutes per card</strong>.
      </p>
    </div>

    <!-- Main Statistics -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <!-- Total Orders -->
      <div class="bg-white p-6 rounded-lg shadow-md border-l-4 border-blue-500 hover:shadow-lg transition-shadow">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">Total Orders</p>
            <p class="text-3xl font-bold text-blue-600">{{ stats.totalOrders || 0 }}</p>
            <p class="text-xs text-gray-500 mt-1">
              {{ stats.totalCards || 0 }} cards total
            </p>
          </div>
          <div class="text-4xl text-blue-600">📦</div>
        </div>
      </div>

      <!-- Pending Orders -->
      <div class="bg-white p-6 rounded-lg shadow-md border-l-4 border-yellow-500 hover:shadow-lg transition-shadow">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">Pending</p>
            <p class="text-3xl font-bold text-yellow-600">{{ stats.pendingOrders || 0 }}</p>
            <p class="text-xs text-gray-500 mt-1">
              Awaiting planning
            </p>
          </div>
          <div class="text-4xl text-yellow-600">⏳</div>
        </div>
      </div>

      <!-- In Progress -->
      <div class="bg-white p-6 rounded-lg shadow-md border-l-4 border-green-500 hover:shadow-lg transition-shadow">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">In Progress</p>
            <p class="text-3xl font-bold text-green-600">{{ stats.inProgressOrders || 0 }}</p>
            <p class="text-xs text-gray-500 mt-1">
              Currently being processed
            </p>
          </div>
          <div class="text-4xl text-green-600">⚙️</div>
        </div>
      </div>

      <!-- Completed -->
      <div class="bg-white p-6 rounded-lg shadow-md border-l-4 border-purple-500 hover:shadow-lg transition-shadow">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">Completed</p>
            <p class="text-3xl font-bold text-purple-600">{{ stats.completedOrders || 0 }}</p>
            <p class="text-xs text-gray-500 mt-1">
              Successfully finished
            </p>
          </div>
          <div class="text-4xl text-purple-600">✅</div>
        </div>
      </div>
    </div>

    <!-- Secondary Statistics -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <!-- Active Employees -->
      <div class="bg-white p-6 rounded-lg shadow-md">
        <h4 class="text-lg font-semibold text-gray-900 mb-4">👥 Active Employees</h4>
        <div class="text-center">
          <p class="text-4xl font-bold text-indigo-600">{{ stats.activeEmployees || 0 }}</p>
          <p class="text-sm text-gray-600 mt-2">
            {{ employeeWorkloadSummary }}
          </p>
        </div>
      </div>

      <!-- Processing Performance -->
      <div class="bg-white p-6 rounded-lg shadow-md">
        <h4 class="text-lg font-semibold text-gray-900 mb-4">⚡ Performance</h4>
        <div class="space-y-3">
          <div class="flex justify-between">
            <span class="text-sm text-gray-600">Avg. processing time:</span>
            <span class="text-sm font-medium">{{ averageProcessingTime }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-sm text-gray-600">Efficiency rate:</span>
            <span class="text-sm font-medium text-green-600">{{ efficiencyRate }}%</span>
          </div>
          <div class="flex justify-between">
            <span class="text-sm text-gray-600">Cards per day:</span>
            <span class="text-sm font-medium">{{ cardsPerDay }}</span>
          </div>
        </div>
      </div>

      <!-- Recent Activity -->
      <div class="bg-white p-6 rounded-lg shadow-md">
        <h4 class="text-lg font-semibold text-gray-900 mb-4">📈 Recent Activity</h4>
        <div class="space-y-2">
          <div v-for="activity in recentActivities" :key="activity.id" class="flex items-center text-sm">
            <span class="mr-2">{{ activity.icon }}</span>
            <span class="text-gray-600">{{ activity.text }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- System Status -->
    <div class="bg-white p-6 rounded-lg shadow-md">
      <h4 class="text-lg font-semibold text-gray-900 mb-4">🔧 System Status</h4>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- API Connection -->
        <div class="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
          <div class="flex items-center space-x-3">
            <div :class="[
              'w-3 h-3 rounded-full',
              apiConnected ? 'bg-green-500' : 'bg-red-500'
            ]"></div>
            <span :class="[
              'text-sm font-medium',
              apiConnected ? 'text-green-700' : 'text-red-700'
            ]">
              {{ apiConnected ? 'Backend API Connected' : 'Backend API Disconnected' }}
            </span>
          </div>
          <code class="text-xs bg-gray-200 px-2 py-1 rounded">
            localhost:8080
          </code>
        </div>

        <!-- Database Status -->
        <div class="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
          <div class="flex items-center space-x-3">
            <div :class="[
              'w-3 h-3 rounded-full',
              databaseConnected ? 'bg-green-500' : 'bg-yellow-500'
            ]"></div>
            <span :class="[
              'text-sm font-medium',
              databaseConnected ? 'text-green-700' : 'text-yellow-700'
            ]">
              {{ databaseConnected ? 'Database Online' : 'Database Status Unknown' }}
            </span>
          </div>
          <span class="text-xs text-gray-500">
            Last sync: {{ lastSync ? formatTime(lastSync) : 'Never' }}
          </span>
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="bg-white p-6 rounded-lg shadow-md">
      <h4 class="text-lg font-semibold text-gray-900 mb-4">⚡ Quick Actions</h4>
      <div class="flex flex-wrap gap-3">
        <button
          @click="goToOrders"
          class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2"
        >
          📋 <span>New Order</span>
        </button>
        <button
          @click="goToEmployees"
          class="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-colors flex items-center gap-2"
        >
          👤 <span>Manage Employees</span>
        </button>
        <button
          @click="planAutomatically"
          :disabled="loading || !apiConnected"
          class="bg-purple-600 text-white px-4 py-2 rounded-lg hover:bg-purple-700 disabled:opacity-50 transition-colors flex items-center gap-2"
        >
          🤖 <span>Auto Planning</span>
        </button>
        <button
          @click="goToPlanning"
          class="bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700 transition-colors flex items-center gap-2"
        >
          📅 <span>View Planning</span>
        </button>
      </div>
    </div>

    <!-- Debug Information (Development Mode) -->
    <div v-if="isDevelopment && rawApiData" class="bg-gray-50 p-6 rounded-lg border">
      <h4 class="text-lg font-semibold text-gray-900 mb-4">🐛 Debug Information</h4>
      <details class="text-sm">
        <summary class="cursor-pointer text-blue-600 hover:text-blue-800">Raw API Data</summary>
        <pre class="bg-white p-3 rounded text-xs overflow-x-auto mt-2">{{ JSON.stringify(rawApiData, null, 2) }}</pre>
      </details>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, inject } from 'vue'

// ========== CONSTANTS ==========
const PROCESSING_TIME_PER_CARD = 3 // minutes per card
const isDevelopment = process.env.NODE_ENV === 'development'

// ========== TYPES ==========
interface DashboardStats {
  totalOrders: number
  pendingOrders: number
  inProgressOrders: number
  completedOrders: number
  activeEmployees: number
  totalCards: number
  status: 'connected' | 'disconnected' | 'demo'
}

interface Activity {
  id: string
  icon: string
  text: string
  timestamp: Date
}

// ========== REACTIVE STATE ==========
const loading = ref(false)
const apiConnected = ref(false)
const databaseConnected = ref(false)
const lastSync = ref<Date | null>(null)
const rawApiData = ref<any>(null)

// Dashboard statistics
const stats = ref<DashboardStats>({
  totalOrders: 0,
  pendingOrders: 0,
  inProgressOrders: 0,
  completedOrders: 0,
  activeEmployees: 0,
  totalCards: 0,
  status: 'disconnected'
})

// Recent activities
const recentActivities = ref<Activity[]>([
  { id: '1', icon: '📦', text: 'New order #1234 created', timestamp: new Date() },
  { id: '2', icon: '✅', text: 'Order #1230 completed', timestamp: new Date() },
  { id: '3', icon: '👤', text: 'Employee John added', timestamp: new Date() }
])

// ========== COMPUTED PROPERTIES ==========
const employeeWorkloadSummary = computed(() => {
  const total = stats.value.activeEmployees
  if (total === 0) return 'No employees active'
  return `${total} employee${total > 1 ? 's' : ''} available`
})

const averageProcessingTime = computed(() => {
  const avgCards = stats.value.totalCards / Math.max(stats.value.totalOrders, 1)
  const avgMinutes = avgCards * PROCESSING_TIME_PER_CARD
  return `${Math.round(avgMinutes)} min/order`
})

const efficiencyRate = computed(() => {
  // Calculate efficiency based on completed vs total orders
  const completed = stats.value.completedOrders
  const total = stats.value.totalOrders
  if (total === 0) return 100
  return Math.round((completed / total) * 100)
})

const cardsPerDay = computed(() => {
  // Assuming 8 hours work day, 60 minutes per hour, 3 minutes per card
  const cardsPerEmployee = (8 * 60) / PROCESSING_TIME_PER_CARD
  return Math.round(cardsPerEmployee * stats.value.activeEmployees)
})

// ========== INJECTED FUNCTIONS ==========
const showNotification = inject('showNotification') as (message: string, type?: 'success' | 'error' | 'warning' | 'info') => void
const changeTab = inject('changeTab') as (tabId: string) => void
const setGlobalLoading = inject('setGlobalLoading') as (loading: boolean, message?: string) => void

// ========== METHODS ==========

/**
 * Test API connection
 */
const testApiConnection = async (): Promise<boolean> => {
  try {
    const response = await fetch('/api/dashboard/stats', {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    })

    apiConnected.value = response.ok
    databaseConnected.value = response.ok

    if (response.ok) {
      console.log('✅ API connection successful')
      return true
    } else {
      console.warn('⚠️ API connection failed:', response.status)
      return false
    }
  } catch (error) {
    console.error('❌ API connection error:', error)
    apiConnected.value = false
    databaseConnected.value = false
    return false
  }
}

/**
 * Load dashboard data
 */
const loadData = async () => {
  try {
    loading.value = true
    lastSync.value = new Date()

    // Test connection first
    const connected = await testApiConnection()

    if (connected) {
      // Try to load real data
      const response = await fetch('/api/dashboard/stats')

      if (response.ok) {
        const data = await response.json()
        rawApiData.value = data

        // Map API response to our stats structure
        stats.value = {
          totalOrders: data.totalOrders || data.totalCommandes || 0,
          pendingOrders: data.pendingOrders || data.commandesEnAttente || 0,
          inProgressOrders: data.inProgressOrders || data.commandesEnCours || 0,
          completedOrders: data.completedOrders || data.commandesTerminees || 0,
          activeEmployees: data.activeEmployees || data.employesActifs || 0,
          totalCards: data.totalCards || 0,
          status: 'connected'
        }

        console.log('✅ Dashboard data updated:', stats.value)
      } else {
        throw new Error(`API responded with status ${response.status}`)
      }
    } else {
      // Fallback: demo data
      console.log('⚠️ Demo mode - using mock data')
      stats.value = {
        totalOrders: 15,
        pendingOrders: 8,
        inProgressOrders: 3,
        completedOrders: 4,
        activeEmployees: 3,
        totalCards: 47,
        status: 'demo'
      }
    }
  } catch (error) {
    console.error('❌ Error loading dashboard data:', error)
    showNotification?.('Failed to load dashboard data', 'error')

    // Default empty state
    stats.value = {
      totalOrders: 0,
      pendingOrders: 0,
      inProgressOrders: 0,
      completedOrders: 0,
      activeEmployees: 0,
      totalCards: 0,
      status: 'disconnected'
    }
  } finally {
    loading.value = false
  }
}

/**
 * Refresh all data
 */
const refreshData = async () => {
  console.log('🔄 Refreshing dashboard data...')
  await loadData()
  showNotification?.('Dashboard data refreshed', 'success')
}

/**
 * Auto planning function
 */
const planAutomatically = async () => {
  try {
    setGlobalLoading?.(true, 'Running automatic planning...')

    const response = await fetch('/api/planning/auto', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    })

    if (response.ok) {
      const result = await response.json()
      showNotification?.(
        `Planning completed: ${result.ordersPlanned || 0} orders planned`,
        'success'
      )
      await loadData() // Refresh data
    } else {
      throw new Error('Planning failed')
    }
  } catch (error) {
    console.error('Error in automatic planning:', error)
    showNotification?.('Automatic planning failed', 'error')
  } finally {
    setGlobalLoading?.(false)
  }
}

// ========== NAVIGATION METHODS ==========
const goToOrders = () => {
  console.log('Navigate to orders')
  changeTab?.('orders')
}

const goToEmployees = () => {
  console.log('Navigate to employees')
  changeTab?.('employees')
}

const goToPlanning = () => {
  console.log('Navigate to planning')
  changeTab?.('planning')
}

// ========== UTILITIES ==========
const formatTime = (date: Date): string => {
  return date.toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// ========== LIFECYCLE ==========
onMounted(async () => {
  console.log('🚀 Dashboard component mounted')

  // Initial data load
  await loadData()

  // Set up auto-refresh every 30 seconds (only if connected)
  setInterval(() => {
    if (apiConnected.value && !loading.value) {
      loadData()
    }
  }, 30000)
})
</script>

<style scoped>
/* Custom scrollbar for debug section */
pre::-webkit-scrollbar {
  height: 4px;
}

pre::-webkit-scrollbar-track {
  background: #f1f5f9;
}

pre::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 2px;
}

/* Hover effects */
.hover\:shadow-lg:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

/* Animation for loading spinner */
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}
</style>
