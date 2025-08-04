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
          {{ loading ? 'Loading...' : 'Refresh Employees' }}
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

    <!-- Statistics Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
      <div class="bg-white p-6 rounded-lg shadow border-l-4 border-blue-500">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">Total Employees</p>
            <p class="text-2xl font-bold text-blue-600">{{ employees.length }}</p>
          </div>
          <div class="text-3xl text-blue-600">👥</div>
        </div>
      </div>

      <div class="bg-white p-6 rounded-lg shadow border-l-4 border-green-500">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">Active</p>
            <p class="text-2xl font-bold text-green-600">{{ activeEmployees }}</p>
          </div>
          <div class="text-3xl text-green-600">✅</div>
        </div>
      </div>

      <div class="bg-white p-6 rounded-lg shadow border-l-4 border-yellow-500">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">Busy</p>
            <p class="text-2xl font-bold text-yellow-600">{{ busyEmployees }}</p>
          </div>
          <div class="text-3xl text-yellow-600">⏳</div>
        </div>
      </div>

      <div class="bg-white p-6 rounded-lg shadow border-l-4 border-purple-500">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">Backend Status</p>
            <p class="text-2xl font-bold text-purple-600">{{
                backendStatus === 'connected' ? '✅' :
                  backendStatus === 'error' ? '❌' : '⏳'
              }}</p>
          </div>
          <div class="text-3xl text-purple-600">🔗</div>
        </div>
      </div>
    </div>

    <!-- Status -->
    <div class="bg-white rounded-lg shadow p-6 mb-6">
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-lg font-semibold text-gray-900">System Status</h2>
          <p class="text-gray-600">Employee management system connectivity</p>
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

    <!-- Employee Grid -->
    <div v-if="loading" class="text-center py-12">
      <div class="flex items-center justify-center">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        <span class="ml-3 text-gray-600">Loading employees...</span>
      </div>
    </div>

    <div v-else-if="employees.length === 0" class="text-center py-12 bg-white rounded-lg shadow">
      <svg class="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path>
      </svg>
      <h3 class="text-lg font-medium text-gray-900 mb-2">No Employees Found</h3>
      <p class="text-gray-600 mb-4">No employees are available. This could be because:</p>
      <ul class="text-sm text-gray-500 list-disc list-inside space-y-1 mb-6">
        <li>Backend server is not running</li>
        <li>Employee database is empty</li>
        <li>API endpoint is not accessible</li>
      </ul>
      <button
        @click="testBackend"
        class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
      >
        Test Backend Connection
      </button>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="employee in employees"
        :key="employee.id"
        class="bg-white rounded-lg shadow-lg hover:shadow-xl transition-shadow duration-200 border"
      >
        <!-- Employee Header -->
        <div class="p-6 border-b border-gray-200">
          <div class="flex items-center justify-between">
            <div class="flex items-center space-x-3">
              <div class="w-12 h-12 bg-gradient-to-r from-blue-500 to-purple-600 rounded-full flex items-center justify-center text-white font-bold text-lg">
                {{ getInitials(employee.name) }}
              </div>
              <div>
                <h3 class="text-lg font-semibold text-gray-900">{{ employee.name }}</h3>
                <p class="text-sm text-gray-500">{{ employee.department || 'No Department' }}</p>
              </div>
            </div>
            <div :class="[
              'w-3 h-3 rounded-full',
              employee.status === 'AVAILABLE' ? 'bg-green-500' :
              employee.status === 'BUSY' ? 'bg-yellow-500' :
              'bg-red-500'
            ]"></div>
          </div>
        </div>

        <!-- Employee Details -->
        <div class="p-6">
          <div class="space-y-4">
            <!-- Status -->
            <div class="flex items-center justify-between">
              <span class="text-sm font-medium text-gray-600">Status</span>
              <span :class="[
                'inline-flex px-2 py-1 text-xs font-semibold rounded-full',
                employee.status === 'AVAILABLE' ? 'bg-green-100 text-green-800' :
                employee.status === 'BUSY' ? 'bg-yellow-100 text-yellow-800' :
                'bg-red-100 text-red-800'
              ]">
                {{ employee.status || 'UNKNOWN' }}
              </span>
            </div>

            <!-- Efficiency -->
            <div class="flex items-center justify-between">
              <span class="text-sm font-medium text-gray-600">Efficiency</span>
              <div class="flex items-center space-x-2">
                <div class="w-20 bg-gray-200 rounded-full h-2">
                  <div
                    :class="[
                      'h-2 rounded-full transition-all duration-300',
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

