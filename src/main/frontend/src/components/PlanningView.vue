<template>
  <div class="planning-view">
    <!-- Header -->
    <div class="flex justify-between items-center mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Order Planning</h1>
        <p class="text-gray-600 mt-1">Automatic distribution of Pokemon card orders</p>
      </div>
      <div class="flex gap-3">
        <button
          @click="generatePlanning"
          :disabled="loading"
          class="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
          </svg>
          Generate Planning
        </button>
        <button
          @click="exportPlanning"
          class="flex items-center gap-2 bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
          </svg>
          Export
        </button>
      </div>
    </div>

    <!-- Filters and Configuration -->
    <div class="bg-white rounded-lg shadow p-6 mb-6">
      <h2 class="text-lg font-semibold text-gray-900 mb-4">Planning Configuration</h2>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Start Date</label>
          <input
            type="date"
            v-model="config.dateDebut"
            class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Number of Employees</label>
          <select
            v-model="config.nombreEmployes"
            class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="2">2 employees</option>
            <option value="3">3 employees</option>
            <option value="4">4 employees</option>
            <option value="5">5 employees</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Processing Time per Card</label>
          <select
            v-model="config.tempsParCarte"
            class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="3">3 minutes</option>
            <option value="4">4 minutes</option>
            <option value="5">5 minutes</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Statistics Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6" v-if="stats">
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0">
            <div class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
              </svg>
            </div>
          </div>
          <div class="ml-4">
            <p class="text-2xl font-semibold text-gray-900">{{ stats.totalCommandes }}</p>
            <p class="text-sm text-gray-500">Total Orders</p>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0">
            <div class="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z"></path>
              </svg>
            </div>
          </div>
          <div class="ml-4">
            <p class="text-2xl font-semibold text-gray-900">{{ stats.totalCartes }}</p>
            <p class="text-sm text-gray-500">Total Cards</p>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0">
            <div class="w-8 h-8 bg-yellow-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
            </div>
          </div>
          <div class="ml-4">
            <p class="text-2xl font-semibold text-gray-900">{{ formatDuration(stats.tempsTotal) }}</p>
            <p class="text-sm text-gray-500">Total Time</p>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0">
            <div class="w-8 h-8 bg-purple-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path>
              </svg>
            </div>
          </div>
          <div class="ml-4">
            <p class="text-2xl font-semibold text-gray-900">{{ config.nombreEmployes }}</p>
            <p class="text-sm text-gray-500">Active Employees</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="flex justify-center items-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      <span class="ml-3 text-gray-600">Generating optimal planning...</span>
    </div>

    <!-- Employee Schedule -->
    <div v-else-if="planning && planning.length > 0" class="space-y-6">
      <h2 class="text-xl font-semibold text-gray-900">Employee Planning</h2>

      <div class="grid gap-6">
        <div
          v-for="employee in planning"
          :key="employee.id"
          class="bg-white rounded-lg shadow-sm border border-gray-200"
        >
          <!-- Employee Header -->
          <div class="p-6 border-b border-gray-200">
            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-4">
                <div class="flex-shrink-0">
                  <div class="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                    <span class="text-blue-600 font-medium">{{ getInitials(employee.nom) }}</span>
                  </div>
                </div>
                <div>
                  <h3 class="text-lg font-medium text-gray-900">{{ employee.nom }}</h3>
                  <p class="text-sm text-gray-500">{{ employee.commandes.length }} orders assigned</p>
                </div>
              </div>
              <div class="flex items-center space-x-4">
                <div class="text-right">
                  <p class="text-sm font-medium text-gray-900">{{ formatDuration(employee.tempsTotal) }}</p>
                  <p class="text-xs text-gray-500">Total time</p>
                </div>
                <div class="text-right">
                  <p class="text-sm font-medium text-gray-900">{{ employee.totalCartes }}</p>
                  <p class="text-xs text-gray-500">Cards</p>
                </div>
                <div
                  :class="[
                    'px-2 py-1 rounded-full text-xs font-medium',
                    employee.efficacite >= 90 ? 'bg-green-100 text-green-800' :
                    employee.efficacite >= 70 ? 'bg-yellow-100 text-yellow-800' :
                    'bg-red-100 text-red-800'
                  ]"
                >
                  {{ employee.efficacite }}% efficiency
                </div>
              </div>
            </div>
          </div>

          <!-- Orders List -->
          <div class="p-6">
            <div class="space-y-3">
              <div
                v-for="commande in employee.commandes"
                :key="commande.id"
                class="flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
              >
                <div class="flex items-center space-x-4">
                  <div
                    :class="[
                      'w-3 h-3 rounded-full',
                      commande.priorite === 'URGENTE' ? 'bg-red-500' :
                      commande.priorite === 'HAUTE' ? 'bg-orange-500' :
                      'bg-green-500'
                    ]"
                  ></div>
                  <div>
                    <p class="font-medium text-gray-900">Order #{{ commande.numeroCommande }}</p>
                    <p class="text-sm text-gray-500">{{ commande.nombreCartes }} cards • {{ commande.priorite }}</p>
                  </div>
                </div>
                <div class="text-right">
                  <p class="text-sm font-medium text-gray-900">{{ formatDuration(commande.dureeEstimee) }}</p>
                  <p class="text-xs text-gray-500">{{ commande.heureDebut }} - {{ commande.heureFin }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="text-center py-12">
      <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V9a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"></path>
      </svg>
      <h3 class="mt-2 text-sm font-medium text-gray-900">No planning generated</h3>
      <p class="mt-1 text-sm text-gray-500">Click "Generate Planning" to start distributing orders</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

// Types
interface Order {
  id: string
  numeroCommande: string
  priorite: string
  nombreCartes: number
  dureeEstimee: number
  heureDebut: string
  heureFin: string
}

interface Employee {
  id: string
  nom: string
  commandes: Order[]
  tempsTotal: number
  totalCartes: number
  efficacite: number
}

interface Config {
  dateDebut: string
  nombreEmployes: number
  tempsParCarte: number
}

interface Stats {
  totalCommandes: number
  totalCartes: number
  tempsTotal: number
}

// Reactive data
const loading = ref(false)
const planning = ref<Employee[]>([])
const stats = ref<Stats | null>(null)

// Methods
const generatePlanning = async () => {
  const response = await fetch('/api/planning/generate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      startDate: config.value.dateDebut,
      numberOfEmployees: parseInt(config.value.nombreEmployes),
      timePerCard: parseInt(config.value.tempsParCarte),
      endDate: "2025-07-04"
    })
  })

  const data = await response.json()
  if (data.success) {
    console.log(`✅ ${data.planningsSaved} planifications créées !`)
  }
}

