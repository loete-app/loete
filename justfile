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

# ── Coverage ────────────────────────────────────────────────────────

# Backend tests with coverage check (JaCoCo 85% enforcement)
[group('test')]
test-coverage-backend:
    cd backend && ./mvnw verify

# Frontend tests with coverage
[group('test')]
test-coverage-frontend:
    cd frontend && pnpm exec ng test --watch=false

# All coverage checks
[group('test')]
test-coverage: test-coverage-backend test-coverage-frontend

# ── E2E ─────────────────────────────────────────────────────────────

# Run Playwright E2E tests (starts postgres, backend, and frontend automatically)
[group('test')]
test-e2e: up
    cd frontend && pnpm exec playwright test

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

# ── Infra (OpenTofu) ─────────────────────────────────────────────────

# Initialize Tofu backend & providers
[group('infra')]
infra-init:
    cd infra && tofu init

# Check Tofu formatting
[group('infra')]
infra-fmt-check:
    cd infra && tofu fmt -check -recursive

# Format Tofu files
[group('infra')]
infra-fmt:
    cd infra && tofu fmt -recursive

# Validate Tofu configuration
[group('infra')]
infra-validate:
    cd infra && tofu validate

# Plan infra changes against production.tfvars
[group('infra')]
infra-plan:
    cd infra && tofu plan -var-file=production.tfvars

# Apply infra changes (requires confirmation)
[group('infra')]
infra-apply:
    cd infra && tofu apply -var-file=production.tfvars

# ── Test Data ────────────────────────────────────────────────────────

# Seed 10 test events
[group('test')]
seed-test:
    cd backend && LOETE_SEED=small ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,testdata

# Seed 500 test events
[group('test')]
seed-test-large:
    cd backend && LOETE_SEED=large ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,testdata

# Clear all events and favorites
[group('test')]
seed-test-clear:
    cd backend && LOETE_SEED=clear ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,testdata
