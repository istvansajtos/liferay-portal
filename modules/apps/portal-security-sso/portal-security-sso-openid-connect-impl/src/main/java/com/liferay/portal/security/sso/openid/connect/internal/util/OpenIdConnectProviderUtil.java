/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.sso.openid.connect.internal.util;

import com.liferay.oauth.client.persistence.model.OAuthClientEntry;
import com.liferay.oauth.client.persistence.service.OAuthClientEntryLocalService;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.security.sso.openid.connect.internal.configuration.OpenIdConnectProviderConfiguration;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Renan Vasconcelos
 */
public class OpenIdConnectProviderUtil {

	public static long getOAuthClientEntryId(
		long companyId, String providerName,
		OAuthClientEntryLocalService oAuthClientEntryLocalService) {

		Map<String, Long> oAuthClientEntryIds = _getOAuthClientEntryIds(
			companyId, oAuthClientEntryLocalService);

		if (oAuthClientEntryIds.isEmpty()) {
			oAuthClientEntryIds = _getOAuthClientEntryIds(
				CompanyConstants.SYSTEM, oAuthClientEntryLocalService);
		}

		if (oAuthClientEntryIds.isEmpty()) {
			return 0;
		}

		Long oAuthClientEntryId = oAuthClientEntryIds.get(providerName);

		if (oAuthClientEntryId == null) {
			return 0;
		}

		return oAuthClientEntryId;
	}

	public static OpenIdConnectProviderConfiguration
			getOpenIdConnectProviderConfiguration(String clientId)
		throws Exception {

		Configuration[] configurations = _configurationAdmin.listConfigurations(
			"(service.factoryPid=com.liferay.portal.security.sso.openid." +
				"connect.internal.configuration." +
					"OpenIdConnectProviderConfiguration)");

		if (configurations != null) {
			for (Configuration configuration : configurations) {
				Dictionary<String, Object> properties =
					configuration.getProperties();

				if (properties != null) {
					String openIdConnectClientId = GetterUtil.getString(
						properties.get("openIdConnectClientId"));

					if (clientId.equals(openIdConnectClientId)) {
						return ConfigurableUtil.createConfigurable(
							OpenIdConnectProviderConfiguration.class,
							properties);
					}
				}
			}
		}

		return null;
	}

	public static Map<String, Long> removeOAuthClientEntryIdsByCompanyId(
		long companyId) {

		return _oAuthClientEntryIds.remove(companyId);
	}

	private static Map<String, Long> _getOAuthClientEntryIds(
		long companyId,
		OAuthClientEntryLocalService oAuthClientEntryLocalService) {

		return _oAuthClientEntryIds.computeIfAbsent(
			companyId,
			key -> {
				Map<String, Long> oAuthClientEntryIds = new HashMap<>();

				for (OAuthClientEntry oAuthClientEntry :
						oAuthClientEntryLocalService.
							getCompanyOAuthClientEntries(companyId)) {

					try {
						JSONObject jsonObject =
							JSONFactoryUtil.createJSONObject(
								oAuthClientEntry.getInfoJSON());

						String clientName = jsonObject.getString(
							"client_name", null);

						if (clientName != null) {
							clientName = clientName.substring(
								_CLIENT_TO.length());
						}

						oAuthClientEntryIds.put(
							clientName,
							oAuthClientEntry.getOAuthClientEntryId());
					}
					catch (JSONException jsonException) {
						throw new RuntimeException(jsonException);
					}
				}

				return oAuthClientEntryIds;
			});
	}

	private static final String _CLIENT_TO = "Client to ";

	private static final Bundle _bundle = FrameworkUtil.getBundle(
		OpenIdConnectProviderUtil.class);
	private static final ConfigurationAdmin _configurationAdmin;
	private static final Map<Long, Map<String, Long>> _oAuthClientEntryIds =
		new ConcurrentHashMap<>();

	private static final ServiceTracker<ConfigurationAdmin, ConfigurationAdmin>
		_serviceTracker =
			new ServiceTracker<ConfigurationAdmin, ConfigurationAdmin>(
				_bundle.getBundleContext(), ConfigurationAdmin.class, null) {

				{
					open();
				}
			};

	static {
		_configurationAdmin = _serviceTracker.getService();
	}

}