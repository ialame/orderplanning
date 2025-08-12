<template>
  <div class="max-w-7xl mx-auto px-6 py-8 bg-gray-100 min-h-screen">
    <div class="bg-white rounded-lg shadow-lg p-6">

      <!-- ✅ HEADER WITH NAVIGATION -->
      <div class="flex items-center justify-between mb-6">
        <div class="flex items-center space-x-4">
          <h1 class="text-3xl font-bold text-gray-900 flex items-center gap-3">
            👥 Employees
          </h1>
          <p class="text-gray-600">Complete management of employees and their planning</p>
        </div>

        <!-- Mode Toggle Buttons -->
        <div class="flex bg-gray-100 rounded-lg p-1">
          <button
            @click="changerModeVue('gestion')"
            :class="[
              'px-4 py-2 rounded-md text-sm font-medium transition-colors',
              modeVue === 'gestion' ? 'bg-white text-blue-600 shadow-sm' : 'text-gray-600 hover:text-gray-900'
            ]"
          >
            👥 Management
          </button>
          <button
            @click="changerModeVue('planning')"
            :class="[
              'px-4 py-2 rounded-md text-sm font-medium transition-colors',
              modeVue === 'planning' ? 'bg-white text-blue-600 shadow-sm' : 'text-gray-600 hover:text-gray-900'
            ]"
          >
            📅 Planning
          </button>
        </div>
      </div>

      <!-- ✅ ACTION BUTTONS -->
      <div class="flex items-center justify-between mb-6">
        <div class="flex space-x-3">
          <button
            v-if="modeVue === 'gestion'"
            @click="afficherFormulaireCreation"
            class="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-colors"
          >
            ➕ New Employee
          </button>
          <div v-if="modeVue === 'planning'" class="flex items-center space-x-3">
            <label class="text-sm font-medium text-gray-700">Date:</label>
            <input
              v-model="selectedDate"
              type="date"
              class="border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>

        <button
          @click="actualiserDonnees"
          :disabled="loading"
          class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
        >
          {{ loading ? '🔄' : '🔄 Refresh' }}
        </button>
      </div>

      <!-- ✅ FEEDBACK MESSAGES -->
      <div v-if="message.text" :class="[
        'mb-4 p-4 rounded-lg border',
        message.type === 'success' ? 'bg-green-50 text-green-800 border-green-200' : 'bg-red-50 text-red-800 border-red-200'
      ]">
        {{ message.text }}
      </div>

      <!-- ✅ MAIN CONTENT -->
      <div v-if="!employeSelectionne">

        <!-- ========== MANAGEMENT MODE ========== -->
        <div v-if="modeVue === 'gestion'">

          <!-- Employee Creation Form -->
          <div v-if="showFormulaire" class="bg-white rounded-lg shadow-md p-6 mb-6">
            <h3 class="text-lg font-semibold text-gray-900 mb-4">➕ Create New Employee</h3>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Last Name</label>
                <input
                  v-model="nouvelEmploye.nom"
                  type="text"
                  class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Last name"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">First Name</label>
                <input
                  v-model="nouvelEmploye.prenom"
                  type="text"
                  class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="First name"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
                <input
                  v-model="nouvelEmploye.email"
                  type="email"
                  class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="email@example.com"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Work Hours/Day</label>
                <input
                  v-model.number="nouvelEmploye.heuresTravailParJour"
                  type="number"
                  min="1"
                  max="12"
                  class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
            </div>

            <div class="flex space-x-3 mt-6">
              <button
                @click="creerEmploye"
                :disabled="!peutCreerEmploye || loading"
                class="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 disabled:opacity-50"
              >
                {{ loading ? '⏳ Creating...' : '✅ Create' }}
              </button>
              <button
                @click="annulerCreation"
                class="bg-gray-600 text-white px-4 py-2 rounded-lg hover:bg-gray-700"
              >
                ❌ Cancel
              </button>
            </div>
          </div>

          <!-- Loading State -->
          <div v-if="loading" class="text-center py-12">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
            <p class="mt-4 text-gray-600">Loading employees...</p>
          </div>

          <!-- Empty State -->
          <div v-else-if="employesListe.length === 0 && !loading && !showFormulaire" class="text-center py-12">
            <div class="text-6xl mb-4">👥</div>
            <h3 class="text-xl font-medium text-gray-900 mb-2">No employees yet</h3>
            <p class="text-gray-600 mb-6">Start by creating your first employee to begin planning Pokemon card orders</p>
            <button
              @click="showFormulaire = true"
              class="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700"
            >
              ➕ Create First Employee
            </button>
          </div>

          <!-- Employee List - Management Mode -->
          <div v-else-if="!loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div
              v-for="employe in employesListe"
              :key="employe.id"
              @click="voirDetailEmploye(employe.id)"
              class="bg-white rounded-lg shadow-md hover:shadow-lg transition-all cursor-pointer border-l-4 border-blue-500 p-6"
            >
              <!-- Avatar and Name -->
              <div class="flex items-center mb-4">
                <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center text-blue-600 font-bold text-lg">
                  {{ getInitiales(employe) }}
                </div>
                <div class="ml-3">
                  <h3 class="text-lg font-semibold text-gray-900">
                    {{ employe.fullName || `${employe.firstName} ${employe.lastName}` }}
                  </h3>
                  <p class="text-sm text-gray-500">{{ employe.email }}</p>
                </div>
              </div>

              <!-- Information -->
              <div class="space-y-2 text-sm">
                <div class="flex justify-between">
                  <span>⏰ Hours/day:</span>
                  <span class="font-medium">{{ employe.workHoursPerDay }}h</span>
                </div>
                <div class="flex justify-between">
                  <span>📅 Since:</span>
                  <span class="font-medium">{{ formatDate(employe.creationDate) }}</span>
                </div>
                <div class="flex justify-between">
                  <span>📊 Status:</span>
                  <span :class="[
                    'px-2 py-1 rounded text-xs font-medium',
                    employe.active ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                  ]">
                    {{ employe.active ? 'Active' : 'Inactive' }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ========== PLANNING MODE ========== -->
        <div v-else>
          <!-- Statistics Cards -->
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
            <div class="bg-blue-50 p-4 rounded-lg">
              <div class="text-2xl font-bold text-blue-900">{{ employesPlanning.length }}</div>
              <div class="text-sm text-blue-800">👥 Active Employees</div>
            </div>
            <div class="bg-green-50 p-4 rounded-lg">
              <div class="text-2xl font-bold text-green-900">{{ employesDisponibles }}</div>
              <div class="text-sm text-green-800">✅ Available</div>
            </div>
            <div class="bg-yellow-50 p-4 rounded-lg">
              <div class="text-2xl font-bold text-yellow-900">{{ employesCharges }}</div>
              <div class="text-sm text-yellow-800">⚠️ Loaded</div>
            </div>
            <div class="bg-red-50 p-4 rounded-lg">
              <div class="text-2xl font-bold text-red-900">{{ employesSurcharges }}</div>
              <div class="text-sm text-red-800">🚨 Overloaded</div>
            </div>
          </div>

          <!-- Loading State -->
          <div v-if="loading" class="text-center py-12">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
            <p class="mt-4 text-gray-600">Loading planning data...</p>
          </div>

          <!-- Employee Grid with Planning -->
          <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div
              v-for="employe in employesPlanning"
              :key="employe.id"
              @click="voirDetailEmploye(employe.id)"
              :class="[
                'bg-white rounded-lg shadow-md hover:shadow-lg transition-all cursor-pointer border-l-4 p-6',
                getStatusBorderColor(employe.status)
              ]"
            >
              <!-- Avatar and Name -->
              <div class="flex items-center mb-4">
                <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center text-blue-600 font-bold text-lg">
                  {{ getInitiales(employe) }}
                </div>
                <div class="ml-3">
                  <h3 class="text-lg font-semibold text-gray-900">{{ employe.name }}</h3>
                  <p class="text-sm text-gray-500">{{ employe.cardCount || 0 }} cards</p>
                </div>
              </div>

              <!-- Planning Information -->
              <div class="space-y-3">
                <!-- Status -->
                <div class="flex justify-between items-center">
                  <span class="text-sm font-medium">Status:</span>
                  <span :class="[
                    'px-2 py-1 rounded text-xs font-medium',
                    employe.status === 'overloaded' ? 'bg-red-100 text-red-800' :
                    employe.status === 'full' ? 'bg-yellow-100 text-yellow-800' :
                    'bg-green-100 text-green-800'
                  ]">
                    {{ getStatusText(employe.status) }}
                  </span>
                </div>

                <!-- Workload -->
                <div class="space-y-1">
                  <div class="flex justify-between text-sm">
                    <span>Workload:</span>
                    <span class="font-medium">
                      {{ formatTime(employe.totalMinutes || 0) }} / {{ formatTime(employe.maxMinutes || 480) }}
                    </span>
                  </div>
                  <div class="w-full bg-gray-200 rounded-full h-2">
                    <div
                      :class="[
                        'h-2 rounded-full transition-all',
                        employe.status === 'overloaded' ? 'bg-red-500' :
                        employe.status === 'full' ? 'bg-yellow-500' : 'bg-green-500'
                      ]"
                      :style="{ width: Math.min(((employe.totalMinutes || 0) / (employe.maxMinutes || 480)) * 100, 100) + '%' }"
                    ></div>
                  </div>
                </div>

                <!-- Planning Actions -->
                <div class="flex space-x-2 mt-4">
                  <button class="flex-1 bg-blue-50 text-blue-600 px-3 py-2 rounded text-sm font-medium hover:bg-blue-100">
                    👁️ Details
                  </button>
                  <button class="flex-1 bg-gray-50 text-gray-600 px-3 py-2 rounded text-sm font-medium hover:bg-gray-100">
                    📋 {{ employe.taskCount || 0 }} tasks
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ✅ EMPLOYEE DETAIL COMPONENT (unified for both modes) -->
      <div v-else>
        <EmployeeDetailPage
          :employeeId="employeSelectionne"
          :selectedDate="selectedDate"
          @back="retourListeEmployes"
          @refresh="actualiserDonnees"
        />
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, inject } from 'vue'
import EmployeeDetailPage from './EmployeeDetailPage.vue'

