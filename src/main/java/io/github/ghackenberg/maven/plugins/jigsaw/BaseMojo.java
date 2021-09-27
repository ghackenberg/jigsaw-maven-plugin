package io.github.ghackenberg.maven.plugins.jigsaw;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Common mojo base.
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
