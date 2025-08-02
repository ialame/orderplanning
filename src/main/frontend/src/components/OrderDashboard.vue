<template>
  <div class="order-dashboard">
    <!-- Welcome Section -->
    <div class="bg-gradient-to-r from-blue-600 to-purple-600 text-white rounded-lg p-8 mb-8 shadow-lg">
      <h1 class="text-4xl font-bold mb-2">🃏 Pokemon Order Planning System</h1>
      <p class="text-xl opacity-90">Efficient distribution of Pokemon card orders among employees</p>
      <div class="mt-4 text-sm opacity-75">
        System Status: <span class="font-semibold">{{ systemStatus }}</span> |
        Last Updated: {{ lastUpdated }}
      </div>
    </div>

    <!-- Quick Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <div class="bg-white rounded-lg shadow p-6 border-l-4 border-blue-500">
        <div class="flex items-center">
          <div class="flex-1">
            <p class="text-sm font-medium text-gray-600">Available Orders</p>
            <p class="text-2xl font-bold text-gray-900">{{ formatNumber(systemStats?.availableOrders || 0) }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
            </svg>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow p-6 border-l-4 border-green-500">
        <div class="flex items-center">
          <div class="flex-1">
            <p class="text-sm font-medium text-gray-600">Active Employees</p>
            <p class="text-2xl font-bold text-gray-900">{{ systemStats?.activeEmployees || 0 }}</p>
          </div>
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
            <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"/>
            </svg>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow p-6 border-l-4 border-purple-500">
        <div class="flex items-center">
          <div class="flex-1">
            <p class="text-sm font-medium text-gray-600">Total Plannings</p>
            <p class="text-2xl font-bold text-gray-900">{{ formatNumber(systemStats?.totalPlannings || 0) }}</p>
          </div>
          <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
            <svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/>
            </svg>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow p-6 border-l-4 border-orange-500">
        <div class="flex items-center">
          <div class="flex-1">
            <p class="text-sm font-medium text-gray-600">Success Rate</p>
            <p class="text-2xl font-bold text-gray-900">{{ successRate }}%</p>
          </div>
          <div class="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center">
            <svg class="w-6 h-6 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
            </svg>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="bg-white rounded-lg shadow p-6 mb-8">
      <h2 class="text-xl font-bold text-gray-900 mb-4">🚀 Quick Actions</h2>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <button
          @click="$router.push('/planning')"
          class="flex items-center p-4 bg-blue-50 hover:bg-blue-100 rounded-lg transition-colors border border-blue-200"
        >
          <div class="w-10 h-10 bg-blue-600 rounded-lg flex items-center justify-center mr-3">
            <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"/>
            </svg>
          </div>
          <div class="text-left">
            <h3 class="font-semibold text-gray-900">Generate Planning</h3>
            <p class="text-sm text-gray-600">Create new order assignments</p>
          </div>
        </button>

        <button
          @click="viewTodaysPlanning"
          class="flex items-center p-4 bg-green-50 hover:bg-green-100 rounded-lg transition-colors border border-green-200"
        >
          <div class="w-10 h-10 bg-green-600 rounded-lg flex items-center justify-center mr-3">
            <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/>
            </svg>
          </div>
          <div class="text-left">
            <h3 class="font-semibold text-gray-900">Today's Planning</h3>
            <p class="text-sm text-gray-600">View today's assignments</p>
          </div>
        </button>

        <button
          @click="refreshStats"
          :disabled="loading"
          class="flex items-center p-4 bg-purple-50 hover:bg-purple-100 rounded-lg transition-colors border border-purple-200 disabled:opacity-50"
        >
          <div class="w-10 h-10 bg-purple-600 rounded-lg flex items-center justify-center mr-3">
            <svg
              class="w-5 h-5 text-white"
              :class="{ 'animate-spin': loading }"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
            </svg>
          </div>
          <div class="text-left">
            <h3 class="font-semibold text-gray-900">Refresh Data</h3>
            <p class="text-sm text-gray-600">Update system statistics</p>
          </div>
        </button>
      </div>
    </div>

    <!-- Priority Breakdown -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
      <!-- Priority Distribution -->
      <div class="bg-white rounded-lg shadow p-6">
        <h2 class="text-xl font-bold text-gray-900 mb-4">📊 Priority Distribution</h2>
        <div class="space-y-3">
          <div v-for="priority in priorityStats" :key="priority.name" class="flex items-center">
            <div class="flex-1">
              <div class="flex justify-between mb-1">
                <span class="text-sm font-medium" :class="priority.color">{{ priority.name }}</span>
                <span class="text-sm text-gray-600">{{ priority.count }}</span>
              </div>
              <div class="w-full bg-gray-200 rounded-full h-2">
                <div
                  class="h-2 rounded-full transition-all duration-300"
                  :class="priority.bgColor"
                  :style="`width: ${priority.percentage}%`"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Recent Activity -->
      <div class="bg-white rounded-lg shadow p-6">
        <h2 class="text-xl font-bold text-gray-900 mb-4">🕒 Recent Activity</h2>
        <div class="space-y-3">
          <div v-for="activity in recentActivity" :key="activity.id" class="flex items-center p-3 bg-gray-50 rounded-lg">
            <div
              class="w-3 h-3 rounded-full mr-3"
              :class="activity.statusColor"
            ></div>
            <div class="flex-1">
              <p class="text-sm font-medium text-gray-900">{{ activity.message }}</p>
              <p class="text-xs text-gray-500">{{ activity.time }}</p>
            </div>
          </div>
          <div v-if="recentActivity.length === 0" class="text-center py-6">
            <p class="text-gray-500">No recent activity</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Status Message -->
    <div
      v-if="message"
      class="mb-6 p-4 rounded-lg"
      :class="{
        'bg-green-50 border border-green-200 text-green-800': message.type === 'success',
        'bg-red-50 border border-red-200 text-red-800': message.type === 'error',
        'bg-blue-50 border border-blue-200 text-blue-800': message.type === 'info'
      }"
    >
      <div class="flex">
        <div class="flex-shrink-0">
          <svg
            v-if="message.type === 'success'"
            class="h-5 w-5 text-green-400"
            fill="currentColor"
            viewBox="0 0 20 20"
          >
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
          </svg>
          <svg
            v-else-if="message.type === 'error'"
            class="h-5 w-5 text-red-400"
            fill="currentColor"
            viewBox="0 0 20 20"
          >
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
          </svg>
          <svg
            v-else
            class="h-5 w-5 text-blue-400"
            fill="currentColor"
            viewBox="0 0 20 20"
          >
            <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/>
          </svg>
        </div>
        <div class="ml-3 flex-1">
          <p class="text-sm font-medium">{{ message.text }}</p>
          <p v-if="message.details" class="text-sm mt-1 opacity-80">{{ message.details }}</p>
        </div>
        <div class="ml-auto pl-3">
          <button @click="clearMessage" class="text-gray-400 hover:text-gray-600">
            <svg class="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { englishApiService } from '@/services/englishApiService'
