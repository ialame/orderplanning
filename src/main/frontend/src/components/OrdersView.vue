<!-- =============================================== -->
<!-- ORDERSVIEW.VUE - FIXED FOR REAL DATA (ENGLISH) -->
<!-- =============================================== -->

<template>
  <div class="orders-view">
    <!-- ✅ HEADER -->
    <div class="bg-white rounded-lg shadow p-6 mb-6">
      <div class="flex justify-between items-center mb-4">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">📦 Orders Management</h1>
          <p class="text-gray-600">Real orders from Pokemon card database</p>
        </div>

        <div class="flex gap-3">
          <button
            @click="refreshOrders"
            :disabled="loading"
            class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50 flex items-center gap-2"
          >
            <span v-if="loading">🔄</span>
            <span v-else>🔄</span>
            {{ loading ? 'Loading...' : 'Refresh' }}
          </button>

          <button
            @click="debugWorkingEndpoint"
            class="bg-orange-600 text-white px-4 py-2 rounded-lg hover:bg-orange-700"
          >
            🔧 Debug API
          </button>
        </div>
      </div>

      <!-- ✅ QUICK STATISTICS -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div class="bg-blue-50 p-4 rounded-lg">
          <div class="text-2xl font-bold text-blue-600">{{ statistics.total }}</div>
          <div class="text-sm text-blue-800">Total Orders</div>
        </div>
        <div class="bg-yellow-50 p-4 rounded-lg">
          <div class="text-2xl font-bold text-yellow-600">{{ statistics.pending }}</div>
          <div class="text-sm text-yellow-800">Pending</div>
        </div>
        <div class="bg-green-50 p-4 rounded-lg">
          <div class="text-2xl font-bold text-green-600">{{ statistics.completed }}</div>
          <div class="text-sm text-green-800">Completed</div>
        </div>
        <div class="bg-purple-50 p-4 rounded-lg">
          <div class="text-2xl font-bold text-purple-600">{{ statistics.totalCards }}</div>
          <div class="text-sm text-purple-800">Total Cards</div>
        </div>
      </div>
    </div>

    <!-- ✅ CONNECTION STATUS -->
    <div class="bg-white rounded-lg shadow p-6 mb-6">
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-lg font-semibold text-gray-900">Backend Connection</h2>
          <p class="text-sm text-gray-600">API endpoints status</p>
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

      <!-- Endpoint status details -->
      <div v-if="backendStatus !== 'testing'" class="mt-4 grid grid-cols-1 md:grid-cols-5 gap-3">
        <div class="flex items-center justify-between p-2 bg-gray-50 rounded">
          <span class="text-sm text-gray-600">/api/commandes/juin-2025</span>
          <span class="text-xs">{{ endpoints.june2025 }}</span>
        </div>
        <div class="flex items-center justify-between p-2 bg-gray-50 rounded">
          <span class="text-sm text-gray-600">/api/frontend/...planification</span>
          <span class="text-xs">{{ endpoints.planning }}</span>
        </div>
        <div class="flex items-center justify-between p-2 bg-gray-50 rounded">
          <span class="text-sm text-gray-600">/api/dashboard/orders</span>
          <span class="text-xs">{{ endpoints.dashboard }}</span>
        </div>
        <div class="flex items-center justify-between p-2 bg-gray-50 rounded">
          <span class="text-sm text-gray-600">/api/orders</span>
          <span class="text-xs">{{ endpoints.orders }}</span>
        </div>
        <div class="flex items-center justify-between p-2 bg-gray-50 rounded">
          <span class="text-sm text-gray-600">/api/orders/frontend</span>
          <span class="text-xs">{{ endpoints.frontend }}</span>
        </div>
      </div>
    </div>

    <!-- ✅ FILTERS -->
    <div class="bg-white rounded-lg shadow p-6 mb-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Search</label>
          <input
            v-model="searchTerm"
            type="text"
            placeholder="Order number..."
            class="w-full border border-gray-300 rounded-md px-3 py-2"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Status</label>
          <select v-model="filterStatus" class="w-full border border-gray-300 rounded-md px-3 py-2">
            <option value="all">All Statuses</option>
            <option value="PENDING">Pending</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="COMPLETED">Completed</option>
          </select>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Priority</label>
          <select v-model="filterPriority" class="w-full border border-gray-300 rounded-md px-3 py-2">
            <option value="all">All Priorities</option>
            <option value="URGENT">Urgent</option>
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>
        </div>

        <div class="flex items-end">
          <button
            @click="loadOrders"
            class="w-full bg-gray-600 text-white px-4 py-2 rounded-md hover:bg-gray-700"
          >
            🔄 Reload Data
          </button>
        </div>
      </div>
    </div>

    <!-- ✅ LOADING STATE -->
    <div v-if="loading" class="text-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
      <span class="text-gray-600 mt-3 block">Loading real orders from database...</span>
    </div>

    <!-- ✅ ORDERS TABLE -->
    <div v-else-if="filteredOrders.length > 0" class="bg-white rounded-lg shadow overflow-hidden">
      <div class="px-6 py-4 border-b border-gray-200">
        <h2 class="text-lg font-semibold text-gray-900">
          📋 {{ filteredOrders.length }} Order{{ filteredOrders.length > 1 ? 's' : '' }}
        </h2>
      </div>

      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Order
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Date
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Cards
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Quality
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Priority
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Status
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Duration
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Actions
            </th>
          </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="order in filteredOrders" :key="order.id" class="hover:bg-gray-50">
            <!-- Order -->
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center">
                <div>
                  <div class="text-sm font-medium text-gray-900">{{ order.orderNumber }}</div>
                  <div class="text-sm text-gray-500">ID: {{ order.id }}</div>
                </div>
              </div>
            </td>

            <!-- Date -->
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm text-gray-900">{{ formatDate(order.createdDate) }}</div>
            </td>

            <!-- Cards -->
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm text-gray-900">{{ order.cardCount }} cards</div>
              <div class="text-sm text-gray-500">
                {{ order.cardsWithName || 0 }} with names ({{ order.namePercentage || 0 }}%)
                <span :class="(order.namePercentage || 0) >= 95 ? 'text-xs text-green-600' : 'text-xs text-orange-600'">
                    {{ (order.namePercentage || 0) >= 95 ? '✅' : '⚠️' }}
                  </span>
              </div>
            </td>

            <!-- Quality -->
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="text-2xl">{{ getQualityIndicator(order.namePercentage || 0) }}</span>
            </td>

            <!-- Priority -->
            <td class="px-6 py-4 whitespace-nowrap">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                      :class="getPriorityColor(order.priority)">
                  {{ getPriorityLabel(order.priority) }}
                </span>
            </td>

            <!-- Status -->
            <td class="px-6 py-4 whitespace-nowrap">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                      :class="getStatusColor(order.status)">
                  {{ getStatusLabel(order.status) }}
                </span>
            </td>

            <!-- Duration -->
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm text-gray-900">{{ order.estimatedDuration || 0 }} min</div>
              <div class="text-xs text-gray-500">{{ formatDuration(order.estimatedDuration || 0) }}</div>
            </td>

            <!-- Actions -->
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
              <button
                @click="viewDetails(order)"
                class="text-blue-600 hover:text-blue-900"
                title="View details"
              >
                👁️
              </button>
              <button
                @click="viewCards(order)"
                class="text-green-600 hover:text-green-900"
                title="View cards"
              >
                🃏
              </button>
              <button
                v-if="order.status === 'PENDING'"
                @click="startOrder(order.id!)"
                class="text-purple-600 hover:text-purple-900"
                title="Start"
              >
                ▶️
              </button>
              <button
                v-if="order.status === 'IN_PROGRESS'"
                @click="completeOrder(order.id!)"
                class="text-green-600 hover:text-green-900"
                title="Complete"
              >
                ✅
              </button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ✅ EMPTY STATE -->
    <div v-else class="text-center py-12">
      <div class="text-gray-500">
        <div class="text-4xl mb-4">📦</div>
        <div>{{ loading ? 'Loading...' : 'No orders found' }}</div>
        <button
          v-if="!loading"
          @click="loadOrders"
          class="mt-4 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
        >
          🔄 Reload
        </button>
      </div>
    </div>

    <!-- ✅ MODALS (simple for now) -->

    <!-- Details Modal -->
    <div v-if="showDetailsModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg max-w-2xl w-full mx-4 max-h-96 overflow-y-auto">
        <div class="p-6">
          <div class="flex justify-between items-center mb-4">
            <h3 class="text-lg font-semibold">Details - {{ selectedOrder?.orderNumber }}</h3>
            <button @click="showDetailsModal = false" class="text-gray-400 hover:text-gray-600">✕</button>
          </div>

          <div v-if="selectedOrder" class="space-y-3">
            <p><strong>ID:</strong> {{ selectedOrder.id }}</p>
            <p><strong>Created Date:</strong> {{ selectedOrder.createdDate }}</p>
            <p><strong>Card Count:</strong> {{ selectedOrder.cardCount }}</p>
            <p><strong>Cards with Names:</strong> {{ selectedOrder.cardsWithName }} ({{ selectedOrder.namePercentage }}%)</p>
            <p><strong>Priority:</strong> {{ selectedOrder.priority }}</p>
            <p><strong>Status:</strong> {{ selectedOrder.status }}</p>
            <p><strong>Estimated Duration:</strong> {{ selectedOrder.estimatedDuration }} minutes</p>
            <p><strong>Total Price:</strong> ${{ selectedOrder.totalPrice || 0 }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Cards Modal -->
    <div v-if="showCardsModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg max-w-4xl w-full mx-4 max-h-96 overflow-y-auto">
        <div class="p-6">
          <div class="flex justify-between items-center mb-4">
            <h3 class="text-lg font-semibold">Cards - {{ selectedOrder?.orderNumber }}</h3>
            <button @click="showCardsModal = false" class="text-gray-400 hover:text-gray-600">✕</button>
          </div>

          <div v-if="loadingCards" class="text-center py-8">
            <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
            <span class="text-gray-600 mt-2 block">Loading cards...</span>
          </div>

          <div v-else-if="orderCards" class="space-y-3">
            <p><strong>Total Cards:</strong> {{ orderCards.cardCount }}</p>
            <p><strong>With Names:</strong> {{ orderCards.cardsWithName }}</p>
            <p><strong>Percentage:</strong> {{ orderCards.namePercentage }}%</p>
            <p class="text-sm text-gray-600">Individual card details available via API</p>
          </div>

          <div v-else class="text-gray-500 text-center py-8">
            No card data available
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, inject } from 'vue'

