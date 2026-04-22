# syntax=docker/dockerfile:1.7

FROM node:22-bookworm-slim@sha256:d415caac2f1f77b98caaf9415c5f807e14bc8d7bdea62561ea2fef4fbd08a73c AS build
ARG API_URL=http://localhost:8080/api
RUN corepack enable pnpm
WORKDIR /app
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile
COPY . .
RUN sed -i "s|http://localhost:8080/api|${API_URL}|g" src/environments/environment.ts \
    && pnpm run build

FROM gcr.io/distroless/nodejs22-debian12:nonroot@sha256:13593b7570658e8477de39e2f4a1dd25db2f836d68a0ba771251572d23bb4f8e
WORKDIR /app
COPY --from=build --chown=nonroot:nonroot /app/dist ./dist
USER nonroot
EXPOSE 4000
CMD ["dist/frontend/server/server.mjs"]
