<template>
  <div class="planning-page">
    <!-- ✅ EN-TÊTE PRINCIPAL -->
    <div class="page-header">
      <div class="flex justify-between items-center">
        <div>
          <h1 class="page-title">📅 Planification des Commandes Pokemon</h1>
          <p class="page-subtitle">
            Gestion automatique de la planification des commandes de cartes Pokemon depuis juin 2025
          </p>
        </div>

        <!-- Actions principales -->
        <div class="action-buttons">
          <button
            @click="refreshData"
            :disabled="loading"
            class="btn btn-secondary"
          >
            <span v-if="loading">🔄</span>
            <span v-else>🔄</span>
            {{ loading ? 'Chargement...' : 'Actualiser' }}
          </button>

          <button
            @click="runOptimization"
            :disabled="optimizing"
            class="btn btn-primary"
          >
            <span v-if="optimizing">⚡</span>
            <span v-else>🚀</span>
            {{ optimizing ? 'Optimisation...' : 'Optimiser Planning' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ✅ STATISTIQUES DE PLANNING -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card blue">
          <div class="stat-icon">📦</div>
          <div class="stat-content">
            <h3>Commandes Totales</h3>
            <p class="stat-number">{{ stats.totalOrders || 0 }}</p>
            <span class="stat-label">Depuis juin 2025</span>
          </div>
        </div>

        <div class="stat-card green">
          <div class="stat-icon">✅</div>
          <div class="stat-content">
            <h3>Planifiées</h3>
            <p class="stat-number">{{ stats.plannedOrders || 0 }}</p>
            <span class="stat-label">Répartition effectuée</span>
          </div>
        </div>

        <div class="stat-card yellow">
          <div class="stat-icon">⏳</div>
          <div class="stat-content">
            <h3>En Attente</h3>
            <p class="stat-number">{{ stats.pendingOrders || 0 }}</p>
            <span class="stat-label">À planifier</span>
          </div>
        </div>

        <div class="stat-card purple">
          <div class="stat-icon">⚡</div>
          <div class="stat-content">
            <h3>Efficacité</h3>
            <p class="stat-number">{{ stats.efficiency || 0 }}%</p>
            <span class="stat-label">Performance globale</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ✅ OUTILS DE PLANIFICATION -->
    <div class="tools-section">
      <div class="tools-grid">

        <!-- Planification Automatique -->
        <div class="tool-card">
          <div class="tool-header">
            <h3>🤖 Planification Automatique</h3>
            <p>Répartition intelligente des commandes entre {{ stats.employeeCount || 'm' }} employés</p>
          </div>

          <div class="tool-content">
            <div class="setting-row">
              <label>Date de début :</label>
              <input
                type="date"
                v-model="config.startDate"
                class="form-input"
                :min="minDate"
              />
            </div>

            <div class="setting-row">
              <label>Temps par carte :</label>
              <div class="input-group">
                <input
                  type="number"
                  v-model="config.cardProcessingTime"
                  class="form-input"
                  min="1"
                  max="10"
                />
                <span class="input-suffix">minutes</span>
              </div>
            </div>

            <div class="setting-row">
              <label>Prioriser :</label>
              <select v-model="config.priorityMode" class="form-select">
                <option value="urgent">Commandes urgentes d'abord</option>
                <option value="date">Par date de commande</option>
                <option value="size">Petites commandes d'abord</option>
                <option value="balanced">Équilibrage de charge</option>
              </select>
            </div>
          </div>

          <div class="tool-actions">
            <button
              @click="generatePlanning"
              :disabled="generating"
              class="btn btn-primary full-width"
            >
              {{ generating ? '🔄 Génération...' : '🚀 Générer Planning' }}
            </button>
          </div>
        </div>

        <!-- Optimisation -->
        <div class="tool-card">
          <div class="tool-header">
            <h3>⚡ Optimisation de Performance</h3>
            <p>Amélioration de la répartition des charges de travail</p>
          </div>

          <div class="tool-content">
            <div class="performance-metrics">
              <div class="metric">
                <span class="metric-label">Charge max :</span>
                <span class="metric-value">{{ performance.maxLoad || 0 }}%</span>
              </div>
              <div class="metric">
                <span class="metric-label">Équilibrage :</span>
                <span class="metric-value">{{ performance.balance || 0 }}%</span>
              </div>
              <div class="metric">
                <span class="metric-label">Temps total :</span>
                <span class="metric-value">{{ performance.totalTime || 0 }}h</span>
              </div>
            </div>

            <div class="optimization-options">
              <label class="checkbox-label">
                <input type="checkbox" v-model="config.redistributeOverload" />
                Redistribuer les surcharges
              </label>
              <label class="checkbox-label">
                <input type="checkbox" v-model="config.respectPriorities" />
                Respecter les priorités
              </label>
              <label class="checkbox-label">
                <input type="checkbox" v-model="config.minimizeDowntime" />
                Minimiser les temps morts
              </label>
            </div>
          </div>

          <div class="tool-actions">
            <button
              @click="optimizePlanning"
              :disabled="optimizing"
              class="btn btn-success full-width"
            >
              {{ optimizing ? '⚡ Optimisation...' : '⚡ Optimiser' }}
            </button>
          </div>
        </div>

        <!-- Gestion des Données -->
        <div class="tool-card">
          <div class="tool-header">
            <h3>🔧 Gestion des Données</h3>
            <p>Nettoyage et maintenance de la base de planning</p>
          </div>

          <div class="tool-content">
            <div class="feature-list">
              <div class="feature-item">
                <span class="feature-icon">🧹</span>
                <span class="feature-text">Nettoyer les doublons</span>
              </div>
              <div class="feature-item">
                <span class="feature-icon">📊</span>
                <span class="feature-text">Recalculer les statistiques</span>
              </div>
              <div class="feature-item">
                <span class="feature-icon">🔄</span>
                <span class="feature-text">Réinitialiser le planning</span>
              </div>
              <div class="feature-item">
                <span class="feature-icon">📤</span>
                <span class="feature-text">Exporter les données</span>
              </div>
            </div>
          </div>

          <div class="tool-actions">
            <button
              @click="cleanupData"
              class="btn btn-warning full-width"
            >
              🧹 Nettoyer Base
            </button>
          </div>
        </div>

      </div>
    </div>

    <!-- ✅ RÉSULTATS DU PLANNING -->
    <div v-if="plannings.length > 0" class="results-section">
      <div class="section-header">
        <h2>📋 Planning Généré</h2>
        <p>{{ plannings.length }} planifications créées</p>
      </div>

      <div class="planning-table-container">
        <table class="planning-table">
          <thead>
          <tr>
            <th>Commande</th>
            <th>Employé</th>
            <th>Date</th>
            <th>Heure</th>
            <th>Durée</th>
            <th>Priorité</th>
            <th>Statut</th>
            <th>Actions</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="planning in plannings" :key="planning.id" class="planning-row">
            <td class="order-cell">
              <span class="order-number">{{ planning.orderNumber || 'N/A' }}</span>
            </td>
            <td class="employee-cell">
              <span class="employee-name">{{ planning.employeeName || 'Non assigné' }}</span>
            </td>
            <td class="date-cell">
              {{ formatDate(planning.scheduledDate) }}
            </td>
            <td class="time-cell">
              {{ planning.startTime }} - {{ planning.endTime }}
            </td>
            <td class="duration-cell">
              <span class="duration-badge">{{ planning.duration || 0 }}min</span>
            </td>
            <td class="priority-cell">
                <span :class="getPriorityClass(planning.priority)">
                  {{ planning.priority || 'MEDIUM' }}
                </span>
            </td>
            <td class="status-cell">
                <span :class="getStatusClass(planning.status)">
                  {{ getStatusLabel(planning.status) }}
                </span>
            </td>
            <td class="actions-cell">
              <button
                @click="editPlanning(planning)"
                class="btn-small btn-edit"
                title="Modifier"
              >
                ✏️
              </button>
              <button
                @click="deletePlanning(planning.id)"
                class="btn-small btn-delete"
                title="Supprimer"
              >
                🗑️
              </button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ✅ MESSAGE D'ÉTAT VIDE -->
    <div v-else-if="!loading" class="empty-state">
      <div class="empty-icon">📅</div>
      <h3>Aucun planning généré</h3>
      <p>Utilisez les outils ci-dessus pour créer une planification automatique des commandes.</p>
      <button @click="generatePlanning" class="btn btn-primary">
        🚀 Créer Premier Planning
      </button>
    </div>

    <!-- ✅ LOADING STATE -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>{{ loadingMessage || 'Chargement en cours...' }}</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Planning',
  data() {
    return {
      loading: false,
      generating: false,
      optimizing: false,
      loadingMessage: '',

      // Configuration
      config: {
        startDate: this.getNextBusinessDay(),
        cardProcessingTime: 3,
        priorityMode: 'urgent',
        redistributeOverload: true,
        respectPriorities: true,
        minimizeDowntime: false
      },

      // Données
      plannings: [],
      stats: {
        totalOrders: 0,
        plannedOrders: 0,
        pendingOrders: 0,
        efficiency: 0,
        employeeCount: 0
      },

      // Performance
      performance: {
        maxLoad: 0,
        balance: 0,
        totalTime: 0
      }
    }
  },

  computed: {
    minDate() {
      return new Date().toISOString().split('T')[0]
    }
  },

  mounted() {
    this.loadInitialData()
  },

  methods: {
    async loadInitialData() {
      this.loading = true
      this.loadingMessage = 'Chargement des données initiales...'

      try {
        await this.loadStats()
        await this.loadPlannings()
      } catch (error) {
        console.error('Erreur chargement initial:', error)
        this.$emit('show-notification', 'Erreur de chargement', 'error')
      } finally {
        this.loading = false
      }
    },

    async loadStats() {
      try {
        const response = await fetch('/api/pokemon-planning/stats')
        if (response.ok) {
          this.stats = await response.json()
        }
      } catch (error) {
        console.error('Erreur stats:', error)
      }
    },

    async loadPlannings() {
      try {
        const response = await fetch('/api/pokemon-planning/view')
        if (response.ok) {
          this.plannings = await response.json()
        }
      } catch (error) {
        console.error('Erreur plannings:', error)
      }
    },

    async refreshData() {
      await this.loadInitialData()
      this.$emit('show-notification', 'Données actualisées', 'success')
    },

    async generatePlanning() {
      this.generating = true
      this.loadingMessage = 'Génération du planning automatique...'

      try {
        const response = await fetch('/api/pokemon-planning/generate', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(this.config)
        })

        if (response.ok) {
          const result = await response.json()
          this.$emit('show-notification', `Planning généré: ${result.count} planifications`, 'success')
          await this.loadPlannings()
        } else {
          throw new Error('Erreur génération planning')
        }
      } catch (error) {
        console.error('Erreur génération:', error)
        this.$emit('show-notification', 'Erreur génération planning', 'error')
      } finally {
        this.generating = false
      }
    },

    async optimizePlanning() {
      this.optimizing = true
      this.loadingMessage = 'Optimisation du planning...'

      try {
        const response = await fetch('/api/pokemon-planning/optimize', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(this.config)
        })

        if (response.ok) {
          const result = await response.json()
          this.$emit('show-notification', 'Planning optimisé avec succès', 'success')
          await this.loadPlannings()
        }
      } catch (error) {
        console.error('Erreur optimisation:', error)
        this.$emit('show-notification', 'Erreur optimisation', 'error')
      } finally {
        this.optimizing = false
      }
    },

    async cleanupData() {
      if (!confirm('Nettoyer les données de planning ?')) return

      try {
        const response = await fetch('/api/pokemon-planning/cleanup', {
          method: 'DELETE'
        })

        if (response.ok) {
          this.$emit('show-notification', 'Nettoyage effectué', 'success')
          await this.loadPlannings()
        }
      } catch (error) {
        console.error('Erreur nettoyage:', error)
        this.$emit('show-notification', 'Erreur nettoyage', 'error')
      }
    },

    // Méthodes utilitaires
    getNextBusinessDay() {
      const date = new Date()
      const day = date.getDay()
      if (day === 6) date.setDate(date.getDate() + 2) // Samedi -> Lundi
      else if (day === 0) date.setDate(date.getDate() + 1) // Dimanche -> Lundi
      return date.toISOString().split('T')[0]
    },

    formatDate(dateStr) {
      if (!dateStr) return 'N/A'
      try {
        return new Date(dateStr).toLocaleDateString('fr-FR')
      } catch {
        return dateStr
      }
    },

    getPriorityClass(priority) {
      const classes = {
        'URGENT': 'priority-urgent',
        'HIGH': 'priority-high',
        'MEDIUM': 'priority-medium',
        'LOW': 'priority-low'
      }
      return classes[priority] || 'priority-medium'
    },

    getStatusClass(status) {
      const classes = {
        'SCHEDULED': 'status-scheduled',
        'IN_PROGRESS': 'status-progress',
        'COMPLETED': 'status-completed',
        'CANCELLED': 'status-cancelled'
      }
      return classes[status] || 'status-scheduled'
    },

    getStatusLabel(status) {
      const labels = {
        'SCHEDULED': 'Planifié',
        'IN_PROGRESS': 'En cours',
        'COMPLETED': 'Terminé',
        'CANCELLED': 'Annulé'
      }
      return labels[status] || 'Planifié'
    },

    editPlanning(planning) {
      // À implémenter
      console.log('Edit planning:', planning)
    },

    deletePlanning(id) {
      if (!confirm('Supprimer cette planification ?')) return
      // À implémenter
      console.log('Delete planning:', id)
    }
  }
}
</script>

