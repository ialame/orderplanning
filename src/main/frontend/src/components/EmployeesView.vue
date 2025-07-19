<template>
  <div class="employees-view">
    <!-- Header -->
    <div class="flex justify-between items-center mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Employee Management</h1>
        <p class="text-gray-600 mt-1">Manage active employees and their workload</p>
      </div>

      <!-- View mode selector -->
      <div class="flex items-center space-x-4">
        <div class="flex bg-gray-100 rounded-lg p-1">
          <button
            @click="viewMode = 'management'"
            :class="[
              'px-4 py-2 rounded-md text-sm font-medium transition-colors',
              viewMode === 'management' ? 'bg-white text-blue-600 shadow-sm' : 'text-gray-600 hover:text-gray-900'
            ]"
          >
            👥 Management
          </button>
          <button
            @click="viewMode = 'planning'"
            :class="[
              'px-4 py-2 rounded-md text-sm font-medium transition-colors',
              viewMode === 'planning' ? 'bg-white text-blue-600 shadow-sm' : 'text-gray-600 hover:text-gray-900'
            ]"
          >
            📅 Planning
          </button>
        </div>

        <button
          v-if="viewMode === 'management'"
          @click="showForm = true"
          class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
        >
          ➕ Add Employee
        </button>

        <button
          @click="refreshData"
          :disabled="loading"
          class="bg-gray-600 text-white px-4 py-2 rounded-lg hover:bg-gray-700 transition-colors disabled:opacity-50"
        >
          🔄 Refresh
        </button>
      </div>
    </div>

    <!-- Success/Error messages -->
    <div v-if="message.text" class="mb-6">
      <div :class="[
        'p-4 rounded-lg border',
        message.type === 'success'
          ? 'bg-green-50 border-green-200 text-green-800'
          : 'bg-red-50 border-red-200 text-red-800'
      ]">
        {{ message.text }}
      </div>
    </div>

    <!-- Employee Creation Form -->
    <div v-if="showForm && viewMode === 'management' && !selectedEmployee" class="mb-6 bg-white border border-gray-200 rounded-lg p-6">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-lg font-semibold text-gray-900">➕ New Employee</h2>
        <button
          @click="cancelForm"
          class="text-gray-500 hover:text-gray-700"
        >
          ❌
        </button>
      </div>

      <div class="space-y-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Last Name *</label>
            <input
              v-model="newEmployee.lastName"
              type="text"
              :class="[
                'w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500',
                formErrors.lastName ? 'border-red-500' : 'border-gray-300'
              ]"
              placeholder="Last name"
            />
            <p v-if="formErrors.lastName" class="text-red-500 text-sm mt-1">{{ formErrors.lastName }}</p>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">First Name *</label>
            <input
              v-model="newEmployee.firstName"
              type="text"
              :class="[
                'w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500',
                formErrors.firstName ? 'border-red-500' : 'border-gray-300'
              ]"
              placeholder="First name"
            />
            <p v-if="formErrors.firstName" class="text-red-500 text-sm mt-1">{{ formErrors.firstName }}</p>
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input
            v-model="newEmployee.email"
            type="email"
            :class="[
              'w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500',
              formErrors.email ? 'border-red-500' : 'border-gray-300'
            ]"
            placeholder="email@example.com"
          />
          <p v-if="formErrors.email" class="text-red-500 text-sm mt-1">{{ formErrors.email }}</p>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Work Hours per Day</label>
          <select
            v-model="newEmployee.workHoursPerDay"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="4">4 hours</option>
            <option value="6">6 hours</option>
            <option value="7">7 hours</option>
            <option value="8" selected>8 hours (standard)</option>
          </select>
        </div>

        <div class="flex items-center">
          <input
            v-model="newEmployee.active"
            type="checkbox"
            id="employee-active"
            class="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
          />
          <label for="employee-active" class="ml-2 text-sm text-gray-700">
            Active employee
          </label>
        </div>

        <div class="flex justify-end space-x-3 pt-4 border-t border-gray-200">
          <button
            @click="cancelForm"
            class="px-4 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
          >
            Cancel
          </button>
          <button
            @click="saveEmployee"
            :disabled="saving"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
          >
            {{ saving ? 'Saving...' : 'Save Employee' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Loading indicator -->
    <div v-if="loading" class="text-center py-8">
      <div class="text-gray-600">
        <div class="animate-spin inline-block w-6 h-6 border-2 border-gray-300 border-t-blue-600 rounded-full"></div>
        <span class="ml-2">{{ loadingMessage || 'Loading employees...' }}</span>
      </div>
    </div>

    <!-- Management View -->
    <div v-else-if="viewMode === 'management'">
      <!-- Filters -->
      <div class="bg-white rounded-lg border border-gray-200 p-4 mb-6">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Search</label>
            <input
              v-model="filters.search"
              type="text"
              placeholder="Search by name or email..."
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Status</label>
            <select
              v-model="filters.active"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">All employees</option>
              <option value="true">Active only</option>
              <option value="false">Inactive only</option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Workload</label>
            <select
              v-model="filters.workload"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">All workloads</option>
              <option value="available">Available</option>
              <option value="full">Full</option>
              <option value="overloaded">Overloaded</option>
            </select>
          </div>
        </div>
      </div>

      <!-- Employee grid -->
      <div v-if="filteredEmployees.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="employee in filteredEmployees"
          :key="employee.id"
          class="bg-white rounded-lg border border-gray-200 p-6 hover:shadow-md transition-shadow"
        >
          <!-- Employee header -->
          <div class="flex justify-between items-start mb-4">
            <div>
              <h3 class="font-semibold text-gray-900">
                {{ employee.firstName }} {{ employee.lastName }}
              </h3>
              <p class="text-sm text-gray-600">{{ employee.email }}</p>
            </div>
            <div class="flex items-center space-x-2">
              <span :class="[
                'px-2 py-1 rounded-full text-xs font-medium',
                employee.active ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
              ]">
                {{ employee.active ? 'Active' : 'Inactive' }}
              </span>
            </div>
          </div>

          <!-- Work info -->
          <div class="space-y-2 mb-4">
            <div class="flex justify-between text-sm">
              <span class="text-gray-600">Work hours/day:</span>
              <span class="font-medium">{{ employee.workHoursPerDay }}h</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-gray-600">Member since:</span>
              <span class="font-medium">{{ formatDate(employee.creationDate) }}</span>
            </div>
          </div>

          <!-- Current workload (if available) -->
          <div v-if="employee.currentWorkload" class="mb-4">
            <div class="flex justify-between items-center mb-2">
              <span class="text-sm font-medium text-gray-700">Current Workload</span>
              <span :class="[
                'text-xs px-2 py-1 rounded-full',
                getWorkloadColor(employee.currentWorkload.status)
              ]">
                {{ employee.currentWorkload.status.toUpperCase() }}
              </span>
            </div>
            <div class="bg-gray-200 rounded-full h-2">
              <div
                :class="[
                  'h-2 rounded-full transition-all',
                  getWorkloadBarColor(employee.currentWorkload.status)
                ]"
                :style="{ width: `${Math.min(100, employee.currentWorkload.percentage)}%` }"
              ></div>
            </div>
            <div class="flex justify-between text-xs text-gray-600 mt-1">
              <span>{{ employee.currentWorkload.currentTasks }} tasks</span>
              <span>{{ employee.currentWorkload.percentage }}%</span>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex space-x-2">
            <button
              @click="viewEmployeeDetails(employee)"
              class="flex-1 bg-blue-600 text-white text-sm py-2 rounded-lg hover:bg-blue-700 transition-colors"
            >
              👤 Details
            </button>
            <button
              @click="editEmployee(employee)"
              class="flex-1 bg-gray-600 text-white text-sm py-2 rounded-lg hover:bg-gray-700 transition-colors"
            >
              ✏️ Edit
            </button>
          </div>
        </div>
      </div>

      <!-- No employees message -->
      <div v-else-if="!loading" class="text-center py-12">
        <div class="text-gray-500">
          <div class="text-4xl mb-4">👥</div>
          <h3 class="text-lg font-medium text-gray-900 mb-2">No employees found</h3>
          <p class="text-gray-600 mb-4">
            {{ filters.search || filters.active || filters.workload
            ? 'No employees match your current filters.'
            : 'Get started by adding your first employee.' }}
          </p>
          <button
            v-if="!filters.search && !filters.active && !filters.workload"
            @click="showForm = true"
            class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
          >
            ➕ Add First Employee
          </button>
        </div>
      </div>
    </div>

    <!-- Planning View -->
    <div v-else-if="viewMode === 'planning'">
      <div v-if="employeesWithTasks.length > 0" class="space-y-6">
        <div
          v-for="employee in employeesWithTasks"
          :key="employee.id"
          class="bg-white rounded-lg border border-gray-200 p-6"
        >
          <!-- Employee header with workload -->
          <div class="flex justify-between items-center mb-4">
            <div class="flex items-center space-x-3">
              <div class="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                <span class="text-blue-600 font-semibold">
                  {{ employee.name.split(' ').map(n => n[0]).join('') }}
                </span>
              </div>
              <div>
                <h3 class="font-semibold text-gray-900">{{ employee.name }}</h3>
                <p class="text-sm text-gray-600">
                  {{ employee.tasks.length }} task{{ employee.tasks.length !== 1 ? 's' : '' }} planned
                </p>
              </div>
            </div>

            <div class="text-right">
              <div class="flex items-center space-x-2 mb-1">
                <span class="text-sm text-gray-600">Workload:</span>
                <span :class="[
                  'px-2 py-1 rounded-full text-xs font-medium',
                  employee.status === 'available' ? 'bg-green-100 text-green-800' :
                  employee.status === 'full' ? 'bg-yellow-100 text-yellow-800' :
                  'bg-red-100 text-red-800'
                ]">
                  {{ employee.status.toUpperCase() }}
                </span>
              </div>
              <div class="text-sm text-gray-600">
                {{ Math.round(employee.totalMinutes / 60 * 10) / 10 }}h / {{ Math.round(employee.maxMinutes / 60) }}h
              </div>
            </div>
          </div>

          <!-- Workload bar -->
          <div class="mb-4">
            <div class="bg-gray-200 rounded-full h-2">
              <div
                :class="[
                  'h-2 rounded-full transition-all',
                  employee.status === 'available' ? 'bg-green-500' :
                  employee.status === 'full' ? 'bg-yellow-500' :
                  'bg-red-500'
                ]"
                :style="{ width: `${Math.min(100, (employee.totalMinutes / employee.maxMinutes) * 100)}%` }"
              ></div>
            </div>
          </div>

          <!-- Tasks list -->
          <div v-if="employee.tasks.length > 0" class="space-y-3">
            <div
              v-for="task in employee.tasks"
              :key="task.id"
              class="border border-gray-200 rounded-lg p-4"
            >
              <div class="flex justify-between items-start">
                <div class="flex-1">
                  <div class="flex items-center space-x-2 mb-2">
                    <span class="font-medium">{{ task.orderNumber || `Order-${task.id}` }}</span>
                    <span :class="[
                      'px-2 py-1 rounded-full text-xs font-medium',
                      getPriorityColor(task.priority)
                    ]">
                      {{ task.priority }}
                    </span>
                    <span :class="[
                      'px-2 py-1 rounded-full text-xs font-medium',
                      getStatusColor(task.status)
                    ]">
                      {{ task.status }}
                    </span>
                  </div>

                  <div class="grid grid-cols-2 gap-4 text-sm text-gray-600">
                    <div>
                      <span class="font-medium">Time:</span>
                      {{ task.startTime }} - {{ task.endTime }}
                    </div>
                    <div>
                      <span class="font-medium">Duration:</span>
                      {{ Math.round(task.duration / 60 * 10) / 10 }}h
                    </div>
                    <div>
                      <span class="font-medium">Cards:</span>
                      {{ task.cardCount }} cards
                    </div>
                    <div>
                      <span class="font-medium">Value:</span>
                      ${{ task.amount.toFixed(2) }}
                    </div>
                  </div>
                </div>

                <div class="flex space-x-2 ml-4">
                  <button
                    @click="viewTaskDetails(task)"
                    class="text-blue-600 hover:text-blue-800 text-sm"
                  >
                    Details
                  </button>
                  <button
                    v-if="task.status !== 'COMPLETED'"
                    @click="markTaskCompleted(task)"
                    class="text-green-600 hover:text-green-800 text-sm"
                  >
                    Complete
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- No tasks message -->
          <div v-else class="text-center py-8 text-gray-500">
            <div class="text-2xl mb-2">📋</div>
            <p>No tasks assigned to this employee</p>
          </div>
        </div>
      </div>

      <!-- No employees with tasks -->
      <div v-else-if="!loading" class="text-center py-12">
        <div class="text-gray-500">
          <div class="text-4xl mb-4">📅</div>
          <h3 class="text-lg font-medium text-gray-900 mb-2">No planning data available</h3>
          <p class="text-gray-600 mb-4">
            No employees have been assigned tasks yet.
          </p>
          <button
            @click="goToPlanningPage"
            class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
          >
            📋 Go to Planning
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, inject } from 'vue'
import type { Employee, EmployeeWithTasks, Task, EmployeeFilters } from '@/types/api'
import { apiService } from '@/services/apiService'
import { formatDate } from '@/utils/formatters'

