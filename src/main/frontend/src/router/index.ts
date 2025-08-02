// ============= ENGLISH ROUTER CONFIGURATION =============
// Configuration du routeur pour l'interface anglaise
// Traduction complète du routeur français

import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

// ========== LAZY LOADED COMPONENTS ==========
const OrderDashboard = () => import('@/components/OrderDashboard.vue')
const OrderPlanningView = () => import('@/components/OrderPlanningView.vue')
const OrderListView = () => import('@/components/OrderListView.vue')
const EmployeeListView = () => import('@/components/EmployeeListView.vue')
const PlanningCalendarView = () => import('@/components/PlanningCalendarView.vue')
const SystemSettingsView = () => import('@/components/SystemSettingsView.vue')

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
  {
    path: '/orders/:id',
    name: 'OrderDetail',
    component: () => import('@/components/OrderDetailView.vue'),
    props: true,
    meta: {
      title: 'Order Details',
      description: 'Detailed view of a specific order',
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
    component: () => import('@/components/EmployeeDetailView.vue'),
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
  {
    path: '/debug',
    name: 'SystemDebug',
    component: () => import('@/components/SystemDebugView.vue'),
    meta: {
      title: 'System Debug',
      description: 'Debug and monitoring tools',
      requiresAuth: true,
      debugOnly: true
    }
  },

  // ========== API DOCUMENTATION ==========
  {
    path: '/api-docs',
    name: 'ApiDocumentation',
    component: () => import('@/components/ApiDocumentationView.vue'),
    meta: {
      title: 'API Documentation',
      description: 'Complete API documentation for developers',
      requiresAuth: false
    }
  },

  // ========== ERROR PAGES ==========
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/components/NotFoundView.vue'),
    meta: {
      title: 'Page Not Found',
      description: 'The requested page could not be found',
      requiresAuth: false
    }
  },
  {
    path: '/error',
    name: 'ErrorPage',
    component: () => import('@/components/ErrorView.vue'),
    meta: {
      title: 'System Error',
      description: 'An error occurred while processing your request',
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
    // Always scroll to top when changing routes
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// ========== NAVIGATION GUARDS ==========

/**
 * Global before guard for authentication and meta handling
 */
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

  // Check if route requires authentication (if implemented)
  if (to.meta.requiresAuth) {
    // For now, we'll just log this - implement your auth logic here
    console.log('🔐 [ROUTER] Route requires authentication')
    // const isAuthenticated = await checkAuthentication()
    // if (!isAuthenticated) {
    //   return next('/login')
    // }
  }

  // Check if route is debug-only in production
  if (to.meta.debugOnly && import.meta.env.PROD) {
    console.warn('🚫 [ROUTER] Debug route accessed in production')
    return next('/404')
  }

  next()
})

/**
 * Global after guard for logging and analytics
 */
router.afterEach((to, from) => {
  console.log(`✅ [ROUTER] Navigation completed: ${to.path}`)

  // Here you could add analytics tracking
  // analytics.track('page_view', { path: to.path, name: to.name })
})

/**
 * Error handling for navigation failures
 */
router.onError((error) => {
  console.error('❌ [ROUTER] Navigation error:', error)

  // Redirect to error page on navigation failure
  router.push('/error')
})

// ========== ROUTER UTILITIES ==========

/**
 * Check if current route matches given path
 */
export const isCurrentRoute = (path: string): boolean => {
  return router.currentRoute.value.path === path
}

/**
 * Get current route name
 */
export const getCurrentRouteName = (): string | null | undefined => {
  return router.currentRoute.value.name?.toString()
}

/**
 * Navigate to route with error handling
 */
export const navigateTo = async (path: string): Promise<boolean> => {
  try {
    await router.push(path)
    return true
  } catch (error) {
    console.error('❌ [ROUTER] Navigation failed:', error)
    return false
  }
}

/**
 * Get route parameters
 */
export const getRouteParams = (): Record<string, string | string[]> => {
  return router.currentRoute.value.params
}

/**
 * Get query parameters
 */
export const getQueryParams = (): Record<string, string | string[]> => {
  return router.currentRoute.value.query
}

// ========== ROUTE DEFINITIONS FOR COMPONENTS ==========

/**
 * Navigation menu structure for frontend components
 */
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

/**
 * Admin menu items
 */
export const adminMenu = [
  {
    name: 'Settings',
    path: '/settings',
    icon: 'cog',
    description: 'System configuration'
  },
  {
    name: 'Debug',
    path: '/debug',
    icon: 'bug',
    description: 'System debugging tools'
  },
  {
    name: 'API Docs',
    path: '/api-docs',
    icon: 'code',
    description: 'API documentation'
  }
]

// ========== EXPORT ==========
export default router

// ========== TYPE DEFINITIONS ==========
export interface NavigationItem {
  name: string
  path: string
  icon: string
  description: string
}

export interface RouteMetaData {
  title?: string
  description?: string
  requiresAuth?: boolean
  debugOnly?: boolean
}

// ========== ROUTE NAMES ENUM ==========
export enum RouteNames {
  Dashboard = 'Dashboard',
  OrderPlanning = 'OrderPlanning',
  PlanningCalendar = 'PlanningCalendar',
  OrderList = 'OrderList',
  OrderDetail = 'OrderDetail',
  EmployeeList = 'EmployeeList',
  EmployeeDetail = 'EmployeeDetail',
  SystemSettings = 'SystemSettings',
  SystemDebug = 'SystemDebug',
  ApiDocumentation = 'ApiDocumentation',
  NotFound = 'NotFound',
  ErrorPage = 'ErrorPage'
}

// ========== API ENDPOINTS FOR REFERENCE ==========
export const API_ENDPOINTS = {
  // Planning endpoints
  generatePlanning: '/api/planning/generate',
  viewPlannings: '/api/planning/view-simple',
  viewPlanningsWithJoins: '/api/planning/view',

  // System endpoints
  systemInfo: '/api/planning/system/info',
  systemStats: '/api/planning/system/stats',

  // Legacy French endpoints (for compatibility)
  legacy: {
    planifierAutomatique: '/api/planifications/planifier-automatique',
    voirPlanifications: '/api/planifications/voir'
  }
} as const
