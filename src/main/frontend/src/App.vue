<template>
  <div id="app" class="min-h-screen bg-gray-100">
    <!-- Main Navigation -->
    <nav class="bg-blue-600 text-white shadow-lg">
      <div class="max-w-7xl mx-auto px-4">
        <div class="flex justify-between items-center h-16">
          <!-- Logo and Title -->
          <div class="flex items-center">
            <div class="flex items-center space-x-3">
              <div class="w-10 h-10 bg-white rounded-lg flex items-center justify-center">
                <span class="text-2xl">🃏</span>
              </div>
              <div>
                <h1 class="text-xl font-bold">OrderPlanning</h1>
                <p class="text-xs text-blue-200">Card Order Management System</p>
              </div>
            </div>
          </div>

          <!-- Navigation Menu -->
          <div class="flex space-x-1">
            <button
              v-for="tab in navigationTabs"
              :key="tab.id"
              @click="switchToTab(tab.id)"
              :class="[
                'px-4 py-2 rounded-md transition-all duration-200 font-medium',
                currentActiveTab === tab.id
                  ? 'bg-blue-700 text-white shadow-lg transform scale-105'
                  : 'text-blue-100 hover:text-white hover:bg-blue-500'
              ]"
            >
              {{ tab.label }}
            </button>
          </div>

          <!-- Status Indicator -->
          <div class="flex items-center space-x-3">
            <div class="flex items-center space-x-2 text-blue-200">
              <div class="w-2 h-2 bg-green-400 rounded-full animate-pulse"></div>
              <span class="text-sm">Online</span>
            </div>
          </div>
        </div>
      </div>
    </nav>

    <!-- Main Content Area -->
    <main class="max-w-7xl mx-auto px-4 py-6">
      <!-- Dashboard Component -->
      <DashboardView
        v-if="currentActiveTab === 'dashboard'"
        @go-to-tab="switchToTab"
      />

      <!-- Orders Management Component -->
      <OrdersView
        v-if="currentActiveTab === 'orders'"
      />

      <!-- Employees Management Component (with integrated planning) -->
      <EmployeesView
        v-if="currentActiveTab === 'employees'"
      />

      <!-- Planning Component -->
      <PlanningView
        v-if="currentActiveTab === 'planning'"
      />
    </main>

    <!-- Enhanced Notification System -->
    <Teleport to="body">
      <Transition
        enter-active-class="transition-all duration-300 ease-out"
        leave-active-class="transition-all duration-200 ease-in"
        enter-from-class="opacity-0 translate-y-2 scale-95"
        enter-to-class="opacity-100 translate-y-0 scale-100"
        leave-from-class="opacity-100 translate-y-0 scale-100"
        leave-to-class="opacity-0 translate-y-2 scale-95"
      >
        <div
          v-if="notificationSystem.show"
          :class="[
            'fixed top-4 right-4 p-4 rounded-lg shadow-2xl transition-all z-50 max-w-md',
            notificationSystem.type === 'success' ? 'bg-green-500 text-white' :
            notificationSystem.type === 'error' ? 'bg-red-500 text-white' :
            notificationSystem.type === 'warning' ? 'bg-yellow-500 text-white' :
            'bg-blue-500 text-white'
          ]"
        >
          <div class="flex items-center space-x-3">
            <div class="flex-shrink-0">
              <span v-if="notificationSystem.type === 'success'">✅</span>
              <span v-else-if="notificationSystem.type === 'error'">❌</span>
              <span v-else-if="notificationSystem.type === 'warning'">⚠️</span>
              <span v-else>ℹ️</span>
            </div>
            <div class="flex-1">
              <p class="font-medium">{{ notificationSystem.message }}</p>
              <p v-if="notificationSystem.details" class="text-sm opacity-90 mt-1">
                {{ notificationSystem.details }}
              </p>
            </div>
            <button
              @click="closeNotification"
              class="flex-shrink-0 text-white hover:text-gray-200 transition-colors"
            >
              <span class="sr-only">Close</span>
              ✕
            </button>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- Global Loading Overlay -->
    <Teleport to="body">
      <Transition
        enter-active-class="transition-opacity duration-300"
        leave-active-class="transition-opacity duration-300"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <div
          v-if="globalLoadingState"
          class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
        >
          <div class="bg-white rounded-lg p-6 flex items-center space-x-4 shadow-2xl">
            <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            <span class="text-gray-700 font-medium">{{ currentLoadingMessage || 'Loading...' }}</span>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, provide, onMounted, onErrorCaptured } from 'vue'
