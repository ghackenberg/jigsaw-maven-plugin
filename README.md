# jigsaw-maven-plugin

Linking and packaging Java applications with old-style **unnamed modules** is cumbersome.
The **jigsaw-maven-plugin** tries to simplify this task with three build goals: **patch**, **link**, and **package** (see below).

## Documents

Read the folowing documents to learn more about the project:

* [License](./LICENSE.md)
* [Changelog](./CHANGELOG.md)
* [Contributing](./CONTRIBUTING.md)

## Preparations

The **jigsaw-maven-plugin** requires the following preparations:

### Output archive

Output archive to **modules** folder:

```xml
<plugin>
    <artifactId>maven-jar-plugin</artifactId>
    <version>3.2.0</version>
    <configuration>
        <outputDirectory>${project.build.directory}/modules</outputDirectory>
        <!-- output to ${project.build.directory}/modules -->
    </configuration>
</plugin>
```

### Copy dependencies

Copy dependencies to **modules** folder:

```xml
<plugin>
    <artifactId>maven-dependency-plugin</artifactId>
    <version>3.2.0</version>
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

The **jigsaw-maven-plugin** provides the following build goals:

### **patch** (based on [jdeps](https://docs.oracle.com/en/java/javase/16/docs/specs/man/jdeps.html) and [javac](https://docs.oracle.com/en/java/javase/16/docs/specs/man/javac.html))

Convert **unnamed modules** to named modules:

```xml
<plugin>
    <groupId>io.github.ghackenberg</groupId>
    <artifactId>jigsaw-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
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
    </executions>
</plugin>
```



### **link** (based on [jlink](https://docs.oracle.com/en/java/javase/16/docs/specs/man/jlink.html))

Link **named modules** to executable image:

```xml
<plugin>
    <groupId>io.github.ghackenberg</groupId>
    <artifactId>jigsaw-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
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
    </executions>
</plugin>
```

### **package** (based on [jpackage](https://docs.oracle.com/en/java/javase/16/docs/specs/man/jpackage.html))

Package **executable image** to OS-specific installer:

```xml
<plugin>
    <groupId>io.github.ghackenberg</groupId>
    <artifactId>jigsaw-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
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
            <module>${project.artifactId</module>
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
    </executions>
</plugin>
```
