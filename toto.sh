# ========== ÉTAPE 2 : CRÉER UNE NOUVELLE VUE DE TEST ==========

# 2.1 Créez le fichier de vue
cd src/main/frontend/src/views/
touch PlanningTest.vue

# 2.2 Copiez ce contenu dans PlanningTest.vue :
cat > PlanningTest.vue << 'EOF'
<template>
  <div class="planning-test">
    <h2>🧪 Test du Nouveau Service de Planification</h2>

    <!-- Formulaire Simple -->
    <div class="test-form">
      <h3>Configuration</h3>
      <div class="form-row">
        <label>Orders depuis :</label>
        <input v-model="config.orderStartDate" type="date" />
      </div>

      <div class="form-row">
        <label>Planification depuis :</label>
        <input v-model="config.planningStartDate" type="date" />
      </div>

      <div class="form-row">
        <label>Max employés :</label>
        <input v-model.number="config.maxEmployees" type="number" min="1" max="10" />
      </div>

      <div class="form-row">
        <label>Temps par carte (min) :</label>
        <input v-model.number="config.timePerCard" type="number" min="1" max="10" />
      </div>

      <div class="checkbox-row">
        <label>
          <input v-model="config.preventDuplicates" type="checkbox" />
          Éviter les doublons
        </label>
      </div>
    </div>

    <!-- Boutons de Test -->
    <div class="test-buttons">
      <button @click="testGeneration" :disabled="testing" class="btn-primary">
        {{ testing ? '⏳ Test...' : '🚀 Tester Génération' }}
      </button>

      <button @click="testStatus" :disabled="checking" class="btn-info">
        {{ checking ? '⏳ Vérif...' : '🔍 Tester Statut' }}
      </button>

      <button @click="testCleanDuplicates" :disabled="cleaning" class="btn-warning">
        {{ cleaning ? '⏳ Nettoyage...' : '🧹 Nettoyer Doublons' }}
      </button>
    </div>

    <!-- Résultats des Tests -->
    <div v-if="results.length > 0" class="test-results">
      <h3>📊 Résultats des Tests</h3>
      <div v-for="(result, index) in results" :key="index" class="result-item">
        <div class="result-header">
          <span class="result-time">{{ result.time }}</span>
          <span :class="['result-status', result.success ? 'success' : 'error']">
            {{ result.success ? '✅ SUCCESS' : '❌ ERROR' }}
          </span>
        </div>
        <div class="result-message">{{ result.message }}</div>
        <pre v-if="result.data" class="result-data">{{ JSON.stringify(result.data, null, 2) }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
// IMPORT DU NOUVEAU SERVICE
import planningService, { type PlanningConfig } from '@/services/planningService'

// État de l'interface
const testing = ref(false)
const checking = ref(false)
const cleaning = ref(false)
const results = ref<any[]>([])

// Configuration par défaut
const config = ref<PlanningConfig>({
  orderStartDate: '2025-06-01',
  planningStartDate: new Date().toISOString().split('T')[0],
  maxEmployees: 4,
  timePerCard: 3,
  preventDuplicates: true,
  respectPriorities: true,
  balanceWorkload: true
})

// Fonction utilitaire pour ajouter un résultat
function addResult(message: string, success: boolean, data?: any) {
  results.value.unshift({
    time: new Date().toLocaleTimeString(),
    message,
    success,
    data
  })
}

// Test de génération de planification
async function testGeneration() {
  testing.value = true

  try {
    console.log('🧪 Test génération avec config:', config.value)

    const result = await planningService.generateOptimizedPlanning(config.value)

    if (result.success) {
      addResult(`Génération réussie: ${result.message}`, true, result.data)
    } else {
      addResult(`Génération échouée: ${result.message}`, false, result.error)
    }

  } catch (error: any) {
    console.error('❌ Erreur test génération:', error)
    addResult(`Erreur génération: ${error.message}`, false)
  } finally {
    testing.value = false
  }
}

// Test de vérification du statut
async function testStatus() {
  checking.value = true

  try {
    console.log('🧪 Test vérification statut')

    const result = await planningService.checkPlanningStatus()

    if (result.success) {
      addResult(`Statut récupéré: ${result.message}`, true, result.data)
    } else {
      addResult(`Échec statut: ${result.message}`, false, result.error)
    }

  } catch (error: any) {
    console.error('❌ Erreur test statut:', error)
    addResult(`Erreur statut: ${error.message}`, false)
  } finally {
    checking.value = false
  }
}

// Test de nettoyage des doublons
async function testCleanDuplicates() {
  cleaning.value = true

  try {
    console.log('🧪 Test nettoyage doublons')

    const result = await planningService.cleanDuplicatePlannings()

    if (result.success) {
      addResult(`Nettoyage réussi: ${result.message}`, true, result.data)
    } else {
      addResult(`Nettoyage échoué: ${result.message}`, false, result.error)
    }

  } catch (error: any) {
    console.error('❌ Erreur test nettoyage:', error)
    addResult(`Erreur nettoyage: ${error.message}`, false)
  } finally {
    cleaning.value = false
  }
}
</script>

<style scoped>
.planning-test {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.test-form {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.form-row {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

.form-row label {
  min-width: 150px;
  font-weight: 600;
}

.form-row input {
  padding: 8px;
  border: 1px solid #ced4da;
  border-radius: 4px;
}

.checkbox-row {
  margin-top: 15px;
}

.checkbox-row label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.test-buttons {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.btn-primary, .btn-info, .btn-warning {
  padding: 12px 20px;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-primary { background: #007bff; color: white; }
.btn-info { background: #17a2b8; color: white; }
.btn-warning { background: #ffc107; color: #212529; }

.btn-primary:hover { background: #0056b3; }
.btn-info:hover { background: #138496; }
.btn-warning:hover { background: #e0a800; }

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.test-results {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.result-item {
  border-bottom: 1px solid #e9ecef;
  padding: 15px 0;
}

.result-item:last-child {
  border-bottom: none;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.result-time {
  font-size: 14px;
  color: #6c757d;
}

.result-status.success {
  color: #28a745;
  font-weight: bold;
}

.result-status.error {
  color: #dc3545;
  font-weight: bold;
}

.result-message {
  margin-bottom: 10px;
  font-weight: 500;
}

.result-data {
  background: #f8f9fa;
  padding: 10px;
  border-radius: 4px;
  font-size: 12px;
  overflow-x: auto;
  border: 1px solid #e9ecef;
}
</style>
EOF