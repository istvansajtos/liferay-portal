/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.gradle.plugins.patcher.internal.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Istvan Sajtos
 */
public class JarPostProcessorUtilTest {

	@Before
	public void setUp() throws Exception {
		_jar = _getTestJar();
	}

	@After
	public void tearDown() throws Exception {
		Files.delete(_jar.toPath());
	}

	@Test
	public void testProcessJar() throws Exception {

		// Values we want to apply

		String groupId = "new.group";
		String artifactId = "new-artifact";
		String version = "1.2.3";

		// Call method under test

		JarPostProcessorUtil.processJar(_jar, groupId, artifactId, version);

		// Open resulting JAR

		try (JarFile jarFile = new JarFile(_jar)) {

			// Manifest checks

			Manifest manifest = jarFile.getManifest();

			Assert.assertNotNull(
				"Manifest should be present after processing", manifest);

			Attributes attributes = manifest.getMainAttributes();

			Assert.assertEquals(
				"Bundle-SymbolicName should be set to artifactId", artifactId,
				attributes.getValue("Bundle-SymbolicName"));
			Assert.assertEquals(
				"Bundle-Version should be set to version", version,
				attributes.getValue("Bundle-Version"));

			// pom.properties checks

			JarEntry pomPropertiesEntry = jarFile.getJarEntry("pom.properties");

			Assert.assertNotNull(
				"pom.properties should exist in jar", pomPropertiesEntry);

			try (InputStream inputStream = jarFile.getInputStream(
					pomPropertiesEntry)) {

				Properties newProps = new Properties();

				newProps.load(inputStream);

				Assert.assertEquals(groupId, newProps.getProperty("groupId"));
				Assert.assertEquals(
					artifactId, newProps.getProperty("artifactId"));
				Assert.assertEquals(version, newProps.getProperty("version"));
			}

			// pom.xml checks

			JarEntry pomXmlEntry = jarFile.getJarEntry("pom.xml");

			Assert.assertNotNull("pom.xml should exist in jar", pomXmlEntry);

			try (InputStream inputStream = jarFile.getInputStream(
					pomXmlEntry)) {

				String content = _readAll(inputStream);

				Assert.assertTrue(
					"pom.xml should contain updated groupId",
					content.contains("<groupId>" + groupId + "</groupId>"));
				Assert.assertTrue(
					"pom.xml should contain updated artifactId",
					content.contains(
						"<artifactId>" + artifactId + "</artifactId>"));
				Assert.assertTrue(
					"pom.xml should contain updated version",
					content.contains("<version>" + version + "</version>"));
			}
		}
	}

	private File _getTestJar() throws Exception {
		Path tempDirPath = Files.createTempDirectory("jartest");

		File tempDir = tempDirPath.toFile();

		tempDir.deleteOnExit();

		Path jarPath = tempDirPath.resolve("test.jar");

		// Create initial JAR with manifest, pom.xml and pom.properties

		Manifest manifest = new Manifest();

		Attributes attributes = manifest.getMainAttributes();

		attributes.putValue("Manifest-Version", "1.0");
		attributes.putValue("Original-Attr", "original");

		try (JarOutputStream jarOutputStream = new JarOutputStream(
				Files.newOutputStream(jarPath), manifest)) {

			// pom.xml

			JarEntry pomXmlEntry = new JarEntry("pom.xml");

			jarOutputStream.putNextEntry(pomXmlEntry);

			String pomXml =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<project>\n" +
					"  <artifactId>old-artifact</artifactId>\n</project>\n";

			jarOutputStream.write(pomXml.getBytes(StandardCharsets.UTF_8));

			jarOutputStream.closeEntry();

			// pom.properties

			JarEntry pomPropertiesEntry = new JarEntry("pom.properties");

			jarOutputStream.putNextEntry(pomPropertiesEntry);

			Properties props = new Properties();

			props.setProperty("groupId", "old.group");
			props.setProperty("artifactId", "old-artifact");
			props.setProperty("version", "0.0.1");

			ByteArrayOutputStream byteArrayOutputStream =
				new ByteArrayOutputStream();

			props.store(byteArrayOutputStream, null);

			jarOutputStream.write(byteArrayOutputStream.toByteArray());

			jarOutputStream.closeEntry();
		}

		return jarPath.toFile();
	}

	private String _readAll(InputStream inputStream) throws Exception {
		ByteArrayOutputStream byteArrayOutputStream =
			new ByteArrayOutputStream();

		byte[] buf = new byte[8192];

		int r;

		while ((r = inputStream.read(buf)) != -1) {
			byteArrayOutputStream.write(buf, 0, r);
		}

		return byteArrayOutputStream.toString(StandardCharsets.UTF_8.name());
	}

	private File _jar;

}