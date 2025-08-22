<template>
  <div id="app">
    <Navigation />
    <main class="main-content">
      <router-view />
      <!-- Outil de debug temporaire -->
      <DebugTool />
    </main>
  </div>
</template>

<script>
import Navigation from './components/Navigation.vue'
import DebugTool from "./components/DebugTool.vue";

export default {
  name: 'App',
  components: {
    DebugTool,
    Navigation
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f8f9fa;
  color: #333;
  line-height: 1.6;
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding-top: 20px;
}

/* Global button styles */
.btn-primary {
  background: #007bff;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  text-decoration: none;
  display: inline-block;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-primary:hover {
  background: #0056b3;
  transform: translateY(-1px);
}

.btn-secondary {
  background: #6c757d;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-secondary:hover {
  background: #545b62;
}

/* Global header styles */
.page-header h1 {
  color: #333;
  font-size: 2rem;
  margin-bottom: 10px;
}

/* Responsive design */
@media (max-width: 768px) {
  .main-content {
    padding: 10px;
  }
}

/* Loading animation */
@keyframes pulse {
  0% { opacity: 0.6; }
  50% { opacity: 1; }
  100% { opacity: 0.6; }
}

.loading {
  animation: pulse 1.5s ease-in-out infinite;
}
</style>
