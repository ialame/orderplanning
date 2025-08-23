<template>
  <div class="employees-page">
    <!-- Header -->
    <div class="bg-white rounded-lg shadow-md p-6 mb-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">👥 Employee Management</h1>
          <p class="text-gray-600 mt-1">Manage team members and view their work assignments</p>
        </div>
        <div class="flex space-x-3">
          <button
            @click="currentView = currentView === 'management' ? 'planning' : 'management'"
            class="btn-secondary"
          >
            {{ currentView === 'management' ? '📊 Switch to Planning' : '👥 Switch to Management' }}
          </button>
          <button
            @click="refreshEmployees"
            :disabled="loading"
            class="btn-primary"
          >
            {{ loading ? '⏳ Loading...' : '🔄 Refresh' }}
          </button>
        </div>
      </div>
    </div>

    <!-- View Mode Selector -->
    <div class="bg-white rounded-lg shadow-md p-4 mb-6">
      <div class="flex items-center space-x-4">
        <button
          @click="currentView = 'management'"
          :class="[
            'px-4 py-2 rounded-lg font-medium transition-colors',
            currentView === 'management'
              ? 'bg-blue-600 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          ]"
        >
          👥 Employee Management
        </button>
        <button
          @click="currentView = 'planning'"
          :class="[
            'px-4 py-2 rounded-lg font-medium transition-colors',
            currentView === 'planning'
              ? 'bg-blue-600 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          ]"
        >
          📊 Work Planning
        </button>
      </div>
    </div>

    <!-- Statistics Cards -->
    <div v-if="currentView === 'planning'" class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
      <div class="card">
        <div class="flex items-center">
          <div class="bg-blue-500 rounded-lg p-3 mr-4">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"></path>
            </svg>
          </div>
          <div>
            <p class="text-sm text-gray-600">Total Employees</p>
            <p class="text-2xl font-semibold text-gray-900">{{ stats.total }}</p>
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
            <p class="text-sm text-gray-600">Available</p>
            <p class="text-2xl font-semibold text-gray-900">{{ stats.available }}</p>
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
            <p class="text-sm text-gray-600">Busy</p>
            <p class="text-2xl font-semibold text-gray-900">{{ stats.busy }}</p>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="flex items-center">
          <div class="bg-red-500 rounded-lg p-3 mr-4">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>
          <div>
            <p class="text-sm text-gray-600">Overloaded</p>
            <p class="text-2xl font-semibold text-gray-900">{{ stats.overloaded }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Employee List -->
    <div v-if="!selectedEmployeeId">
      <!-- Management View -->
      <div v-if="currentView === 'management'" class="bg-white rounded-lg shadow-md overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-200">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-semibold text-gray-900">Team Members</h2>
            <button @click="showAddForm = !showAddForm" class="btn-primary">
              {{ showAddForm ? 'Cancel' : '+ Add Employee' }}
            </button>
          </div>
        </div>

        <!-- Add Employee Form -->
        <div v-if="showAddForm" class="p-6 bg-gray-50 border-b">
          <form @submit.prevent="addEmployee" class="grid grid-cols-1 md:grid-cols-4 gap-4">
            <input
              v-model="newEmployee.firstName"
              placeholder="First Name"
              class="input-field"
              required
            >
            <input
              v-model="newEmployee.lastName"
              placeholder="Last Name"
              class="input-field"
              required
            >
            <input
              v-model="newEmployee.email"
              type="email"
              placeholder="Email"
              class="input-field"
              required
            >
            <div class="flex space-x-2">
              <button type="submit" class="btn-primary flex-1">Add</button>
              <button type="button" @click="showAddForm = false" class="btn-secondary">Cancel</button>
            </div>
          </form>
        </div>

        <!-- Employee Grid -->
        <div v-if="employees.length > 0" class="p-6">
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div
              v-for="employee in employees"
              :key="employee.id"
              class="border border-gray-200 rounded-lg p-6 hover:shadow-md transition-shadow cursor-pointer"
              @click="viewEmployee(employee.id)"
            >
              <!-- Employee Header -->
              <div class="flex items-center mb-4">
                <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center text-blue-600 font-bold text-lg">
                  {{ getInitials(employee) }}
                </div>
                <div class="ml-3">
                  <h3 class="text-lg font-medium text-gray-900">{{ employee.firstName }} {{ employee.lastName }}</h3>
                  <p class="text-sm text-gray-600">{{ employee.email }}</p>
                </div>
              </div>

              <!-- Employee Stats -->
              <div class="space-y-2">
                <div class="flex justify-between text-sm">
                  <span class="text-gray-600">Status:</span>
                  <span :class="[
                    'px-2 py-1 rounded-full text-xs font-medium',
                    employee.status === 'AVAILABLE' ? 'bg-green-100 text-green-800' :
                    employee.status === 'BUSY' ? 'bg-orange-100 text-orange-800' :
                    'bg-gray-100 text-gray-800'
                  ]">
                    {{ employee.status }}
                  </span>
                </div>
                <div class="flex justify-between text-sm">
                  <span class="text-gray-600">Work Hours:</span>
                  <span class="font-medium">{{ employee.workHoursPerDay || 8 }}h/day</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Planning View -->
      <div v-else-if="currentView === 'planning'" class="space-y-6">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div
            v-for="employee in employeesWithWorkload"
            :key="employee.id"
            class="bg-white rounded-lg shadow-md p-6 cursor-pointer hover:shadow-lg transition-shadow"
            @click="viewEmployee(employee.id)"
          >
            <!-- Employee Header -->
            <div class="flex items-center mb-4">
              <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center text-blue-600 font-bold text-lg">
                {{ getInitials(employee) }}
              </div>
              <div class="ml-3">
                <h3 class="text-lg font-medium text-gray-900">{{ employee.firstName }} {{ employee.lastName }}</h3>
                <span :class="[
                  'inline-flex px-2 py-1 text-xs font-semibold rounded-full',
                  getWorkloadColor(employee.workload)
                ]">
                  {{ getWorkloadStatus(employee.workload) }}
                </span>
              </div>
            </div>

            <!-- Workload Progress -->
            <div class="mb-4">
              <div class="flex justify-between text-sm text-gray-600 mb-1">
                <span>Workload</span>
                <span>{{ employee.workload }}%</span>
              </div>
              <div class="w-full bg-gray-200 rounded-full h-2">
                <div
                  :class="[
                    'h-2 rounded-full transition-all duration-300',
                    getWorkloadProgressColor(employee.workload)
                  ]"
                  :style="{ width: Math.min(employee.workload, 100) + '%' }"
                ></div>
              </div>
            </div>

            <!-- Current Tasks -->
            <div class="space-y-2">
              <div class="flex justify-between text-sm">
                <span class="text-gray-600">Active Orders:</span>
                <span class="font-medium">{{ employee.activeOrders || 0 }}</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-gray-600">Estimated Hours:</span>
                <span class="font-medium">{{ employee.estimatedHours || 0 }}h</span>
              </div>
            </div>

            <!-- Actions -->
            <div class="mt-4 flex space-x-2">
              <button
                @click.stop="viewEmployee(employee.id)"
                class="flex-1 bg-blue-50 text-blue-600 px-3 py-2 rounded text-sm font-medium hover:bg-blue-100"
              >
                👁️ View Details
              </button>
              <button
                @click.stop="assignWork(employee.id)"
                class="flex-1 bg-green-50 text-green-600 px-3 py-2 rounded text-sm font-medium hover:bg-green-100"
                :disabled="employee.workload >= 100"
              >
                📋 Assign Work
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="employees.length === 0 && !loading" class="bg-white rounded-lg shadow-md p-8 text-center">
        <svg class="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"></path>
        </svg>
        <h3 class="text-lg font-medium text-gray-900 mb-2">No employees found</h3>
        <p class="text-gray-600 mb-4">Start by adding your first team member</p>
        <button @click="showAddForm = true" class="btn-primary">
          + Add First Employee
        </button>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="bg-white rounded-lg shadow-md p-8 text-center">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
        <p class="text-gray-600">Loading employees...</p>
      </div>
    </div>

    <!-- Employee Detail View -->
    <div v-else>
      <EmployeeDetailPage
        :employeeId="selectedEmployeeId"
        @back="selectedEmployeeId = null"
        @refresh="refreshEmployees"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