import DashboardView from './components/DashboardView.vue'
import OrdersView from './components/OrdersView.vue'
import EmployeesView from './components/EmployeesView.vue'
import PlanningView from './components/PlanningView.vue'

// ========== TYPE DEFINITIONS ==========
interface NotificationConfig {
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
const currentActiveTab = ref('dashboard')
const globalLoadingState = ref(false)
const currentLoadingMessage = ref('')

// Dans App.vue, remplacer la définition des onglets par :

const tabs = [
  { id: 'dashboard', label: '📊 Dashboard' },
  { id: 'orders', label: '📋 Orders' },      // au lieu de 'commandes'
  { id: 'employees', label: '👥 Employees' }, // au lieu de 'employés'
  { id: 'planning', label: '📅 Planning' }   // au lieu de 'planification'
]

// Enhanced notification system
const notificationSystem = ref<NotificationConfig>({
  show: false,
  message: '',
  details: '',
  type: 'success'
})

// Timer for auto-closing notifications
let notificationAutoCloseTimer: NodeJS.Timeout | null = null

// ========== APPLICATION CONFIGURATION ==========
const navigationTabs: NavigationTab[] = [
  { id: 'dashboard', label: '📊 Dashboard' },
  { id: 'orders', label: '📋 Orders' },
  { id: 'employees', label: '👥 Employees' },
  { id: 'planning', label: '📅 Planning' }
]

// ========== NAVIGATION METHODS ==========

/**
 * Switch to a different tab/section
 * @param tabId - The ID of the tab to switch to
 */
const switchToTab = (tabId: string) => {
  console.log(`[App] Switching to tab: ${tabId}`)
  currentActiveTab.value = tabId

  // Close any open notifications when switching tabs
  closeNotification()
}

// ========== NOTIFICATION SYSTEM ==========

/**
 * Display a notification message to the user
 * @param message - The main notification message
 * @param type - Type of notification (success, error, warning, info)
 * @param details - Optional additional details
 * @param autoCloseDuration - Duration in ms before auto-close (0 = no auto-close)
 */
const displayNotification = (
  message: string,
  type: 'success' | 'error' | 'warning' | 'info' = 'success',
  details?: string,
  autoCloseDuration: number = 5000
) => {
  // Clear any existing timer
  if (notificationAutoCloseTimer) {
    clearTimeout(notificationAutoCloseTimer)
  }

  // Set notification content
  notificationSystem.value = {
    show: true,
    message,
    details,
    type
  }

  // Set auto-close timer if duration > 0
  if (autoCloseDuration > 0) {
    notificationAutoCloseTimer = setTimeout(() => {
      closeNotification()
    }, autoCloseDuration)
  }

  console.log(`[App] Notification displayed: ${type} - ${message}`)
}

/**
 * Close the current notification
 */
const closeNotification = () => {
  notificationSystem.value.show = false
  if (notificationAutoCloseTimer) {
    clearTimeout(notificationAutoCloseTimer)
    notificationAutoCloseTimer = null
  }
}

// ========== LOADING SYSTEM ==========

/**
 * Control global loading state
 * @param isLoading - Whether to show loading overlay
 * @param message - Optional loading message
 */
const setGlobalLoadingState = (isLoading: boolean, message?: string) => {
  globalLoadingState.value = isLoading
  currentLoadingMessage.value = message || ''
  console.log(`[App] Global loading state: ${isLoading} - ${message || 'No message'}`)
}

// ========== ERROR HANDLING ==========

/**
 * Handle global application errors
 * @param error - The error that occurred
 * @param errorContext - Context where the error occurred
 */
const handleApplicationError = (error: Error, errorContext: string = 'Application') => {
  console.error(`[App] Error in ${errorContext}:`, error)
  displayNotification(
    'An error occurred',
    'error',
    error.message,
    0 // Don't auto-close error notifications
  )
  setGlobalLoadingState(false)
}

// ========== DEPENDENCY INJECTION ==========
// Provide functions for child components to use
provide('showNotification', displayNotification)
provide('changeTab', switchToTab)
provide('setGlobalLoading', setGlobalLoadingState)
provide('handleGlobalError', handleApplicationError)

// ========== ERROR CAPTURE ==========
onErrorCaptured((error: Error, componentInstance, errorInfo) => {
  handleApplicationError(error, `Vue Component (${errorInfo})`)
  return false // Prevent error propagation
})

// ========== LIFECYCLE HOOKS ==========
onMounted(() => {
  console.log('[App] OrderPlanning application initialized successfully')

  // Display welcome notification
  setTimeout(() => {
    displayNotification(
      'Welcome to OrderPlanning!',
      'success',
      'Card order management system ready',
      3000
    )
  }, 1000)

  // Set up global error handling for unhandled promise rejections
  window.addEventListener('unhandledrejection', (event) => {
    console.error('[App] Unhandled promise rejection:', event.reason)
    handleApplicationError(
      new Error(event.reason?.message || 'Network error occurred'),
      'Network Request'
    )
  })

  // Set up global error handling for uncaught exceptions
  window.addEventListener('error', (event) => {
    console.error('[App] Uncaught error:', event.error)
    handleApplicationError(
      event.error || new Error('Unknown error occurred'),
      'JavaScript Runtime'
    )
  })
})
</script>

<style scoped>
/* ========== CUSTOM ANIMATIONS ========== */
@keyframes slideInFromTop {
  from {
    transform: translateY(-100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@keyframes pulseAnimation {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* ========== VISUAL ENHANCEMENTS ========== */
nav {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
}

.animate-pulse {
  animation: pulseAnimation 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

/* ========== RESPONSIVE DESIGN ========== */
@media (max-width: 768px) {
  .max-w-7xl {
    padding-left: 1rem;
    padding-right: 1rem;
  }

  nav .flex.space-x-1 {
    gap: 0.25rem;
  }

  nav button {
    padding: 0.5rem 0.75rem;
    font-size: 0.875rem;
  }

  /* Stack navigation vertically on very small screens */
  @media (max-width: 480px) {
    nav .flex.justify-between {
      flex-direction: column;
      align-items: center;
      gap: 0.5rem;
      padding: 0.5rem 0;
    }

    nav .h-16 {
      height: auto;
      min-height: 4rem;
    }
  }
}

/* ========== ACCESSIBILITY IMPROVEMENTS ========== */
@media (prefers-reduced-motion: reduce) {
  .transition-all,
  .animate-spin,
  .animate-pulse {
    animation: none !important;
    transition: none !important;
  }
}

/* ========== FOCUS STATES FOR ACCESSIBILITY ========== */
button:focus-visible {
  outline: 2px solid #ffffff;
  outline-offset: 2px;
}

/* ========== LOADING SPINNER ANIMATION ========== */
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}
</style>

<style>
/* ========== GLOBAL APPLICATION STYLES ========== */
#app {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
  'Helvetica Neue', Arial, sans-serif;
  line-height: 1.6;
  color: #1f2937;
  background-color: #f9fafb;
}

/* ========== CUSTOM SCROLLBAR STYLES ========== */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* ========== FOCUS VISIBLE FOR ACCESSIBILITY ========== */
button:focus-visible,
input:focus-visible,
select:focus-visible,
textarea:focus-visible {
  outline: 2px solid #3b82f6;
  outline-offset: 2px;
}

/* ========== UTILITY CLASSES ========== */
.text-shadow {
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.glass-effect {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* ========== SMOOTH SCROLLING ========== */
html {
  scroll-behavior: smooth;
}

/* ========== SELECTION STYLING ========== */
::selection {
  background-color: #3b82f6;
  color: white;
}

/* ========== PRINT STYLES ========== */
@media print {
  .no-print {
    display: none !important;
  }

  #app {
    background: white;
  }

  nav {
    background: #3b82f6 !important;
    -webkit-print-color-adjust: exact;
  }
}
</style>
