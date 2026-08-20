









# PlantPulse Development Status

## 🎯 Project Vision
PlantPulse is a smart plant care companion that:
- Logs plant observations (leaf color, soil moisture, pests, etc.)
- Detects diseases automatically (91% accuracy on symptoms)
- Calculates health scores (0-100)
- Sends watering reminders
- Detects regional disease outbreaks
- Tracks plant health history over time

---

## ✅ PLANT-SERVICE (Dev 1) - Phase 1 Complete

### What's Working:
- ✅ Species management (POST/GET `/api/species`)
- ✅ Plant CRUD operations (POST/GET/PATCH/DELETE `/api/plants`)
- ✅ Observation logging (POST `/api/plants/{id}/observations`)
- ✅ Observation history (GET `/api/plants/{id}/observations`)
- ✅ Basic disease matching (returns "Healthy" + 100% confidence)
- ✅ H2 in-memory database for testing
- ✅ Swagger API documentation
- ✅ Security config (currently public for testing)

### Current API Endpoints:
```
POST   /api/species                          - Create species
GET    /api/species?query=...                - Search species
GET    /api/species/{id}                     - Get species by ID

POST   /api/plants/manual                    - Create plant manually
GET    /api/plants                           - Get user's garden
GET    /api/plants/{id}                      - Get plant details
PATCH  /api/plants/{id}                      - Update plant
DELETE /api/plants/{id}                      - Delete plant

POST   /api/plants/{plantId}/observations    - Log observation
GET    /api/plants/{plantId}/observations    - Get observation history
```

---

## ❌ Still Needed - Plant-Service

### Priority 1 (MVP Features):
1. **Health Score System**
   - Calculate health 0-100 based on observation history
   - Rules: leaf color, texture, soil moisture, pest presence affect score
   - Store health score in plant entity

2. **Disease Detection Algorithm**
   - Replace "Healthy" hardcoded response
   - Match symptoms: leaf color (YELLOW/BROWN) + texture (WILTING/MUSHY) = specific diseases
   - Return diseases with treatment steps

3. **Watering Schedule**
   - Calculate next watering date based on:
     - Species watering frequency (days)
     - Last observation's soil moisture
   - Add `nextWateringDate` field to PlantResponse

4. **Health Timeline**
   - Store health score snapshots when observations logged
   - GET `/api/plants/{id}/health-history` - returns timeline data
   - Used for the Jan 95 → Apr 41 → May 58 graph

### Priority 2 (Enhanced Features):
5. **User Location Integration**
   - Use existing user location (city) data
   - Required for regional alerts

6. **Disease Pattern Analysis**
   - Aggregate observations across region
   - Detect if disease is spreading in user's city

7. **Image-Based Plant Identification** (Already partially done)
   - POST `/api/plants/by-photo` endpoint exists
   - Uses PlantIdClient for external API

---

## 🔄 Other Microservices Needed

### Alert Service
- Listens for regional disease outbreaks
- Sends notifications to at-risk users
- Publishes: `/api/alerts/regional`

### Analytics Service
- Aggregates observation data across all users
- Detects disease patterns by region
- Calculates regional statistics

### Notification Service
- Sends push notifications
- Email alerts
- In-app notifications

### User Service
- User profile management
- Location/city data
- Settings & preferences

---

## 🗄️ Database Schema (Current)

### Species Table
```
- id (UUID) - Primary Key
- name (String)
- scientificName (String)
- careDifficulty (ENUM: EASY, MODERATE, DIFFICULT)
- lightNeeds (ENUM: LOW, MEDIUM, BRIGHT_INDIRECT, DIRECT_SUN)
- wateringFrequencyDays (Integer)
- description (String, nullable)
```

### Plants Table
```
- id (UUID) - Primary Key
- userId (UUID) - Foreign Key
- speciesId (UUID) - Foreign Key
- nickname (String)
- room (String, nullable)
- currentPhotoUrl (String, nullable)
- addedAt (Timestamp)
```

### Observations Table
```
- id (UUID) - Primary Key
- plantId (UUID) - Foreign Key
- leafColor (ENUM: GREEN, YELLOW, BROWN, SPOTTED)
- leafTexture (ENUM: HEALTHY, CRISPY, WILTING, MUSHY)
- soilMoisture (ENUM: DRY, MOIST, WATERLOGGED)
- growth (ENUM: NONE, NORMAL, SLOW, STUNTED)
- visiblePests (Boolean)
- notes (String, nullable)
- photoUrl (String, nullable)
- loggedAt (Timestamp)
```

### New Table Needed: PlantHealth (for timeline)
```
- id (UUID) - Primary Key
- plantId (UUID) - Foreign Key
- healthScore (Integer: 0-100)
- observationDate (Date)
- status (ENUM: HEALTHY, DECLINING, RECOVERING, CRITICAL)
```

---

## 🛠️ Tech Stack

- **Language**: Kotlin
- **Framework**: Spring Boot 3.4.1
- **Database**: H2 (dev) / PostgreSQL (prod)
- **JPA**: Hibernate
- **API Docs**: Springdoc OpenAPI (Swagger UI)
- **Java Version**: 21 LTS
- **Build Tool**: Maven

---

## 📝 Next Steps

### Option A: Complete MVP (Recommended)
1. Implement Health Score calculation
2. Improve Disease Detection algorithm
3. Add Watering Schedule logic
4. Create Health Timeline endpoint

### Option B: Design Other Microservices First
1. Design Alert Service API
2. Design Analytics Service API
3. Design Notification Service API
4. Plan inter-service communication (Kafka/REST)

### Which path would you like to take?

---

## 🚀 Testing Status
- ✅ All endpoints tested manually via Swagger
- ✅ Basic CRUD operations working
- ✅ Database persistence working
- ❌ Unit tests - Not yet written
- ❌ Integration tests - Not yet written
- ❌ Load testing - Not done

---

## 🔐 Security Notes
- Currently: All endpoints public (`.anyRequest().permitAll()`)
- TODO: Implement JWT authentication properly
- TODO: Add role-based access control
- TODO: Validate user ownership of plants/observations

---

## 📞 Questions?
Ask about:
- Priority of remaining features
- Architecture for other microservices
- Database migrations strategy
- Testing approach
- Deployment strategy
