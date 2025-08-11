// ============= PLANNING VIEW COMPONENT - 100% ENGLISH =============
// Complete English component for Pokemon card planning management
// NO FRENCH variables, comments, or methods

<template>
  <div class="planning-view">
    <!-- Header Section -->
    <div class="header-section">
      <h1 class="page-title">🎮 Pokemon Card Order Planning</h1>
      <p class="page-description">
        Manage and view automatic planning for Pokemon card certification orders
      </p>
    </div>

    <!-- Action Bar -->
    <div class="action-bar">
      <button
        @click="generateAutomaticPlanning"
        :disabled="isGenerating"
        class="btn btn-primary"
      >
        <span v-if="isGenerating">🔄 Generating...</span>
        <span v-else>🎮 Generate Pokemon Planning</span>
      </button>

      <button
        @click="loadPlannings"
        :disabled="isLoading"
        class="btn btn-secondary"
      >
        <span v-if="isLoading">🔄 Loading...</span>
        <span v-else">📋 Refresh Plannings</span>
      </button>

      <button
        @click="cleanDuplicates"
        class="btn btn-warning"
      >
        🧹 Clean Duplicates
      </button>
    </div>

    <!-- Loading Indicator -->
    <div v-if="isLoading" class="loading-section">
      <div class="loading-spinner"></div>
      <p>{{ loadingMessage }}</p>
    </div>

    <!-- Statistics Section -->
    <div v-if="!isLoading && plannings.length > 0" class="stats-section">
      <div class="stats-grid">
        <div class="stat-card">
          <h3>Total Plannings</h3>
          <p class="stat-number">{{ plannings.length }}</p>
        </div>
        <div class="stat-card">
          <h3>Completed</h3>
          <p class="stat-number">{{ completedPlannings }}</p>
        </div>
        <div class="stat-card">
          <h3>In Progress</h3>
          <p class="stat-number">{{ inProgressPlannings }}</p>
        </div>
        <div class="stat-card">
          <h3>Employees Used</h3>
          <p class="stat-number">{{ employeesUsed }}</p>
        </div>
      </div>
    </div>

    <!-- Plannings Table -->
    <div v-if="!isLoading" class="plannings-section">
      <h2>📋 Planning Overview</h2>

      <div v-if="plannings.length === 0" class="empty-state">
        <p>🎮 No Pokemon card plannings found.</p>
        <p>Click "Generate Pokemon Planning" to create automatic plannings.</p>
      </div>

      <div v-else class="plannings-table-container">
        <table class="plannings-table">
          <thead>
          <tr>
            <th>Order Number</th>
            <th>Employee</th>
            <th>Date</th>
            <th>Start Time</th>
            <th>Duration</th>
            <th>Cards</th>
            <th>Priority</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
          </thead>
          <tbody>
          <tr
            v-for="planning in groupedPlannings"
            :key="planning.id"
            :class="{ 'completed': planning.completed }"
          >
            <td class="order-number">
              {{ planning.orderNumber }}
            </td>
            <td class="employee-name">
              {{ planning.employeeName }}
            </td>
            <td class="planning-date">
              {{ formatDate(planning.planningDate) }}
            </td>
            <td class="start-time">
              {{ planning.startTime }}
            </td>
            <td class="duration">
              {{ planning.durationMinutes }}min
            </td>
            <td class="card-count">
              {{ planning.cardCount }} cards
            </td>
            <td class="priority">
                <span :class="getPriorityClass(planning.priority)">
                  {{ planning.priority }}
                </span>
            </td>
            <td class="status">
                <span :class="getStatusClass(planning.status)">
                  {{ planning.status }}
                </span>
            </td>
            <td class="actions">
              <button
                @click="viewPlanningDetails(planning)"
                class="btn btn-small"
              >
                👁️ View
              </button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Planning Details Modal -->
    <div v-if="selectedPlanning" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <h3>Planning Details</h3>
        <div class="planning-details">
          <p><strong>Order:</strong> {{ selectedPlanning.orderNumber }}</p>
          <p><strong>Employee:</strong> {{ selectedPlanning.employeeName }}</p>
          <p><strong>Date:</strong> {{ formatDate(selectedPlanning.planningDate) }}</p>
          <p><strong>Time:</strong> {{ selectedPlanning.startTime }} - {{ selectedPlanning.endTime }}</p>
          <p><strong>Duration:</strong> {{ selectedPlanning.durationMinutes }} minutes</p>
          <p><strong>Cards:</strong> {{ selectedPlanning.cardCount }}</p>
          <p><strong>Priority:</strong> {{ selectedPlanning.priority }}</p>
          <p><strong>Status:</strong> {{ selectedPlanning.status }}</p>
          <p v-if="selectedPlanning.notes"><strong>Notes:</strong> {{ selectedPlanning.notes }}</p>
        </div>
        <button @click="closeModal" class="btn btn-secondary">Close</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, inject } from 'vue'
