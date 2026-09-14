import { useState, useEffect } from 'react'
import { authService } from '../services/authService'
import './ObservationModal.css'
import ObservationCalendar from './ObservationCalendar'

export default function ObservationModal({ isOpen, onClose, plant, onObservationLogged, token }) {
  const [tab, setTab] = useState('log')
  const [leafColor, setLeafColor] = useState('GREEN')
  const [leafTexture, setLeafTexture] = useState('HEALTHY')
  const [soilMoisture, setSoilMoisture] = useState('MOIST')
  const [visiblePests, setVisiblePests] = useState(false)
  const [growth, setGrowth] = useState('NORMAL')
  const [notes, setNotes] = useState('')
  const [photoUrl, setPhotoUrl] = useState('')
  const [photoPreview, setPhotoPreview] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)
  const [history, setHistory] = useState([])
  const [historyLoading, setHistoryLoading] = useState(false)
  const [cityLocation, setCityLocation] = useState('Skopje')
  // Kaggle dataset fields
  const [heightCm, setHeightCm] = useState('')
  const [leafCount, setLeafCount] = useState('')
  const [newGrowthCount, setNewGrowthCount] = useState('')
  const [healthNotes, setHealthNotes] = useState('')
  const [wateringAmountMl, setWateringAmountMl] = useState('')
  const [wateringFrequencyDays, setWateringFrequencyDays] = useState('')
  const [sunlightExposure, setSunlightExposure] = useState('')
  const [roomTemperatureC, setRoomTemperatureC] = useState('')
  const [humidityPercent, setHumidityPercent] = useState('')
  const [fertilizerType, setFertilizerType] = useState('')
  const [fertilizerAmountMl, setFertilizerAmountMl] = useState('')
  const [pestPresence, setPestPresence] = useState('')
  const [pestSeverity, setPestSeverity] = useState('')
  const [soilMoisturePercent, setSoilMoisturePercent] = useState('')
  const [soilType, setSoilType] = useState('')
  const [healthScore, setHealthScore] = useState('')

  useEffect(() => {
    if (isOpen && plant) {
      loadObservationHistory()
    }
  }, [isOpen, plant])

  const loadObservationHistory = async () => {
    setHistoryLoading(true)
    try {
      const authToken = token || await authService.getToken()
      if (!authToken) {
        setHistoryLoading(false)
        return
      }
      const response = await fetch(`http://localhost:8081/api/plants/${plant.id}/observations`, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      })
      if (response.ok) {
        const data = await response.json()
        setHistory(data)
      }
    } catch (err) {
      console.error('Failed to load observation history:', err)
    } finally {
      setHistoryLoading(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess(false)
    setLoading(true)

    try {
      const authToken = await authService.getToken()
      if (!authToken) {
        setError('Authentication failed - please log in again')
        setLoading(false)
        return
      }

      const response = await fetch(`http://localhost:8081/api/plants/${plant.id}/observations`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          leafColor,
          leafTexture,
          soilMoisture,
          visiblePests,
          growth,
          notes: notes || null,
          photoUrl: photoUrl || null,
          heightCm: heightCm ? parseFloat(heightCm) : null,
          leafCount: leafCount ? parseInt(leafCount) : null,
          newGrowthCount: newGrowthCount ? parseInt(newGrowthCount) : null,
          healthNotes: healthNotes || null,
          wateringAmountMl: wateringAmountMl ? parseFloat(wateringAmountMl) : null,
          wateringFrequencyDays: wateringFrequencyDays ? parseInt(wateringFrequencyDays) : null,
          sunlightExposure: sunlightExposure || null,
          roomTemperatureC: roomTemperatureC ? parseFloat(roomTemperatureC) : null,
          humidityPercent: humidityPercent ? parseFloat(humidityPercent) : null,
          fertilizerType: fertilizerType || null,
          fertilizerAmountMl: fertilizerAmountMl ? parseFloat(fertilizerAmountMl) : null,
          pestPresence: pestPresence || null,
          pestSeverity: pestSeverity || null,
          soilMoisturePercent: soilMoisturePercent ? parseFloat(soilMoisturePercent) : null,
          soilType: soilType || null,
          healthScore: healthScore ? parseInt(healthScore) : null,
          cityLocation: cityLocation || null
        })
      })

      if (response.ok) {
        // Also update health score in Health Service
        const observationData = await response.json()
        if (observationData.diseaseMatch) {
          try {
            const healthToken = await authService.getToken()
            await fetch(`http://localhost:8082/api/health/plants/${plant.id}/record-observation`, {
              method: 'POST',
              headers: {
                'Authorization': `Bearer ${healthToken}`,
                'Content-Type': 'application/json'
              },
              body: JSON.stringify({
                diseaseMatchName: observationData.diseaseMatch.diseaseName,
                diseaseMatchPercentage: observationData.diseaseMatch.matchPercentage
              })
            })
          } catch (err) {
            console.warn('Failed to update health score in Health Service:', err)
            // Don't fail the observation if health service update fails
          }
        }

        setSuccess(true)
        setTimeout(() => {
          resetForm()
          onObservationLogged()
          onClose()
        }, 1500)
      } else {
        const errorData = await response.json()
        setError(errorData.message || 'Failed to log observation')
      }
    } catch (err) {
      setError('Error: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  const resetForm = () => {
    setLeafColor('GREEN')
    setLeafTexture('HEALTHY')
    setSoilMoisture('MOIST')
    setVisiblePests(false)
    setGrowth('NORMAL')
    setNotes('')
    setPhotoUrl('')
    setPhotoPreview(null)
    setHeightCm('')
    setLeafCount('')
    setNewGrowthCount('')
    setHealthNotes('')
    setWateringAmountMl('')
    setWateringFrequencyDays('')
    setSunlightExposure('')
    setRoomTemperatureC('')
    setHumidityPercent('')
    setFertilizerType('')
    setFertilizerAmountMl('')
    setPestPresence('')
    setPestSeverity('')
    setSoilMoisturePercent('')
    setSoilType('')
    setHealthScore('')
    setCityLocation('Skopje')
    setError('')
    setSuccess(false)
  }

  if (!isOpen) return null

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Log Observation: {plant.nickname}</h2>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>

        <div className="modal-tabs">
          <button
            className={`tab-btn ${tab === 'log' ? 'active' : ''}`}
            onClick={() => setTab('log')}
          >
            Log
          </button>
          <button
            className={`tab-btn ${tab === 'calendar' ? 'active' : ''}`}
            onClick={() => setTab('calendar')}
          >
            Calendar
          </button>
        </div>

        <div className="modal-content">
          {tab === 'calendar' ? (
            <ObservationCalendar plant={plant} token={token} onLogObservation={(p, date) => {
              setTab('log')
            }} />
          ) : success ? (
            <div className="modal-success">
              <div className="success-icon">✓</div>
              <p>Observation logged successfully</p>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="modal-form">
            {error && <div className="form-error">{error}</div>}

            {/* Leaf Color */}
            {/* City Location for Outbreak Detection */}
            <div className="form-group">
              <label>City Location</label>
              <input
                type="text"
                placeholder="e.g., Skopje"
                value={cityLocation}
                onChange={(e) => setCityLocation(e.target.value)}
                style={{ padding: '8px', width: '100%', borderRadius: '4px', border: '1px solid #ddd' }}
              />
            </div>

            <div className="form-group">
              <label>Leaf Color</label>
              <div className="radio-group">
                {['GREEN', 'YELLOW', 'BROWN', 'SPOTTED'].map((color) => (
                  <label key={color} className="radio-label">
                    <input
                      type="radio"
                      name="leafColor"
                      value={color}
                      checked={leafColor === color}
                      onChange={(e) => setLeafColor(e.target.value)}
                    />
                    <span className="radio-display">{color.charAt(0) + color.slice(1).toLowerCase()}</span>
                  </label>
                ))}
              </div>
            </div>

            {/* Leaf Texture */}
            <div className="form-group">
              <label>Leaf Texture</label>
              <div className="radio-group">
                {['HEALTHY', 'WILTING', 'MUSHY', 'CRISPY'].map((texture) => (
                  <label key={texture} className="radio-label">
                    <input
                      type="radio"
                      name="leafTexture"
                      value={texture}
                      checked={leafTexture === texture}
                      onChange={(e) => setLeafTexture(e.target.value)}
                    />
                    <span className="radio-display">{texture.charAt(0) + texture.slice(1).toLowerCase()}</span>
                  </label>
                ))}
              </div>
            </div>

            {/* Soil Moisture */}
            <div className="form-group">
              <label>Soil Moisture</label>
              <div className="radio-group">
                {['DRY', 'MOIST', 'WATERLOGGED'].map((moisture) => (
                  <label key={moisture} className="radio-label">
                    <input
                      type="radio"
                      name="soilMoisture"
                      value={moisture}
                      checked={soilMoisture === moisture}
                      onChange={(e) => setSoilMoisture(e.target.value)}
                    />
                    <span className="radio-display">{moisture.charAt(0) + moisture.slice(1).toLowerCase()}</span>
                  </label>
                ))}
              </div>
            </div>

            {/* Growth */}
            <div className="form-group">
              <label>Growth Rate</label>
              <div className="radio-group">
                {['NORMAL', 'SLOW', 'STUNTED', 'NONE'].map((g) => (
                  <label key={g} className="radio-label">
                    <input
                      type="radio"
                      name="growth"
                      value={g}
                      checked={growth === g}
                      onChange={(e) => setGrowth(e.target.value)}
                    />
                    <span className="radio-display">{g.charAt(0) + g.slice(1).toLowerCase()}</span>
                  </label>
                ))}
              </div>
            </div>

            {/* Visible Pests */}
            <div className="form-group">
              <label className="checkbox-label">
                <input
                  type="checkbox"
                  checked={visiblePests}
                  onChange={(e) => setVisiblePests(e.target.checked)}
                />
                <span>Visible pests detected</span>
              </label>
            </div>

            {/* Kaggle Dataset Measurements */}
            <div className="form-divider">
              <h4>Detailed Measurements (Optional)</h4>
              <p>Log actual plant measurements to track growth over time</p>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Height (cm)</label>
                <input
                  type="number"
                  step="0.1"
                  placeholder="Plant height in cm"
                  value={heightCm}
                  onChange={(e) => setHeightCm(e.target.value)}
                />
              </div>
              <div className="form-group">
                <label>Leaf Count</label>
                <input
                  type="number"
                  placeholder="Number of leaves"
                  value={leafCount}
                  onChange={(e) => setLeafCount(e.target.value)}
                />
              </div>
              <div className="form-group">
                <label>New Growth</label>
                <input
                  type="number"
                  placeholder="New shoots/leaves"
                  value={newGrowthCount}
                  onChange={(e) => setNewGrowthCount(e.target.value)}
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Watering Amount (ml)</label>
                <input
                  type="number"
                  step="0.1"
                  placeholder="ml per watering"
                  value={wateringAmountMl}
                  onChange={(e) => setWateringAmountMl(e.target.value)}
                />
              </div>
              <div className="form-group">
                <label>Watering Frequency (days)</label>
                <input
                  type="number"
                  placeholder="Days between watering"
                  value={wateringFrequencyDays}
                  onChange={(e) => setWateringFrequencyDays(e.target.value)}
                />
              </div>
              <div className="form-group">
                <label>Sunlight Exposure</label>
                <input
                  type="text"
                  placeholder="e.g., 4h indirect"
                  value={sunlightExposure}
                  onChange={(e) => setSunlightExposure(e.target.value)}
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Temperature (°C)</label>
                <input
                  type="number"
                  step="0.1"
                  placeholder="Room temperature"
                  value={roomTemperatureC}
                  onChange={(e) => setRoomTemperatureC(e.target.value)}
                />
              </div>
              <div className="form-group">
                <label>Humidity (%)</label>
                <input
                  type="number"
                  step="0.1"
                  placeholder="Humidity percentage"
                  value={humidityPercent}
                  onChange={(e) => setHumidityPercent(e.target.value)}
                />
              </div>
              <div className="form-group">
                <label>Soil Moisture (%)</label>
                <input
                  type="number"
                  step="0.1"
                  placeholder="Soil moisture %"
                  value={soilMoisturePercent}
                  onChange={(e) => setSoilMoisturePercent(e.target.value)}
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Soil Type</label>
                <input
                  type="text"
                  placeholder="e.g., Loamy, Sandy"
                  value={soilType}
                  onChange={(e) => setSoilType(e.target.value)}
                />
              </div>
              <div className="form-group">
                <label>Fertilizer Type</label>
                <input
                  type="text"
                  placeholder="e.g., NPK 20-20-20"
                  value={fertilizerType}
                  onChange={(e) => setFertilizerType(e.target.value)}
                />
              </div>
              <div className="form-group">
                <label>Fertilizer Amount (ml)</label>
                <input
                  type="number"
                  step="0.1"
                  placeholder="ml per application"
                  value={fertilizerAmountMl}
                  onChange={(e) => setFertilizerAmountMl(e.target.value)}
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Pest Presence</label>
                <input
                  type="text"
                  placeholder="e.g., Mealybugs, None"
                  value={pestPresence}
                  onChange={(e) => setPestPresence(e.target.value)}
                />
              </div>
              <div className="form-group">
                <label>Pest Severity</label>
                <input
                  type="text"
                  placeholder="e.g., Low, High"
                  value={pestSeverity}
                  onChange={(e) => setPestSeverity(e.target.value)}
                />
              </div>
              <div className="form-group">
                <label>Health Score (1-5)</label>
                <input
                  type="number"
                  min="1"
                  max="5"
                  placeholder="Overall plant health"
                  value={healthScore}
                  onChange={(e) => setHealthScore(e.target.value)}
                />
              </div>
            </div>

            <div className="form-group">
              <label>Health Notes</label>
              <textarea
                placeholder="General health observations..."
                value={healthNotes}
                onChange={(e) => setHealthNotes(e.target.value)}
                rows="2"
              />
            </div>

            {/* Notes */}
            <div className="form-group">
              <label>Additional Notes (optional)</label>
              <textarea
                placeholder="Any other observations..."
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                rows="3"
              />
            </div>

            {/* Photo URL */}
            <div className="form-group">
              <label>Photo URL (optional)</label>
              <input
                type="url"
                placeholder="e.g., https://example.com/plant-photo.jpg"
                value={photoUrl}
                onChange={(e) => {
                  setPhotoUrl(e.target.value)
                  if (e.target.value) setPhotoPreview(e.target.value)
                  else setPhotoPreview(null)
                }}
              />
              {photoPreview && (
                <div className="photo-preview">
                  <img src={photoPreview} alt="Observation photo" onError={(e) => {
                    e.target.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"%3E%3Crect fill="%23ddd" width="100" height="100"/%3E%3Ctext x="50" y="50" text-anchor="middle" dy=".3em" fill="%23999" font-size="12"%3EInvalid URL%3C/text%3E%3C/svg%3E'
                  }} />
                </div>
              )}
            </div>

            {/* Buttons */}
            <div className="form-actions">
              <button type="button" className="btn-cancel" onClick={onClose}>
                Cancel
              </button>
              <button type="submit" className="btn-submit" disabled={loading}>
                {loading ? 'Logging...' : 'Log Observation'}
              </button>
            </div>
          </form>
          )}

          {tab === 'log' && (
            <div className="observation-history">
          <h3 className="history-title">Observation History</h3>
          {historyLoading ? (
            <div className="history-loading">Loading history...</div>
          ) : history.length === 0 ? (
            <div className="history-empty">No observations logged yet</div>
          ) : (
            <div className="history-list">
              {history.map((obs, idx) => (
                <div key={idx} className="history-item">
                  <div className="history-date">
                    {new Date(obs.loggedAt).toLocaleDateString()} {new Date(obs.loggedAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                  </div>
                  <div className="history-symptoms">
                    <span className="symptom-tag">{obs.leafColor}</span>
                    <span className="symptom-tag">{obs.leafTexture}</span>
                    <span className="symptom-tag">{obs.soilMoisture}</span>
                    {obs.visiblePests && <span className="symptom-tag pest">Pests 🐛</span>}
                  </div>
                  {obs.diseaseMatch && (
                    <div className="history-disease">
                      <span className="disease-name">{obs.diseaseMatch.diseaseName}</span>
                      <span className="disease-confidence">{obs.diseaseMatch.matchPercentage}% match</span>
                    </div>
                  )}
                  {obs.photoUrl && (
                    <div className="history-photo">
                      <img src={obs.photoUrl} alt="Observation" />
                    </div>
                  )}
                  {obs.notes && <div className="history-notes">"{obs.notes}"</div>}
                </div>
              ))}
            </div>
          )}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