// ========== TYPES ==========
interface NouvelEmploye {
  nom: string
  prenom: string
  email: string
  heuresTravailParJour: number
}

interface Employee {
  id: string
  name?: string
  fullName?: string
  firstName?: string
  lastName?: string
  email: string
  workHoursPerDay?: number
  active?: boolean
  creationDate?: string
  totalMinutes?: number
  maxMinutes?: number
  status?: 'overloaded' | 'available' | 'full'
  taskCount?: number
  cardCount?: number
}

// ========== SERVICE INJECTION ==========
const showNotification = inject<(message: string, type: 'success' | 'error') => void>('showNotification')

// ========== REACTIVE STATE ==========
const selectedDate = ref(new Date().toISOString().split('T')[0])
const modeVue = ref<'gestion' | 'planning'>('gestion')
const employeSelectionne = ref<string | null>(null)
const loading = ref(false)

// Employee data
const employesListe = ref<Employee[]>([])     // Management mode
const employesPlanning = ref<Employee[]>([])  // Planning mode

// Creation form
const showFormulaire = ref(false)
const nouvelEmploye = ref<NouvelEmploye>({
  nom: '',
  prenom: '',
  email: '',
  heuresTravailParJour: 8
})

// Messages
const message = ref({
  text: '',
  type: 'success' as 'success' | 'error'
})

