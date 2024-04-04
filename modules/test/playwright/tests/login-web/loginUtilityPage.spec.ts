import { test, expect } from '@playwright/test';

test('valami', async ({ page }) => {
    await page.goto('https://playwright.dev/');

    await expect(page).toHaveTitle('Home - Liferay DXP');
    //await test.expect(page).toHaveTitle('login-utility-page - Liferay DXP');
});