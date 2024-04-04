import { expect, mergeTests } from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {liferayConfig} from '../../liferay.config';
import {loginTest} from '../../fixtures/loginTest';
import {UtilityPagesPage} from '../login-web/pages/UtilityPagesPage';
import {featureFlagsTest} from '../../fixtures/featureFlagsTest';
import {isolatedSiteTest} from '../../fixtures/isolatedSiteTest';
import getRandomString from '../../utils/getRandomString';
import {navigationMenusPagesTest} from '../site-navigation-admin-web/fixtures/navigationMenusPagesTest';

export const test = mergeTests(
    apiHelpersTest,
    featureFlagsTest({
		'LPD-6378': true
	}),
    loginTest()
);

test('asdfafasdfasdfa', async ({ apiHelpers, page }) => {
    await page.goto('/');
    //await page.goto(liferayConfig.environment.baseUrl);

    // add utility page
    await page.getByLabel('Open Product Menu').click();
    await page.getByRole('menuitem', { name: 'Site Builder' }).click();
    await page.getByRole('menuitem', { name: 'Pages' }).click();
    await page.locator('a').filter({ hasText: 'Utility Pages' }).click();
    await page.getByRole('button', { name: 'New' }).click();
    await page.getByRole('menuitem', { name: 'Sign In' }).click();
    await page.getByRole('button', { name: 'Blank' }).click();
    await page.getByPlaceholder('Name').click();
    await page.getByPlaceholder('Name').fill('SignInUtilityPage');
    await page.getByRole('button', { name: 'Save' }).click();
    await page.getByLabel('Publish', { exact: true }).click();

    // mark as default
 /*   await page.locator('[id="_com_liferay_layout_admin_web_portlet_GroupPagesPortlet_entries_3"]').getByLabel('More actions').click();
    page.once('dialog', dialog => {
      console.log(`Dialog message: ${dialog.message()}`);
      dialog.dismiss().catch(() => {});
    });
    await page.getByRole('menuitem', { name: 'Mark as Default' }).click();*/

    // create private page
    /*await page.getByLabel('Site Builder').getByText('Pages').click();
    await page.getByRole('button', { name: 'New' }).click();
    await page.getByRole('menuitem', { name: 'Page', exact: true }).click();
    await page.getByRole('button', { name: 'Widget Page' }).click();
    await page.frameLocator('iframe[title="Add Page"]').getByPlaceholder('Add Page Name').click();
    await page.frameLocator('iframe[title="Add Page"]').getByPlaceholder('Add Page Name').fill('PrivateTestPage');
    await page.frameLocator('iframe[title="Add Page"]').getByRole('button', { name: 'Add' }).click();
    await page.goto('http://localhost:8080/group/guest/~/control_panel/manage?p_p_id=com_liferay_layout_admin_web_portlet_GroupPagesPortlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_r_p_selPlid=52&_com_liferay_layout_admin_web_portlet_GroupPagesPortlet_mvcRenderCommandName=%2Flayout_admin%2Fedit_layout&_com_liferay_layout_admin_web_portlet_GroupPagesPortlet_redirect=http%3A%2F%2Flocalhost%3A8080%2Fgroup%2Fguest%2F%7E%2Fcontrol_panel%2Fmanage%3Fp_p_id%3Dcom_liferay_layout_admin_web_portlet_GroupPagesPortlet%26p_p_lifecycle%3D0%26p_p_state%3Dmaximized%26p_p_auth%3D0OiydUqf&_com_liferay_layout_admin_web_portlet_GroupPagesPortlet_portletResource=com_liferay_layout_admin_web_portlet_GroupPagesPortlet&_com_liferay_layout_admin_web_portlet_GroupPagesPortlet_backURLTitle=Pages&_com_liferay_layout_admin_web_portlet_GroupPagesPortlet_groupId=20117&_com_liferay_layout_admin_web_portlet_GroupPagesPortlet_privateLayout=false&p_p_auth=0OiydUqf');
    await page.getByRole('button', { name: 'Save' }).click();
    await page.getByRole('link', { name: 'Go to Pages' }).click();
    await page.locator('li').filter({ hasText: 'PrivateTestPagePrivateTestPageWidget Page' }).getByRole('button').nth(2).click();
    await page.getByRole('menuitem', { name: 'Permissions' }).click();
    await page.frameLocator('iframe[title="Permissions"]').locator('#guest_ACTION_VIEW').uncheck();
    await page.frameLocator('iframe[title="Permissions"]').getByRole('button', { name: 'Save' }).click();*/

    // enable login prompt
 /*   await page.getByLabel('Options').click();
    await page.getByLabel('Open Applications MenuCtrl+⌥+A').click();
    await page.getByRole('tab', { name: 'Control Panel' }).click();
    await page.getByRole('menuitem', { name: 'System Settings' }).click();
    //await page.goto('http://localhost:8080/group/control_panel/manage?p_p_id=com_liferay_configuration_admin_web_portlet_SystemSettingsPortlet&p_p_lifecycle=0&p_p_state=maximized&p_v_l_s_g_id=20117');
    await page.getByRole('link', { name: 'Login' }).click();
    await page.getByLabel('Prompt Enabled').check();
    await page.getByRole('button', { name: 'Update' }).click();*/

    // sign out
 /*   await page.getByLabel('Test Test User Profile').click();
    await page.getByRole('menuitem', { name: 'Sign Out' }).click();*/
    
//    await page.goto('http://localhost:8080/group/guest/~/control_panel/manage?p_p_id=com_liferay_my_account_web_portlet_MyAccountPortlet&p_p_lifecycle=0');
    //await page.goto('http://localhost:8080/privatetestpage');
    
    await expect(page).toHaveTitle('SignInUtilityPage - Liferay DXP');
    //await test.expect(page).toHaveTitle('login-utility-page - Liferay DXP');
});