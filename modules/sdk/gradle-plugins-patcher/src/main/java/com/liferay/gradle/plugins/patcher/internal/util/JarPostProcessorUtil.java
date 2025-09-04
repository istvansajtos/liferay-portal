/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.gradle.plugins.patcher.internal.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Istvan Sajtos
 */
public class JarPostProcessorUtil {

	public static void processJar(
			File jar, String groupId, String artifactId, String version)
		throws IOException {

		Manifest manifest = new JarFile(
			jar
		).getManifest();

		if (manifest != null) {
			Attributes attributes = manifest.getMainAttributes();

			attributes.putValue("Bundle-SymbolicName", artifactId);
			attributes.putValue("Bundle-Version", version);
		}

		File tempFile = new File(jar.getParent(), jar.getName() + ".tmp");

		try (JarInputStream jarInputStream = new JarInputStream(
				new FileInputStream(jar));
			JarOutputStream jarOutputStream = (manifest != null) ?
				new JarOutputStream(new FileOutputStream(tempFile), manifest) :
					new JarOutputStream(new FileOutputStream(tempFile))) {

			/*File tempFile = new File(jar.getParent(), jar.getName() + ".tmp");

			Manifest manifest = jarInputStream.getManifest();

			if (manifest != null) {
				Attributes attributes = manifest.getMainAttributes();

				attributes.putValue("Bundle-SymbolicName", artifactId);
				attributes.putValue("Bundle-Version", version);

				jarOutputStream = new JarOutputStream(
					new FileOutputStream(tempFile), manifest);
			}
			else {
				jarOutputStream = new JarOutputStream(
					new FileOutputStream(tempFile));
			}*/

			JarEntry jarEntry = null;

			while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
				String fileName = jarEntry.getName();

				fileName = fileName.toLowerCase();

				if (fileName.endsWith("manifest.mf")) {
					continue;
				}

				byte[] content = jarInputStream.readAllBytes();

				if (fileName.endsWith("pom.xml")) {
					content = _processPomXml(
						content, groupId, artifactId, version);
				}
				else if (fileName.endsWith("pom.properties")) {
					content = _processPomProperties(
						content, groupId, artifactId, version);
				}

				jarOutputStream.putNextEntry(new JarEntry(jarEntry.getName()));

				jarOutputStream.write(content);

				jarOutputStream.closeEntry();
			}

			Files.move(
				tempFile.toPath(), jar.toPath(),
				StandardCopyOption.REPLACE_EXISTING);

			System.out.println(">>>JarPostProcessorUtil; jar path is " + jar.toPath);
		}
		catch (Exception exception) {
			throw new RuntimeException("Failed to post-process JAR", exception);
		}
	}

	private static String _insertTag(
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

	private static byte[] _processPomProperties(
			byte[] bytes, String groupId, String artifactId, String version)
		throws IOException {

		Properties props = new Properties();

		props.load(new ByteArrayInputStream(bytes));

		props.setProperty("groupId", groupId);
		props.setProperty("artifactId", artifactId);
		props.setProperty("version", version);

		try (ByteArrayOutputStream byteArrayOutputStream =
				new ByteArrayOutputStream()) {

			props.store(byteArrayOutputStream, null);

			return byteArrayOutputStream.toByteArray();
		}
	}

	private static byte[] _processPomXml(
			byte[] bytes, String groupId, String artifactId, String version)
		throws IOException {

		String content = new String(bytes, StandardCharsets.UTF_8);

		Matcher artifactIdMatcher = _artifactIdPattern.matcher(content);

		if (!artifactIdMatcher.find()) {
			throw new IllegalStateException(
				"No <artifactId> found in pom.content");
		}

		// Replace artifactId

		String indent = artifactIdMatcher.group(1);

		String artifactIdTag =
			indent + "<artifactId>" + artifactId + "</artifactId>";

		content = artifactIdMatcher.replaceFirst(
			Matcher.quoteReplacement(artifactIdTag));

		// Insert or update groupId

		Matcher groupIdMatcher = _groupIdPattern.matcher(content);

		if (groupIdMatcher.find()) {
			content = groupIdMatcher.replaceFirst(
				indent + "<groupId>" + groupId + "</groupId>");
		}
		else {
			content = _insertTag(
				content, "artifactId", "groupId", groupId, indent);
		}

		// Insert or update version

		Matcher versionMatcher = _versionPattern.matcher(content);

		if (versionMatcher.find()) {
			content = versionMatcher.replaceFirst(
				indent + "<version>" + version + "</version>");
		}
		else {
			content = _insertTag(
				content, "groupId", "version", version, indent);
		}

		return content.getBytes(StandardCharsets.UTF_8);
	}

	private static final Pattern _artifactIdPattern = Pattern.compile(
		"(?m)^([ \t]*)<artifactId>(.*?)</artifactId>");
	private static final Pattern _groupIdPattern = Pattern.compile(
		"(?m)^([ \t]*)<groupId>(.*?)</groupId>");
	private static final Pattern _versionPattern = Pattern.compile(
		"(?m)^([ \t]*)<version>(.*?)</version>");

}