// import { apiService } from '../services/api'
import EmployeeDetailPage from '../components/EmployeeDetailPage.vue'

// ========== INTERFACES ==========
interface Employee {
  id: string
  firstName: string
  lastName: string
  email: string
  status: 'AVAILABLE' | 'BUSY' | 'OFFLINE'
  workHoursPerDay?: number
  workload?: number
  activeOrders?: number
  estimatedHours?: number
}

interface NewEmployee {
  firstName: string
  lastName: string
  email: string
}

// ========== STATE ==========
const loading = ref(false)
const employees = ref<Employee[]>([])
const selectedEmployeeId = ref<string | null>(null)
const currentView = ref<'management' | 'planning'>('management')
const showAddForm = ref(false)

const newEmployee = ref<NewEmployee>({
  firstName: '',
  lastName: '',
  email: ''
})

// ========== COMPUTED ==========
const stats = computed(() => {
  const total = employees.value.length
  const available = employees.value.filter(e => e.status === 'AVAILABLE').length
  const busy = employees.value.filter(e => e.status === 'BUSY').length
  const overloaded = employees.value.filter(e => (e.workload || 0) > 100).length

  return { total, available, busy, overloaded }
})

const employeesWithWorkload = computed(() => {
  return employees.value.map(employee => ({
    ...employee,
    workload: employee.workload || Math.floor(Math.random() * 120), // Mock workload if not available
    activeOrders: employee.activeOrders || Math.floor(Math.random() * 5),
    estimatedHours: employee.estimatedHours || Math.floor(Math.random() * 40)
  }))
})

