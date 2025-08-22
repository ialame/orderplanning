#!/bin/bash

# =======================================================
# FIX NAVIGATION BUTTONS - CREATE MISSING VUE.JS VIEWS
# =======================================================

echo "🔧 FIXING NAVIGATION BUTTONS"
echo "============================="

cd src/main/frontend || {
    echo "❌ Cannot access frontend directory"
    exit 1
}

# 1. Create Orders.vue view
echo "📝 Creating Orders.vue view..."
cat > src/views/Orders.vue << 'EOF'
<template>
  <div class="orders-page">
    <!-- Header -->
    <div class="page-header">
      <h1 class="page-title">📦 Orders Management</h1>
      <p class="page-subtitle">Manage Pokemon card orders and track their processing status</p>
    </div>

    <!-- Statistics Cards -->
    <div class="stats-grid">
      <div class="stat-card total">
        <div class="stat-icon">📊</div>
        <div class="stat-content">
          <h3>Total Orders</h3>
          <p class="stat-number">{{ stats.total }}</p>
        </div>
      </div>

      <div class="stat-card pending">
        <div class="stat-icon">⏳</div>
        <div class="stat-content">
          <h3>Pending</h3>
          <p class="stat-number">{{ stats.pending }}</p>
        </div>
      </div>

      <div class="stat-card processing">
        <div class="stat-icon">⚡</div>
        <div class="stat-content">
          <h3>Processing</h3>
          <p class="stat-number">{{ stats.processing }}</p>
        </div>
      </div>

      <div class="stat-card completed">
        <div class="stat-icon">✅</div>
        <div class="stat-content">
          <h3>Completed</h3>
          <p class="stat-number">{{ stats.completed }}</p>
        </div>
      </div>
    </div>

    <!-- Action Buttons -->
    <div class="action-bar">
      <button @click="refreshOrders" class="btn-primary" :disabled="loading">
        {{ loading ? '🔄 Loading...' : '🔄 Refresh Orders' }}
      </button>
      <button @click="loadSampleData" class="btn-secondary">
        🧪 Load Sample Data
      </button>
      <button @click="generatePlanning" class="btn-success">
        🤖 Generate Planning
      </button>
    </div>

    <!-- Orders Table -->
    <div class="orders-table-container">
      <div class="table-header">
        <h2>📋 Orders List ({{ orders.length }} orders)</h2>
        <div class="table-filters">
          <select v-model="filterPriority" class="filter-select">
            <option value="">All Priorities</option>
            <option value="URGENT">🔴 Urgent</option>
            <option value="HIGH">🟠 High</option>
            <option value="MEDIUM">🟡 Medium</option>
            <option value="LOW">🟢 Low</option>
          </select>

          <select v-model="filterStatus" class="filter-select">
            <option value="">All Status</option>
            <option value="PENDING">⏳ Pending</option>
            <option value="PROCESSING">⚡ Processing</option>
            <option value="COMPLETED">✅ Completed</option>
          </select>
        </div>
      </div>

      <div v-if="loading" class="loading-state">
        <div class="loading-spinner">🔄</div>
        <p>Loading orders...</p>
      </div>

      <div v-else-if="filteredOrders.length === 0" class="empty-state">
        <div class="empty-icon">📦</div>
        <h3>No orders found</h3>
        <p>No orders match your current filters or no orders are available.</p>
        <button @click="loadSampleData" class="btn-primary">Load Sample Data</button>
      </div>

      <div v-else class="table-wrapper">
        <table class="orders-table">
          <thead>
            <tr>
              <th>Order ID</th>
              <th>Customer</th>
              <th>Cards</th>
              <th>Priority</th>
              <th>Status</th>
              <th>Created</th>
              <th>Estimated Time</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in filteredOrders" :key="order.id" class="order-row">
              <td class="order-id">{{ order.orderNumber }}</td>
              <td class="customer">{{ order.customer }}</td>
              <td class="cards">
                <span class="card-count">{{ order.cardCount }}</span>
                <span class="card-label">cards</span>
              </td>
              <td class="priority">
                <span :class="['priority-badge', order.priority.toLowerCase()]">
                  {{ getPriorityIcon(order.priority) }} {{ order.priority }}
                </span>
              </td>
              <td class="status">
                <span :class="['status-badge', order.status.toLowerCase()]">
                  {{ getStatusIcon(order.status) }} {{ order.status }}
                </span>
              </td>
              <td class="created">{{ formatDate(order.createdAt) }}</td>
              <td class="estimated-time">{{ order.estimatedMinutes }} min</td>
              <td class="actions">
                <button @click="viewOrder(order)" class="btn-view">👁️ View</button>
                <button @click="editOrder(order)" class="btn-edit">✏️ Edit</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Message -->
    <div v-if="message.text" :class="['message', message.type]">
      {{ message.text }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// State
const loading = ref(false)
const orders = ref([])
const filterPriority = ref('')
const filterStatus = ref('')
const message = ref({ text: '', type: 'info' })

// Stats
const stats = computed(() => {
  const total = orders.value.length
  const pending = orders.value.filter(o => o.status === 'PENDING').length
  const processing = orders.value.filter(o => o.status === 'PROCESSING').length
  const completed = orders.value.filter(o => o.status === 'COMPLETED').length

  return { total, pending, processing, completed }
})

// Filtered orders
const filteredOrders = computed(() => {
  return orders.value.filter(order => {
    const priorityMatch = !filterPriority.value || order.priority === filterPriority.value
    const statusMatch = !filterStatus.value || order.status === filterStatus.value
    return priorityMatch && statusMatch
  })
})

// Methods
const refreshOrders = async () => {
  loading.value = true
  try {
    console.log('🔄 Refreshing orders...')

    const response = await fetch('/api/orders')
    if (response.ok) {
      const data = await response.json()
      orders.value = data.map(order => ({
        ...order,
        orderNumber: order.orderNumber || `ORD-${order.id?.slice(-6)}`,
        customer: order.customer || `Customer ${Math.floor(Math.random() * 1000)}`,
        cardCount: order.cardCount || Math.floor(Math.random() * 50) + 1,
        priority: order.priority || ['URGENT', 'HIGH', 'MEDIUM', 'LOW'][Math.floor(Math.random() * 4)],
        status: order.status || ['PENDING', 'PROCESSING', 'COMPLETED'][Math.floor(Math.random() * 3)],
        createdAt: order.createdAt || new Date().toISOString(),
        estimatedMinutes: (order.cardCount || 10) * 3
      }))

      showMessage(`✅ Loaded ${orders.value.length} orders`, 'success')
    } else {
      throw new Error(`HTTP ${response.status}`)
    }
  } catch (error) {
    console.error('Error loading orders:', error)
    loadSampleOrders() // Fallback to sample data
    showMessage('⚠️ Using sample data (backend not available)', 'warning')
  } finally {
    loading.value = false
  }
}

const loadSampleData = () => {
  console.log('🧪 Loading sample orders...')

  const sampleOrders = [
    {
      id: '1',
      orderNumber: 'ORD-001',
      customer: 'Pokemon Trainer Alex',
      cardCount: 25,
      priority: 'URGENT',
      status: 'PENDING',
      createdAt: new Date().toISOString(),
      estimatedMinutes: 75
    },
    {
      id: '2',
      orderNumber: 'ORD-002',
      customer: 'Card Collector Sarah',
      cardCount: 12,
      priority: 'HIGH',
      status: 'PROCESSING',
      createdAt: new Date(Date.now() - 3600000).toISOString(),
      estimatedMinutes: 36
    },
    {
      id: '3',
      orderNumber: 'ORD-003',
      customer: 'Battle League Mike',
      cardCount: 8,
      priority: 'MEDIUM',
      status: 'COMPLETED',
      createdAt: new Date(Date.now() - 7200000).toISOString(),
      estimatedMinutes: 24
    },
    {
      id: '4',
      orderNumber: 'ORD-004',
      customer: 'Gym Leader Emma',
      cardCount: 35,
      priority: 'LOW',
      status: 'PENDING',
      createdAt: new Date(Date.now() - 10800000).toISOString(),
      estimatedMinutes: 105
    }
  ]

  orders.value = sampleOrders
  showMessage('🧪 Sample orders loaded successfully', 'success')
}

const loadSampleOrders = () => loadSampleData()

const generatePlanning = async () => {
  try {
    showMessage('🤖 Generating planning...', 'info')

    const response = await fetch('/api/planning/generate', { method: 'POST' })
    if (response.ok) {
      showMessage('✅ Planning generated successfully!', 'success')
    } else {
      throw new Error('Planning generation failed')
    }
  } catch (error) {
    showMessage('❌ Planning generation failed', 'error')
  }
}

const viewOrder = (order) => {
  showMessage(`👁️ Viewing order ${order.orderNumber}`, 'info')
}

const editOrder = (order) => {
  showMessage(`✏️ Editing order ${order.orderNumber}`, 'info')
}

const showMessage = (text, type = 'info') => {
  message.value = { text, type }
  setTimeout(() => {
    message.value = { text: '', type: 'info' }
  }, 3000)
}

// Utility functions
const getPriorityIcon = (priority) => {
  const icons = {
    'URGENT': '🔴',
    'HIGH': '🟠',
    'MEDIUM': '🟡',
    'LOW': '🟢'
  }
  return icons[priority] || '⚪'
}

const getStatusIcon = (status) => {
  const icons = {
    'PENDING': '⏳',
    'PROCESSING': '⚡',
    'COMPLETED': '✅'
  }
  return icons[status] || '❓'
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Initialize
onMounted(() => {
  refreshOrders()
})
</script>

<style scoped>
.orders-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-title {
  font-size: 2.5rem;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 8px;
}

.page-subtitle {
  color: #6b7280;
  font-size: 1.1rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 15px;
  border-left: 4px solid;
}

.stat-card.total { border-left-color: #3b82f6; }
.stat-card.pending { border-left-color: #f59e0b; }
.stat-card.processing { border-left-color: #8b5cf6; }
.stat-card.completed { border-left-color: #10b981; }

.stat-icon {
  font-size: 2rem;
}

.stat-content h3 {
  font-size: 0.9rem;
  color: #6b7280;
  margin-bottom: 4px;
  text-transform: uppercase;
  font-weight: 600;
}

.stat-number {
  font-size: 2rem;
  font-weight: bold;
  color: #1f2937;
}

.action-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.btn-primary, .btn-secondary, .btn-success, .btn-view, .btn-edit {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover { background: #2563eb; }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }

.btn-secondary {
  background: #6b7280;
  color: white;
}

.btn-secondary:hover { background: #4b5563; }

.btn-success {
  background: #10b981;
  color: white;
}

.btn-success:hover { background: #059669; }

.orders-table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.table-header {
  padding: 20px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.table-header h2 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
}

.table-filters {
  display: flex;
  gap: 10px;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
}

.loading-state, .empty-state {
  text-align: center;
  padding: 60px 20px;
}

.loading-spinner {
  font-size: 3rem;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.table-wrapper {
  overflow-x: auto;
}

.orders-table {
  width: 100%;
  border-collapse: collapse;
}

.orders-table th {
  background: #f9fafb;
  padding: 12px;
  text-align: left;
  font-weight: 600;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
}

.orders-table td {
  padding: 12px;
  border-bottom: 1px solid #f3f4f6;
}

.order-row:hover {
  background: #f9fafb;
}

.order-id {
  font-family: monospace;
  font-weight: 600;
  color: #3b82f6;
}

.cards {
  display: flex;
  align-items: center;
  gap: 5px;
}

.card-count {
  font-weight: 600;
  color: #1f2937;
}

.card-label {
  color: #6b7280;
  font-size: 0.9rem;
}

.priority-badge, .status-badge {
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
}

.priority-badge.urgent { background: #fee2e2; color: #dc2626; }
.priority-badge.high { background: #fed7aa; color: #ea580c; }
.priority-badge.medium { background: #fef3c7; color: #d97706; }
.priority-badge.low { background: #dcfce7; color: #16a34a; }

.status-badge.pending { background: #fef3c7; color: #d97706; }
.status-badge.processing { background: #e0e7ff; color: #7c3aed; }
.status-badge.completed { background: #dcfce7; color: #16a34a; }

.actions {
  display: flex;
  gap: 8px;
}

.btn-view, .btn-edit {
  padding: 4px 8px;
  font-size: 0.8rem;
}

.btn-view {
  background: #e0e7ff;
  color: #3730a3;
}

.btn-view:hover { background: #c7d2fe; }

.btn-edit {
  background: #fef3c7;
  color: #92400e;
}

.btn-edit:hover { background: #fde68a; }

.message {
  position: fixed;
  bottom: 20px;
  right: 20px;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  font-weight: 500;
  z-index: 1000;
}

.message.success {
  background: #dcfce7;
  color: #16a34a;
  border: 1px solid #bbf7d0;
}

.message.error {
  background: #fee2e2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.message.warning {
  background: #fef3c7;
  color: #d97706;
  border: 1px solid #fde68a;
}

.message.info {
  background: #e0f2fe;
  color: #0891b2;
  border: 1px solid #bae6fd;
}
</style>
EOF

# 2. Create Employees.vue view
echo "📝 Creating Employees.vue view..."
cat > src/views/Employees.vue << 'EOF'
<template>
  <div class="employees-page">
    <!-- Header -->
    <div class="page-header">
      <h1 class="page-title">👥 Employee Management</h1>
      <p class="page-subtitle">Manage your team and track their workload</p>
    </div>

    <!-- Statistics Cards -->
    <div class="stats-grid">
      <div class="stat-card total">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <h3>Total Employees</h3>
          <p class="stat-number">{{ stats.total }}</p>
        </div>
      </div>

      <div class="stat-card active">
        <div class="stat-icon">✅</div>
        <div class="stat-content">
          <h3>Active</h3>
          <p class="stat-number">{{ stats.active }}</p>
        </div>
      </div>

      <div class="stat-card busy">
        <div class="stat-icon">⚡</div>
        <div class="stat-content">
          <h3>Busy</h3>
          <p class="stat-number">{{ stats.busy }}</p>
        </div>
      </div>

      <div class="stat-card available">
        <div class="stat-icon">🆓</div>
        <div class="stat-content">
          <h3>Available</h3>
          <p class="stat-number">{{ stats.available }}</p>
        </div>
      </div>
    </div>

    <!-- Action Buttons -->
    <div class="action-bar">
      <button @click="refreshEmployees" class="btn-primary" :disabled="loading">
        {{ loading ? '🔄 Loading...' : '🔄 Refresh Employees' }}
      </button>
      <button @click="addEmployee" class="btn-success">
        ➕ Add Employee
      </button>
      <button @click="loadSampleData" class="btn-secondary">
        🧪 Load Sample Data
      </button>
    </div>

    <!-- Employees Grid -->
    <div class="employees-container">
      <div class="section-header">
        <h2>👥 Team Members ({{ employees.length }})</h2>
        <div class="view-options">
          <button
            @click="viewMode = 'grid'"
            :class="['view-btn', { active: viewMode === 'grid' }]"
          >
            🔲 Grid
          </button>
          <button
            @click="viewMode = 'list'"
            :class="['view-btn', { active: viewMode === 'list' }]"
          >
            📋 List
          </button>
        </div>
      </div>

      <div v-if="loading" class="loading-state">
        <div class="loading-spinner">🔄</div>
        <p>Loading employees...</p>
      </div>

      <div v-else-if="employees.length === 0" class="empty-state">
        <div class="empty-icon">👥</div>
        <h3>No employees found</h3>
        <p>Add your first team member to get started.</p>
        <button @click="loadSampleData" class="btn-primary">Load Sample Data</button>
      </div>

      <!-- Grid View -->
      <div v-else-if="viewMode === 'grid'" class="employees-grid">
        <div
          v-for="employee in employees"
          :key="employee.id"
          class="employee-card"
          @click="viewEmployee(employee)"
        >
          <div class="employee-avatar">
            {{ getInitials(employee.name) }}
          </div>
          <div class="employee-info">
            <h3 class="employee-name">{{ employee.name }}</h3>
            <p class="employee-role">{{ employee.role }}</p>
            <div class="employee-stats">
              <span class="stat">
                📋 {{ employee.totalTasks || 0 }} tasks
              </span>
              <span class="stat">
                ⏱️ {{ employee.totalHours || 0 }}h
              </span>
            </div>
          </div>
          <div class="employee-status">
            <span :class="['status-badge', employee.status.toLowerCase()]">
              {{ getStatusIcon(employee.status) }} {{ employee.status }}
            </span>
          </div>
          <div class="employee-actions">
            <button @click.stop="editEmployee(employee)" class="btn-edit">✏️</button>
            <button @click.stop="viewSchedule(employee)" class="btn-schedule">📅</button>
          </div>
        </div>
      </div>

      <!-- List View -->
      <div v-else class="employees-list">
        <div class="list-header">
          <div class="col-name">Name</div>
          <div class="col-role">Role</div>
          <div class="col-status">Status</div>
          <div class="col-workload">Workload</div>
          <div class="col-actions">Actions</div>
        </div>
        <div
          v-for="employee in employees"
          :key="employee.id"
          class="employee-row"
          @click="viewEmployee(employee)"
        >
          <div class="col-name">
            <div class="employee-avatar-small">
              {{ getInitials(employee.name) }}
            </div>
            <div>
              <h4>{{ employee.name }}</h4>
              <p class="employee-email">{{ employee.email }}</p>
            </div>
          </div>
          <div class="col-role">{{ employee.role }}</div>
          <div class="col-status">
            <span :class="['status-badge', employee.status.toLowerCase()]">
              {{ getStatusIcon(employee.status) }} {{ employee.status }}
            </span>
          </div>
          <div class="col-workload">
            <div class="workload-bar">
              <div
                class="workload-fill"
                :style="{ width: `${employee.workloadPercentage || 0}%` }"
              ></div>
            </div>
            <span class="workload-text">{{ employee.workloadPercentage || 0 }}%</span>
          </div>
          <div class="col-actions">
            <button @click.stop="editEmployee(employee)" class="btn-action">✏️ Edit</button>
            <button @click.stop="viewSchedule(employee)" class="btn-action">📅 Schedule</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Message -->
    <div v-if="message.text" :class="['message', message.type]">
      {{ message.text }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// State
const loading = ref(false)
const employees = ref([])
const viewMode = ref('grid')
const message = ref({ text: '', type: 'info' })

// Stats
const stats = computed(() => {
  const total = employees.value.length
  const active = employees.value.filter(e => e.status === 'ACTIVE').length
  const busy = employees.value.filter(e => e.status === 'BUSY').length
  const available = employees.value.filter(e => e.status === 'AVAILABLE').length

  return { total, active, busy, available }
})

// Methods
const refreshEmployees = async () => {
  loading.value = true
  try {
    console.log('🔄 Refreshing employees...')

    const response = await fetch('/api/employees')
    if (response.ok) {
      const data = await response.json()
      employees.value = data.map(employee => ({
        ...employee,
        role: employee.role || 'Card Processor',
        status: employee.status || ['ACTIVE', 'BUSY', 'AVAILABLE'][Math.floor(Math.random() * 3)],
        email: employee.email || `${employee.name.toLowerCase().replace(' ', '.')}@pokemon.com`,
        totalTasks: employee.totalTasks || Math.floor(Math.random() * 20),
        totalHours: employee.totalHours || Math.floor(Math.random() * 40),
        workloadPercentage: employee.workloadPercentage || Math.floor(Math.random() * 100)
      }))

      showMessage(`✅ Loaded ${employees.value.length} employees`, 'success')
    } else {
      throw new Error(`HTTP ${response.status}`)
    }
  } catch (error) {
    console.error('Error loading employees:', error)
    loadSampleEmployees() // Fallback to sample data
    showMessage('⚠️ Using sample data (backend not available)', 'warning')
  } finally {
    loading.value = false
  }
}

const loadSampleData = () => {
  console.log('🧪 Loading sample employees...')

  const sampleEmployees = [
    {
      id: '1',
      name: 'Alice Johnson',
      role: 'Senior Card Processor',
      status: 'ACTIVE',
      email: 'alice.johnson@pokemon.com',
      totalTasks: 15,
      totalHours: 32,
      workloadPercentage: 85
    },
    {
      id: '2',
      name: 'Bob Smith',
      role: 'Card Processor',
      status: 'BUSY',
      email: 'bob.smith@pokemon.com',
      totalTasks: 12,
      totalHours: 28,
      workloadPercentage: 95
    },
    {
      id: '3',
      name: 'Carol Davis',
      role: 'Quality Checker',
      status: 'AVAILABLE',
      email: 'carol.davis@pokemon.com',
      totalTasks: 8,
      totalHours: 20,
      workloadPercentage: 60
    },
    {
      id: '4',
      name: 'David Wilson',
      role: 'Card Processor',
      status: 'ACTIVE',
      email: 'david.wilson@pokemon.com',
      totalTasks: 10,
      totalHours: 25,
      workloadPercentage: 75
    }
  ]

  employees.value = sampleEmployees
  showMessage('🧪 Sample employees loaded successfully', 'success')
}

const loadSampleEmployees = () => loadSampleData()

const addEmployee = () => {
  showMessage('➕ Add employee feature coming soon!', 'info')
}

const editEmployee = (employee) => {
  showMessage(`✏️ Editing ${employee.name}`, 'info')
}

const viewEmployee = (employee) => {
  showMessage(`👁️ Viewing ${employee.name}'s profile`, 'info')
}

const viewSchedule = (employee) => {
  showMessage(`📅 Viewing ${employee.name}'s schedule`, 'info')
}

const showMessage = (text, type = 'info') => {
  message.value = { text, type }
  setTimeout(() => {
    message.value = { text: '', type: 'info' }
  }, 3000)
}

// Utility functions
const getInitials = (name) => {
  return name.split(' ').map(n => n[0]).join('').toUpperCase()
}

const getStatusIcon = (status) => {
  const icons = {
    'ACTIVE': '✅',
    'BUSY': '⚡',
    'AVAILABLE': '🆓'
  }
  return icons[status] || '❓'
}

// Initialize
onMounted(() => {
  refreshEmployees()
})
</script>

<style scoped>
.employees-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-title {
  font-size: 2.5rem;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 8px;
}

.page-subtitle {
  color: #6b7280;
  font-size: 1.1rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 15px;
  border-left: 4px solid;
}

.stat-card.total { border-left-color: #3b82f6; }
.stat-card.active { border-left-color: #10b981; }
.stat-card.busy { border-left-color: #f59e0b; }
.stat-card.available { border-left-color: #8b5cf6; }

.stat-icon {
  font-size: 2rem;
}

.stat-content h3 {
  font-size: 0.9rem;
  color: #6b7280;
  margin-bottom: 4px;
  text-transform: uppercase;
  font-weight: 600;
}

.stat-number {
  font-size: 2rem;
  font-weight: bold;
  color: #1f2937;
}

.action-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.btn-primary, .btn-secondary, .btn-success {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover { background: #2563eb; }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }

.btn-secondary {
  background: #6b7280;
  color: white;
}

.btn-secondary:hover { background: #4b5563; }

.btn-success {
  background: #10b981;
  color: white;
}

.btn-success:hover { background: #059669; }

.employees-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.section-header {
  padding: 20px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.section-header h2 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
}

.view-options {
  display: flex;
  gap: 8px;
}

.view-btn {
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.view-btn.active {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.loading-state, .empty-state {
  text-align: center;
  padding: 60px 20px;
}

.loading-spinner {
  font-size: 3rem;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.employees-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  padding: 20px;
}

.employee-card {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.employee-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.employee-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #3b82f6;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 1.2rem;
  margin-bottom: 15px;
}

.employee-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 5px;
}

.employee-role {
  color: #6b7280;
  font-size: 0.9rem;
  margin-bottom: 15px;
}

.employee-stats {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.stat {
  font-size: 0.8rem;
  color: #6b7280;
}

.employee-status {
  margin-bottom: 15px;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
}

.status-badge.active { background: #dcfce7; color: #16a34a; }
.status-badge.busy { background: #fef3c7; color: #d97706; }
.status-badge.available { background: #e0e7ff; color: #7c3aed; }

.employee-actions {
  display: flex;
  gap: 8px;
  position: absolute;
  top: 15px;
  right: 15px;
}

.btn-edit, .btn-schedule {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.btn-edit {
  background: #fef3c7;
  color: #92400e;
}

.btn-edit:hover { background: #fde68a; }

.btn-schedule {
  background: #e0e7ff;
  color: #3730a3;
}

.btn-schedule:hover { background: #c7d2fe; }

.employees-list {
  padding: 0;
}

.list-header {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr;
  gap: 20px;
  padding: 15px 20px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
  font-weight: 600;
  color: #374151;
  font-size: 0.9rem;
  text-transform: uppercase;
}

.employee-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr;
  gap: 20px;
  padding: 15px 20px;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
  transition: background-color 0.2s;
  align-items: center;
}

.employee-row:hover {
  background: #f9fafb;
}

.col-name {
  display: flex;
  align-items: center;
  gap: 12px;
}

.employee-avatar-small {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #3b82f6;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 0.9rem;
}

.col-name h4 {
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 2px;
}

.employee-email {
  color: #6b7280;
  font-size: 0.8rem;
}

.workload-bar {
  width: 60px;
  height: 8px;
  background: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 4px;
}

.workload-fill {
  height: 100%;
  background: #3b82f6;
  transition: width 0.3s;
}

.workload-text {
  font-size: 0.8rem;
  color: #6b7280;
}

.btn-action {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  background: #e0e7ff;
  color: #3730a3;
  cursor: pointer;
  font-size: 0.8rem;
  margin-right: 4px;
  transition: background-color 0.2s;
}

.btn-action:hover {
  background: #c7d2fe;
}

.message {
  position: fixed;
  bottom: 20px;
  right: 20px;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  font-weight: 500;
  z-index: 1000;
}

.message.success {
  background: #dcfce7;
  color: #16a34a;
  border: 1px solid #bbf7d0;
}

.message.error {
  background: #fee2e2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.message.warning {
  background: #fef3c7;
  color: #d97706;
  border: 1px solid #fde68a;
}

.message.info {
  background: #e0f2fe;
  color: #0891b2;
  border: 1px solid #bae6fd;
}
</style>
EOF