import { apiService } from '../services/api'
import type { Planning } from '../services/api'

// ========== REACTIVE DATA ==========
const plannings = ref<Planning[]>([])
const isLoading = ref(false)
const isGenerating = ref(false)
const loadingMessage = ref('')
const selectedPlanning = ref<Planning | null>(null)

// ========== INJECTED FUNCTIONS ==========
const showNotification = inject('showNotification') as (message: string, type?: 'success' | 'error' | 'info') => void

// ========== COMPUTED PROPERTIES ==========
const groupedPlannings = computed(() => {
  return plannings.value
    .sort((a, b) => {
      const dateA = new Date(`${a.planningDate} ${a.startTime}`)
      const dateB = new Date(`${b.planningDate} ${b.startTime}`)
      return dateA.getTime() - dateB.getTime()
    })
})

const completedPlannings = computed(() =>
  plannings.value.filter(p => p.completed).length
)

const inProgressPlannings = computed(() =>
  plannings.value.filter(p => !p.completed).length
)

const employeesUsed = computed(() =>
  new Set(plannings.value.map(p => p.employeeId).filter(Boolean)).size
)

// ========== METHODS ==========

/**
 * ✅ FIXED: Load all plannings from backend (direct API call)
 * Replace this method in your PlanningView.vue file
 */
