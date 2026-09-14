import './Hero.css'

export default function Hero({ plantsCount, onAddPlant }) {
  return (
    <section className="hero">
      <div className="hero-content">
        <div className="eyebrow">Field notes — Skopje</div>
        <h1 className="hero-title">
          Good afternoon.<br/>
          <span className="accent">One plant</span> could use your care today.
        </h1>
      </div>
      <div className="hero-footer">
        <p className="hero-sub">
          {plantsCount > 0
            ? `${plantsCount} specimen${plantsCount !== 1 ? 's' : ''} under watch. Watering, health, and a few notes on what's changed.`
            : 'Watering, health, and a few notes on what\'s changed since last week.'}
        </p>
        {onAddPlant && (
          <button className="btn-add-plant" onClick={onAddPlant}>
            <span className="btn-icon">+</span>
            Add Plant
          </button>
        )}
      </div>
    </section>
  )
}