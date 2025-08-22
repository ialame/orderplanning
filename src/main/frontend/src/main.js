import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

console.log('🚀 Starting Pokemon Card Planning App...')

const app = createApp(App)

app.use(router)

app.mount('#app')

console.log('✅ Vue.js application mounted successfully')
