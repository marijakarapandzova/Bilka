import { useState, useEffect } from 'react'
import './App.css'
import { authService } from './services/authService'
import Login from './pages/Login'
import Register from './pages/Register'
import Navigation from './components/Navigation'
import Hero from './components/Hero'
import Alert from './components/Alert'
import PlantGrid from './components/PlantGrid'
import AddPlantModal from './components/AddPlantModal'
import ObservationModal from './components/ObservationModal'
import PlantDetailsModal from './components/PlantDetailsModal'
import SharePlantModal from './components/SharePlantModal'
import NotificationsPanel from './components/NotificationsPanel'
import CommunityMap from './pages/CommunityMap'
import Observations from './pages/Observations'

function App() {
  // Auth state
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [showRegister, setShowRegister] = useState(false)
  const [authLoading, setAuthLoading] = useState(true)

  // App state
  const [plants, setPlants] = useState([])
  const [speciesMap, setSpeciesMap] = useState({})
  const [loading, setLoading] = useState(true)
  const [activeTab, setActiveTab] = useState('all')
  const [showAddModal, setShowAddModal] = useState(false)
  const [showObservationModal, setShowObservationModal] = useState(false)
  const [selectedPlant, setSelectedPlant] = useState(null)
  const [showDetailsModal, setShowDetailsModal] = useState(false)
  const [detailsPlant, setDetailsPlant] = useState(null)
  const [showShareModal, setShowShareModal] = useState(false)
  const [plantToShare, setPlantToShare] = useState(null)
  const [error, setError] = useState(null)

  // Check authentication on mount
  useEffect(() => {
    const checkAuth = () => {
      const authenticated = authService.isAuthenticated()
      console.log('Auth check - Authenticated:', authenticated)
      setIsAuthenticated(authenticated)
      setAuthLoading(false)

      // If authenticated, load data
      if (authenticated) {
        fetchSpecies()
        fetchPlants()
      }
    }

    checkAuth()

    // Listen for auth changes from other pages
    window.addEventListener('showLogin', () => {
      setShowRegister(false)
    })
    window.addEventListener('showRegister', () => {
      setShowRegister(true)
    })

    return () => {
      window.removeEventListener('showLogin', () => {})
      window.removeEventListener('showRegister', () => {})
    }
  }, [])

  const getAuthHeaders = () => {
    const token = authService.getToken()
    return {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  }

  const fetchSpecies = async () => {
    try {
      console.log('Fetching species...')
      const response = await fetch('http://localhost:8081/api/species', {
        headers: getAuthHeaders()
      })

      console.log('Species response status:', response.status)

      if (response.status === 401) {
        console.error('Unauthorized - logging out')
        handleLogout()
        return
      }

      if (response.ok) {
        const data = await response.json()
        console.log('✅ Fetched species:', data.length)
        const map = {}
        data.forEach(s => {
          map[s.id] = s
        })
        setSpeciesMap(map)
      } else {
        console.error('Species fetch failed:', response.status)
        setError(`Failed to fetch species: ${response.status}`)
      }
    } catch (error) {
      console.error('Failed to fetch species:', error)
      setError(`Error fetching species: ${error.message}`)
    }
  }

  const fetchPlants = async () => {
    try {
      setLoading(true)
      setError(null)
      console.log('Fetching plants...')

      const response = await fetch('http://localhost:8081/api/plants', {
        headers: getAuthHeaders()
      })

      console.log('Plants response status:', response.status)

      if (response.status === 401) {
        console.error('Unauthorized - logging out')
        handleLogout()
        return
      }

      if (response.ok) {
        const data = await response.json()
        console.log('✅ Fetched plants:', data.length)

        // Enrich plants with health data from Health Service
        const enrichedPlants = await Promise.all(data.map(async (plant) => {
          const spec = speciesMap[plant.speciesId]
          let health = 75

          try {
            const healthResponse = await fetch(`http://localhost:8082/api/health/plants/${plant.id}`, {
              headers: getAuthHeaders()
            })

            if (healthResponse.ok) {
              const healthData = await healthResponse.json()
              console.log(`✅ Health data synced for ${plant.nickname}:`, healthData.healthScore)
              health = healthData.healthScore || 75
            } else if (healthResponse.status === 404) {
              console.log(`ℹ️ Plant ${plant.nickname} health: using default 75`)
              health = 75
            } else if (healthResponse.status === 401) {
              console.error('Unauthorized health service access')
              health = 75
            }
          } catch (err) {
            console.error(`Failed to fetch health for plant ${plant.id}:`, err)
            health = 75
          }

          return {
            ...plant,
            speciesName: spec?.name || 'Unknown',
            health: health,
            wateringDaysLeft: plant.wateringFrequencyDays || 7,
            notes: 'Sample observation data'
          }
        }))

        setPlants(enrichedPlants)
      } else if (response.status === 401) {
        setError('Session expired. Please log in again.')
      } else {
        setError(`Failed to fetch plants: ${response.status}`)
      }
    } catch (error) {
      console.error('Failed to fetch plants:', error)
      setError(`Error fetching plants: ${error.message}`)
    } finally {
      setLoading(false)
    }
  }

  const handleLoginSuccess = () => {
    console.log('Login successful!')
    setIsAuthenticated(true)
    // Fetch data after login
    fetchSpecies()
    fetchPlants()
  }

  const handleRegisterSuccess = () => {
    console.log('Registration successful!')
    setIsAuthenticated(true)
    setShowRegister(false)
    // Fetch data after registration
    fetchSpecies()
    fetchPlants()
  }

  const handleLogout = () => {
    console.log('Logging out...')
    authService.logout()
    setIsAuthenticated(false)
    setPlants([])
    setSpeciesMap({})
    setError(null)
  }

  // Filter plants based on active tab
  const getFilteredPlants = () => {
    switch (activeTab) {
      case 'watering':
        return plants
          .filter(plant => plant.wateringDaysLeft <= 3)
          .sort((a, b) => a.wateringDaysLeft - b.wateringDaysLeft)

      case 'attention':
        return plants
          .filter(plant => plant.health < 70)
          .sort((a, b) => a.health - b.health)

      case 'community':
        return plants

      case 'all':
      default:
        return [...plants].sort((a, b) => {
          if (a.health !== b.health) return a.health - b.health
          return a.wateringDaysLeft - b.wateringDaysLeft
        })
    }
  }

  const filteredPlants = getFilteredPlants()

  const getEmptyMessage = () => {
    switch (activeTab) {
      case 'watering':
        return '✓ No plants need watering in the next 3 days'
      case 'attention':
        return '✓ All plants are healthy!'
      default:
        return 'No plants yet. Click "Add Plant" to get started!'
    }
  }

  const handleLogObservation = (plant) => {
    setSelectedPlant(plant)
    setShowObservationModal(true)
  }

  const handleShowDetails = (plant) => {
    setDetailsPlant(plant)
    setShowDetailsModal(true)
  }

  const handleShare = (plant) => {
    setPlantToShare(plant)
    setShowShareModal(true)
  }

  const handleWaterPlant = async (plant) => {
    try {
      console.log('Watering plant:', plant.nickname)
      const response = await fetch(`http://localhost:8081/api/plants/${plant.id}/water`, {
        method: 'POST',
        headers: getAuthHeaders()
      })

      console.log('Water response status:', response.status)

      if (response.status === 401) {
        handleLogout()
        return
      }

      if (response.ok) {
        console.log('✅ Plant watered successfully')
        fetchPlants()
      } else {
        const errorData = await response.text()
        console.error('Water log failed:', response.status, errorData)
        setError(`Failed to log watering: ${response.status}`)
      }
    } catch (error) {
      console.error('Failed to log watering:', error)
      setError(`Error logging watering: ${error.message}`)
    }
  }

  const handleDeletePlant = async (plantId) => {
    if (!window.confirm('Are you sure you want to delete this plant?')) {
      return
    }
    try {
      const response = await fetch(`http://localhost:8081/api/plants/${plantId}`, {
        method: 'DELETE',
        headers: getAuthHeaders()
      })

      if (response.status === 401) {
        handleLogout()
        return
      }

      if (response.ok) {
        console.log('✅ Plant deleted successfully')
        fetchPlants()
      } else {
        setError(`Failed to delete plant: ${response.status}`)
      }
    } catch (error) {
      console.error('Failed to delete plant:', error)
      setError(`Error deleting plant: ${error.message}`)
    }
  }

  // Loading auth
  if (authLoading) {
    return (
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        height: '100vh',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        color: 'white',
        fontFamily: 'system-ui, -apple-system, sans-serif'
      }}>
        <div style={{ textAlign: 'center' }}>
          <h1 style={{ fontSize: '32px', marginBottom: '10px' }}>🌱 PlantPulse</h1>
          <p>Loading...</p>
        </div>
      </div>
    )
  }

  // Not authenticated - show login/register
  if (!isAuthenticated) {
    return showRegister ? (
      <Register onRegisterSuccess={handleRegisterSuccess} />
    ) : (
      <Login onLoginSuccess={handleLoginSuccess} />
    )
  }

  // Authenticated - show main app
  return (
    <>
      {activeTab === 'community' ? (
        <div className="wrap">
          <Navigation
            activeTab={activeTab}
            setActiveTab={setActiveTab}
            token={authService.getToken()}
            userEmail={authService.getUserEmail()}
            onLogout={handleLogout}
          />
          <CommunityMap token={authService.getToken()} />
        </div>
      ) : activeTab === 'observations' ? (
        <div className="wrap">
          <Navigation
            activeTab={activeTab}
            setActiveTab={setActiveTab}
            token={authService.getToken()}
            userEmail={authService.getUserEmail()}
            onLogout={handleLogout}
          />
          <Observations plants={plants} token={authService.getToken()} />
        </div>
      ) : (
        <div className="wrap">
          <Navigation
            activeTab={activeTab}
            setActiveTab={setActiveTab}
            token={authService.getToken()}
            userEmail={authService.getUserEmail()}
            onLogout={handleLogout}
          />

          {error && (
            <div style={{
              background: '#fee',
              border: '1px solid #fcc',
              color: '#c33',
              padding: '12px 16px',
              borderRadius: '6px',
              margin: '20px',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center'
            }}>
              <span>⚠️ {error}</span>
              <button
                onClick={() => setError(null)}
                style={{
                  background: 'none',
                  border: 'none',
                  cursor: 'pointer',
                  fontSize: '18px',
                  color: '#c33'
                }}
              >
                ✕
              </button>
            </div>
          )}

          <Hero plantsCount={plants.length} onAddPlant={() => setShowAddModal(true)} />
          <Alert token={authService.getToken()} city="Skopje" />

          {loading ? (
            <div className="loading">Loading your garden...</div>
          ) : plants.length === 0 ? (
            <div className="empty-state">
              <p>{getEmptyMessage()}</p>
            </div>
          ) : filteredPlants.length === 0 ? (
            <div className="empty-state">
              <p>{getEmptyMessage()}</p>
            </div>
          ) : (
            <PlantGrid
              plants={filteredPlants}
              onLogObservation={handleLogObservation}
              onShowDetails={handleShowDetails}
              onDelete={handleDeletePlant}
              onShare={handleShare}
              onWaterPlant={handleWaterPlant}
            />
          )}

          <div className="footer-stamp">✓ No other active alerts in your region this week</div>
        </div>
      )}

      <AddPlantModal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        onPlantAdded={fetchPlants}
        token={authService.getToken()}
      />

      {selectedPlant && (
        <ObservationModal
          isOpen={showObservationModal}
          onClose={() => setShowObservationModal(false)}
          plant={selectedPlant}
          onObservationLogged={fetchPlants}
          token={authService.getToken()}
        />
      )}

      {detailsPlant && (
        <PlantDetailsModal
          isOpen={showDetailsModal}
          onClose={() => setShowDetailsModal(false)}
          plant={detailsPlant}
          species={speciesMap[detailsPlant.speciesId]}
          token={authService.getToken()}
        />
      )}

      {plantToShare && (
        <SharePlantModal
          isOpen={showShareModal}
          onClose={() => setShowShareModal(false)}
          plant={plantToShare}
          token={authService.getToken()}
          onShared={fetchPlants}
        />
      )}
    </>
  )
}

export default App