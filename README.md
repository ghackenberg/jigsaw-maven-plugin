# maven-jigsaw-plugin

## Documents

* [License](./LICENSE.md)
* [Changelog](./CHANGELOG.md)
* [Contributing](./CONTRIBUTING.md)

## Goals

### patch

Convert automatic modules to standard modules:

```xml
<plugin>
    <groupId>at.fhooe.hackenberg</groupId>
    <artifactId>maven-jpms-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <phase>package</phase>
        <goals>
            <goal>patch</goal>
        </goals>
        <configuration>
            <multiRelease>${maven.compiler.target}</multiRelease>
            <ignoreMissingDeps>true</ignoreMissingDeps>
        </configuration>
    </executions>
</plugin>
```

### link

Link modules to executable image

```xml
<plugin>
    <groupId>at.fhooe.hackenberg</groupId>
    <artifactId>maven-jpms-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <phase>package</phase>
        <goals>
            <goal>link</goal>
        </goals>
        <configuration>
            <ignoreSigningInfo>true</ignoreSigningInfo>
        </configuration>
    </executions>
</plugin>
```

### package

Package image to executable installer

```xml
<plugin>
    <groupId>at.fhooe.hackenberg</groupId>
    <artifactId>maven-jpms-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <phase>package</phase>
        <goals>
            <goal>package</goal>
        </goals>
        <configuration>
            TODO
        </configuration>
    </executions>
</plugin>
```
