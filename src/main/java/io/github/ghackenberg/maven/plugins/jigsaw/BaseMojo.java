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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.List;
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

	/**
	 * Module path.
	 */
	@Parameter(defaultValue = "${project.build.directory}/modules")
	protected File modulePath;

	@Override
	public final void execute() throws MojoExecutionException, MojoFailureException {
		
		// Check requirements
		
		if (!modulePath.exists()) {
			throw new MojoExecutionException("Modules folder does not exist!");
		}
		if (!modulePath.isDirectory()) {
			throw new MojoExecutionException("Modules folder is not a directory!");
		}
		
		// Run task
		
		run();
		
	}
	
	protected final String getModulePath() {
		return new File(System.getProperty("java.home"), "jmods").getAbsolutePath() + ";" + getRelativePath(modulePath);
	}
	
	protected final String getRelativePath(File file) {
		return Paths.get(System.getProperty("user.dir")).relativize(Paths.get(file.getAbsolutePath())).toString();
	}
	
	protected final void call(String prefix, String toolName, List<String> toolParams) throws MojoExecutionException, InterruptedException {
		
		getLog().info(prefix + toolName + " " + String.join(" ", toolParams));
		
		int code;
		
		BufferedReader out;
		BufferedReader err;
		
		ToolProvider toolProvider = ToolProvider.findFirst(toolName).orElse(null);
		
		if (toolProvider != null) {
			
			StringWriter outString = new StringWriter();
			StringWriter errString = new StringWriter();
			
			code = toolProvider.run(new PrintWriter(outString), new PrintWriter(errString), toolParams.toArray(new String[] {}));
			
			out = new BufferedReader(new StringReader(outString.toString()));
			err = new BufferedReader(new StringReader(errString.toString()));
			
		} else {
			
			try {
				
				// JDK 14 and JDK 15 workaround for JPACKAGE

				toolParams.add(0, new File(new File(System.getProperty("java.home"), "bin"), toolName).getAbsolutePath());
				
				ProcessBuilder builder = new ProcessBuilder(toolParams);
				
				builder.directory(new File(System.getProperty("user.dir")));
				
				Process process = builder.start();
				
				code = process.waitFor();
				
				out = new BufferedReader(new InputStreamReader(process.getInputStream()));
				err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				
				toolParams.remove(0);
				
			} catch (IOException e) {
				throw new MojoExecutionException(e.getLocalizedMessage());
			}
			
		}
		
		String line;
		
		try {
			while ((line = out.readLine()) != null) {
				getLog().debug("  " + prefix + line);
			}
		} catch (IOException e) {
			getLog().error(e.getLocalizedMessage());
		}
		
		try {
			while ((line = err.readLine()) != null) {
				getLog().debug("  " + prefix + line);
			}	
		} catch (IOException e) {
			getLog().error(e.getLocalizedMessage());
		}
		
		if (code != 0) {
			throw new MojoExecutionException(toolName + " failed!");
		}
		
	}

	/**
	 * Run the mojo.
	 * 
	 * @throws MojoExecutionException Execution exception.
	 * @throws MojoFailureException   Failure exception.
	 */
	protected abstract void run() throws MojoExecutionException, MojoFailureException;

}
