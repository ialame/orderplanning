<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="max-w-7xl mx-auto">

      <!-- ✅ EN-TÊTE UNIFIÉ avec modes de vue -->
      <div class="flex justify-between items-center mb-6">
        <div>
          <h1 class="text-3xl font-bold text-gray-900">
            👥 Employés
          </h1>
          <p class="text-gray-600 mt-1">
            Gestion complète des employés et de leurs planifications
          </p>
        </div>

        <!-- Actions et sélecteur de mode/date -->
        <div class="flex items-center space-x-3">
          <!-- Mode de vue -->
          <div class="flex bg-gray-200 rounded-lg p-1">
            <button
              @click="changerModeVue('gestion')"
              :class="[
                'px-3 py-1 rounded text-sm font-medium transition-colors',
                modeVue === 'gestion' ? 'bg-white text-blue-600 shadow' : 'text-gray-600'
              ]"
            >
              👥 Gestion
            </button>
            <button
              @click="changerModeVue('planning')"
              :class="[
                'px-3 py-1 rounded text-sm font-medium transition-colors',
                modeVue === 'planning' ? 'bg-white text-blue-600 shadow' : 'text-gray-600'
              ]"
            >
              📅 Planning
            </button>
          </div>

          <!-- Bouton Nouvel Employé (mode gestion seulement) -->
          <button
            v-if="modeVue === 'gestion' && !employeSelectionne"
            @click="afficherFormulaireCreation"
            class="bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700 flex items-center gap-2"
          >
            ➕ Nouvel Employé
          </button>

          <!-- Sélecteur de date (mode planning) -->
          <div v-if="modeVue === 'planning'" class="flex items-center gap-2">
            <label class="text-sm font-medium text-gray-700">Date :</label>
            <input
              v-model="selectedDate"
              type="date"
              class="border border-gray-300 rounded-md px-3 py-2"
            />
          </div>

          <button
            @click="actualiserDonnees"
            :disabled="loading"
            class="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 disabled:opacity-50"
          >
            {{ loading ? '🔄' : '🔄 Actualiser' }}
          </button>
        </div>
      </div>

      <!-- ✅ MESSAGES DE FEEDBACK -->
      <div v-if="message.text" :class="[
        'mb-4 p-4 rounded-lg border',
        message.type === 'success' ? 'bg-green-50 text-green-800 border-green-200' : 'bg-red-50 text-red-800 border-red-200'
      ]">
        {{ message.text }}
      </div>

      <!-- ✅ CONTENU PRINCIPAL -->
      <div v-if="!employeSelectionne">

        <!-- ========== MODE GESTION ========== -->
        <div v-if="modeVue === 'gestion'">

          <!-- Formulaire création employé -->
          <div v-if="showFormulaire" class="bg-white rounded-lg shadow-md p-6 mb-6">
            <h3 class="text-lg font-semibold text-gray-900 mb-4">➕ Créer un nouvel employé</h3>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Nom</label>
                <input
                  v-model="nouvelEmploye.nom"
                  type="text"
                  class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Nom de famille"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Prénom</label>
                <input
                  v-model="nouvelEmploye.prenom"
                  type="text"
                  class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Prénom"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
                <input
                  v-model="nouvelEmploye.email"
                  type="email"
                  class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="email@exemple.com"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Heures de travail par jour</label>
                <input
                  v-model.number="nouvelEmploye.heuresTravailParJour"
                  type="number"
                  min="1"
                  max="12"
                  step="0.5"
                  class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
            </div>

            <div class="flex justify-end space-x-3 mt-6">
              <button
                @click="annulerCreation"
                class="px-4 py-2 text-gray-600 bg-gray-100 rounded-md hover:bg-gray-200"
              >
                Annuler
              </button>
              <button
                @click="creerEmploye"
                :disabled="!peutCreerEmploye"
                class="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50"
              >
                Créer l'employé
              </button>
            </div>
          </div>

          <!-- État vide (mode gestion) -->
          <div v-if="employesListe.length === 0" class="text-center py-12">
            <div class="w-24 h-24 mx-auto bg-gray-100 rounded-full flex items-center justify-center mb-4">
              <span class="text-4xl">👥</span>
            </div>
            <h3 class="text-lg font-medium text-gray-900 mb-2">Aucun employé</h3>
            <p class="text-gray-500 mb-4">Commencez par créer votre premier employé</p>
            <button
              @click="afficherFormulaireCreation"
              class="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700"
            >
              ➕ Créer le premier employé
            </button>
          </div>

          <!-- Liste des employés mode gestion -->
          <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div
              v-for="employe in employesListe"
              :key="employe.id"
              @click="voirDetailEmploye(employe.id)"
              class="bg-white rounded-lg shadow-md hover:shadow-lg transition-all cursor-pointer border-l-4 border-blue-500 p-6"
            >
              <!-- Avatar et nom -->
              <div class="flex items-center mb-4">
                <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center text-blue-600 font-bold text-lg">
                  {{ getInitiales(employe) }}
                </div>
                <div class="ml-3">
                  <h3 class="text-lg font-semibold text-gray-900">
                    {{ employe.nomComplet || `${employe.firstName} ${employe.lastName}` }}
                  </h3>
                  <p class="text-sm text-gray-500">{{ employe.email }}</p>
                </div>
              </div>

              <!-- Informations -->
              <div class="space-y-2 text-sm">
                <div class="flex justify-between">
                  <span>⏰ Heures/jour:</span>
                  <span class="font-medium">{{ employe.heuresTravailParJour || employe.workHoursPerDay }}h</span>
                </div>
                <div class="flex justify-between">
                  <span>📅 Depuis:</span>
                  <span class="font-medium">{{ formatDate(employe.dateCreation || employe.creationDate) }}</span>
                </div>
                <div class="flex justify-between">
                  <span>📊 Statut:</span>
                  <span :class="[
                    'px-2 py-1 rounded text-xs font-medium',
                    (employe.actif !== false && employe.active !== false) ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                  ]">
                    {{ (employe.actif !== false && employe.active !== false) ? 'Actif' : 'Inactif' }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ========== MODE PLANNING ========== -->
        <div v-else-if="modeVue === 'planning'">

          <!-- Statistiques planning -->
          <div v-if="employesPlanning.length > 0" class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
            <div class="bg-blue-50 p-4 rounded-lg">
              <div class="text-2xl font-bold text-blue-900">{{ employesPlanning.length }}</div>
              <div class="text-sm text-blue-800">👥 Employés actifs</div>
            </div>
            <div class="bg-green-50 p-4 rounded-lg">
              <div class="text-2xl font-bold text-green-900">{{ employesDisponibles }}</div>
              <div class="text-sm text-green-800">✅ Disponibles</div>
            </div>
            <div class="bg-yellow-50 p-4 rounded-lg">
              <div class="text-2xl font-bold text-yellow-900">{{ employesCharges }}</div>
              <div class="text-sm text-yellow-800">⚠️ Chargés</div>
            </div>
            <div class="bg-red-50 p-4 rounded-lg">
              <div class="text-2xl font-bold text-red-900">{{ employesSurcharges }}</div>
              <div class="text-sm text-red-800">🚨 Surchargés</div>
            </div>
          </div>

          <!-- État vide (mode planning) -->
          <div v-if="employesPlanning.length === 0" class="text-center py-12">
            <div class="w-24 h-24 mx-auto bg-gray-100 rounded-full flex items-center justify-center mb-4">
              <span class="text-4xl">📅</span>
            </div>
            <h3 class="text-lg font-medium text-gray-900 mb-2">Aucune donnée de planning</h3>
            <p class="text-gray-500 mb-4">Aucun employé n'a de planification pour cette date</p>
          </div>

          <!-- Grille des employés avec planning -->
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
              <!-- Avatar et nom -->
              <div class="flex items-center mb-4">
                <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center text-blue-600 font-bold text-lg">
                  {{ getInitiales(employe) }}
                </div>
                <div class="ml-3">
                  <h3 class="text-lg font-semibold text-gray-900">{{ employe.name || employe.nomComplet }}</h3>
                  <p :class="[
                    'text-sm font-medium',
                    employe.status === 'overloaded' ? 'text-red-600' :
                    employe.status === 'available' ? 'text-green-600' : 'text-yellow-600'
                  ]">
                    {{ getStatusText(employe.status) }}
                  </p>
                </div>
              </div>

              <!-- Métriques -->
              <div class="space-y-3">
                <div class="flex justify-between text-sm">
                  <span>📋 Tâches:</span>
                  <span class="font-medium">{{ employe.taskCount || 0 }}</span>
                </div>
                <div class="flex justify-between text-sm">
                  <span>🃏 Cartes:</span>
                  <span class="font-medium">{{ employe.cardCount || 0 }}</span>
                </div>
                <div class="flex justify-between text-sm">
                  <span>⏱️ Temps:</span>
                  <span class="font-medium">{{ formatTime(employe.totalMinutes || 0) }}</span>
                </div>

                <!-- Barre de progression -->
                <div>
                  <div class="flex justify-between text-xs text-gray-600 mb-1">
                    <span>Charge de travail</span>
                    <span>{{ Math.round(((employe.totalMinutes || 0) / (employe.maxMinutes || 480)) * 100) }}%</span>
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

                <!-- Actions planning -->
                <div class="flex space-x-2 mt-4">
                  <button class="flex-1 bg-blue-50 text-blue-600 px-3 py-2 rounded text-sm font-medium hover:bg-blue-100">
                    👁️ Détail
                  </button>
                  <button class="flex-1 bg-gray-50 text-gray-600 px-3 py-2 rounded text-sm font-medium hover:bg-gray-100">
                    📋 {{ employe.taskCount || 0 }} tâches
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ✅ COMPOSANT DE DÉTAIL EMPLOYÉ (unifié pour les deux modes) -->
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
  nomComplet?: string
  firstName?: string
  lastName?: string
  email: string
  workHoursPerDay?: number
  heuresTravailParJour?: number
  active?: boolean
  actif?: boolean
  creationDate?: string
  dateCreation?: string
  totalMinutes?: number
  maxMinutes?: number
  status?: 'overloaded' | 'available' | 'full'
  taskCount?: number
  cardCount?: number
}

