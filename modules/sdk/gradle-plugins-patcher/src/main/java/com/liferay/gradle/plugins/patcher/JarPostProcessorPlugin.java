/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.gradle.plugins.patcher;

import aQute.bnd.osgi.Constants;

import com.liferay.gradle.plugins.extensions.BundleExtension;
import com.liferay.gradle.plugins.util.BndUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

							System.out.println(
								">>> JarPostProcessorPlugin - groupId " +
									groupId);
							System.out.println(
								">>> JarPostProcessorPlugin - artifactId " +
									artifactId);
							System.out.println(
								">>> JarPostProcessorPlugin - version " +
									version);

							JarInputStream jarInputStream = new JarInputStream(
								new FileInputStream(file));

							Manifest manifest = jarInputStream.getManifest();

							JarOutputStream jarOutputStream = null;

							File tempFile = new File(
								file.getParentFile(), file.getName() + ".tmp");

							if (manifest != null) {
								Attributes attributes =
									manifest.getMainAttributes();

								attributes.putValue(
									"Bundle-SymbolicName", artifactId);
								attributes.putValue("Bundle-Version", version);

								jarOutputStream = new JarOutputStream(
									new FileOutputStream(tempFile), manifest);
							}
							else {
								jarOutputStream = new JarOutputStream(
									new FileOutputStream(tempFile));
							}

							JarEntry jarEntry = null;

							while ((jarEntry =
										jarInputStream.getNextJarEntry()) !=
											null) {

								String fileName = jarEntry.getName();

								fileName = fileName.toLowerCase();

								if (fileName.endsWith("manifest.mf")) {
									continue;
								}

								ByteArrayOutputStream byteArrayOutputStream =
									new ByteArrayOutputStream();

								byte[] buffer = new byte[4096];

								int bytesRead;

								while ((bytesRead = jarInputStream.read(
											buffer)) != -1) {

									byteArrayOutputStream.write(
										buffer, 0, bytesRead);
								}

								byte[] newContent = null;

								if (fileName.endsWith("pom.xml")) {
									String xml = new String(
										byteArrayOutputStream.toByteArray(),
										StandardCharsets.UTF_8);

									Matcher artifactIdMatcher =
										_artifactIdPattern.matcher(xml);

									if (!artifactIdMatcher.find()) {
										throw new IllegalStateException(
											"No <artifactId> found in pom.xml");
									}

									// Replace artifactId

									String indent = artifactIdMatcher.group(1);

									String artifactIdTag =
										indent + "<artifactId>" + artifactId +
											"</artifactId>";

									xml = artifactIdMatcher.replaceFirst(
										Matcher.quoteReplacement(
											artifactIdTag));

									// Insert or update groupId

									Matcher groupIdMatcher =
										_groupIdPattern.matcher(xml);

									if (groupIdMatcher.find()) {
										xml = groupIdMatcher.replaceFirst(
											indent + "<groupId>" + groupId +
												"</groupId>");
									}
									else {
										xml = _insertTag(
											xml, "artifactId", "groupId",
											groupId, indent);
									}

									// Insert or update version

									Matcher versionMatcher =
										_versionPattern.matcher(xml);

									if (versionMatcher.find()) {
										xml = versionMatcher.replaceFirst(
											indent + "<version>" + version +
												"</version>");
									}
									else {
										xml = _insertTag(
											xml, "groupId", "version", version,
											indent);
									}

									newContent = xml.getBytes(
										StandardCharsets.UTF_8);
								}
								else if (fileName.endsWith("pom.properties")) {
									Properties props = new Properties();

									props.load(
										new ByteArrayInputStream(
											byteArrayOutputStream.
												toByteArray()));

									props.setProperty("groupId", groupId);
									props.setProperty("artifactId", artifactId);
									props.setProperty("version", version);

									ByteArrayOutputStream
										propsByteArrayOutputStream =
											new ByteArrayOutputStream();

									props.store(
										propsByteArrayOutputStream, null);

									newContent =
										propsByteArrayOutputStream.
											toByteArray();
								}
								else {
									newContent =
										byteArrayOutputStream.toByteArray(); // Leave other entries unchanged
								}

								jarOutputStream.putNextEntry(
									new JarEntry(jarEntry.getName()));

								jarOutputStream.write(newContent);

								jarOutputStream.closeEntry();
							}

							jarInputStream.close();
							jarOutputStream.close();

							Files.move(
								tempFile.toPath(), file.toPath(),
								StandardCopyOption.REPLACE_EXISTING);
							//project.getLogger().lifecycle("Updated pom.xml and pom.properties in: " + file.getName());
						}
						catch (Exception exception) {
							throw new RuntimeException(
								"Failed to post-process JAR", exception);
						}
					});
			});
	}

	private String _insertTag(
		String xml, String previousTag, String newTag, String value,
		String indent) {

		Pattern pattern = Pattern.compile(
			"(?m)^" + Pattern.quote(indent) + "<" + previousTag + ">.*?</" +
				previousTag + ">");

		Matcher matcher = pattern.matcher(xml);

		if (matcher.find()) {
			int index = matcher.end();

			String line =
				"\n" + indent + "<" + newTag + ">" + value + "</" + newTag +
					">";

			return xml.substring(0, index) + line + xml.substring(index);
		}

		throw new IllegalStateException(
			"Could not find <" + previousTag + "> to insert after.");
	}

	/*	private void _setElement(
			Element parent, String tag, String newValue, String insertAfterTag) {

			Element existing = parent.selectFirst("> " + tag);

			if (existing != null) {
				existing.text(newValue);

				return;
			}

			Element newElem = new Element(
				tag
			).text(
				newValue
			);

			if (insertAfterTag != null) {
				Element afterElem = parent.selectFirst("> " + insertAfterTag);

				if (afterElem != null) {
					afterElem.after(new TextNode("\n  ", ""), newElem);
				}
				else {
					parent.appendChild(new TextNode("\n  ", ""));
					parent.appendChild(newElem);
				}
			}
			else {
				parent.appendChild(newElem);
			}
		}
	*/

	private static final Pattern _artifactIdPattern = Pattern.compile(
		"(?m)^([ \t]*)<artifactId>(.*?)</artifactId>");
	private static final Pattern _groupIdPattern = Pattern.compile(
		"(?m)^([ \t]*)<groupId>(.*?)</groupId>");
	private static final Pattern _versionPattern = Pattern.compile(
		"(?m)^([ \t]*)<version>(.*?)</version>");

}