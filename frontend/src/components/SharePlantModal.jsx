import { useState } from 'react'
import './SharePlantModal.css'

export default function SharePlantModal({ isOpen, onClose, plant, token, onShared }) {
  const [city, setCity] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!city.trim()) {
      setError('Please enter a city')
      return
    }

    setError('')
    setLoading(true)

    try {
      const response = await fetch('http://localhost:8081/api/shared-plants/share', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          plantId: plant.id,
          city: city.trim(),
          photoUrl: plant.currentPhotoUrl
        })
      })

      if (response.ok) {
        setSuccess(true)
        setTimeout(() => {
          resetForm()
          onShared()
          onClose()
        }, 1500)
      } else {
        const errorData = await response.json()
        setError(errorData.message || 'Failed to share plant')
      }
    } catch (err) {
      setError('Error: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  const resetForm = () => {
    setCity('')
    setError('')
    setSuccess(false)
  }

  if (!isOpen) return null

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Share {plant.nickname}</h2>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>

        {success ? (
          <div className="modal-success">
            <div className="success-icon">✓</div>
            <p>Plant shared successfully!</p>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="share-form">
            {error && <div className="form-error">{error}</div>}

            {plant.currentPhotoUrl && (
              <div className="share-photo-preview">
                <img src={plant.currentPhotoUrl} alt={plant.nickname} />
              </div>
            )}

            <div className="form-info">
              <p><strong>Plant:</strong> {plant.nickname}</p>
              <p><strong>Species:</strong> {plant.speciesName}</p>
            </div>

            <div className="form-group">
              <label>City (where this plant is located)</label>
              <input
                type="text"
                placeholder="e.g., New York, Tokyo, London..."
                value={city}
                onChange={(e) => setCity(e.target.value)}
                disabled={loading}
                autoFocus
              />
              <p className="form-hint">Other users will see your plant on the world map in this city</p>
            </div>

            <div className="form-actions">
              <button type="button" className="btn-cancel" onClick={onClose}>
                Cancel
              </button>
              <button type="submit" className="btn-submit" disabled={loading}>
                {loading ? 'Sharing...' : 'Share on Map'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}
