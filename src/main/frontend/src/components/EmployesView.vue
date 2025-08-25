<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="max-w-7xl mx-auto">

      <!-- ✅ EN-TÊTE UNIFIÉ avec modes de vue -->
      <div class="flex justify-between items-center mb-6">
        <div>
          <h1 class="text-3xl font-bold text-gray-900">👥 Employee Management & Planning</h1>
          <p class="text-gray-600 mt-1">
            {{ modeVue === 'gestion' ? 'Manage employees and their information' : 'View employee schedules and planning' }}
          </p>
        </div>

        <!-- Sélecteur de mode -->
        <div class="flex bg-white rounded-lg shadow-sm border p-1">
          <button
            @click="modeVue = 'gestion'"
            :class="[
              'px-4 py-2 rounded-md text-sm font-medium transition-colors',
              modeVue === 'gestion' ? 'bg-blue-600 text-white' : 'text-gray-600 hover:text-gray-900'
            ]"
          >
            👤 Employee Management
          </button>
          <button
            @click="modeVue = 'planning'"
            :class="[
              'px-4 py-2 rounded-md text-sm font-medium transition-colors',
              modeVue === 'planning' ? 'bg-blue-600 text-white' : 'text-gray-600 hover:text-gray-900'
            ]"
          >
            📋 Planning View
          </button>
        </div>
      </div>

      <!-- MODE GESTION -->
      <div v-if="modeVue === 'gestion' && !employeSelectionne">
        <!-- Boutons d'action pour la gestion -->
        <div class="flex justify-between items-center mb-6">
          <div class="flex space-x-3">
            <button
              @click="actualiserDonnees"
              :disabled="loading"
              class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50"
            >
              {{ loading ? '⏳ Loading...' : '🔄 Refresh' }}
            </button>
            <button
              @click="afficherFormulaireCreation"
              class="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700"
            >
              ➕ Add Employee
            </button>
          </div>
          <div class="text-sm text-gray-600">
            {{ employesListe.length }} employee{{ employesListe.length !== 1 ? 's' : '' }} registered
          </div>
        </div>

        <!-- Liste des employés en mode gestion -->
        <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          <div
            v-for="employe in employesListe"
            :key="employe.id"
            class="bg-white rounded-lg shadow-sm border hover:shadow-md transition-shadow"
          >
            <div class="p-6">
              <div class="flex items-center justify-between mb-4">
                <div class="flex items-center">
                  <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
                    <span class="text-blue-600 font-semibold text-lg">
                      {{ employe.fullName?.charAt(0) || '?' }}
                    </span>
                  </div>
                  <div class="ml-4">
                    <h3 class="text-lg font-semibold text-gray-900">{{ employe.fullName || 'Unknown' }}</h3>
                    <p class="text-sm text-gray-600">{{ employe.email }}</p>
                  </div>
                </div>
                <span :class="[
                  'px-2 py-1 text-xs rounded-full',
                  employe.active ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                ]">
                  {{ employe.active ? 'Active' : 'Inactive' }}
                </span>
              </div>

              <div class="space-y-2 text-sm">
                <div class="flex justify-between">
                  <span class="text-gray-600">Work hours/day:</span>
                  <span class="font-medium">{{ employe.workHoursPerDay || 8 }}h</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-600">Creation date:</span>
                  <span class="font-medium">{{ formatDate(employe.creationDate) }}</span>
                </div>
              </div>

              <div class="mt-4 flex space-x-2">
                <button
                  @click="voirDetailEmploye(employe.id)"
                  class="flex-1 bg-blue-50 text-blue-600 px-3 py-2 rounded text-sm font-medium hover:bg-blue-100"
                >
                  👁️ View Details
                </button>
                <button
                  @click="voirPlanningEmploye(employe.id)"
                  class="flex-1 bg-green-50 text-green-600 px-3 py-2 rounded text-sm font-medium hover:bg-green-100"
                >
                  📋 View Planning
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- MODE PLANNING -->
      <div v-else-if="modeVue === 'planning' && !employeSelectionne">
        <!-- Contrôles de planning -->
        <div class="bg-white rounded-lg shadow-sm border p-6 mb-6">
          <div class="flex justify-between items-center mb-4">
            <h2 class="text-lg font-semibold text-gray-900">📅 Planning Dashboard</h2>
            <input
              v-model="selectedDate"
              type="date"
              class="border rounded-lg px-3 py-2"
            >
          </div>

          <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-4">
            <div class="bg-blue-50 rounded-lg p-4">
              <div class="text-blue-600 text-sm font-medium">Total Employees</div>
              <div class="text-2xl font-bold text-blue-900">{{ employesPlanning.length }}</div>
            </div>
            <div class="bg-green-50 rounded-lg p-4">
              <div class="text-green-600 text-sm font-medium">Available</div>
              <div class="text-2xl font-bold text-green-900">
                {{ employesPlanning.filter(e => e.available).length }}
              </div>
            </div>
            <div class="bg-yellow-50 rounded-lg p-4">
              <div class="text-yellow-600 text-sm font-medium">Busy</div>
              <div class="text-2xl font-bold text-yellow-900">
                {{ employesPlanning.filter(e => e.status === 'busy').length }}
              </div>
            </div>
            <div class="bg-red-50 rounded-lg p-4">
              <div class="text-red-600 text-sm font-medium">Overloaded</div>
              <div class="text-2xl font-bold text-red-900">
                {{ employesPlanning.filter(e => e.status === 'overloaded').length }}
              </div>
            </div>
          </div>
        </div>

        <!-- Liste des employés avec planning -->
        <div class="grid gap-6">
          <div
            v-for="employe in employesPlanning"
            :key="employe.id"
            class="bg-white rounded-lg shadow-sm border"
          >
            <div class="p-6">
              <!-- En-tête employé -->
              <div class="flex items-center justify-between mb-4">
                <div class="flex items-center">
                  <div class="w-16 h-16 bg-gradient-to-r from-blue-500 to-purple-600 rounded-full flex items-center justify-center">
                    <span class="text-white font-bold text-xl">
                      {{ employe.name?.charAt(0) || '?' }}
                    </span>
                  </div>
                  <div class="ml-4">
                    <h3 class="text-xl font-bold text-gray-900">{{ employe.name || 'Unknown Employee' }}</h3>
                    <p class="text-gray-600">{{ employe.taskCount || 0 }} active tasks</p>
                  </div>
                </div>

                <div class="text-right">
                  <span :class="[
                    'px-3 py-1 rounded-full text-sm font-medium',
                    employe.status === 'available' ? 'bg-green-100 text-green-800' :
                    employe.status === 'busy' ? 'bg-yellow-100 text-yellow-800' :
                    'bg-red-100 text-red-800'
                  ]">
                    {{ employe.status === 'available' ? '✅ Available' :
                    employe.status === 'busy' ? '⏳ Busy' : '🔴 Overloaded' }}
                  </span>
                  <div class="text-sm text-gray-600 mt-1">
                    {{ employe.totalMinutes || 0 }}min / {{ employe.maxMinutes || 480 }}min
                  </div>
                </div>
              </div>

              <!-- Barre de progression -->
              <div class="mb-4">
                <div class="flex justify-between text-sm mb-1">
                  <span class="text-gray-600">Workload</span>
                  <span class="text-gray-900 font-medium">
                    {{ Math.round(((employe.totalMinutes || 0) / (employe.maxMinutes || 480)) * 100) }}%
                  </span>
                </div>
                <div class="w-full bg-gray-200 rounded-full h-2">
                  <div
                    :class="[
                      'h-2 rounded-full transition-all duration-300',
                      employe.status === 'overloaded' ? 'bg-red-500' :
                      employe.status === 'busy' ? 'bg-yellow-500' : 'bg-green-500'
                    ]"
                    :style="{ width: Math.min(((employe.totalMinutes || 0) / (employe.maxMinutes || 480)) * 100, 100) + '%' }"
                  ></div>
                </div>
              </div>

              <!-- Actions planning -->
              <div class="flex space-x-3">
                <button
                  @click="voirDetailEmployePlanning(employe.id)"
                  class="flex-1 bg-blue-50 text-blue-600 px-4 py-2 rounded-lg text-sm font-medium hover:bg-blue-100 transition-colors"
                >
                  👁️ View Orders & Cards
                </button>
                <button
                  class="bg-gray-50 text-gray-600 px-4 py-2 rounded-lg text-sm font-medium hover:bg-gray-100 transition-colors"
                >
                  📊 {{ employe.taskCount || 0 }} tasks
                </button>
                <button
                  class="bg-green-50 text-green-600 px-4 py-2 rounded-lg text-sm font-medium hover:bg-green-100 transition-colors"
                >
                  💳 {{ employe.cardCount || 0 }} cards
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ✅ DÉTAIL EMPLOYÉ (unifié pour les deux modes) -->
      <div v-else-if="employeSelectionne">
        <EmployeeDetailPage
          :employeeId="employeSelectionne"
          :selectedDate="selectedDate"
          :mode="modeVue"
          @back="retourListeEmployes"
          @refresh="actualiserDonnees"
        />
      </div>

      <!-- Message de chargement -->
      <div v-if="loading && !employeSelectionne" class="text-center py-12">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
        <p class="text-gray-600">Loading employee data...</p>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { apiService } from '../services/api'