// ========== COMPUTED PROPERTIES ==========
const peutCreerEmploye = computed(() => {
  return nouvelEmploye.value.nom.trim() !== '' &&
    nouvelEmploye.value.prenom.trim() !== '' &&
    nouvelEmploye.value.email.trim() !== '' &&
    nouvelEmploye.value.heuresTravailParJour > 0
})

const employesDisponibles = computed(() =>
  employesPlanning.value.filter(emp => emp.status === 'available').length
)

const employesCharges = computed(() =>
  employesPlanning.value.filter(emp => emp.status === 'full').length
)

const employesSurcharges = computed(() =>
  employesPlanning.value.filter(emp => emp.status === 'overloaded').length
)

// ========== METHODS ==========

// Navigation and modes
const changerModeVue = (mode: 'gestion' | 'planning') => {
  modeVue.value = mode
  employeSelectionne.value = null
  actualiserDonnees()
}

const voirDetailEmploye = (employeId: string) => {
  employeSelectionne.value = employeId
}

const retourListeEmployes = () => {
  employeSelectionne.value = null
}

// Employee management
const afficherFormulaireCreation = () => {
  showFormulaire.value = true
}

const annulerCreation = () => {
  showFormulaire.value = false
  resetFormulaire()
}

const resetFormulaire = () => {
  nouvelEmploye.value = {
    nom: '',
    prenom: '',
    email: '',
    heuresTravailParJour: 8
  }
}

