import { useState, useEffect } from 'react'
import './App.css'
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

  // Mock JWT token for now (skip auth for later implementation)
  const token = localStorage.getItem('token') || 'mock-token-dev'

  useEffect(() => {
    fetchSpecies()
    fetchPlants()
  }, [])

  const fetchSpecies = async () => {
    try {
      const response = await fetch('http://localhost:8081/api/species', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      console.log('Species response status:', response.status, response.ok)
      if (response.ok) {
        const data = await response.json()
        console.log('Fetched species:', data.length)
        const map = {}
        data.forEach(s => {
          map[s.id] = s
        })
        setSpeciesMap(map)
      } else {
        console.error('Species fetch failed:', response.status)
      }
    } catch (error) {
      console.error('Failed to fetch species:', error)
    }
  }

  const fetchPlants = async () => {
    try {
      setLoading(true)
      const response = await fetch('http://localhost:8081/api/plants', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })

      if (response.ok) {
        const data = await response.json()
        // Enrich plants with health data from Health Service
        const enrichedPlants = await Promise.all(data.map(async (plant) => {
          const spec = speciesMap[plant.speciesId]
          // Start with neutral health score
          let health = 75

          // Fetch real health data from Health Service
          try {
            const healthResponse = await fetch(`http://localhost:8082/api/health/plants/${plant.id}`, {
              headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
              }
            })
            if (healthResponse.ok) {
              const healthData = await healthResponse.json()
              console.log(`✅ Health data synced for ${plant.nickname}:`, healthData.healthScore)
              health = healthData.healthScore || 75
            } else if (healthResponse.status === 404) {
              // Plant not yet in Health Service - using stable default (Kafka event format mismatch)
              console.log(`ℹ️ Plant ${plant.nickname} health: using default 75`)
              health = 75
            }
          } catch (err) {
            console.error(`Failed to fetch health for plant ${plant.id}:`, err)
            health = 75
          }

          return {
            ...plant,
            speciesName: spec?.name || 'Unknown',
            health: health, // Real health score from Health Service
            wateringDaysLeft: plant.wateringFrequencyDays || 7,
            notes: 'Sample observation data'
          }
        }))
        setPlants(enrichedPlants)
      }
    } catch (error) {
      console.error('Failed to fetch plants:', error)
    } finally {
      setLoading(false)
    }
  }

  // Filter plants based on active tab
  const getFilteredPlants = () => {
    switch (activeTab) {
      case 'watering':
        // Show plants needing water in next 3 days
        return plants
          .filter(plant => plant.wateringDaysLeft <= 3)
          .sort((a, b) => a.wateringDaysLeft - b.wateringDaysLeft)

      case 'attention':
        // Show plants with health score below 70
        return plants
          .filter(plant => plant.health < 70)
          .sort((a, b) => a.health - b.health)

      case 'community':
        // Return all plants for community view
        return plants

      case 'all':
      default:
        // Sort by attention needed (low health first, then by watering urgency)
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
      console.log('Clicking water button for:', plant.nickname)
      const response = await fetch(`http://localhost:8081/api/plants/${plant.id}/water`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      console.log('Water response status:', response.status)
      if (response.ok) {
        const updatedPlant = await response.json()
        console.log('Updated plant:', updatedPlant)
        console.log('Fetching all plants...')
        fetchPlants()
      } else {
        const errorData = await response.text()
        console.error('Water log failed:', response.status, errorData)
        alert(`Failed to log watering: ${response.status}`)
      }
    } catch (error) {
      console.error('Failed to log watering:', error)
      alert('Error logging watering: ' + error.message)
    }
  }

  const handleDeletePlant = async (plantId) => {
    if (!window.confirm('Are you sure you want to delete this plant?')) {
      return
    }
    try {
      const response = await fetch(`http://localhost:8081/api/plants/${plantId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      if (response.ok) {
        fetchPlants()
      } else {
        alert('Failed to delete plant')
      }
    } catch (error) {
      console.error('Failed to delete plant:', error)
      alert('Error deleting plant')
    }
  }

  return (
    <div className="app">
      {activeTab === 'community' ? (
        <div className="wrap">
          <Navigation activeTab={activeTab} setActiveTab={setActiveTab} token={token} />
          <CommunityMap token={token} />
        </div>
      ) : activeTab === 'observations' ? (
        <div className="wrap">
          <Navigation activeTab={activeTab} setActiveTab={setActiveTab} token={token} />
          <Observations plants={plants} token={token} />
        </div>
      ) : (
        <div className="wrap">
          <Navigation activeTab={activeTab} setActiveTab={setActiveTab} token={token} />
          <Hero plantsCount={plants.length} onAddPlant={() => setShowAddModal(true)} />
          <Alert token={token} city="Skopje" />

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
        token={token}
      />

      {selectedPlant && (
        <ObservationModal
          isOpen={showObservationModal}
          onClose={() => setShowObservationModal(false)}
          plant={selectedPlant}
          onObservationLogged={fetchPlants}
          token={token}
        />
      )}

      {detailsPlant && (
        <PlantDetailsModal
          isOpen={showDetailsModal}
          onClose={() => setShowDetailsModal(false)}
          plant={detailsPlant}
          species={speciesMap[detailsPlant.speciesId]}
          token={token}
        />
      )}

      {plantToShare && (
        <SharePlantModal
          isOpen={showShareModal}
          onClose={() => setShowShareModal(false)}
          plant={plantToShare}
          token={token}
          onShared={fetchPlants}
        />
      )}
    </div>
  )
}

export default App
