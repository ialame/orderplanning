<template>
  <div class="orders-view">
    <!-- Header -->
    <div class="bg-white rounded-lg shadow-md p-6 mb-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">📦 Orders Management</h1>
          <p class="text-gray-600 mt-1">Planning and processing of Pokemon card orders since June 2025</p>
        </div>
        <div class="flex space-x-3">
          <button
            @click="refreshOrders"
            :disabled="loading"
            class="btn-primary"
          >
            {{ loading ? '⏳ Loading...' : '🔄 Refresh' }}
          </button>
          <button
            @click="showFilters = !showFilters"
            class="btn-secondary"
          >
            {{ showFilters ? 'Hide Filters' : 'Show Filters' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Statistiques -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
      <div class="card">
        <div class="flex items-center">
          <div class="bg-blue-500 rounded-lg p-3 mr-4">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
            </svg>
          </div>
          <div>
            <p class="text-sm text-gray-600">Total Orders</p>
            <p class="text-2xl font-semibold text-gray-900">{{ stats.total }}</p>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="flex items-center">
          <div class="bg-yellow-500 rounded-lg p-3 mr-4">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>
          <div>
            <p class="text-sm text-gray-600">Pending</p>
            <p class="text-2xl font-semibold text-gray-900">{{ stats.pending }}</p>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="flex items-center">
          <div class="bg-orange-500 rounded-lg p-3 mr-4">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
            </svg>
          </div>
          <div>
            <p class="text-sm text-gray-600">In Progress</p>
            <p class="text-2xl font-semibold text-gray-900">{{ stats.processing }}</p>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="flex items-center">
          <div class="bg-green-500 rounded-lg p-3 mr-4">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>
          <div>
            <p class="text-sm text-gray-600">Completed</p>
            <p class="text-2xl font-semibold text-gray-900">{{ stats.completed }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Filtres -->
    <div v-if="showFilters" class="bg-white rounded-lg shadow-md p-6 mb-6">
      <h2 class="text-lg font-semibold text-gray-900 mb-4">Filters</h2>
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Status</label>
          <select v-model="filters.status" class="input-field">
            <option value="">All statuses</option>
            <option value="PENDING">Pending</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Priority</label>
          <select v-model="filters.priority" class="input-field">
            <option value="">All priorities</option>
            <option value="URGENT">Urgent</option>
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Start Date</label>
          <input type="date" v-model="filters.startDate" class="input-field">
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">End Date</label>
          <input type="date" v-model="filters.endDate" class="input-field">
        </div>
      </div>
    </div>

    <!-- Tableau des commandes -->
    <div class="bg-white rounded-lg shadow-md overflow-hidden">
      <div class="px-6 py-4 border-b border-gray-200">
        <h2 class="text-lg font-semibold text-gray-900">
          Orders ({{ filteredOrders.length }} {{ filteredOrders.length === 1 ? 'order' : 'orders' }})
        </h2>
      </div>

      <div v-if="loading" class="p-8 text-center">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
        <p class="text-gray-600">Loading orders...</p>
      </div>

      <div v-else-if="filteredOrders.length === 0" class="p-8 text-center">
        <svg class="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V9a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"></path>
        </svg>
        <h3 class="text-lg font-medium text-gray-900 mb-2">No orders found</h3>
        <p class="text-gray-600 mb-4">No orders match the selected criteria.</p>
      </div>

      <div v-else class="overflow-x-auto">
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
              Estimated Duration
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Actions
            </th>
          </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="order in filteredOrders" :key="order.id" class="hover:bg-gray-50 transition-colors">
            <!-- Commande -->
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center">
                <div>
                  <div class="text-sm font-medium text-gray-900">{{ order.orderNumber }}</div>
                  <div class="text-sm text-gray-500">ID: {{ order.id.slice(-8) }}</div>
                </div>
              </div>
            </td>

            <!-- Date -->
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm text-gray-900">{{ formatDate(order.createdDate) }}</div>
            </td>

            <!-- Cartes -->
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm text-gray-900">{{ order.cardCount }} cards</div>
              <div class="text-sm text-gray-500">
                {{ order.cardsWithName || 0 }} with names ({{ order.namePercentage || 0 }}%)
              </div>
            </td>

            <!-- Quality -->
            <td class="px-6 py-4 whitespace-nowrap">
                <span :class="[
                  'inline-flex px-2 py-1 text-xs font-semibold rounded-full',
                  (order.namePercentage || 0) >= 95 ? 'bg-green-100 text-green-800' : 'bg-orange-100 text-orange-800'
                ]">
                  {{ (order.namePercentage || 0) >= 95 ? '✅ Excellent' : '⚠️ Needs Improvement' }}
                </span>
            </td>

            <!-- Priorité -->
            <td class="px-6 py-4 whitespace-nowrap">
                <span :class="[
                  'inline-flex px-2 py-1 text-xs font-semibold rounded-full',
                  order.priority === 'URGENT' ? 'bg-red-100 text-red-800' :
                  order.priority === 'HIGH' ? 'bg-orange-100 text-orange-800' :
                  order.priority === 'MEDIUM' ? 'bg-yellow-100 text-yellow-800' :
                  'bg-green-100 text-green-800'
                ]">
                  {{ getPriorityLabel(order.priority) }}
                </span>
            </td>

            <!-- Statut -->
            <td class="px-6 py-4 whitespace-nowrap">
                <span :class="[
                  'inline-flex px-2 py-1 text-xs font-semibold rounded-full',
                  order.status === 'COMPLETED' ? 'bg-green-100 text-green-800' :
                  order.status === 'IN_PROGRESS' ? 'bg-blue-100 text-blue-800' :
                  order.status === 'CANCELLED' ? 'bg-red-100 text-red-800' :
                  'bg-gray-100 text-gray-800'
                ]">
                  {{ getStatusLabel(order.status) }}
                </span>
            </td>

            <!-- Durée estimée -->
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm text-gray-900">{{ formatDuration(order.estimatedDuration) }}</div>
              <div class="text-sm text-gray-500">{{ order.cardCount * 3 }}min (3min/carte)</div>
            </td>

            <!-- Actions -->
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium space-x-2">
              <button
                @click="viewDetails(order)"
                class="text-blue-600 hover:text-blue-900"
              >
                Details
              </button>
              <button
                @click="assignToEmployee(order)"
                class="text-green-600 hover:text-green-900"
                v-if="order.status === 'PENDING'"
              >
                Assign
              </button>
              <button
                @click="updateStatus(order)"
                class="text-purple-600 hover:text-purple-900"
              >
                Update
              </button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="filteredOrders.length > 0" class="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6 mt-6 rounded-lg shadow-md">
      <div class="flex-1 flex justify-between sm:hidden">
        <button
          @click="previousPage"
          :disabled="currentPage === 1"
          class="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
        >
          Previous
        </button>
        <button
          @click="nextPage"
          :disabled="currentPage === totalPages"
          class="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
        >
          Next
        </button>
      </div>
      <div class="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
        <div>
          <p class="text-sm text-gray-700">
            Showing <span class="font-medium">{{ (currentPage - 1) * itemsPerPage + 1 }}</span>
            to <span class="font-medium">{{ Math.min(currentPage * itemsPerPage, filteredOrders.length) }}</span>
            of <span class="font-medium">{{ filteredOrders.length }}</span> orders
          </p>
        </div>
        <div>
          <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
            <button
              @click="previousPage"
              :disabled="currentPage === 1"
              class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
            >
              <span class="sr-only">Previous</span>
              <svg class="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clip-rule="evenodd" />
              </svg>
            </button>

            <span v-for="page in visiblePages" :key="page">
              <button
                v-if="page !== '...'"
                @click="goToPage(page)"
                :class="[
                  'relative inline-flex items-center px-4 py-2 border text-sm font-medium',
                  page === currentPage
                    ? 'z-10 bg-blue-50 border-blue-500 text-blue-600'
                    : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
                ]"
              >
                {{ page }}
              </button>
              <span
                v-else
                class="relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-700"
              >
                ...
              </span>
            </span>

            <button
              @click="nextPage"
              :disabled="currentPage === totalPages"
              class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
            >
              <span class="sr-only">Next</span>
              <svg class="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd" />
              </svg>
            </button>
          </nav>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

