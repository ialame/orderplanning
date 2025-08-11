<template>
  <div class="p-6">
    <!-- Header -->
    <div class="flex justify-between items-center mb-6">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">👥 Employee Management</h1>
        <p class="text-gray-600 mt-1">Manage your team members and their assignments</p>
      </div>
      <div class="flex gap-3">
        <button
          @click="loadEmployees"
          :disabled="loading"
          class="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
        >
          <svg v-if="loading" class="animate-spin h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
          </svg>
          <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
          </svg>
          {{ loading ? 'Loading...' : 'Refresh' }}
        </button>
        <button
          @click="testBackend"
          class="flex items-center gap-2 bg-gray-600 text-white px-4 py-2 rounded-lg hover:bg-gray-700 transition-colors"
        >
          🔧 Test Backend
        </button>
      </div>
    </div>

    <!-- Backend Status -->
    <div v-if="backendStatus !== 'connected'" class="mb-6">
      <div :class="[
        'p-4 rounded-lg border',
        backendStatus === 'testing' ? 'bg-blue-50 border-blue-200' :
        backendStatus === 'error' ? 'bg-red-50 border-red-200' :
        'bg-gray-50 border-gray-200'
      ]">
        <p :class="[
          'text-sm font-medium',
          backendStatus === 'testing' ? 'text-blue-800' :
          backendStatus === 'error' ? 'text-red-800' :
          'text-gray-800'
        ]">
          {{ statusMessage }}
        </p>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
      <!-- Total Employees -->
      <div class="bg-white rounded-lg shadow p-6 border-l-4 border-blue-500">
        <div class="flex items-center">
          <div class="flex-1">
            <p class="text-blue-600 text-sm font-medium uppercase tracking-wide">Total Employees</p>
            <p class="text-3xl font-bold text-gray-900">{{ employees.length }}</p>
          </div>
          <div class="text-3xl text-blue-600">👥</div>
        </div>
      </div>

      <!-- Active Employees -->
      <div class="bg-white rounded-lg shadow p-6 border-l-4 border-green-500">
        <div class="flex items-center">
          <div class="flex-1">
            <p class="text-green-600 text-sm font-medium uppercase tracking-wide">Active</p>
            <p class="text-3xl font-bold text-gray-900">{{ activeEmployees }}</p>
          </div>
          <div class="text-3xl text-green-600">✅</div>
        </div>
      </div>

      <!-- Busy Employees -->
      <div class="bg-white rounded-lg shadow p-6 border-l-4 border-yellow-500">
        <div class="flex items-center">
          <div class="flex-1">
            <p class="text-yellow-600 text-sm font-medium uppercase tracking-wide">Busy</p>
            <p class="text-3xl font-bold text-gray-900">{{ busyEmployees }}</p>
          </div>
          <div class="text-3xl text-yellow-600">⏰</div>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
      <p class="mt-4 text-gray-600">Loading employees...</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="employees.length === 0" class="text-center py-12">
      <div class="text-6xl mb-4">👥</div>
      <h3 class="text-xl font-medium text-gray-900 mb-2">No employees found</h3>
      <p class="text-gray-600 mb-6">No employees are currently available in the system.</p>
      <button
        @click="loadEmployees"
        class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
      >
        🔄 Retry Loading
      </button>
    </div>

    <!-- Employee Grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="employee in employees"
        :key="employee.id"
        class="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow border-l-4 border-blue-500"
      >
        <div class="p-6">
          <!-- Employee Header -->
          <div class="flex items-center mb-4">
            <div class="w-12 h-12 bg-gradient-to-r from-blue-500 to-purple-600 rounded-full flex items-center justify-center text-white font-bold text-lg">
              {{ getInitials(employee.name) }}
            </div>
            <div class="ml-3 flex-1">
              <h3 class="text-lg font-semibold text-gray-900">{{ employee.name }}</h3>
              <p class="text-sm text-gray-600">{{ employee.department || 'PROCESSING' }}</p>
            </div>
            <div :class="[
              'px-2 py-1 rounded-full text-xs font-medium',
              employee.status === 'AVAILABLE' ? 'bg-green-100 text-green-800' :
              employee.status === 'BUSY' ? 'bg-yellow-100 text-yellow-800' :
              'bg-red-100 text-red-800'
            ]">
              {{ employee.status || 'AVAILABLE' }}
            </div>
          </div>

          <!-- Employee Stats -->
          <div class="space-y-3 mb-6">
            <!-- Efficiency -->
            <div class="flex items-center justify-between">
              <span class="text-sm font-medium text-gray-600">Efficiency</span>
              <div class="flex items-center space-x-2">
                <div class="w-20 h-2 bg-gray-200 rounded-full overflow-hidden">
                  <div
                    :class="[
                      'h-full transition-all',
                      (employee.efficiency || 75) >= 90 ? 'bg-green-500' :
                      (employee.efficiency || 75) >= 70 ? 'bg-yellow-500' :
                      'bg-red-500'
                    ]"
                    :style="{ width: `${employee.efficiency || 75}%` }"
                  ></div>
                </div>
                <span class="text-sm font-medium text-gray-900">{{ employee.efficiency || 75 }}%</span>
              </div>
            </div>

            <!-- Current Orders -->
            <div class="flex items-center justify-between">
              <span class="text-sm font-medium text-gray-600">Current Orders</span>
              <span class="text-sm font-medium text-gray-900">{{ employee.currentOrders || 0 }}</span>
            </div>

            <!-- ID -->
            <div class="flex items-center justify-between">
              <span class="text-sm font-medium text-gray-600">Employee ID</span>
              <span class="text-sm font-medium text-gray-900">{{ employee.id }}</span>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="mt-6 flex space-x-2">
            <button
              @click="viewEmployee(employee)"
              class="flex-1 bg-blue-600 text-white px-3 py-2 rounded-md text-sm font-medium hover:bg-blue-700 transition-colors"
            >
              View Details
            </button>
            <button
              @click="toggleStatus(employee)"
              :class="[
                'px-3 py-2 rounded-md text-sm font-medium transition-colors',
                employee.status === 'AVAILABLE'
                  ? 'bg-yellow-600 text-white hover:bg-yellow-700'
                  : 'bg-green-600 text-white hover:bg-green-700'
              ]"
            >
              {{ employee.status === 'AVAILABLE' ? 'Set Busy' : 'Set Available' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

// ========== INTERFACES ==========
interface Employee {
  id: string
  name: string
  department?: string
  status?: 'AVAILABLE' | 'BUSY' | 'OFFLINE'
  efficiency?: number
  currentOrders?: number
}

// ========== ROUTER ==========
const router = useRouter()

// ========== EMITS ==========
const emit = defineEmits(['show-notification'])

// ========== STATE ==========
const loading = ref(false)
const employees = ref<Employee[]>([])
const backendStatus = ref<'testing' | 'connected' | 'error'>('testing')
const statusMessage = ref('')

// ========== COMPUTED ==========
const activeEmployees = computed(() =>
  employees.value.filter(e => e.status !== 'OFFLINE').length
)

const busyEmployees = computed(() =>
  employees.value.filter(e => e.status === 'BUSY').length
)

// ========== METHODS ==========
const viewEmployee = (employee: Employee) => {
  console.log(`🔄 Navigating to employee details: ${employee.id}`)
  console.log(`👤 Employee name: ${employee.name}`)

  try {
    // Navigation via Vue Router
    router.push(`/employees/${employee.id}`)

    emit('show-notification', {
      message: `Opening ${employee.name}'s details`,
      type: 'info'
    })

  } catch (error) {
    console.error('❌ Navigation error:', error)

    // Fallback navigation
    window.location.href = `/employees/${employee.id}`

    emit('show-notification', {
      message: `Failed to open ${employee.name}'s details`,
      details: error.message,
      type: 'error'
    })
  }
}

const toggleStatus = (employee: Employee) => {
  const oldStatus = employee.status
  employee.status = employee.status === 'AVAILABLE' ? 'BUSY' : 'AVAILABLE'

  emit('show-notification', {
    message: `${employee.name} status changed`,
    details: `Changed from ${oldStatus} to ${employee.status}`,
    type: 'success'
  })
}

const testBackend = async () => {
  backendStatus.value = 'testing'
  statusMessage.value = 'Testing backend connection...'

  try {
    const endpoints = [
      '/api/employees',
      '/api/employees/active',
      '/api/employes',
      '/api/employes/active'
    ]

    let successCount = 0
    let workingEndpoint = ''

    for (const endpoint of endpoints) {
      try {
        console.log(`🔄 Testing endpoint: ${endpoint}`)
        const response = await fetch(endpoint)

        if (response.ok) {
          console.log(`✅ Endpoint working: ${endpoint}`)
          successCount++
          if (!workingEndpoint) workingEndpoint = endpoint
        } else {
          console.log(`❌ Endpoint failed: ${endpoint} - HTTP ${response.status}`)
        }
      } catch (error) {
        console.log(`❌ Endpoint error: ${endpoint} - ${error.message}`)
      }
    }

    if (successCount > 0) {
      backendStatus.value = 'connected'
      statusMessage.value = `✅ Backend accessible (${successCount}/4 employee endpoints working)`
      emit('show-notification', {
        message: 'Backend connection successful',
        details: `Employee management system is online. Working endpoint: ${workingEndpoint}`,
        type: 'success'
      })
    } else {
      throw new Error('No employee endpoints available')
    }

  } catch (error) {
    backendStatus.value = 'error'
    statusMessage.value = '❌ Backend not accessible. Please check if Spring Boot server is running on port 8080.'
    emit('show-notification', {
      message: 'Backend connection failed',
      details: 'Could not connect to any employee endpoints. Using mock data.',
      type: 'warning'
    })
  }
}

/**
 * ✅ REMPLACEZ la méthode loadEmployees dans EmployeeListView.vue
 * Le problème : utilise des données mock au lieu des vraies données
 */
const loadEmployees = async () => {
  loading.value = true

  try {
    console.log('👥 Loading employees from backend...')

    // ✅ CHARGEMENT DIRECT DES VRAIES DONNÉES
    const response = await fetch('/api/employees')

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    }

    const data = await response.json()
    console.log('📥 Raw backend data:', data)
    console.log('📊 Data type:', typeof data, 'Is array:', Array.isArray(data))
    console.log('📊 Data length:', data?.length)

    // ✅ VÉRIFIER QUE LES DONNÉES SONT VALIDES
    if (!Array.isArray(data) || data.length === 0) {
      console.error('❌ No valid employee data from backend')
      console.log('📋 Backend returned:', data)

      // ✅ NE PAS UTILISER DE DONNÉES MOCK - Afficher une erreur claire
      employees.value = []

      emit('show-notification', {
        message: 'No employees found in database',
        details: 'The backend returned no employee data. Check if employees exist in j_employee table.',
        type: 'error'
      })

      return
    }

    // ✅ MAPPING DES VRAIES DONNÉES DE LA BASE
    employees.value = data.map(emp => {
      console.log('👤 Processing employee:', emp)

      return {
        id: emp.id || `emp-${Math.random().toString(36).substr(2, 9)}`,
        name: emp.fullName || `${emp.firstName || ''} ${emp.lastName || ''}`.trim() || 'Unknown Employee',
        department: 'PROCESSING', // Vous pouvez ajouter ce champ à votre base si nécessaire
        status: emp.active ? 'AVAILABLE' : 'OFFLINE',
        efficiency: 85, // Valeur par défaut, vous pouvez calculer cela selon vos critères
        currentOrders: 0 // À remplir avec les vraies données de planning si disponible
      }
    })

    console.log(`✅ Successfully loaded ${employees.value.length} real employees from database`)
    console.log('👥 Employee names:', employees.value.map(e => e.name))

    emit('show-notification', {
      message: 'Real employees loaded successfully',
      details: `Found ${employees.value.length} employees from j_employee table`,
      type: 'success'
    })

  } catch (error) {
    console.error('❌ Error loading employees:', error)

    // ✅ EN CAS D'ERREUR, AFFICHER L'ERREUR AU LIEU DE DONNÉES MOCK
    employees.value = []

    emit('show-notification', {
      message: 'Failed to load employees from database',
      details: `Error: ${error.message}. Check backend connection.`,
      type: 'error'
    })
  } finally {
    loading.value = false
  }
}

const getInitials = (name: string): string => {
  if (!name || typeof name !== 'string') {
    return 'XX'
  }

  return name
    .split(' ')
    .map(part => part.charAt(0))
    .join('')
    .toUpperCase()
    .slice(0, 2) || 'XX'
}

// ========== LIFECYCLE ==========
onMounted(async () => {
  console.log('👥 EmployeeListView mounted')

  // Automatically test backend and load employees on mount
  await testBackend()
  await loadEmployees()
})
</script>

<style scoped>
/* Component specific styles */
</style>
