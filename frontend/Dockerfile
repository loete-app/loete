FROM node:22-alpine AS build
ARG API_URL=http://localhost:8080/api
RUN corepack enable pnpm
WORKDIR /app
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile
COPY . .
RUN sed -i "s|http://localhost:8080/api|${API_URL}|g" src/environments/environment.ts \
    && pnpm run build

FROM node:22-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /app/dist ./dist
RUN chown -R appuser:appgroup /app
USER appuser
EXPOSE 4000
CMD ["node", "dist/frontend/server/server.mjs"]
