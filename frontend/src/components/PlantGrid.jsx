import './PlantGrid.css'
import PlantCard from './PlantCard'

export default function PlantGrid({ plants, onLogObservation, onShowDetails, onDelete, onShare, onWaterPlant }) {
  return (
    <>
      <div className="section-label">
        Specimens — {plants.length} plant{plants.length !== 1 ? 's' : ''}
      </div>
      <section className="grid">
        {plants.map((plant) => (
          <PlantCard key={plant.id} plant={plant} onLogObservation={onLogObservation} onShowDetails={onShowDetails} onDelete={onDelete} onShare={onShare} onWaterPlant={onWaterPlant} />
        ))}
      </section>
    </>
  )
}