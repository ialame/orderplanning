<template>
  <div class="simple-test">
    <div class="bg-white rounded-lg shadow p-6 mb-6">
      <h2 class="text-xl font-bold text-gray-900 mb-4">🧪 Simple Test - /api/orders</h2>

      <button
        @click="testOrdersEndpoint"
        :disabled="testing"
        class="bg-green-600 text-white px-6 py-3 rounded-lg hover:bg-green-700 disabled:opacity-50"
      >
        {{ testing ? '🔄 Testing...' : '🧪 Test /api/orders Endpoint' }}
      </button>
    </div>

    <div v-if="result" class="bg-white rounded-lg shadow p-6">
      <h3 class="text-lg font-semibold mb-4">📊 Results from /api/orders</h3>

      <div class="mb-4">
        <p><strong>Total Orders:</strong> {{ result.totalCount }}</p>
        <p><strong>Data Type:</strong> {{ result.dataType }}</p>
        <p><strong>Is Array:</strong> {{ result.isArray ? 'Yes' : 'No' }}</p>
      </div>

      <div v-if="result.sampleOrders" class="space-y-4">
        <h4 class="font-semibold">📋 Sample Orders (First 3):</h4>

        <div
          v-for="(order, index) in result.sampleOrders.slice(0, 3)"
          :key="index"
          class="bg-gray-50 p-4 rounded border"
        >
          <h5 class="font-medium mb-2">Order {{ index + 1 }}:</h5>
          <div class="grid grid-cols-2 gap-2 text-sm">
            <p><strong>ID:</strong> {{ order.id }}</p>
            <p><strong>Order Number:</strong> {{ order.orderNumber }}</p>
            <p><strong>Creation Date:</strong> {{ order.creationDate || 'null' }}</p>
            <p><strong>Order Date:</strong> {{ order.orderDate || 'null' }}</p>
            <p><strong>Deadline:</strong> {{ order.deadline || 'null' }}</p>
            <p><strong>Cards:</strong> {{ order.cardCount }}</p>
            <p><strong>Priority:</strong> {{ order.priority }}</p>
            <p><strong>Status:</strong> {{ order.statusText }}</p>
          </div>

          <details class="mt-2">
            <summary class="cursor-pointer text-blue-600 text-sm">Show All Fields</summary>
            <pre class="text-xs bg-white p-2 rounded mt-2 overflow-x-auto">{{ JSON.stringify(order, null, 2) }}</pre>
          </details>
        </div>
      </div>

      <div v-if="result.dateAnalysis" class="mt-6">
        <h4 class="font-semibold mb-2">📅 Date Analysis:</h4>
        <div class="bg-blue-50 p-3 rounded">
          <p><strong>Orders with Creation Date:</strong> {{ result.dateAnalysis.withCreationDate }}</p>
          <p><strong>Orders with Order Date:</strong> {{ result.dateAnalysis.withOrderDate }}</p>
          <p><strong>Orders with Deadline:</strong> {{ result.dateAnalysis.withDeadline }}</p>
          <p><strong>Date Range:</strong> {{ result.dateAnalysis.dateRange }}</p>
        </div>
      </div>

      <div v-if="result.june2025Analysis" class="mt-4">
        <h4 class="font-semibold mb-2">🗓️ June 2025 Analysis:</h4>
        <div class="bg-green-50 p-3 rounded">
          <p><strong>Orders from June 2025:</strong> {{ result.june2025Analysis.june2025Count }}</p>
          <p><strong>Orders before June 2025:</strong> {{ result.june2025Analysis.beforeJune2025Count }}</p>
          <p><strong>Orders after June 2025:</strong> {{ result.june2025Analysis.afterJune2025Count }}</p>
        </div>
      </div>

      <div class="mt-6 p-4 bg-yellow-50 border border-yellow-200 rounded">
        <h4 class="font-semibold text-yellow-800 mb-2">🎯 Recommendation:</h4>
        <p class="text-yellow-700">
          Use <code class="bg-white px-2 py-1 rounded">/api/orders</code> endpoint - it works perfectly!
          {{ result.recommendation }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const testing = ref(false)
const result = ref(null)

const testOrdersEndpoint = async () => {
  testing.value = true
  result.value = null

  try {
    console.log('🧪 Testing /api/orders endpoint...')

    const response = await fetch('http://localhost:8080/api/orders')

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    }

    const data = await response.json()
    console.log('📊 Raw data:', data)

    // Analyze the data
    const analysis = {
      totalCount: Array.isArray(data) ? data.length : 0,
      dataType: typeof data,
      isArray: Array.isArray(data),
      sampleOrders: Array.isArray(data) ? data.slice(0, 3) : [],
      dateAnalysis: null,
      june2025Analysis: null,
      recommendation: ''
    }

    if (Array.isArray(data) && data.length > 0) {
      // Date analysis
      const withCreationDate = data.filter(order => order.creationDate).length
      const withOrderDate = data.filter(order => order.orderDate).length
      const withDeadline = data.filter(order => order.deadline).length

      // Get all dates to find range
      const allDates = []
      data.forEach(order => {
        if (order.creationDate) allDates.push(order.creationDate)
        if (order.orderDate) allDates.push(order.orderDate)
        if (order.deadline) allDates.push(order.deadline)
      })

      analysis.dateAnalysis = {
        withCreationDate,
        withOrderDate,
        withDeadline,
        dateRange: allDates.length > 0 ?
          `${Math.min(...allDates)} to ${Math.max(...allDates)}` :
          'No dates found'
      }

      // June 2025 analysis
      const june2025Count = data.filter(order => {
        const dates = [order.creationDate, order.orderDate].filter(d => d)
        return dates.some(date => date && date.includes('2025-06'))
      }).length

      const beforeJune2025Count = data.filter(order => {
        const dates = [order.creationDate, order.orderDate].filter(d => d)
        return dates.some(date => date && date < '2025-06-01')
      }).length

      const afterJune2025Count = data.filter(order => {
        const dates = [order.creationDate, order.orderDate].filter(d => d)
        return dates.some(date => date && date > '2025-06-30')
      }).length

      analysis.june2025Analysis = {
        june2025Count,
        beforeJune2025Count,
        afterJune2025Count
      }

      // Generate recommendation
      if (june2025Count > 0) {
        analysis.recommendation = `Found ${june2025Count} orders from June 2025. This endpoint contains real data!`
      } else if (beforeJune2025Count > 0 || afterJune2025Count > 0) {
        analysis.recommendation = 'This endpoint contains real data but from different dates. Filter client-side for June 2025.'
      } else {
        analysis.recommendation = 'This endpoint contains real order data. Use it with client-side filtering.'
      }
    }

    result.value = analysis

  } catch (error) {
    console.error('❌ Test failed:', error)
    result.value = {
      error: error.message,
      totalCount: 0,
      dataType: 'error',
      isArray: false
    }
  } finally {
    testing.value = false
  }
}
</script>

<style scoped>
.simple-test {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

code {
  font-family: 'Courier New', monospace;
  background: #f3f4f6;
  padding: 2px 4px;
  border-radius: 4px;
}

pre {
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