// ========== INJECTION DES SERVICES ==========
const showNotification = inject<(message: string, type: 'success' | 'error') => void>('showNotification')

// ========== ÉTAT RÉACTIF ==========
const selectedDate = ref(new Date().toISOString().split('T')[0])
const modeVue = ref<'gestion' | 'planning'>('gestion')
const employeSelectionne = ref<string | null>(null)
const loading = ref(false)

// Données employés
const employesListe = ref<Employee[]>([])     // Mode gestion
const employesPlanning = ref<Employee[]>([])  // Mode planning

// Formulaire création
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

// ========== MÉTHODES ==========

// Navigation et modes
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

// Gestion des employés
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

    // Appel API pour créer l'employé
    const response = await fetch('/api/employees', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        lastName: nouvelEmploye.value.nom,
        firstName: nouvelEmploye.value.prenom,
        email: nouvelEmploye.value.email,
        workHoursPerDay: nouvelEmploye.value.heuresTravailParJour,
        active: true
      })
    })

    if (response.ok) {
      showNotification?.('Employé créé avec succès', 'success')
      showFormulaire.value = false
      resetFormulaire()
      await chargerEmployesGestion()
    } else {
      throw new Error('Erreur lors de la création')
    }
  } catch (error) {
    console.error('Erreur création employé:', error)
    showNotification?.('Erreur lors de la création de l\'employé', 'error')
  } finally {
    loading.value = false
  }
}

