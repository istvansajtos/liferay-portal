import { Page, expect, mergeTests } from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {liferayConfig} from '../../liferay.config';
import {loginTest} from '../../fixtures/loginTest';
import {featureFlagsTest} from '../../fixtures/featureFlagsTest';
import {utilityPagesPage} from '../login-web/fixtures/utilityPagesPageTest';
import {UtilityPagesPage} from '../login-web/pages/UtilityPagesPage';

/*
export const test = mergeTests(
    apiHelpersTest,
    featureFlagsTest({
		'LPD-6368': true,
	}),
    loginTest()
);
*/

export const test = mergeTests(
    apiHelpersTest,
    loginTest(),
    utilityPagesPage
);

const addUtilityPage = async (
    name: string,
    type: string,
    page: Page,
    utilityPagesPage: UtilityPagesPage
) => {
    await utilityPagesPage.addNewPage(name, type);

	await expect(page.getByText(message)).toBeVisible();
};

test('valami', async ({ apiHelpers, page, utilityPagesPage }) => {
	await page.goto(liferayConfig.environment.baseUrl);

	await expect(page).toHaveTitle('Home - Liferay DXP');

    UtilityPagesPage
    //await test.expect(page).toHaveTitle('login-utility-page - Liferay DXP');
});