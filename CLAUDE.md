# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Monorepo with a Spring Boot 4 backend (Java 25) and an Angular 21 frontend with SSR. Currently contains a sample items CRUD to demonstrate the architecture patterns.

## Commands

All commands are orchestrated via `just` (justfile at root). Run `just` to see all recipes.

### Development

```
just up                  # Start PostgreSQL via Docker
just dev-backend         # Spring Boot with dev profile (hot reload)
just dev-frontend        # Angular dev server (pnpm start → ng serve)
```

### Build

```
just build-backend       # ./mvnw package -DskipTests
just build-frontend      # pnpm run build
just build               # Both
```

### Test

```
just test-backend        # ./mvnw test
just test-frontend       # pnpm test (Vitest)
just test                # Both
```

Single backend test: `cd backend && ./mvnw test -Dtest=ClassName`
Single frontend test: `cd frontend && pnpm exec vitest run src/path/to/file.spec.ts`

### Lint & Format

```
just lint                # ESLint (frontend) + Checkstyle (backend)
just fmt                 # Prettier (frontend) + Spotless/google-java-format (backend)
just fmt-check           # Check formatting without modifying
just typecheck           # tsc --noEmit on frontend
just check               # typecheck + lint + fmt-check
```

### Docker Deployment

```
just deploy              # postgres + backend + frontend containers
just deploy-build        # Same but rebuilds images
```

## Architecture

### Backend (`backend/`)

Spring Boot 4 with strict three-layer package structure under `ch.loete.backend`:

- **`web/`** — REST controllers and DTOs (request/response). Depends only on `domain/`.
- **`domain/`** — Business logic, services, entities, exceptions, enums. Depends on `process/`.
- **`process/`** — Data access (JPA repositories) and external API clients. No business logic.
- **`config/`** — Security, CORS, OpenAPI, dev data seeder.
- **`util/`** — NanoIdGenerator (8-char URL-safe IDs).

Key conventions:

- Jakarta EE namespace only (`jakarta.persistence.*`, `jakarta.validation.*`) — no `javax.*`
- Database migrations via Flyway (`src/main/resources/db/migration/`)
- API base path: `/api` (via `server.servlet.context-path`)

### Frontend (`frontend/`)

Angular 21 with SSR, standalone components (no NgModules), Tailwind CSS v4, Vitest.

- **`core/`** — Services, guards, interceptors, models
- **`features/`** — Route-level page components
- **`shared/`** — Reusable UI components, pipes, directives

Key conventions:

- Path alias `@/*` maps to `src/app/*`
- Package manager: pnpm
- Standalone components with signal-based state (`signal`, `input`, `output`)
- Functional guards and interceptors
