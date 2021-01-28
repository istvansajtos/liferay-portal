/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.security.sso.openid.connect.util;

import com.liferay.portal.cache.io.SerializableObjectWrapper;
import com.liferay.portal.security.sso.openid.connect.OpenIdConnectSession;
import com.liferay.portal.security.sso.openid.connect.constants.OpenIdConnectWebKeys;

import java.io.Serializable;

import javax.servlet.http.HttpSession;

/**
 * @author Istvan Sajtos
 */
public class OpenIdConnectUtil {

	public static OpenIdConnectSession getOpenIdConnectSession(HttpSession httpSession) {
		Object sessionAttribute = httpSession.getAttribute(
			OpenIdConnectWebKeys.OPEN_ID_CONNECT_SESSION);

		OpenIdConnectSession openIdConnectSession =
			(OpenIdConnectSession)SerializableObjectWrapper.unwrap(
				sessionAttribute);

		return openIdConnectSession;
	}

	public static void setOpenIdConnectSession(HttpSession httpSession,
		OpenIdConnectSession openIdConnectSessionImpl) {

		httpSession.setAttribute(OpenIdConnectWebKeys.OPEN_ID_CONNECT_SESSION,
			new SerializableObjectWrapper((Serializable)openIdConnectSessionImpl));
	}
}