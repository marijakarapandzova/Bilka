# User Authentication & Location-Based Community Features
## Implementation Plan

---

## **Overview**

Implement user registration with mandatory location, login authentication, personal plant dashboards, and a community map showing only publicly-shared plants from other users.

---

## **Phase 1: Database Schema Updates**

### **1.1 Users Table (NEW)**
```sql
CREATE TABLE users (
  id UUID PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  location VARCHAR(255) NOT NULL,        -- City name (required)
  latitude DECIMAL(10,8),                -- Optional for future GPS
  longitude DECIMAL(11,8),
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);
```

### **1.2 Plants Table Changes**
```sql
ALTER TABLE plants ADD COLUMN (
  user_id UUID NOT NULL REFERENCES users(id),
  is_public BOOLEAN DEFAULT FALSE,       -- For community sharing
  posted_at TIMESTAMP                    -- When user shared to community
);

CREATE INDEX idx_plants_user_id ON plants(user_id);
CREATE INDEX idx_plants_public ON plants(is_public) WHERE is_public = TRUE;
```

### **1.3 Observations Table Updates**
```sql
ALTER TABLE observations ADD COLUMN (
  user_location VARCHAR(255)             -- Capture user's location at time of observation
);
```

---

## **Phase 2: Backend API Endpoints**

### **Plant Service Updates**

#### **2.1 Authentication Endpoints**

**POST /api/auth/register**
```json
Request:
{
  "email": "marija@example.com",
  "password": "secure_password",
  "location": "Skopje"
}

Response (201):
{
  "userId": "uuid",
  "email": "marija@example.com",
  "location": "Skopje",
  "token": "jwt_token"
}
```

**POST /api/auth/login**
```json
Request:
{
  "email": "marija@example.com",
  "password": "secure_password"
}

Response (200):
{
  "userId": "uuid",
  "email": "marija@example.com",
  "location": "Skopje",
  "token": "jwt_token"
}
```

**GET /api/auth/profile** (Requires auth)
```json
Response (200):
{
  "userId": "uuid",
  "email": "marija@example.com",
  "location": "Skopje"
}
```

#### **2.2 Plant Management Endpoints**

**GET /api/plants** (My Plants - Requires auth)
```json
Response (200):
[
  {
    "id": "plant_uuid",
    "nickname": "My Succulent",
    "speciesId": "species_uuid",
    "speciesName": "Aloe Vera",
    "userId": "user_uuid",
    "location": "Skopje",
    "isPublic": false,
    "wateringFrequencyDays": 7,
    "lastWateredAt": "2026-09-12T10:00:00Z"
  }
]
```

**POST /api/plants/:plantId/share** (Post to Community - Requires auth)
```json
Request: {} (empty body)

Response (200):
{
  "id": "plant_uuid",
  "nickname": "My Succulent",
  "isPublic": true,
  "postedAt": "2026-09-12T16:00:00Z"
}
```

**POST /api/plants/:plantId/unshare** (Remove from Community - Requires auth)
```json
Response (200):
{
  "isPublic": false
}
```

#### **2.3 Community Map Endpoints**

**GET /api/community/plants** (All Public Plants - Requires auth)
```json
Request Query:
  ?location=Skopje (optional filter)

Response (200):
[
  {
    "id": "plant_uuid",
    "nickname": "My Succulent",
    "speciesName": "Aloe Vera",
    "userId": "other_user_uuid",
    "userEmail": "other_user@example.com",
    "userLocation": "Skopje",
    "health": 85,
    "healthStatus": "HEALTHY",
    "lastObservedAt": "2026-09-12T15:00:00Z",
    "postedAt": "2026-09-12T14:00:00Z"
  }
]
```

