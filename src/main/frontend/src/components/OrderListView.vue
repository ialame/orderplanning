<template>
  <div class="p-6">
    <!-- Header -->
    <div class="flex justify-between items-center mb-6">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">📋 Order Management</h1>
        <p class="text-gray-600 mt-1">View and manage all Pokemon card orders</p>
      </div>
      <div class="flex gap-3">
        <button
          @click="loadOrders"
          :disabled="loading"
          class="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
        >
          <svg v-if="loading" class="animate-spin h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
          </svg>
          <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
          </svg>
          {{ loading ? 'Loading...' : 'Refresh Orders' }}
        </button>
        <button
          @click="debugWorkingEndpoint"
          class="flex items-center gap-2 bg-orange-600 text-white px-4 py-2 rounded-lg hover:bg-orange-700 transition-colors"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path>
          </svg>
          Debug Endpoint
        </button>
        <button
          @click="testBackend"
          class="flex items-center gap-2 bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-colors"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
          </svg>
          Test Backend
        </button>
      </div>
    </div>

    <!-- Status -->
    <div class="bg-white rounded-lg shadow p-6 mb-6">
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-lg font-semibold text-gray-900">System Status</h2>
          <p class="text-gray-600">Backend connection and API availability</p>
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
    </div>

    <!-- Debug Information -->
    <div class="bg-white rounded-lg shadow p-6 mb-6">
      <h2 class="text-lg font-semibold text-gray-900 mb-4">🔧 Debug Information</h2>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <h3 class="font-medium text-gray-900 mb-2">Frontend Status</h3>
          <ul class="text-sm text-gray-600 space-y-1">
            <li>✅ Vue.js application loaded</li>
            <li>✅ Component rendered successfully</li>
            <li>✅ Navigation working</li>
            <li>{{ orders.length > 0 ? '✅' : '❌' }} Orders data loaded ({{ orders.length }} orders)</li>
          </ul>
        </div>

        <div>
          <h3 class="font-medium text-gray-900 mb-2">Order Endpoints</h3>
          <ul class="text-sm text-gray-600 space-y-1">
            <li>📡 /api/orders/frontend/orders - {{ endpoints.orders }}</li>
            <li>📡 /api/commandes/frontend/commandes - {{ endpoints.commandes }}</li>
            <li>📡 /api/frontend/commandes - {{ endpoints.frontend }}</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Orders Table -->
    <div class="bg-white rounded-lg shadow overflow-hidden">
      <div class="px-6 py-4 border-b border-gray-200">
        <h2 class="text-lg font-semibold text-gray-900">Orders ({{ orders.length }} total)</h2>
      </div>

      <div v-if="loading" class="p-8 text-center">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-4"></div>
        <p>Loading orders...</p>
      </div>

      <div v-else-if="orders.length === 0" class="p-8 text-center">
        <svg class="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V9a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"></path>
        </svg>
        <h3 class="text-lg font-medium text-gray-900 mb-2">No Orders Found</h3>
        <p class="text-gray-600 mb-4">Backend connected but no orders found in database.</p>
        <div class="space-x-2">
          <button
            @click="loadOrders"
            class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
          >
            Refresh Orders
          </button>
          <button
            @click="debugWorkingEndpoint"
            class="bg-orange-600 text-white px-4 py-2 rounded-lg hover:bg-orange-700"
          >
            Debug Raw Response
          </button>
        </div>
      </div>

      <div v-else>
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Order ID</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Cards</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Priority</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Date</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Duration</th>
          </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="order in orders" :key="order.id" class="hover:bg-gray-50 transition-colors">
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm font-medium text-gray-900">{{ order.orderNumber }}</div>
              <div class="text-sm text-gray-500">{{ order.id.slice(-8) }}</div>
            </td>

            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm text-gray-900">{{ order.cardCount }} cards</div>
            </td>

            <td class="px-6 py-4 whitespace-nowrap">
                <span :class="[
                  'inline-flex px-2 py-1 text-xs font-semibold rounded-full',
                  order.priority === 'URGENT' ? 'bg-red-100 text-red-800' :
                  order.priority === 'HIGH' ? 'bg-orange-100 text-orange-800' :
                  order.priority === 'MEDIUM' ? 'bg-yellow-100 text-yellow-800' :
                  'bg-green-100 text-green-800'
                ]">
                  {{ order.priority }}
                </span>
            </td>

            <td class="px-6 py-4 whitespace-nowrap">
                <span :class="[
                  'inline-flex px-2 py-1 text-xs font-semibold rounded-full',
                  order.status === 'COMPLETED' ? 'bg-green-100 text-green-800' :
                  order.status === 'IN_PROGRESS' ? 'bg-blue-100 text-blue-800' :
                  order.status === 'CANCELLED' ? 'bg-red-100 text-red-800' :
                  'bg-gray-100 text-gray-800'
                ]">
                  {{ order.status }}
                </span>
            </td>

            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
              {{ formatDate(order.createdDate) }}
            </td>

            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
              {{ formatDuration(order.estimatedDuration) }}
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

