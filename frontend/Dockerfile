FROM node:22-alpine AS build
RUN corepack enable pnpm
WORKDIR /app
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile
COPY . .
RUN pnpm run build

FROM node:22-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /app/dist ./dist
RUN chown -R appuser:appgroup /app
USER appuser
EXPOSE 4000
CMD ["node", "dist/frontend/server/server.mjs"]