import EmployeeDetailPage from './EmployeeDetailPage.vue'

// ========== TYPES ==========
interface NouvelEmploye {
  nom: string
  prenom: string
  email: string
  heuresTravailParJour: number
}

// ========== ÉTAT RÉACTIF ==========
const selectedDate = ref(new Date().toISOString().split('T')[0])
const modeVue = ref<'gestion' | 'planning'>('gestion') // ✅ Mode de vue unifié
const employeSelectionne = ref<string | null>(null)
const loading = ref(false)

// Données employés
const employesListe = ref<any[]>([]) // Mode gestion
const employesPlanning = ref<any[]>([]) // Mode planning

// ========== MÉTHODES ==========

/**
 * Actualiser les données selon le mode
 */
const actualiserDonnees = async () => {
  loading.value = true
  try {
    if (modeVue.value === 'gestion') {
      await chargerEmployesGestion()
    } else {
      await chargerEmployesPlanning()
    }
  } catch (error) {
    console.error('Erreur actualisation:', error)
  } finally {
    loading.value = false
  }
}

/**
 * Charger les employés pour la gestion
 */
const chargerEmployesGestion = async () => {
  try {
    const response = await fetch('/api/employees/all')
    if (response.ok) {
      const data = await response.json()
      employesListe.value = data.employees || data || []
    }
  } catch (error) {
    console.error('Erreur chargement employés gestion:', error)
    employesListe.value = []
  }
}

