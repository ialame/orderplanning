<template>
  <nav class="navbar">
    <div class="nav-container">
      <router-link to="/" class="nav-brand">
        🎴 Pokemon Card Planning
      </router-link>

      <div class="nav-menu">
        <router-link to="/" class="nav-link">Dashboard</router-link>
        <router-link to="/orders" class="nav-link">Orders</router-link>
        <router-link to="/employees" class="nav-link">Employees</router-link>
        <router-link to="/planning" class="nav-link">Planning</router-link>
      </div>

      <div class="nav-status">
        <div class="status-indicator" :class="{ 'online': isOnline }"></div>
        <span class="status-text">{{ isOnline ? 'Online' : 'Offline' }}</span>
      </div>
    </div>
  </nav>
</template>

<script>
export default {
  name: 'Navigation',
  data() {
    return {
      isOnline: navigator.onLine
    }
  },
  mounted() {
    window.addEventListener('online', this.updateOnlineStatus)
    window.addEventListener('offline', this.updateOnlineStatus)
  },
  unmounted() {
    window.removeEventListener('online', this.updateOnlineStatus)
    window.removeEventListener('offline', this.updateOnlineStatus)
  },
  methods: {
    updateOnlineStatus() {
      this.isOnline = navigator.onLine
    }
  }
}
</script>

<style scoped>
.navbar {
  background: #343a40;
  color: white;
  padding: 1rem 0;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.nav-brand {
  font-size: 1.5rem;
  font-weight: bold;
  color: white;
  text-decoration: none;
  transition: color 0.2s;
}

.nav-brand:hover {
  color: #ccc;
}

.nav-menu {
  display: flex;
  gap: 2rem;
}

.nav-link {
  color: #adb5bd;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
  position: relative;
}

.nav-link:hover {
  color: white;
}

.nav-link.router-link-active {
  color: white;
}

.nav-link.router-link-active::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 0;
  right: 0;
  height: 2px;
  background: #007bff;
}

.nav-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #dc3545;
  transition: background 0.2s;
}

.status-indicator.online {
  background: #28a745;
}

.status-text {
  color: #adb5bd;
}

@media (max-width: 768px) {
  .nav-container {
    flex-direction: column;
    gap: 1rem;
  }

  .nav-menu {
    gap: 1.5rem;
  }

  .nav-status {
    order: -1;
  }
}
</style>