// ========== INTERFACES ==========
interface Order {
  id?: number | string
  orderNumber: string
  createdDate: string
  cardCount: number
  cardsWithName?: number
  namePercentage?: number
  priority: string
  totalPrice?: number
  status: string
  estimatedDuration?: number
  actualDuration?: number
}

// ========== STATE ==========
const loading = ref(false)
const orders = ref<Order[]>([])
const selectedOrder = ref<Order | null>(null)
const showDetailsModal = ref(false)
const showCardsModal = ref(false)
const orderCards = ref<any>(null)
const loadingCards = ref(false)

// Backend status
const backendStatus = ref<'testing' | 'connected' | 'error'>('testing')
const statusMessage = ref('')
const endpoints = ref({
  june2025: '❓ Not tested',
  planning: '❓ Not tested',
  dashboard: '❓ Not tested',
  orders: '❓ Not tested',
  frontend: '❓ Not tested'
})

// Filters
const filterStatus = ref('all')
const filterPriority = ref('all')
const searchTerm = ref('')

// ✅ NEW: Date filtering with configurable date
const enableDateFilter = ref(true)
const filterFromDate = ref('2025-06-01') // Default to June 1, 2025
const allOrders = ref<Order[]>([]) // Store all orders without filtering

// Injection
const showNotification = inject('showNotification') as ((message: string, type?: 'success' | 'error') => void) | undefined

