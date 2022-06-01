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

import java.io.File;
import java.io.IOException;
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

	@Parameter
	private boolean ignoreMissingDeps;

	@Parameter
	private String multiRelease;

	@Override
	protected final void run() throws MojoExecutionException, MojoFailureException {

		try {
			
			// Patch modules
		
			getLog().info("Patching modules");
	
			File[] jars = modulePath.listFiles(file -> !file.isDirectory() && file.getName().endsWith(".jar"));
	
			for (File jar : jars) {
				
				getLog().info("> " + jar.getName());

				// Check module info

				try (JarFile zip = multiRelease != null ? new JarFile(jar, false, 1, Version.parse(multiRelease)) : new JarFile(jar, false, 1)) {
					if (zip.stream().anyMatch(file -> file.getName().endsWith("module-info.class"))) {
						getLog().info("  > module info already exists");
						continue;
					}
				}
				
				// Generate module info

				File jarName = new File(modulePath, jar.getName().substring(0, jar.getName().length() - ".jar".length()));

				File additionalSources = new File(jarName, "sources");
				File additionalClasses = new File(jarName, "classes");
				
				// Define params (JDEPS)

				List<String> jdepsParams = new ArrayList<>();

				if (ignoreMissingDeps) {
					jdepsParams.add("--ignore-missing-deps");
				}
				
				if (multiRelease != null) {
					jdepsParams.add("--multi-release");
					jdepsParams.add(multiRelease);
				}

				jdepsParams.add("--module-path");
				jdepsParams.add(getModulePath());

				jdepsParams.add("--generate-module-info");
				jdepsParams.add(getRelativePath(additionalSources));

				jdepsParams.add(getRelativePath(jar));

				// Run tool (JDEPS)

				call("  > ", "jdeps", jdepsParams);

				// Compile and package module info

				if (!additionalSources.exists()) {
					throw new MojoExecutionException("Generating module info for " + jar.getName() + " failed");
				}
				
				for (File module : additionalSources.listFiles()) {
					
					File moduleInfoSource = new File(module, "module-info.java");
					
					if (moduleInfoSource.exists()) {
						compileAndPackage(jar, module, moduleInfoSource, additionalClasses);
					}

					File versions = new File(module, "versions");

					if (versions.exists()) {
						
						for (File version : versions.listFiles()) {
							
							File versionModuleInfoSource = new File(version, "module-info.java");
							
							if (versionModuleInfoSource.exists()) {
								compileAndPackage(jar, module, versionModuleInfoSource, additionalClasses);	
							}
							
						}
					}
					
				}
				
			}

		} catch (MojoExecutionException e) {
			throw e;
		} catch (Exception e) {
			throw new MojoExecutionException(e);
		}

	}
	
	private void compileAndPackage(File jar, File module, File moduleInfoSource, File additionalClasses) throws IOException, MojoExecutionException, InterruptedException {

		// Compile module info

		// Define params (JAVAC)

		List<String> javacParams = new ArrayList<>();

		javacParams.add("--module-path");
		javacParams.add(getModulePath());

		javacParams.add("--patch-module");
		javacParams.add(module.getName() + "=" + getRelativePath(jar));

		javacParams.add("-Xlint:-module");
		javacParams.add("-Xlint:-exports");
		javacParams.add("-Xlint:-requires-transitive-automatic");
		javacParams.add("-Xlint:-requires-automatic");

		javacParams.add("-d");
		javacParams.add(getRelativePath(additionalClasses));

		javacParams.add(getRelativePath(moduleInfoSource));

		// Run tool (JAVAC)

		call("  > ", "javac", javacParams);

		// Package module info

		// Define params (JAR)

		List<String> jarParams = new ArrayList<>();

		jarParams.add("uf");
		jarParams.add(getRelativePath(jar));

		jarParams.add("-C");
		jarParams.add(getRelativePath(additionalClasses));

		jarParams.add("module-info.class");

		// Run tool (JAR)

		call("  > ", "jar", jarParams);
		
	}

}