// ========== INTERFACES ==========
interface Order {
  id: string
  orderNumber: string
  cardCount: number
  cardsWithName?: number
  namePercentage?: number
  priority: 'URGENT' | 'HIGH' | 'MEDIUM' | 'LOW'
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
  createdDate: string
  estimatedDuration: number
  actualDuration?: number
}

// ========== STATE ==========
const loading = ref(false)
const orders = ref<Order[]>([])
const showFilters = ref(false)

const filters = ref({
  status: '',
  priority: '',
  startDate: '',
  endDate: ''
})

// Pagination
const currentPage = ref(1)
const itemsPerPage = ref(20)

// ========== COMPUTED ==========
const stats = computed(() => {
  const total = orders.value.length
  const pending = orders.value.filter(o => o.status === 'PENDING').length
  const processing = orders.value.filter(o => o.status === 'IN_PROGRESS').length
  const completed = orders.value.filter(o => o.status === 'COMPLETED').length

  return { total, pending, processing, completed }
})

const filteredOrders = computed(() => {
  let filtered = orders.value

  if (filters.value.status) {
    filtered = filtered.filter(order => order.status === filters.value.status)
  }

  if (filters.value.priority) {
    filtered = filtered.filter(order => order.priority === filters.value.priority)
  }

  if (filters.value.startDate) {
    filtered = filtered.filter(order => new Date(order.createdDate) >= new Date(filters.value.startDate))
  }

  if (filters.value.endDate) {
    filtered = filtered.filter(order => new Date(order.createdDate) <= new Date(filters.value.endDate))
  }

  return filtered
})

