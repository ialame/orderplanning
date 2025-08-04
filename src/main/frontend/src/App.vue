<template>
  <div id="app" class="pokemon-order-planning-app">
    <!-- Loading Screen -->
    <div
      v-if="isLoading"
      class="loading-screen fixed inset-0 bg-white flex items-center justify-center z-50"
    >
      <div class="text-center">
        <div class="animate-spin rounded-full h-16 w-16 border-b-2 border-blue-600 mx-auto mb-4"></div>
        <h2 class="text-2xl font-semibold text-gray-900 mb-2">🃏 Pokemon Order Planning</h2>
        <p class="text-gray-600">{{ loadingMessage || 'Loading application...' }}</p>
      </div>
    </div>

    <!-- Main Application -->
    <div v-else class="app-container min-h-screen bg-gray-50">
      <!-- Top Navigation Bar -->
      <header class="bg-white shadow-sm border-b">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div class="flex justify-between items-center h-16">
            <!-- Logo and Title -->
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <h1 class="text-2xl font-bold text-blue-600">🃏 Pokemon Planning</h1>
              </div>
            </div>

            <!-- Status Indicator -->
            <div class="flex items-center space-x-4">
              <div class="flex items-center text-sm text-gray-500">
                <div class="w-2 h-2 bg-green-500 rounded-full mr-2"></div>
                System Online
              </div>
              <div class="text-sm text-gray-500">
                {{ new Date().toLocaleDateString('en-US', {
                weekday: 'short',
                year: 'numeric',
                month: 'short',
                day: 'numeric'
              }) }}
              </div>
            </div>
          </div>
        </div>
      </header>

      <!-- Navigation Tabs -->
      <nav class="bg-white border-b">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div class="flex space-x-8">
            <button
              v-for="tab in tabs"
              :key="tab.id"
              @click="switchTab(tab.id)"
              :class="[
                'py-4 px-1 border-b-2 font-medium text-sm transition-colors duration-200',
                activeTab === tab.id
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              ]"
            >
              {{ tab.label }}
            </button>
          </div>
        </div>
      </nav>

      <!-- Main Content Area -->
      <main class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div class="px-4 py-6 sm:px-0">
          <!-- Notification System -->
          <div
            v-if="notification.show"
            :class="[
              'mb-6 p-4 rounded-lg border-l-4 transition-all duration-300',
              notification.type === 'success' ? 'bg-green-50 border-green-400' :
              notification.type === 'error' ? 'bg-red-50 border-red-400' :
              notification.type === 'warning' ? 'bg-yellow-50 border-yellow-400' :
              'bg-blue-50 border-blue-400'
            ]"
          >
            <div class="flex">
              <div class="flex-shrink-0">
                <svg
                  v-if="notification.type === 'success'"
                  class="h-5 w-5 text-green-400"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                >
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                </svg>
                <svg
                  v-else-if="notification.type === 'error'"
                  class="h-5 w-5 text-red-400"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                >
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
                </svg>
                <svg
                  v-else-if="notification.type === 'warning'"
                  class="h-5 w-5 text-yellow-400"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                >
                  <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
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
                <p :class="[
                  'text-sm font-medium',
                  notification.type === 'success' ? 'text-green-800' :
                  notification.type === 'error' ? 'text-red-800' :
                  notification.type === 'warning' ? 'text-yellow-800' :
                  'text-blue-800'
                ]">
                  {{ notification.message }}
                </p>
                <p
                  v-if="notification.details"
                  :class="[
                    'mt-1 text-sm',
                    notification.type === 'success' ? 'text-green-700' :
                    notification.type === 'error' ? 'text-red-700' :
                    notification.type === 'warning' ? 'text-yellow-700' :
                    'text-blue-700'
                  ]"
                >
                  {{ notification.details }}
                </p>
              </div>
              <div class="ml-auto pl-3">
                <button
                  @click="closeNotification"
                  :class="[
                    'inline-flex rounded-md p-1.5 focus:outline-none focus:ring-2 focus:ring-offset-2',
                    notification.type === 'success' ? 'text-green-500 hover:bg-green-100 focus:ring-green-600' :
                    notification.type === 'error' ? 'text-red-500 hover:bg-red-100 focus:ring-red-600' :
                    notification.type === 'warning' ? 'text-yellow-500 hover:bg-yellow-100 focus:ring-yellow-600' :
                    'text-blue-500 hover:bg-blue-100 focus:ring-blue-600'
                  ]"
                >
                  <svg class="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>

          <!-- Dynamic Component Loading -->
          <div class="bg-white shadow rounded-lg">
            <div class="px-4 py-5 sm:p-6">
              <!-- Dashboard View -->
              <OrderDashboard
                v-if="activeTab === 'dashboard'"
                @show-notification="handleShowNotification"
                @set-loading="setLoading"
              />

              <!-- Order Planning View -->
              <OrderPlanningView
                v-else-if="activeTab === 'planning'"
                @show-notification="handleShowNotification"
                @set-loading="setLoading"
              />

              <!-- Order List View -->
              <OrderListView
                v-else-if="activeTab === 'orders'"
                @show-notification="handleShowNotification"
                @set-loading="setLoading"
              />

              <!-- Employee List View -->
              <EmployeeListView
                v-else-if="activeTab === 'employees'"
                @show-notification="handleShowNotification"
                @set-loading="setLoading"
              />

              <!-- Fallback for unknown tabs -->
              <div v-else class="text-center py-12">
                <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 12h6m-6-4h6m2 5.291A7.962 7.962 0 0112 15c-2.34 0-4.441.935-6 2.456"></path>
                </svg>
                <h3 class="mt-2 text-sm font-medium text-gray-900">Feature Coming Soon</h3>
                <p class="mt-1 text-sm text-gray-500">This section is under development.</p>
              </div>
            </div>
          </div>
        </div>
      </main>

      <!-- Footer -->
      <footer class="bg-white border-t mt-12">
        <div class="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          <div class="flex justify-between items-center">
            <div class="text-sm text-gray-500">
              © 2025 Pokemon Order Planning System - Version 1.0.0
            </div>
            <div class="flex space-x-6 text-sm text-gray-500">
              <span>🚀 Built with Vue 3 + TypeScript</span>
              <span>⚡ Powered by Spring Boot</span>
            </div>
          </div>
        </div>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

