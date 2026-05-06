import { defineConfig } from "@playwright/test";

export default defineConfig({
  testDir: "./e2e",
  timeout: 30_000,
  retries: process.env.CI ? 2 : 0,
  use: {
    baseURL: "http://localhost:4200",
    trace: "on-first-retry",
    screenshot: "only-on-failure",
  },
  projects: [{ name: "chromium", use: { browserName: "chromium" } }],
  webServer: [
    {
      command:
        "cd ../backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments=--spring.docker.compose.enabled=false",
      url: "http://localhost:8080/api/sample/health",
      reuseExistingServer: true,
      timeout: 120_000,
      stdout: "ignore",
      stderr: "pipe",
    },
    {
      command: "pnpm start",
      port: 4200,
      reuseExistingServer: true,
      timeout: 60_000,
    },
  ],
});