// ========== INTERFACES ==========
interface Order {
  id: string
  orderNumber: string
  cardCount: number
  priority: string
  status: string
  createdDate: string
  estimatedDuration: number
  actualDuration?: number
  totalPrice?: number
}

// ========== EMITS ==========
const emit = defineEmits(['show-notification'])

// ========== STATE ==========
const loading = ref(false)
const orders = ref<Order[]>([])
const backendStatus = ref<'testing' | 'connected' | 'error'>('testing')
const statusMessage = ref('')

const endpoints = ref({
  orders: '❓ Not tested',
  commandes: '❓ Not tested',
  frontend: '❓ Not tested'
})

// ========== METHODS ==========

/**
 * Debug the working endpoint specifically
 */
const debugWorkingEndpoint = async () => {
  try {
    console.log('🔍 === DEBUGGING WORKING ENDPOINT ===')
    console.log('🔍 Calling: /api/orders/frontend/orders')

    const response = await fetch('/api/orders/frontend/orders')
    console.log('🔍 Response status:', response.status)
    console.log('🔍 Response headers:', Object.fromEntries(response.headers.entries()))

    if (response.ok) {
      const text = await response.text()
      console.log('🔍 Raw response text:', text)

      try {
        const json = JSON.parse(text)
        console.log('🔍 Parsed JSON:', json)
        console.log('🔍 JSON type:', typeof json)
        console.log('🔍 Is array:', Array.isArray(json))
        console.log('🔍 Length:', json?.length)

        if (Array.isArray(json) && json.length > 0) {
          console.log('🔍 First item:', json[0])
          console.log('🔍 All keys in first item:', Object.keys(json[0]))
        }

        alert(`Found ${json?.length || 0} orders in backend response. Check console for details.`)

      } catch (parseError) {
        console.error('❌ JSON parse error:', parseError)
        alert('Response is not valid JSON. Check console.')
      }
    } else {
      console.error('❌ HTTP Error:', response.status, response.statusText)
      alert(`HTTP Error: ${response.status} ${response.statusText}`)
    }

  } catch (error) {
    console.error('❌ Network error:', error)
    alert(`Network error: ${error.message}`)
  }
}

/**
 * Test backend connectivity
 */
