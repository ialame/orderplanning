<template>
  <nav class="navigation-menu bg-white shadow-lg border-r border-gray-200">
    <!-- Logo Section -->
    <div class="logo-section p-6 border-b border-gray-200">
      <div class="flex items-center">
        <div class="w-10 h-10 bg-gradient-to-r from-blue-600 to-purple-600 rounded-lg flex items-center justify-center mr-3">
          <span class="text-white font-bold text-lg">🃏</span>
        </div>
        <div v-if="!collapsed">
          <h1 class="text-xl font-bold text-gray-900">Order Planning</h1>
          <p class="text-sm text-gray-600">Pokemon Cards</p>
        </div>
      </div>

      <!-- Collapse Toggle -->
      <button
        @click="toggleCollapse"
        class="mt-4 w-full flex items-center justify-center py-2 px-3 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors"
        :class="{ 'px-2': collapsed }"
      >
        <svg
          class="w-5 h-5 transition-transform"
          :class="{ 'rotate-180': collapsed }"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 19l-7-7 7-7m8 14l-7-7 7-7"/>
        </svg>
        <span v-if="!collapsed" class="ml-2 text-sm">Collapse</span>
      </button>
    </div>

    <!-- Main Navigation -->
    <div class="main-navigation p-4">
      <div class="space-y-2">
        <!-- Primary Menu Items -->
        <div class="menu-section">
          <h3 v-if="!collapsed" class="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-3">Main Menu</h3>

          <router-link
            v-for="item in mainMenuItems"
            :key="item.path"
            :to="item.path"
            class="nav-item flex items-center py-3 px-4 text-gray-700 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-all duration-200 group"
            :class="{
              'bg-blue-100 text-blue-700 shadow-sm': isActiveRoute(item.path),
              'justify-center': collapsed
            }"
            active-class="bg-blue-100 text-blue-700"
          >
            <component
              :is="getIconComponent(item.icon)"
              class="w-5 h-5 flex-shrink-0"
              :class="{ 'mr-3': !collapsed }"
            />
            <div v-if="!collapsed" class="flex-1">
              <div class="font-medium">{{ item.name }}</div>
              <div class="text-xs text-gray-500 group-hover:text-blue-500">{{ item.description }}</div>
            </div>
            <!-- Active indicator -->
            <div
              v-if="isActiveRoute(item.path) && !collapsed"
              class="w-2 h-2 bg-blue-600 rounded-full ml-2"
            ></div>
          </router-link>
        </div>

        <!-- Secondary Menu -->
        <div class="menu-section mt-8">
          <h3 v-if="!collapsed" class="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-3">System</h3>

          <router-link
            v-for="item in systemMenuItems"
            :key="item.path"
            :to="item.path"
            class="nav-item flex items-center py-3 px-4 text-gray-700 hover:text-purple-600 hover:bg-purple-50 rounded-lg transition-all duration-200 group"
            :class="{
              'bg-purple-100 text-purple-700 shadow-sm': isActiveRoute(item.path),
              'justify-center': collapsed
            }"
          >
            <component
              :is="getIconComponent(item.icon)"
              class="w-5 h-5 flex-shrink-0"
              :class="{ 'mr-3': !collapsed }"
            />
            <div v-if="!collapsed" class="flex-1">
              <div class="font-medium">{{ item.name }}</div>
              <div class="text-xs text-gray-500 group-hover:text-purple-500">{{ item.description }}</div>
            </div>
          </router-link>
        </div>
      </div>
    </div>

    <!-- Status Section -->
    <div class="status-section p-4 border-t border-gray-200 mt-auto">
      <!-- System Status -->
      <div class="mb-4">
        <div
          class="flex items-center p-3 rounded-lg"
          :class="{
            'bg-green-50 border border-green-200': systemStatus === 'operational',
            'bg-red-50 border border-red-200': systemStatus === 'error',
            'bg-yellow-50 border border-yellow-200': systemStatus === 'warning',
            'justify-center': collapsed
          }"
        >
          <div
            class="w-3 h-3 rounded-full"
            :class="{
              'bg-green-500': systemStatus === 'operational',
              'bg-red-500': systemStatus === 'error',
              'bg-yellow-500': systemStatus === 'warning',
              'mr-3': !collapsed
            }"
          ></div>
          <div v-if="!collapsed" class="flex-1">
            <div class="text-sm font-medium">
              {{ systemStatus === 'operational' ? 'System Operational' :
              systemStatus === 'error' ? 'System Error' : 'System Warning' }}
            </div>
            <div class="text-xs text-gray-600">{{ lastUpdated }}</div>
          </div>
        </div>
      </div>

      <!-- Quick Stats -->
      <div v-if="!collapsed && quickStats" class="bg-gray-50 rounded-lg p-3 mb-4">
        <h4 class="text-sm font-medium text-gray-900 mb-2">Quick Stats</h4>
        <div class="space-y-1">
          <div class="flex justify-between text-xs">
            <span class="text-gray-600">Orders:</span>
            <span class="font-medium">{{ formatNumber(quickStats.orders) }}</span>
          </div>
          <div class="flex justify-between text-xs">
            <span class="text-gray-600">Employees:</span>
            <span class="font-medium">{{ quickStats.employees }}</span>
          </div>
          <div class="flex justify-between text-xs">
            <span class="text-gray-600">Plannings:</span>
            <span class="font-medium">{{ formatNumber(quickStats.plannings) }}</span>
          </div>
        </div>
      </div>

      <!-- Language Toggle -->
      <div class="language-toggle">
        <button
          @click="$emit('toggle-language')"
          class="w-full flex items-center py-2 px-3 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors"
          :class="{ 'justify-center': collapsed }"
          title="Switch to French / Passer au français"
        >
          <svg class="w-5 h-5" :class="{ 'mr-3': !collapsed }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129"/>
          </svg>
          <span v-if="!collapsed" class="text-sm">Français</span>
        </button>
      </div>
    </div>

    <!-- Tooltip for collapsed items -->
    <div
      v-if="hoveredItem && collapsed"
      class="tooltip fixed bg-gray-900 text-white px-3 py-2 rounded-lg text-sm z-50 pointer-events-none"
      :style="tooltipStyle"
    >
      {{ hoveredItem.name }}
      <div class="text-xs opacity-75">{{ hoveredItem.description }}</div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'

