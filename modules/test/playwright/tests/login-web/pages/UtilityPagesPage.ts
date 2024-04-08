/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

// @ts-ignore

import {Locator, Page} from '@playwright/test';

import {clickAndExpectToBeVisible} from '../../../utils/clickAndExpectToBeVisible';
import {PORTLET_URLS} from '../../../utils/portletUrls';

export class UtilityPagesPage {
	readonly newButton: Locator;
	readonly page: Page;
	readonly signInOption: Locator;

	constructor(page: Page) {
		this.newButton = page.getByRole('button', { name: 'New' });
		this.page = page;
		//await page.getByRole('menuitem', { name: 'Sign In' })
	}

	async goto(siteUrl?: Site['friendlyUrlPath']) {
		await this.page.goto(
			`/group${siteUrl || '/guest'}${PORTLET_URLS.utilityPages}`
		);
	}

	async addNewPage(name: string, type: string) {
		await this.newButton.click();

		await this.page.getByRole('menuitem', { name: type }).click();
		await this.page.getByRole('button', { name: 'Blank' }).click();
		await this.page.getByPlaceholder('Name').click();
		await this.page.getByPlaceholder('Name').fill(name);
		
		//await this.page.getByRole('button', { name: 'Save' }).click();

		await clickAndExpectToBeVisible({
			autoClick: true,
			target: this.page.getByRole('button', { name: 'Publish' }),
			trigger: this.page.getByRole('button', { name: 'Save' }),
		});

		//await this.page.getByRole('button', { name: 'Publish' }).click();
	}

	async clickOnAction(action: string, title: string) {
		await clickAndExpectToBeVisible({
			autoClick: true,
			target: this.page.getByRole('menuitem', {
				exact: true,
				name: action,
			}),
			trigger: this.page
				.locator('div.card-row', {has: this.page.getByTitle(title)})
				.getByRole('button'),
		});
	}

	async goToEdit(pageTitle: string) {
		await this.page.getByLabel(pageTitle).waitFor();

		const href = await this.page
			.locator('div.card-row', {has: this.page.getByLabel(pageTitle)})
			.getByRole('link')
			.getAttribute('href');

		await this.page.goto(href);

		await this.page
			.getByRole('button', {exact: true, name: 'Publish'})
			.waitFor();
	}
}
