<template>
  <div class="planning">
    <div class="page-header">
      <h1>📅 Planning & Scheduling</h1>
    </div>
    <div class="coming-soon">
      <div class="icon">🔮</div>
      <h2>Advanced Planning System</h2>
      <p>Dynamic planning algorithm will be available soon!</p>
      <div class="features">
        <div class="feature">
          <h3>🤖 AI Planning</h3>
          <p>Intelligent task scheduling</p>
        </div>
        <div class="feature">
          <h3>⚡ Real-time Updates</h3>
          <p>Live planning adjustments</p>
        </div>
        <div class="feature">
          <h3>📊 Analytics</h3>
          <p>Performance insights</p>
        </div>
      </div>
      <router-link to="/" class="btn-primary">Back to Dashboard</router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Planning'
}
</script>

<style scoped>
.planning {
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
  padding: 60px 20px;
  background: #f8f9fa;
  border-radius: 15px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin: 40px 0;
}

.feature {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.feature h3 {
  color: #333;
  margin-bottom: 10px;
}

.feature p {
  color: #666;
  font-size: 0.9rem;
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