// ========== EMITS ==========
const emit = defineEmits<{
  'toggle-language': []
  'collapse-change': [collapsed: boolean]
}>()

// ========== REACTIVE DATA ==========
const route = useRoute()
const collapsed = ref(false)
const systemStatus = ref<'operational' | 'error' | 'warning'>('operational')
const quickStats = ref<{ orders: number; employees: number; plannings: number } | null>(null)
const hoveredItem = ref<{ name: string; description: string } | null>(null)
const tooltipPosition = ref({ x: 0, y: 0 })

// ========== MENU CONFIGURATION ==========
const mainMenuItems = [
  {
    name: 'Dashboard',
    path: '/',
    icon: 'home',
    description: 'Overview & Statistics'
  },
  {
    name: 'Order Planning',
    path: '/planning',
    icon: 'calendar',
    description: 'Generate Assignments'
  },
  {
    name: 'Orders',
    path: '/orders',
    icon: 'shopping-bag',
    description: 'Manage Pokemon Orders'
  },
  {
    name: 'Employees',
    path: '/employees',
    icon: 'users',
    description: 'Team Management'
  },
  {
    name: 'Calendar',
    path: '/planning/calendar',
    icon: 'calendar-days',
    description: 'Schedule View'
  }
]

const systemMenuItems = [
  {
    name: 'Settings',
    path: '/settings',
    icon: 'settings',
    description: 'System Configuration'
  },
  {
    name: 'Debug',
    path: '/debug',
    icon: 'bug',
    description: 'Debugging Tools'
  },
  {
    name: 'API Docs',
    path: '/api-docs',
    icon: 'code',
    description: 'Developer Documentation'
  }
]

// ========== COMPUTED ==========
const lastUpdated = computed(() => {
  return new Date().toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit'
  })
})

const tooltipStyle = computed(() => ({
  left: `${tooltipPosition.value.x + 10}px`,
  top: `${tooltipPosition.value.y - 20}px`
}))

// ========== METHODS ==========
const toggleCollapse = () => {
  collapsed.value = !collapsed.value
  emit('collapse-change', collapsed.value)
}