<style scoped>
/* =============================================== */
/* STYLES CORRIGÉS POUR LA PAGE PLANNING          */
/* =============================================== */

.planning-page {
  min-height: 100vh;
  background: #f8fafc;
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

/* ✅ EN-TÊTE */
.page-header {
  background: white;
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1a202c;
  margin-bottom: 8px;
}

.page-subtitle {
  color: #64748b;
  font-size: 1rem;
  line-height: 1.5;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

/* ✅ BOUTONS */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  text-decoration: none;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2563eb;
}

.btn-secondary {
  background: #f1f5f9;
  color: #475569;
}

.btn-secondary:hover:not(:disabled) {
  background: #e2e8f0;
}

.btn-success {
  background: #10b981;
  color: white;
}

.btn-success:hover:not(:disabled) {
  background: #059669;
}

.btn-warning {
  background: #f59e0b;
  color: white;
}

.btn-warning:hover:not(:disabled) {
  background: #d97706;
}

.full-width {
  width: 100%;
}

/* ✅ STATISTIQUES */
.stats-section {
  margin-bottom: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border-left: 4px solid #e2e8f0;
}

.stat-card.blue { border-left-color: #3b82f6; }
.stat-card.green { border-left-color: #10b981; }
.stat-card.yellow { border-left-color: #f59e0b; }
.stat-card.purple { border-left-color: #8b5cf6; }

.stat-icon {
  font-size: 2rem;
}

.stat-content h3 {
  font-size: 0.875rem;
  color: #64748b;
  margin: 0 0 4px 0;
  font-weight: 500;
}

.stat-number {
  font-size: 2rem;
  font-weight: 700;
  color: #1a202c;
  margin: 0 0 4px 0;
}

.stat-label {
  font-size: 0.75rem;
  color: #94a3b8;
}

/* ✅ OUTILS */
.tools-section {
  margin-bottom: 24px;
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 24px;
}

.tool-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.tool-header {
  padding: 24px 24px 16px 24px;
  border-bottom: 1px solid #e2e8f0;
}

.tool-header h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 8px 0;
}

.tool-header p {
  color: #64748b;
  font-size: 0.875rem;
  margin: 0;
}

.tool-content {
  padding: 24px;
}

.tool-actions {
  padding: 0 24px 24px 24px;
}

/* ✅ FORMULAIRES */
.setting-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.setting-row label {
  min-width: 120px;
  font-weight: 500;
  color: #374151;
}

.form-input, .form-select {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.875rem;
}

.input-group {
  display: flex;
  align-items: center;
  flex: 1;
}

.input-suffix {
  margin-left: 8px;
  color: #6b7280;
  font-size: 0.875rem;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 0.875rem;
  color: #374151;
  cursor: pointer;
}

/* ✅ MÉTRIQUES */
.performance-metrics {
  background: #f8fafc;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.metric {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.metric:last-child {
  margin-bottom: 0;
}

.metric-label {
  color: #64748b;
  font-size: 0.875rem;
}

.metric-value {
  font-weight: 600;
  color: #1a202c;
}

/* ✅ FEATURES */
.feature-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.feature-icon {
  font-size: 1.25rem;
}

.feature-text {
  color: #374151;
  font-size: 0.875rem;
}

/* ✅ TABLEAU */
.results-section {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.section-header {
  padding: 24px;
  border-bottom: 1px solid #e2e8f0;
}

.section-header h2 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 4px 0;
}

.section-header p {
  color: #64748b;
  margin: 0;
}

.planning-table-container {
  overflow-x: auto;
}

.planning-table {
  width: 100%;
  border-collapse: collapse;
}

.planning-table th {
  background: #f8fafc;
  padding: 12px;
  text-align: left;
  font-weight: 600;
  color: #374151;
  font-size: 0.875rem;
  border-bottom: 1px solid #e2e8f0;
}

.planning-table td {
  padding: 12px;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
}

.planning-row:hover {
  background: #f8fafc;
}

/* Cellules spécifiques */
.order-number {
  font-family: monospace;
  font-weight: 600;
  color: #3b82f6;
}

.employee-name {
  font-weight: 500;
  color: #1a202c;
}

.duration-badge {
  background: #e0e7ff;
  color: #3730a3;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

/* ✅ PRIORITÉS */
.priority-urgent { color: #dc2626; font-weight: 600; }
.priority-high { color: #ea580c; font-weight: 600; }
.priority-medium { color: #059669; font-weight: 600; }
.priority-low { color: #64748b; font-weight: 600; }

/* ✅ STATUTS */
.status-scheduled { color: #3b82f6; }
.status-progress { color: #f59e0b; }
.status-completed { color: #10b981; }
.status-cancelled { color: #ef4444; }

/* ✅ BOUTONS PETITS */
.btn-small {
  padding: 4px 8px;
  font-size: 0.75rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 4px;
}

.btn-edit {
  background: #e0e7ff;
  color: #3730a3;
}

.btn-delete {
  background: #fee2e2;
  color: #dc2626;
}

/* ✅ ÉTATS VIDES ET LOADING */
.empty-state {
  text-align: center;
  padding: 64px 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 1.25rem;
  color: #374151;
  margin-bottom: 8px;
}

.empty-state p {
  color: #64748b;
  margin-bottom: 24px;
}

.loading-state {
  text-align: center;
  padding: 64px 24px;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #e2e8f0;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px auto;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* ✅ RESPONSIVE */
@media (max-width: 768px) {
  .planning-page {
    padding: 16px;
    max-width: 100%;
  }

  .page-header {
    padding: 24px;
  }

  .page-header .flex {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .tools-grid {
    grid-template-columns: 1fr;
  }

  .setting-row {
    flex-direction: column;
    align-items: stretch;
  }

  .setting-row label {
    min-width: auto;
    margin-bottom: 4px;
  }

  .planning-table-container {
    font-size: 0.875rem;
  }
}
</style>
