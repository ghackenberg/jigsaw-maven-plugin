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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.Runtime.Version;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.spi.ToolProvider;
import java.util.stream.Collectors;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Patch mojo.
 * <p>
 * Creates named modules from unnamed modules.
 *
 * @author Georg Hackenberg
 */
@Mojo(name = "patch", defaultPhase = LifecyclePhase.PACKAGE)
public class PatchMojo extends BaseMojo {

    private static final Pattern PATTERN = Pattern.compile(
        "^META-INF/versions/([0-9]*)/module-info.class$");
    private static final int JAVA_VERSION_MAJOR = Runtime.version().feature();

    @Parameter
    private boolean ignoreMissingDeps;

    @Parameter
    private String multiRelease;

    @Override
    protected final void run() throws MojoExecutionException, MojoFailureException {
        int targetJavaVersionNumber =
            multiRelease != null ? Integer.parseInt(multiRelease) : JAVA_VERSION_MAJOR;
        ;

        File[] jars = modulePath.listFiles(
            file -> !file.isDirectory() && file.getName().endsWith(".jar"));

        ToolProvider javac = ToolProvider.findFirst("javac").orElseThrow();
        ToolProvider jdeps = ToolProvider.findFirst("jdeps").orElseThrow();
        ToolProvider jarTool = ToolProvider.findFirst("jar").orElseThrow();

        for (File jar : jars) {
            try {
                // Is modular?

                getLog().debug("[" + jar.getName() + "] Checking module info");

                try (JarFile zip = new JarFile(jar, false, 1, Version.parse(multiRelease))) {
                    if (zip.versionedStream()
                        .anyMatch(file -> file.getName().equals("module-info.class"))) {
                        continue;
                    }
                }

                // Define folders

                File temp = new File(jar.getParentFile(),
                    jar.getName().substring(0, jar.getName().length() - 4));

                File sources = new File(temp, "sources");
                File classes = new File(temp, "classes");

                // Generate module info

                getLog().debug("[" + jar.getName() + "] Generating module info");

                String moduleName = null;

                {
                    List<String> command = new ArrayList<>();

                    if (ignoreMissingDeps) {
                        command.add("--ignore-missing-deps");
                    }
                    command.add("--multi-release");
                    command.add(String.valueOf(targetJavaVersionNumber));

                    command.add("--module-path");
                    command.add(modulePath.getAbsolutePath());
                    command.add("--generate-module-info");
                    command.add(sources.getAbsolutePath());
                    command.add(jar.getAbsolutePath());
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    jdeps.run(new PrintStream(byteArrayOutputStream), System.err,
                        command.toArray(String[]::new));
                    List<String> collect = new BufferedReader(
                        new InputStreamReader(
                            new ByteArrayInputStream(
                                byteArrayOutputStream.toByteArray()))).lines()
                        .dropWhile(x -> !x.startsWith("writing to"))
                        .collect(Collectors.toList());
                    String s = collect.get(collect.size() == 1 ? 0 : 1);
                    String substring = s.substring(
                        "writing to ".length() + sources.getAbsolutePath().length() + 1);
                    moduleName = substring.substring(0, substring.indexOf('/'));

                }


                // Compile and package module info

                for (File module : sources.listFiles()) {

                    File versions = new File(module, "versions");

                    for (File version : versions.listFiles()) {

                        // Compile module info

                        File moduleInfoSource = new File(version, "module-info.java");

                        getLog().debug(
                            "[" + jar.getName() + "] Compiling " + moduleInfoSource.getName());

                        List<String> command = new ArrayList<>();

                        command.add("--module-path");
                        command.add(modulePath.getAbsolutePath());

                        command.add("--patch-module");
                        command.add(moduleName + "=" + jar.getAbsolutePath());

                        command.add("-Xlint:-module");
                        command.add("-Xlint:-exports");
                        command.add("-Xlint:-requires-transitive-automatic");
                        command.add("-Xlint:-requires-automatic");

                        command.add("-d");
                        command.add(classes.getAbsolutePath());

                        command.add(moduleInfoSource.getAbsolutePath());

                        javac.run(System.out, System.err, command.toArray(String[]::new));

                        // Package module info

                        File moduleInfoClass = new File(classes, "module-info.class");

                        getLog().debug(
                            "[" + jar.getName() + "] Packaging " + moduleInfoClass.getName());

                        jarTool.run(System.out, System.err,
                            "uf", jar.getAbsolutePath(),
                            "-C", classes.getAbsolutePath(),
                            "module-info.class");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
