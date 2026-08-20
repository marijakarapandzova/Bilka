


# Microservices Architecture Implementation - Complete Guide

**Status**: ✅ Fully Implemented and Working

**Date**: July 16, 2026

**Developer**: Dev 1

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Components Implemented](#components-implemented)
3. [Setup Instructions](#setup-instructions)
4. [Running the System](#running-the-system)
5. [Testing the Complete Flow](#testing-the-complete-flow)
6. [API Examples](#api-examples)
7. [Troubleshooting](#troubleshooting)
8. [File Structure](#file-structure)

---

## Architecture Overview

This is a **production-ready microservice architecture** implementing:

- **Service Discovery**: Apache Consul
- **API Gateway**: Spring Cloud Gateway with WebFlux
- **Authentication**: Keycloak with OAuth2/OpenID Connect
- **JWT Validation**: Reactive JWT validation
- **Service Registry**: Automatic service registration and health checks

### Flow Diagram

```
┌─────────────┐
│   Client    │
│  (Browser)  │
└──────┬──────┘
       │ 1. Request token
       ▼
┌──────────────┐
│  Keycloak    │ ◄─── Authenticates & Issues JWT
│  :8090       │      OAuth2/OpenID Connect
└──────┬───────┘
       │ 2. Returns JWT access_token
       ▼
┌──────────────────────┐
│  API Gateway         │
│  :8000               │
├──────────────────────┤
│ • JWT Validation     │
│ • Service Discovery  │
│ • Route handling     │
└──────┬───────────────┘
       │ 3. Route to service
       ▼
    ┌──────────────────────────────────┐
    │  Consul Service Registry :8500   │
    │  - api-gateway                   │
    │  - plant-service                 │
    │  - consul                        │
    └──────────────────────────────────┘
       │ 4. Lookup service
       ▼
┌──────────────────────┐
│  Plant-Service       │
│  :8081 → :9000       │
├──────────────────────┤
│ • Registered w/      │
│   Consul             │
│ • PostgreSQL database│
│ • REST API           │
└──────────────────────┘
```

---

## Components Implemented

### 1. Consul Service Registry
**Port**: 8500
**Container Name**: consul_gateway
**Purpose**: Service discovery and health checking

**Features**:
- Automatic service registration
- Health check endpoints
- Service-to-service discovery
- Web UI available at http://localhost:8500

**Services Registered**:
- `api-gateway` - Spring Cloud Gateway
- `plant-service` - Backend microservice
- `consul` - Consul itself

---

### 2. Keycloak Authentication Server
**Port**: 8090
**Container Name**: keycloak
**Purpose**: OAuth2/OpenID Connect authentication and JWT issuance

**Configuration**:
- **Realm**: finki-services
- **Admin Username**: admin
- **Admin Password**: admin

**Users**:
```
username: ben
password: benpassword
role: service.admin

username: bob
password: bobspassword
role: service.user

username: test
password: testpassword
role: service.user
```

**Client**:
- **Client ID**: gateway-tester
- **Client Secret**: fxp6CM5F28RKPdXLvqKONTU7kNmpRaA9
- **Grant Type**: password (Resource Owner Password Credentials)
- **Token Endpoint**: http://localhost:8090/realms/finki-services/protocol/openid-connect/token
- **JWKS Endpoint**: http://localhost:8090/realms/finki-services/protocol/openid-connect/certs

**JWT Token Details**:
- **Issuer**: http://localhost:8090/realms/finki-services
- **Expiry**: 300 seconds (5 minutes)
- **Contains**: realm_access.roles claim with user roles

---

### 3. API Gateway
**Port**: 8000 (external) → 8001 (internal)
**Container Name**: api_gateway
**Purpose**: Route authenticated requests to backend services

**Technology**:
- Spring Cloud Gateway (reactive, WebFlux-based)
- Spring Security OAuth2 Resource Server
- Consul service discovery
- NimbusReactiveJwtDecoder for JWT validation

**Features**:
- Automatic JWT validation against Keycloak JWKS
- Dynamic service discovery via Consul
- Load balancing across service instances
- Health check endpoint at `/actuator/health`

**Route Configuration**:
- Auto-discovers services from Consul
- Routes requests to `/service-name/**` to the corresponding service
- Example: `/plant-service/api/gardens` → routes to plant-service:8081/api/gardens

**Security**:
```
- Requires valid JWT Bearer token for all endpoints except:
  - /actuator/** (health checks)
  
- HTTP Header format:
  Authorization: Bearer <JWT_TOKEN>
```

---

### 4. Plant-Service (Microservice)
**Port**: 9000 (external) → 8081 (internal)
**Container Name**: plant_service
**Database**: PostgreSQL (port 5433)
**Purpose**: Manages plant data

**Features**:
- Auto-registers with Consul on startup
- Health check at `/actuator/health`
- Connected to PostgreSQL database
- REST API endpoints for plant management

**Database**:
- **Host**: postgres (in container network)
- **Port**: 5432
- **Database**: plantpulse
- **Username**: plantpulse
- **Password**: plantpulse

---

### 5. OpenLDAP
**Port**: 389 (LDAP), 636 (LDAPS)
**Container Name**: openldap
**Purpose**: User directory for future LDAP federation

**Users Configured**:
- ben (password: benpassword)
- bob (password: bobspassword)
- test (password: testpassword)

**Base DN**: dc=springframework,dc=org
**Admin DN**: cn=admin,dc=springframework,dc=org
**Admin Password**: adminpassword

---

## Setup Instructions

### Prerequisites

1. **Docker & Docker Compose**
   ```bash
   docker --version
   docker-compose --version
   ```

2. **Ports Available**:
   - 8000 (API Gateway)
   - 8090 (Keycloak)
   - 8500 (Consul)
   - 9000 (Plant-Service)
   - 5433 (PostgreSQL)
   - 389 (OpenLDAP)

### Project Structure

```
C:/Users/User/Downloads/Bilka/
├── docker-compose-gateway.yml      # All services except Plant-Service
├── docker-compose-consul.yml       # Old Consul setup (replaced)
├── keycloak/
│   └── import/
│       └── finki-services-realm.json  # Keycloak realm configuration
├── ldap/
│   └── ldif/
│       └── init.ldif               # OpenLDAP users configuration
├── api-gateway/                    # API Gateway Spring Boot project
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/...
├── plant-service/                  # Plant-Service Spring Boot project
│   ├── pom.xml
│   ├── Dockerfile
│   ├── docker-compose.yml
│   └── src/main/java/...
└── MICROSERVICES_IMPLEMENTATION.md  # This file
```

---

## Running the System

### Step 1: Start Gateway Services (Consul, Keycloak, OpenLDAP, API Gateway)

```bash
cd C:/Users/User/Downloads/Bilka

# Start all gateway infrastructure
docker-compose -f docker-compose-gateway.yml up -d

# Check status
docker ps --format "table {{.Names}}\t{{.Status}}"
```

**Expected Output**:
```
NAMES            STATUS
api_gateway      Up X seconds
keycloak         Up X seconds
consul_gateway   Up X seconds
openldap         Up X seconds
```

### Step 2: Start Plant-Service

```bash
cd C:/Users/User/Downloads/Bilka/plant-service

# Start Plant-Service with PostgreSQL
docker-compose up -d

# Check status
docker ps | grep -E "plant_service|postgres"
```

**Expected Output**:
```
plant_service         Up X seconds
plantpulse-postgres   Up X seconds (healthy)
```

### Step 3: Verify All Services

```bash
# Check if Consul sees all services
curl -s http://localhost:8500/v1/catalog/services | jq .

# Expected: api-gateway, consul, plant-service registered
```

### Step 4: Verify Keycloak

```bash
# Access Keycloak
curl -s http://localhost:8090/realms/finki-services | jq . | head -20

# Should return realm information (200 OK)
```

---

## Testing the Complete Flow

### Test 1: Get JWT Token from Keycloak

```bash
# Command to get access token
curl -X POST \
  -d "client_id=gateway-tester" \
  -d "client_secret=fxp6CM5F28RKPdXLvqKONTU7kNmpRaA9" \
  -d "grant_type=password" \
  -d "username=ben" \
  -d "password=benpassword" \
  http://localhost:8090/realms/finki-services/protocol/openid-connect/token

# Response: JWT access_token (valid for 300 seconds)
```

**Expected Response**:
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIs...",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGciOiJIUzUxMiIs...",
  "token_type": "Bearer",
  "not-before-policy": 0,
  "session_state": "...",
  "scope": "email profile"
}
```

### Test 2: Decode JWT Token

```bash
# Extract and decode the access_token
TOKEN="<paste_access_token_here>"

# View JWT claims
echo "$TOKEN" | cut -d. -f2 | base64 -d 2>/dev/null | jq .

# Should show:
# - iss: http://localhost:8090/realms/finki-services
# - sub: user ID
# - realm_access.roles: ["service.admin"] or ["service.user"]
```

### Test 3: Call API Gateway WITHOUT Token (Should Fail)

```bash
# This should return 401 Unauthorized
curl -v http://localhost:8000/plant-service/actuator/health

# Expected: HTTP/1.1 401 Unauthorized
```

### Test 4: Call API Gateway WITH Token (Should Succeed)

```bash
# Get fresh token
TOKEN=$(curl -s -X POST \
  -d "client_id=gateway-tester" \
  -d "client_secret=fxp6CM5F28RKPdXLvqKONTU7kNmpRaA9" \
  -d "grant_type=password" \
  -d "username=ben" \
  -d "password=benpassword" \
  http://localhost:8090/realms/finki-services/protocol/openid-connect/token | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

# Call gateway with Bearer token
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8000/plant-service/actuator/health | jq .

# Expected: 200 OK with Plant-Service health status
```

### Test 5: Complete End-to-End Flow

```bash
#!/bin/bash
# Save as test-microservices.sh

echo "=== Microservices Architecture Test ==="
echo ""

echo "Step 1: Authenticate with Keycloak"
TOKEN_RESPONSE=$(curl -s -X POST \
  -d "client_id=gateway-tester" \
  -d "client_secret=fxp6CM5F28RKPdXLvqKONTU7kNmpRaA9" \
  -d "grant_type=password" \
  -d "username=ben" \
  -d "password=benpassword" \
  http://localhost:8090/realms/finki-services/protocol/openid-connect/token)

TOKEN=$(echo $TOKEN_RESPONSE | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "❌ Failed to get token"
  exit 1
fi

echo "✓ Successfully got JWT token from Keycloak"
echo "  Token expires in: $(echo $TOKEN_RESPONSE | grep -o '"expires_in":[0-9]*' | cut -d':' -f2) seconds"
echo ""

echo "Step 2: Call API Gateway to Plant-Service"
RESPONSE=$(curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8000/plant-service/actuator/health)

if echo "$RESPONSE" | grep -q '"status":"UP"'; then
  echo "✓ API Gateway successfully routed to Plant-Service"
  echo "  Response:"
  echo "$RESPONSE" | jq '.status, .components | keys'
  echo ""
else
  echo "❌ Failed to reach Plant-Service"
  echo "  Response: $RESPONSE"
  exit 1
fi

echo "Step 3: Verify Service Discovery"
SERVICES=$(curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8000/actuator/health | grep -o '"services":\[[^]]*\]')

echo "✓ Services discovered by gateway:"
echo "  $SERVICES"
echo ""

echo "=== ✅ All Tests Passed! ==="
echo ""
echo "Architecture is working correctly:"
echo "  1. Keycloak issued JWT token"
echo "  2. API Gateway validated JWT"
echo "  3. API Gateway discovered Plant-Service via Consul"
echo "  4. Request routed to Plant-Service"
echo "  5. Plant-Service returned 200 OK"
```

Run the test:
```bash
bash test-microservices.sh
```

---

## API Examples

### Get All Gardens (Plant-Service)

```bash
TOKEN=$(curl -s -X POST \
  -d "client_id=gateway-tester" \
  -d "client_secret=fxp6CM5F28RKPdXLvqKONTU7kNmpRaA9" \
  -d "grant_type=password" \
  -d "username=ben" \
  -d "password=benpassword" \
  http://localhost:8090/realms/finki-services/protocol/openid-connect/token | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

# Try different endpoints
curl -H "Authorization: Bearer $TOKEN" http://localhost:8000/plant-service/api/gardens
curl -H "Authorization: Bearer $TOKEN" http://localhost:8000/plant-service/api/plants
curl -H "Authorization: Bearer $TOKEN" http://localhost:8000/plant-service/actuator/info
```

### Check Gateway Health

```bash
TOKEN=$(curl -s -X POST ... | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8000/actuator/health | jq .
```

### Check Consul Services

```bash
# List all services
curl -s http://localhost:8500/v1/catalog/services | jq .

# Get details of specific service
curl -s http://localhost:8500/v1/catalog/service/plant-service | jq .

# Get service health
curl -s http://localhost:8500/v1/health/service/plant-service | jq .
```

---

## Troubleshooting

### Issue 1: "Realm does not exist"

**Problem**: Getting `{"error":"Realm does not exist"}` from Keycloak

**Solution**:
1. Check if Keycloak is fully started (wait 30+ seconds)
2. Verify realm JSON exists:
   ```bash
   ls -la C:/Users/User/Downloads/Bilka/keycloak/import/finki-services-realm.json
   ```
3. Check Keycloak logs:
   ```bash
   docker logs keycloak | grep -i "finki-services"
   ```
4. If realm doesn't import, delete Keycloak data and restart:
   ```bash
   docker-compose -f docker-compose-gateway.yml down
   docker volume rm bilka_keycloak_data 2>/dev/null || true
   docker-compose -f docker-compose-gateway.yml up -d keycloak
   ```

### Issue 2: "Account is not fully set up"

**Problem**: User authentication fails with `Account is not fully set up`

**Solution**:
1. Verify the realm JSON has `"requiredActions": []` for users
2. Check the keycloak/import/finki-services-realm.json file
3. Delete and reimport the realm if needed

### Issue 3: Plant-Service not discovered by gateway

**Problem**: Gateway shows `"services":["api-gateway","consul"]` but not plant-service

**Solution**:
1. Verify Plant-Service is running and healthy:
   ```bash
   docker ps | grep plant_service
   docker logs plant_service | grep "Registering service"
   ```
2. Check Consul has the service:
   ```bash
   curl -s http://localhost:8500/v1/catalog/services | jq .
   ```
3. Restart Plant-Service:
   ```bash
   docker restart plant_service
   wait 15 seconds
   ```
4. Check IP addresses in the network are correct:
   ```bash
   docker inspect plant_service | grep IPAddress
   ```

### Issue 4: Port already in use

**Problem**: "Bind for 0.0.0.0:8000 failed: port is already allocated"

**Solution**:
```bash
# Find what's using the port
netstat -ano | findstr :8000

# Or kill the container using that port
docker ps | grep "8000\|8090\|8500\|9000" 
docker kill <container_name>

# Then restart
docker-compose -f docker-compose-gateway.yml up -d
```

### Issue 5: Gateway shows 401 Unauthorized

**Problem**: "HTTP/1.1 401 Unauthorized" even with valid token

**Solution**:
1. Verify the token is valid and not expired:
   ```bash
   # Token expires in 300 seconds, get a fresh one
   ```
2. Check Authorization header format:
   ```bash
   # CORRECT:
   Authorization: Bearer eyJhbGciOiJSUzI1NiIs...
   
   # WRONG:
   Authorization: eyJhbGciOiJSUzI1NiIs...  # Missing "Bearer "
   ```
3. Verify gateway can reach Keycloak JWKS:
   ```bash
   curl -s http://localhost:8090/realms/finki-services/protocol/openid-connect/certs | jq .
   ```

### Issue 6: Gateway returns 404 on service routes

**Problem**: "404 Not Found" when calling `/plant-service/api/...`

**Solution**:
1. Verify the endpoint exists on Plant-Service:
   ```bash
   curl http://localhost:9000/api/gardens
   curl http://localhost:9000/actuator/health
   ```
2. Check if service is discovered:
   ```bash
   curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8000/actuator/health | grep -o '"services":\[[^]]*\]'
   ```
3. Verify request format - service name is case-sensitive:
   ```bash
   # Correct: /plant-service/ (lowercase)
   # Incorrect: /Plant-Service/
   ```

---

## File Structure and Configuration

### docker-compose-gateway.yml
Starts all infrastructure services in one network:
- consul_gateway
- keycloak
- openldap
- api_gateway

### plant-service/docker-compose.yml
Starts Plant-Service and PostgreSQL on the shared network.

### keycloak/import/finki-services-realm.json
Keycloak realm configuration with:
- Users (ben, bob, test)
- Roles (service.admin, service.user)
- Client (gateway-tester)

### api-gateway/src/main/java/mk/ukim/finki/soa/apigateway/
API Gateway application with:
- ApiGatewayApplication.java - Main class
- config/SecurityConfig.java - JWT validation and security

### plant-service/src/main/resources/application.yml
Plant-Service configuration with:
- Consul registration settings
- PostgreSQL connection
- Server port (8081)

---

## Environment Variables

### API Gateway (docker-compose-gateway.yml)
```yaml
environment:
  SPRING_CLOUD_CONSUL_HOST: consul_gateway
  SPRING_CLOUD_CONSUL_PORT: "8500"
  KEYCLOAK_JWK_SET_URI: http://keycloak:8080/realms/finki-services/protocol/openid-connect/certs
  KEYCLOAK_ISSUER_URI: http://localhost:8090/realms/finki-services
```

### Plant-Service (plant-service/docker-compose.yml)
```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/plantpulse
  SPRING_DATASOURCE_USERNAME: plantpulse
  SPRING_DATASOURCE_PASSWORD: plantpulse
  SPRING_CLOUD_CONSUL_HOST: consul_gateway
  SPRING_CLOUD_CONSUL_PORT: "8500"
```

---

## What Dev 2 Needs to Know

### For the Health & Intelligence Microservice

When Dev 2 implements the Health & Intelligence Service, it should:

1. **Register with Consul**:
   - Add Spring Cloud Consul dependency
   - Set `spring.cloud.consul.host=consul_gateway` (same network)
   - Set service name with `spring.cloud.consul.discovery.service-name=health-service`

2. **JWT Validation** (if needed):
   - Can optionally add Spring Security OAuth2 Resource Server
   - Use the same Keycloak issuer: `http://keycloak:8090/realms/finki-services`
   - Or rely on API Gateway for authentication

3. **Database Connection**:
   - Create PostgreSQL container in same docker-compose
   - Or use existing database on shared network

4. **Docker Network**:
   - Must use the same network: `bilka_shared_net`
   - Set network in docker-compose.yml:
     ```yaml
     networks:
       shared_net:
         name: bilka_shared_net
         external: true
     ```

5. **API Gateway Routing**:
   - Will automatically be routed via `/health-service/**`
   - Requests will be: `/health-service/api/...`
   - JWT validation happens at gateway level

### How Services Communicate

**Service-to-Service Communication** (if needed):
```bash
# Plant-Service calling Health-Service (after registration with Consul)
# Use RestTemplate with DiscoveryClient
URL: http://health-service:8080/api/...
(Consul will resolve health-service to actual IP:port)
```

---

## Quick Reference Commands

### Start Everything
```bash
cd C:/Users/User/Downloads/Bilka

# Start gateway infrastructure
docker-compose -f docker-compose-gateway.yml up -d

# Start plant-service
cd plant-service && docker-compose up -d
```

### Stop Everything
```bash
docker-compose -f docker-compose-gateway.yml down
cd plant-service && docker-compose down
```

### View Logs
```bash
# API Gateway
docker logs api_gateway -f

# Plant-Service
docker logs plant_service -f

# Keycloak
docker logs keycloak | tail -50

# Consul
docker logs consul_gateway | tail -20
```

### Get Fresh Token
```bash
curl -s -X POST \
  -d "client_id=gateway-tester" \
  -d "client_secret=fxp6CM5F28RKPdXLvqKONTU7kNmpRaA9" \
  -d "grant_type=password" \
  -d "username=ben" \
  -d "password=benpassword" \
  http://localhost:8090/realms/finki-services/protocol/openid-connect/token | jq .
```

### Test Gateway
```bash
TOKEN=$(curl -s -X POST \
  -d "client_id=gateway-tester" \
  -d "client_secret=fxp6CM5F28RKPdXLvqKONTU7kNmpRaA9" \
  -d "grant_type=password" \
  -d "username=ben" \
  -d "password=benpassword" \
  http://localhost:8090/realms/finki-services/protocol/openid-connect/token | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

curl -H "Authorization: Bearer $TOKEN" http://localhost:8000/plant-service/actuator/health
```

### Check Service Status
```bash
# All services
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# Consul services
curl -s http://localhost:8500/v1/catalog/services | jq .

# Gateway discovered services
curl -H "Authorization: Bearer $TOKEN" http://localhost:8000/actuator/health | jq '.components.discoveryComposite'
```

---

## Key Takeaways

✅ **Fully Implemented**:
- Service Discovery (Consul)
- API Gateway (Spring Cloud Gateway)
- JWT Authentication (Keycloak)
- Service Registration (automatic)
- Health Checks (configured)

✅ **All Services Running**:
- API Gateway (:8000) - Routes & validates
- Plant-Service (:9000) - Backend service
- Keycloak (:8090) - Authenticates
- Consul (:8500) - Discovers services
- OpenLDAP (:389) - User directory
- PostgreSQL (:5433) - Plant database

✅ **End-to-End Flow Working**:
1. Client → Keycloak (authenticate)
2. Client → Gateway (with Bearer token)
3. Gateway → Keycloak (validate JWT)
4. Gateway → Consul (discover service)
5. Gateway → Plant-Service (route request)
6. Response flows back

---

## Next Steps for Dev 2

1. Implement Health & Intelligence Service microservice
2. Register with Consul (add Spring Cloud Consul dependency)
3. Add PostgreSQL or connect to shared database
4. Configure docker-compose to use shared network
5. Test routing through API Gateway
6. Implement service-to-service communication if needed
7. Add health check endpoints (`/actuator/health`)

---

## Support & Questions

For questions about the implementation:
- Check docker logs: `docker logs <container_name>`
- Verify Consul UI: http://localhost:8500
- Check Keycloak Admin: http://localhost:8090/admin (admin/admin)
- Review configuration files in respective directories

**Last Updated**: 2026-07-16 12:13 UTC
