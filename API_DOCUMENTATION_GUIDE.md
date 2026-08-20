# Exercise 7 - API Documentation & Docker Containerization

**Status**: ✅ Completed

**Date**: July 16, 2026

---

## Overview

This exercise demonstrates OpenAPI documentation for REST endpoints. We've added Swagger UI to Plant-Service so that all endpoints are automatically documented and discoverable. This allows other developers and teams to understand the API without reading code.

---

## What Was Implemented

### 1. OpenAPI/Swagger Documentation (COMPLETED)

#### Dependency
SpringDoc OpenAPI is already included in `pom.xml`:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.5</version>
</dependency>
```

#### Configuration
Already enabled in `application.yml`:
```yaml
springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
```

#### Annotations Added

All Plant-Service controllers now have:

**@Tag** — Groups endpoints by feature:
```kotlin
@RestController
@RequestMapping("/api/plants")
@Tag(name = "Plant API", description = "Manage your plants and garden")
class PlantController { ... }
```

**@Operation** — Documents individual endpoints:
```kotlin
@GetMapping
@Operation(
    summary = "Get my garden",
    description = "Retrieve all plants in your garden"
)
fun myGarden(): List<PlantResponse> = ...
```

#### Updated Controllers

1. **AuthController** (`/api/auth`)
   - Register user
   - Login user

2. **PlantController** (`/api/plants`)
   - Add plant by photo
   - Add plant manually
   - Get my garden
   - Get plant details
   - Update plant
   - Delete plant
   - Log watering

3. **SpeciesController** (`/api/species`)
   - Search species
   - Browse catalog (paginated)
   - Get species details
   - Create species
   - Get full details with care requirements

4. **ObservationController** (`/api/plants/{plantId}/observations`)
   - Log observation
   - Get observation history

5. **SharedPlantController** (`/api/shared-plants`)
   - Share a plant
   - Get all shared plants
   - Get plants by city
   - Get cities with plants
   - Get my shared plants

6. **MessageController** (`/api/messages`)
   - Send message
   - Get inbox
   - Get conversation with user

---

## Accessing the Documentation

### Via API Gateway (Recommended for production)
```
http://localhost:8000/plant-service/swagger-ui.html
```

Provides an interactive UI where you can:
- Browse all endpoints organized by tag
- See request/response schemas
- Try out endpoints with sample data
- View error codes and descriptions

### Direct Access (Development)
```
http://localhost:9000/swagger-ui.html
```

### Raw OpenAPI JSON Spec
```
http://localhost:9000/v3/api-docs
```

Returns the complete OpenAPI 3.0 specification in JSON format. This can be imported into other tools like:
- Postman (generate API tests)
- Insomnia (REST client)
- ReDoc (alternative documentation viewer)
- Code generators (generate SDKs)

---

## Key Features of the Documentation

### 1. Automatic Endpoint Discovery
SpringDoc scans all `@RestController` classes and automatically documents:
- HTTP method (GET, POST, PUT, DELETE, etc.)
- Path and path parameters
- Request body schema
- Response schema
- Status codes

### 2. Human-Readable Descriptions
Instead of raw code, teams see:
```
Plant API - Manage your plants and garden

POST /api/plants/by-photo
  Summary: Add plant by photo
  Description: Identify and add a plant to your garden by uploading a photo
  Parameters:
    - photo (file, required)
  Response: 201 Created with PlantResponse
```

### 3. Schema Definitions
All request and response DTOs are documented with their fields, types, and requirements:
```json
{
  "RegisterRequest": {
    "type": "object",
    "properties": {
      "email": {"type": "string", "format": "email"},
      "password": {"type": "string"}
    },
    "required": ["email", "password"]
  }
}
```

### 4. Try-It-Out Feature
In Swagger UI, developers can click "Try it out" on any endpoint to:
- Fill in parameters
- Send a real request
- See the response
- Debug API issues interactively

---

## Docker Containerization Status

✅ **Already Completed** (Exercises 1-2)

Plant-Service is fully containerized:
- `Dockerfile` — Multi-stage build for production
- `docker-compose.yml` — Orchestrates Plant-Service + PostgreSQL
- Network configuration — Connected to shared Docker network for inter-service communication
- Environment variables — Container URLs use container names (e.g., `postgres:5432`)

### Running the System
```bash
# Start Plant-Service infrastructure
cd plant-service
docker-compose up -d