// ========== STATE ==========
const employees = ref<Employee[]>([])
const employeesWithTasks = ref<EmployeeWithTasks[]>([])
const loading = ref(false)
const saving = ref(false)
const loadingMessage = ref('')
const viewMode = ref<'management' | 'planning'>('management')
const showForm = ref(false)
const selectedEmployee = ref<Employee | null>(null)

// Form data
const newEmployee = ref<Employee>({
  lastName: '',
  firstName: '',
  email: '',
  workHoursPerDay: 8,
  active: true
})

const formErrors = ref<Record<string, string>>({})

// Filters
const filters = ref<EmployeeFilters>({
  search: '',
  active: '',
  workload: ''
})

// Message system
const message = ref<{ text: string; type: 'success' | 'error' }>({ text: '', type: 'success' })

// Injected functions
const showNotification = inject('showNotification') as (message: string, type?: string) => void

// ========== COMPUTED ==========
const filteredEmployees = computed(() => {
  return employees.value.filter(employee => {
    // Search filter
    if (filters.value.search) {
      const searchTerm = filters.value.search.toLowerCase()
      const fullName = `${employee.firstName} ${employee.lastName}`.toLowerCase()
      const email = employee.email?.toLowerCase() || ''
      if (!fullName.includes(searchTerm) && !email.includes(searchTerm)) {
        return false
      }
    }

    // Active filter
    if (filters.value.active !== '') {
      if (filters.value.active === 'true' && !employee.active) return false
      if (filters.value.active === 'false' && employee.active) return false
    }

    // Workload filter (if available)
    if (filters.value.workload && employee.currentWorkload) {
      if (employee.currentWorkload.status !== filters.value.workload) return false
    }

    return true
  })
})

