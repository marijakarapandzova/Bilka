import { useState } from 'react'
import { authService } from '../services/authService'
import './Auth.css'

export default function Login({ onLoginSuccess }) {
  const [email, setEmail] = useState('')
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
      if (!email || !password) {
        setError('Please enter both email and password')
        setLoading(false)
        return
      }

      if (!email.includes('@')) {
        setError('Please enter a valid email address')
        setLoading(false)
        return
      }

      console.log('Attempting login with:', email)

      // Call login API
      const result = await authService.login(email, password)

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
          <h1>🌱 PlantPulse</h1>
          <p>Welcome Back</p>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          {error && (
            <div className="auth-error">
              <span>⚠️ {error}</span>
            </div>
          )}

          <div className="form-group">
            <label htmlFor="email">Email Address</label>
            <input
              id="email"
              type="email"
              placeholder="your@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disabled={loading}
              autoComplete="email"
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
                {showPassword ? '👁️' : '👁️‍🗨️'}
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
          <p className="demo-label">Demo Credentials:</p>
          <code>Email: ben@example.com</code>
          <code>Password: benspassword</code>
        </div>
      </div>
    </div>
  )
}