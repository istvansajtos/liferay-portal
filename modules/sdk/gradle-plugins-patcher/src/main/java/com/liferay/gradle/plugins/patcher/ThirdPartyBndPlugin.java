package com.liferay.gradle.plugins.patcher;

import com.liferay.gradle.util.GradleUtil;

import org.gradle.api.java.archives.Manifest;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.Jar;

import java.util.HashMap;
import java.util.Map;

public class ThirdPartyBndPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		System.out.println(">>> ThirdPartyBndPlugin.apply");

		project.afterEvaluate(p -> {
			// Apply only in modules under "modules/third-party"
			if (!isThirdPartyModule(project)) {
				return;
			}

			// Find and configure the Jar task
			Jar jarTask = (Jar) GradleUtil.getTask(project, "jar");

/*			BundleTaskExtension bundleExt = GradleUtil.getExtension(jarTask, BundleTaskExtension.class);

			if (bundleExt != null) {
				// Use `bnd` map instead of `instructions`
				Map<String, Object> bndMap = new HashMap<>();

				bndMap.put("-removeheaders", "Implementation-*, Specification-*, Built-By, Created-By");
				bndMap.put("Implementation-Title", project.getName() + "-patched");
				bndMap.put("Implementation-Version", "1.0.0-patched");
				bndMap.put("Implementation-Vendor", "Your Company");
				bndMap.put("Bundle-SymbolicName", project.getName());
				bndMap.put("Bundle-Version", "1.0.0.patched");

				bundleExt.setBnd(bndMap);
			}
		*/
/*
			if (jarTask != null) {
				jarTask.doFirst(task -> {
					Manifest manifest = jarTask.getManifest();

					Attributes attributes = manifest.getAttributes("Main");

					if (attributes == null) {
						attributes = new Attributes();
						manifest.getAttributes().putAll(attributes);
					}

					// Replace existing or set new attributes
					attributes.putValue("Bundle-SymbolicName", project.getName());
					attributes.putValue("Bundle-Version", project.getVersion().toString());
					attributes.putValue("Implementation-Title", project.getName());
					attributes.putValue("Implementation-Version", project.getVersion().toString());
					attributes.putValue("Implementation-Vendor", "Your Company");

					// Remove original 3rd-party artifact details (if known)
					attributes.remove(new Attributes.Name("Implementation-Title"));
					attributes.remove(new Attributes.Name("Implementation-Version"));
				});
			}*/
		});
	}

	private boolean isThirdPartyModule(Project project) {
		return project.getProjectDir().getAbsolutePath().replace("\\", "/").contains("/modules/third-party/");
	}
}