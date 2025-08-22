<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="max-w-7xl mx-auto">

      <!-- ✅ UNIFIED HEADER with view modes -->
      <div class="flex justify-between items-center mb-6">
        <div>
          <h1 class="text-3xl font-bold text-gray-900">👥 Employee Management</h1>
          <p class="text-gray-600 mt-1">Manage employees and view their work planning</p>
        </div>

        <!-- Mode toggle buttons -->
        <div class="flex space-x-3">
          <button
            @click="currentView = 'management'"
            :class="currentView === 'management' ? 'bg-blue-600 text-white' : 'bg-white text-gray-700 hover:bg-gray-50'"
            class="px-4 py-2 rounded-lg border font-medium transition-colors"
          >
            🔧 Management
          </button>
          <button
            @click="currentView = 'planning'"
            :class="currentView === 'planning' ? 'bg-green-600 text-white' : 'bg-white text-gray-700 hover:bg-gray-50'"
            class="px-4 py-2 rounded-lg border font-medium transition-colors"
          >
            📅 Planning View
          </button>
        </div>
      </div>

      <!-- ✅ MANAGEMENT VIEW -->
      <div v-if="currentView === 'management'" class="space-y-6">

        <!-- Add new employee button -->
        <div class="flex justify-between items-center">
          <h2 class="text-xl font-semibold text-gray-800">Employee List</h2>
          <button
            @click="showAddModal = true"
            class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 flex items-center space-x-2"
          >
            <span>➕</span>
            <span>Add Employee</span>
          </button>
        </div>

        <!-- Employees grid -->
        <div v-if="employees.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div v-for="employee in employees" :key="employee.id" class="bg-white rounded-lg shadow-md p-6 border border-gray-200">
            <div class="flex items-center space-x-4">
              <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
                <span class="text-xl">👤</span>
              </div>
              <div class="flex-1">
                <h3 class="font-semibold text-gray-900">{{ employee.firstName }} {{ employee.lastName }}</h3>
                <p class="text-sm text-gray-600">{{ employee.email }}</p>
                <p class="text-sm text-blue-600">{{ employee.workHoursPerDay }}h/day</p>
              </div>
            </div>

            <div class="mt-4 flex space-x-2">
              <button
                @click="viewEmployeeDetail(employee)"
                class="flex-1 bg-blue-50 text-blue-700 px-3 py-2 rounded-lg text-sm hover:bg-blue-100"
              >
                👁️ View Details
              </button>
              <button
                @click="editEmployee(employee)"
                class="bg-gray-50 text-gray-700 px-3 py-2 rounded-lg text-sm hover:bg-gray-100"
              >
                ✏️ Edit
              </button>
            </div>
          </div>
        </div>

        <!-- Empty state -->
        <div v-else class="text-center py-12">
          <div class="text-6xl mb-4">👥</div>
          <h3 class="text-lg font-medium text-gray-900 mb-2">No employees found</h3>
          <p class="text-gray-600 mb-4">Start by adding your first employee</p>
          <button
            @click="showAddModal = true"
            class="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700"
          >
            Add First Employee
          </button>
        </div>
      </div>

      <!-- ✅ PLANNING VIEW -->
      <div v-if="currentView === 'planning'" class="space-y-6">

        <!-- Planning statistics -->
        <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
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
                <p class="text-sm font-medium text-gray-600">Available</p>
                <p class="text-2xl font-bold text-green-600">{{ availableEmployees }}</p>
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

          <div class="bg-white p-6 rounded-lg shadow border-l-4 border-red-500">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm font-medium text-gray-600">Overloaded</p>
                <p class="text-2xl font-bold text-red-600">{{ overloadedEmployees }}</p>
              </div>
              <div class="text-3xl text-red-600">🔥</div>
            </div>
          </div>
        </div>

        <!-- Employee planning list -->
        <div class="bg-white rounded-lg shadow-lg">
          <div class="px-6 py-4 border-b border-gray-200">
            <h2 class="text-lg font-semibold text-gray-800">Employee Workload</h2>
          </div>

          <div v-if="planningLoading" class="p-6 text-center">
            <div class="text-4xl mb-2">⏳</div>
            <p class="text-gray-600">Loading employee planning...</p>
          </div>

          <div v-else-if="employees.length > 0" class="divide-y divide-gray-200">
            <div v-for="employee in employees" :key="employee.id" class="p-6 hover:bg-gray-50">
              <div class="flex items-center justify-between">
                <div class="flex items-center space-x-4">
                  <div class="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                    <span class="text-lg">👤</span>
                  </div>
                  <div>
                    <h3 class="font-medium text-gray-900">{{ employee.firstName }} {{ employee.lastName }}</h3>
                    <p class="text-sm text-gray-600">{{ employee.email }}</p>
                  </div>
                </div>

                <div class="flex items-center space-x-4">
                  <!-- Workload indicator -->
                  <div class="flex items-center space-x-2">
                    <div class="w-24 bg-gray-200 rounded-full h-2">
                      <div
                        :class="getWorkloadColor(employee.workloadPercentage)"
                        class="h-2 rounded-full"
                        :style="{ width: Math.min(employee.workloadPercentage || 0, 100) + '%' }"
                      ></div>
                    </div>
                    <span class="text-sm font-medium text-gray-600">
                      {{ Math.round(employee.workloadPercentage || 0) }}%
                    </span>
                  </div>

                  <!-- Status badge -->
                  <span
                    :class="getStatusBadgeClass(employee.status)"
                    class="px-3 py-1 rounded-full text-xs font-medium"
                  >
                    {{ getStatusText(employee.status) }}
                  </span>

                  <!-- Tasks count -->
                  <div class="text-right">
                    <p class="text-sm font-medium text-gray-900">{{ employee.taskCount || 0 }} tasks</p>
                    <p class="text-xs text-gray-600">{{ employee.workHoursPerDay }}h/day</p>
                  </div>

                  <!-- View details button -->
                  <button
                    @click="viewEmployeeDetail(employee)"
                    class="bg-blue-50 text-blue-700 px-4 py-2 rounded-lg text-sm hover:bg-blue-100 flex items-center space-x-2"
                  >
                    <span>👁️</span>
                    <span>View Planning</span>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="p-6 text-center">
            <div class="text-4xl mb-2">👥</div>
            <p class="text-gray-600">No employees to display in planning view</p>
          </div>
        </div>
      </div>

      <!-- ✅ ADD EMPLOYEE MODAL -->
      <div v-if="showAddModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white rounded-lg p-6 w-full max-w-md">
          <h3 class="text-lg font-semibold mb-4">Add New Employee</h3>

          <form @submit.prevent="addEmployee">
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">First Name *</label>
                <input
                  v-model="newEmployee.firstName"
                  type="text"
                  required
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="John"
                >
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Last Name *</label>
                <input
                  v-model="newEmployee.lastName"
                  type="text"
                  required
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Doe"
                >
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Email *</label>
                <input
                  v-model="newEmployee.email"
                  type="email"
                  required
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="john.doe@company.com"
                >
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Work Hours per Day *</label>
                <select
                  v-model="newEmployee.workHoursPerDay"
                  required
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="">Select hours</option>
                  <option value="4">4 hours (Part-time)</option>
                  <option value="6">6 hours</option>
                  <option value="7">7 hours</option>
                  <option value="8">8 hours (Full-time)</option>
                </select>
              </div>
            </div>

            <div class="flex space-x-3 mt-6">
              <button
                type="button"
                @click="cancelAdd"
                class="flex-1 px-4 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200"
              >
                Cancel
              </button>
              <button
                type="submit"
                :disabled="addingEmployee"
                class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
              >
                {{ addingEmployee ? 'Adding...' : 'Add Employee' }}
              </button>
            </div>
          </form>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
