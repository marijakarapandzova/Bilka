import { useState, useEffect } from 'react'
import './NotificationsPanel.css'

export default function NotificationsPanel({ token }) {
  const [notifications, setNotifications] = useState([])
  const [outbreaks, setOutbreaks] = useState([])
  const [showPanel, setShowPanel] = useState(false)
  const [city, setCity] = useState('Skopje') // Default city
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    fetchNotifications()
    fetchOutbreaks()
    // Refresh every 5 minutes
    const interval = setInterval(() => {
      fetchNotifications()
      fetchOutbreaks()
    }, 5 * 60 * 1000)
    return () => clearInterval(interval)
  }, [city])

  const fetchNotifications = async () => {
    try {
      const response = await fetch('http://localhost:9001/api/notifications', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      if (response.ok) {
        const data = await response.json()
        // Get unread notifications (or first 10)
        const notifs = data.content || data
        setNotifications(Array.isArray(notifs) ? notifs.slice(0, 10) : [])
      }
    } catch (err) {
      console.error('Failed to fetch notifications:', err)
    }
  }

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

  const unreadCount = notifications.filter(n => !n.read).length

  const markAsRead = async (notifId) => {
    try {
      const response = await fetch(`http://localhost:9001/api/notifications/${notifId}/read`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      if (response.ok) {
        // Update local state
        setNotifications(notifications.map(n =>
          n.id === notifId ? { ...n, read: true } : n
        ))
      }
    } catch (err) {
      console.error('Failed to mark notification as read:', err)
    }
  }

  return (
    <>
      {/* Notification Bell Button */}
      <button
        className="notification-bell"
        onClick={() => setShowPanel(!showPanel)}
        title="Alerts"
      >
        <span className="bell-icon">Alerts</span>
        {unreadCount > 0 && <span className="notification-badge">{unreadCount}</span>}
      </button>

      {/* Notifications Panel */}
      {showPanel && (
        <div className="notifications-panel">
          <div className="panel-header">
            <h3>Alerts & Reminders</h3>
            <button className="panel-close" onClick={() => setShowPanel(false)}>×</button>
          </div>

          <div className="panel-content">
            {/* Outbreak Alerts */}
            {outbreaks.length > 0 && (
              <div className="outbreak-section">
                <h4 className="section-title">Regional Disease Alerts</h4>
                {outbreaks.map((outbreak, idx) => (
                  <div key={idx} className="outbreak-alert">
                    <div className="alert-badge">Alert</div>
                    <div className="alert-content">
                      <p className="alert-title">{outbreak.diseaseName}</p>
                      <p className="alert-message">
                        {outbreak.affectedPlantCount} plant{outbreak.affectedPlantCount !== 1 ? 's' : ''} affected in {outbreak.cityLocation}
                      </p>
                      <p className="alert-time">
                        {new Date(outbreak.detectedAt).toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            )}

            {/* Plant Notifications */}
            {notifications.length > 0 && (
              <div className="reminders-section">
                <h4 className="section-title">Plant Notifications</h4>
                {notifications.map((notif, idx) => (
                  <div
                    key={idx}
                    className={`notification-item ${notif.read ? 'read' : 'unread'}`}
                    onClick={() => !notif.read && markAsRead(notif.id)}
                    style={{ cursor: notif.read ? 'default' : 'pointer' }}
                  >
                    <div className="notif-badge">{notif.type === 'DISEASE_ALERT' ? 'Alert' : 'Notification'}</div>
                    <div className="notif-content">
                      <p className="notif-title">{notif.title}</p>
                      <p className="notif-message">{notif.message}</p>
                      <p className="notif-time">
                        {new Date(notif.createdAt).toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            )}

            {/* Empty State */}
            {notifications.length === 0 && outbreaks.length === 0 && (
              <div className="empty-notifications">
                <p>No active alerts</p>
                <p>All plants are healthy</p>
              </div>
            )}
          </div>

          <button className="refresh-btn" onClick={() => {
            setLoading(true)
            fetchNotifications()
            fetchOutbreaks()
            setTimeout(() => setLoading(false), 500)
          }}>
            Refresh
          </button>
        </div>
      )}
    </>
  )
}