const creerEmploye = async () => {
  try {
    loading.value = true
    console.log('💾 Creating employee:', nouvelEmploye.value)

    const requestData = {
      lastName: nouvelEmploye.value.nom,
      firstName: nouvelEmploye.value.prenom,
      email: nouvelEmploye.value.email,
      workHoursPerDay: nouvelEmploye.value.heuresTravailParJour
    }

    console.log('📤 Request payload:', requestData)

    // Use Vite proxy instead of absolute URL
    const response = await fetch('/api/employees', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(requestData)
    })

    console.log('📥 Response status:', response.status)
    const result = await response.json()
    console.log('📥 Response data:', result)

    if (response.ok && result.success) {
      message.value = {
        text: 'Employee created successfully!',
        type: 'success'
      }
      showNotification?.('Employee created successfully!', 'success')
      annulerCreation()
      await actualiserDonnees()
    } else {
      message.value = {
        text: result.message || 'Error creating employee',
        type: 'error'
      }
      showNotification?.(result.message || 'Error creating employee', 'error')
    }
  } catch (error) {
    console.error('❌ Error creating employee:', error)
    message.value = {
      text: 'Network error creating employee',
      type: 'error'
    }
    showNotification?.('Network error creating employee', 'error')
  } finally {
    loading.value = false
  }
}

// Data loading - REAL DATA FROM DATABASE
const chargerEmployesGestion = async () => {
  try {
    loading.value = true
    console.log('📥 Loading employees from database...')

    // ✅ Appel API corrigé
    const response = await fetch('/api/employees/active', {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      }
    })

    console.log('📥 Response status:', response.status)

    if (response.ok) {
      const data = await response.json()
      console.log('✅ Raw data from API:', data)

      if (!Array.isArray(data)) {
        throw new Error('API returned non-array data')
      }

      // ✅ Mapping sécurisé pour l'affichage
      employesListe.value = data.map((emp) => {
        console.log('👤 Processing employee:', emp)

        return {
          id: emp.id,
          firstName: emp.firstName,
          lastName: emp.lastName,
          fullName: emp.fullName || `${emp.firstName || ''} ${emp.lastName || ''}`.trim(),
          nomComplet: emp.fullName || `${emp.firstName || ''} ${emp.lastName || ''}`.trim(), // Pour template français
          email: emp.email || 'No email',
          workHoursPerDay: emp.workHoursPerDay || 8,
          heuresTravailParJour: emp.workHoursPerDay || 8, // Pour template français
          active: emp.active !== false, // Default true si undefined
          actif: emp.active !== false, // Pour template français
          creationDate: emp.creationDate,
          dateCreation: emp.creationDate // Pour template français
        }
      })

      console.log('✅ Mapped employees for display:', employesListe.value)

      if (employesListe.value.length === 0) {
        message.value = {
          text: 'No employees found in database. Create your first employee!',
          type: 'error'
        }
      } else {
        message.value = {
          text: `✅ ${employesListe.value.length} employees loaded from database`,
          type: 'success'
        }
      }
    } else {
      console.error('❌ Failed to load employees, status:', response.status)
      const errorText = await response.text()
      console.error('❌ Error response:', errorText)

      employesListe.value = []
      message.value = {
        text: `Failed to load employees (HTTP ${response.status})`,
        type: 'error'
      }
    }
  } catch (error) {
    console.error('❌ Network error loading employees:', error)
    employesListe.value = []
    message.value = {
      text: `Network error: ${error.message}. Check backend connection.`,
      type: 'error'
    }
  } finally {
    loading.value = false
  }
}

