package at.fhooe.hackenberg.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

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
	private boolean winChooseDir;
	
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
			
			System.out.println("Packaging application");
		
			List<String> command = new ArrayList<>();
			
			command.add(tool("jpackage"));
			
			command.add("--name");
			command.add(name);
			command.add("--vendor");
			command.add(vendor);
			command.add("--app-version");
			command.add(appVersion);
			command.add("--copyright");
			command.add(copyright);
			command.add("--description");
			command.add(description);
			command.add("--runtime-image");
			command.add(runtimeImage.getAbsolutePath());
			command.add("--module");
			command.add(module + "/" + mainClass);
			command.add("--dest");
			command.add(dest.getAbsolutePath());
			
			if (icon != null) {
				command.add("--icon");
				command.add(icon.getAbsolutePath());
			}
			if (licenseFile != null) {
				command.add("--license-file");
				command.add(licenseFile.getAbsolutePath());
			}
			if (fileAssociations != null) {
				command.add("--file-associations");
				command.add(fileAssociations.getAbsolutePath());
			}
			if (javaOptions != null) {
				command.add("--java-options");
				command.add(javaOptions);
			}
			if (winUpgradeUuid != null) {
				command.add("--win-upgrade-uuid");
				command.add(winUpgradeUuid);
			}
			if (winPerUserInstall) {
				command.add("--win-per-user-install");
			}
			if (winChooseDir) {
				command.add("--win-choose-dir");
			}
			if (winMenu) {
				command.add("--win-menu");
			}
			if (winMenuGroup != null) {
				command.add("--win-menu-group");
				command.add(winMenuGroup);
			}
			if (winShortcut) {
				command.add("--win-shortcut");
			}
			
			exec(command, "Jpackage did not terminate successfully!");
			
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
		}
	}

}
