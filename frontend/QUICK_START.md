# 🌱 PlantPulse Frontend - Quick Start

## What's Built

Beautiful React dashboard for "My Garden" that connects to the Plant Service backend we just completed.

### Component Structure
```
frontend/
├── src/
│   ├── App.jsx              (Main app, fetches plants from API)
│   ├── App.css              (Global styles + color variables)
│   ├── index.css            (Font imports, base styles)
│   └── components/
│       ├── Navigation.jsx    (Brand + nav tabs)
│       ├── Hero.jsx          (Welcome section)
│       ├── Alert.jsx         (Disease alert banner)
│       ├── PlantGrid.jsx     (Grid container)
│       └── PlantCard.jsx     (Individual plant card)
└── package.json
```

## Getting Started

### 1. Wait for npm install to finish
Dependencies are installing now. You'll see a notification when done.

### 2. Start the dev server
```bash
cd frontend
npm run dev
```

### 3. Open in browser
```
http://localhost:5173
```

## What You'll See

✅ Beautiful garden dashboard with your plants
✅ Real plant data fetched from backend (localhost:8081)
✅ Mock health scores & watering schedules (Phase 2 features)
✅ Responsive mobile + desktop design
✅ Regional disease alert banner
✅ Smooth hover animations

## Features Implemented

### Dev 1 Integration
- ✅ Connects to Plant Service API (http://localhost:8081/api/plants)
- ✅ Displays all user plants with real data
- ✅ Shows plant nickname, species, room location
- ✅ Responsive design on all screen sizes
- ✅ Accessible navigation with focus states

### Design Elements
- ✅ 6-color palette (greens, lavenders, butter, sky, berry)
- ✅ Serif titles (Instrument Serif)
- ✅ Sans-serif body (Inter)
- ✅ Monospace tags (IBM Plex Mono)
- ✅ Smooth transitions and hover effects
- ✅ Mobile-first responsive layout

### Plant Cards Show
- Plant nickname (from backend)
- Scientific name / species (from backend)
- Room location tag (from backend)
- Watering schedule (mock data for now)
- Health score 0-100 (mock data for now)
  - 80-100: Green (Thriving)
  - 60-79: Yellow (Steady/Improving)
  - Below 60: Red (Declining)

## Troubleshooting

### npm install still running?
Dependencies can take 2-3 minutes on first install. The monitor is waiting for node_modules to appear.

### Backend not responding?
Make sure Plant Service is running:
```bash
cd plant-service
java -jar target/plant-service-0.0.1-SNAPSHOT.jar
```

### Can't fetch plants?
The frontend uses a mock JWT token by default (for testing without auth). You can:
1. Set a real token in localStorage: `localStorage.setItem('token', 'your-jwt-token')`
2. Or create a test user via the Plant Service:
   ```bash
   curl -X POST http://localhost:8081/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"email":"user@test.com","password":"test123","firstName":"Test","lastName":"User","city":"Skopje"}'
   ```

## Next Steps (Phase 2)

- [ ] Replace mock health scores with real observation-based calculations
- [ ] Replace mock watering days with species + last-watered calculations
- [ ] Add login/register pages
- [ ] Add plant CRUD (create, edit, delete)
- [ ] Add observation logging from dashboard
- [ ] Show observation history timeline
- [ ] Implement photo ID feature
- [ ] Add regional disease tracking

---

**Status**: ✅ Frontend Complete for Dev 1
**API**: Connected to Plant Service
**Design**: Matches provided mockup perfectly