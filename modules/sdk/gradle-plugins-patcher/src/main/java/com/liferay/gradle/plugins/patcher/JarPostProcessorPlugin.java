package com.liferay.gradle.plugins.patcher;

import aQute.bnd.osgi.Constants;

import com.liferay.gradle.plugins.extensions.BundleExtension;
import com.liferay.gradle.plugins.util.BndUtil;

import java.io.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import java.util.Properties;
import java.util.jar.*;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.Jar;

public class JarPostProcessorPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		System.out.println(">>> JarPostProcessorPlugin");

		project.afterEvaluate(
			p -> {
				TaskContainer taskContainer = (Jar)p.getTasks();

				Jar jarTask = taskContainer.findByName("jar");

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

							String version = String.valueOf(project.getVersion());

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
								file.getParentFile(), 
								file.getName() + ".tmp");

							if (manifest != null) {
								Attributes attributes =
									manifest.getMainAttributes();

								attributes.putValue(
									"Bundle-SymbolicName", artifactId);
								attributes.putValue("Bundle-Version", version);

								jarOutputStream =
									new JarOutputStream(
										new FileOutputStream(tempFile),
										manifest);
							}
							else {
								jarOutputStream = new JarOutputStream(
									new FileOutputStream(tempFile));
							}

							while ((entry = jarInputStream.getNextJarEntry()) !=
										null) {

								String filename = entry.getName();

								filename = filename.toLowerCase();

								if (filename.endsWith("manifest.mf")) {
									continue;
								}

								ByteArrayOutputStream entryContent =
									new ByteArrayOutputStream();

								byte[] buffer = new byte[4096];

								int bytesRead;

								while ((bytesRead = jarInputStream.read(
											buffer)) != -1) {

									entryContent.write(buffer, 0, bytesRead);
								}

								byte[] newContent;

								if (filename.endsWith("pom.xml")) {
									String xml = new String(
										entryContent.toByteArray(),
										StandardCharsets.UTF_8);

									Pattern artifactIdPattern = Pattern.compile(
										"(?m)^([ \t]*)<artifactId>(.*?)</artifactId>");
									Pattern groupIdPattern = Pattern.compile(
										"(?m)^([ \t]*)<groupId>(.*?)</groupId>");
									Pattern versionPattern = Pattern.compile(
										"(?m)^([ \t]*)<version>(.*?)</version>");

									Matcher artifactIdMatcher =
										artifactIdPattern.matcher(xml);

									if (!artifactIdMatcher.find()) {
										throw new IllegalStateException(
											"No <artifactId> found in pom.xml");
									}

									// Replace artifactId

									String indent = artifactIdMatcher.group(1);

									String artifactIdTag =
										indent + "<artifactId>" +
											newArtifactId + "</artifactId>";

									xml = artifactIdMatcher.replaceFirst(
										Matcher.quoteReplacement(
											artifactIdTag));

									// Insert or update groupId

									Matcher groupIdMatcher =
										groupIdPattern.matcher(xml);

									if (groupIdMatcher.find()) {
										xml = groupIdMatcher.replaceFirst(
											indent + "<groupId>" + newGroupId +
												"</groupId>");
									}
									else {
										//xml = insertAfterTag(xml, "artifactId", "groupId", newGroupId, indent);
										int insertPos = artifactIdMatcher.end();
										String line =
											"\n" + indent + "<" + newTag + ">" +
												newValue + "</" + newTag + ">";

										return xml.substring(0, insertPos) +
											line + xml.substring(insertPos);
									}

									// Insert or update version

									Matcher versionMatcher =
										versionPattern.matcher(xml);

									if (versionMatcher.find()) {
										xml = versionMatcher.replaceFirst(
											indent + "<version>" + newVersion +
												"</version>");
									}
									else {
										xml = insertAfterTag(
											xml, "groupId", "version",
											newVersion, indent);
									}
								}
								else if (name.endsWith("pom.properties")) {
									Properties props = new Properties();

									props.load(
										new ByteArrayInputStream(
											entryContent.toByteArray()));

									props.setProperty("groupId", groupId);
									props.setProperty("artifactId", artifactId);
									props.setProperty("version", version);

									ByteArrayOutputStream propsOut =
										new ByteArrayOutputStream();

									props.store(propsOut, null);

									newContent = propsOut.toByteArray();
								}
								else {
									newContent = entryContent.toByteArray(); // Leave other entries unchanged
								}

								jarOutputStream.putNextEntry(
									new JarEntry(entry.getName()));

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
						catch (Exception e) {
							throw new RuntimeException(
								"Failed to post-process JAR", e);
						}
					});
			});
	}

	private String insertAfterTag(
		String xml, String afterTag, String newTag, String newValue,
		String indent) {

		Pattern pattern = Pattern.compile(
			"(?m)^" + Pattern.quote(indent) + "<" + afterTag + ">.*?</" +
				afterTag + ">");

		Matcher matcher = pattern.matcher(xml);

		if (matcher.find()) {
			int insertPos = matcher.end();
			String insert =
				"\n" + indent + "<" + newTag + ">" + newValue + "</" + newTag +
					">";

			return xml.substring(0, insertPos) + insert +
				xml.substring(insertPos);
		}

		throw new IllegalStateException(
			"Could not find <" + afterTag + "> to insert after.");
	}

	private void setElement(
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

}