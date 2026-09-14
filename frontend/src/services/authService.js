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

// Logout helper
const performLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('userEmail')
}

// Refresh token via Plant Service
const refreshToken = async (email) => {
  try {
    console.log('🔄 Refreshing JWT token...')
    // Note: Token refresh typically not needed for JWT since they don't expire during session
    // Just return the current token - in production, implement token refresh with refresh_token
    const currentToken = localStorage.getItem('token')
    if (currentToken && !isTokenExpired(currentToken)) {
      return currentToken
    }

    // If token is expired, user needs to login again
    throw new Error('Session expired - please login again')
  } catch (error) {
    console.error('❌ Token refresh failed:', error)
    // Clear auth on refresh failure
    performLogout()
    throw error
  }
}

export const authService = {
  // Login user with JWT (Plant Service)
  login: async (email, password) => {
    try {
      console.log('🔐 Attempting login for:', email)
      const response = await fetch(`${API_BASE}/api/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: email,
          password: password
        })
      })

      console.log('📡 Login response status:', response.status)

      const data = await response.json()
      console.log('📦 Login response data:', data)

      if (!response.ok) {
        const errorMsg = data.error || data.message || 'Login failed'
        console.error('❌ Login failed with status', response.status, ':', errorMsg)
        throw new Error(errorMsg)
      }

      // Extract user ID from JWT's 'sub' claim
      const decoded = decodeJWT(data.token)
      const userId = decoded?.userId || data.userId

      if (!userId) {
        console.error('❌ No userId found in token:', decoded)
        throw new Error('Failed to extract user ID from token')
      }

      // Store token and user info
      localStorage.setItem('token', data.token)
      localStorage.setItem('userId', userId)
      localStorage.setItem('userEmail', data.email || email)

      console.log('✅ Login successful for:', email)
      return {
        token: data.token,
        userId: userId,
        userEmail: data.email || email
      }
    } catch (error) {
      console.error('❌ Login error:', error.message)
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
  },

  // Get time until token expires (in seconds)
  getTokenExpiryIn: () => {
    const token = localStorage.getItem('token')
    if (!token) return 0

    const decoded = decodeJWT(token)
    if (!decoded || !decoded.exp) return 0

    const expiresAt = decoded.exp * 1000 // convert to milliseconds
    const secondsRemaining = Math.round((expiresAt - Date.now()) / 1000)
    return Math.max(0, secondsRemaining)
  }
}