const totalPages = computed(() => Math.ceil(filteredOrders.value.length / itemsPerPage.value))

const visiblePages = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = currentPage.value

  if (total <= 7) {
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    pages.push(1)

    if (current > 4) {
      pages.push('...')
    }

    const start = Math.max(2, current - 2)
    const end = Math.min(total - 1, current + 2)

    for (let i = start; i <= end; i++) {
      pages.push(i)
    }

    if (current < total - 3) {
      pages.push('...')
    }

    pages.push(total)
  }

  return pages
})

// ========== METHODS ==========
const refreshOrders = async () => {
  loading.value = true

  try {
    console.log('🔄 Loading orders since June 2025...')

    // Try different endpoints
    const endpoints = [
      '/api/commandes/juin-2025',
      '/api/orders/june-2025',
      '/api/commandes/frontend/commandes',
      '/api/frontend/commandes'
    ]

    let ordersData = []

    for (const endpoint of endpoints) {
      try {
        const response = await fetch(`http://localhost:8080${endpoint}`)
        if (response.ok) {
          const data = await response.json()

          if (Array.isArray(data)) {
            ordersData = data
          } else if (data.commandes && Array.isArray(data.commandes)) {
            ordersData = data.commandes
          }

          if (ordersData.length > 0) {
            console.log(`✅ Orders loaded from ${endpoint}:`, ordersData.length)
            break
          }
        }
      } catch (error) {
        console.warn(`⚠️ Failed ${endpoint}:`, error.message)
      }
    }

    // Map data to expected format
    orders.value = ordersData.map(mapOrder)

    console.log(`📦 ${orders.value.length} orders loaded`)
    showNotification('Orders refreshed successfully', 'success')

  } catch (error) {
    console.error('❌ Error loading orders:', error)
    showNotification(`Error: ${error.message}`, 'error')
  } finally {
    loading.value = false
  }
}