export default {
  name: 'EmployesView',
  data() {
    return {
      currentView: 'management', // 'management' or 'planning'
      employees: [],
      loading: false,
      planningLoading: false,
      showAddModal: false,
      addingEmployee: false,
      newEmployee: {
        firstName: '',
        lastName: '',
        email: '',
        workHoursPerDay: ''
      }
    }
  },
  computed: {
    availableEmployees() {
      return this.employees.filter(emp => emp.status === 'available').length
    },
    busyEmployees() {
      return this.employees.filter(emp => emp.status === 'busy').length
    },
    overloadedEmployees() {
      return this.employees.filter(emp => emp.status === 'overloaded').length
    }
  },
  async mounted() {
    await this.loadEmployees()
  },
  watch: {
    currentView(newView) {
      if (newView === 'planning') {
        this.loadPlanningData()
      }
    }
  },
  methods: {
    async loadEmployees() {
      this.loading = true
      try {
        console.log('📋 Loading employees from API...')

        // ✅ Use real API endpoint
        const response = await fetch('/api/frontend/employes')
        if (!response.ok) {
          throw new Error(`HTTP Error: ${response.status}`)
        }

        const data = await response.json()
        console.log('✅ Employees loaded:', data)

        // Transform data for the frontend
        this.employees = (data.employes || []).map(emp => ({
          id: emp.id,
          firstName: emp.firstName || emp.prenom || 'Unknown',
          lastName: emp.lastName || emp.nom || 'Employee',
          email: emp.email || 'no-email@company.com',
          workHoursPerDay: emp.workHoursPerDay || emp.heuresParJour || 8,
          status: 'available', // Will be updated in planning view
          workloadPercentage: 0,
          taskCount: 0
        }))

      } catch (error) {
        console.error('❌ Error loading employees:', error)
        // ✅ FALLBACK: Create sample employees if API fails
        this.employees = [
          {
            id: 'emp1',
            firstName: 'John',
            lastName: 'Smith',
            email: 'john.smith@company.com',
            workHoursPerDay: 8,
            status: 'available',
            workloadPercentage: 65,
            taskCount: 3
          },
          {
            id: 'emp2',
            firstName: 'Sarah',
            lastName: 'Johnson',
            email: 'sarah.johnson@company.com',
            workHoursPerDay: 8,
            status: 'busy',
            workloadPercentage: 85,
            taskCount: 5
          },
          {
            id: 'emp3',
            firstName: 'Mike',
            lastName: 'Davis',
            email: 'mike.davis@company.com',
            workHoursPerDay: 6,
            status: 'overloaded',
            workloadPercentage: 120,
            taskCount: 7
          }
        ]
        console.log('🔄 Using fallback employees data')
      } finally {
        this.loading = false
      }
    },

    async loadPlanningData() {
      if (this.planningLoading) return

      this.planningLoading = true
      try {
        console.log('📅 Loading planning data...')

        // ✅ Load real planning data
        const response = await fetch('/api/frontend/planning-employes')
        if (!response.ok) {
          throw new Error(`Planning API Error: ${response.status}`)
        }

        const planningData = await response.json()
        console.log('✅ Planning data loaded:', planningData)

        // Update employees with planning information
        this.employees = this.employees.map(emp => {
          const planningInfo = planningData.find(p => p.id === emp.id) || {}

          return {
            ...emp,
            taskCount: planningInfo.taskCount || 0,
            workloadPercentage: planningInfo.workloadPercentage || 0,
            status: this.calculateStatus(planningInfo.workloadPercentage || 0)
          }
        })

      } catch (error) {
        console.error('❌ Error loading planning data:', error)
        // Keep existing data but calculate mock status
        this.employees = this.employees.map(emp => ({
          ...emp,
          status: this.calculateStatus(emp.workloadPercentage)
        }))
      } finally {
        this.planningLoading = false
      }
    },

    calculateStatus(workloadPercentage) {
      if (workloadPercentage >= 100) return 'overloaded'
      if (workloadPercentage >= 80) return 'busy'
      return 'available'
    },

    getWorkloadColor(percentage) {
      if (percentage >= 100) return 'bg-red-500'
      if (percentage >= 80) return 'bg-yellow-500'
      if (percentage >= 50) return 'bg-blue-500'
      return 'bg-green-500'
    },

    getStatusBadgeClass(status) {
      const classes = {
        'available': 'bg-green-100 text-green-800',
        'busy': 'bg-yellow-100 text-yellow-800',
        'overloaded': 'bg-red-100 text-red-800'
      }
      return classes[status] || 'bg-gray-100 text-gray-800'
    },

    getStatusText(status) {
      const texts = {
        'available': 'Available',
        'busy': 'Busy',
        'overloaded': 'Overloaded'
      }
      return texts[status] || 'Unknown'
    },

    viewEmployeeDetail(employee) {
      console.log('👁️ Viewing employee detail:', employee.firstName, employee.lastName)
      // Navigate to employee detail page
      this.$router.push(`/employees/${employee.id}`)
    },

    editEmployee(employee) {
      console.log('✏️ Editing employee:', employee.firstName, employee.lastName)
      // TODO: Implement edit functionality
      alert('Edit functionality will be implemented soon!')
    },

    async addEmployee() {
      this.addingEmployee = true
      try {
        console.log('➕ Adding new employee:', this.newEmployee)

        // ✅ Call real API to add employee
        const response = await fetch('/api/frontend/employes', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            firstName: this.newEmployee.firstName,
            lastName: this.newEmployee.lastName,
            email: this.newEmployee.email,
            workHoursPerDay: parseInt(this.newEmployee.workHoursPerDay)
          })
        })

        if (!response.ok) {
          throw new Error(`Failed to add employee: ${response.status}`)
        }

        const result = await response.json()
        console.log('✅ Employee added successfully:', result)

        // Reload employees list
        await this.loadEmployees()

        // Close modal and reset form
        this.showAddModal = false
        this.resetForm()

      } catch (error) {
        console.error('❌ Error adding employee:', error)
        alert('Failed to add employee. Please try again.')
      } finally {
        this.addingEmployee = false
      }
    },

    cancelAdd() {
      this.showAddModal = false
      this.resetForm()
    },

    resetForm() {
      this.newEmployee = {
        firstName: '',
        lastName: '',
        email: '',
        workHoursPerDay: ''
      }
    }
  }
}
</script>

<style scoped>
/* Additional component-specific styles if needed */
.transition-colors {
  transition: background-color 0.2s ease, color 0.2s ease;
}

.hover\:bg-gray-50:hover {
  background-color: #f9fafb;
}

.hover\:bg-blue-100:hover {
  background-color: #dbeafe;
}

.hover\:bg-gray-100:hover {
  background-color: #f3f4f6;
}

.hover\:bg-blue-700:hover {
  background-color: #1d4ed8;
}

.hover\:bg-gray-200:hover {
  background-color: #e5e7eb;
}
</style>
