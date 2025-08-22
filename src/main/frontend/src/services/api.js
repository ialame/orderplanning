const API_BASE_URL = 'http://localhost:8080'

class ApiService {
  async request(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`
    const config = {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    }

    try {
      const response = await fetch(url, config)
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      return await response.json()
    } catch (error) {
      console.error('API request failed:', error)
      throw error
    }
  }

  // Employee methods
  async getEmployees() {
    return this.request('/api/employees')
  }

  async createEmployee(employee) {
    return this.request('/api/employees', {
      method: 'POST',
      body: JSON.stringify(employee),
    })
  }

  // Order methods
  async getOrders() {
    return this.request('/api/orders')
  }

  async createOrder(order) {
    return this.request('/api/orders', {
      method: 'POST',
      body: JSON.stringify(order),
    })
  }

  // Planning methods
  async getPlanning() {
    return this.request('/api/planning')
  }

  async generatePlanning() {
    return this.request('/api/planning/generate', {
      method: 'POST',
    })
  }

  // Health check
  async healthCheck() {
    try {
      const response = await fetch(`${API_BASE_URL}/actuator/health`)
      return response.ok
    } catch {
      return false
    }
  }
}

export default new ApiService()
