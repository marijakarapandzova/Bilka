# PlantPulse MCP Server

Exposes PlantPulse plant care as MCP tools for an LLM client (e.g. Claude Desktop, Claude Code):

- **Photo diagnosis** — the assistant looks at a plant photo itself and reports symptoms; this server submits them to plant-service's disease matcher and returns the diagnosis.
- **Slack reminders** — a daily watering/care checklist DM'd to the user automatically, plus an on-demand "send it now" tool.
- **Google Calendar sync** — recurring "water this plant" events based on each plant's watering frequency.

It talks to `plant-service` and `health-service` directly (not through `api-gateway`, which only understands Keycloak JWTs — see the top-level `MICROSERVICES_IMPLEMENTATION.md` for why the two auth systems are currently separate).

## Running locally

```bash
mvn spring-boot:run
```

Needs `plant-service` and `health-service` already running (defaults: `http://localhost:8081` / `http://localhost:8082`), and a Postgres reachable at `jdbc:postgresql://localhost:5436/mcpserver` (or run `docker compose up mcp-postgres` from this directory).

The MCP server listens on **port 8085**, with the SSE transport at `/sse` (message endpoint `/mcp/message`). Point your MCP client at `http://localhost:8085/sse`.

## Running with Docker

From this directory, after `docker-compose-gateway.yml` (for the shared network) and the other services are up:

```bash
docker compose up --build
```

## First use

1. Call the `plantpulse_login` tool with a PlantPulse email/password (register one via plant-service's `POST /api/auth/login`/`/register` if needed). It returns a `session_token` — pass that to every other PlantPulse tool for the rest of the conversation.
2. Call `list_my_plants` to get plant ids.
3. Call `diagnose_plant_photo` with a plant id and the symptoms you observe in a photo the user shared.
4. Call `get_slack_install_link` / `get_google_calendar_auth_link`, open the link, approve, then `send_daily_checklist_now` / `sync_watering_calendar`.

## Prerequisites you need to set up yourself

**Slack app** (once, at [api.slack.com/apps](https://api.slack.com/apps)):
- Create an app, add bot scopes `chat:write` and `im:write`.
- Add OAuth redirect URL: `http://localhost:8085/slack/oauth/callback` (or your deployed URL).
- Set env vars `SLACK_CLIENT_ID`, `SLACK_CLIENT_SECRET` from the app's "Basic Information" page.

**Google Cloud OAuth client** (once, at [console.cloud.google.com](https://console.cloud.google.com)):
- Enable the Google Calendar API for a project.
- Create an OAuth 2.0 Client ID (Web application), redirect URI `http://localhost:8085/google/oauth/callback`.
- Add your own Google account as a test user (the app will be unverified).
- Set env vars `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`.

**Encryption key:** set `MCP_TOKEN_ENCRYPTION_KEY` to a long random string in any real deployment — it protects the Slack bot token, Google refresh token, and (see below) the user's PlantPulse password at rest. The default in `application.yml` is dev-only, same pattern as `JWT_SECRET` in the other services.

## Design notes / tradeoffs

- **Session model:** MCP tool calls have no built-in "who is calling" concept, so `plantpulse_login` mints an opaque `session_token` held in an in-memory map for the life of the process — simple, but it means a server restart forces everyone to log in again. Slack/Google integrations persist in Postgres and don't need re-linking.
- **Why the daily Slack cron stores an encrypted password:** the automatic daily digest (`DailySlackReminderScheduler`) has to run with nobody's chat session open, so it can't reuse a `session_token`/JWT captured during login (JWTs expire). It re-authenticates via plant-service's `/api/auth/login` using the user's password, stored AES-GCM-encrypted in `linked_accounts`. This is a real credential store — acceptable for this project's scope given plant-service's own auth is already basic email/password with no OAuth to delegate to, but call it out before using real user passwords in production.
- **Not registered with Consul / not routed through api-gateway** — nothing else needs to discover this service, and MCP clients connect to it directly.