// ========== CORRECTION du config reactive ==========
const config = ref({
  dateDebut: new Date().toISOString().split('T')[0],
  nombreEmployes: 3,  // Number instead of string
  tempsParCarte: 3    // Number instead of string
})

// ========== AJOUT d'une méthode de test ==========
const testBackendConnection = async () => {
  try {
    console.log('🔍 Testing backend connection...')

    const response = await fetch('/api/planning/debug')
    const data = await response.json()

    console.log('✅ Backend test result:', data)

    if (data.controllerLoaded && data.employeeServiceWorking) {
      console.log('✅ Backend is working correctly')
      console.log(`Found ${data.employeeCount} employees`)
    } else {
      console.error('❌ Backend has issues:', data)
    }

    return data
  } catch (error) {
    console.error('❌ Backend connection failed:', error)
    return null
  }
}

// ========== AJOUT d'une méthode de debug ==========
const debugPlanning = async () => {
  console.log('🔍 Debug mode activated')

  // Test backend
  await testBackendConnection()

  // Test current config
  console.log('Current config:', config.value)

  // Test API call
  try {
    const response = await fetch('/api/planning/generate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({})  // Empty config for test
    })

    const data = await response.json()
    console.log('✅ Test API call result:', data)

  } catch (error) {
    console.error('❌ Test API call failed:', error)
  }
}

const exportPlanning = async () => {
  try {
    const response = await fetch('/api/planning/export', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ planning: planning.value, config: config.value })
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `planning-${config.value.dateDebut}.xlsx`
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)

    console.log('✅ Planning exported successfully')
  } catch (error) {
    console.error('❌ Error exporting planning:', error)
  }
}

const getInitials = (name: string): string => {
  return name
    .split(' ')
    .map(n => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2)
}

const formatDuration = (minutes: number): string => {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60

  if (hours > 0) {
    return `${hours}h${mins > 0 ? ` ${mins}m` : ''}`
  }
  return `${mins}m`
}

// Lifecycle
onMounted(() => {
  console.log('📋 PlanningView mounted')
})
</script>

<style scoped>
.planning-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

@media (max-width: 768px) {
  .planning-view {
    padding: 1rem;
  }
}
</style>
