import { useState } from 'react'
import { authService } from '../services/authService'
import './Auth.css'

export default function Login({ onLoginSuccess }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [showPassword, setShowPassword] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      // Validate inputs
      if (!username || !password) {
        setError('Please enter both username and password')
        setLoading(false)
        return
      }

      console.log('Attempting Keycloak login with:', username)

      // Call Keycloak login
      const result = await authService.login(username, password)

      console.log('Login successful:', result)

      // Give it a moment to ensure token is stored
      setTimeout(() => {
        onLoginSuccess()
      }, 100)
    } catch (err) {
      console.error('Login failed:', err)
      setError(err.message || 'Login failed. Please try again.')
      setLoading(false)
    }
  }

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <h1>Bilka</h1>
          <p>Welcome Back</p>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          {error && (
            <div className="auth-error">
              <span>{error}</span>
            </div>
          )}

          <div className="form-group">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              type="text"
              placeholder="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={loading}
              autoComplete="username"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <div className="password-input-wrapper">
              <input
                id="password"
                type={showPassword ? 'text' : 'password'}
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={loading}
                autoComplete="current-password"
                required
              />
              <button
                type="button"
                className="toggle-password"
                onClick={() => setShowPassword(!showPassword)}
                disabled={loading}
              >
                {showPassword ? 'Hide' : 'Show'}
              </button>
            </div>
          </div>

          <button
            type="submit"
            className="auth-button"
            disabled={loading}
          >
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        <div className="auth-footer">
          <p>Don't have an account? <a href="#" onClick={(e) => {
            e.preventDefault()
            // This will be handled by parent component
            window.dispatchEvent(new CustomEvent('showRegister'))
          }}>Create one</a></p>
        </div>

        <div className="demo-credentials">
          <p className="demo-label">Demo Credentials (Keycloak):</p>
          <code>ben / benpassword</code>
          <code>bob / bobspassword</code>
          <code>test / testpassword</code>
        </div>
      </div>
    </div>
  )
}