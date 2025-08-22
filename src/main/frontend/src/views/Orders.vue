<template>
  <div class="orders">
    <div class="page-header">
      <h1>📦 Order Management</h1>
      <button @click="showCreateForm = true" class="btn-primary">
        ➕ New Order
      </button>
    </div>

    <!-- Create form modal -->
    <div v-if="showCreateForm" class="modal-overlay" @click="closeModal">
      <div class="modal" @click.stop>
        <h2>Create New Order</h2>
        <form @submit.prevent="createOrder">
          <div class="form-group">
            <label>Order Number:</label>
            <input v-model="newOrder.numCommande" required placeholder="CMD-001">
          </div>
          <div class="form-group">
            <label>Number of Cards:</label>
            <input v-model.number="newOrder.nombreCartes" type="number" required min="1">
          </div>
          <div class="form-group">
            <label>Priority:</label>
            <select v-model="newOrder.priorite">
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
              <option value="URGENT">Urgent</option>
            </select>
          </div>
          <div class="form-actions">
            <button type="button" @click="closeModal" class="btn-secondary">Cancel</button>
            <button type="submit" class="btn-primary">Create Order</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Orders list -->
    <div class="orders-list">
      <div v-if="loading" class="loading">
        <p>Loading orders...</p>
      </div>

      <div v-else-if="orders.length === 0" class="empty-state">
        <h3>No orders found</h3>
        <p class="hint">{{ backendConnected ? 'Create your first order!' : 'Check backend connection' }}</p>
      </div>

      <div v-else class="orders-grid">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-header">
            <h3>{{ order.numCommande }}</h3>
            <span :class="'priority-' + order.priorite.toLowerCase()">{{ order.priorite }}</span>
          </div>
          <div class="order-details">
            <p><strong>Cards:</strong> {{ order.nombreCartes }}</p>
            <p><strong>Status:</strong> {{ getStatusLabel(order.statut) }}</p>
            <p v-if="order.prixTotal"><strong>Total:</strong> ${{ order.prixTotal }}</p>
            <p v-if="order.dureeEstimeeMinutes"><strong>Est. Duration:</strong> {{ order.dureeEstimeeMinutes }}min</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ApiService from '../services/api.js'

export default {
  name: 'Orders',
  data() {
    return {
      orders: [],
      showCreateForm: false,
      backendConnected: false,
      loading: true,
      newOrder: {
        numCommande: '',
        nombreCartes: 1,
        priorite: 'MEDIUM'
      }
    }
  },
  async mounted() {
    await this.loadOrders()
  },
  methods: {
    async loadOrders() {
      this.loading = true
      try {
        this.orders = await ApiService.getOrders()
        this.backendConnected = true
      } catch (error) {
        console.error('Error loading orders:', error)
        this.backendConnected = false
        // Demo data when backend is not available
        this.orders = [
          {
            id: '1',
            numCommande: 'CMD-001',
            nombreCartes: 100,
            priorite: 'HIGH',
            statut: 1,
            prixTotal: 250.00,
            dureeEstimeeMinutes: 300
          },
          {
            id: '2',
            numCommande: 'CMD-002',
            nombreCartes: 50,
            priorite: 'MEDIUM',
            statut: 2,
            prixTotal: 125.00,
            dureeEstimeeMinutes: 150
          }
        ]
      }
      this.loading = false
    },
    async createOrder() {
      try {
        await ApiService.createOrder(this.newOrder)
        this.closeModal()
        await this.loadOrders()
        alert('Order created successfully!')
      } catch (error) {
        alert('Error creating order - check backend connection')
        console.error(error)
      }
    },
    closeModal() {
      this.showCreateForm = false
      this.newOrder = { numCommande: '', nombreCartes: 1, priorite: 'MEDIUM' }
    },
    getStatusLabel(status) {
      const labels = { 1: 'Pending', 2: 'In Progress', 3: 'Completed' }
      return labels[status] || 'Unknown'
    }
  }
}
</script>

<style scoped>
.orders {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e1e5e9;
}

.orders-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.order-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  border-left: 4px solid #007bff;
  transition: transform 0.2s ease;
}

.order-card:hover {
  transform: translateY(-2px);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.order-header h3 {
  margin: 0;
  color: #333;
}

.priority-high, .priority-urgent {
  background: #dc3545;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: bold;
}

.priority-medium {
  background: #ffc107;
  color: #333;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: bold;
}

.priority-low {
  background: #28a745;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: bold;
}

.order-details p {
  margin: 8px 0;
  color: #666;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: white;
  padding: 30px;
  border-radius: 12px;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 20px 40px rgba(0,0,0,0.2);
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #333;
}

.form-group input, .form-group select {
  width: 100%;
  padding: 12px;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-group input:focus, .form-group select:focus {
  outline: none;
  border-color: #007bff;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 30px;
}

.btn-primary, .btn-secondary {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover {
  background: #0056b3;
  transform: translateY(-1px);
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #545b62;
}

.empty-state, .loading {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.empty-state h3 {
  margin-bottom: 10px;
  color: #333;
}

.hint {
  font-size: 0.9em;
  color: #999;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }

  .orders-grid {
    grid-template-columns: 1fr;
  }
}
</style>
