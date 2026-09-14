import { useState, useEffect } from 'react'
import { authService } from '../services/authService'
import '../styles/Observations.css'

export default function Observations({ plants, token }) {
  const [observations, setObservations] = useState([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('all') // all, healthy, at-risk, diseased
  const [sortBy, setSortBy] = useState('recent') // recent, oldest, health

  useEffect(() => {
    fetchAllObservations()
  }, [plants])

  const fetchAllObservations = async () => {
    setLoading(true)
    try {
      const authToken = await authService.getToken()
      if (!authToken) {
        setLoading(false)
        return
      }

      const allObs = []

      // Fetch observations for each plant
      for (const plant of plants) {
        try {
          const response = await fetch(`http://localhost:8081/api/plants/${plant.id}/observations`, {
            headers: {
              'Authorization': `Bearer ${authToken}`,
              'Content-Type': 'application/json'
            }
          })

          if (response.ok) {
            const data = await response.json()
            allObs.push(...data.map(obs => ({
              ...obs,
              plantId: plant.id,
              plantNickname: plant.nickname,
              speciesName: plant.speciesName
            })))
          }
        } catch (err) {
          console.error(`Failed to fetch observations for ${plant.nickname}:`, err)
        }
      }

      setObservations(allObs)
    } catch (err) {
      console.error('Failed to fetch observations:', err)
    } finally {
      setLoading(false)
    }
  }

  const getFilteredObservations = () => {
    let filtered = observations

    switch (filter) {
      case 'healthy':
        filtered = observations.filter(obs => obs.healthScore >= 4)
        break
      case 'at-risk':
        filtered = observations.filter(obs => obs.healthScore && obs.healthScore < 3)
        break
      case 'diseased':
        filtered = observations.filter(obs => obs.diseaseMatch)
        break
      case 'all':
      default:
        break
    }

    // Sort
    switch (sortBy) {
      case 'oldest':
        return filtered.sort((a, b) => new Date(a.loggedAt) - new Date(b.loggedAt))
      case 'health':
        return filtered.sort((a, b) => (b.healthScore || 0) - (a.healthScore || 0))
      case 'recent':
      default:
        return filtered.sort((a, b) => new Date(b.loggedAt) - new Date(a.loggedAt))
    }
  }

  const getHealthColor = (obs) => {
    // Check if plant is healthy
    if (!obs.diseaseMatch) {
      return '#10b981' // green - No disease
    }

    // If disease name is "Healthy", it's green regardless of percentage
    if (obs.diseaseMatch.diseaseName && obs.diseaseMatch.diseaseName.toLowerCase() === 'healthy') {
      return '#10b981' // green - Healthy
    }

    // Disease match: higher % = worse health
    const percentage = obs.diseaseMatch.matchPercentage || 0
    if (percentage >= 70) return '#ef4444' // red - Critical disease
    if (percentage >= 40) return '#f59e0b' // orange - Moderate disease
    return '#10b981' // green - Minor issue
  }

  const getHealthStatus = (obs) => {
    // No disease
    if (!obs.diseaseMatch) {
      return 'Healthy'
    }

    // If "Healthy", return healthy
    if (obs.diseaseMatch.diseaseName && obs.diseaseMatch.diseaseName.toLowerCase() === 'healthy') {
      return 'Healthy'
    }

    // Disease status based on percentage
    const percentage = obs.diseaseMatch.matchPercentage || 0
    if (percentage >= 70) return 'Critical'
    if (percentage >= 40) return 'At Risk'
    return 'Minor Issue'
  }


  const filteredObs = getFilteredObservations()

  return (
    <div className="observations-page">
      <div className="obs-header">
        <h2>Observation Log</h2>
        <p>Track all plant observations and health history</p>
      </div>

      <div className="obs-controls">
        <div className="control-group">
          <label>Filter:</label>
          <select value={filter} onChange={(e) => setFilter(e.target.value)} className="select-filter">
            <option value="all">All Observations</option>
            <option value="healthy">Healthy Plants</option>
            <option value="at-risk">At Risk</option>
            <option value="diseased">Diseased</option>
          </select>
        </div>

        <div className="control-group">
          <label>Sort by:</label>
          <select value={sortBy} onChange={(e) => setSortBy(e.target.value)} className="select-filter">
            <option value="recent">Most Recent</option>
            <option value="oldest">Oldest First</option>
            <option value="health">Health Score</option>
          </select>
        </div>

        <button className="btn-refresh" onClick={fetchAllObservations}>
          🔄 Refresh
        </button>
      </div>

      {loading ? (
        <div className="obs-loading">🔄 Loading observations...</div>
      ) : filteredObs.length === 0 ? (
        <div className="obs-empty">
          <p>📭 No observations found</p>
        </div>
      ) : (
        <div className="obs-timeline">
          {filteredObs.map((obs, idx) => (
            <div key={idx} className="obs-item">
              <div className="obs-header-bar">
                <div className="obs-plant-info">
                  <span className="obs-plant-name">{obs.plantNickname}</span>
                  <span className="obs-species">{obs.speciesName}</span>
                </div>
                <div className="obs-date">
                  {new Date(obs.loggedAt).toLocaleDateString()}
                  {' '}
                  <span className="obs-time">
                    {new Date(obs.loggedAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                  </span>
                </div>
              </div>

              <div className="obs-content">
                {/* Health Score */}
                <div className="obs-section health-section">
                  <div className="health-badge" style={{ backgroundColor: getHealthColor(obs) }}>
                    {obs.diseaseMatch ? `${obs.diseaseMatch.matchPercentage}%` : 'Healthy'}
                  </div>
                  <span className="health-status">{getHealthStatus(obs)}</span>
                </div>

                {/* Visual Symptoms */}
                <div className="obs-section symptoms-section">
                  <div className="symptom-row">
                    <div className="symptom-item">
                      <span className="symptom-label">Leaf Color:</span>
                      <span className="symptom-value">{obs.leafColor}</span>
                    </div>
                    <div className="symptom-item">
                      <span className="symptom-label">Texture:</span>
                      <span className="symptom-value">{obs.leafTexture}</span>
                    </div>
                    <div className="symptom-item">
                      <span className="symptom-label">Soil:</span>
                      <span className="symptom-value">{obs.soilMoisture}</span>
                    </div>
                    {obs.visiblePests && (
                      <div className="symptom-item pest">
                        <span className="symptom-value">Pests Detected</span>
                      </div>
                    )}
                  </div>
                </div>

                {/* Disease Detection */}
                {obs.diseaseMatch && (
                  <div className="obs-section disease-section">
                    <h4>🦠 Disease Detection</h4>
                    <div className="disease-info">
                      <span className="disease-name">{obs.diseaseMatch.diseaseName}</span>
                      <div className="disease-bar">
                        <div
                          className="disease-bar-fill"
                          style={{ width: `${obs.diseaseMatch.matchPercentage}%` }}
                        >
                          {obs.diseaseMatch.matchPercentage}%
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {/* Growth & Measurements */}
                {(obs.heightCm || obs.leafCount || obs.newGrowthCount) && (
                  <div className="obs-section measurements-section">
                    <h4>📊 Measurements</h4>
                    <div className="measurements-grid">
                      {obs.heightCm && <div><span className="meas-label">Height:</span> {obs.heightCm} cm</div>}
                      {obs.leafCount && <div><span className="meas-label">Leaves:</span> {obs.leafCount}</div>}
                      {obs.newGrowthCount && <div><span className="meas-label">New Growth:</span> {obs.newGrowthCount}</div>}
                    </div>
                  </div>
                )}

                {/* Environmental Data */}
                {(obs.roomTemperatureC || obs.humidityPercent || obs.soilMoisturePercent) && (
                  <div className="obs-section environment-section">
                    <h4>🌡️ Environment</h4>
                    <div className="environment-grid">
                      {obs.roomTemperatureC && <div><span className="env-label">Temp:</span> {obs.roomTemperatureC}°C</div>}
                      {obs.humidityPercent && <div><span className="env-label">Humidity:</span> {obs.humidityPercent}%</div>}
                      {obs.soilMoisturePercent && <div><span className="env-label">Soil:</span> {obs.soilMoisturePercent}%</div>}
                    </div>
                  </div>
                )}

                {/* Care Details */}
                {(obs.fertilizerType || obs.pestPresence) && (
                  <div className="obs-section care-section">
                    <h4>🌱 Care</h4>
                    <div className="care-items">
                      {obs.fertilizerType && <span className="care-item">Fertilizer: {obs.fertilizerType}</span>}
                      {obs.pestPresence && <span className="care-item">Pest: {obs.pestPresence}</span>}
                    </div>
                  </div>
                )}

                {/* Notes */}
                {obs.notes && (
                  <div className="obs-section notes-section">
                    <p className="notes-text">"{obs.notes}"</p>
                  </div>
                )}

                {/* Photo */}
                {obs.photoUrl && (
                  <div className="obs-section photo-section">
                    <img src={obs.photoUrl} alt="Observation" onError={(e) => {
                      e.target.style.display = 'none'
                    }} />
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
