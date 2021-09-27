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
 * Link mojo.
 * 
 * @author Georg Hackenberg
 */
@Mojo(name = "link", defaultPhase = LifecyclePhase.PACKAGE)
public class LinkMojo extends BaseMojo {
	
	@Parameter(defaultValue = "${project.artifactId}")
	private String module;

	@Parameter(defaultValue = "${project.build.directory}/image")
	private File output;
	
	@Parameter
	private boolean ignoreSigningInformation;

	@Override
	protected final void run() throws MojoExecutionException, MojoFailureException {
		
		try {
			
			// Link modules
			
			System.out.println("Linking modules");
		
			List<String> command = new ArrayList<>();
			
			command.add(tool("jlink"));
			
			command.add("--module-path");
			command.add(modulePath.getAbsolutePath());
			command.add("--add-modules");
			command.add(module);
			command.add("--output");
			command.add(output.getAbsolutePath());
			
			if (ignoreSigningInformation) {
				command.add("--ignore-signing-information");
			}
			
			exec(command, "Jlink did not terminate successfully!");
			
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
		}
		
	}

}
