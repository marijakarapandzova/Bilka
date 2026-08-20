import './Alert.css'

export default function Alert() {
  return (
    <div className="alert">
      <div className="alert-icon">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
          <path d="M12 9V13M12 17H12.01M10.29 3.86L1.82 18A2 2 0 0 0 3.55 21H20.45A2 2 0 0 0 22.18 18L13.71 3.86A2 2 0 0 0 10.29 3.86Z" stroke="#C15C74" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
      </div>
      <div className="alert-text">
        <strong>Root rot reported nearby</strong>
        <span>Four cases logged in your area this week — check drainage before your Monstera's next watering.</span>
      </div>
    </div>
  )
}