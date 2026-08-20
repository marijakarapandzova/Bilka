# PlantPulse Frontend - My Garden Dashboard

Beautiful React frontend for the PlantPulse Plant & User Service.

## Design Features

- 🎨 Modern, nature-inspired color palette
- 📱 Fully responsive (mobile, tablet, desktop)
- 🌱 Plant cards with health scores, watering schedules, room tags
- 📊 Health status indicators (Thriving, Steady, Declining)
- ⚠️ Regional alerts for disease outbreaks
- 🎯 Clean, semantic HTML structure
- ♿ Accessible navigation and focus states

## Setup

### Prerequisites
- Node.js 18+ (npm comes bundled)

### Installation

```bash
cd frontend
npm install
npm run dev
```

The app will be available at `http://localhost:5173`

### Build for Production

```bash
npm run build
npm run preview
```

## Architecture

### Components

- **App.jsx** - Main component, handles API calls to Plant Service
- **Navigation.jsx** - Top nav with brand and tabs
- **Hero.jsx** - Welcome section with dynamic greeting
- **Alert.jsx** - Regional alert banner for disease outbreaks
- **PlantGrid.jsx** - Grid container for plant cards
- **PlantCard.jsx** - Individual plant card with health, watering, room info

### API Integration

Connects to **Plant Service** backend at `http://localhost:8081`

**Endpoints used**:
- `GET /api/plants` - Fetch user's plants
- `GET /api/observations/{plantId}` - Fetch observation history (for Phase 2)

**Mock data**: Currently using random health scores and watering days (Phase 2 features)

## Design System

### Color Palette

```
Primary:
--green: #5FA758 (main brand)
--green-deep: #3F7C3F (accents)
--green-pale: #E3F1DC (badges)

Secondary:
--lavender: #B9AEE0
--butter: #EFC94C
--sky: #97C9D6
--berry: #C15C74

Neutral:
--bg: #F5F9F0
--card: #FFFFFF
--ink: #22301F
--ink-soft: #66765F
--track: #EAF0E4
```

### Typography

- **Serif**: Instrument Serif (headings, titles)
- **Sans**: Inter (body text)
- **Mono**: IBM Plex Mono (labels, tags)

## Dev 1 Features Integrated

✅ Fetches plants from backend API
✅ Displays real plant data (nickname, species, room)
✅ Mock health scores and watering schedules (Dev 2 features)
✅ Beautiful card design matching provided mockup
✅ Responsive mobile/desktop layout
✅ Disease alert banner

## Future Development (Phase 2)

- [ ] Authentication & login page
- [ ] Real health scores from observations
- [ ] Real watering schedule calculations
- [ ] Observation history timeline
- [ ] Photo-based plant identification
- [ ] Plant add/edit/delete functionality
- [ ] Regional disease outbreak tracking

## Environment Variables

Currently using mock authentication. For production:

```bash
VITE_API_URL=http://localhost:8081  # Plant Service backend
VITE_API_TOKEN=your-jwt-token       # Auth token (to be implemented)
```

## Notes

- Authentication is deferred to Phase 2
- Current implementation uses mock JWT token for testing
- Health scores and watering days are randomized (Phase 2 will calculate real values)
- API calls will fail gracefully if backend is unavailable

---

**Status**: ✅ Dev 1 Frontend Complete
**Next Steps**: Phase 2 authentication, real data calculations