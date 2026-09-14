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

// Check if token is expired (expires in next 60 seconds)
const isTokenExpired = (token) => {
  const decoded = decodeJWT(token)
  if (!decoded || !decoded.exp) return true

  const expiresAt = decoded.exp * 1000 // convert to milliseconds
  return Date.now() >= expiresAt - 60000 // refresh 60s before expiry
}

// Refresh token via Plant Service proxy
const refreshToken = async (username) => {
  try {
    console.log('🔄 Refreshing Keycloak token...')
    const password = localStorage.getItem('userPassword')

    if (!password) {
      throw new Error('Password not stored - please log in again')
    }

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
      throw new Error(data.error_description || 'Token refresh failed')
    }

    // Extract user ID from JWT's 'sub' claim
    const decoded = decodeJWT(data.access_token)
    const userId = decoded?.sub

    if (!userId) {
      throw new Error('Failed to extract user ID from token')
    }

    // Store new token
    localStorage.setItem('token', data.access_token)
    localStorage.setItem('userId', userId)

    console.log('✅ Token refreshed successfully')
    return data.access_token
  } catch (error) {
    console.error('❌ Token refresh failed:', error)
    // Clear auth on refresh failure
    authService.logout()
    throw error
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

      // Store token, user info, and password for refresh
      localStorage.setItem('token', data.access_token)
      localStorage.setItem('userId', userId)
      localStorage.setItem('userEmail', username)
      localStorage.setItem('userPassword', password)

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
    localStorage.removeItem('userPassword')
  },

  // Get stored token (refreshes if expired)
  getToken: async () => {
    let token = localStorage.getItem('token')

    if (!token) return null

    // Check if token is expired or about to expire
    if (isTokenExpired(token)) {
      try {
        const username = localStorage.getItem('userEmail')
        token = await refreshToken(username)
      } catch (error) {
        console.error('Failed to refresh token:', error)
        return null
      }
    }

    return token
  },

  // Check if user is authenticated
  isAuthenticated: () => {
    const token = localStorage.getItem('token')
    return !!token && !isTokenExpired(token)
  },

  // Get user info
  getUserEmail: () => {
    return localStorage.getItem('userEmail')
  },

  getUserId: () => {
    return localStorage.getItem('userId')
  }
}