// ========== METHODS ==========
const refreshEmployees = async () => {
  loading.value = true
  try {
    console.log('🔄 Loading employees from API...')

    // Try multiple endpoints like the Dashboard does
    const endpoints = [
      'http://localhost:8080/api/employes/avec-stats',
      'http://localhost:8080/api/employes',
      'http://localhost:8080/api/employees',
      'http://localhost:8080/api/test/employes'
    ]

    let employeesData = []

    for (const endpoint of endpoints) {
      try {
        console.log(`🔄 Trying endpoint: ${endpoint}`)
        const response = await fetch(endpoint)

        if (response.ok) {
          const data = await response.json()
          console.log(`✅ Success with ${endpoint}:`, data)

          // Handle different response formats
          if (Array.isArray(data)) {
            employeesData = data
          } else if (data.employes && Array.isArray(data.employes)) {
            employeesData = data.employes
          } else if (data.employees && Array.isArray(data.employees)) {
            employeesData = data.employees
          }

          if (employeesData.length > 0) {
            console.log(`🎯 Found ${employeesData.length} employees from ${endpoint}`)
            break
          }
        }
      } catch (endpointError) {
        console.warn(`⚠️ ${endpoint} failed:`, endpointError.message)
        continue
      }
    }

    if (employeesData && employeesData.length > 0) {
      employees.value = employeesData.map(mapEmployee)
      console.log(`✅ Loaded ${employees.value.length} real employees:`)
      employees.value.forEach(emp => console.log(`  - ${emp.firstName} ${emp.lastName} (${emp.email})`))
      showNotification(`Successfully loaded ${employees.value.length} employees`, 'success')
    } else {
      // Fallback to demo data if no employees found
      console.log('⚠️ No real employees found, using demo data')
      employees.value = generateDemoEmployees()
      showNotification('No employees found in database - showing demo data', 'success')
    }
  } catch (error) {
    console.error('❌ Error loading employees:', error)
    // Use demo data as fallback
    employees.value = generateDemoEmployees()
    showNotification('API error - using demo employees', 'error')
  } finally {
    loading.value = false
  }
}

