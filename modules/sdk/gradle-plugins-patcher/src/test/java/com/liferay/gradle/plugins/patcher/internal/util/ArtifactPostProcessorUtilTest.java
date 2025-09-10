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
public class ArtifactPostProcessorUtilTest {

	@Before
	public void setUp() throws Exception {
		_createTestJar();
	}

	@After
	public void tearDown() throws Exception {
		Files.delete(_jar.toPath());
		Files.delete(_tempDir.toPath());
	}

	@Test
	public void testPostProcess() throws Exception {
		String groupId = "new.group";
		String artifactId = "new-artifact";
		String version = "1.2.3";

		ArtifactPostProcessorUtil.postProcess(
			_jar, groupId, artifactId, version);

		try (JarFile jarFile = new JarFile(_jar)) {
			_assertManifestValues(jarFile, artifactId, version);
			_assertPomPropertiesValues(jarFile, groupId, artifactId, version);
			_assertPomXmlValues(jarFile, groupId, artifactId, version);
		}
	}

	private void _assertManifestValues(
			JarFile jarFile, String artifactId, String version)
		throws Exception {

		Manifest manifest = jarFile.getManifest();

		Attributes attributes = manifest.getMainAttributes();

		Assert.assertEquals(
			artifactId, attributes.getValue("Bundle-SymbolicName"));
		Assert.assertEquals(version, attributes.getValue("Bundle-Version"));
	}

	private void _assertPomPropertiesValues(
			JarFile jarFile, String groupId, String artifactId, String version)
		throws Exception {

		try (InputStream inputStream = jarFile.getInputStream(
				jarFile.getJarEntry("pom.properties"))) {

			Properties props = new Properties();

			props.load(inputStream);

			Assert.assertEquals(groupId, props.getProperty("groupId"));
			Assert.assertEquals(artifactId, props.getProperty("artifactId"));
			Assert.assertEquals(version, props.getProperty("version"));
		}
	}

	private void _assertPomXmlValues(
			JarFile jarFile, String groupId, String artifactId, String version)
		throws Exception {

		try (InputStream inputStream = jarFile.getInputStream(
				jarFile.getJarEntry("pom.xml"))) {

			String content = new String(
				inputStream.readAllBytes(), StandardCharsets.UTF_8);

			Assert.assertTrue(
				content.contains("<groupId>" + groupId + "</groupId>"));
			Assert.assertTrue(
				content.contains(
					"<artifactId>" + artifactId + "</artifactId>"));
			Assert.assertTrue(
				content.contains("<version>" + version + "</version>"));
		}
	}

	private void _createTestJar() throws Exception {
		String groupId = "old.group";
		String artifactId = "old-artifact";
		String version = "0.0.1";

		Path tempDirPath = Files.createTempDirectory("jartest");

		_tempDir = tempDirPath.toFile();

		_tempDir.deleteOnExit();

		Path jarPath = tempDirPath.resolve("test.jar");

		Manifest manifest = new Manifest();

		Attributes attributes = manifest.getMainAttributes();

		attributes.putValue("Manifest-Version", "1.0");
		attributes.putValue("Bundle-SymbolicName", artifactId);
		attributes.putValue("Bundle-Version", version);

		try (JarOutputStream jarOutputStream = new JarOutputStream(
				Files.newOutputStream(jarPath), manifest)) {

			jarOutputStream.putNextEntry(new JarEntry("pom.xml"));

			String pomXml =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<project>\n" +
					"  <artifactId>old-artifact</artifactId>\n</project>\n";

			jarOutputStream.write(pomXml.getBytes(StandardCharsets.UTF_8));

			jarOutputStream.closeEntry();

			jarOutputStream.putNextEntry(new JarEntry("pom.properties"));

			Properties props = new Properties();

			props.setProperty("groupId", groupId);
			props.setProperty("artifactId", artifactId);
			props.setProperty("version", version);

			ByteArrayOutputStream byteArrayOutputStream =
				new ByteArrayOutputStream();

			props.store(byteArrayOutputStream, null);

			jarOutputStream.write(byteArrayOutputStream.toByteArray());

			jarOutputStream.closeEntry();
		}

		_jar = jarPath.toFile();
	}

	private File _jar;
	private File _tempDir;

}