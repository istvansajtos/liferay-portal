/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.exportimport.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.security.ldap.exportimport.LDAPUserImporter;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.user.associated.data.anonymizer.UADAnonymousUserProvider;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Istvan Sajtos
 */
@RunWith(Arquillian.class)
public class LDAPUserExporterImplTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Ignore
	@Test
	public void testAnonymousUserExport() throws Exception {
		User user1 = _uadAnonymousUserProvider.getAnonymousUser(
			TestPropsValues.getCompanyId());

		Assert.assertNotNull(user1);

		user1 = _userLocalService.updateUser(user1);

		User user2 = _ldapUserImporter.importUser(
			user1.getCompanyId(), user1.getEmailAddress(),
			user1.getScreenName());

		Assert.assertNull(user2);
	}

	@Test
	public void testInactiveUserExport() throws Exception {
		User user1 = UserTestUtil.addUser();

		_userLocalService.updateStatus(
			user1.getUserId(), WorkflowConstants.STATUS_INACTIVE,
			ServiceContextTestUtil.getServiceContext());

		User user2 = _ldapUserImporter.importUser(
			user1.getCompanyId(), user1.getEmailAddress(),
			user1.getScreenName());

		Assert.assertNotNull(user2);

		Assert.assertEquals(
			user2.getStatus(), WorkflowConstants.STATUS_INACTIVE);
	}

	@Inject
	private LDAPUserImporter _ldapUserImporter;

	@Inject
	private UADAnonymousUserProvider _uadAnonymousUserProvider;

	@Inject
	private UserLocalService _userLocalService;

}