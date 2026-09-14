import { useState, useEffect } from 'react'
import { authService } from '../services/authService'
import HealthTimeline from './HealthTimeline'
import './PlantDetailsModal.css'

export default function PlantDetailsModal({ isOpen, onClose, plant, species: passedSpecies, token }) {
  const [species, setSpecies] = useState(passedSpecies)

  useEffect(() => {
    if (isOpen && plant && plant.speciesId) {
      // Always fetch the full details from the API
      fetchSpecies(plant.speciesId)
    }
  }, [isOpen, plant, token])

  const fetchSpecies = async (speciesId) => {
    try {
      console.log('Fetching species details for ID:', speciesId)
      // Always get fresh token
      const authToken = await authService.getToken()
      if (!authToken) {
        console.warn('No auth token available for species details')
        return
      }
      const response = await fetch(`http://localhost:8081/api/species/${speciesId}/details`, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      })
      if (response.ok) {
        const data = await response.json()
        console.log('Fetched species with data:', data)
        console.log('CSV fields present:', {
          height_cm: data.height_cm,
          leaf_count: data.leaf_count,
          watering_amount_ml: data.watering_amount_ml,
          sunlight_exposure: data.sunlight_exposure,
          roomTemperatureC: data.roomTemperatureC,
          humidity_percent: data.humidity_percent,
          soil_type: data.soil_type,
          soil_moisture_percent: data.soil_moisture_percent,
          fertilizer_type: data.fertilizer_type,
          pest_presence: data.pest_presence,
          health_score: data.health_score
        })
        setSpecies(data)
      } else if (response.status === 401) {
        console.error('Unauthorized for species details - token may have expired')
      } else {
        console.error('Failed to fetch full species details:', response.status)
      }
    } catch (err) {
      console.error('Failed to fetch species:', err)
    }
  }

  if (!isOpen || !plant || !species) return null

  const isValidData = (value) => {
    if (value === null || value === undefined) return false
    if (typeof value === 'string') {
      const lower = value.toLowerCase()
      if (lower === 'unknown' || lower === 'none') return false
      return value.trim().length > 0
    }
    return true
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal details-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{plant.nickname}</h2>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>

        {(plant.currentPhotoUrl || species.imageUrl) && (
          <div className="details-photo">
            <img src={plant.currentPhotoUrl || species.imageUrl} alt={plant.nickname} />
          </div>
        )}

        <div className="details-content">
          {/* Species Information - CSV Data Only */}
          <section className="detail-section">
            <h3 className="section-title">Species Information</h3>
            {isValidData(species.name) && (
              <div className="detail-item">
                <span className="label">Plant Name</span>
                <span className="value">{species.name}</span>
              </div>
            )}
          </section>

          {/* Physical Characteristics - CSV Data Only */}
          {(isValidData(species.height_cm) || isValidData(species.leaf_count) || isValidData(species.new_growth_count)) && (
            <section className="detail-section">
              <h3 className="section-title">Physical Characteristics</h3>
              {isValidData(species.height_cm) && (
                <div className="detail-item">
                  <span className="label">Height</span>
                  <span className="value">{species.height_cm} cm</span>
                </div>
              )}
              {isValidData(species.leaf_count) && (
                <div className="detail-item">
                  <span className="label">Leaf Count</span>
                  <span className="value">{species.leaf_count} leaves</span>
                </div>
              )}
              {isValidData(species.new_growth_count) && (
                <div className="detail-item">
                  <span className="label">New Growth</span>
                  <span className="value">{species.new_growth_count} new shoots</span>
                </div>
              )}
            </section>
          )}

          {/* Care Requirements - CSV Data Only */}
          {(isValidData(species.wateringFrequencyDays) || isValidData(species.watering_amount_ml) || isValidData(species.sunlight_exposure) || isValidData(species.fertilizer_type)) && (
            <section className="detail-section">
              <h3 className="section-title">Care Requirements</h3>
              {isValidData(species.wateringFrequencyDays) && (
                <div className="detail-item">
                  <span className="label">Watering Frequency</span>
                  <span className="value">Every {species.wateringFrequencyDays} days</span>
                </div>
              )}
              {isValidData(species.watering_amount_ml) && (
                <div className="detail-item">
                  <span className="label">Watering Amount</span>
                  <span className="value">{species.watering_amount_ml} ml</span>
                </div>
              )}
              {isValidData(species.sunlight_exposure) && (
                <div className="detail-item">
                  <span className="label">Sunlight Exposure</span>
                  <span className="value">{species.sunlight_exposure}</span>
                </div>
              )}
              {isValidData(species.fertilizer_type) && (
                <div className="detail-item">
                  <span className="label">Fertilizer Type</span>
                  <span className="value">{species.fertilizer_type}</span>
                </div>
              )}
              {isValidData(species.fertilizer_amount_ml) && (
                <div className="detail-item">
                  <span className="label">Fertilizer Amount</span>
                  <span className="value">{species.fertilizer_amount_ml} ml</span>
                </div>
              )}
            </section>
          )}

          {/* Environmental Conditions - CSV Data Only */}
          {(isValidData(species.roomTemperatureC) || isValidData(species.humidity_percent) || isValidData(species.soil_moisture_percent)) && (
            <section className="detail-section">
              <h3 className="section-title">Environmental Conditions</h3>
              {isValidData(species.roomTemperatureC) && (
                <div className="detail-item">
                  <span className="label">Room Temperature</span>
                  <span className="value">{species.roomTemperatureC}°C</span>
                </div>
              )}
              {isValidData(species.humidity_percent) && (
                <div className="detail-item">
                  <span className="label">Humidity</span>
                  <span className="value">{species.humidity_percent}%</span>
                </div>
              )}
              {isValidData(species.soil_moisture_percent) && (
                <div className="detail-item">
                  <span className="label">Soil Moisture</span>
                  <span className="value">{species.soil_moisture_percent}%</span>
                </div>
              )}
            </section>
          )}

          {/* Soil Requirements - CSV Data Only */}
          {(isValidData(species.soil_type) || isValidData(species.soil_moisture_percent)) && (
            <section className="detail-section">
              <h3 className="section-title">Soil Requirements</h3>
              {isValidData(species.soil_type) && (
                <div className="detail-item">
                  <span className="label">Soil Type</span>
                  <span className="value">{species.soil_type}</span>
                </div>
              )}
              {isValidData(species.soil_moisture_percent) && (
                <div className="detail-item">
                  <span className="label">Soil Moisture</span>
                  <span className="value">{species.soil_moisture_percent}%</span>
                </div>
              )}
            </section>
          )}

          {/* Pest & Disease Information - CSV Data Only */}
          {(isValidData(species.pest_presence) || isValidData(species.pest_severity)) && (
            <section className="detail-section">
              <h3 className="section-title">Pest & Disease Info</h3>
              {isValidData(species.pest_presence) && (
                <div className="detail-item">
                  <span className="label">Pest Presence</span>
                  <span className="value">{species.pest_presence}</span>
                </div>
              )}
              {isValidData(species.pest_severity) && (
                <div className="detail-item">
                  <span className="label">Pest Severity</span>
                  <span className="value">{species.pest_severity}</span>
                </div>
              )}
            </section>
          )}

          {/* Health & Growth */}
          {(isValidData(species.health_score) || isValidData(species.health_notes)) && (
            <section className="detail-section">
              <h3 className="section-title">Health & Growth</h3>
              {isValidData(species.health_score) && (
                <div className="detail-item">
                  <span className="label">Health Score</span>
                  <span className="value">{species.health_score}/5</span>
                </div>
              )}
              {isValidData(species.health_notes) && (
                <div className="detail-item">
                  <span className="label">Health Notes</span>
                  <span className="value">{species.health_notes}</span>
                </div>
              )}
            </section>
          )}

          {/* Your Plant Details */}
          <section className="detail-section">
            <h3 className="section-title">Your Plant</h3>
            {isValidData(plant.nickname) && (
              <div className="detail-item">
                <span className="label">Nickname</span>
                <span className="value">{plant.nickname}</span>
              </div>
            )}
            {isValidData(plant.room) && (
              <div className="detail-item">
                <span className="label">Location</span>
                <span className="value">{plant.room}</span>
              </div>
            )}
            {plant.addedAt && (
              <div className="detail-item">
                <span className="label">Added to Garden</span>
                <span className="value">{new Date(plant.addedAt).toLocaleDateString()}</span>
              </div>
            )}
            {plant.wateringFrequencyDays && (
              <div className="detail-item">
                <span className="label">Next Watering</span>
                <span className="value highlight">In {plant.wateringFrequencyDays} days</span>
              </div>
            )}
          </section>

          {/* Health Timeline */}
          <HealthTimeline plant={plant} token={token} />
        </div>

        <button className="btn-close" onClick={onClose}>Close</button>
      </div>
    </div>
  )
}