// Import only existing components - use async imports to avoid errors
const OrderDashboard = defineAsyncComponent(() => import('./components/OrderDashboard.vue'))
const OrderPlanningView = defineAsyncComponent(() => import('./components/OrderPlanningView.vue'))

// Create simple placeholder components for missing ones
const OrderListView = defineAsyncComponent(() =>
  import('./components/OrderListView.vue').catch(() => {
    console.warn('OrderListView not found, using placeholder')
    return Promise.resolve({
      template: `
        <div class="p-8 text-center">
          <h2 class="text-2xl font-bold text-gray-900 mb-4">📋 Order List</h2>
          <p class="text-gray-600 mb-4">Order management feature coming soon!</p>
          <button
            @click="$emit('show-notification', { message: 'Order List feature is under development', type: 'info' })"
            class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
          >
            Coming Soon
          </button>
        </div>
      `,
      emits: ['show-notification']
    })
  })
)

const EmployeeListView = defineAsyncComponent(() =>
  import('./components/EmployeeListView.vue').catch(() => {
    console.warn('EmployeeListView not found, using placeholder')
    return Promise.resolve({
      template: `
        <div class="p-8 text-center">
          <h2 class="text-2xl font-bold text-gray-900 mb-4">👥 Employee Management</h2>
          <p class="text-gray-600 mb-4">Employee management feature coming soon!</p>
          <button
            @click="$emit('show-notification', { message: 'Employee management feature is under development', type: 'info' })"
            class="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700"
          >
            Coming Soon
          </button>
        </div>
      `,
      emits: ['show-notification']
    })
  })
)

// Import defineAsyncComponent
import { defineAsyncComponent } from 'vue'

// ========== INTERFACES ==========
interface NotificationMessage {
  show: boolean
  message: string
  details?: string
  type: 'success' | 'error' | 'warning' | 'info'
}

interface NavigationTab {
  id: string
  label: string
}

// ========== APPLICATION STATE ==========
const isLoading = ref(true)
const loadingMessage = ref('Initializing Pokemon Order Planning System...')
const activeTab = ref('dashboard')

// Notification system
const notification = ref<NotificationMessage>({
  show: false,
  message: '',
  details: '',
  type: 'success'
})

// Auto-close timer for notifications
let notificationTimer: NodeJS.Timeout | null = null

// ========== NAVIGATION CONFIGURATION ==========
const tabs: NavigationTab[] = [
  { id: 'dashboard', label: '📊 Dashboard' },
  { id: 'planning', label: '📅 Order Planning' },
  { id: 'orders', label: '📋 Order List' },
  { id: 'employees', label: '👥 Employees' }
]

// ========== NAVIGATION METHODS ==========

/**
 * Switch between application tabs
 */
const switchTab = (tabId: string) => {
  console.log(`🔄 Switching to tab: ${tabId}`)
  activeTab.value = tabId

  // Close any open notifications when switching tabs
  closeNotification()

  // Update document title based on active tab
  updateDocumentTitle(tabId)
}

/**
 * Update browser document title based on current tab
 */
const updateDocumentTitle = (tabId: string) => {
  const tabLabel = tabs.find(tab => tab.id === tabId)?.label || 'Pokemon Planning'
  document.title = `${tabLabel} - Pokemon Order Planning`
}

// ========== NOTIFICATION SYSTEM ==========

/**
 * Handle show notification event from child components
 */