const isActiveRoute = (path: string): boolean => {
  if (path === '/') {
    return route.path === '/'
  }
  return route.path.startsWith(path)
}

const getIconComponent = (iconName: string) => {
  // Return appropriate icon component based on icon name
  const iconMap: Record<string, string> = {
    'home': 'HomeIcon',
    'calendar': 'CalendarIcon',
    'shopping-bag': 'ShoppingBagIcon',
    'users': 'UsersIcon',
    'calendar-days': 'CalendarDaysIcon',
    'settings': 'SettingsIcon',
    'bug': 'BugIcon',
    'code': 'CodeIcon'
  }

  // For now, return a simple component - replace with actual icon library
  return {
    template: `
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        ${getIconSvgPath(iconName)}
      </svg>
    `
  }
}

const getIconSvgPath = (iconName: string): string => {
  const iconPaths: Record<string, string> = {
    'home': '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/>',
    'calendar': '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/>',
    'shopping-bag': '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l-1 8H6L5 9z"/>',
    'users': '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"/>',
    'calendar-days': '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/>',
    'settings': '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>',
    'bug': '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h4l4-4h2a1 1 0 011 1v4a1 1 0 01-1 1h-4l-4 4v2a1 1 0 001 1z"/>',
    'code': '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"/>'
  }
  return iconPaths[iconName] || iconPaths['home']
}

const formatNumber = (num: number): string => {
  return num.toLocaleString()
}

const loadQuickStats = async () => {
  try {
    // In a real app, this would call your API
    // For now, we'll use mock data
    quickStats.value = {
      orders: 450,
      employees: 8,
      plannings: 127
    }
    systemStatus.value = 'operational'
  } catch (error) {
    console.error('Failed to load quick stats:', error)
    systemStatus.value = 'error'
  }
}

const handleMouseEnter = (event: MouseEvent, item: { name: string; description: string }) => {
  if (collapsed.value) {
    hoveredItem.value = item
    tooltipPosition.value = { x: event.clientX, y: event.clientY }
  }
}

const handleMouseLeave = () => {
  hoveredItem.value = null
}

const handleMouseMove = (event: MouseEvent) => {
  if (hoveredItem.value && collapsed.value) {
    tooltipPosition.value = { x: event.clientX, y: event.clientY }
  }
}

// ========== LIFECYCLE ==========
onMounted(() => {
  loadQuickStats()

  // Refresh stats every 5 minutes
  const interval = setInterval(loadQuickStats, 5 * 60 * 1000)

  // Add mouse event listeners for tooltips
  document.addEventListener('mousemove', handleMouseMove)

  onUnmounted(() => {
    clearInterval(interval)
    document.removeEventListener('mousemove', handleMouseMove)
  })
})
</script>

<style scoped>
.navigation-menu {
  @apply h-screen flex flex-col;
  width: 280px;
  transition: width 0.3s ease;
}

.navigation-menu.collapsed {
  width: 80px;
}

.nav-item {
  position: relative;
  transition: all 0.2s ease;
}

.nav-item:hover {
  transform: translateX(2px);
}

.nav-item.router-link-active {
  transform: translateX(4px);
}

.tooltip {
  transition: opacity 0.2s ease;
  max-width: 200px;
  word-wrap: break-word;
}

.menu-section {
  position: relative;
}

.status-section {
  margin-top: auto;
}

/* Custom scrollbar for navigation */
.main-navigation {
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: #cbd5e0 transparent;
}

.main-navigation::-webkit-scrollbar {
  width: 4px;
}

.main-navigation::-webkit-scrollbar-track {
  background: transparent;
}

.main-navigation::-webkit-scrollbar-thumb {
  background: #cbd5e0;
  border-radius: 2px;
}

.main-navigation::-webkit-scrollbar-thumb:hover {
  background: #a0aec0;
}

/* Animation for collapse/expand */
@media (prefers-reduced-motion: no-preference) {
  .navigation-menu * {
    transition: all 0.3s ease;
  }
}

/* Responsive design */
@media (max-width: 768px) {
  .navigation-menu {
    position: fixed;
    left: 0;
    top: 0;
    z-index: 50;
    transform: translateX(-100%);
  }

  .navigation-menu.mobile-open {
    transform: translateX(0);
  }
}
</style>