// Chargement des données
const chargerEmployesGestion = async () => {
  try {
    loading.value = true
    const response = await fetch('/api/employees')

    if (response.ok) {
      employesListe.value = await response.json()
    } else {
      // Données de fallback pour développement
      employesListe.value = [
        {
          id: '1',
          firstName: 'Sophie',
          lastName: 'Dubois',
          email: 'sophie.dubois@email.com',
          workHoursPerDay: 7.5,
          active: true,
          creationDate: '2024-01-15'
        },
        {
          id: '2',
          firstName: 'Pierre',
          lastName: 'Bernard',
          email: 'pierre.bernard@email.com',
          workHoursPerDay: 8,
          active: true,
          creationDate: '2024-02-01'
        }
      ]
    }
  } catch (error) {
    console.error('Erreur chargement employés gestion:', error)
    employesListe.value = []
  } finally {
    loading.value = false
  }
}

const chargerEmployesPlanning = async () => {
  try {
    loading.value = true
    const response = await fetch(`/api/employees/planning?date=${selectedDate.value}`)

    if (response.ok) {
      employesPlanning.value = await response.json()
    } else {
      // Données de fallback pour développement
      employesPlanning.value = [
        {
          id: '1',
          name: 'Sophie Dubois',
          totalMinutes: 360,
          maxMinutes: 450,
          status: 'available',
          taskCount: 3,
          cardCount: 12
        },
        {
          id: '2',
          name: 'Pierre Bernard',
          totalMinutes: 520,
          maxMinutes: 480,
          status: 'overloaded',
          taskCount: 5,
          cardCount: 18
        },
        {
          id: '3',
          name: 'Marie Martin',
          totalMinutes: 480,
          maxMinutes: 480,
          status: 'full',
          taskCount: 4,
          cardCount: 16
        }
      ]
    }
  } catch (error) {
    console.error('Erreur chargement planning:', error)
    employesPlanning.value = []
  } finally {
    loading.value = false
  }
}

