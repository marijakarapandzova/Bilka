import './Navigation.css'

export default function Navigation({ activeTab, setActiveTab }) {
  const tabs = [
    { label: 'All plants', value: 'all' },
    { label: 'Watering soon', value: 'watering' },
    { label: 'Needs attention', value: 'attention' },
    { label: 'Community Map', value: 'community' }
  ]

  return (
    <nav className="nav">
      <div className="brand">
        <div className="brand-mark">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <path d="M12 21C12 21 5 17.5 5 10.5C5 6 8 3 12 3C16 3 19 6 19 10.5C19 17.5 12 21 12 21Z" stroke="#FFFFFF" strokeWidth="1.6"/>
            <path d="M12 21V8" stroke="#FFFFFF" strokeWidth="1.6"/>
          </svg>
        </div>
        <div className="brand-name">My Garden</div>
      </div>
      <div className="nav-tabs">
        {tabs.map((tab) => (
          <button
            key={tab.value}
            className={`nav-tab ${activeTab === tab.value ? 'active' : ''}`}
            onClick={() => setActiveTab(tab.value)}
          >
            {tab.label}
          </button>
        ))}
      </div>
    </nav>
  )
}