const loadPlannings = async () => {
  isLoading.value = true
  loadingMessage.value = 'Loading Pokemon card plannings...'

  try {
    console.log('🔍 Loading plannings from backend...')

    // ✅ DIRECT API CALL - bypass the apiService for now
    const response = await fetch('/api/planning/view-simple', {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    }

    const data = await response.json()
    console.log('✅ Raw backend data:', data)

    // ✅ TRANSFORM DATA to match expected format
    plannings.value = data.map((item: any) => ({
      id: item.id,
      orderId: item.orderId,
      employeeId: item.employeeId,
      planningDate: item.planningDate,
      startTime: item.startTime,
      endTime: item.endTime,
      durationMinutes: item.durationMinutes,
      priority: item.priority,
      status: item.status,
      completed: item.completed,
      cardCount: item.cardCount,
      notes: item.notes,
      progressPercentage: item.progressPercentage,
      employeeName: item.employeeName,
      orderNumber: item.orderNumber
    }))

    console.log(`✅ Transformed ${plannings.value.length} plannings successfully`)

    if (plannings.value.length === 0) {
      showNotification?.('No plannings found. Try generating some first!', 'info')
    } else {
      // Count Pokemon-related plannings
      const pokemonPlannings = plannings.value.filter(p =>
        p.notes?.includes('🎮') ||
        p.notes?.includes('Pokémon') ||
        p.notes?.includes('Pokemon')
      )

      if (pokemonPlannings.length > 0) {
        showNotification?.(`🎮 ${pokemonPlannings.length} Pokemon plannings loaded`, 'success')
      } else {
        showNotification?.(`📋 ${plannings.value.length} plannings loaded`, 'success')
      }
    }

  } catch (error) {
    console.error('❌ Error loading plannings:', error)
    showNotification?.(`Error loading plannings: ${error instanceof Error ? error.message : 'Unknown error'}`, 'error')
    plannings.value = []
  } finally {
    isLoading.value = false
    loadingMessage.value = ''
  }
}

/**
 * Generate automatic Pokemon card planning
 */
const generateAutomaticPlanning = async () => {
  if (isGenerating.value) return

  isGenerating.value = true
  loadingMessage.value = 'Generating Pokemon card plannings...'

  try {
    console.log('🎮 Starting Pokemon card planning generation...')

    const result = await apiService.generatePokemonPlanning({
      cleanFirst: false,
      startDate: '2025-06-01',
      timePerCard: 3
    })

    console.log('✅ Generation result:', result)

    if (result.success) {
      const planningsCreated = result.planningsSaved || 0
      const totalCards = result.totalCards || 0
      const totalHours = result.totalHours || '0'

      showNotification?.(
        `🎮 ${planningsCreated} Pokemon plannings created (${totalCards} cards, ${totalHours}h)`,
        'success'
      )

      // Reload plannings to show new ones
      await loadPlannings()
    } else {
      showNotification?.(`❌ Generation error: ${result.error || 'Unknown error'}`, 'error')
    }

  } catch (error) {
    console.error('❌ Planning generation error:', error)
    showNotification?.('Error during automatic planning generation', 'error')
  } finally {
    isGenerating.value = false
    loadingMessage.value = ''
  }
}

/**
 * Clean duplicate plannings
 */
const cleanDuplicates = async () => {
  try {
    console.log('🧹 Cleaning duplicate plannings...')

    const result = await apiService.cleanDuplicatePlannings()

    if (result.success) {
      showNotification?.(
        `🧹 ${result.duplicatesRemoved || 0} duplicates cleaned`,
        'success'
      )
      await loadPlannings()
    } else {
      showNotification?.('Error cleaning duplicates', 'error')
    }

  } catch (error) {
    console.error('❌ Cleanup error:', error)
    showNotification?.('Error cleaning duplicates', 'error')
  }
}

/**
 * View planning details
 */
const viewPlanningDetails = (planning: Planning) => {
  selectedPlanning.value = planning
}

/**
 * Close modal
 */
const closeModal = () => {
  selectedPlanning.value = null
}

/**
 * Format date for display
 */
const formatDate = (dateStr: string | undefined): string => {
  if (!dateStr) return 'N/A'

  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('en-US', {
      weekday: 'short',
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  } catch (error) {
    return dateStr
  }
}

/**
 * Get CSS class for priority
 */
const getPriorityClass = (priority: string | undefined): string => {
  switch (priority?.toUpperCase()) {
    case 'URGENT': return 'priority-urgent'
    case 'HIGH': return 'priority-high'
    case 'MEDIUM': return 'priority-medium'
    case 'LOW': return 'priority-low'
    default: return 'priority-medium'
  }
}

/**
 * Get CSS class for status
 */
const getStatusClass = (status: string | undefined): string => {
  switch (status?.toUpperCase()) {
    case 'SCHEDULED': return 'status-scheduled'
    case 'IN_PROGRESS': return 'status-in-progress'
    case 'COMPLETED': return 'status-completed'
    case 'CANCELLED': return 'status-cancelled'
    default: return 'status-scheduled'
  }
}

/**
 * 🧪 TEST METHOD: Add this to PlanningView.vue for debugging
 */
const testDirectApiCall = async () => {
  try {
    console.log('🧪 Testing direct API call...')

    const response = await fetch('/api/planning/view-simple')
    const data = await response.json()

    console.log('🧪 Test result:', {
      status: response.status,
      dataType: typeof data,
      isArray: Array.isArray(data),
      count: Array.isArray(data) ? data.length : 0,
      firstItem: Array.isArray(data) && data.length > 0 ? data[0] : null
    })

    showNotification?.(`Test: ${Array.isArray(data) ? data.length : 0} items found`, 'info')

  } catch (error) {
    console.error('🧪 Test failed:', error)
    showNotification?.('Test failed - check console', 'error')
  }
}

// ========== LIFECYCLE ==========
onMounted(async () => {
  console.log('🎮 PlanningView mounted - loading initial data...')
  await loadPlannings()
})
</script>

<style scoped>
/* ========== COMPONENT STYLES ========== */

.planning-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header-section {
  text-align: center;
  margin-bottom: 30px;
}

.page-title {
  font-size: 2.5rem;
  color: #2563eb;
  margin-bottom: 10px;
}

.page-description {
  color: #6b7280;
  font-size: 1.1rem;
}

.action-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
  justify-content: center;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: #2563eb;
  color: white;
}

.btn-secondary {
  background: #6b7280;
  color: white;
}

.btn-warning {
  background: #f59e0b;
  color: white;
}

.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.loading-section {
  text-align: center;
  padding: 40px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f4f6;
  border-top: 4px solid #2563eb;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.stats-section {
  margin-bottom: 30px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.stat-number {
  font-size: 2rem;
  font-weight: bold;
  color: #2563eb;
  margin: 10px 0;
}

.plannings-section h2 {
  margin-bottom: 20px;
  color: #374151;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: #f9fafb;
  border-radius: 12px;
  color: #6b7280;
}

.plannings-table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.plannings-table {
  width: 100%;
  border-collapse: collapse;
}

.plannings-table th {
  background: #f9fafb;
  padding: 15px 12px;
  text-align: left;
  font-weight: 600;
  color: #374151;
  border-bottom: 2px solid #e5e7eb;
}

.plannings-table td {
  padding: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.plannings-table tr:hover {
  background: #f9fafb;
}

.plannings-table tr.completed {
  opacity: 0.7;
}

.order-number {
  font-family: monospace;
  font-weight: 600;
  color: #2563eb;
}

.priority-urgent { color: #dc2626; font-weight: 600; }
.priority-high { color: #f59e0b; font-weight: 600; }
.priority-medium { color: #059669; font-weight: 600; }
.priority-low { color: #6b7280; font-weight: 600; }

.status-scheduled { color: #2563eb; }
.status-in-progress { color: #f59e0b; }
.status-completed { color: #059669; }
.status-cancelled { color: #dc2626; }

.btn-small {
  padding: 6px 12px;
  font-size: 0.875rem;
  background: #e5e7eb;
  color: #374151;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 12px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.planning-details p {
  margin: 8px 0;
}
</style>
