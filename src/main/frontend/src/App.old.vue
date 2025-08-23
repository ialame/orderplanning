<template>
  <div id="app" class="min-h-screen bg-gray-100">
    <!-- Navigation simple -->
    <nav class="bg-white shadow-md border-b">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center space-x-8">
            <!-- Logo -->
            <div class="flex-shrink-0">
              <h1 class="text-xl font-bold text-blue-600">🃏 Pokemon Orders</h1>
            </div>

            <!-- Menu principal -->
            <div class="flex space-x-4">
              <router-link
                to="/"
                class="px-3 py-2 rounded-md text-sm font-medium transition-colors"
                :class="$route.path === '/' ? 'bg-blue-100 text-blue-700' : 'text-gray-700 hover:text-blue-600 hover:bg-blue-50'"
              >
                📊 Dashboard
              </router-link>

              <router-link
                to="/orders"
                class="px-3 py-2 rounded-md text-sm font-medium transition-colors"
                :class="$route.path === '/orders' ? 'bg-blue-100 text-blue-700' : 'text-gray-700 hover:text-blue-600 hover:bg-blue-50'"
              >
                📦 Commandes
              </router-link>

              <router-link
                to="/employees"
                class="px-3 py-2 rounded-md text-sm font-medium transition-colors"
                :class="$route.path === '/employees' ? 'bg-blue-100 text-blue-700' : 'text-gray-700 hover:text-blue-600 hover:bg-blue-50'"
              >
                👥 Employés
              </router-link>

              <router-link
                to="/planning"
                class="px-3 py-2 rounded-md text-sm font-medium transition-colors"
                :class="$route.path === '/planning' ? 'bg-blue-100 text-blue-700' : 'text-gray-700 hover:text-blue-600 hover:bg-blue-50'"
              >
                📅 Planning
              </router-link>
            </div>
          </div>

          <!-- Status indicator -->
          <div class="flex items-center">
            <div class="flex items-center space-x-2">
              <div class="w-2 h-2 bg-green-500 rounded-full"></div>
              <span class="text-sm text-gray-600">Système opérationnel</span>
            </div>
          </div>
        </div>
      </div>
    </nav>

    <!-- Contenu principal -->
    <main class="flex-1">
      <router-view />
    </main>

    <!-- Notifications (si besoin) -->
    <div
      v-if="notification.show"
      class="fixed top-4 right-4 max-w-sm bg-white border border-gray-200 rounded-lg shadow-lg p-4 z-50"
      :class="notification.type === 'success' ? 'border-green-200 bg-green-50' : 'border-red-200 bg-red-50'"
    >
      <div class="flex">
        <div class="flex-shrink-0">
          <svg
            v-if="notification.type === 'success'"
            class="h-5 w-5 text-green-400"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
          </svg>
          <svg
            v-else
            class="h-5 w-5 text-red-400"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </div>
        <div class="ml-3">
          <p class="text-sm font-medium" :class="notification.type === 'success' ? 'text-green-800' : 'text-red-800'">
            {{ notification.message }}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, provide } from 'vue'

// État global pour les notifications
const notification = ref({
  show: false,
  message: '',
  type: 'success' as 'success' | 'error'
})

// Fonction pour afficher les notifications
const showNotification = (message: string, type: 'success' | 'error' = 'success') => {
  notification.value = { show: true, message, type }
  setTimeout(() => {
    notification.value.show = false
  }, 4000)
}

// Provide pour les composants enfants
provide('showNotification', showNotification)
</script>

<style>
/* Reset et styles globaux */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

#app {
  min-height: 100vh;
}

/* Classes utilitaires personnalisées pour compatibilité */
.btn-primary {
  @apply bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors font-medium;
}

.btn-secondary {
  @apply bg-gray-600 text-white px-4 py-2 rounded-lg hover:bg-gray-700 transition-colors font-medium;
}

.card {
  @apply bg-white rounded-lg shadow-md p-6;
}

.input-field {
  @apply w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent;
}

/* Animations */
.router-link-active {
  @apply bg-blue-100 text-blue-700;
}

/* Transition pour le changement de route */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