**GET /api/community/outbreaks** (Regional Outbreaks - Requires auth)
```json
Request Query:
  ?location=Skopje (required)

Response (200):
[
  {
    "id": "outbreak_uuid",
    "diseaseName": "Root Rot",
    "affectedPlantCount": 3,
    "location": "Skopje",
    "detectedAt": "2026-09-12T16:00:00Z",
    "affectedPlants": [
      {
        "plantId": "uuid1",
        "nickname": "Plant A",
        "userLocation": "Skopje"
      }
    ]
  }
]
```

---

## **Phase 3: Frontend Changes**

### **3.1 Navigation Changes**

**Before:**
```
Home | Watering | Attention | Community | Observations | Alerts
```

**After (Logged In):**
```
Dashboard | My Plants | Community Map | Observations | Profile | Logout
```

**After (Not Logged In):**
```
Register | Login
```

### **3.2 New Pages**

#### **3.2.1 Register Page** (/register)
```jsx
Form Fields:
- Email (text input, required, unique validation)
- Password (password input, required, min 8 chars)
- Location (text input, required) 
  └─ Placeholder: "e.g., Skopje, Sofia, Belgrade"
- Terms checkbox (required)

Buttons:
- Register (POST /api/auth/register)
- Already have account? Login
```

#### **3.2.2 Login Page** (/login)
```jsx
Form Fields:
- Email (text input, required)
- Password (password input, required)

Buttons:
- Login (POST /api/auth/login)
- Don't have account? Register
- Remember me (optional)

Success: Redirect to Dashboard
Failure: Show error message
```

#### **3.2.3 Profile Page** (/profile)
```jsx
Display:
- Email: marija@example.com (read-only)
- Location: Skopje (editable)
- Member since: Sept 12, 2026
- Total plants: 5
- Plants shared: 2

Actions:
- Edit Location
- Change Password
- Logout
```

#### **3.2.4 Community Map Page** (/community)
```jsx
Layout:
- Left Sidebar:
  └─ Location Filter (dropdown or text input)
  └─ Disease Filter (optional)
  └─ List of public plants from that location
  
- Main Area:
  └─ Map view (optional: actual map showing locations)
  └─ Outbreak alerts for selected location
  └─ Cards showing each public plant:
    ├─ Plant name
    ├─ Owner (anonymized or partial: "marija@...")
    ├─ Health status (with color coding)
    ├─ Last observed
    └─ Disease info

Search/Filter:
- "Showing 8 plants in Skopje"
- Regional outbreaks: "3 cases of Root Rot detected"
```

### **3.3 Dashboard Page Changes** (/dashboard or /)

**Before (Current):**
- Hardcoded "Skopje" location
- All plants shown
- No user context

**After (With Auth):**
```jsx
Header:
- "Welcome, Marija" (from user context)
- "Location: Skopje" (from user profile)
- "Manage Location" link → Profile page

Sections:
1. My Plants (filtered to current user)
   - Plant cards with share/unshare button
   - "Share to Community" button if not shared
   - "Unshare" button if already shared

2. Quick Stats
   - Total plants: 5
   - Healthy: 3
   - At risk: 1
   - Public plants: 2

3. Recent Activity
   - Observations logged today
   - Plants added this week
```

### **3.4 Plant Card Updates**

```jsx
If Logged In:
- Add "Share to Community" button (if isPublic = false)
- Add "Unshare from Community" button (if isPublic = true)
- Show "Shared 2 days ago" badge if shared

If Not Logged In:
- Show "Login to share" message
```

---

## **Phase 4: Authentication Flow**

### **4.1 Login Flow**
```
User → Register/Login Page
  ↓
POST /api/auth/register (or /login)
  ↓
Backend validates email/password
  ↓
Generate JWT token
  ↓
Store token in localStorage
  ↓
Redirect to Dashboard
  ↓
All subsequent requests include token in Authorization header
```

### **4.2 Token Storage**
```javascript
// localStorage
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "userId": "uuid",
  "email": "marija@example.com",
  "location": "Skopje"
}
```

