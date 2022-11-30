# jigsaw-maven-plugin

Linking and packaging Java applications with old-style **unnamed modules** is cumbersome.
The **jigsaw-maven-plugin** tries to simplify this task with three build goals: **patch**, **link**, and **package** (see below).

## Documents

Read the folowing documents to learn more about the project:

* [License](./LICENSE.md)
* [Changelog](./CHANGELOG.md)
* [Contributing](./CONTRIBUTING.md)

Also, do not hesitate to contact the project owner 😉 (ghackenberg@gmail.com).

## Preparations

The **jigsaw-maven-plugin** requires two preparational steps:

1. Output archive
2. Copy dependencies

In the following, each step is described in more detail.

### Step 1: Output archive

Output archive to **modules** folder:

```xml
<plugin>
    <artifactId>maven-jar-plugin</artifactId>
    <version>3.3.0</version>
    <configuration>
        <outputDirectory>${project.build.directory}/modules</outputDirectory>
        <!-- output to ${project.build.directory}/modules -->
    </configuration>
</plugin>
```

### Step 2: Copy dependencies

Copy dependencies to **modules** folder:

```xml
<plugin>
    <artifactId>maven-dependency-plugin</artifactId>
    <version>3.4.0</version>
    <executions>
        <execution>
            <phase>package</phase>
            <!-- execute during package phase -->
            <goals>
                <goal>copy-dependencies</goal>
                <!-- execute copy-dependencies goal -->
            </goals>
            <configuration>
                <outputDirectory>${project.build.directory}/modules</outputDirectory>
                <!-- output to ${project.build.directory}/modules -->
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Goals

The **jigsaw-maven-plugin** provides three build goals:

* `patch`
* `link`
* `package`

In the following, each goal is described in more detail.

### Goal `patch` (using [jdeps](https://docs.oracle.com/en/java/javase/16/docs/specs/man/jdeps.html) and [javac](https://docs.oracle.com/en/java/javase/16/docs/specs/man/javac.html))

Convert **unnamed modules** (i.e. Java archives missing a **module-info.class** file) to named modules. This step is necessary for **jlink** and **jpackage** to work properly (see following sections). Unfortunatelly, both tools cannot process unnamed modules, which are common in many Java projects until today.

#### Configuration details

The **patch mojo** can be configured as follows:

```xml
<plugin>
    <groupId>io.github.ghackenberg</groupId>
    <artifactId>jigsaw-maven-plugin</artifactId>
    <version>1.1.3</version>
    <executions>
        <execution>
            <id>jigsaw-package</id>
            <phase>package</phase>
            <!-- execute during package phase -->
            <goals>
                <goal>patch</goal>
                <!-- execute patch goal -->
            </goals>
            <configuration>
                <modulePath>${project.build.directory}/modules</modulePath>
                <!-- type = string, default = ${project.build.directory}/modules -->
                <multiRelease>${maven.compiler.target}</multiRelease>
                <!-- type = string -->
                <ignoreMissingDeps>true</ignoreMissingDeps>
                <!-- type = boolean -->
            </configuration>
        </execution>
    </executions>
</plugin>
```

#### Implementation details

The **patch mojo** searched for unnamed modules (i.e. Java archives missing a **module-info.class** file) in the modules path. For each unnamed module the mojo generates a **module-info.java** file using the **jdeps** tool. Then, the mojo compiles the module descriptors and adds the to the original Java archives. Consequently, unnamed modules are turned into named modules.

### Goal `link` (using [jlink](https://docs.oracle.com/en/java/javase/16/docs/specs/man/jlink.html))

Link **named modules** to executable images. Executable images only include the necessary Java modules. Consequently, smaller executable bundles can be achieved. The executable images can be packaged later using **jpackage** (see next section).

#### Configuration details

The **link mojo** can be configured as follows:

```xml
<plugin>
    <groupId>io.github.ghackenberg</groupId>
    <artifactId>jigsaw-maven-plugin</artifactId>
    <version>1.1.3</version>
    <executions>
        <execution>
            <id>jigsaw-link</id>
            <phase>package</phase>
            <!-- execute during package phase -->
            <goals>
                <goal>link</goal>
                <!-- execute link goal -->
            </goals>
            <configuration>
                <modulePath>${project.build.directory}/modules</modulePath>
                <!-- type = string, default = ${project.build.directory}/modules -->
                <module>${project.artifactId}</module>
                <!-- type = string, default = ${project.artifactId} -->
                <ignoreSigningInfo>true</ignoreSigningInfo>
                <!-- type = boolean -->
                <output>${project.build.directory}/image</output>
                <!-- type = file, default = ${project.build.directory}/image -->
            </configuration>
        </execution>
    </executions>