const testBackend = async () => {
  backendStatus.value = 'testing'
  statusMessage.value = 'Testing backend connection...'

  const tests = [
    { name: 'orders', url: '/api/orders/frontend/orders' },
    { name: 'commandes', url: '/api/commandes/frontend/commandes' },
    { name: 'frontend', url: '/api/frontend/commandes' }
  ]

  let successCount = 0

  for (const test of tests) {
    try {
      const response = await fetch(test.url)
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
    emit('show-notification', {
      message: 'Backend connection test completed',
      details: `${successCount} out of ${tests.length} endpoints are working`,
      type: 'success'
    })
  } else {
    backendStatus.value = 'error'
    statusMessage.value = '❌ Backend not accessible. Please check if Spring Boot server is running on port 8080.'
    emit('show-notification', {
      message: 'Backend connection failed',
      details: 'Please start your Spring Boot server and try again',
      type: 'error'
    })
  }
}

/**
 * Load orders from backend
 */
const loadOrders = async () => {
  loading.value = true

  try {
    console.log('🔍 === LOADING ORDERS ===')

    // Try multiple endpoints in order of preference
    const endpoints = [
      '/api/orders/frontend/orders',
      '/api/commandes/frontend/commandes',
      '/api/frontend/commandes'
    ]

    let response = null
    let usedEndpoint = ''

    for (const endpoint of endpoints) {
      try {
        console.log(`🔄 Trying endpoint: ${endpoint}`)
        response = await fetch(endpoint)

        if (response.ok) {
          usedEndpoint = endpoint
          console.log(`✅ Success with endpoint: ${endpoint}`)
          break
        } else {
          console.log(`❌ Failed endpoint ${endpoint}: HTTP ${response.status}`)
        }
      } catch (error) {
        console.log(`❌ Failed endpoint ${endpoint}:`, error.message)
        continue
      }
    }

    if (!response || !response.ok) {
      throw new Error('All order endpoints failed')
    }

    const data = await response.json()
    console.log('🔍 Raw data from backend:', data)
    console.log('🔍 Data type:', typeof data, 'Is array:', Array.isArray(data))

    if (Array.isArray(data)) {
      console.log(`📊 Found ${data.length} orders in response`)

      if (data.length > 0) {
        console.log('🔍 First order structure:', data[0])
      }

      // Transform data to consistent format
      orders.value = data.map(order => {
        console.log('🔄 Processing order:', order)

        return {
          id: order.id || `unknown-${Math.random()}`,
          orderNumber: order.orderNumber || order.numeroCommande || order.num_commande || `ORD-${order.id?.slice(-6) || 'XXX'}`,
          cardCount: order.cardCount || order.nombreCartes || order.card_count || order.nombreCartesExactes || 0,
          priority: mapPriority(order.priority || order.priorite || order.delai),
          status: mapStatus(order.status || order.statut),
          createdDate: order.createdDate || order.dateCreation || order.date || order.dateReception || new Date().toISOString().split('T')[0],
          estimatedDuration: order.estimatedDuration || order.tempsEstimeMinutes || order.dureeEstimeeMinutes || order.estimatedTimeMinutes || 0,
          actualDuration: order.actualDuration,
          totalPrice: order.totalPrice || order.prixTotal || order.total_price || 0
        }
      })

      if (orders.value.length === 0) {
        console.log('⚠️ No orders after transformation - data might be empty')
      } else {
        console.log(`✅ Successfully processed ${orders.value.length} orders`)
      }

    } else {
      console.log('⚠️ Response is not an array:', data)
      orders.value = []
    }

    if (orders.value.length > 0) {
      emit('show-notification', {
        message: 'Real orders loaded successfully!',
        details: `Found ${orders.value.length} orders from ${usedEndpoint}`,
        type: 'success'
      })
    } else {
      console.log('⚠️ No orders found in database')
      emit('show-notification', {
        message: 'No orders found in database',
        details: 'Backend connected but no orders found.',
        type: 'warning'
      })
    }

  } catch (error) {
    console.error('❌ Error loading orders:', error)
    emit('show-notification', {
      message: 'Error loading orders',
      details: error.message,
      type: 'error'
    })
  } finally {
    loading.value = false
  }
}

// ========== UTILITY FUNCTIONS ==========

// Helper functions to map backend data to frontend format
const mapPriority = (priority: any): string => {
  if (!priority) return 'MEDIUM'

  const p = String(priority).toUpperCase()

  // Map French priorities to English
  if (p.includes('URGENT') || p === 'X') return 'URGENT'
  if (p.includes('HAUTE') || p.includes('HIGH') || p === 'F+' || p === 'F') return 'HIGH'
  if (p.includes('BASSE') || p.includes('LOW') || p === 'C') return 'LOW'

  return 'MEDIUM'
}

const mapStatus = (status: any): string => {
  if (!status) return 'PENDING'

  if (typeof status === 'number') {
    switch (status) {
      case 1: return 'PENDING'
      case 2: return 'IN_PROGRESS'
      case 3: return 'COMPLETED'
      case 4: return 'CANCELLED'
      default: return 'PENDING'
    }
  }

  const s = String(status).toUpperCase()
  if (s.includes('PENDING') || s.includes('ATTENTE')) return 'PENDING'
  if (s.includes('PROGRESS') || s.includes('COURS')) return 'IN_PROGRESS'
  if (s.includes('COMPLETED') || s.includes('TERMINE')) return 'COMPLETED'
  if (s.includes('CANCELLED') || s.includes('ANNULE')) return 'CANCELLED'

  return 'PENDING'
}

const formatDate = (dateStr?: string): string => {
  if (!dateStr) return 'N/A'

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

const formatDuration = (minutes?: number): string => {
  if (!minutes) return 'N/A'

  const hours = Math.floor(minutes / 60)
  const remainingMinutes = minutes % 60

  if (hours > 0) {
    return `${hours}h ${remainingMinutes}m`
  }
  return `${remainingMinutes}m`
}

// ========== LIFECYCLE ==========
onMounted(async () => {
  console.log('📋 OrderListView mounted - Loading orders automatically...')

  // Automatically test backend and load orders on mount
  await testBackend()
  await loadOrders()
})
</script>

<style scoped>
/* Component specific styles */
.transition-colors {
  transition-property: color, background-color, border-color;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 150ms;
}
</style>
