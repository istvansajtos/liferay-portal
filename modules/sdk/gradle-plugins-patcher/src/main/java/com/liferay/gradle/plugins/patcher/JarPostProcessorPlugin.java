package com.liferay.gradle.plugins.patcher;

import aQute.bnd.osgi.Constants;

import com.liferay.gradle.plugins.util.BndUtil;
import com.liferay.gradle.plugins.extensions.BundleExtension;


import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.Task;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

//import org.w3c.dom.*;

import java.io.*;
import java.nio.file.*;
import java.util.jar.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class JarPostProcessorPlugin implements Plugin<Project> {

/*	@Override
	public void apply(Project project) {
		System.out.println(">>> JarPostProcessorPlugin.apply");
		project.getTasks().withType(Jar.class).configureEach(jarTask -> {
			jarTask.doLast(task -> {
				File jarFile = jarTask.getArchiveFile().get().getAsFile();
				File tempJarFile = new File(jarFile.getParentFile(), jarFile.getName() + ".tmp");

				System.out.println(">>> JarPostProcessorPlugin.apply jarTask");

				try (
					JarInputStream jarInputStream = new JarInputStream(new FileInputStream(jarFile));
					JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tempJarFile));
				) {
					JarEntry entry;
					while ((entry = jarInputStream.getNextJarEntry()) != null) {
						String name = entry.getName();

						System.out.println(">>> JarPostProcessorPlugin.apply - " + name);

						if (name.equals("META-INF/pom.xml") || name.equals("META-INF/pom.properties")) {
							continue;
						}

						jarOutputStream.putNextEntry(new JarEntry(name));
						byte[] buffer = new byte[4096];
						int bytesRead;
						while ((bytesRead = jarInputStream.read(buffer)) != -1) {
							jarOutputStream.write(buffer, 0, bytesRead);
						}
						jarOutputStream.closeEntry();
					}

				} catch (IOException e) {
					throw new RuntimeException("Failed to post-process JAR file", e);
				}

				try {
					Files.move(tempJarFile.toPath(), jarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					throw new RuntimeException("Failed to replace original JAR with processed version", e);
				}

				project.getLogger().lifecycle("Post-processed JAR: removed pom.xml and pom.properties");
			});
		});
	}

	@Override
	public void apply(Project project) {
		System.out.println(">>> JarPostProcessorPlugin.apply");
		project.afterEvaluate(p -> {
			Jar jarTask = (Jar)p.getTasks().findByName("jar");

			jarTask.doLast(task -> {
				File jarFile = jarTask.getArchiveFile().get().getAsFile();
				File tempJarFile = new File(jarFile.getParentFile(), jarFile.getName() + ".tmp");

				//GradleUtil.getArchivesBaseName(project)

				System.out.println(">>> JarPostProcessorPlugin.apply jarTask");

				try (
					JarInputStream jarInputStream = new JarInputStream(new FileInputStream(jarFile));
					JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tempJarFile));
				) {
					JarEntry entry;
					while ((entry = jarInputStream.getNextJarEntry()) != null) {
						String name = entry.getName();

						System.out.println(">>> JarPostProcessorPlugin.apply - " + name);

						if (name.endsWith("/pom.xml") || name.endsWith("/pom.properties")) {
							continue;
						}

						jarOutputStream.putNextEntry(new JarEntry(name));
						byte[] buffer = new byte[4096];
						int bytesRead;
						while ((bytesRead = jarInputStream.read(buffer)) != -1) {
							jarOutputStream.write(buffer, 0, bytesRead);
						}
						jarOutputStream.closeEntry();
					}

				} catch (IOException e) {
					throw new RuntimeException("Failed to post-process JAR file", e);
				}

				try {
					Files.move(tempJarFile.toPath(), jarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					throw new RuntimeException("Failed to replace original JAR with processed version", e);
				}

				project.getLogger().lifecycle("Post-processed JAR: removed pom.xml and pom.properties");
			});
		});
	}*/

	@Override
	public void apply(Project project) {
		System.out.println(">>> JarPostProcessorPlugin.apply");
		project.afterEvaluate(p -> {
			Jar jarTask = (Jar)p.getTasks().findByName("jar");

			jarTask.doLast(task -> {
				try {
					File jarFile = jarTask.getArchiveFile().get().getAsFile();
	
					String groupId = String.valueOf(project.getGroup());
					//String artifactId = GradleUtil.getArchivesBaseName(project);

					BundleExtension bundleExtension =
						BndUtil.getBundleExtension(project.getExtensions());

					String artifactId = bundleExtension.getInstruction(
						Constants.BUNDLE_SYMBOLICNAME);
					// String artifactId = project.getName();
					String version = project.getVersion().toString();
	
					System.out.println(">>> JarPostProcessorPlugin.apply - groupId " + groupId);
					System.out.println(">>> JarPostProcessorPlugin.apply - artifactId " + artifactId);
					System.out.println(">>> JarPostProcessorPlugin.apply - version " + version);
	
					JarInputStream jarInputStream = new JarInputStream(new FileInputStream(jarFile));
	
					Manifest manifest = jarInputStream.getManifest();
	
					if (manifest == null) {
						manifest = new Manifest();
						manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
					}
	
					Attributes attributes = manifest.getMainAttributes();
	
					attributes.putValue("Bundle-SymbolicName", artifactId);
					attributes.putValue("Bundle-Version", version);
	
					File tempJarFile = new File(jarFile.getParentFile(), jarFile.getName() + ".tmp");
	
					JarOutputStream jarOutputStream =
						new JarOutputStream(new FileOutputStream(tempJarFile), manifest);
					//JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tempJarFile), jarInputStream.getManifest());
					//JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tempJarFile);
	
					JarEntry entry;
	
					while ((entry = jarInputStream.getNextJarEntry()) != null) {
						String name = entry.getName();

						name = name.toLowerCase();

						if (name.endsWith("manifest.mf")) {
							continue;
						}
	
						ByteArrayOutputStream entryContent = new ByteArrayOutputStream();
	
						byte[] buffer = new byte[4096];
	
						int bytesRead;
	
						while ((bytesRead = jarInputStream.read(buffer)) != -1) {
							entryContent.write(buffer, 0, bytesRead);
						}
	
						byte[] newContent;
	
						if (name.endsWith("pom.xml")) {
							String xml = new String(entryContent.toByteArray(), StandardCharsets.UTF_8);

							Pattern artifactIdPattern = Pattern.compile("(?m)^([ \t]*)<artifactId>(.*?)</artifactId>");
							Pattern groupIdPattern = Pattern.compile("(?m)^([ \t]*)<groupId>(.*?)</groupId>");
							Pattern versionPattern = Pattern.compile("(?m)^([ \t]*)<version>(.*?)</version>");

							Matcher artifactIdMatcher = artifactIdPattern.matcher(xml);

							if (!artifactIdMatcher.find()) {
								throw new IllegalStateException("No <artifactId> found in pom.xml");
							}

							// Replace artifactId
							String indent = artifactIdMatcher.group(1);
							String artifactIdTag = indent + "<artifactId>" + newArtifactId + "</artifactId>";
							xml = artifactIdMatcher.replaceFirst(Matcher.quoteReplacement(artifactIdTag));

							// Insert or update groupId
							Matcher groupIdMatcher = groupIdPattern.matcher(xml);

							if (groupIdMatcher.find()) {
								xml = groupIdMatcher.replaceFirst(indent + "<groupId>" + newGroupId + "</groupId>");
							} else {
								//xml = insertAfterTag(xml, "artifactId", "groupId", newGroupId, indent);
								int insertPos = artifactIdMatcher.end();
								String line = "\n" + indent + "<" + newTag + ">" + newValue + "</" + newTag + ">";
								return xml.substring(0, insertPos) + line + xml.substring(insertPos);
							}

							// Insert or update version
							Matcher versionMatcher = versionPattern.matcher(xml);
							if (versionMatcher.find()) {
								xml = versionMatcher.replaceFirst(
									indent + "<version>" + newVersion + "</version>");
							} else {
								xml = insertAfterTag(xml, "groupId", "version", newVersion, indent);
							}

							/*String xml = new String(entryContent.toByteArray(), StandardCharsets.UTF_8);

							// Parse XML using JSoup (note: use Parser.xmlParser())
							Document document = Jsoup.parse(xml, "", Parser.xmlParser());

							Element element = document.selectFirst("project");

							if (element == null) {
								throw new IllegalStateException("No <project> element found in pom.xml");
							}

							System.out.println("########");
							System.out.println(element);

							// Update or insert <artifactId>
							setElement(element, "artifactId", artifactId, null);

							// Update or insert <groupId> after <artifactId>
							setElement(element, "groupId", groupId, "artifactId");

							// Update or insert <version> after <groupId>
							setElement(element, "version", version, "groupId");

							// Output with consistent indentation
							newContent = document.outerHtml().getBytes(StandardCharsets.UTF_8);*/

							/*String xml = new String(entryContent.toByteArray(), StandardCharsets.UTF_8);
	
							DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
							factory.setNamespaceAware(true);
	
							DocumentBuilder builder = factory.newDocumentBuilder();
	
							Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
	
							Element projectElement = doc.getDocumentElement(); // <project>
	
							// Only change the direct children of <project>
							updateTextNodeIfExists(projectElement, "groupId", groupId);
							updateTextNodeIfExists(projectElement, "artifactId", artifactId);
							updateTextNodeIfExists(projectElement, "version", version);
	
							// Convert DOM back to byte array
							Transformer transformer = TransformerFactory.newInstance().newTransformer();
	
							transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	
							ByteArrayOutputStream out = new ByteArrayOutputStream();
	
							transformer.transform(new DOMSource(doc), new StreamResult(out));
	
							newContent = out.toByteArray();*/
	
							/*xml = xml.replaceAll("<groupId>.*?</groupId>", "<groupId>" + groupId + "</groupId>");
							xml = xml.replaceAll("<artifactId>.*?</artifactId>",
									"<artifactId>" + artifactId + "</artifactId>");
							xml = xml.replaceAll("<version>.*?</version>", "<version>" + version + "</version>");
							newContent = xml.getBytes(StandardCharsets.UTF_8);*/
						} else if (name.endsWith("pom.properties")) {
							Properties props = new Properties();

							props.load(new ByteArrayInputStream(entryContent.toByteArray()));

							props.setProperty("groupId", groupId);
							props.setProperty("artifactId", artifactId);
							props.setProperty("version", version);

							ByteArrayOutputStream propsOut = new ByteArrayOutputStream();

							props.store(propsOut, null);

							newContent = propsOut.toByteArray();
						} else {
							newContent = entryContent.toByteArray(); // Leave other entries unchanged
						}

						jarOutputStream.putNextEntry(new JarEntry(entry.getName()));

						jarOutputStream.write(newContent);

						jarOutputStream.closeEntry();
					}

					jarInputStream.close();
					jarOutputStream.close();

					Files.move(tempJarFile.toPath(), jarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					//project.getLogger().lifecycle("Updated pom.xml and pom.properties in: " + jarFile.getName());
				} catch (Exception e) {
					throw new RuntimeException("Failed to post-process JAR", e);
				}
			});
		});
	}

	private void setElement(Element parent, String tag, String newValue, String insertAfterTag) {
		System.out.println("########");
		System.out.println(tag);
		System.out.println("########");

		Element existing = parent.selectFirst("> " + tag);
		if (existing != null) {
			existing.text(newValue);
			return;
		}
	
		Element newElem = new Element(tag).text(newValue);
	
		if (insertAfterTag != null) {
			Element afterElem = parent.selectFirst("> " + insertAfterTag);
			if (afterElem != null) {
				afterElem.after(new TextNode("\n  ", ""), newElem);
			} else {
				parent.appendChild(new TextNode("\n  ", ""));
				parent.appendChild(newElem);
			}
		} else {
			parent.appendChild(newElem);
		}
	}

	private static String insertAfterTag(String xml, String afterTag, String newTag, String newValue, String indent) {
		Pattern pattern = Pattern.compile("(?m)^" + Pattern.quote(indent) + "<" + afterTag + ">.*?</" + afterTag + ">");
		Matcher matcher = pattern.matcher(xml);
		if (matcher.find()) {
			int insertPos = matcher.end();
			String insert = "\n" + indent + "<" + newTag + ">" + newValue + "</" + newTag + ">";
			return xml.substring(0, insertPos) + insert + xml.substring(insertPos);
		}
		throw new IllegalStateException("Could not find <" + afterTag + "> to insert after.");
	}
/*
	private void updateTextNodeIfExists(Element parent, String tagName, String value) {
		NodeList list = parent.getElementsByTagName(tagName);

		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);

			if (node.getParentNode().equals(parent)) { // only direct child
				node.setTextContent(value);

				return;
			}
		}

		Element element = parent.getOwnerDocument().createElement(tagName);

		element.setTextContent(value);

		parent.appendChild(element);
	}*/
}
