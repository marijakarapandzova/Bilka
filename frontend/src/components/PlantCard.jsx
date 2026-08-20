import './PlantCard.css'

const badgeColors = {
  0: 'badge-lavender',
  1: 'badge-butter',
  2: 'badge-sky',
  3: 'badge-green',
}

const badgeEmojis = ['🌿', '🌱', '🌾', '🍃', '🌳', '🪴']

function PlantCard({ plant, onLogObservation, onShowDetails, onDelete, onShare, onWaterPlant }) {
  const badgeColor = badgeColors[Object.keys(badgeColors).length % 4] || 'badge-green'
  const badgeIndex = plant.id.charCodeAt(0) % badgeEmojis.length
  const badgeEmoji = badgeEmojis[badgeIndex]

  const getHealthStatus = (health) => {
    if (health >= 80) return 'Thriving'
    if (health >= 60) return health > 70 ? 'Improving' : 'Steady'
    return 'Declining'
  }

  const getHealthColor = (health) => {
    if (health >= 80) return 'fill-green'
    if (health >= 60) return 'fill-butter'
    return 'fill-berry'
  }

  const getScoreColor = (health) => {
    if (health >= 80) return 'score-green'
    if (health >= 60) return 'score-butter'
    return 'score-berry'
  }

  const calculateWateringDays = () => {
    const freq = plant.wateringFrequencyDays || 7
    if (!plant.lastWateredAt) {
      console.log(`${plant.nickname}: Never watered, frequency: ${freq} days`)
      return freq
    }
    const lastWatered = new Date(plant.lastWateredAt)
    const now = new Date()
    const daysSinceWatering = Math.floor((now - lastWatered) / (1000 * 60 * 60 * 24))
    const daysUntilNextWatering = Math.max(0, freq - daysSinceWatering)
    console.log(`${plant.nickname}: Last watered ${daysSinceWatering} days ago, next watering in ${daysUntilNextWatering} days`)
    return daysUntilNextWatering
  }

  const getWateringStatus = () => {
    const daysLeft = calculateWateringDays()
    if (daysLeft <= 1) return 'Water this plant NOW!'
    return `Water in ${daysLeft} days`
  }

  return (
    <article className="card" tabIndex="0">
      {plant.currentPhotoUrl && (
        <div className="card-photo">
          <img src={plant.currentPhotoUrl} alt={plant.nickname} onError={(e) => {
            e.target.style.display = 'none'
          }} />
        </div>
      )}
      <div className="card-top">
        <div className={`badge ${badgeColor}`}>
          <span style={{ fontSize: '28px' }}>{badgeEmoji}</span>
        </div>
        <div className="card-top-actions">
          {plant.room && <span className="room-tag">{plant.room}</span>}
          {onShare && (
            <button className="icon-btn share-btn" onClick={() => onShare(plant)} title="Share on map">
              ⬆
            </button>
          )}
          {onDelete && (
            <button className="icon-btn delete-btn" onClick={() => onDelete(plant.id)} title="Delete plant">
              ✕
            </button>
          )}
        </div>
      </div>
      <h3 className="card-name">{plant.nickname}</h3>
      <div className="card-species">{plant.speciesName}</div>
      {plant.addedAt && (
        <div className="card-date">
          Added {new Date(plant.addedAt).toLocaleDateString()}
        </div>
      )}
      <div className="water-row">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
          <path d="M12 3C12 3 6 11 6 15.5C6 19.09 8.69 22 12 22C15.31 22 18 19.09 18 15.5C18 11 12 3 12 3Z" stroke="currentColor" strokeWidth="1.6"/>
        </svg>
        <span className={`water-status ${calculateWateringDays() <= 0 ? 'urgent' : ''}`}>{getWateringStatus()}</span>
        {onWaterPlant && calculateWateringDays() <= 0 && (
          <button className="water-btn" onClick={() => onWaterPlant(plant)} title="Log watering">
            ✓
          </button>
        )}
      </div>
      <div className="health-label">
        <span className="status">Health</span>
        <span className={`score ${getScoreColor(plant.health)}`}>{plant.health}/100 · {getHealthStatus(plant.health)}</span>
      </div>
      <div className="bar-track">
        <div className={`bar-fill ${getHealthColor(plant.health)}`} style={{ width: `${plant.health}%` }}></div>
      </div>
      <div className="card-actions">
        <button className="card-action-btn log-btn" onClick={() => onLogObservation(plant)}>
          Log
        </button>
        <button className="card-action-btn info-btn" onClick={() => onShowDetails(plant)}>
          Info
        </button>
      </div>
    </article>
  )
}

export default PlantCard