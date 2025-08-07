// ============= ENGLISH ROUTER CONFIGURATION =============
// Configuration du routeur pour l'interface anglaise
// Corrigé pour n'importer que les composants existants

import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { defineComponent, h } from 'vue'

// ========== EXISTING COMPONENTS ==========
const OrderDashboard = () => import('@/components/OrderDashboard.vue')
const OrderPlanningView = () => import('@/components/OrderPlanningView.vue')
const EmployeeListView = () => import('@/components/EmployeeListView.vue')
const EmployeeDetailView = () => import('@/components/EmployeeDetailView.vue')

// ========== PLACEHOLDER COMPONENTS FOR MISSING ONES ==========
const PlaceholderComponent = defineComponent({
  props: {
    title: { type: String, required: true },
    message: { type: String, required: true }
  },
  setup(props) {
    return () => h('div', { class: 'flex items-center justify-center min-h-screen bg-gray-50' }, [
      h('div', { class: 'text-center p-8' }, [
        h('div', { class: 'text-6xl mb-4' }, '🚧'),
        h('h1', { class: 'text-2xl font-bold text-gray-900 mb-2' }, props.title),
        h('p', { class: 'text-gray-600 mb-4' }, props.message),
        h('p', { class: 'text-sm text-gray-500' }, 'This component will be implemented soon.')
      ])
    ])
  }
})

// Create specific placeholder components
const OrderListView = defineComponent({
  setup() {
    return () => h(PlaceholderComponent, {
      title: 'Orders Management',
      message: 'Order list and management features are coming soon.'
    })
  }
})

const PlanningCalendarView = defineComponent({
  setup() {
    return () => h(PlaceholderComponent, {
      title: 'Planning Calendar',
      message: 'Calendar view for planning will be available soon.'
    })
  }
})

const SystemSettingsView = defineComponent({
  setup() {
    return () => h(PlaceholderComponent, {
      title: 'System Settings',
      message: 'System configuration panel is under development.'
    })
  }
})

const NotFoundView = defineComponent({
  setup() {
    return () => h('div', { class: 'flex items-center justify-center min-h-screen bg-gray-50' }, [
      h('div', { class: 'text-center p-8' }, [
        h('div', { class: 'text-6xl mb-4' }, '404'),
        h('h1', { class: 'text-2xl font-bold text-gray-900 mb-2' }, 'Page Not Found'),
        h('p', { class: 'text-gray-600 mb-4' }, 'The page you are looking for does not exist.'),
        h('button', {
          class: 'bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700',
          onClick: () => window.location.href = '/'
        }, 'Go Home')
      ])
    ])
  }
})

// ========== ROUTE DEFINITIONS ==========
const routes: Array<RouteRecordRaw> = [
  // ========== DASHBOARD ==========
  {
    path: '/',
    name: 'Dashboard',
    component: OrderDashboard,
    meta: {
      title: 'Dashboard - Pokemon Order Planning',
      description: 'Main dashboard for Pokemon card order planning system',
      requiresAuth: false
    }
  },

  // ========== PLANNING ROUTES ==========
  {
    path: '/planning',
    name: 'OrderPlanning',
    component: OrderPlanningView,
    meta: {
      title: 'Order Planning - Generate Assignments',
      description: 'Generate and manage Pokemon card order assignments',
      requiresAuth: false
    }
  },
  {
    path: '/planning/calendar',
    name: 'PlanningCalendar',
    component: PlanningCalendarView,
    meta: {
      title: 'Planning Calendar - Schedule View',
      description: 'Calendar view of all planned assignments',
      requiresAuth: false
    }
  },

  // ========== ORDER MANAGEMENT ==========
  {
    path: '/orders',
    name: 'OrderList',
    component: OrderListView,
    meta: {
      title: 'Orders - Manage Pokemon Orders',
      description: 'View and manage all Pokemon card orders',
      requiresAuth: false
    }
  },

  // ========== EMPLOYEE MANAGEMENT ==========
  {
    path: '/employees',
    name: 'EmployeeList',
    component: EmployeeListView,
    meta: {
      title: 'Employees - Manage Team',
      description: 'View and manage employee information',
      requiresAuth: false
    }
  },
  {
    path: '/employees/:id',
    name: 'EmployeeDetail',
    component: EmployeeDetailView,
    props: true,
    meta: {
      title: 'Employee Details',
      description: 'Detailed view of employee information',
      requiresAuth: false
    }
  },

  // ========== SYSTEM & SETTINGS ==========
  {
    path: '/settings',
    name: 'SystemSettings',
    component: SystemSettingsView,
    meta: {
      title: 'System Settings',
      description: 'Configure system parameters and preferences',
      requiresAuth: true
    }
  },

  // ========== ERROR PAGES ==========
  {
    path: '/404',
    name: 'NotFound',
    component: NotFoundView,
    meta: {
      title: 'Page Not Found',
      description: 'The requested page could not be found',
      requiresAuth: false
    }
  },

  // ========== REDIRECTS ==========
  {
    path: '/home',
    redirect: '/'
  },
  {
    path: '/dashboard',
    redirect: '/'
  },
  {
    path: '/planification', // French legacy support
    redirect: '/planning'
  },
  {
    path: '/commandes', // French legacy support
    redirect: '/orders'
  },
  {
    path: '/employes', // French legacy support
    redirect: '/employees'
  },

  // ========== CATCH-ALL ==========
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

// ========== ROUTER INSTANCE ==========
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// ========== NAVIGATION GUARDS ==========
router.beforeEach(async (to, from, next) => {
  console.log(`🧭 [ROUTER] Navigating from ${from.path} to ${to.path}`)

  // Update document title
  if (to.meta.title) {
    document.title = String(to.meta.title)
  }

  // Update meta description
  if (to.meta.description) {
    let metaDescription = document.querySelector('meta[name="description"]')
    if (!metaDescription) {
      metaDescription = document.createElement('meta')
      metaDescription.setAttribute('name', 'description')
      document.head.appendChild(metaDescription)
    }
    metaDescription.setAttribute('content', String(to.meta.description))
  }

  next()
})

router.afterEach((to, from) => {
  console.log(`✅ [ROUTER] Navigation completed: ${to.path}`)
})

router.onError((error) => {
  console.error('❌ [ROUTER] Navigation error:', error)
})

// ========== ROUTER UTILITIES ==========
export const navigateTo = async (path: string): Promise<boolean> => {
  try {
    await router.push(path)
    return true
  } catch (error) {
    console.error('❌ [ROUTER] Navigation failed:', error)
    return false
  }
}

// ========== NAVIGATION MENU ==========
export const navigationMenu = [
  {
    name: 'Dashboard',
    path: '/',
    icon: 'home',
    description: 'Main overview and statistics'
  },
  {
    name: 'Order Planning',
    path: '/planning',
    icon: 'calendar',
    description: 'Generate and manage order assignments'
  },
  {
    name: 'Orders',
    path: '/orders',
    icon: 'shopping-bag',
    description: 'View and manage Pokemon card orders'
  },
  {
    name: 'Employees',
    path: '/employees',
    icon: 'users',
    description: 'Manage team members and assignments'
  },
  {
    name: 'Calendar',
    path: '/planning/calendar',
    icon: 'calendar-days',
    description: 'Calendar view of all planned work'
  }
]

export const adminMenu = [
  {
    name: 'Settings',
    path: '/settings',
    icon: 'cog',
    description: 'System configuration'
  }
]

// ========== EXPORT ==========
export default router
