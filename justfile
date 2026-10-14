set dotenv-load
set dotenv-filename := "backend/.env"

# List all available recipes
default:
    @just --list

# ── Dev ──────────────────────────────────────────────────────────────

# Start the Angular dev server
[group('dev')]
dev-frontend:
    cd frontend && pnpm start

# Start Spring Boot with hot reload (devtools)
[group('dev')]
dev-backend:
    cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# ── Docker ───────────────────────────────────────────────────────────

# Start postgres
[group('docker')]
up:
    docker compose up -d postgres

# Stop all services
[group('docker')]
down:
    docker compose down

# Show service logs (pass service name to filter)
[group('docker')]
logs *args:
    docker compose logs {{ args }}

# Deploy all services (postgres + backend + frontend containers)
[group('docker')]
deploy:
    docker compose --profile deploy up -d

# Rebuild and deploy all services
[group('docker')]
deploy-build:
    docker compose --profile deploy up -d --build

# ── Lint ─────────────────────────────────────────────────────────────

# Lint frontend (ESLint)
[group('lint')]
lint-frontend:
    cd frontend && pnpm exec ng lint

# Lint backend (Checkstyle)
[group('lint')]
lint-backend:
    cd backend && ./mvnw checkstyle:check

# Lint all
[group('lint')]
lint: lint-frontend lint-backend

# ── Format ───────────────────────────────────────────────────────────

# Format frontend (Prettier)
[group('format')]
fmt-frontend:
    cd frontend && pnpm exec prettier --write "src/**/*.{ts,html,scss,json}"

# Format backend (google-java-format via Spotless)
[group('format')]
fmt-backend:
    cd backend && ./mvnw spotless:apply

# Format all
[group('format')]
fmt: fmt-frontend fmt-backend

# Check frontend formatting
[group('format')]
fmt-check-frontend:
    cd frontend && pnpm exec prettier --check "src/**/*.{ts,html,scss,json}"

# Check backend formatting
[group('format')]
fmt-check-backend:
    cd backend && ./mvnw spotless:check

# Check all formatting
[group('format')]
fmt-check: fmt-check-frontend fmt-check-backend

# ── Typecheck ────────────────────────────────────────────────────────

# Type-check the Angular frontend
[group('check')]
typecheck:
    cd frontend && pnpm exec tsc --noEmit -p tsconfig.app.json

# Run all checks (typecheck + lint + format check)
[group('check')]
check: typecheck lint fmt-check

# ── Test ─────────────────────────────────────────────────────────────

# Test frontend
[group('test')]
test-frontend:
    cd frontend && pnpm test

# Test backend
[group('test')]
test-backend:
    cd backend && ./mvnw test

# Test all
[group('test')]
test: test-frontend test-backend

# ── Build ────────────────────────────────────────────────────────────

# Build frontend for production
[group('build')]
build-frontend:
    cd frontend && pnpm run build

# Build backend jar
[group('build')]
build-backend:
    cd backend && ./mvnw package -DskipTests

# Build all
[group('build')]
build: build-frontend build-backend

# ── Install ──────────────────────────────────────────────────────────

# Install frontend dependencies
[group('install')]
install-frontend:
    cd frontend && pnpm install

# Resolve backend Maven dependencies
[group('install')]
install-backend:
    cd backend && ./mvnw dependency:resolve

# Install all dependencies
[group('install')]
install: install-frontend install-backend