const mapOrder = (orderData: any): Order => {
  // Calcul du pourcentage de cartes avec nom
  const cardsWithName = orderData.nombreAvecNom || orderData.cardsWithName || 0
  const cardCount = orderData.nombreCartes || orderData.cardCount || 0
  const namePercentage = cardCount > 0 ? Math.round((cardsWithName / cardCount) * 100) : 0

  return {
    id: orderData.id || `order-${Date.now()}`,
    orderNumber: orderData.numeroCommande || orderData.orderNumber || `CMD-${orderData.id?.substring(0, 8)}`,
    cardCount,
    cardsWithName,
    namePercentage,
    priority: mapPriority(orderData.priority || orderData.priorite),
    status: mapStatus(orderData.status || orderData.statut),
    createdDate: orderData.dateCreation || orderData.createdDate || orderData.date || new Date().toISOString(),
    estimatedDuration: cardCount * 3, // 3 minutes par carte
    actualDuration: orderData.dureeReelle || orderData.actualDuration
  }
}

const mapPriority = (priority: any): 'URGENT' | 'HIGH' | 'MEDIUM' | 'LOW' => {
  if (!priority) return 'MEDIUM'

  const p = String(priority).toUpperCase()
  if (p.includes('URGENT') || p === 'X') return 'URGENT'
  if (p.includes('HAUTE') || p.includes('HIGH') || p === 'F') return 'HIGH'
  if (p.includes('BASSE') || p.includes('LOW') || p === 'C') return 'LOW'
  return 'MEDIUM'
}

const mapStatus = (status: any): 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' => {
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
  if (s.includes('PROGRESS') || s.includes('COURS')) return 'IN_PROGRESS'
  if (s.includes('COMPLETED') || s.includes('TERMINE')) return 'COMPLETED'
  if (s.includes('CANCELLED') || s.includes('ANNULE')) return 'CANCELLED'
  return 'PENDING'
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
    return dateStr || 'Unknown date'
  }
}

const formatDuration = (minutes: number): string => {
  const hours = Math.floor(minutes / 60)
  const remainingMinutes = minutes % 60

  if (hours > 0) {
    return `${hours}h ${remainingMinutes}m`
  }
  return `${remainingMinutes}min`
}

const getPriorityLabel = (priority: string): string => {
  const labels = {
    URGENT: '🔴 Urgent',
    HIGH: '🟠 High',
    MEDIUM: '🟡 Medium',
    LOW: '🟢 Low'
  }
  return labels[priority as keyof typeof labels] || priority
}

const getStatusLabel = (status: string): string => {
  const labels = {
    PENDING: '⏳ Pending',
    IN_PROGRESS: '⚡ In Progress',
    COMPLETED: '✅ Completed',
    CANCELLED: '❌ Cancelled'
  }
  return labels[status as keyof typeof labels] || status
}

// Actions
const viewDetails = (order: Order) => {
  console.log('View details:', order)
  // Implement details modal
}

const assignToEmployee = (order: Order) => {
  console.log('Assign employee:', order)
  // Implement assignment logic
}

const updateStatus = (order: Order) => {
  console.log('Update status:', order)
  // Implement status update logic
}

// Pagination
const goToPage = (page: number | string) => {
  if (typeof page === 'number') {
    currentPage.value = page
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}

const previousPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

// Notification simple
const showNotification = (message: string, type: 'success' | 'error') => {
  console.log(`${type === 'success' ? '✅' : '❌'} ${message}`)
  // Ici on pourrait utiliser une vraie notification toast
}

// ========== LIFECYCLE ==========
onMounted(() => {
  console.log('📦 OrdersView mounted - Loading orders automatically...')
  refreshOrders()
})
</script>

<style scoped>
.orders-view {
  max-width: 1400px;
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

.transition-colors {
  transition-property: color, background-color, border-color;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 150ms;
}

/* Responsive design */
@media (max-width: 768px) {
  .orders-view {
    padding: 16px;
  }

  .overflow-x-auto {
    font-size: 0.875rem;
  }

  .grid-cols-1 {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }
}

@media (min-width: 768px) {
  .md\:grid-cols-4 {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}
</style>