const testBackend = async () => {
  backendStatus.value = 'testing'
  statusMessage.value = 'Testing backend connection...'

  try {
    // Test multiple employee endpoints
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
      details: 'Please start your Spring Boot server and try again',
      type: 'error'
    })
  }
}

const loadEmployees = async () => {
  loading.value = true

  try {
    const response = await fetch('/api/employees')

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    }

    const data = await response.json()
    console.log('🔍 Raw employee data from backend:', data)

    // Transform and validate data
    employees.value = Array.isArray(data) ? data.map(emp => ({
      id: emp.id || `emp-${Math.random().toString(36).substr(2, 9)}`,
      name: emp.name || emp.fullName || emp.firstName + ' ' + emp.lastName || 'Unknown Employee',
      department: emp.department || emp.role || 'PROCESSING',
      status: emp.status || emp.active ? 'AVAILABLE' : 'OFFLINE',
      efficiency: emp.efficiency || emp.workEfficiency || 75,
      currentOrders: emp.currentOrders || emp.activeOrders || 0,
      totalProcessed: emp.totalProcessed || emp.completedOrders || 0,
      hoursToday: emp.hoursToday || emp.workHoursToday || 0
    })) : []

    console.log(`✅ Processed ${employees.value.length} employees`)

    emit('show-notification', {
      message: 'Employees loaded successfully',
      details: `Found ${employees.value.length} employees`,
      type: 'success'
    })

  } catch (error) {
    console.error('❌ Error loading employees:', error)

    // Create mock data for demonstration
    employees.value = [
      {
        id: 'EMP-001',
        name: 'Alice Johnson',
        department: 'PROCESSING',
        status: 'AVAILABLE',
        efficiency: 92,
        currentOrders: 2,
        totalProcessed: 156,
        hoursToday: 6.5
      },
      {
        id: 'EMP-002',
        name: 'Bob Smith',
        department: 'PROCESSING',
        status: 'BUSY',
        efficiency: 88,
        currentOrders: 3,
        totalProcessed: 134,
        hoursToday: 7.0
      },
      {
        id: 'EMP-003',
        name: 'Carol Williams',
        department: 'QUALITY',
        status: 'AVAILABLE',
        efficiency: 95,
        currentOrders: 1,
        totalProcessed: 89,
        hoursToday: 5.5
      },
      {
        id: 'EMP-004',
        name: 'David Brown',
        department: 'PACKAGING',
        status: 'BUSY',
        efficiency: 78,
        currentOrders: 4,
        totalProcessed: 201,
        hoursToday: 8.0
      },
      {
        id: 'EMP-005',
        name: 'Eva Davis',
        department: 'MANAGEMENT',
        status: 'AVAILABLE',
        efficiency: 85,
        currentOrders: 0,
        totalProcessed: 45,
        hoursToday: 4.0
      },
      {
        id: 'EMP-006',
        name: 'Frank Miller',
        department: 'PROCESSING',
        status: 'OFFLINE',
        efficiency: 72,
        currentOrders: 0,
        totalProcessed: 98,
        hoursToday: 0
      }
    ]

    emit('show-notification', {
      message: 'Using mock data',
      details: 'Backend not available, showing sample employees',
      type: 'warning'
    })
  } finally {
    loading.value = false
  }
}

const viewEmployee = (employee: Employee) => {
  emit('show-notification', {
    message: `Viewing ${employee.name}'s details`,
    type: 'info'
  })
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
  console.log('👥 EmployeeListView mounted - Loading employees automatically...')

  // Automatically test backend and load employees on mount
  await testBackend()
  await loadEmployees()
})
</script>

<style scoped>
/* Component specific styles */
</style>