// ========== METHODS ==========

/**
 * Load all employees
 */
const loadEmployees = async () => {
  loading.value = true
  loadingMessage.value = 'Loading employees...'

  try {
    employees.value = await apiService.getEmployees()
    console.log('✅ Loaded', employees.value.length, 'employees')
  } catch (error) {
    console.error('❌ Error loading employees:', error)
    showMessage('Error loading employees', 'error')
  } finally {
    loading.value = false
    loadingMessage.value = ''
  }
}

/**
 * Load employees with tasks (for planning view)
 */
const loadEmployeesWithTasks = async () => {
  loading.value = true
  loadingMessage.value = 'Loading employee planning...'

  try {
    employeesWithTasks.value = await apiService.getEmployeesWithTasks()
    console.log('✅ Loaded', employeesWithTasks.value.length, 'employees with tasks')
  } catch (error) {
    console.error('❌ Error loading employee planning:', error)
    showMessage('Error loading employee planning', 'error')
  } finally {
    loading.value = false
    loadingMessage.value = ''
  }
}

/**
 * Refresh data based on current view
 */
const refreshData = async () => {
  if (viewMode.value === 'management') {
    await loadEmployees()
  } else {
    await loadEmployeesWithTasks()
  }
}

/**
 * Save new employee
 */