const handleShowNotification = (config: any) => {
  showNotification(config)
}

/**
 * Display a notification message
 */
const showNotification = (config: {
  message: string
  details?: string
  type?: 'success' | 'error' | 'warning' | 'info'
  duration?: number
}) => {
  // Clear existing timer
  if (notificationTimer) {
    clearTimeout(notificationTimer)
  }

  // Set notification content
  notification.value = {
    show: true,
    message: config.message,
    details: config.details,
    type: config.type || 'success'
  }

  // Auto-close after specified duration (default 5 seconds for success, 10 for errors)
  const duration = config.duration || (config.type === 'error' ? 10000 : 5000)

  notificationTimer = setTimeout(() => {
    closeNotification()
  }, duration)

  console.log(`📢 Notification: ${config.type} - ${config.message}`)
}

/**
 * Close the current notification
 */
const closeNotification = () => {
  notification.value.show = false
  if (notificationTimer) {
    clearTimeout(notificationTimer)
    notificationTimer = null
  }
}

// ========== LOADING SYSTEM ==========

/**
 * Control global loading state
 */
const setLoading = (loading: boolean, message?: string) => {
  isLoading.value = loading
  if (message) {
    loadingMessage.value = message
  }
}

// ========== APPLICATION LIFECYCLE ==========

/**
 * Initialize the application
 */
const initializeApp = async () => {
  console.log('🚀 Starting Pokemon Order Planning System...')

  try {
    // Simulate application initialization
    setLoading(true, 'Loading system configuration...')
    await new Promise(resolve => setTimeout(resolve, 800))

    setLoading(true, 'Connecting to backend services...')
    await new Promise(resolve => setTimeout(resolve, 600))

    setLoading(true, 'Preparing user interface...')
    await new Promise(resolve => setTimeout(resolve, 400))

    // Application is ready
    setLoading(false)

    // Welcome notification
    showNotification({
      message: '🎉 Welcome to Pokemon Order Planning System!',
      details: 'System is ready for managing your Pokemon card orders.',
      type: 'success'
    })

    console.log('✅ Application initialized successfully')

  } catch (error) {
    console.error('❌ Application initialization failed:', error)
    setLoading(false)
    showNotification({
      message: 'Application initialization failed',
      details: error instanceof Error ? error.message : 'Unknown error occurred',
      type: 'error'
    })
  }
}

/**
 * Handle browser tab visibility changes
 */
const handleVisibilityChange = () => {
  if (document.hidden) {
    console.log('📱 Application tab hidden')
  } else {
    console.log('📱 Application tab visible')
    // Optionally refresh data when user returns to tab
  }
}

/**
 * Handle keyboard shortcuts
 */
const handleKeyboardShortcuts = (event: KeyboardEvent) => {
  // Ctrl/Cmd + Number keys for tab switching
  if ((event.ctrlKey || event.metaKey) && event.key >= '1' && event.key <= '4') {
    event.preventDefault()
    const tabIndex = parseInt(event.key) - 1
    if (tabs[tabIndex]) {
      switchTab(tabs[tabIndex].id)
    }
  }

  // Escape key to close notifications
  if (event.key === 'Escape' && notification.value.show) {
    closeNotification()
  }
}

// ========== LIFECYCLE HOOKS ==========

onMounted(async () => {
  console.log('🔧 App.vue mounted')

  // Add event listeners
  document.addEventListener('visibilitychange', handleVisibilityChange)
  document.addEventListener('keydown', handleKeyboardShortcuts)

  // Initialize the application
  await initializeApp()
})

onUnmounted(() => {
  console.log('🧹 App.vue unmounted')

  // Cleanup event listeners
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  document.removeEventListener('keydown', handleKeyboardShortcuts)

  // Clear notification timer
  if (notificationTimer) {
    clearTimeout(notificationTimer)
  }
})
</script>

<style scoped>
.pokemon-order-planning-app {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.loading-screen {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

main {
  flex: 1;
}

/* Smooth transitions for tab switching */
.tab-content {
  transition: all 0.3s ease-in-out;
}

/* Custom scrollbar for better UX */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}

/* Responsive design adjustments */
@media (max-width: 768px) {
  .tabs {
    flex-wrap: wrap;
  }

  .tab-button {
    flex: 1;
    min-width: 120px;
  }
}

/* Animation for notifications */
.notification-enter-active,
.notification-leave-active {
  transition: all 0.3s ease;
}

.notification-enter-from {
  opacity: 0;
  transform: translateY(-20px);
}

.notification-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* Focus states for accessibility */
button:focus {
  outline: 2px solid #3b82f6;
  outline-offset: 2px;
}

/* Print styles */
@media print {
  .loading-screen,
  header,
  nav,
  footer {
    display: none !important;
  }

  main {
    padding: 0 !important;
    margin: 0 !important;
  }
}
</style>
