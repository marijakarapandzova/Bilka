import { useState, useEffect } from 'react'
import './Alert.css'

export default function Alert({ token, city = 'Skopje' }) {
  const [outbreaks, setOutbreaks] = useState([])

  useEffect(() => {
    fetchOutbreaks()
    // Refresh every 10 minutes
    const interval = setInterval(fetchOutbreaks, 10 * 60 * 1000)
    return () => clearInterval(interval)
  }, [city])

  const fetchOutbreaks = async () => {
    try {
      const response = await fetch(`http://localhost:9001/api/alerts/regional/${city}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      if (response.ok) {
        const data = await response.json()
        setOutbreaks(Array.isArray(data) ? data : [])
      }
    } catch (err) {
      console.error('Failed to fetch outbreaks:', err)
    }
  }

  // Only show if there are outbreaks
  if (!outbreaks || outbreaks.length === 0) {
    return null
  }

  const firstOutbreak = outbreaks[0]

  return (
    <div className="alert">
      <div className="alert-icon">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
          <path d="M12 9V13M12 17H12.01M10.29 3.86L1.82 18A2 2 0 0 0 3.55 21H20.45A2 2 0 0 0 22.18 18L13.71 3.86A2 2 0 0 0 10.29 3.86Z" stroke="#C15C74" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
      </div>
      <div className="alert-text">
        <strong>{firstOutbreak.diseaseName} reported nearby</strong>
        <span>{firstOutbreak.affectedPlantCount} case{firstOutbreak.affectedPlantCount !== 1 ? 's' : ''} logged in {firstOutbreak.cityLocation} this week — monitor your plants closely.</span>
      </div>
    </div>
  )
}