import type { SystemDebugResponse, SystemStatsResponse } from '@/services/englishApiService'

// ========== ROUTER ==========
const router = useRouter()

// ========== REACTIVE DATA ==========
const loading = ref(false)
const systemStats = ref<SystemDebugResponse | null>(null)
const planningStats = ref<SystemStatsResponse | null>(null)
const message = ref<{ text: string; details?: string; type: 'success' | 'error' | 'info' } | null>(null)

// ========== COMPUTED ==========
const systemStatus = computed(() => {
  if (!systemStats.value) return 'Loading...'
  return systemStats.value.status === 'SUCCESS' ? 'Operational' : 'Error'
})

const lastUpdated = computed(() => {
  return new Date().toLocaleString('en-US', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
})

const successRate = computed(() => {
  if (!planningStats.value || !planningStats.value.totalPlannings) return 0
  const completedCount = planningStats.value.statusBreakdown?.COMPLETED || 0
  return Math.round((completedCount / planningStats.value.totalPlannings) * 100)
})

const priorityStats = computed(() => {
  if (!systemStats.value?.sampleOrders) return []

  const priorities = systemStats.value.sampleOrders.reduce((acc: Record<string, number>, order) => {
    acc[order.priority] = (acc[order.priority] || 0) + 1
    return acc
  }, {})

  const total = Object.values(priorities).reduce((sum, count) => sum + count, 0)

  return [
    {
      name: 'URGENT',
      count: priorities.URGENT || 0,
      percentage: total ? Math.round(((priorities.URGENT || 0) / total) * 100) : 0,
      color: 'text-red-700',
      bgColor: 'bg-red-500'
    },
    {
      name: 'HIGH',
      count: priorities.HIGH || 0,
      percentage: total ? Math.round(((priorities.HIGH || 0) / total) * 100) : 0,
      color: 'text-orange-700',
      bgColor: 'bg-orange-500'
    },
    {
      name: 'MEDIUM',
      count: priorities.MEDIUM || 0,
      percentage: total ? Math.round(((priorities.MEDIUM || 0) / total) * 100) : 0,
      color: 'text-yellow-700',
      bgColor: 'bg-yellow-500'
    },
    {
      name: 'LOW',
      count: priorities.LOW || 0,
      percentage: total ? Math.round(((priorities.LOW || 0) / total) * 100) : 0,
      color: 'text-green-700',
      bgColor: 'bg-green-500'
    }
  ]
})

const recentActivity = computed(() => {
  // Mock recent activity - in real app, this would come from API
  return [
    {
      id: '1',
      message: 'Planning generation completed successfully',
      time: '2 minutes ago',
      statusColor: 'bg-green-500'
    },
    {
      id: '2',
      message: '15 orders processed for urgent priority',
      time: '5 minutes ago',
      statusColor: 'bg-blue-500'
    },
    {
      id: '3',
      message: 'Employee workload balanced',
      time: '10 minutes ago',
      statusColor: 'bg-purple-500'
    }
  ]
})

// ========== METHODS ==========
const loadSystemStats = async () => {
  try {
    loading.value = true
    console.log('📊 Loading system statistics...')

    // Load debug info for basic stats
    const debugResponse = await englishApiService.getSystemInfo()
    systemStats.value = debugResponse

    // Load planning stats
    const statsResponse = await englishApiService.getSystemStats()
    planningStats.value = statsResponse

    console.log('✅ System stats loaded:', { debugResponse, statsResponse })

  } catch (error) {
    console.error('❌ Error loading system stats:', error)
    showMessage({
      text: 'Failed to load system statistics',
      details: error instanceof Error ? error.message : 'Unknown error',
      type: 'error'
    })
  } finally {
    loading.value = false
  }
}

const refreshStats = async () => {
  await loadSystemStats()
  showMessage({
    text: 'Statistics refreshed successfully',
    type: 'success'
  })
}

const viewTodaysPlanning = () => {
  const today = new Date().toISOString().split('T')[0]
  router.push(`/planning?date=${today}`)
}

const formatNumber = (num: number): string => {
  return num.toLocaleString()
}

const showMessage = (msg: { text: string; details?: string; type: 'success' | 'error' | 'info' }) => {
  message.value = msg
  // Auto-clear success messages after 5 seconds
  if (msg.type === 'success') {
    setTimeout(() => {
      if (message.value?.type === 'success') {
        message.value = null
      }
    }, 5000)
  }
}

const clearMessage = () => {
  message.value = null
}

// ========== LIFECYCLE ==========
onMounted(() => {
  loadSystemStats()
})
</script>

<style scoped>
.order-dashboard {
  @apply max-w-7xl mx-auto p-6;
}

/* Custom animations */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

/* Loading animation */
@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}
</style>