const mapEmployee = (employeeData: any): Employee => {
  console.log('🔧 Mapping employee data:', employeeData)

  // Handle different data structures from different endpoints
  return {
    id: employeeData.id || `emp-${Date.now()}`,
    firstName: employeeData.firstName || employeeData.prenom || extractFirstName(employeeData),
    lastName: employeeData.lastName || extractLastName(employeeData),
    email: employeeData.email || `employee${employeeData.id}@company.com`,
    status: mapEmployeeStatus(employeeData.status || employeeData.statut || employeeData.active),
    workHoursPerDay: employeeData.workHoursPerDay || employeeData.heuresTravailParJour || 8,
    workload: employeeData.workload || employeeData.chargeActuelle || calculateWorkload(employeeData),
    activeOrders: employeeData.activeOrders || employeeData.commandesActives || employeeData.nombreTaches || 0,
    estimatedHours: employeeData.estimatedHours || employeeData.heuresEstimees || (employeeData.totalMinutes ? Math.round(employeeData.totalMinutes / 60) : 0)
  }
}

const extractFirstName = (employeeData: any): string => {
  if (employeeData.firstName) return employeeData.firstName
  if (employeeData.prenom) return employeeData.prenom
  if (employeeData.nom) {
    // If nom contains full name, extract first part
    const parts = employeeData.nom.split(' ')
    return parts[0] || 'Unknown'
  }
  if (employeeData.name || employeeData.fullName) {
    const parts = (employeeData.name || employeeData.fullName).split(' ')
    return parts[0] || 'Unknown'
  }
  return 'Unknown'
}

const extractLastName = (employeeData: any): string => {
  if (employeeData.lastName) return employeeData.lastName
  if (employeeData.nom) {
    const parts = employeeData.nom.split(' ')
    return parts.slice(1).join(' ') || 'Employee'
  }
  if (employeeData.name || employeeData.fullName) {
    const parts = (employeeData.name || employeeData.fullName).split(' ')
    return parts.slice(1).join(' ') || 'Employee'
  }
  return 'Employee'
}

const mapEmployeeStatus = (status: any): 'AVAILABLE' | 'BUSY' | 'OFFLINE' => {
  if (!status) return 'AVAILABLE'

  // Handle boolean active status
  if (typeof status === 'boolean') {
    return status ? 'AVAILABLE' : 'OFFLINE'
  }

  const s = String(status).toUpperCase()
  if (s.includes('BUSY') || s.includes('OCCUPE') || s.includes('WORKING')) return 'BUSY'
  if (s.includes('OFFLINE') || s.includes('ABSENT') || s.includes('INACTIVE') || s === 'FALSE') return 'OFFLINE'
  if (s.includes('AVAILABLE') || s.includes('LIBRE') || s.includes('ACTIVE') || s === 'TRUE') return 'AVAILABLE'
  return 'AVAILABLE'
}

const calculateWorkload = (employeeData: any): number => {
  // Calculate workload based on assigned tasks/orders
  const tasks = employeeData.tasks || employeeData.commandes || employeeData.taches || []
  const totalMinutes = employeeData.totalMinutes || 0
  const maxMinutes = employeeData.maxMinutes || (employeeData.heuresTravailParJour || 8) * 60

  if (totalMinutes && maxMinutes) {
    return Math.min((totalMinutes / maxMinutes) * 100, 150)
  }

  if (tasks.length === 0) return Math.floor(Math.random() * 30) // Light workload if no tasks

  // Simple calculation: each task takes 3 hours on average
  const hoursPerDay = employeeData.workHoursPerDay || employeeData.heuresTravailParJour || 8
  const estimatedHours = tasks.length * 3
  return Math.min((estimatedHours / hoursPerDay) * 100, 150)
}

const generateDemoEmployees = (): Employee[] => {
  return [
    {
      id: 'emp-1',
      firstName: 'John',
      lastName: 'Smith',
      email: 'john.smith@company.com',
      status: 'AVAILABLE',
      workHoursPerDay: 8,
      workload: 65,
      activeOrders: 3,
      estimatedHours: 24
    },
    {
      id: 'emp-2',
      firstName: 'Sarah',
      lastName: 'Johnson',
      email: 'sarah.johnson@company.com',
      status: 'BUSY',
      workHoursPerDay: 8,
      workload: 95,
      activeOrders: 5,
      estimatedHours: 38
    },
    {
      id: 'emp-3',
      firstName: 'Mike',
      lastName: 'Davis',
      email: 'mike.davis@company.com',
      status: 'AVAILABLE',
      workHoursPerDay: 6,
      workload: 40,
      activeOrders: 2,
      estimatedHours: 12
    }
  ]
}

