package at.fhooe.hackenberg.maven;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "fix-automatic-modules", defaultPhase = LifecyclePhase.PACKAGE)
public class FixAutomaticModulesMojo extends AbstractMojo {
	
	@Parameter(required = true)
	private File modulePath;
	
	@Parameter(required = true)
	private String multiRelease;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		String cwd = System.getProperty("user.dir");
		
		String javaHome = System.getProperty("java.home");
		
		String jdeps = javaHome + File.separator + "bin" + File.separator + "jdeps.exe";
		String javac = javaHome + File.separator + "bin" + File.separator + "javac.exe";
		
		if (!modulePath.exists()) {
			throw new MojoExecutionException("Modules folder does not exist!");
		}
		if (!modulePath.isDirectory()) {
			throw new MojoExecutionException("Modules folder is not a directory!");
		}
		
		File[] jars = modulePath.listFiles(file -> !file.isDirectory() && file.getName().endsWith(".jar"));
		
		for (File jar : jars) {
			try {
				// Is modular?
				
				System.out.println("[" + jar.getName() + "] Checking module info");
				
				try (ZipFile zip = new ZipFile(jar)) {
					if (zip.getEntry("module-info.class") != null) {
						continue;
					}
					if (zip.getEntry("META-INF/versions/") != null) {
						continue;
					}
				}
				
				// Define folders
				
				File temp = new File(jar.getParentFile(), jar.getName().substring(0, jar.getName().length() - 4));
				
				File sources = new File(temp, "sources");
				File classes = new File(temp, "classes");
				
				// Generate module info
				
				System.out.println("[" + jar.getName() + "] Generating module info");
				
				{	
					ProcessBuilder builder = new ProcessBuilder(jdeps, "--ignore-missing-deps", "--multi-release", multiRelease, "--module-path", modulePath.getAbsolutePath(), "--generate-module-info", sources.getAbsolutePath(), jar.getAbsolutePath());
					builder.directory(new File(cwd));
					builder.redirectInput(Redirect.INHERIT);
					builder.redirectOutput(Redirect.DISCARD);
					builder.redirectError(Redirect.INHERIT);
					
					Process process = builder.start();
					
					if (process.waitFor() != 0) {
						throw new Exception("[" + jar.getName() + "] Jdeps did not terminate successfully!");
					}
				}
				
				// Unzip jar
				
				System.out.println("[" + jar.getName() + "] Unzipping");

				try (ZipFile zip = new ZipFile(jar)) {
					Enumeration<? extends ZipEntry> entries = zip.entries();
					
					while (entries.hasMoreElements()) {
						ZipEntry entry = entries.nextElement();
						
						File destination = new File(classes, entry.getName());
						
						destination.getParentFile().mkdirs();
						
						if (entry.isDirectory()) {
							destination.mkdir();
						} else {
							BufferedInputStream input = new BufferedInputStream(zip.getInputStream(entry));
							BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(destination));
							
							output.write(input.readAllBytes());
							
							output.close();
							input.close();
						}
					}
				}
				
				// Compile and package module info
				
				for (File module : sources.listFiles()) {
					
					File versions = new File(module, "versions");
					
					for (File version : versions.listFiles()) {
						
						// Compile module info
						
						File moduleInfoSource = new File(version, "module-info.java");
						
						System.out.println("[" + jar.getName() + "] Compiling " + moduleInfoSource.getName());
						
						ProcessBuilder builder = new ProcessBuilder(javac, "--module-path", modulePath.getAbsolutePath(), "-d", classes.getAbsolutePath(), moduleInfoSource.getAbsolutePath());
						builder.directory(new File(cwd));
						builder.redirectInput(Redirect.INHERIT);
						builder.redirectOutput(Redirect.DISCARD);
						builder.redirectError(Redirect.INHERIT);
						
						Process process = builder.start();
						
						if (process.waitFor() != 0) {
							throw new Exception("[" + jar.getName() + "] Javac did not terminate successfully!");
						}
						
						// Package module info
						
						File moduleInfoClass = new File(classes, "module-info.class");
						
						System.out.println("[" + jar.getName() + "] Packaging " + moduleInfoClass.getName());
						
						FileSystem fileSystem = FileSystems.newFileSystem(jar.toPath(), new HashMap<>());
						
						Path path = fileSystem.getPath("module-info.class");

						BufferedInputStream input = new BufferedInputStream(new FileInputStream(moduleInfoClass));
						BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(path, StandardOpenOption.CREATE));
						
						output.write(input.readAllBytes());
						
						output.close();
						input.close();
						
						fileSystem.close();
						
					}
				}
			} catch (Exception e) {
				System.err.println(e.getLocalizedMessage());
			}
		}
	}

}
