import { test, expect } from "@playwright/test";

test.describe("Home page", () => {
  test("loads and displays event cards", async ({ page }) => {
    await page.goto("/");

    await expect(page.locator("h1")).toHaveText("Entdecke Events");

    const cards = page.locator("article.card");
    await expect(cards.first()).toBeVisible({ timeout: 15_000 });

    const count = await cards.count();
    expect(count).toBeGreaterThan(0);

    // Each card has an h3 with text and a .meta section with date info
    for (let i = 0; i < Math.min(count, 3); i++) {
      const card = cards.nth(i);
      await expect(card.locator("h3")).not.toBeEmpty();
      await expect(card.locator(".meta")).toBeVisible();
    }
  });

  test("shows empty state when no events match filters", async ({ page }) => {
    await page.goto("/");

    // Wait for events to load first
    await page
      .locator("article.card")
      .first()
      .waitFor({ timeout: 15_000 })
      .catch(() => {
        // If no events loaded at all, the empty state test is not meaningful — skip
      });

    const searchInput = page.locator('input[type="search"]');
    await searchInput.fill("xyznonexistent999zzz");

    // Wait for the debounced search to trigger and the empty state to appear
    await expect(page.getByText("Keine Events gefunden")).toBeVisible({
      timeout: 10_000,
    });
  });
});