// ========== COMPUTED ==========
const filteredOrders = computed(() => {
  let filtered = [...orders.value]

  // ✅ Date filter (applied first)
  if (enableDateFilter.value && filterFromDate.value) {
    filtered = filtered.filter(order => {
      const orderDate = order.createdDate || order.orderDate || order.date
      if (!orderDate) return true // Keep orders without date

      // Extract date part and compare
      const dateOnly = orderDate.split('T')[0]
      return dateOnly >= filterFromDate.value
    })
  }

  // Filter by status
  if (filterStatus.value !== 'all') {
    filtered = filtered.filter(order => order.status === filterStatus.value)
  }

  // Filter by priority
  if (filterPriority.value !== 'all') {
    filtered = filtered.filter(order => order.priority === filterPriority.value)
  }

  // Search filter
  if (searchTerm.value) {
    const term = searchTerm.value.toLowerCase()
    filtered = filtered.filter(order =>
      order.orderNumber.toLowerCase().includes(term) ||
      order.id?.toString().toLowerCase().includes(term)
    )
  }

  return filtered
})

const statistics = computed(() => {
  const total = orders.value.length
  const filtered = filteredOrders.value.length
  const pending = filteredOrders.value.filter(o => o.status === 'PENDING').length
  const inProgress = filteredOrders.value.filter(o => o.status === 'IN_PROGRESS').length
  const completed = filteredOrders.value.filter(o => o.status === 'COMPLETED').length
  const totalCards = filteredOrders.value.reduce((sum, o) => sum + o.cardCount, 0)

  return {
    total: filtered,
    totalInDb: total,
    pending,
    inProgress,
    completed,
    totalCards
  }
})

