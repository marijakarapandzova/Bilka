# Frontend Authentication Setup

## Overview

The frontend now has a complete authentication system with:
- ✅ Login page
- ✅ Registration page  
- ✅ Token management (localStorage)
- ✅ Automatic logout on 401 errors
- ✅ User menu with logout button
- ✅ Secure API calls with token headers

## How It Works

### Authentication Flow

```
1. User visits frontend
   ↓
2. No token in localStorage → Show Login/Register page
   ↓
3. User enters credentials → Sends to Plant Service /api/auth/login or /api/auth/register
   ↓
4. Backend returns JWT token
   ↓
5. Frontend stores token in localStorage
   ↓
6. All API calls include: Authorization: Bearer <token>
   ↓
7. If backend returns 401 (unauthorized) → Clear token and show Login page
```

## Getting Started

### 1. Make Sure Backend is Running

```bash
# Terminal 1 - Plant Service
cd plant-service
mvn spring-boot:run

# Terminal 2 - Health Service  
cd health-service
mvn spring-boot:run

# Terminal 3 - API Gateway (optional for development)
cd api-gateway
mvn spring-boot:run
```

### 2. Start Frontend

```bash
cd frontend
npm run dev
```

Then open: http://localhost:5173

### 3. Test Login

#### Option A: Demo Account
```
Email: ben@example.com
Password: benspassword
```

#### Option B: Create New Account
1. Click "Create one" on login page
2. Enter any email and password (min 6 chars)
3. Passwords must match
4. Click "Create Account"
5. You're automatically logged in!

## Files Created/Modified

### New Files
```
frontend/src/services/authService.js     # Auth API calls
frontend/src/pages/Login.jsx             # Login page
frontend/src/pages/Register.jsx          # Registration page
frontend/src/pages/Auth.css              # Auth page styling
```

### Modified Files
```
frontend/src/App.jsx                     # Added auth state management
frontend/src/components/Navigation.jsx   # Added user menu + logout
frontend/src/components/Navigation.css   # Added user menu styling
```

## How to Use the Auth Service

### In Components

```javascript
import { authService } from '../services/authService'

// Check if user is logged in
if (authService.isAuthenticated()) {
  // Do something
}

// Get current user info
const token = authService.getToken()
const email = authService.getUserEmail()
const userId = authService.getUserId()

// Logout
authService.logout()

// Make authenticated API call
const response = await fetch('http://localhost:8081/api/plants', {
  headers: {
    'Authorization': `Bearer ${authService.getToken()}`,
    'Content-Type': 'application/json'
  }
})
```

## Key Features

### 1. Automatic Token Handling
- Token is stored in `localStorage` as `token`
- User email stored as `userEmail`
- User ID stored as `userId`

### 2. Secure API Calls
All API calls in App.jsx use `getAuthHeaders()`:
```javascript
const getAuthHeaders = () => {
  const token = authService.getToken()
  return {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
}
```

### 3. Automatic Logout on 401
If backend returns 401 (unauthorized):
```javascript
if (response.status === 401) {
  console.error('Unauthorized - logging out')
  handleLogout()
  return
}
```

### 4. Error Handling
- Login/register errors show in red banner
- API errors show in error message
- Users can dismiss error messages with X button

### 5. User Menu
- Click user email in top-right to open menu
- Shows current email
- "Sign Out" button logs out and returns to login page

## Testing

### Test Scenario 1: Login with Demo Account
1. Go to http://localhost:5173
2. Click on email field
3. Enter: `ben@example.com`
4. Enter password: `benspassword`
5. Click "Sign In"
6. ✅ Should see your plants loaded

### Test Scenario 2: Register New Account
1. Go to http://localhost:5173
2. Click "Create one"
3. Enter new email (e.g., `test@example.com`)
4. Enter password (min 6 chars)
5. Confirm password
6. Click "Create Account"
7. ✅ Should be logged in immediately

### Test Scenario 3: Token Persistence
1. Login successfully
2. Refresh the page (F5 or Cmd+R)
3. ✅ Should stay logged in (token in localStorage)

### Test Scenario 4: Token Expiration
1. Login as ben@example.com
2. Open browser DevTools → Application → Storage → localStorage
3. Find `token` and delete it
4. Try to perform an action (add plant, water plant, etc.)
5. ✅ Should automatically logout and show login page

### Test Scenario 5: Wrong Credentials
1. Try to login with wrong password
2. ✅ Should show error message

### Test Scenario 6: Invalid Email Format
1. Try to register with invalid email
2. ✅ Should show validation error

## Debugging

### Check if Authenticated
Open browser console and run:
```javascript
localStorage.getItem('token')  // Should return JWT token
localStorage.getItem('userEmail')  // Should return email
```

### Check API Calls
Open browser DevTools → Network tab and look for:
```
Plant Service API calls: http://localhost:8081/api/*
Health Service API calls: http://localhost:8082/api/*
```

Watch the Authorization header:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Check Console Logs
The app logs:
```
Auth check - Authenticated: true/false
Fetching species...
✅ Fetched species: 50
Fetching plants...
✅ Fetched plants: 3
✅ Health data synced for Monstera: 85
```

## Common Issues & Solutions

### Issue: "Failed to fetch plants: 401"
**Cause:** Token expired or invalid  
**Solution:** Logout and login again

### Issue: Plant Service API not responding
**Cause:** Plant Service not running  
**Solution:** 
```bash
cd plant-service
mvn spring-boot:run
```

### Issue: Login button disabled
**Cause:** Form is submitting  
**Solution:** Wait for response, check console for errors

### Issue: Plants not loading after login
**Cause:** Health Service not running or network error  
**Solution:** 
```bash
# Check both services are running
curl -H "Authorization: Bearer <token>" http://localhost:8081/api/plants
curl -H "Authorization: Bearer <token>" http://localhost:8082/api/health/plants
```

### Issue: "Can't reach server" errors
**Cause:** CORS or backend not running  
**Solution:**
1. Make sure Plant Service is running on port 8081
2. Check backend logs for errors
3. Check browser console for CORS errors

## Production Checklist

- [ ] Change `API_BASE` to production backend URL
- [ ] Store token in secure httpOnly cookie (if possible)
- [ ] Add token refresh logic (currently uses single token)
- [ ] Add password reset functionality
- [ ] Add email verification
- [ ] Add rate limiting for login/register
- [ ] Add 2FA (two-factor authentication)
- [ ] Use HTTPS only
- [ ] Set secure cookie flags

## Next Steps

1. **Token Refresh:** Currently, token lasts 24 hours. Add refresh token logic.
2. **Password Reset:** Add forgot password flow
3. **Email Verification:** Verify email before account activation
4. **Social Login:** Add Google/GitHub OAuth
5. **Profile Page:** Let users update their profile
6. **2FA:** Add two-factor authentication

---

**Happy planting! 🌱**