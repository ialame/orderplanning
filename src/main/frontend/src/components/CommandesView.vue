<template>
  <div class="orders-view">
    <div class="header">
      <h1>Order Management</h1>
      <button @click="createNewOrder" class="btn-primary">
        ➕ New Order
      </button>
    </div>

    <div class="filters">
      <select v-model="filters.status">
        <option value="">All statuses</option>
        <option value="PENDING">Pending</option>
        <option value="IN_PROGRESS">In Progress</option>
        <option value="COMPLETED">Completed</option>
      </select>

      <select v-model="filters.priority">
        <option value="">All priorities</option>
        <option value="URGENT">Urgent</option>
        <option value="HIGH">High</option>
        <option value="MEDIUM">Medium</option>
        <option value="NORMAL">Normal</option>
      </select>
    </div>

    <div class="table-container">
      <table>
        <thead>
        <tr>
          <th>Number</th>
          <th>Cards</th>
          <th>Priority</th>
          <th>Status</th>
          <th>Deadline</th>
          <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="order in filteredOrders" :key="order.id">
          <td>{{ order.orderNumber }}</td>
          <td>{{ order.cardCount }} cards</td>
          <td>
              <span :class="getPriorityClass(order.priority)">
                {{ order.priority }}
              </span>
          </td>
          <td>{{ order.status }}</td>
          <td>{{ formatDate(order.deadline) }}</td>
          <td>
            <button @click="viewDetails(order)">Details</button>
            <button @click="scheduleOrder(order)">Schedule</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

const orders = ref([])
const loading = ref(false)

const filters = ref({
  status: '',
  priority: '',
  search: ''
})

const filteredOrders = computed(() => {
  return orders.value.filter(order => {
    if (filters.value.status && order.status !== filters.value.status) return false
    if (filters.value.priority && order.priority !== filters.value.priority) return false
    return true
  })
})

const loadOrders = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/orders/frontend/orders')
    orders.value = await response.json()
  } catch (error) {
    console.error('Error loading orders:', error)
  } finally {
    loading.value = false
  }
}

const createNewOrder = () => {
  // Creation logic
}

const viewDetails = (order) => {
  // Details logic
}

const scheduleOrder = (order) => {
  // Scheduling logic
}

onMounted(() => {
  loadOrders()
})
</script>
