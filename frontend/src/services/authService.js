const API_BASE = 'http://localhost:8081'

// Helper function to decode JWT and extract claims
const decodeJWT = (token) => {
  try {
    const parts = token.split('.')
    if (parts.length !== 3) throw new Error('Invalid token format')

    const decoded = JSON.parse(atob(parts[1]))
    return decoded
  } catch (error) {
    console.error('Failed to decode JWT:', error)
    return null
  }
}

export const authService = {
  // Login user with Keycloak OAuth2 (via Plant Service proxy)
  login: async (username, password) => {
    try {
      const response = await fetch(`${API_BASE}/api/auth/keycloak-login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: username,
          password: password
        })
      })

      const data = await response.json()

      if (!response.ok) {
        throw new Error(data.error_description || data.error || 'Login failed')
      }

      // Extract user ID from JWT's 'sub' claim
      const decoded = decodeJWT(data.access_token)
      const userId = decoded?.sub

      if (!userId) {
        throw new Error('Failed to extract user ID from token')
      }

      // Store token and user info
      localStorage.setItem('token', data.access_token)
      localStorage.setItem('userId', userId)
      localStorage.setItem('userEmail', username)

      return {
        token: data.access_token,
        userId: userId,
        userEmail: username
      }
    } catch (error) {
      console.error('Login error:', error)
      throw error
    }
  },

  // Register new user (placeholder - Keycloak handles registration)
  register: async (username, password) => {
    throw new Error('Registration must be done through Keycloak admin console or a separate registration endpoint')
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