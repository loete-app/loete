import { test, expect } from "@playwright/test";

test.describe("Favorites", () => {
  test("favorites page shows anon banner for unauthenticated user", async ({
    page,
  }) => {
    await page.goto("/favoriten");

    await expect(page.locator("h1")).toHaveText("Meine Favoriten", {
      timeout: 10_000,
    });
    await expect(page.getByText("Logge dich ein")).toBeVisible();
  });

  test("event cards have a favorite button", async ({ page }) => {
    await page.goto("/");

    const firstCard = page.locator("article.card").first();
    await expect(firstCard).toBeVisible({ timeout: 15_000 });

    const favBtn = firstCard.locator("button.fav-btn");
    await expect(favBtn).toBeVisible();
    await expect(favBtn).toHaveAttribute("aria-label", /Favorit/);
  });
});
