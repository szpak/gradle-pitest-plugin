# Gradle plugin for PIT Mutation Testing

The plugin provides an ability to perform a [mutation testing](https://en.wikipedia.org/wiki/Mutation_testing) and
calculate a mutation coverage of a [Gradle](https://gradle.org/)-based projects with [PIT](http://pitest.org/).

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/info.solidsoft.gradle.pitest/gradle-pitest-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/info.solidsoft.gradle.pitest/gradle-pitest-plugin)
[![Build Status Travis](https://travis-ci.org/szpak/gradle-pitest-plugin.svg?branch=master)](https://travis-ci.org/szpak/gradle-pitest-plugin)
[![Windows Build Status](https://ci.appveyor.com/api/projects/status/github/szpak/gradle-pitest-plugin?branch=master&svg=true)](https://ci.appveyor.com/project/szpak/gradle-pitest-plugin/)
[![Dependabot Status](https://api.dependabot.com/badges/status?host=github&repo=szpak/gradle-pitest-plugin)](https://dependabot.com)

## Quick start

### The simplest way

Add gradle-pitest-plugin to the `plugins` configuration in your `build.gradle` file:

```groovy
plugins {
    id 'info.solidsoft.pitest' version '1.4.7'
}
```

Call Gradle with pitest task:

    gradle pitest

After the measurements a report created by PIT will be placed in `${PROJECT_DIR}/build/reports/pitest` directory.

Optionally make it depend on build:

```groovy
build.dependsOn 'pitest'
```

Note that when making `pitest` depend on another task, it must be referred to by name. Otherwise Gradle will resolve `pitest` to the configuration and not the task.

Please take into account that only versions starting with 1.1.11 are available via the [Plugin Portal](https://plugins.gradle.org/plugin/info.solidsoft.pitest).

### Generic approach

"The `plugins` way" has some limitations. As the primary repository for the plugin is the [Central Repository](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22info.solidsoft.gradle.pitest%22%20AND%20a%3A%22gradle-pitest-plugin%22) (aka Maven Central) it is also possible to add the plugin to your project using "the generic way":

```groovy
buildscript {
    repositories {
        mavenCentral()
        //Needed only for SNAPSHOT versions
        //maven { url 'https://oss.sonatype.org/content/repositories/snapshots/' }
    }
    dependencies {
        classpath 'info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.4.7'
    }
}
```

Apply the plugin:

```groovy
apply plugin: 'info.solidsoft.pitest'
```


## Plugin configuration

The Pitest plugin has to be configured. All the [command line options](https://pitest.org/quickstart/commandline/) are
supported. To make life easier `taskClasspath`, `mutableCodePaths`, `sourceDirs`, `reportDir` and `pitestVersion` are
automatically set by a plugin. In addition `sourceDirs`, `reportDir` and `pitestVersion` can be overridden by an user.

In the past there was one mandatory parameter - `targetClasses` - which points to the classes which should be mutated.
Starting from 0.32.0 it is only required if a [group](https://www.gradle.org/docs/current/userguide/writing_build_scripts.html#N10A34)
for the project is not set. Otherwise value `"${project.group}.*"` is set by default (which can be overridden using `pitest.targetClasses` parameter).

In case of using not default PIT version the `pitestVersion` parameter should be used to override it.

The configuration in Gradle is the real Groovy code which makes all assignments very intuitive. All values expected by
PIT should be passed as a corresponding types. There is only one important difference. For the parameters where PIT expects
a coma separated list of strings in a Gradle configuration a list of strings should be used (see `outputFormats` in the
following example).

```groovy
pitest {
    targetClasses = ['our.base.package.*']  //by default "${project.group}.*"
    pitestVersion = '1.4.1' //not needed when a default PIT version should be used
    threads = 4
    outputFormats = ['XML', 'HTML']
    timestampedReports = false
}
```

Check PIT documentation for a [list](https://pitest.org/quickstart/commandline/) of all available command line parameters.
The expected parameter format in a plugin configuration can be taken from
[PitestPluginExtension](https://github.com/szpak/gradle-pitest-plugin/blob/master/src/main/groovy/info/solidsoft/gradle/pitest/PitestPluginExtension.groovy).

There are a few parameters specific for Gradle plugin:

 - `testSourceSets` - defines test source sets which should be used by PIT (by default sourceSets.test, but allows
to add integration tests located in a different source set) (since 0.30.1)
 - `mainSourceSets` - defines main source sets which should be used by PIT (by default sourceSets.main) (since 0.30.1)
 - `mainProcessJvmArgs` - JVM arguments to be used when launching the main PIT process; make a note that PIT itself launches
another Java processes for mutation testing execution and usually `jvmArgs` should be used to for example increase maximum memory size
(since 0.33.0 - see [#7](https://github.com/szpak/gradle-pitest-plugin/issues/7));
 - `additionalMutableCodePaths` - additional classes to mutate (useful for integration tests with production code in a different module - since 1.1.4 -
see [#25](https://github.com/szpak/gradle-pitest-plugin/issues/25))
 - `useClasspathFile` - enables passing additional classpath as a file content (useful for Windows users with lots of classpath elements, disabled by default - since 1.2.2)
 - `fileExtensionsToFilter` - provides ability to filter additional file extensions from PIT classpath (since 1.2.4 - see [#53](https://github.com/szpak/gradle-pitest-plugin/issues/53))

For example:

```groovy
pitest {
    ...
    testSourceSets = [sourceSets.test, sourceSets.integrationTest]
    mainSourceSets = [sourceSets.main, sourceSets.additionalMain]
    jvmArgs = ['-Xmx1024m']
    useClasspathFile = true     //useful with bigger projects on Windows
    fileExtensionsToFilter.addAll('xml', 'orbit')
}
```

### Eliminate warning in Idea

As reported in [#170](https://github.com/szpak/gradle-pitest-plugin/pull/170) Idea displays warnings about setting final fields (of [lazy configuration](https://docs.gradle.org/current/userguide/lazy_configuration.html)) in `build.gradle`. It is not a real problem as Gradle internally intercepts those calls and use a setter instead . Nevertheless, people which prefer to have no (less) warnings at the cost of less readable code can use setters instead, e.g:

```groovy
    testSourceSets.set([sourceSets.test, sourceSets.integrationTest])
    mainSourceSets.set([sourceSets.main, sourceSets.additionalMain])
    jvmArgs.set(['-Xmx1024m'])
    useClasspathFile.set(true)     //useful with bigger projects on Windows
    fileExtensionsToFilter.addAll('xml', 'orbit')
```

Similar syntax can be used also for Kotlin configuration (`build.gradle.kts`).

## Multi-module projects support

gradle-pitest-plugin can be used in multi-module projects. The gradle-pitest-plugin dependency should be added to the buildscript configuration in
the root project while the plugin has to be applied in all subprojects which should be processed with PIT. A sample snippet from build.gradle located
for the root project:

```groovy
//in root project configuration
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.4.7'
        (...)
    }
}

subprojects {
    ...
    apply plugin: 'info.solidsoft.pitest'   //'pitest' for plugin versions <1.1.0

    pitest {
        threads = 4

        if (project.name in ['module-without-any-test']) {
            failWhenNoMutations = false
        }
    }
}
```

Currently PIT [does not provide](https://code.google.com/p/pitestrunner/issues/detail?id=41) an aggregated report for
multi-module project. A report for each module has to be browsed separately. Alternatively a
[PIT plugin for Sonar](https://github.com/SonarCommunity/sonar-pitest) can be used to get aggregated results.

## Integration tests in separate subproject

Since gradle-pitest-plugin 1.1.4 it is possible to mutate code located in different subproject. Gradle internally does not rely on
output directory from other subproject, but builds JAR and uses classes from it. For PIT those are two different sets of class files, so
to make it work it is required to define both `mainSourceSets` and `additionalMutableCodePaths`. For example:

```groovy
configure(project(':itest')) {
    apply plugin: 'info.solidsoft.pitest'
    dependencies {
        compile project(':shared')
    }

    configurations { mutableCodeBase { transitive false } }
    dependencies { mutableCodeBase project(':shared') }
    pitest {
        mainSourceSets = [project.sourceSets.main, project(':shared').sourceSets.main]
        additionalMutableCodePaths = [configurations.mutableCodeBase.singleFile]
    }
}
```

The above is the way recommended by the [Gradle team](http://forums.gradle.org/gradle/topics/how-to-get-file-path-to-binary-jar-produced-by-subproject#reply_15315782),
but in specific cases the simpler solution should also work:

```groovy
configure(project(':itest')) {
    apply plugin: 'info.solidsoft.pitest'
    dependencies {
        compile project(':shared')
    }

    pitest {
        mainSourceSets = [project.sourceSets.main, project(':shared').sourceSets.main]
        additionalMutableCodePaths = project(':shared').jar.outputs.files.getFiles()
    }
}
```

Minimal working multi-project build is available in
[functional tests suite](https://github.com/szpak/gradle-pitest-plugin/tree/master/src/funcTest/resources/testProjects/multiproject).

## PIT test-plugins support

Test plugins are used to support different test frameworks than JUnit4. They are officially supported by gradle-pitest-plugin staring with version 1.1.4
(although it was possible to use it since 1.1.0).

### JUnit 5 plugin for PIT support (gradle-pitest-plugin 1.4.7+)

Starting with this release the configuration required to use PIT with JUnit 5 has been simplified to the following:

```groovy
plugins {
    id 'java'
    id 'info.solidsoft.pitest' version '1.4.7'
}

pitest {
    //adds dependency to org.pitest:pitest-junit5-plugin and sets "testPlugin" to "junit5"
    junit5PluginVersion = '0.12'
    // ...
}
```

The minimal working example for JUhnit 5 is available in the [functional tests suite](https://github.com/szpak/gradle-pitest-plugin/blob/master/src/funcTest/resources/testProjects/junit5simple/build.gradle).

For mixing JUnit 5 with other PIT plugins, you can read [this section](https://blog.solidsoft.pl/2020/02/27/pit-junit-5-and-gradle-with-just-one-extra-line-of-configuration/#modern-approach-with-plugins-br-with-older-gradle-pitest-plugin) in my blog post.

### Generic plugin support (also JUnit 5 in gradle-pitest-plugin <1.4.7)

To enable PIT plugins, it is enough to add it to the pitest configuration in the buildscript closure and also set the `testPlugin` property. For example:

```groovy
buildscript {
   repositories {
       mavenCentral()
   }
   configurations.maybeCreate('pitest')
   dependencies {
       classpath 'info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.4.7'
       pitest 'org.pitest:pitest-junit5-plugin:0.12'
   }
}

pitest {
    testPlugin = 'junit5' //or built-in 'testng' which also has to be activated
    // ...
}
```

The minimal working example is available in the [functional tests suite](https://github.com/szpak/gradle-pitest-plugin/blob/master/src/funcTest/groovy/info/solidsoft/gradle/pitest/functional/PitestPluginFunctional1Spec.groovy#L69-91).


## Versions

Every gradle-pitest-plugin version by default uses a predefined PIT version. Usually this a the latest released version
of PIT available at the time of releasing a plugin version. It can be overridden by using `pitestVersion` parameter
in a `pitest` configuration closure.

Please be aware that in some cases there could be some issues when using non default PIT versions.

gradle-pitest-plugin 1.3.x by default uses PIT 1.3.x, 1.2.x uses PIT 1.2.x, etc.

Starting with version 1.4.5 gradle-pitest-plugin requires Gradle 5.1. The latest version with the Gradle 4.x support is 1.4.0.
The current version was automatically smoke tested with Gradle 5.1, 5.6.1 and 6.0-SNAPSHOT under Java 8.
Tests with Java 9 - 13 are limited to the compatible versions of Gradle and PIT.

Java 11 is officially supported starting with gradle-pitest-plugin 1.4.0 (thanks to using PIT 1.4.3).

Due to incompatible changes in Gradle 4/5/6 support for the older Gradle versions have been limited/removed.
The latest version which supports Gradle 2 and 3 is gradle-pitest-plugin 1.3.0. Gradle 1.x (1.6+) was supported by gradle-pitest-plugin 1.1.4.

Starting with the version 1.3.0 the produced binaries [require](https://github.com/szpak/gradle-pitest-plugin/issues/70#issuecomment-360989155) Java 8
(as a JDK used for running a Gradle build). The latest version with Java 7 compatible binaries is 1.2.4.

See the [changelog file](https://github.com/szpak/gradle-pitest-plugin/blob/master/CHANGELOG.md) for more detailed list of changes in the plugin itself.


## FAQ

### 1. Why have I got `java.lang.VerifyError: Expecting a stackmap frame...` when using Java 7?

    It should be fixed in PIT 0.29.
    As a workaround in older versions add `jvmArgs = '-XX:-UseSplitVerifier'` to a pitest configuration block

```groovy
pitest {
    ...
    //jvmArgs = '-XX:-UseSplitVerifier'     //<0.33.0
    jvmArgs = ['-XX:-UseSplitVerifier']     //>=0.33.0
}
```

### 2. Why have I got `GroovyCastException: Cannot cast object '-Xmx1024', '-Xms512m' with class 'java.lang.String' to class 'java.util.List'` after upgrade to version 0.33.0?

To keep consistency with the new `mainProcessJvmArgs` configuration parameter and make an input format more predictable `jvmArgs` parameter type was changed from `String` to `List<String>` in gradle-pitest-plugin 0.33.0. The migration is trivial, but unfortunately I am not aware of the way to keep both parameter types active at the same time.

```groovy
pitest {
    ...
    //jvmArgs = '-Xmx1024 -Xms512m'     //old format
    jvmArgs = ['-Xmx1024', '-Xms512m']  //new format
}
```

### 3. Why my Spring Boot application doesn't work correctly with gradle-pitest-plugin 0.33.0 applied?

**Update**. Spring Boot 1.1.0 is fully compatible with gradle-pitest-plugin.

There ~~is~~ was an [issue](https://github.com/spring-projects/spring-boot/issues/721) with the way how spring-boot-gradle-plugin (<1.1.0) handles JavaExec tasks
(including pitest task which in the version 0.33.0 became JavaExec task to resolve classpath issue with configured non default PIT version - see
[issue #7](https://github.com/szpak/gradle-pitest-plugin/issues/7)).

Luckily there is a workaround which allows to run PIT 0.33 (with Java 8 support) with gradle-pitest-plugin 0.32.0:

```groovy
buildscript {
    (...)
    dependencies {
        classpath('info.solidsoft.gradle.pitest:gradle-pitest-plugin:0.32.0') {
          exclude group: 'org.pitest'
        }
        classpath 'org.pitest:pitest-command-line:0.33'
    }
}

pitest {
    pitestVersion = '0.33'
}
```

### 4. How can I override plugin configuration from command line/system properties?

Gradle does not provide a built-in way to override plugin configuration via command line, but [gradle-override-plugin](https://github.com/nebula-plugins/gradle-override-plugin)
can be used to do that.

After [applied](https://github.com/nebula-plugins/gradle-override-plugin) gradle-override-plugin in **your** project it is possible to do following:

    ./gradlew pitest -Doverride.pitest.reportDir=build/pitReport -Doverride.pitest.threads=8

Note. The mechanism should work fine for String and numeric properties, but the are limitations with support of
[Lists/Sets/Maps](https://github.com/nebula-plugins/gradle-override-plugin/issues/3) and [Boolean values](https://github.com/nebula-plugins/gradle-override-plugin/issues/1).

For more information see project [web page](https://github.com/nebula-plugins/gradle-override-plugin).

### 5. Why I see `Could not find org.pitest:pitest-command-line:1.1.0` error in my multiproject build?

    Could not resolve all dependencies for configuration ':pitest'.
    > Could not find org.pitest:pitest-command-line:1.1.0.
      Required by:
          :Gradle-Pitest-Example:unspecified

Starting from version 1.0.0 for multi-project builds gradle-pitest-plugin dependency should be added to the buildscript configuration in the root project.
The plugin should be applied in all subprojects which should be processed with PIT.

### 6. How can I change PIT version from default to just released the newest one?

gradle-pitest-plugin by default uses a corresponsing PIT version (with the same number). The plugin is released only if there are internal changes or
there is a need to adjust to changes in newer PIT version. There is a dedicated mechanism to allow to use the latest PIT version (e.g, a bugfix release)
or to downgrade PIT in case of detected issues. To override a defalt version it is enough to set `pitestVersion` property in the `pitest` configuration
closure.

```groovy
pitest {
    pitestVersion = '2.8.1-the.greatest.one'
}
```

In case of errors detected when the latest available version of the plugin is used with newer PIT version please raise an [issue](https://github.com/szpak/gradle-pitest-plugin/issues).

### 7. How to disable placing PIT reports in time-based subfolders?

Placing PIT reports directly in `${PROJECT_DIR}/build/reports/pitest` can be enabled with `timestampedReports` configuration property:

```groovy
pitest {
    timestampedReports = false
}
```

### 8. How can I debug a gradle-pitest-plugin execution or a PIT process execution itself in a Gradle build?

Ocasionally, it may be useful to debug a gradle-pitest-plugin execution or a PIT execution itself (e.g. [NPE in PIT](https://github.com/hcoles/pitest/issues/345)) to provide sensible error report.

The gradle-pitest-plugin execution can be remotely debugged with adding `-Dorg.gradle.debug=true` to the command line.

However, as PIT is started as a separate process to debug its execution the following arguments need to be added to the plugin configuration:

```groovy
pitest {
    mainProcessJvmArgs = ['-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005']
}
```

### 9. Can I use gradle-pitest-plugin with my Android application?

Short answer is: not directly. Due to some [incompatibilities](https://github.com/szpak/gradle-pitest-plugin/issues/31) between "standard" Java applications and Android Java applications in Gradle the plugin does not support the later. Luckily, there is an Android [fork](https://github.com/koral--/gradle-pitest-plugin/) of the plugin maintained by [Karol Wrótniak](https://github.com/koral--) which provides a modified version supporting Android applications (but on the other hand it doesn't work with standard Java applications).

### 10. How to use gradle-pitest-plugin 1.3.0 with Java 11?

**Update**. gradle-pitest-plugin 1.4.0 should work with Java 11 out of the box.

The gradle-pitest-plugin 1.3.0 is smoke testing with Java 8, 9, 10 and 11. However, to run PIT sucessfully with Java 11, it is required to override a default PIT version defined in the plugin to 1.4.3 (or preferably to the latest one). It can be simply done in the project configuration with:

```groovy
pitest {
    pitestVersion = '1.4.3'  //for Java 11 compatibility with gradle-pitest-plugin 1.3.0
}
```

## Known issues

 - too verbose output from PIT

 - ~~0.33.0+ is not compatible with Spring Boot projects due to a [bug](https://github.com/spring-projects/spring-boot/issues/721) in spring-boot-gradle-plugin - see FAQ for a workaround~~ - works with Spring Boot 1.1.0+


## Development

gradle-pitest-plugin cloned from the repository can be built using Gradle command:

    ./gradlew build

The easiest way to make a JAR with local changes visible in another project is to install it into the local Maven repository:

    ./gradlew install

There are also basic functional tests written using [nebula-test](https://github.com/nebula-plugins/nebula-test/) which can be run with:

    ./gradlew funcTest


## Support

[gradle-pitest-plugin](https://gradle-pitest-plugin.solidsoft.info/) was written by Marcin Zajączkowski with a help from [contributors](https://github.com/szpak/gradle-pitest-plugin/graphs/contributors).
The author can be contacted directly via email: mszpak ATT wp DOTT pl.
There is also Marcin's blog available: [Solid Soft](https://blog.solidsoft.info) - working code is not enough.

The plugin surely has some bugs and missing features. They can be reported using an [issue tracker](https://github.com/szpak/gradle-pitest-plugin/issues).
However it is often a better idea to send a questions to the [PIT mailing list](https://groups.google.com/group/pitusers) first.

The plugin is licensed under the terms of [the Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).

![Stat Counter stats](https://c.statcounter.com/9394072/0/db9b06ab/0/)
