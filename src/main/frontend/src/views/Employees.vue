<template>
  <div class="employees">
    <div class="page-header">
      <h1>👥 Employee Management</h1>
    </div>
    <div class="coming-soon">
      <div class="icon">🚧</div>
      <h2>Under Development</h2>
      <p>Employee management features will be available soon!</p>
      <router-link to="/" class="btn-primary">Back to Dashboard</router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Employees'
}
</script>

<style scoped>
.employees {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e1e5e9;
}

.coming-soon {
  text-align: center;
  padding: 80px 20px;
  background: #f8f9fa;
  border-radius: 15px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.coming-soon h2 {
  color: #333;
  margin-bottom: 15px;
}

.coming-soon p {
  color: #666;
  margin-bottom: 30px;
  font-size: 1.1rem;
}

.btn-primary {
  display: inline-block;
  background: #007bff;
  color: white;
  padding: 12px 24px;
  text-decoration: none;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-primary:hover {
  background: #0056b3;
  transform: translateY(-1px);
}
</style>
