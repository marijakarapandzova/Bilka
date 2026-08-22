# PlantPulse - Service-Oriented Architecture Project

A comprehensive microservices application demonstrating modern SOA patterns including service discovery, API gateway, event-driven architecture, security with Keycloak, and consumer-driven contract testing.

##  Overview

PlantPulse is a plant health monitoring system built with microservices architecture. It consists of:

- **Plant Service**: Manages plants and observations
- **Health Service**: Tracks plant health and detects outbreaks
- **API Gateway**: Centralized entry point with OAuth2/OIDC security
- **Supporting Infrastructure**: Kafka, Consul, Keycloak, OpenLDAP

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    API Gateway (8000)                   │
│         Spring Cloud Gateway + OAuth2 + Consul          │
└────────────┬──────────────────────────────┬─────────────┘
             │                              │
    ┌────────▼────────┐          ┌─────────▼──────────┐
    │ Plant Service   │          │ Health Service     │
    │ Port: 8081      │          │ Port: 9001         │
    │ - REST APIs     │          │ - Health APIs      │
    │ - Event Pub     │◄─────────┤ - Event Consume    │
    │ - Consul Reg    │  Kafka   │ - Feign Client     │
    └────────┬────────┘          └─────────┬──────────┘
             │                              │
    ┌────────▼──────────────────────────────▼────────┐
    │           Kafka (Port 9092)                    │
    │  • plant.added                                 │
    │  • observation.logged                          │
    │  • plant.watered                               │
    │  • plant.removed                               │
    └──────────────────────────────────────────────┘
             │                              │
    ┌────────▼────────┐          ┌─────────▼──────────┐
    │ Consul (8500)   │          │ Keycloak (8090)    │
    │ Service Reg     │          │ OAuth2/OIDC        │
    │ Discovery       │          │ JWT Tokens         │
    └─────────────────┘          └────────┬───────────┘
                                          │
                                 ┌────────▼──────────┐
                                 │ OpenLDAP (389)    │
                                 │ User Federation   │
                                 └───────────────────┘
```

##  Quick Start

### Prerequisites

- **Java 17+** (JDK installed)
- **Maven 3.8+** (for building)
- **Docker & Docker Compose** (for infrastructure)
- **Git** (for cloning)

### 1. Clone Repository

```bash
git clone https://github.com/yourusername/bilka.git
cd Bilka
```

### 2. Start Infrastructure (Docker)

```bash
docker-compose up -d
```

This starts:
- Kafka broker
- Consul service registry
- Keycloak identity provider
-  OpenLDAP directory
-  PostgreSQL databases

**Verify services are running**:
```bash
docker ps
```

Expected output: 5+ containers running

### 3. Build Microservices

```bash
# Build Plant Service
cd plant-service
mvn clean package -DskipTests

# Build Health Service
cd ../health-service
mvn clean package -DskipTests

# Build API Gateway
cd ../api-gateway
mvn clean package -DskipTests
```

### 4. Start Microservices

**Terminal 1 - Plant Service**:
```bash
cd plant-service
mvn spring-boot:run
```
Expected output: `PlantPulse Plant Service started on port 8081`

**Terminal 2 - Health Service**:
```bash
cd health-service
mvn spring-boot:run
```
Expected output: `PlantPulse Health Service started on port 8082` (internal), `9001` (external)

**Terminal 3 - API Gateway**:
```bash
cd api-gateway
mvn spring-boot:run
```
Expected output: `API Gateway started on port 8001` (internal), `8000` (external)

### 5. Verify Everything is Running

```bash
# Check Consul UI
open http://localhost:8500/ui

# Check Keycloak Admin
open http://localhost:8090/admin
# Login: admin / admin

# Check API Gateway Health
curl http://localhost:8000/actuator/health

# Check Swagger UI (Plant Service)
open http://localhost:8081/swagger-ui.html

# Check Swagger UI (Health Service)
open http://localhost:9001/swagger-ui.html
```

---

##  Access Points

| Service | URL | Purpose |
|---------|-----|---------|
| **API Gateway** | http://localhost:8000 | Main entry point |
| **Plant Service** | http://localhost:8081 | Plant management APIs |
| **Plant Swagger** | http://localhost:8081/swagger-ui.html | API documentation |
| **Plant AsyncAPI** | http://localhost:8081/springwolf/asyncapi-ui.html | Event documentation |
| **Health Service** | http://localhost:9001 | Health tracking APIs |
| **Health Swagger** | http://localhost:9001/swagger-ui.html | API documentation |
| **Consul** | http://localhost:8500/ui | Service discovery UI |
| **Keycloak Admin** | http://localhost:8090/admin | Identity management |
| **Keycloak Realms** | http://localhost:8090/realms/finki-services/.well-known/openid-configuration | OIDC configuration |

---

##  Authentication

### Default Users

#### Keycloak Admin
- **Username**: `admin`
- **Password**: `admin`

#### Test Users (via LDAP)
- **Ben** (admin role)
  - Username: `ben`
  - Password: `benspassword`

- **Alice** (user role)
  - Username: `alice`
  - Password: `alicespassword`

### Getting a Token

```bash
# Get JWT token
TOKEN=$(curl -s -X POST http://localhost:8090/realms/finki-services/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=gateway-tester&client_secret=gateway-tester-secret-change-in-production&grant_type=password&username=ben&password=benspassword" \
  | jq -r '.access_token')