const saveEmployee = async () => {
  // Validate form
  formErrors.value = {}

  if (!newEmployee.value.lastName.trim()) {
    formErrors.value.lastName = 'Last name is required'
  }

  if (!newEmployee.value.firstName.trim()) {
    formErrors.value.firstName = 'First name is required'
  }

  if (newEmployee.value.email && !isValidEmail(newEmployee.value.email)) {
    formErrors.value.email = 'Please enter a valid email address'
  }

  if (Object.keys(formErrors.value).length > 0) {
    return
  }

  saving.value = true

  try {
    const savedEmployee = await apiService.createEmployee(newEmployee.value)
    employees.value.push(savedEmployee)

    showMessage(`Employee ${savedEmployee.firstName} ${savedEmployee.lastName} created successfully`, 'success')
    cancelForm()

  } catch (error) {
    console.error('❌ Error creating employee:', error)
    showMessage('Error creating employee', 'error')
  } finally {
    saving.value = false
  }
}

/**
 * Cancel form
 */
const cancelForm = () => {
  showForm.value = false
  selectedEmployee.value = null
  newEmployee.value = {
    lastName: '',
    firstName: '',
    email: '',
    workHoursPerDay: 8,
    active: true
  }
  formErrors.value = {}
}

/**
 * Edit employee
 */