const addEmployee = async () => {
  try {
    const employeeData = {
      firstName: newEmployee.value.firstName,
      lastName: newEmployee.value.lastName,
      email: newEmployee.value.email,
      workHoursPerDay: 8,
      active: true
    }

    // Try the same endpoints used for fetching employees
    const createEndpoints = [
      'http://localhost:8080/api/employes',
      'http://localhost:8080/api/employees',
      'http://localhost:8080/api/test/employes'
    ]

    let success = false

    for (const endpoint of createEndpoints) {
      try {
        console.log(`🔄 Trying to create employee at: ${endpoint}`)
        const response = await fetch(endpoint, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(employeeData)
        })

        if (response.ok) {
          const result = await response.json()
          console.log(`✅ Employee created successfully at ${endpoint}:`, result)
          success = true
          break
        } else {
          console.warn(`⚠️ ${endpoint} failed with status:`, response.status)
        }
      } catch (endpointError) {
        console.warn(`⚠️ ${endpoint} error:`, endpointError.message)
        continue
      }
    }

    if (success) {
      // Refresh the list to show the new employee
      await refreshEmployees()
      showNotification('Employee added successfully', 'success')
    } else {
      console.warn('⚠️ All API endpoints failed, adding to demo data')
      // Add to local demo data as fallback
      const newEmp = {
        id: `emp-${Date.now()}`,
        ...employeeData,
        status: 'AVAILABLE' as const,
        workload: 0,
        activeOrders: 0,
        estimatedHours: 0
      }
      employees.value.push(newEmp)
      showNotification('Employee added to local data (API unavailable)', 'success')
    }

    // Reset form
    newEmployee.value = { firstName: '', lastName: '', email: '' }
    showAddForm.value = false

  } catch (error) {
    console.error('❌ Error adding employee:', error)
    showNotification('Error adding employee', 'error')
  }
}

const viewEmployee = (employeeId: string) => {
  selectedEmployeeId.value = employeeId
}

const assignWork = (employeeId: string) => {
  console.log('Assign work to employee:', employeeId)
  // Implement work assignment logic
  showNotification('Work assignment feature coming soon', 'success')
}

const getInitials = (employee: Employee): string => {
  return `${employee.firstName?.[0] || ''}${employee.lastName?.[0] || ''}`.toUpperCase()
}

const getWorkloadColor = (workload: number): string => {
  if (workload <= 60) return 'bg-green-100 text-green-800'
  if (workload <= 85) return 'bg-yellow-100 text-yellow-800'
  if (workload <= 100) return 'bg-orange-100 text-orange-800'
  return 'bg-red-100 text-red-800'
}

const getWorkloadStatus = (workload: number): string => {
  if (workload <= 60) return '✅ Available'
  if (workload <= 85) return '⚡ Busy'
  if (workload <= 100) return '⚠️ Full'
  return '🚨 Overloaded'
}

const getWorkloadProgressColor = (workload: number): string => {
  if (workload <= 60) return 'bg-green-500'
  if (workload <= 85) return 'bg-yellow-500'
  if (workload <= 100) return 'bg-orange-500'
  return 'bg-red-500'
}

// Simple notification function
const showNotification = (message: string, type: 'success' | 'error') => {
  console.log(`${type === 'success' ? '✅' : '❌'} ${message}`)
  // You can implement a real toast notification here
}

// ========== LIFECYCLE ==========
onMounted(() => {
  console.log('👥 Employees page mounted - Loading employees...')
  refreshEmployees()
})
</script>

<style scoped>
.employees-page {
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

/* Responsive design */
@media (max-width: 768px) {
  .employees-page {
    padding: 16px;
  }

  .grid-cols-1 {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }
}

@media (min-width: 768px) {
  .md\:grid-cols-2 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .md\:grid-cols-4 {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (min-width: 1024px) {
  .lg\:grid-cols-3 {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
</style>
