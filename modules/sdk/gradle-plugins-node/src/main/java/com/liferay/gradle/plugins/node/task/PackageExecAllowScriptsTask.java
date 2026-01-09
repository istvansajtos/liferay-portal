package com.liferay.gradle.plugins.node.task;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.Internal;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author Istvan Sajtos
 */
public class PackageExecAllowScriptsTask extends ExecutePackageManagerTask {

/*	@Override
	public synchronized void executeNode() throws Exception {
		File npmrcFile = _getNpmrcFile();

		File packageJsonFile = new File(getWorkingDir(), "package.json");

		Path packageJsonPath = packageJsonFile.toPath();
	}
*/

	@Override
	public void executeNode() throws Exception {
		if (!isUseNpm()) {
			environment("npm_config_legacy_peer_deps", "true");
		}

		super.executeNode();
	}

	@Internal
	@Override
	protected List<String> getCompleteArgs() {
		List<String> completeArgs = super.getCompleteArgs();

		if (isUseNpm()) {
			completeArgs.add("exec");
		}

		completeArgs.add("allow-scripts");
		completeArgs.add("setup");

		return completeArgs;
	}
/*	private void _createNpmrcFile(File npmrcFile) throws IOException {
		List<String> npmrcContents = new ArrayList<>();

		npmrcContents.add(
			"//registry.npmjs.org/:_authToken=" + getNpmAccessToken());

		FileUtil.write(npmrcFile, npmrcContents);
	}
*/
	private File _getNpmrcFile() {
		if (isUseNpm()) {
			_logger.quiet("isUseNpm(): true");

			return new File(getTemporaryDir(), "npmrc");
		}

		Project curProject = getProject();

		do {
			File file = curProject.file("yarn.lock");

			if (file.exists()) {
				return curProject.file(".npmrc");
			}
		}
		while ((curProject = curProject.getParent()) != null);

		Project project = getProject();

		return project.file(".npmrc");
	}

	private Logger _logger  = getLogger();
}
