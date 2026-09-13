import { useState, useEffect, useRef } from 'react'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import '../styles/CommunityMap.css'

const CITY_COORDINATES = {
  'New York': [40.7128, -74.0060],
  'Los Angeles': [34.0522, -118.2437],
  'Chicago': [41.8781, -87.6298],
  'Houston': [29.7604, -95.3698],
  'Phoenix': [33.4484, -112.0742],
  'San Francisco': [37.7749, -122.4194],
  'Seattle': [47.6062, -122.3321],
  'Denver': [39.7392, -104.9903],
  'Boston': [42.3601, -71.0589],
  'Miami': [25.7617, -80.1918],
  'London': [51.5074, -0.1278],
  'Paris': [48.8566, 2.3522],
  'Berlin': [52.5200, 13.4050],
  'Amsterdam': [52.3676, 4.9041],
  'Barcelona': [41.3851, 2.1734],
  'Rome': [41.9028, 12.4964],
  'Madrid': [40.4168, -3.7038],
  'Vienna': [48.2082, 16.3738],
  'Prague': [50.0755, 14.4378],
  'Budapest': [47.4979, 19.0402],
  'Tokyo': [35.6762, 139.6503],
  'Seoul': [37.5665, 126.9780],
  'Shanghai': [31.2304, 121.4737],
  'Beijing': [39.9042, 116.4074],
  'Hong Kong': [22.3193, 114.1694],
  'Bangkok': [13.7563, 100.5018],
  'Singapore': [1.3521, 103.8198],
  'Sydney': [33.8688, 151.2093],
  'Melbourne': [-37.8136, 144.9631],
  'Auckland': [-37.0882, 174.8860],
  'Mumbai': [19.0760, 72.8777],
  'Delhi': [28.7041, 77.1025],
  'Bangalore': [12.9716, 77.5946],
  'Dubai': [25.2048, 55.2708],
  'Abu Dhabi': [24.4539, 54.3773],
  'Cairo': [30.0444, 31.2357],
  'São Paulo': [-23.5505, -46.6333],
  'Rio de Janeiro': [-22.9068, -43.1729],
  'Mexico City': [19.4326, -99.1332],
  'Toronto': [43.6532, -79.3832],
  'Vancouver': [49.2827, -123.1207],
  'Istanbul': [41.0082, 28.9784],
  'Moscow': [55.7558, 37.6173],
  'St. Petersburg': [59.9311, 30.3609],
  'Athens': [37.9838, 23.7275],
  'Lisbon': [38.7223, -9.1393],
  'Stockholm': [59.3293, 18.0686],
  'Zurich': [47.3769, 8.5472],
  'Milan': [45.4642, 9.1900],
  'Venice': [45.4408, 12.3155],
  'Florence': [43.7696, 11.2558],
  'Reykjavik': [64.1466, -21.9426],
  'Dublin': [53.3498, -6.2603],
  'Edinburgh': [55.9533, -3.1883],
  'Skopje': [41.9973, 21.4280],
  'Bitola': [41.0144, 21.3280],
  'Kumanovo': [42.1327, 21.7097],
  'Ohrid': [41.1186, 20.7986],
  'Tetovo': [42.0081, 20.9731],
  'Veles': [41.7198, 21.7631],
  'Stip': [41.7451, 21.7727],
  'Kavadarci': [41.4231, 21.9044],
  'Prilep': [41.3506, 21.5475],
  'Strumica': [41.4431, 22.6420],
}

export default function CommunityMap({ token }) {
  const mapContainer = useRef(null)
  const map = useRef(null)
  const [cities, setCities] = useState([])
  const [loading, setLoading] = useState(true)
  const [selectedCity, setSelectedCity] = useState(null)
  const [cityPlants, setCityPlants] = useState([])
  const [showDetails, setShowDetails] = useState(false)

  useEffect(() => {
    fetchCities()
    initMap()
  }, [])

  useEffect(() => {
    if (selectedCity) {
      fetchCityPlants(selectedCity)
    }
  }, [selectedCity])

  const initMap = () => {
    if (map.current) return

    map.current = L.map(mapContainer.current).setView([20, 0], 2)

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
      maxZoom: 19,
    }).addTo(map.current)
  }

  const fetchCities = async () => {
    setLoading(true)
    try {
      const response = await fetch('http://localhost:8081/api/shared-plants/cities', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      if (response.ok) {
        const data = await response.json()
        setCities(data)
        addMarkers(data)
      }
    } catch (err) {
      console.error('Failed to fetch cities:', err)
    } finally {
      setLoading(false)
    }
  }

  const addMarkers = (citiesList) => {
    if (!map.current) return

    citiesList.forEach(city => {
      const coords = CITY_COORDINATES[city]
      if (coords) {
        const marker = L.circleMarker(coords, {
          radius: 12,
          fillColor: '#3F7C3F',
          color: '#1F4F1F',
          weight: 2,
          opacity: 0.8,
          fillOpacity: 0.7
        })
          .bindPopup(`<strong>${city}</strong><br><a href="#" onclick="window.selectCity('${city}')">View plants</a>`)
          .addTo(map.current)

        marker.on('click', () => {
          setSelectedCity(city)
          setShowDetails(true)
        })
      }
    })
  }

  const fetchCityPlants = async (city) => {
    try {
      const response = await fetch(`http://localhost:8081/api/shared-plants/city/${encodeURIComponent(city)}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      if (response.ok) {
        const data = await response.json()
        setCityPlants(data.plants || [])
      }
    } catch (err) {
      console.error('Failed to fetch city plants:', err)
    }
  }

  window.selectCity = (city) => {
    setSelectedCity(city)
    setShowDetails(true)
  }

  return (
    <div className="community-container">
      <div className="map-section">
        <h1>Plant Community Map</h1>
        <div ref={mapContainer} className="map-container" />
      </div>

      {showDetails && selectedCity && (
        <div className="city-panel">
          <div className="panel-header">
            <h2>{selectedCity}</h2>
            <button className="close-btn" onClick={() => setShowDetails(false)}>✕</button>
          </div>

          <div className="plants-list">
            {loading ? (
              <p className="loading">Loading plants...</p>
            ) : cityPlants.length === 0 ? (
              <p className="empty">No plants shared in {selectedCity} yet</p>
            ) : (
              cityPlants.map(plant => (
                <div key={plant.id} className="plant-card">
                  {plant.photoUrl && (
                    <img src={plant.photoUrl} alt={plant.plantName} className="plant-img" />
                  )}
                  <div className="plant-info">
                    <h3>{plant.plantName}</h3>
                    <p className="species">{plant.speciesName}</p>
                    <p className="user">Shared by {plant.userName}</p>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  )
}
