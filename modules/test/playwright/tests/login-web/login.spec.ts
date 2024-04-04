const {expect, test} = require('@playwright/test');
test('title is H - Liferay DXP', async ({page}) => {
	await page.goto('/');
	await expect(page).toHaveTitle('Home - Liferay DXP');
});