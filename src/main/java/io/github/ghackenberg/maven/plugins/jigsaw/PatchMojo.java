/**
 * Copyright © 2021 Georg Hackenberg
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.ghackenberg.maven.plugins.jigsaw;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.Runtime.Version;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Patch mojo.
 * 
 * Creates named modules from unnamed modules.
 *
 * @author Georg Hackenberg
 */
@Mojo(name = "patch", defaultPhase = LifecyclePhase.PACKAGE)
public class PatchMojo extends BaseMojo {

	private static final int JAVA_VERSION_MAJOR = Runtime.version().major();

	@Parameter
	private boolean ignoreMissingDeps;

	@Parameter
	private String multiRelease;

	@Override
	protected final void run() throws MojoExecutionException, MojoFailureException {

		getLog().info("Patching modules");

		int multiReleaseInternal = multiRelease != null ? Integer.parseInt(multiRelease) : JAVA_VERSION_MAJOR;

		File[] jars = modulePath.listFiles(file -> !file.isDirectory() && file.getName().endsWith(".jar"));

		for (File jar : jars) {

			try {

				// Is modular?

				getLog().debug("[" + jar.getName() + "] Checking module info");

				try (JarFile zip = new JarFile(jar, false, 1, Version.parse(String.valueOf(multiReleaseInternal)))) {
					if (zip.stream().anyMatch(file -> file.getName().endsWith("module-info.class"))) {
						continue;
					}
				}

				// Define folders

				File jarName = new File(modulePath, jar.getName().substring(0, jar.getName().length() - ".jar".length()));

				File additionalSources = new File(jarName, "sources");
				File additionalClasses = new File(jarName, "classes");

				// Generate module info

				getLog().debug("[" + jar.getName() + "] Generating module info");

				// Define params (JDEPS)

				List<String> jdepsParams = new ArrayList<>();

				if (ignoreMissingDeps) {
					jdepsParams.add("--ignore-missing-deps");
				}

				jdepsParams.add("--multi-release");
				jdepsParams.add(String.valueOf(multiReleaseInternal));

				jdepsParams.add("--module-path");
				jdepsParams.add(modulePath.getAbsolutePath());

				jdepsParams.add("--generate-module-info");
				jdepsParams.add(additionalSources.getAbsolutePath());

				jdepsParams.add(jar.getAbsolutePath());

				// Run tool (JDEPS)

				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				PrintStream printStream = new PrintStream(outputStream);
				JDEPS.run(printStream, System.err, jdepsParams.toArray(new String[] {}));

				// Compile and package module info

				for (File module : additionalSources.listFiles()) {

					File versions = new File(module, "versions");

					for (File version : versions.listFiles()) {

						// Compile module info

						File moduleInfoSource = new File(version, "module-info.java");

						getLog().debug("[" + jar.getName() + "] Compiling " + moduleInfoSource.getName());

						// Define params (JAVAC)

						List<String> javacParams = new ArrayList<>();

						javacParams.add("--module-path");
						javacParams.add(modulePath.getAbsolutePath());

						javacParams.add("--patch-module");
						javacParams.add(module.getName() + "=" + jar.getAbsolutePath());

						javacParams.add("-Xlint:-module");
						javacParams.add("-Xlint:-exports");
						javacParams.add("-Xlint:-requires-transitive-automatic");
						javacParams.add("-Xlint:-requires-automatic");

						javacParams.add("-d");
						javacParams.add(additionalClasses.getAbsolutePath());

						javacParams.add(moduleInfoSource.getAbsolutePath());

						// Run tool (JAVAC)

						JAVAC.run(System.out, System.err, javacParams.toArray(new String[] {}));

						// Package module info

						File moduleInfoClass = new File(additionalClasses, "module-info.class");

						getLog().info("[" + jar.getName() + "] Packaging " + moduleInfoClass.getName());

						// Define params (JAR)

						List<String> jarParams = new ArrayList<>();

						jarParams.add("uf");
						jarParams.add(jar.getAbsolutePath());

						jarParams.add("-C");
						jarParams.add(additionalClasses.getAbsolutePath());

						jarParams.add("module-info.class");

						// Run tool (JAR)

						JAR.run(System.out, System.err, jarParams.toArray(new String[] {}));

					}
				}

			} catch (Exception e) {
				getLog().error(e.getLocalizedMessage(), e);
			}

		}

	}

}