/**
 * Charger les employés avec données de planning
 */
const chargerEmployesPlanning = async () => {
  try {
    const response = await fetch(`/api/employees/planning-data?date=${selectedDate.value}`)
    if (response.ok) {
      const data = await response.json()
      employesPlanning.value = data.employees || data || []
    }
  } catch (error) {
    console.error('Erreur chargement planning employés:', error)
    employesPlanning.value = []
  }
}

/**
 * Voir le détail d'un employé (mode gestion)
 */
const voirDetailEmploye = (employeId: string) => {
  employeSelectionne.value = employeId
}

/**
 * Voir le planning d'un employé (depuis mode gestion)
 */
const voirPlanningEmploye = (employeId: string) => {
  modeVue.value = 'planning'
  employeSelectionne.value = employeId
}

/**
 * Voir le détail d'un employé (mode planning)
 */
const voirDetailEmployePlanning = (employeId: string) => {
  employeSelectionne.value = employeId
}

/**
 * Retour à la liste des employés
 */
const retourListeEmployes = () => {
  employeSelectionne.value = null
}

/**
 * Afficher le formulaire de création
 */
const afficherFormulaireCreation = () => {
  // TODO: Implémenter la modal de création d'employé
  console.log('Afficher formulaire création employé')
}

/**
 * Formater une date
 */
const formatDate = (date: string | null) => {
  if (!date) return 'N/A'
  return new Date(date).toLocaleDateString('fr-FR')
}

// ========== WATCHERS ==========

// Actualiser les données quand le mode change
watch(modeVue, (newMode) => {
  employeSelectionne.value = null // Reset selection
  actualiserDonnees()
})

// Actualiser les données de planning quand la date change
watch(selectedDate, () => {
  if (modeVue.value === 'planning') {
    chargerEmployesPlanning()
  }
})

// ========== LIFECYCLE ==========
onMounted(() => {
  actualiserDonnees()
})
</script>

<style scoped>
.transition-colors {
  transition: background-color 0.2s, color 0.2s;
}

.transition-all {
  transition: all 0.3s ease;
}
</style>
