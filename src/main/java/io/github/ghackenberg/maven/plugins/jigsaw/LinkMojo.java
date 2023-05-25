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
import java.util.stream.Stream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Link mojo.
 * 
 * Creates executable runtimes from named modules.
 * 
 * @author Georg Hackenberg
 */
@Mojo(name = "link", defaultPhase = LifecyclePhase.PACKAGE)
public class LinkMojo extends BaseMojo {

	@Parameter(defaultValue = "${project.artifactId}")
	private String module;

	@Parameter(defaultValue = "${project.build.directory}/image")
	private File output;

	@Parameter(defaultValue = "")
	private String jlinkOptions;

	@Parameter
	private String launcher;

	@Parameter
	private boolean ignoreSigningInformation;

	@Override
	protected final void run() throws MojoExecutionException, MojoFailureException {

		try {

			// Link modules

			getLog().info("Linking modules");

			// Define params (JLINK)

			List<String> params = new ArrayList<>();

			params.add("--module-path");
			params.add(getModulePath());

			params.add("--add-modules");
			params.add(module);

			params.add("--output");
			params.add(getRelativePath(output));

			if (launcher != null) {
				params.add("--launcher=" + launcher);
			}

			if (ignoreSigningInformation) {
				params.add("--ignore-signing-information");
			}

			if (jlinkOptions != null) {
				Stream.of(jlinkOptions.split(",")).forEach(params::add);
			}

			// Run tool (JLINK)

			call("> ", "jlink", params);

		} catch (Exception e) {
			throw new MojoExecutionException(e);
		}

	}

}