const actualiserDonnees = async () => {
  if (modeVue.value === 'gestion') {
    await chargerEmployesGestion()
  } else {
    await chargerEmployesPlanning()
  }
}

// Utilitaires
const getInitiales = (employe: Employee): string => {
  if (employe.firstName && employe.lastName) {
    return (employe.firstName[0] + employe.lastName[0]).toUpperCase()
  }
  if (employe.name) {
    const parts = employe.name.split(' ')
    return parts.map(p => p[0]).join('').toUpperCase().slice(0, 2)
  }
  if (employe.nomComplet) {
    const parts = employe.nomComplet.split(' ')
    return parts.map(p => p[0]).join('').toUpperCase().slice(0, 2)
  }
  return '??'
}

const formatDate = (dateStr: string): string => {
  try {
    return new Date(dateStr).toLocaleDateString('fr-FR')
  } catch {
    return dateStr
  }
}

const formatTime = (minutes: number): string => {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return `${hours}h${mins.toString().padStart(2, '0')}`
}

const getStatusText = (status: string): string => {
  switch (status) {
    case 'overloaded': return '🚨 Surchargé'
    case 'available': return '✅ Disponible'
    case 'full': return '⚠️ Complet'
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

// Affichage des messages temporaires
const afficherMessage = (texte: string, type: 'success' | 'error' = 'success') => {
  message.value = { text: texte, type }
  setTimeout(() => {
    message.value = { text: '', type: 'success' }
  }, 5000)
}

// ========== WATCHERS ==========
watch(selectedDate, () => {
  if (modeVue.value === 'planning') {
    chargerEmployesPlanning()
  }
})

// ========== MONTAGE DU COMPOSANT ==========
onMounted(() => {
  actualiserDonnees()
})
</script>