const editEmployee = (employee: Employee) => {
  selectedEmployee.value = employee
  newEmployee.value = { ...employee }
  showForm.value = true
}

/**
 * View employee details
 */
const viewEmployeeDetails = (employee: Employee) => {
  // Navigate to employee detail page or show modal
  console.log('View details for employee:', employee.id)
  showNotification?.(`Viewing details for ${employee.firstName} ${employee.lastName}`)
}

/**
 * View task details
 */
const viewTaskDetails = (task: Task) => {
  console.log('View task details:', task.id)
  showNotification?.(`Viewing details for order ${task.orderNumber}`)
}

/**
 * Mark task as completed
 */
const markTaskCompleted = async (task: Task) => {
  try {
    await apiService.completeTask(task.id)
    task.status = 'COMPLETED'
    showNotification?.(`Task ${task.orderNumber} marked as completed`, 'success')
  } catch (error) {
    console.error('❌ Error completing task:', error)
    showNotification?.('Error completing task', 'error')
  }
}

/**
 * Go to planning page
 */
const goToPlanningPage = () => {
  // Navigate to planning page
  console.log('Navigate to planning page')
}

/**
 * Show message
 */
const showMessage = (text: string, type: 'success' | 'error' = 'success') => {
  message.value = { text, type }
  setTimeout(() => {
    message.value = { text: '', type: 'success' }
  }, 5000)
}

/**
 * Email validation
 */
const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

// ========== STYLE HELPERS ==========

const getWorkloadColor = (status: string): string => {
  switch (status) {
    case 'available': return 'bg-green-100 text-green-800'
    case 'full': return 'bg-yellow-100 text-yellow-800'
    case 'overloaded': return 'bg-red-100 text-red-800'
    default: return 'bg-gray-100 text-gray-800'
  }
}

const getWorkloadBarColor = (status: string): string => {
  switch (status) {
    case 'available': return 'bg-green-500'
    case 'full': return 'bg-yellow-500'
    case 'overloaded': return 'bg-red-500'
    default: return 'bg-gray-500'
  }
}

const getPriorityColor = (priority: string): string => {
  switch (priority?.toUpperCase()) {
    case 'URGENT': return 'bg-red-100 text-red-800'
    case 'HIGH': return 'bg-orange-100 text-orange-800'
    case 'MEDIUM': return 'bg-yellow-100 text-yellow-800'
    case 'NORMAL': return 'bg-green-100 text-green-800'
    case 'LOW': return 'bg-gray-100 text-gray-800'
    default: return 'bg-blue-100 text-blue-800'
  }
}

const getStatusColor = (status: string): string => {
  switch (status?.toUpperCase()) {
    case 'COMPLETED': return 'bg-green-100 text-green-800'
    case 'IN_PROGRESS': return 'bg-blue-100 text-blue-800'
    case 'SCHEDULED': return 'bg-purple-100 text-purple-800'
    default: return 'bg-gray-100 text-gray-800'
  }
}

// ========== LIFECYCLE ==========
onMounted(() => {
  console.log('📊 EmployeesView mounted')
  loadEmployees()
})

// Watch view mode changes
import { watch } from 'vue'
watch(viewMode, (newMode) => {
  if (newMode === 'planning') {
    loadEmployeesWithTasks()
  } else {
    loadEmployees()
  }
})
</script>

<style scoped>
.employees-view {
  @apply max-w-7xl mx-auto p-6;
}

/* Custom scrollbar for task lists */
.space-y-3 {
  max-height: 400px;
  overflow-y: auto;
}

.space-y-3::-webkit-scrollbar {
  width: 4px;
}

.space-y-3::-webkit-scrollbar-track {
  @apply bg-gray-100 rounded;
}

.space-y-3::-webkit-scrollbar-thumb {
  @apply bg-gray-400 rounded;
}

.space-y-3::-webkit-scrollbar-thumb:hover {
  @apply bg-gray-500;
}
</style>
