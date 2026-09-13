import { useState } from 'react'
import { authService } from '../services/authService'
import './Auth.css'

export default function Register({ onRegisterSuccess }) {
  const handleBackToLogin = (e) => {
    e.preventDefault()
    window.dispatchEvent(new CustomEvent('showLogin'))
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    // Registration is managed through Keycloak - do nothing
  }

  const handleKeycloakAdminClick = () => {
    window.open('http://localhost:8090/admin/master/console/#/realms/finki-services/users', '_blank')
  }

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <h1>🌱 PlantPulse</h1>
          <p>Account Registration</p>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="registration-info">
            <div className="info-icon">ℹ️</div>
            <div className="info-content">
              <h3>Registration via Keycloak</h3>
              <p>User accounts are managed through Keycloak Identity Provider.</p>
              <p>To create a new user account, please contact your administrator or use the Keycloak admin console.</p>
              <button
                type="button"
                className="auth-button secondary"
                onClick={handleKeycloakAdminClick}
              >
                Open Keycloak Admin Console
              </button>
            </div>
          </div>

          <button
            type="button"
            className="auth-button"
            onClick={handleBackToLogin}
          >
            Back to Login
          </button>
        </form>

        <div className="demo-credentials">
          <p className="demo-label">Try these demo accounts:</p>
          <code>ben / benpassword</code>
          <code>bob / bobspassword</code>
          <code>test / testpassword</code>
        </div>
      </div>
    </div>
  )
}