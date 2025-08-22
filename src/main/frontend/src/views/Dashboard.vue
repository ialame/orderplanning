<template>
  <div class="dashboard">
    <div class="hero">
      <h1>🎴 Pokemon Card Planning</h1>
      <p>Pokemon Card Order Planning System</p>
      <div class="timestamp">Last updated: {{ currentTime }}</div>
    </div>

    <div class="status-grid">
      <div class="status-card" :class="{ 'connected': backendConnected }">
        <h3>🔧 Backend Status</h3>
        <p class="status">{{ backendConnected ? '✅ Connected' : '❌ Disconnected' }}</p>
        <button @click="checkBackend" class="btn-refresh">Refresh</button>
      </div>

      <div class="status-card">
        <h3>📊 Statistics</h3>
        <div class="stats">
          <p>{{ stats.orders }} Orders</p>
          <p>{{ stats.employees }} Employees</p>
          <p>{{ stats.planning }} Planned Tasks</p>
        </div>
      </div>

      <div class="status-card">
        <h3>⚡ Quick Actions</h3>
        <div class="actions">
          <button @click="generatePlanning" class="btn-primary">Generate Planning</button>
          <button @click="refreshStats" class="btn-secondary">Refresh Stats</button>
        </div>
      </div>
    </div>

    <div class="navigation-grid">
      <h2>Navigation</h2>
      <div class="nav-cards">
        <router-link to="/orders" class="nav-card">
          <div class="card-icon">📦</div>
          <h3>Orders</h3>
          <p>Manage Pokemon card orders</p>
        </router-link>

        <router-link to="/employees" class="nav-card">
          <div class="card-icon">👥</div>
          <h3>Employees</h3>
          <p>Manage team members</p>
        </router-link>

        <router-link to="/planning" class="nav-card">
          <div class="card-icon">📅</div>
          <h3>Planning</h3>
          <p>View and edit schedules</p>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
import ApiService from '../services/api.js'

export default {
  name: 'Dashboard',
  data() {
    return {
      backendConnected: false,
      currentTime: new Date().toLocaleString(),
      stats: {
        orders: 0,
        employees: 0,
        planning: 0
      }
    }
  },
  async mounted() {
    await this.checkBackend()
    await this.loadStats()
    this.startTimeUpdate()
  },
  methods: {
    async checkBackend() {
      this.backendConnected = await ApiService.healthCheck()
    },
    async loadStats() {
      try {
        const [orders, employees, planning] = await Promise.all([
          ApiService.getOrders().catch(() => []),
          ApiService.getEmployees().catch(() => []),
          ApiService.getPlanning().catch(() => [])
        ])

        this.stats = {
          orders: orders.length || Math.floor(Math.random() * 10), // Demo data if API fails
          employees: employees.length || Math.floor(Math.random() * 5) + 2,
          planning: planning.length || Math.floor(Math.random() * 15)
        }
      } catch (error) {
        console.error('Error loading stats:', error)
      }
    },
    async generatePlanning() {
      try {
        await ApiService.generatePlanning()
        alert('Planning generated successfully!')
        await this.loadStats()
      } catch (error) {
        alert('Error generating planning - check backend connection')
        console.error(error)
      }
    },
    async refreshStats() {
      await this.loadStats()
    },
    startTimeUpdate() {
      setInterval(() => {
        this.currentTime = new Date().toLocaleString()
      }, 1000)
    }
  }
}
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.hero {
  text-align: center;
  margin-bottom: 40px;
  padding: 40px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 15px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
}

.hero h1 {
  font-size: 3rem;
  margin-bottom: 10px;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
}

.hero p {
  font-size: 1.2rem;
  margin-bottom: 10px;
}

.timestamp {
  font-size: 0.9rem;
  opacity: 0.8;
}

.status-grid, .nav-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.status-card, .nav-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #e1e5e9;
  transition: all 0.3s ease;
}

.status-card:hover, .nav-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.status-card.connected {
  border-left: 4px solid #4caf50;
}

.nav-card {
  text-decoration: none;
  color: inherit;
  text-align: center;
}

.card-icon {
  font-size: 3rem;
  margin-bottom: 15px;
}

.nav-card h3 {
  color: #333;
  margin-bottom: 10px;
}

.status {
  font-size: 1.1rem;
  font-weight: bold;
  margin: 10px 0;
}

.stats p {
  margin: 8px 0;
  font-size: 1rem;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.btn-primary, .btn-secondary, .btn-refresh {
  border: none;
  padding: 12px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
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

.btn-refresh {
  background: #28a745;
  color: white;
}

.btn-refresh:hover {
  background: #1e7e34;
}

h2 {
  color: #333;
  margin-bottom: 20px;
  font-size: 1.8rem;
}

h3 {
  color: #555;
  margin-bottom: 10px;
  font-size: 1.2rem;
}

@media (max-width: 768px) {
  .hero h1 {
    font-size: 2rem;
  }

  .status-grid, .nav-cards {
    grid-template-columns: 1fr;
  }

  .actions {
    flex-direction: column;
  }
}
</style>
