/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.gradle.plugins.patcher;

import aQute.bnd.osgi.Constants;

import com.liferay.gradle.plugins.extensions.BundleExtension;
import com.liferay.gradle.plugins.patcher.internal.util.JarPostProcessorUtil;
import com.liferay.gradle.plugins.util.BndUtil;

import java.io.File;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.Jar;

/**
 * @author Istvan Sajtos
 */
public class JarPostProcessorPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		System.out.println(">>> JarPostProcessorPlugin");

		project.afterEvaluate(
			p -> {
				TaskContainer taskContainer = p.getTasks();

				Jar jarTask = (Jar)taskContainer.findByName("jar");

				jarTask.doLast(
					task -> {
						try {
							File file = jarTask.getArchiveFile(
							).get(
							).getAsFile();

							BundleExtension bundleExtension =
								BndUtil.getBundleExtension(
									project.getExtensions());

							String artifactId = bundleExtension.getInstruction(
								Constants.BUNDLE_SYMBOLICNAME);

							// String artifactId = project.getName();

							String groupId = String.valueOf(project.getGroup());

							String version = String.valueOf(
								project.getVersion());

							JarPostProcessorUtil.processJar(
								file, groupId, artifactId, version);
						}
						catch (Exception exception) {
							throw new RuntimeException(
								"Failed to post-process JAR", exception);
						}
					});
			});
	}

}