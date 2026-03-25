# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Löte is an event discovery platform that aggregates events from Ticketmaster and lets users browse, filter, search, and favorite events. Monorepo with a Spring Boot 4 backend (Java 25) and an Angular 21 frontend with SSR.

The project is currently a scaffold with a sample items CRUD. All real features are built incrementally from Linear issues.

## POC Reference

A fully coded proof-of-concept lives at `../loete-poc/`. Use it as an **implementation reference**, not as copy-paste source. The POC is overengineered — always simplify when bringing patterns into this project.

### How to use the POC

When the user gives you a Linear issue, follow this workflow:

1. **Read the issue** — understand what feature or fix is requested.
2. **Find the relevant POC code** — locate the corresponding files in `../loete-poc/backend/` and `../loete-poc/frontend/` that implement or relate to the requested feature.
3. **Read the POC implementation** — understand the patterns, entities, endpoints, components, and queries involved.
4. **Simplify and adapt** — implement only what the issue requires. Strip out anything unnecessary:
   - No pgvector/embeddings unless the issue specifically asks for semantic search.
   - No behavioral tracking, taste profiles, or discovery system unless requested.
   - No rate limiting, request logging, or token blacklisting unless requested.
   - No scheduled batch jobs unless requested.
   - Keep entities to the fields the issue needs — don't add extra columns "because the POC has them."
5. **Follow this project's conventions** (below) — the POC may diverge slightly in naming or structure. This project's patterns take precedence.
6. **Run checks** — `just check` (typecheck + lint + format check) and `just test` before considering work done.

### POC file map (quick lookup)

| Concern                      | POC Backend Path                                       | POC Frontend Path                                     |
| ---------------------------- | ------------------------------------------------------ | ----------------------------------------------------- |
| Entities / Models            | `backend/src/main/java/ch/loete/backend/domain/model/` | `frontend/src/app/core/models/`                       |
| Services                     | `backend/.../domain/service/`                          | `frontend/src/app/core/services/`                     |
| Controllers                  | `backend/.../web/controller/`                          | —                                                     |
| DTOs (request/response)      | `backend/.../web/dto/`                                 | —                                                     |
| Repositories                 | `backend/.../process/repository/`                      | —                                                     |
| External API clients         | `backend/.../process/client/`                          | —                                                     |
| Flyway migrations            | `backend/src/main/resources/db/migration/`             | —                                                     |
| Config (security, CORS, JWT) | `backend/.../config/`                                  | —                                                     |
| Feature pages                | —                                                      | `frontend/src/app/features/`                          |
| Shared UI components         | —                                                      | `frontend/src/app/shared/components/`                 |
| Guards / Interceptors        | —                                                      | `frontend/src/app/core/guards/`, `core/interceptors/` |
| Routes                       | —                                                      | `frontend/src/app/app.routes.ts`                      |
| Application config           | `backend/src/main/resources/application.yml`           | `frontend/src/environments/`                          |

### POC architecture doc

`../loete-poc/IMPLEMENATION.md` contains the full specification the POC was built from. Consult it for detailed schema definitions, endpoint contracts, and business rules when an issue is ambiguous.

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
- Entities use Lombok `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Entities with String PKs use NanoID(8) via `@PrePersist` callback
- Repositories for NanoID entities extend `JpaRepository<Entity, String>`
- Use proper HTTP status codes: 200, 201, 204, 400, 401, 403, 404, 409, 500

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
- Icons: lucide-angular
- UI components follow variant pattern with `class-variance-authority`
- Auth token stored in sessionStorage as `loete_token`

## Workflow: Implementing a Linear Issue

1. Read and understand the issue requirements.
2. Locate corresponding POC code in `../loete-poc/` for reference.
3. Plan the minimal set of changes needed (migrations, entities, repositories, services, controllers, DTOs, frontend models, services, components, routes).
4. Implement backend changes first:
   - Flyway migration (`V{next}__description.sql`)
   - Entity in `domain/model/`
   - Repository in `process/repository/`
   - Service in `domain/service/`
   - DTOs in `web/dto/request/` and `web/dto/response/`
   - Controller in `web/controller/`
5. Implement frontend changes:
   - Model in `core/models/`
   - Service in `core/services/`
   - Feature component in `features/`
   - Route in `app.routes.ts`
   - Shared components in `shared/components/` if reusable
6. Run `just fmt` to auto-format all code.
7. Run `just check` to verify typecheck + lint + formatting.
8. Run `just test` to verify nothing is broken.
