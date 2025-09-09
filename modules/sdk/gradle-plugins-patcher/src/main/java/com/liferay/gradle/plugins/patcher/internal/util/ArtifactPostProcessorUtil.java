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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Istvan Sajtos
 */
public class ArtifactPostProcessorUtil {

	public static void postProcess(
			File jar, String groupId, String artifactId, String version)
		throws Exception {

		JarFile jarFile = new JarFile(jar);

		Manifest manifest = jarFile.getManifest();

		if (manifest != null) {
			Attributes attributes = manifest.getMainAttributes();

			attributes.putValue("Bundle-SymbolicName", artifactId);
			attributes.putValue("Bundle-Version", version);
		}

		File tempFile = new File(jar.getParent(), jar.getName() + ".tmp");

		try (JarInputStream jarInputStream = new JarInputStream(
				new FileInputStream(jar));
			JarOutputStream jarOutputStream = _getJarOutputStream(
				manifest, tempFile)) {

			JarEntry jarEntry;

			while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
				String fileName = jarEntry.getName();

				fileName = fileName.toLowerCase();

				if (fileName.endsWith("manifest.mf")) {
					continue;
				}

				byte[] content = jarInputStream.readAllBytes();

				if (fileName.endsWith("pom.xml")) {
					content = _getUpdatedPomXml(
						content, groupId, artifactId, version);
				}
				else if (fileName.endsWith("pom.properties")) {
					content = _getUpdatedPomProperties(
						content, groupId, artifactId, version);
				}

				jarOutputStream.putNextEntry(new JarEntry(jarEntry.getName()));

				jarOutputStream.write(content);

				jarOutputStream.closeEntry();
			}

			Files.move(
				tempFile.toPath(), jar.toPath(),
				StandardCopyOption.REPLACE_EXISTING);

			System.out.println(
				">>>ArtifactPostProcessorUtil; jar path is " + jar.toPath());
		}
	}

	private static JarOutputStream _getJarOutputStream(
			Manifest manifest, File tempFile)
		throws Exception {

		if (manifest != null) {
			return new JarOutputStream(
				new FileOutputStream(tempFile), manifest);
		}

		return new JarOutputStream(new FileOutputStream(tempFile));
	}

	private static byte[] _getUpdatedPomProperties(
			byte[] bytes, String groupId, String artifactId, String version)
		throws IOException {

		System.out.println(new String(bytes, StandardCharsets.UTF_8));
		System.out.println("\n\n\n");

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

	private static byte[] _getUpdatedPomXml(
			byte[] bytes, String groupId, String artifactId, String version)
		throws Exception {

		System.out.println(new String(bytes, StandardCharsets.UTF_8));

		DocumentBuilderFactory documentBuilderFactory =
			DocumentBuilderFactory.newInstance();

		documentBuilderFactory.setIgnoringElementContentWhitespace(true);
		documentBuilderFactory.setFeature(
			"http://apache.org/xml/features/nonvalidating/load-external-dtd",
			false);

		DocumentBuilder documentBuilder =
			documentBuilderFactory.newDocumentBuilder();

		Document document;

		try (ByteArrayInputStream byteArrayInputStream =
				new ByteArrayInputStream(bytes)) {

			document = documentBuilder.parse(byteArrayInputStream);
		}

		Element project = document.getDocumentElement();

		_updateOrCreateChild(
			project, "groupId", groupId, document, "artifactId", true);

		_updateOrCreateChild(
			project, "artifactId", artifactId, document, null, null);

		_updateOrCreateChild(
			project, "version", version, document, "artifactId", false);

		_removeWhitespaceNodes(project);

		TransformerFactory transformerFactory =
			TransformerFactory.newInstance();

		Transformer transformer = transformerFactory.newTransformer();

		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		//transformer.setOutputProperty(
		//	"{http://xml.apache.org/xslt}indent-amount", "4");

		try (ByteArrayOutputStream byteArrayOutputStream =
				new ByteArrayOutputStream()) {

			transformer.transform(
				new DOMSource(document),
				new StreamResult(byteArrayOutputStream));

			return byteArrayOutputStream.toByteArray();
		}
	}

	private static void _removeWhitespaceNodes(Element element) {
		NodeList children = element.getChildNodes();

		for (int i = children.getLength() - 1; i >= 0; i--) {
			Node child = children.item(i);

			if (child.getNodeType() == Node.TEXT_NODE) {
				String textContent = child.getTextContent();

				textContent = textContent.trim();

				if (textContent.isEmpty()) {
					element.removeChild(child);
				}
			}
			else if (child.getNodeType() == Node.ELEMENT_NODE) {
				_removeWhitespaceNodes((Element)child);
			}
		}
	}

	private static void _updateOrCreateChild(
		Element parent, String tagName, String value, Document document,
		String siblingTag, Boolean insertBefore) {

		NodeList nodes = parent.getElementsByTagName(tagName);

		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);

			if (node.getParentNode() == parent) {
				node.setTextContent(value);

				return;
			}
		}

		Element newElement = document.createElement(tagName);

		newElement.setTextContent(value);

		if (siblingTag != null) {
			NodeList siblingNodes = parent.getElementsByTagName(siblingTag);

			for (int i = 0; i < siblingNodes.getLength(); i++) {
				Node node = siblingNodes.item(i);

				if (node.getParentNode() == parent) {
					if (!insertBefore && (node.getNextSibling() != null)) {
						node = node.getNextSibling();
					}

					parent.insertBefore(newElement, node);

					return;
				}
			}
		}

		parent.appendChild(newElement);
	}

}