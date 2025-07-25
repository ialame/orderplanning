<template>
  <div class="orders-view p-6">
    <div class="header flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-900">Order Management</h1>
      <button @click="createNewOrder" class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700">
        ➕ New Order
      </button>
    </div>

    <!-- Debug Information -->
    <div v-if="debugMode" class="debug-panel bg-gray-100 p-4 rounded-lg mb-6">
      <h3 class="font-bold mb-2">🐛 Debug Information</h3>
      <p><strong>Loading:</strong> {{ loading }}</p>
      <p><strong>Error:</strong> {{ error || 'None' }}</p>
      <p><strong>Orders count:</strong> {{ orders.length }}</p>
      <p><strong>Filtered orders count:</strong> {{ filteredOrders.length }}</p>

      <details v-if="orders.length > 0" class="mt-2">
        <summary class="cursor-pointer text-blue-600">Raw Orders Data (first order)</summary>
        <pre class="bg-white p-2 rounded text-xs mt-2 overflow-x-auto">{{ JSON.stringify(orders[0], null, 2) }}</pre>
      </details>

      <button @click="toggleDebug" class="mt-2 bg-gray-500 text-white px-2 py-1 rounded text-sm">
        Hide Debug
      </button>
    </div>

    <div v-else class="mb-4">
      <button @click="toggleDebug" class="bg-gray-500 text-white px-2 py-1 rounded text-sm">
        Show Debug
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-8">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
      <p class="mt-2">Loading orders...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
      <h3 class="text-red-800 font-medium">Error loading orders</h3>
      <p class="text-red-600 text-sm mt-1">{{ error }}</p>
      <button @click="loadOrders" class="mt-2 bg-red-600 text-white px-3 py-1 rounded text-sm hover:bg-red-700">
        Retry
      </button>
    </div>

    <!-- Orders Table -->
    <div v-else class="bg-white rounded-lg shadow overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
        <tr>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Number</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Cards</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Priority</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Date</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Time</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="filteredOrders.length === 0">
          <td colspan="7" class="px-6 py-8 text-center text-gray-500">
            {{ orders.length === 0 ? 'No orders found' : 'No orders match the current filters' }}
          </td>
        </tr>
        <tr v-for="order in filteredOrders" :key="order.id" class="hover:bg-gray-50">
          <td class="px-6 py-4 text-sm text-gray-900">
            {{ safeGet(order, 'orderNumber') || safeGet(order, 'numeroCommande') || 'N/A' }}
          </td>
          <td class="px-6 py-4 text-sm text-gray-900">
            {{ safeGet(order, 'cardCount') || safeGet(order, 'nombreCartes') || 0 }} cards
          </td>
          <td class="px-6 py-4 text-sm">
              <span :class="getPriorityClass(safeGet(order, 'priority'))">
                {{ safeGet(order, 'priority') || 'MEDIUM' }}
              </span>
          </td>
          <td class="px-6 py-4 text-sm">
              <span :class="getStatusClass(safeGet(order, 'status') || safeGet(order, 'statusText'))">
                {{ getStatusText(safeGet(order, 'status') || safeGet(order, 'statusText')) }}
              </span>
          </td>
          <td class="px-6 py-4 text-sm text-gray-900">
            {{ formatDate(safeGet(order, 'orderDate') || safeGet(order, 'dateCreation')) }}
          </td>
          <td class="px-6 py-4 text-sm text-gray-900">
            {{ formatTime(safeGet(order, 'estimatedTimeMinutes')) }}
          </td>
          <td class="px-6 py-4 text-sm">
            <button @click="viewDetails(order)" class="bg-gray-600 text-white px-3 py-1 rounded text-sm hover:bg-gray-700 mr-2">
              Details
            </button>
            <button @click="scheduleOrder(order)" class="bg-blue-600 text-white px-3 py-1 rounded text-sm hover:bg-blue-700">
              Schedule
            </button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

// ========== REACTIVE DATA ==========
const orders = ref([])
const loading = ref(false)
const error = ref('')
const debugMode = ref(false)

// ========== COMPUTED PROPERTIES ==========
const filteredOrders = computed(() => {
  return orders.value || []
})

// ========== UTILITY FUNCTIONS ==========

/**
 * Safe property getter to avoid undefined errors
 */
const safeGet = (obj, property) => {
  try {
    return obj && obj[property] !== undefined ? obj[property] : null
  } catch (e) {
    console.warn(`Error accessing property ${property}:`, e)
    return null
  }
}

/**
 * Get CSS class for priority display
 */