# Verify it's running
docker ps | grep plant
```

---

## Benefits of API Documentation

### For Developers
- **Discoverability**: Find endpoints without reading code
- **Testing**: Test endpoints interactively before writing client code
- **Integration**: Understand request/response formats for integration

### For Teams
- **Self-service**: No need to ask "What endpoints do you have?"
- **Contract clarity**: Clear definition of API contract
- **Version tracking**: OpenAPI spec can be versioned with API changes
- **Code generation**: Tools can generate client SDKs from the spec

### For Operations
- **Monitoring**: Track which endpoints are being used
- **Rate limiting**: Apply limits based on endpoint criticality
- **Security**: Document authentication requirements per endpoint

---

## OpenAPI Specification Details

### Title and Version
Automatically generated from Spring Boot application properties:
- **Title**: PlantPulse API
- **Version**: 1.0.0 (from `pom.xml`)
- **Description**: Plant and User Service for PlantPulse

### Servers
- Local development: `http://localhost:9000`
- Via API Gateway: `http://localhost:8000/plant-service`

### Security Schemes
Currently documented as:
- **JWT Bearer Token** (if configured in `@SecurityScheme` annotations)
- **Basic Authentication** (if applicable)

### Content Types
- **Request**: `application/json`
- **Response**: `application/json`

---

## Future Enhancements (Exercise 5)

### AsyncAPI Documentation (When Kafka is implemented)
Will document Kafka events similar to OpenAPI documenting REST endpoints:
- **Topics**: Which topics the service publishes/consumes
- **Message Schemas**: JSON schema of event payloads
- **Event Types**: What events are published and when

### Implementation
```kotlin
@Component
class KafkaEventDocumentation {
    @AsyncPublisher(
        operation = AsyncOperation(
            channelName = "plant.created",
            description = "Published when a new plant is created",
            payloadType = PlantCreatedEvent::class
        )
    )
    fun plantCreated(payload: PlantCreatedEvent) {
        // Documentation only — never called at runtime
    }
}
```

Access at: `http://localhost:9000/springwolf/asyncapi-ui.html`

---

## Testing the Documentation

### Test 1: Access Swagger UI
```bash
curl -s http://localhost:9000/swagger-ui.html | grep -i "swagger" | head -1
# Should return HTML content indicating Swagger UI loaded
```

### Test 2: Get OpenAPI Spec
```bash
curl -s http://localhost:9000/v3/api-docs | jq '.tags'
# Should list all tag groups: Plant API, Species API, etc.
```

### Test 3: Try an Endpoint
```bash
# Via Swagger UI: Click "Try it out" on GET /api/species
# Or via curl:
curl -s http://localhost:9000/api/species | jq '.[] | .name' | head -5
```

---

## Files Modified

1. **PlantController.kt** — Added `@Tag` and `@Operation` annotations
2. **SpeciesController.kt** — Added `@Tag` and `@Operation` annotations
3. **ObservationController.kt** — Added `@Tag` and `@Operation` annotations
4. **SharedPlantController.kt** — Added `@Tag` and `@Operation` annotations
5. **MessageController.kt** — Added `@Tag` and `@Operation` annotations
6. **AuthController.kt** — Added `@Tag` and `@Operation` annotations

---

## What Exercise 7 Demonstrates

✅ **API Documentation Best Practices**
- Clear descriptions of what each endpoint does
- Organized by feature area (tags)
- Automatic schema generation from code
- No manual documentation to maintain

✅ **Docker Containerization**
- Multi-stage Docker build
- Production-ready Docker image
- docker-compose orchestration
- Network isolation and communication

✅ **Developer Experience**
- Zero onboarding friction for new developers
- Interactive API testing without tools
- Machine-readable API contract

---

## Summary

Exercise 7 is now **COMPLETE**. Plant-Service has professional API documentation automatically generated from code annotations. The API is:

- **Discoverable** — browse all endpoints in Swagger UI
- **Testable** — try endpoints interactively
- **Portable** — OpenAPI spec can be shared with other teams
- **Maintainable** — documentation stays in sync with code
- **Containerized** — runs reliably in any Docker environment

Next steps (when ready):
- Implement Kafka producer/consumer (Exercise 4)
- Add AsyncAPI documentation (Exercise 5)
- Implement Feign Client + Circuit Breaker (Exercise 6)
