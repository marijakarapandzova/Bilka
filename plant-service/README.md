# plant-service

Plant & User Service for PlantPulse — Kotlin + Spring Boot + Maven.

Covers (Dev 1 scope):
- Auth (JWT) — register/login
- Species knowledge base (seeded from the Trefle API)
- Photo ID (Plant.id API)
- My Garden (add by photo / manual, view, update, delete)
- Observation logging
- Symptom → disease matcher
- Kafka events published for the Health & Intelligence service
- Consul service discovery
- Swagger/OpenAPI docs

## Requirements
- JDK 17
- Maven
- Docker (for local Postgres/Kafka/Consul via docker-compose)

## Quick start

```bash
# 1. Start local infrastructure
docker compose up -d

# 2. Set required env vars (or edit application.yml directly)
export TREFLE_API_KEY=your_trefle_token       # https://trefle.io
export PLANT_ID_API_KEY=your_plantid_key      # https://web.plant.id (optional, photo ID won't work without it)
export JWT_SECRET=some-long-random-secret-at-least-32-chars

# 3. Run
mvn spring-boot:run
```

The app starts on **port 8081**.

- Swagger UI: http://localhost:8081/swagger-ui.html
- Health: http://localhost:8081/actuator/health
- Consul UI: http://localhost:8500

## Package structure (DDD-style)
```
com.plantpulse.plantservice/
├── domain/
│   ├── user/         # User aggregate + Location value object
│   ├── plant/         # Plant aggregate
│   ├── species/        # Species reference/knowledge data
│   ├── observation/     # Observation entity
│   └── disease/        # Disease knowledge base + SymptomMatcher
├── application/       # Application services (use cases), exceptions
├── api/            # REST controllers + DTOs, global exception handler
├── infrastructure/
│   ├── external/       # TrefleClient, PlantIdClient
│   └── messaging/       # Kafka events + publisher
├── security/         # JWT filter, SecurityConfig, CurrentUser
└── config/          # OpenApiConfig, SpeciesDataSeeder
```

## API overview

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/register` | Register with email + password |
| POST | `/api/auth/login` | Login, returns JWT |
| GET | `/api/species?query=` | Search species knowledge base |
| GET | `/api/species/{id}` | Get one species |
| POST | `/api/plants/by-photo` | Add plant via photo identification |
| POST | `/api/plants/manual` | Add plant via manual species selection |
| GET | `/api/plants` | My Garden — list current user's plants |
| GET | `/api/plants/{id}` | Get one plant |
| PATCH | `/api/plants/{id}` | Update nickname/room |
| DELETE | `/api/plants/{id}` | Remove plant |
| POST | `/api/plants/{plantId}/observations` | Log observation, get disease match |
| GET | `/api/plants/{plantId}/observations` | Observation/health history |

All endpoints except `/api/auth/**` require `Authorization: Bearer <token>`.

## Kafka topics published

- `plant.added` — consumed by Health & Intelligence service to start care schedule/reminders
- `observation.logged` — consumed to update health score / outbreak detection
- `plant.removed` — consumed to stop tracking a plant

## Notes
- `SpeciesDataSeeder` runs once on startup and only if the `species` table is empty and `TREFLE_API_KEY` is set. Safe to skip if you don't have a token yet — you can add species manually via the repository/DB in the meantime.
- Photo ID requires a `PLANT_ID_API_KEY`. Without it, `/api/plants/by-photo` returns a 502.
