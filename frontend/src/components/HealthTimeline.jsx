import { useState, useEffect } from 'react'
import { authService } from '../services/authService'
import './HealthTimeline.css'

export default function HealthTimeline({ plant, token }) {
  const [history, setHistory] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (plant?.id) {
      fetchHistory()
    }
  }, [plant])

  const fetchHistory = async () => {
    try {
      setLoading(true)
      const authToken = token || await authService.getToken()
      if (!authToken) {
        setLoading(false)
        return
      }
      const response = await fetch(`http://localhost:8082/api/health/plants/${plant.id}/history`, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      })
      if (response.ok) {
        const data = await response.json()
        setHistory(Array.isArray(data) ? data : [])
      }
    } catch (err) {
      console.error('Failed to fetch health history:', err)
    } finally {
      setLoading(false)
    }
  }

  const getStatusEmoji = (status) => {
    const emojis = {
      'HEALTHY': '🟢',
      'STABLE': '🔵',
      'DECLINING': '🔴',
      'RECOVERING': '🟡',
      'CRITICAL': '⚫'
    }
    return emojis[status] || '⚪'
  }

  const getStatusColor = (status) => {
    const colors = {
      'HEALTHY': '#10b981',
      'STABLE': '#3b82f6',
      'DECLINING': '#ef4444',
      'RECOVERING': '#f59e0b',
      'CRITICAL': '#6b7280'
    }
    return colors[status] || '#9ca3af'
  }

  if (loading) {
    return <div className="timeline-loading">Loading health timeline...</div>
  }

  if (!history || history.length === 0) {
    return (
      <div className="timeline-empty">
        <p>No health history yet. Start logging observations!</p>
      </div>
    )
  }

  // Calculate SVG dimensions
  const width = 800
  const height = 300
  const padding = 60
  const graphWidth = width - padding * 2
  const graphHeight = height - padding - 40

  // Get min/max for scaling
  const scores = history.map(h => h.healthScore || 50)
  const minScore = Math.min(...scores)
  const maxScore = Math.max(...scores)
  const scoreRange = maxScore - minScore || 10

  // Calculate points for line chart
  const points = history.map((item, idx) => {
    const x = padding + (idx / (history.length - 1 || 1)) * graphWidth
    const normalized = (item.healthScore - minScore) / scoreRange
    const y = padding + graphHeight - normalized * graphHeight
    return { x, y, ...item }
  })

  // Build path for line
  const pathData = points.map((p, idx) => `${idx === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ')

  return (
    <div className="health-timeline">
      <h3>📊 Health Timeline</h3>
      <div className="timeline-chart">
        <svg viewBox={`0 0 ${width} ${height}`} className="timeline-svg">
          {/* Grid lines */}
          {[0, 25, 50, 75, 100].map((val) => {
            const y = padding + graphHeight - (val / 100) * graphHeight
            return (
              <g key={`grid-${val}`}>
                <line x1={padding} y1={y} x2={width - padding} y2={y} stroke="#e5e7eb" strokeDasharray="4" />
                <text x={padding - 40} y={y + 5} textAnchor="end" fontSize="12" fill="#9ca3af">
                  {val}
                </text>
              </g>
            )
          })}

          {/* Y-axis label */}
          <text x="20" y={height / 2} textAnchor="middle" fontSize="14" fill="#6b7280" transform={`rotate(-90 20 ${height / 2})`}>
            Health Score
          </text>

          {/* X-axis */}
          <line x1={padding} y1={height - 40} x2={width - padding} y2={height - 40} stroke="#d1d5db" strokeWidth="2" />

          {/* Line chart */}
          <path d={pathData} fill="none" stroke="#10b981" strokeWidth="3" className="chart-line" />

          {/* Data points */}
          {points.map((point, idx) => (
            <g key={`point-${idx}`}>
              <circle
                cx={point.x}
                cy={point.y}
                r="6"
                fill={getStatusColor(point.status)}
                stroke="white"
                strokeWidth="2"
                className="chart-point"
              />
              <title>{`${point.healthScore}/100 - ${point.status}`}</title>
            </g>
          ))}

          {/* X-axis labels */}
          {points.map((point, idx) => {
            if (idx % Math.ceil(points.length / 5) === 0 || idx === points.length - 1) {
              const date = new Date(point.recordedAt)
              const label = `${date.getMonth() + 1}/${date.getDate()}`
              return (
                <text
                  key={`label-${idx}`}
                  x={point.x}
                  y={height - 15}
                  textAnchor="middle"
                  fontSize="12"
                  fill="#6b7280"
                >
                  {label}
                </text>
              )
            }
            return null
          })}
        </svg>
      </div>

      {/* Timeline list */}
      <div className="timeline-list">
        {history.map((item, idx) => (
          <div key={idx} className="timeline-item">
            <div className="timeline-marker" style={{ backgroundColor: getStatusColor(item.status) }}>
              {getStatusEmoji(item.status)}
            </div>
            <div className="timeline-content">
              <div className="timeline-score">
                <span className="score-value">{item.healthScore}/100</span>
                <span className="score-status">{item.status}</span>
              </div>
              <div className="timeline-date">
                {new Date(item.recordedAt).toLocaleDateString()} {new Date(item.recordedAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
              </div>
              {item.diseaseName && (
                <div className="timeline-disease">
                  🦠 {item.diseaseName} ({item.diseaseMatchPercentage}%)
                </div>
              )}
              {item.notes && (
                <div className="timeline-notes">
                  "{item.notes}"
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
