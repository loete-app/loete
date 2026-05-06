import { test, expect } from "@playwright/test";

test.describe("Event detail page", () => {
  test("navigates to event detail page", async ({ page }) => {
    await page.goto("/");

    // Wait for at least one event card to be visible
    const firstCardLink = page.locator("article.card a.card-link").first();
    await expect(firstCardLink).toBeVisible({ timeout: 15_000 });

    // Get the event name from the card before clicking
    const eventName = await firstCardLink.locator("h3").textContent();

    await firstCardLink.click();

    // Verify URL changed to /events/...
    await expect(page).toHaveURL(/\/events\/.+/);

    // Verify h1 with the event name appears
    const heading = page.locator("h1");
    await expect(heading).toBeVisible({ timeout: 10_000 });
    if (eventName) {
      await expect(heading).toContainText(eventName.trim());
    }

    // Verify the back button exists
    await expect(page.locator("button.back")).toContainText("Zurück");
  });
});
