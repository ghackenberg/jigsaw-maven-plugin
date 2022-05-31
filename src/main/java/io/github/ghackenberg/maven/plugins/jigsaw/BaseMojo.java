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
import java.util.spi.ToolProvider;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Base mojo.
 * 
 * Captures parameters and functions, which are common to all mojos.
 * 
 * @author Georg Hackenberg
 */
public abstract class BaseMojo extends AbstractMojo {

    protected static final ToolProvider JAVAC = ToolProvider.findFirst("javac").get();
    protected static final ToolProvider JAR = ToolProvider.findFirst("jar").get();
    protected static final ToolProvider JDEPS = ToolProvider.findFirst("jdeps").get();
	protected static final ToolProvider JLINK = ToolProvider.findFirst("jlink").get();
	protected static final ToolProvider JPACKAGE = ToolProvider.findFirst("jpackage").get();
	
	/**
	 * Module path.
	 */
	@Parameter(defaultValue = "${project.build.directory}/modules")
	protected File modulePath;

	@Override
	public final void execute() throws MojoExecutionException, MojoFailureException {
		if (!modulePath.exists()) {
			throw new MojoExecutionException("Modules folder does not exist!");
		}
		if (!modulePath.isDirectory()) {
			throw new MojoExecutionException("Modules folder is not a directory!");
		}
		run();
	}
	
	/**
	 * Run the mojo.
	 *  
	 * @throws MojoExecutionException Execution exception.
	 * @throws MojoFailureException Failure exception.
	 */
	protected abstract void run() throws MojoExecutionException, MojoFailureException;

}
