package io.github.ghackenberg.maven.plugins.jigsaw;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class BaseMojo extends AbstractMojo {
	
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
	
	protected String tool(String name) {
		return new File(new File(javaHome, "bin"), name).getAbsolutePath();
	}
	
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
	
	protected abstract void run() throws MojoExecutionException, MojoFailureException;

}
