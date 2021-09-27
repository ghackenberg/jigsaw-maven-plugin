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
import java.lang.ProcessBuilder.Redirect;
import java.util.List;

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
	
	/**
	 * Module path.
	 */
	@Parameter(defaultValue = "${project.build.directory}/modules")
	protected File modulePath;
	
	private File cwd;
	
	private File javaHome;

	@Override
	public final void execute() throws MojoExecutionException, MojoFailureException {
		if (!modulePath.exists()) {
			throw new MojoExecutionException("Modules folder does not exist!");
		}
		if (!modulePath.isDirectory()) {
			throw new MojoExecutionException("Modules folder is not a directory!");
		}
		
		cwd = new File(System.getProperty("user.dir"));
		
		javaHome = new File(System.getProperty("java.home"));
		
		run();
	}
	
	/**
	 * Get path to JDK tool. 
	 * 
	 * @param name Name of the JDK tool (e.g. javac).
	 * 
	 * @return Path to the JDK tool.
	 */
	protected String tool(String name) {
		return new File(new File(javaHome, "bin"), name).getAbsolutePath();
	}
	
	/**
	 * Execute a command and check for errors.
	 * 
	 * @param command The command to execute.
	 * @param errorMessage The error message to throw upon failure.
	 * 
	 * @throws Exception Thrown if command execution fails. 
	 */
	protected void exec(List<String> command, String errorMessage) throws Exception {
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(cwd);
		builder.redirectInput(Redirect.INHERIT);
		builder.redirectOutput(Redirect.DISCARD);
		builder.redirectError(Redirect.INHERIT);
		
		Process process = builder.start();
		
		if (process.waitFor() != 0) {
			throw new Exception(errorMessage);
		}
	}
	
	/**
	 * Run the mojo.
	 *  
	 * @throws MojoExecutionException Execution exception.
	 * @throws MojoFailureException Failure exception.
	 */
	protected abstract void run() throws MojoExecutionException, MojoFailureException;

}