echo $TOKEN

# Use token in requests
curl -H "Authorization: Bearer $TOKEN" http://localhost:8000/actuator/health
```

---

##  Running Tests

### Unit & Integration Tests

```bash
# Plant Service tests
cd plant-service
mvn test

# Health Service tests
cd health-service
mvn test

# API Gateway tests
cd api-gateway
mvn test
```

### Consumer-Driven Contract Tests (Pact)

```bash
# Generate consumer pacts (Health Service)
cd health-service
mvn test -Dtest='PactPlantService*,PactPlantEvent*'

# Copy pacts to provider
mkdir -p ../plant-service/src/test/resources/pacts
cp target/pacts/*.json ../plant-service/src/test/resources/pacts/

# Verify provider contracts (Plant Service)
cd ../plant-service
mvn test -Dtest='PactPlantService*,PactPlantEvent*'
```

Expected output: `12 tests run, 0 failures`

---

##  Example API Calls

### 1. Create a Plant

```bash
# Get token first
TOKEN=$(curl -s -X POST http://localhost:8090/realms/finki-services/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=gateway-tester&client_secret=gateway-tester-secret-change-in-production&grant_type=password&username=ben&password=benspassword" \
  | jq -r '.access_token')

# Create plant
curl -X POST http://localhost:8000/plant-service/api/plants \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "speciesId": "660e8400-e29b-41d4-a716-446655440000",
    "nickname": "My Monstera"
  }'
```

### 2. Get All Plants

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8000/plant-service/api/plants
```

### 3. Log Observation

```bash
curl -X POST http://localhost:8000/plant-service/api/observations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "plantId": "550e8400-e29b-41d4-a716-446655440000",
    "diseaseMatchName": "Leaf Spot",
    "diseaseMatchPercentage": 85,
    "cityLocation": "Skopje"
  }'
```

### 4. Get Health Status

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8000/health-service/health/plants
```

---

##  Configuration

### Environment Variables

**Plant Service** (`plant-service/application.yml`):
```yaml
spring.datasource.url: jdbc:postgresql://localhost:5433/plantdb
spring.cloud.consul.host: localhost
spring.cloud.consul.port: 8500
spring.kafka.bootstrap-servers: localhost:9092
```

**Health Service** (`health-service/application.yml`):
```yaml
spring.datasource.url: jdbc:postgresql://localhost:5434/healthdb
spring.cloud.consul.host: localhost
spring.cloud.consul.port: 8500
spring.kafka.bootstrap-servers: localhost:9092
```

**API Gateway** (`api-gateway/application.properties`):
```properties
spring.cloud.consul.host=localhost
spring.cloud.consul.port=8500
spring.cloud.gateway.discovery.locator.enabled=true
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8090/realms/finki-services
```

---

##  Troubleshooting

### Services Not Starting

**Problem**: Port already in use
```bash
# Find process using port
lsof -i :8000  # Check port 8000

# Kill process
kill -9 <PID>
```

**Problem**: Database connection refused
```bash
# Verify Docker containers running
docker ps | grep postgres

# Check container logs
docker logs <container-id>
```

### Services Not Registering with Consul

```bash
# Check Consul logs
docker logs consul

# Verify Consul is reachable
curl http://localhost:8500/v1/status/leader

# Check registered services
curl http://localhost:8500/v1/catalog/services
```

### JWT Token Issues

```bash
# Verify Keycloak is running
curl http://localhost:8090/auth/realms/finki-services

# Check realm configuration
curl http://localhost:8090/realms/finki-services/.well-known/openid-configuration
```

### Kafka Message Issues

```bash
# Check Kafka topics
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

# View messages in topic
docker exec kafka kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic plant.added --from-beginning
```

---


##  Project Structure

```
Bilka/
├── plant-service/              # Plant management service
│   ├── src/main/kotlin/
│   ├── src/test/kotlin/        # Pact tests
│   ├── pom.xml
│   └── docker-compose.yml
├── health-service/             # Health tracking service
│   ├── src/main/kotlin/
│   ├── src/test/kotlin/        # Pact tests
│   ├── pom.xml
│   └── docker-compose.yml
├── api-gateway/                # API Gateway
│   ├── src/main/java/
│   ├── src/test/java/
│   ├── keycloak/
│   │   └── import/
│   │       └── finki-services-realm.json
│   ├── ldap/
│   │   └── ldif/
│   │       └── init.ldif
│   ├── pom.xml
│   └── docker-compose.yml
├── docker-compose.yml          # Main infrastructure
└── README.md
```

---

## 🚀 Deployment

### Production Configuration

1. **Update Keycloak URL** in `application.properties`:
   ```properties
   spring.security.oauth2.resourceserver.jwt.issuer-uri=https://keycloak.youromain.com/realms/finki-services
   ```

2. **Update Database URLs**:
   ```yaml
   spring.datasource.url: jdbc:postgresql://prod-db:5432/plantdb
   ```

3. **Enable HTTPS**:
   ```yaml
   server.ssl.enabled: true
   server.ssl.key-store: classpath:keystore.p12
   ```

4. **Configure Consul Datacenter**:
   ```yaml
   spring.cloud.consul.datacenter: prod
   ```
