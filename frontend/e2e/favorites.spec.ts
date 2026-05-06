import { test, expect } from "@playwright/test";

test.describe("Favorites", () => {
  test("favorites page redirects to login for anonymous user", async ({
    page,
  }) => {
    await page.goto("/favoriten");

    // Unauthenticated users are redirected to login
    await expect(page.locator("h1")).toHaveText("Anmelden", {
      timeout: 10_000,
    });
    await expect(
      page.getByText("Melde dich an, um Favoriten zu speichern"),
    ).toBeVisible();
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