</plugin>
```

#### Implementation details

The **link mojo** uses the **jlink** tool internally. The mojo simply wraps the command line call **jlink**.

### Goal `package` (using [jpackage](https://docs.oracle.com/en/java/javase/16/docs/specs/man/jpackage.html))

Package **executable image** to OS-specific installers. OS-specific installers can be used to install the Java application on any target machine running a compatible OS installation. Note that the installers also update previously installed versions of the same Java application. The installers can be built from the executable images generated with **jlink** (see previous section).

#### Configuration details

The **package mojo** can be configured as follows:

```xml
<plugin>
    <groupId>io.github.ghackenberg</groupId>
    <artifactId>jigsaw-maven-plugin</artifactId>
    <version>1.1.3</version>
    <executions>
        <execution>
            <id>jigsaw-package</id>
            <phase>package</phase>
            <!-- execute during package phase -->
            <goals>
                <goal>package</goal>
                <!-- execute package goal -->
            </goals>
            <configuration>
                <runtimeImage>${project.build.directory}/image</runtimeImage>
                <!-- type = file, default = ${project.build.directory}/image -->
                <modulePath>${project.build.directory}/modules</modulePath>
                <!-- type = string, default = ${project.build.directory}/modules -->
                <module>${project.artifactId}</module>
                <!-- type = string, default = ${project.artifactId} -->
                <mainClass>path.to.Main</mainClass>
                <!-- type = string, required = true -->
                <ignoreSigningInfo>true</ignoreSigningInfo>
                <!-- type = boolean -->
                <javaOptions>-Xmx64m</javaOptions>
                <!-- type = string -->
                <name>${project.name}</name>
                <!-- type = string, default = ${project.name} -->
                <appVersion>${project.version}</appVersion>
                <!-- type = string, default = ${project.version} -->
                <description>${project.description}</description>
                <!-- type = file -->
                <vendor>${project.organization.name}</vendor>
                <!-- type = string, default = ${project.organization.name} -->
                <copyright>${project.organization.name}</copyright>
                <!-- type = string, default = ${project.organization.name} -->
                <licenseFile>path/to/license.rtf</licenseFile>
                <!-- type = file -->
                <icon>path/to/icon.ico</icon>
                <!-- type = string -->
                <fileAssociations>path/to/associations.properties</fileAssociations>
                <!-- type = file -->
                <winUpgradeUuid>...</winUpgradeUuid>
                <!-- type = string -->
                <winPerUserInstall>true</winPerUserInstall>
                <!-- type = boolean -->
                <winDirChooser>true</winDirChooser>
                <!-- type = boolean -->
                <winShortcut>true</winShortcut>
                <!-- type = boolean -->
                <winMenu>true</winMenu>
                <!-- type = boolean -->
                <winMenuGroup>${project.organization.name}</winMenuGroup>
                <!-- type = string -->
                <dest>${project.build.directory}</dest>
                <!-- type = file, default = ${project.build.directory} -->
            </configuration>
        </execution>
    </executions>
</plugin>
```

#### Implementation details

The **package mojo** uses the **jpackage** tool internally. The mojo simply wraps the command line call **jpackage**.
