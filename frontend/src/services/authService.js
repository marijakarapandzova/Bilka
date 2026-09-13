const API_BASE = 'http://localhost:8081'

export const authService = {
  // Register new user
  register: async (email, password) => {
    try {
      const response = await fetch(`${API_BASE}/api/auth/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password })
      })

      const data = await response.json()

      if (!response.ok) {
        throw new Error(data.error || data.message || 'Registration failed')
      }

      // Store token
      if (data.token) {
        localStorage.setItem('token', data.token)
        localStorage.setItem('userId', data.userId)
        localStorage.setItem('userEmail', email)
      }

      return data
    } catch (error) {
      console.error('Registration error:', error)
      throw error
    }
  },

  // Login user
  login: async (email, password) => {
    try {
      const response = await fetch(`${API_BASE}/api/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password })
      })

      const data = await response.json()

      if (!response.ok) {
        throw new Error(data.error || data.message || 'Login failed')
      }

      // Store token
      if (data.token) {
        localStorage.setItem('token', data.token)
        localStorage.setItem('userId', data.userId)
        localStorage.setItem('userEmail', email)
      }

      return data
    } catch (error) {
      console.error('Login error:', error)
      throw error
    }
  },

  // Logout user
  logout: () => {
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('userEmail')
  },

  // Get stored token
  getToken: () => {
    return localStorage.getItem('token')
  },

  // Check if user is authenticated
  isAuthenticated: () => {
    return !!localStorage.getItem('token')
  },

  // Get user info
  getUserEmail: () => {
    return localStorage.getItem('userEmail')
  },

  getUserId: () => {
    return localStorage.getItem('userId')
  }
}