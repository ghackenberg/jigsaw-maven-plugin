/**
 * Copyright © 2021 Georg Hackenberg
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.ghackenberg.maven.plugins.jigsaw;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Package mojo.
 * 
 * Creates OS-specific installers from executable runtimes.
 * 
 * @author Georg Hackenberg
 */
@Mojo(name = "package", defaultPhase = LifecyclePhase.PACKAGE)
public class PackageMojo extends BaseMojo {

	@Parameter(defaultValue = "${project.build.directory}")
	private File dest;

	@Parameter(defaultValue = "${project.artifactId}")
	private String module;

	@Parameter(defaultValue = "${project.build.directory}/image")
	private File runtimeImage;

	@Parameter(required = true)
	private String mainClass;

	@Parameter
	private boolean ignoreSigningInformation;

	@Parameter
	private String javaOptions;

	@Parameter(defaultValue = "${project.name}")
	private String name;

	@Parameter
	private File icon;

	@Parameter(defaultValue = "${project.organization.name}")
	private String vendor;

	@Parameter(defaultValue = "${project.version}")
	private String appVersion;

	@Parameter(defaultValue = "${project.organization.name}")
	private String copyright;

	@Parameter(defaultValue = "${project.description}")
	private String description;

	@Parameter
	private File licenseFile;

	@Parameter
	private File fileAssociations;

	@Parameter
	private String winUpgradeUuid;

	@Parameter
	private boolean winPerUserInstall;

	@Parameter
	private boolean winDirChooser;

	@Parameter
	private boolean winMenu;

	@Parameter
	private String winMenuGroup;

	@Parameter
	private boolean winShortcut;

	@Override
	protected final void run() throws MojoExecutionException, MojoFailureException {

		try {

			// Package application

			getLog().info("Packaging application");

			// Define params (JPACKAGE)

			List<String> params = new ArrayList<>();

			params.add("--name");
			params.add(name);

			params.add("--vendor");
			params.add(vendor);

			params.add("--app-version");
			params.add(appVersion);

			params.add("--copyright");
			params.add(copyright);

			params.add("--description");
			params.add(description);

			params.add("--runtime-image");
			params.add(runtimeImage.getAbsolutePath());

			params.add("--module");
			params.add(module + "/" + mainClass);

			params.add("--dest");
			params.add(dest.getAbsolutePath());

			if (icon != null) {
				params.add("--icon");
				params.add(icon.getAbsolutePath());
			}

			if (licenseFile != null) {
				params.add("--license-file");
				params.add(licenseFile.getAbsolutePath());
			}

			if (fileAssociations != null) {
				params.add("--file-associations");
				params.add(fileAssociations.getAbsolutePath());
			}

			if (javaOptions != null) {
				params.add("--java-options");
				params.add(javaOptions);
			}

			if (winUpgradeUuid != null) {
				params.add("--win-upgrade-uuid");
				params.add(winUpgradeUuid);
			}

			if (winPerUserInstall) {
				params.add("--win-per-user-install");
			}

			if (winDirChooser) {
				params.add("--win-dir-chooser");
			}

			if (winMenu) {
				params.add("--win-menu");
			}

			if (winMenuGroup != null) {
				params.add("--win-menu-group");
				params.add(winMenuGroup);
			}

			if (winShortcut) {
				params.add("--win-shortcut");
			}

			// Run tool (JPACKAGE)

			JPACKAGE.run(System.out, System.err, params.toArray(new String[] {}));

		} catch (Exception e) {
			getLog().error(e.getLocalizedMessage(), e);
		}
	}

}