const getPriorityClass = (priority) => {
  const priorityStr = String(priority || 'MEDIUM').toUpperCase()

  const baseClasses = 'px-2 py-1 rounded text-xs font-medium'

  switch (priorityStr) {
    case 'HIGH':
    case 'URGENT':
    case 'HAUTE':
      return `${baseClasses} bg-red-100 text-red-800`
    case 'MEDIUM':
    case 'MOYENNE':
      return `${baseClasses} bg-yellow-100 text-yellow-800`
    case 'LOW':
    case 'NORMAL':
    case 'BASSE':
      return `${baseClasses} bg-green-100 text-green-800`
    default:
      return `${baseClasses} bg-gray-100 text-gray-800`
  }
}

/**
 * Get CSS class for status display
 */
const getStatusClass = (status) => {
  const statusStr = String(status || 'UNKNOWN').toUpperCase()

  const baseClasses = 'px-2 py-1 rounded text-xs font-medium'

  switch (statusStr) {
    case 'PENDING':
    case 'EN_ATTENTE':
    case '1':
      return `${baseClasses} bg-blue-100 text-blue-800`
    case 'IN_PROGRESS':
    case 'EN_COURS':
    case '2':
      return `${baseClasses} bg-yellow-100 text-yellow-800`
    case 'COMPLETED':
    case 'TERMINE':
    case '3':
      return `${baseClasses} bg-green-100 text-green-800`
    case 'CANCELLED':
    case 'ANNULE':
    case '4':
      return `${baseClasses} bg-red-100 text-red-800`
    default:
      return `${baseClasses} bg-gray-100 text-gray-800`
  }
}

/**
 * Get human-readable status text
 */
const getStatusText = (status) => {
  const statusStr = String(status || 'UNKNOWN').toUpperCase()

  switch (statusStr) {
    case 'PENDING':
    case 'EN_ATTENTE':
    case '1':
      return 'Pending'
    case 'IN_PROGRESS':
    case 'EN_COURS':
    case '2':
      return 'In Progress'
    case 'COMPLETED':
    case 'TERMINE':
    case '3':
      return 'Completed'
    case 'CANCELLED':
    case 'ANNULE':
    case '4':
      return 'Cancelled'
    default:
      return 'Unknown'
  }
}

/**
 * Format date for display
 */
const formatDate = (date) => {
  if (!date) return 'N/A'

  try {
    const dateObj = new Date(date)
    if (isNaN(dateObj.getTime())) return 'Invalid Date'

    return dateObj.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  } catch (e) {
    console.warn('Error formatting date:', e)
    return 'Invalid Date'
  }
}

/**
 * Format time duration for display
 */
const formatTime = (minutes) => {
  if (!minutes || isNaN(minutes)) return 'N/A'

  const mins = parseInt(minutes)
  if (mins < 60) {
    return `${mins}min`
  } else {
    const hours = Math.floor(mins / 60)
    const remainingMins = mins % 60
    return remainingMins > 0 ? `${hours}h ${remainingMins}min` : `${hours}h`
  }
}

// ========== API FUNCTIONS ==========
const loadOrders = async () => {
  loading.value = true
  error.value = ''

  try {
    console.log('📋 Loading orders from API...')

    const response = await fetch('/api/orders/frontend/orders')

    if (!response.ok) {
      const errorText = await response.text()
      console.error('❌ Server error:', response.status, errorText)
      throw new Error(`Server returned ${response.status}: ${errorText}`)
    }

    const contentType = response.headers.get('content-type')
    if (!contentType || !contentType.includes('application/json')) {
      const text = await response.text()
      console.error('❌ Response is not JSON:', text)
      throw new Error('Server did not return JSON data')
    }

    const data = await response.json()

    if (Array.isArray(data)) {
      orders.value = data
      console.log('✅ Orders loaded successfully:', data.length, 'orders')
      console.log('📋 First order data:', data[0])
    } else {
      console.error('❌ Response is not an array:', data)
      orders.value = []
    }

  } catch (err) {
    console.error('❌ Error loading orders:', err)
    error.value = err.message || 'Failed to load orders'
    orders.value = []
  } finally {
    loading.value = false
  }
}

// ========== ACTION FUNCTIONS ==========
const toggleDebug = () => {
  debugMode.value = !debugMode.value
}

const createNewOrder = () => {
  console.log('📝 Creating new order...')
  alert('Create new order functionality will be implemented')
}

const viewDetails = (order) => {
  console.log('👁️ Viewing order details:', order.id)
  alert(`View details for order: ${safeGet(order, 'orderNumber') || safeGet(order, 'numeroCommande')}`)
}

const scheduleOrder = (order) => {
  console.log('📅 Scheduling order:', order.id)
  alert(`Schedule order: ${safeGet(order, 'orderNumber') || safeGet(order, 'numeroCommande')}`)
}

// ========== LIFECYCLE ==========
onMounted(() => {
  loadOrders()
})
</script>