// ========== METHODS ==========

/**
 * 📦 LOAD REAL ORDERS FROM DATABASE (USING /api/orders)
 */
const loadOrders = async () => {
  loading.value = true
  try {
    console.log('📦 Loading real orders from /api/orders...')

    const response = await fetch('http://localhost:8080/api/orders', {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    })

    if (response.ok) {
      const data = await response.json()
      console.log('✅ Orders from /api/orders retrieved:', data.length)
      console.log('📊 Sample order:', data[0])

      if (Array.isArray(data) && data.length > 0) {
        // Map all orders first
        const allMappedOrders = data.map(mapOrderFromApi).filter(order => order !== null)

        // Then filter for June 2025+ if needed
        const filteredOrders = allMappedOrders.filter(order => {
          // Check if order has a creation date >= 2025-06-01
          const orderDate = order.creationDate || order.orderDate || order.date
          if (!orderDate) {
            console.log('📅 Order without date, keeping:', order.orderNumber)
            return true // Keep orders without date (might be valid)
          }

          // Since your data is from July 2025, let's include all orders from 2025-06-01 onwards
          // Extract just the date part (remove time)
          const dateOnly = orderDate.split('T')[0] // Gets "2025-07-04" from "2025-07-04T00:59:38.000+00:00"
          const isJune2025Plus = dateOnly >= '2025-06-01'

          if (!isJune2025Plus) {
            console.log('📅 Filtering out order from', dateOnly, ':', order.orderNumber)
          } else {
            console.log('📅 Keeping order from', dateOnly, ':', order.orderNumber)
          }
          return isJune2025Plus
        })

        orders.value = filteredOrders

        console.log(`📊 Total orders: ${allMappedOrders.length}`)
        console.log(`📊 Orders since June 1, 2025: ${filteredOrders.length}`)
        console.log('📊 Sample filtered order:', filteredOrders[0])

        showNotification?.(`✅ ${filteredOrders.length}/${allMappedOrders.length} orders loaded (since June 1, 2025)`, 'success')
        return
      }
    }

    throw new Error('Failed to load orders from /api/orders')

  } catch (error) {
    console.error('❌ Error loading orders:', error)
    showNotification?.('❌ Error loading orders from database', 'error')
    orders.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 🔄 Map API data to frontend format
 */
const mapOrderFromApi = (order: any): Order => {
  // Handle different date formats and ensure we're using the right date fields
  let orderDate = order.creationDate || order.orderDate || order.date_creation ||
    order.dateReception || order.date_reception || order.date || '2025-06-01'

  // Extract date part if it's a timestamp (remove time part)
  if (orderDate.includes('T')) {
    orderDate = orderDate.split('T')[0] // "2025-07-04T00:59:38.000+00:00" -> "2025-07-04"
  }

  return {
    id: order.id,
    orderNumber: order.orderNumber || order.numeroCommande || order.numero_commande ||
      order.num_commande || `ORD-${order.id}`,
    createdDate: orderDate,
    cardCount: order.cardCount || order.nombreCartes || order.nombre_cartes || 1,
    cardsWithName: order.cardsWithName || order.nombreAvecNom || order.nombre_avec_nom || 0,
    namePercentage: order.namePercentage || order.pourcentageAvecNom || order.pourcentage_avec_nom || 0,
    priority: mapPriority(order.priority || order.priorite || 'MEDIUM'),
    totalPrice: order.totalPrice || order.prixTotal || order.prix_total || 0,
    status: mapStatus(order.status || order.statut || 'PENDING'),
    estimatedDuration: order.estimatedDuration || order.estimatedTimeMinutes || order.dureeEstimeeMinutes ||
      order.duree_estimee_minutes || order.tempsEstimeMinutes || order.temps_estime_minutes ||
      ((order.cardCount || order.nombreCartes || 1) * 3),
    actualDuration: order.actualDuration || order.dureeActuelle || order.duree_actuelle
  }
}

/**
 * 🔧 Utility mapping functions
 */
const mapPriority = (priority: any): string => {
  if (!priority) return 'MEDIUM'

  const p = String(priority).toUpperCase()
  if (p.includes('URGENT') || p === 'X') return 'URGENT'
  if (p.includes('HIGH') || p.includes('HAUTE') || p === 'F+' || p === 'F') return 'HIGH'
  if (p.includes('LOW') || p.includes('BASSE') || p === 'C') return 'LOW'

  return 'MEDIUM'
}

const mapStatus = (status: any): string => {
  if (typeof status === 'number') {
    switch (status) {
      case 1: return 'PENDING'
      case 2: return 'SCHEDULED'
      case 3: return 'IN_PROGRESS'
      case 4: return 'COMPLETED'
      case 5: return 'CANCELLED'
      default: return 'PENDING'
    }
  }

  if (typeof status === 'string') {
    const s = status.toUpperCase()
    if (s.includes('PENDING') || s.includes('ATTENTE')) return 'PENDING'
    if (s.includes('SCHEDULED') || s.includes('PLANIFI')) return 'SCHEDULED'
    if (s.includes('PROGRESS') || s.includes('COURS')) return 'IN_PROGRESS'
    if (s.includes('COMPLETED') || s.includes('TERMINE')) return 'COMPLETED'
    if (s.includes('CANCELLED') || s.includes('ANNULE')) return 'CANCELLED'

    // Direct mapping for your exact status values
    if (s === 'PENDING') return 'PENDING'
    if (s === 'IN_PROGRESS') return 'IN_PROGRESS'
    if (s === 'COMPLETED') return 'COMPLETED'
  }

  return 'PENDING'
}

/**
 * 🔄 Refresh orders
 */
const refreshOrders = async () => {
  await loadOrders()
  showNotification?.('🔄 Orders refreshed', 'success')
}

/**
 * 🔧 Debug working endpoint
 */
const debugWorkingEndpoint = async () => {
  try {
    console.log('🔍 === DEBUGGING WORKING ENDPOINT FOR JUNE 2025 ORDERS ===')

    const testEndpoints = [
      '/api/commandes/juin-2025',
      '/api/frontend/commandes-periode-planification',
      '/api/orders/june-2025',
      '/api/orders/planning-period',
      '/api/commandes/frontend/commandes',
      '/api/frontend/commandes'
    ]

    for (const endpoint of testEndpoints) {
      console.log(`🔍 Testing: ${endpoint}`)

      try {
        const response = await fetch(`http://localhost:8080${endpoint}`)
        console.log(`🔍 ${endpoint} - Status:`, response.status)

        if (response.ok) {
          const data = await response.json()
          console.log(`🔍 ${endpoint} - Data:`, data)
          console.log(`🔍 ${endpoint} - Type:`, typeof data, 'Is array:', Array.isArray(data))

          let ordersData = data
          if (data.commandes) {
            ordersData = data.commandes
            console.log(`🔍 ${endpoint} - Found commandes array:`, ordersData.length)
          }

          if (Array.isArray(ordersData)) {
            console.log(`🔍 ${endpoint} - Orders count:`, ordersData.length)

            if (ordersData.length > 0) {
              console.log(`🔍 ${endpoint} - First order:`, ordersData[0])
              console.log(`🔍 ${endpoint} - First order keys:`, Object.keys(ordersData[0]))

              // Check dates
              const firstOrder = ordersData[0]
              const orderDate = firstOrder.date || firstOrder.dateCreation || firstOrder.dateReception
              console.log(`🔍 ${endpoint} - First order date:`, orderDate)
            }

            alert(`✅ ${endpoint} works! Found ${ordersData.length} orders. Check console for details.`)
            return
          }
        }
      } catch (error) {
        console.log(`❌ ${endpoint} failed:`, error.message)
      }
    }

    alert('❌ No working endpoints found. Check console and verify backend is running.')
  } catch (error) {
    console.error('❌ Debug error:', error)
    alert(`Network error: ${error.message}`)
  }
}

/**
 * 👁️ View order details
 */
const viewDetails = (order: Order) => {
  selectedOrder.value = order
  showDetailsModal.value = true
}

/**
 * 🃏 View order cards
 */
const viewCards = async (order: Order) => {
  if (!order.id) {
    showNotification?.('Order ID missing', 'error')
    return
  }

  selectedOrder.value = order
  loadingCards.value = true
  showCardsModal.value = true
  orderCards.value = null

  try {
    console.log('🃏 Loading cards for order:', order.orderNumber)

    // Try to load card details
    const response = await fetch(`/api/orders/${order.id}/cards`)
    if (response.ok) {
      orderCards.value = await response.json()
    } else {
      // Fallback to basic card info
      orderCards.value = {
        cardCount: order.cardCount,
        cardsWithName: order.cardsWithName || 0,
        namePercentage: order.namePercentage || 0
      }
    }
  } catch (error) {
    console.error('❌ Error loading cards:', error)
    showNotification?.('Error loading cards', 'error')
  } finally {
    loadingCards.value = false
  }
}

/**
 * 🔧 Order actions
 */
const startOrder = async (id: string | number) => {
  try {
    // Implement start order logic
    console.log('▶️ Starting order:', id)
    await loadOrders()
    showNotification?.('✅ Order started', 'success')
  } catch (error) {
    console.error('❌ Error starting order:', error)
    showNotification?.('Error starting order', 'error')
  }
}

const completeOrder = async (id: string | number) => {
  try {
    // Implement complete order logic
    console.log('✅ Completing order:', id)
    await loadOrders()
    showNotification?.('✅ Order completed', 'success')
  } catch (error) {
    console.error('❌ Error completing order:', error)
    showNotification?.('Error completing order', 'error')
  }
}

/**
 * 🎨 Style helper functions
 */
const getPriorityColor = (priority: string) => {
  switch (priority?.toUpperCase()) {
    case 'URGENT': return 'bg-red-100 text-red-800'
    case 'HIGH': return 'bg-orange-100 text-orange-800'
    case 'MEDIUM': return 'bg-yellow-100 text-yellow-800'
    case 'LOW': return 'bg-green-100 text-green-800'
    default: return 'bg-gray-100 text-gray-800'
  }
}

const getPriorityLabel = (priority: string) => {
  switch (priority?.toUpperCase()) {
    case 'URGENT': return '🔴 Urgent'
    case 'HIGH': return '🟠 High'
    case 'MEDIUM': return '🟡 Medium'
    case 'LOW': return '🟢 Low'
    default: return '⚪ Unknown'
  }
}

const getStatusColor = (status: string) => {
  switch (status?.toUpperCase()) {
    case 'PENDING': return 'bg-gray-100 text-gray-800'
    case 'SCHEDULED': return 'bg-blue-100 text-blue-800'
    case 'IN_PROGRESS': return 'bg-yellow-100 text-yellow-800'
    case 'COMPLETED': return 'bg-green-100 text-green-800'
    case 'CANCELLED': return 'bg-red-100 text-red-800'
    default: return 'bg-gray-100 text-gray-800'
  }
}

const getStatusLabel = (status: string) => {
  switch (status?.toUpperCase()) {
    case 'PENDING': return 'Pending'
    case 'SCHEDULED': return 'Scheduled'
    case 'IN_PROGRESS': return 'In Progress'
    case 'COMPLETED': return 'Completed'
    case 'CANCELLED': return 'Cancelled'
    default: return 'Unknown'
  }
}

const getQualityIndicator = (percentage: number): string => {
  if (percentage >= 95) return '🟢'
  if (percentage >= 80) return '🟡'
  return '🔴'
}

const formatDate = (dateStr?: string): string => {
  if (!dateStr) return 'N/A'
  try {
    return new Date(dateStr).toLocaleDateString('en-US', {
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

/**
 * 🔧 Test backend connectivity
 */
const testBackend = async () => {
  backendStatus.value = 'testing'
  statusMessage.value = 'Testing backend connection...'

  const tests = [
    { name: 'june2025', url: '/api/commandes/juin-2025' },
    { name: 'planning', url: '/api/frontend/commandes-periode-planification' },
    { name: 'dashboard', url: '/api/dashboard/orders' },
    { name: 'orders', url: '/api/orders' },
    { name: 'frontend', url: '/api/orders/frontend/orders' }
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
  } else {
    backendStatus.value = 'error'
    statusMessage.value = '❌ Backend not accessible. Please check if Spring Boot server is running on port 8080.'
  }
}

// ========== LIFECYCLE ==========
onMounted(async () => {
  console.log('📦 OrdersView mounted - Loading real orders from June 2025...')

  // Test backend and load orders
  await testBackend()
  await loadOrders()

  // Filter orders to show only those from June 1, 2025 onwards
  if (orders.value.length > 0) {
    const filteredCount = orders.value.filter(order => order.createdDate >= '2025-06-01').length
    console.log(`🗓️ Showing ${filteredCount}/${orders.value.length} orders from June 1, 2025 onwards`)
    orders.value = orders.value.filter(order => order.createdDate >= '2025-06-01')
  }
})
</script>

<style scoped>
.orders-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

/* Loading spinner */
.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Hover effects */
.hover\:bg-gray-50:hover {
  background-color: #f9fafb;
}

/* Responsive design */
@media (max-width: 768px) {
  .orders-view {
    padding: 16px;
  }

  .overflow-x-auto {
    font-size: 0.875rem;
  }
}
</style>
