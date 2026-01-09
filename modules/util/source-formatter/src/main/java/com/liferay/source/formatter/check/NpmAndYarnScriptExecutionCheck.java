/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.check;

import com.liferay.source.formatter.check.BaseSourceCheck;
import java.io.File;

/**
 * @author Istvan Sajtos
 */
public class NpmAndYarnScriptExecutionCheck extends BaseFileCheck {

	@Override
	protected void doProcess(
		String fileName, String absolutePath, String content) throws Exception {

		if (!fileName.endsWith("package.json")) {
			return;
		}

		File file = new File(absolutePath);
		File parentDir = file.getParentFile();

		if (parentDir == null) {
			return;

		File npmrcFile = new File(parentDir, ".npmrc");
		File yarnrcFile = new File(parentDir, ".yarnrc");

		if (!npmrcFile.exists()) {
			addMessage(fileName,
				"Missing configuration file(s)! Every module with " +
					"package.json must have configuration to prevent " +
						"automatic script execution during package installation." );
		}

}