// ✅ Fonction utilitaire pour l'affichage
const getInitiales = (employe) => {
  // Essayer plusieurs formats de nom
  if (employe.firstName && employe.lastName) {
    return (employe.firstName[0] + employe.lastName[0]).toUpperCase()
  }
  if (employe.fullName) {
    const parts = employe.fullName.split(' ')
    return parts.map(p => p[0]).join('').toUpperCase().slice(0, 2)
  }
  if (employe.nomComplet) {
    const parts = employe.nomComplet.split(' ')
    return parts.map(p => p[0]).join('').toUpperCase().slice(0, 2)
  }
  return '??'
}

const chargerEmployesPlanning = async () => {
  try {
    loading.value = true
    console.log('📅 Loading employee planning for date:', selectedDate.value)

    // Call the real API endpoint for planning data
    const response = await fetch(`/api/employees/planning-data?date=${selectedDate.value}`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      }
    })

    console.log('📥 Planning response status:', response.status)

    if (response.ok) {
      const data = await response.json()
      console.log('✅ Real planning data loaded:', data)
      employesPlanning.value = data

      if (employesPlanning.value.length === 0) {
        message.value = {
          text: 'No employees with planning data found for this date',
          type: 'error'
        }
      }
    } else {
      console.error('❌ Failed to load planning data, status:', response.status)
      employesPlanning.value = []
      message.value = {
        text: 'Failed to load planning data',
        type: 'error'
      }
    }
  } catch (error) {
    console.error('❌ Error loading planning data:', error)
    employesPlanning.value = []
    message.value = {
      text: 'Network error loading planning data',
      type: 'error'
    }
  } finally {
    loading.value = false
  }
}

const actualiserDonnees = async () => {
  console.log('🔄 Refreshing data for mode:', modeVue.value)
  if (modeVue.value === 'gestion') {
    await chargerEmployesGestion()
  } else {
    await chargerEmployesPlanning()
  }
}

// Utilities
const getInitiales = (employe: Employee): string => {
  if (employe.firstName && employe.lastName) {
    return (employe.firstName[0] + employe.lastName[0]).toUpperCase()
  }
  if (employe.name) {
    const parts = employe.name.split(' ')
    return parts.map(p => p[0]).join('').toUpperCase().slice(0, 2)
  }
  if (employe.fullName) {
    const parts = employe.fullName.split(' ')
    return parts.map(p => p[0]).join('').toUpperCase().slice(0, 2)
  }
  return '??'
}

const formatDate = (dateStr: string): string => {
  try {
    return new Date(dateStr).toLocaleDateString('en-US')
  } catch {
    return dateStr || 'N/A'
  }
}

const formatTime = (minutes: number): string => {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return `${hours}h${mins.toString().padStart(2, '0')}`
}

const getStatusText = (status: string): string => {
  switch (status) {
    case 'overloaded': return '🚨 Overloaded'
    case 'available': return '✅ Available'
    case 'full': return '⚠️ Full'
    default: return '📊 Normal'
  }
}

const getStatusBorderColor = (status: string): string => {
  switch (status) {
    case 'overloaded': return 'border-red-500'
    case 'available': return 'border-green-500'
    case 'full': return 'border-yellow-500'
    default: return 'border-blue-500'
  }
}

// ========== WATCHERS ==========
watch(selectedDate, () => {
  if (modeVue.value === 'planning') {
    chargerEmployesPlanning()
  }
})

// ========== LIFECYCLE ==========
onMounted(() => {
  console.log('🚀 EmployeesView mounted, loading initial data...')
  actualiserDonnees()
})
</script>