### **4.3 Protected Routes**
```jsx
// Frontend routing
{
  path: '/dashboard',
  component: Dashboard,
  protected: true  // Redirect to /login if no token
}

{
  path: '/community',
  component: CommunityMap,
  protected: true
}

{
  path: '/login',
  component: Login,
  public: true
}
```

---

## **Phase 5: Data Migration Strategy**

### **5.1 Existing Plants Migration**

**Problem:** Current plants have no user association

**Solution:**
```sql
-- Create default admin user
INSERT INTO users VALUES (
  'admin-uuid',
  'admin@plantpulse.com',
  'hashed_password',
  'Skopje',
  NULL, NULL,
  NOW(), NOW()
);

-- Migrate existing plants to admin user
UPDATE plants 
SET user_id = 'admin-uuid'
WHERE user_id IS NULL;
```

### **5.2 Backward Compatibility**
- Keep mock token (`'mock-token-dev'`) working in development
- Default to "Skopje" if no location provided
- Existing observations still work without user_location
- Plants without user_id not shown in community map

---

## **Phase 6: Health Service Updates**

### **6.1 Location-Aware Outbreak Detection**

**Current:**
```kotlin
checkCityForDisease(location: String, diseaseName: String)
```

**Updated:**
```kotlin
checkCityForDisease(
  location: String,
  diseaseName: String,
  userId: UUID,              // NEW: Track which user reported
  userEmail: String          // NEW: For community map display
)
```

### **6.2 Regional Outbreak Queries**

**New Endpoint:** `GET /api/outbreaks?location=Skopje`

```sql
SELECT 
  o.disease_name,
  COUNT(DISTINCT o.plant_id) as affected_count,
  o.location,
  MAX(o.detected_at) as latest_detection
FROM outbreaks o
WHERE o.location = 'Skopje'
  AND o.detected_at > NOW() - INTERVAL 7 DAY
GROUP BY o.disease_name, o.location
ORDER BY affected_count DESC;
```

---

## **Implementation Timeline**

| Phase | Components | Effort | Duration |
|-------|-----------|--------|----------|
| 1 | Database schema | 2 hrs | Day 1 |
| 2 | Backend endpoints (Auth + Plants) | 4 hrs | Day 1-2 |
| 3 | Frontend pages (Register, Login, Profile, Community) | 6 hrs | Day 2-3 |
| 4 | Auth flow + protected routes | 2 hrs | Day 3 |
| 5 | Data migration | 1 hr | Day 3 |
| 6 | Health Service updates | 2 hrs | Day 4 |
| **Total** | | **17 hrs** | **~4 days** |

---

## **Testing Checklist**

- [ ] Register new user with location
- [ ] Login with created account
- [ ] See only own plants in dashboard
- [ ] Share a plant to community
- [ ] Unshare a plant from community
- [ ] View community map (all public plants in region)
- [ ] View regional outbreaks
- [ ] Filter by location
- [ ] Verify old plants still work
- [ ] Mock token still works in dev
- [ ] Token expires and requires re-login
- [ ] Logged out user cannot access protected pages

---

## **Security Considerations**

- ✅ Password hashing (use bcrypt)
- ✅ JWT token expiration (24 hours)
- ✅ HTTPS only in production
- ✅ CORS configured for frontend
- ✅ Rate limiting on auth endpoints
- ✅ Location is public but email can be anonymized in community view
- ✅ Users can only modify their own plants

---

## **Future Enhancements**

1. **OAuth2 with Keycloak** - Replace email/password login
2. **GPS Coordinates** - Auto-detect user location
3. **Plant Visibility Settings** - Public/Private/Friends-only
4. **User Profiles** - Show user's all public plants
5. **Following System** - Follow other gardeners
6. **Regional Statistics** - Disease trends by region
7. **Notifications** - Alert when nearby outbreak detected

---

**Ready to implement? Start with Phase 1 (Database) → Phase 2 (Backend) → Phase 3 (Frontend)**
