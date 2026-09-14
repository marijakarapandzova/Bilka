import { useState, useEffect } from 'react'
import { authService } from '../services/authService'
import './ObservationCalendar.css'

export default function ObservationCalendar({ plant, token, onLogObservation }) {
  const [currentMonth, setCurrentMonth] = useState(new Date())
  const [observations, setObservations] = useState([])
  const [loading, setLoading] = useState(false)
  const [selectedDate, setSelectedDate] = useState(null)

  useEffect(() => {
    loadObservations()
  }, [plant, currentMonth])

  const loadObservations = async () => {
    setLoading(true)
    try {
      // Always get fresh token, ignore token prop
      const authToken = await authService.getToken()
      if (!authToken) {
        console.warn('No auth token available for observations')
        setLoading(false)
        return
      }
      console.log('Fetching observations with token, expires in:', authService.getTokenExpiryIn(), 'seconds')
      const response = await fetch(`http://localhost:8081/api/plants/${plant.id}/observations`, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      })
      console.log('Observations response:', response.status)
      if (response.ok) {
        const data = await response.json()
        setObservations(data)
      } else if (response.status === 401) {
        console.error('Unauthorized for observations - token may have expired')
      }
    } catch (err) {
      console.error('Failed to load observations:', err)
    } finally {
      setLoading(false)
    }
  }

  const getDaysInMonth = (date) => {
    return new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate()
  }

  const getFirstDayOfMonth = (date) => {
    return new Date(date.getFullYear(), date.getMonth(), 1).getDay()
  }

  const getObservationsForDate = (date) => {
    return observations.filter(obs => {
      const obsDate = new Date(obs.loggedAt)
      return obsDate.getDate() === date.getDate() &&
             obsDate.getMonth() === date.getMonth() &&
             obsDate.getFullYear() === date.getFullYear()
    })
  }

  const getDiseaseStatus = (obs) => {
    if (obs.diseaseMatch) {
      if (obs.diseaseMatch.diseaseName === 'Healthy') return 'healthy'
      return 'diseased'
    }
    return 'neutral'
  }

  const monthName = currentMonth.toLocaleDateString('en-US', { month: 'long', year: 'numeric' })
  const daysInMonth = getDaysInMonth(currentMonth)
  const firstDay = getFirstDayOfMonth(currentMonth)
  const days = []

  // Empty cells for days before month starts
  for (let i = 0; i < firstDay; i++) {
    days.push(null)
  }

  // Days of the month
  for (let i = 1; i <= daysInMonth; i++) {
    days.push(new Date(currentMonth.getFullYear(), currentMonth.getMonth(), i))
  }

  return (
    <div className="observation-calendar">
      <div className="calendar-header">
        <button
          className="nav-btn"
          onClick={() => setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1))}
        >
          ←
        </button>
        <h3>{monthName}</h3>
        <button
          className="nav-btn"
          onClick={() => setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1))}
        >
          →
        </button>
      </div>

      <div className="calendar-weekdays">
        <div>Sun</div>
        <div>Mon</div>
        <div>Tue</div>
        <div>Wed</div>
        <div>Thu</div>
        <div>Fri</div>
        <div>Sat</div>
      </div>

      <div className="calendar-grid">
        {days.map((date, idx) => {
          if (!date) {
            return <div key={`empty-${idx}`} className="calendar-day empty"></div>
          }

          const dayObservations = getObservationsForDate(date)
          const isToday = new Date().toDateString() === date.toDateString()
          const status = dayObservations.length > 0
            ? dayObservations.map(getDiseaseStatus).includes('diseased') ? 'diseased' : 'healthy'
            : 'empty'

          return (
            <div
              key={date.toISOString()}
              className={`calendar-day ${status} ${isToday ? 'today' : ''}`}
              onClick={() => {
                setSelectedDate(date)
                onLogObservation(plant, date)
              }}
            >
              <div className="day-number">{date.getDate()}</div>
              {dayObservations.length > 0 && (
                <div className="day-indicator">
                  <span className="obs-count">{dayObservations.length}</span>
                  {dayObservations[0].disease && (
                    <span className={`disease-dot ${getDiseaseStatus(dayObservations[0])}`}></span>
                  )}
                </div>
              )}
            </div>
          )
        })}
      </div>

      <div className="calendar-legend">
        <div className="legend-item">
          <span className="legend-dot empty"></span>
          <span>No observation</span>
        </div>
        <div className="legend-item">
          <span className="legend-dot healthy"></span>
          <span>Healthy</span>
        </div>
        <div className="legend-item">
          <span className="legend-dot diseased"></span>
          <span>Disease detected</span>
        </div>
      </div>
    </div>
  )
}
