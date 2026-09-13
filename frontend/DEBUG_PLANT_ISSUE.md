# Debug: Plant Not Appearing After Creation

## The Problem

1. ✅ Plant is added (shows "Plant added successfully!")
2. ✅ API call succeeds (200 OK)
3. ❌ Plant doesn't appear in the list
4. ❌ Fetching plants still returns 0

## Root Cause (Most Likely)

**User ID mismatch** - The plant is being created with one user ID, but being fetched with a different user ID.

## How to Debug

### Step 1: Check Your User ID in Browser Console

Open DevTools (F12) → Console tab and run:

```javascript
// Check stored user ID
console.log('Stored userId:', localStorage.getItem('userId'))
console.log('Stored userEmail:', localStorage.getItem('userEmail'))
console.log('Token:', localStorage.getItem('token'))

// Decode token to see what's inside
const token = localStorage.getItem('token')
const parts = token.split('.')
const payload = JSON.parse(atob(parts[1]))
console.log('Token payload:', payload)
```

**Expected output:**
```
Stored userId: 550e8400-e29b-41d4-a716-446655440001
Stored userEmail: ben@example.com
Token payload: {
  sub: "ben@example.com",
  userId: "550e8400-e29b-41d4-a716-446655440001",
  iat: 1694000000,
  exp: 1694086400
}
```

### Step 2: Check Backend Logs

Look at your Plant Service console output when you add a plant:

```bash
# Should see something like:
2026-09-13 14:30:45 INFO PlantService: Creating plant for userId: 550e8400-e29b-41d4-a716-446655440001
2026-09-13 14:30:46 INFO PlantService: Plant created: Plant(id=..., userId=550e8400...)
2026-09-13 14:30:46 INFO PlantEventPublisher: Publishing plant.added event for plant: ...
```

**If you don't see these logs, the plant might not be created at all.**

### Step 3: Make Raw API Calls to Test

Open browser console and run:

```javascript
const token = localStorage.getItem('token')

// Test 1: Get your plants
fetch('http://localhost:8081/api/plants', {
  headers: { 'Authorization': `Bearer ${token}` }
})
.then(r => r.json())
.then(data => console.log('Your plants:', data))

// Test 2: Create a plant
fetch('http://localhost:8081/api/plants/manual', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    speciesId: '660e8400-e29b-41d4-a716-446655440000', // Snake Plant
    nickname: 'Test Plant',
    room: 'Living Room'
  })
})
.then(r => r.json())
.then(data => console.log('Created plant:', data))

// Test 3: Check plants again
setTimeout(() => {
  fetch('http://localhost:8081/api/plants', {
    headers: { 'Authorization': `Bearer ${token}` }
  })
  .then(r => r.json())
  .then(data => console.log('Plants after creation:', data))
}, 500)
```

Watch the console for:
- ✅ Plants list (should show your created plant)
- ❌ Errors (401 unauthorized, 500 server error, etc.)

### Step 4: Check Health Service Kafka Events

The Health Service should also get the Kafka event when a plant is created:

```bash
# In Health Service logs, look for:
2026-09-13 14:30:46 INFO PlantEventListener: Received plant.added event for plantId: ...
2026-09-13 14:30:47 INFO PlantHealthProfileService: Created health profile for plant: ...
```

**If this doesn't appear, Kafka events might not be flowing.**

---

## Possible Issues & Fixes

### Issue 1: Token Doesn't Have userId Claim

**Symptom:** `Token payload` doesn't include `userId`  
**Cause:** Plant Service generating tokens without userId  
**Fix:** Check `JwtService.kt` - make sure it includes `userId` claim:

```kotlin
fun generateToken(email: String, userId: String): String {
  return Jwts.builder()
    .subject(email)
    .claim("userId", userId)  // ✅ Must be here
    .issuedAt(now)
    .expiration(expiry)
    .signWith(key)
    .compact()
}
```

### Issue 2: Plant Service Not Getting User ID from Token

**Symptom:** Plant created but with wrong userId  
**Cause:** Backend not extracting userId from token correctly  
**Fix:** Check `JwtAuthenticationFilter.kt` - should extract userId and set in SecurityContext

### Issue 3: Plant Service & Health Service Have Different Secrets

**Symptom:** Token works for creating plants but plants don't sync with Health Service  
**Cause:** Services using different JWT secrets  
**Fix:** Make sure `JWT_SECRET` is the same in both services:

```bash
# Both should use same secret:
export JWT_SECRET="your-secret-key-here"
```

### Issue 4: Kafka Not Delivering Events

**Symptom:** Plant created but Health Service doesn't see it  
**Cause:** Kafka broker down or topic not created  
**Fix:**

```bash
# Check Kafka topics
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

# Should show: plant.added, plant.removed, observation.logged, plant.watered

# Check Kafka logs
docker logs kafka | tail -50
```

---

## Quick Fix Steps

Try these in order:

### Step 1: Verify Token Has userId
```javascript
const token = localStorage.getItem('token')
const payload = JSON.parse(atob(token.split('.')[1]))
console.log('Has userId?', !!payload.userId)
```

### Step 2: Restart Services with Same JWT Secret
```bash
# Terminal 1
export JWT_SECRET="test-secret-key-change-me"
cd plant-service && mvn spring-boot:run

# Terminal 2  
export JWT_SECRET="test-secret-key-change-me"
cd health-service && mvn spring-boot:run
```

### Step 3: Clear LocalStorage & Re-Login
```javascript
// In browser console:
localStorage.clear()
// Then refresh page and login again
```

### Step 4: Check Plant Service Logs
When you add a plant, watch for:
```
PlantService: Creating plant for userId: ...
PlantEventPublisher: Publishing plant.added event
```

If you see errors, share them with me.

---

## What to Report Back

Run these in browser console and share the output:

```javascript
// 1. Check token
const token = localStorage.getItem('token')
const payload = JSON.parse(atob(token.split('.')[1]))
console.log('=== TOKEN ===')
console.log('Email:', payload.sub)
console.log('UserId:', payload.userId)
console.log('Expires:', new Date(payload.exp * 1000))

// 2. Get your plants
console.log('=== FETCHING PLANTS ===')
fetch('http://localhost:8081/api/plants', {
  headers: { 'Authorization': `Bearer ${token}` }
})
.then(r => r.json())
.then(data => console.log('Plants:', data.length, data))

// 3. Get all users' plants (to see if plant was created under wrong user)
console.log('=== DEBUG: Checking if plant exists ===')
fetch('http://localhost:8081/api/plants?userId=ALL', {
  headers: { 'Authorization': `Bearer ${token}` }
})
.then(r => r.json())
.then(data => console.log('All plants:', data))
.catch(e => console.log('Error:', e))
```

Share the console output with me and I can help fix it!