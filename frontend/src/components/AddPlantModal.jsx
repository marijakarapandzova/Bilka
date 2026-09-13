import { useState, useEffect } from 'react'
import './AddPlantModal.css'

export default function AddPlantModal({ isOpen, onClose, onPlantAdded, token }) {
  const [tab, setTab] = useState('manual')
  const [allSpecies, setAllSpecies] = useState([])
  const [filteredSpecies, setFilteredSpecies] = useState([])
  const [searchQuery, setSearchQuery] = useState('')
  const [selectedSpecies, setSelectedSpecies] = useState(null)
  const [nickname, setNickname] = useState('')
  const [room, setRoom] = useState('')
  const [photoUrl, setPhotoUrl] = useState('')
  const [photoFile, setPhotoFile] = useState(null)
  const [photoPreview, setPhotoPreview] = useState(null)
  const [identifiedSpecies, setIdentifiedSpecies] = useState(null)
  const [confidence, setConfidence] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)
  const [searchLoading, setSearchLoading] = useState(false)

  // Load all species when modal opens
  useEffect(() => {
    if (isOpen && allSpecies.length === 0) {
      loadAllSpecies()
    }
  }, [isOpen])

  // Search species via backend API as user types (with debounce)
  useEffect(() => {
    const timer = setTimeout(() => {
      if (searchQuery.trim().length > 0) {
        searchSpecies(searchQuery)
      } else {
        // Show all species when search is empty
        setFilteredSpecies(allSpecies)
        setSearchLoading(false)
      }
    }, 300) // Wait 300ms after user stops typing

    return () => clearTimeout(timer)
  }, [searchQuery, allSpecies])

  const searchSpecies = async (query) => {
    setSearchLoading(true)
    try {
      console.log('Searching for:', query)
      const url = `http://localhost:8081/api/species?query=${encodeURIComponent(query)}`
      console.log('Fetch URL:', url)
      const response = await fetch(url, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      console.log('Response status:', response.status, response.ok)
      if (response.ok) {
        const data = await response.json()
        console.log('Search results for', query, ':', data.length, data)
        setFilteredSpecies(data)
      } else {
        console.error('Search failed with status:', response.status)
        setFilteredSpecies([])
      }
    } catch (err) {
      console.error('Search error:', err)
      setFilteredSpecies([])
    } finally {
      setSearchLoading(false)
    }
  }

  const loadAllSpecies = async () => {
    try {
      console.log('Loading Perenual catalog...')
      const response = await fetch('http://localhost:8081/api/species/catalog/browse?page=1', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      console.log('Catalog response status:', response.status, response.ok)
      if (response.ok) {
        const data = await response.json()
        console.log('Successfully loaded catalog species:', data.length)
        setAllSpecies(data)
        setFilteredSpecies(data)
      } else {
        const text = await response.text()
        console.error('Catalog fetch failed:', response.status, text)
      }
    } catch (err) {
      console.error('Failed to load catalog:', err.message, err)
    }
  }

  const handlePhotoUpload = (e) => {
    const file = e.target.files?.[0]
    if (!file) return

    setPhotoFile(file)
    const reader = new FileReader()
    reader.onload = (event) => {
      setPhotoPreview(event.target?.result)
    }
    reader.readAsDataURL(file)
  }

  const identifyPlant = async () => {
    if (!photoFile || !nickname.trim()) {
      setError('Please select a photo and enter a nickname')
      return
    }

    setLoading(true)
    setError('')

    try {
      const reader = new FileReader()
      reader.onload = async (event) => {
        const base64 = event.target?.result?.split(',')[1]
        if (!base64) {
          setError('Failed to process image')
          setLoading(false)
          return
        }

        try {
          const response = await fetch('http://localhost:8081/api/plants/by-photo', {
            method: 'POST',
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'application/json'
            },
            body: JSON.stringify({
              imageBase64: base64,
              nickname: nickname,
              room: room || null
            })
          })

          if (response.ok) {
            const data = await response.json()
            console.log('Plant identified:', data)
            setIdentifiedSpecies(data)
            setConfidence(data.photoMatchConfidencePercent || 0)
            setSuccess(true)
            setTimeout(() => {
              resetForm()
              onPlantAdded()
              onClose()
            }, 1500)
          } else {
            const errorData = await response.json()
            setError('Could not identify from photo. Switch to Manual tab to select species manually.')
            setLoading(false)
          }
        } catch (err) {
          setError('Error: ' + err.message)
        } finally {
          setLoading(false)
        }
      }
      reader.readAsDataURL(photoFile)
    } catch (err) {
      setError('Error processing image: ' + err.message)
      setLoading(false)
    }
  }


  const handleSelectSpecies = (spec) => {
    console.log('Selected species:', spec.id, spec.name, spec.careDifficulty)
    setSelectedSpecies(spec)
    setSearchQuery(spec.name)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess(false)

    if (!selectedSpecies) {
      setError('Please select a species from the dropdown list')
      return
    }

    if (!nickname.trim()) {
      setError('Please enter a nickname for your plant')
      return
    }

    setLoading(true)
    try {
      const response = await fetch('http://localhost:8081/api/plants/manual', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          speciesId: selectedSpecies.id,
          nickname: nickname,
          room: room || null,
          currentPhotoUrl: photoUrl || null
        })
      })

      if (response.ok) {
        setSuccess(true)
        setTimeout(() => {
          resetForm()
          onPlantAdded()
          onClose()
        }, 1500)
      } else {
        const errorData = await response.json()
        setError(errorData.message || 'Failed to add plant')
      }
    } catch (err) {
      setError('Error adding plant: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  const resetForm = () => {
    setSearchQuery('')
    setSelectedSpecies(null)
    setNickname('')
    setRoom('')
    setPhotoUrl('')
    setPhotoFile(null)
    setPhotoPreview(null)
    setIdentifiedSpecies(null)
    setConfidence(0)
    setError('')
    setSuccess(false)
  }

  if (!isOpen) return null

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Add Plant to Your Garden</h2>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>

        <div className="modal-tabs">
          <button
            className={`tab-btn ${tab === 'manual' ? 'active' : ''}`}
            onClick={() => setTab('manual')}
          >
            ✏️ Manual
          </button>
          <button
            className={`tab-btn ${tab === 'photo' ? 'active' : ''}`}
            onClick={() => setTab('photo')}
          >
            📷 Photo ID
          </button>
        </div>

        {success ? (
          <div className="modal-success">
            <div className="success-icon">✓</div>
            <p>Plant added successfully!</p>
          </div>
        ) : tab === 'manual' ? (
          <form onSubmit={handleSubmit} className="modal-form">
            {error && <div className="form-error">{error}</div>}

            {/* Species Search */}
            <div className="form-group">
              <label>
                Species {selectedSpecies && <span className="label-check">✓</span>}
                <div style={{fontSize: '10px', color: '#999'}}>
                  Debug: filtered={filteredSpecies.length}, all={allSpecies.length}, search="{searchQuery}", selected={selectedSpecies?.name}
                </div>
              </label>
              <div className="species-search">
                <input
                  type="text"
                  placeholder="Type species name or click to browse all..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  autoComplete="off"
                  style={selectedSpecies ? { borderColor: 'var(--green)' } : {}}
                />
                {selectedSpecies && <span className="selected-badge">{selectedSpecies.name}</span>}

                {!selectedSpecies && filteredSpecies.length > 0 && (
                  <div className="species-dropdown">
                    <div className="dropdown-label">Select a species:</div>
                    {filteredSpecies.map((spec) => (
                      <button
                        key={spec.id}
                        type="button"
                        className={`species-option ${selectedSpecies?.id === spec.id ? 'active' : ''}`}
                        onClick={() => handleSelectSpecies(spec)}
                      >
                        <div className="species-name">{spec.name}</div>
                        <div className="species-scientific">{spec.scientificName}</div>
                        {spec.description && (
                          <div className="species-description">{spec.description}</div>
                        )}
                        <div className="species-details">
                          {spec.careDifficulty && (
                            <span className="detail-badge">Difficulty: {spec.careDifficulty}</span>
                          )}
                          {spec.lightNeeds && (
                            <span className="detail-badge">Light: {spec.lightNeeds.replace(/_/g, ' ')}</span>
                          )}
                          {spec.wateringFrequencyDays && (
                            <span className="detail-badge">Water: every {spec.wateringFrequencyDays}d</span>
                          )}
                          {spec.temperatureMin && (
                            <span className="detail-badge">Temp: {spec.temperatureMin}°C - {spec.temperatureMax}°C</span>
                          )}
                          {spec.phMin && (
                            <span className="detail-badge">pH: {spec.phMin} - {spec.phMax}</span>
                          )}
                          {spec.soilDescription && (
                            <span className="detail-badge">Soil: {spec.soilDescription}</span>
                          )}
                        </div>
                      </button>
                    ))}
                  </div>
                )}
              </div>

              {!selectedSpecies && allSpecies.length === 0 && (
                <div className="species-empty">Loading species...</div>
              )}

              {!selectedSpecies && searchQuery.length > 0 && searchLoading && (
                <div className="species-empty">Searching...</div>
              )}

              {!selectedSpecies && searchQuery.length > 0 && !searchLoading && filteredSpecies.length === 0 && (
                <div className="species-empty">No species match "{searchQuery}"</div>
              )}
            </div>

            {/* Plant Nickname */}
            <div className="form-group">
              <label>Plant Nickname</label>
              <input
                type="text"
                placeholder="e.g., My Green Friend"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                required
              />
            </div>

            {/* Room */}
            <div className="form-group">
              <label>Room (optional)</label>
              <input
                type="text"
                placeholder="e.g., Living Room, Kitchen"
                value={room}
                onChange={(e) => setRoom(e.target.value)}
              />
            </div>

            {/* Photo URL */}
            <div className="form-group">
              <label>Photo URL (optional)</label>
              <input
                type="url"
                placeholder="e.g., https://example.com/plant.jpg"
                value={photoUrl}
                onChange={(e) => setPhotoUrl(e.target.value)}
              />
              {photoUrl && (
                <div className="photo-preview">
                  <img src={photoUrl} alt="Preview" onError={(e) => {
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
                {loading ? 'Adding...' : 'Add Plant'}
              </button>
            </div>
          </form>
        ) : (
          <div className="modal-form">
            {error && <div className="form-error">{error}</div>}

            {/* Photo Upload */}
            <div className="form-group">
              <label>Upload Plant Photo</label>
              <div className="photo-upload-area">
                <input
                  type="file"
                  accept="image/*"
                  onChange={handlePhotoUpload}
                  id="photo-input"
                  style={{ display: 'none' }}
                />
                <label htmlFor="photo-input" className="photo-upload-btn">
                  📸 Click to select photo or drag & drop
                </label>
              </div>
              {photoPreview && (
                <div className="photo-preview-large">
                  <img src={photoPreview} alt="Preview" />
                </div>
              )}
            </div>

            {/* Plant Nickname */}
            <div className="form-group">
              <label>Plant Nickname</label>
              <input
                type="text"
                placeholder="e.g., My Green Friend"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                required
              />
            </div>

            {/* Room */}
            <div className="form-group">
              <label>Room (optional)</label>
              <input
                type="text"
                placeholder="e.g., Living Room, Kitchen"
                value={room}
                onChange={(e) => setRoom(e.target.value)}
              />
            </div>

            {identifiedSpecies && (
              <div className="species-identified">
                <div className="identified-header">🎯 Plant Identified!</div>
                <div className="identified-species">
                  <span className="species-name">{identifiedSpecies.speciesName}</span>
                  <span className="confidence-badge">{confidence}% match</span>
                </div>
              </div>
            )}

            {/* Buttons */}
            <div className="form-actions">
              <button type="button" className="btn-cancel" onClick={onClose}>
                Cancel
              </button>
              <button
                type="button"
                className="btn-submit"
                disabled={loading || !photoFile}
                onClick={identifyPlant}
              >
                {loading ? 'Identifying...' : 'Identify & Add'}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}