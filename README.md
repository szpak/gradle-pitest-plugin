# Gradle plugin for PIT Mutation Testing

The plugin provides an ability to perform a [mutation testing](https://en.wikipedia.org/wiki/Mutation_testing) and
calculate a mutation coverage of a [Gradle](http://gradle.org/)-based projects with [PIT](http://pitest.org/).

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/info.solidsoft.gradle.pitest/gradle-pitest-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/info.solidsoft.gradle.pitest/gradle-pitest-plugin)

## Quick start

### Generic approach

Add gradle-pitest-plugin to the buildscript dependencies in your build.gradle file:

    buildscript {
        repositories {
            mavenCentral()
            //Needed only for SNAPSHOT versions
            //maven { url "http://oss.sonatype.org/content/repositories/snapshots/" }
        }
        dependencies {
            classpath 'info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.1.1'
        }
    }

Apply plugin:

    apply plugin: "info.solidsoft.pitest"

Call Gradle with pitest task:

    gradle pitest

After the measurements a report created by PIT will be placed in `${PROJECT_DIR}/build/reports/pitest` directory.

### Older gradle-pitest-plugin versions (<1.1.0)

For versions <1.1.0 the plugin can be applied with:

    apply plugin: "pitest"

### New plugin mechanism introduced in Gradle 2.1

    plugins {
      id "info.solidsoft.pitest" version "1.1.1"
    }

Please note that as of Gradle 2.1 the new mechanism cannot be used in multi project builds.


## Plugin configuration

The Pitest plugin has to be configured. All the [command line options](http://pitest.org/quickstart/commandline/) are
supported. To make life easier `taskClasspath`, `mutableCodePaths`, `sourceDirs`, `reportDir` and `pitestVersion` are
automatically set by a plugin. In addition `sourceDirs`, `reportDir` and `pitestVersion` can be overridden by an user.

In the past there was one mandatory parameter - `targetClasses` - which points to the classes which should be mutated.
Starting from 0.32.0 it is only required if a [group](http://www.gradle.org/docs/current/userguide/writing_build_scripts.html#N10A34)
for the project is not set. Otherwise value `"${project.group}.*"` is set by default (which can be overridden using pitest.targetClasses parameter).

In case of using not default PIT version the `pitestVersion` parameter should be used to override it.

The configuration in Gradle is the real Groovy code which makes all assignments very intuitive. All values expected by
PIT should be passed as a corresponding types. There is only one important difference. For the parameters where PIT expects
a coma separated list of strings in a Gradle configuration a list of strings should be used (see `outputFormats` in the
following example).

    pitest {
        targetClasses = ['our.base.package.*']  //by default "${project.group}.*"
        pitestVersion = "1.1.0" //not needed when a default PIT version should be used
        threads = 4
        outputFormats = ['XML', 'HTML']
    }

Check PIT documentation for a [list](http://pitest.org/quickstart/commandline/) of all available command line parameters.
The expected parameter format in a plugin configuration can be taken from
[PitestPluginExtension](https://github.com/szpak/gradle-pitest-plugin/blob/master/src/main/groovy/info/solidsoft/gradle/pitest/PitestPluginExtension.groovy).

There are a few parameters specific for Gradle plugin:

 - enableDefaultIncrementalAnalysis - enables incremental analysis in PIT using the default settings (build/pitHistory.txt
file for both input and output locations) (since 0.29.0)
 - testSourceSets - defines test source sets which should be used by PIT (by default sourceSets.test, but allows
to add integration tests located in a different source set) (since 0.30.1)
 - mainSourceSets - defines main source sets which should be used by PIT (by default sourceSets.main) (since 0.30.1)
 - mainProcessJvmArgs - JVM arguments to be used when launching the main PIT process; make a note that PIT itself launches
another Java processes for mutation testing execution and usually `jvmArgs` should be used to for example increase maximum memory size
(since 0.33.0 - see [#7](https://github.com/szpak/gradle-pitest-plugin/issues/7));

For example:

    pitest {
        ...
        enableDefaultIncrementalAnalysis = true
        testSourceSets = [sourceSets.test, sourceSets.integrationTest]
        mainSourceSets = [sourceSets.main, sourceSets.additionalMain]
        jvmArgs = ['-Xmx1024m']
    }


## Multi-module projects support

gradle-pitest-plugin can be used in multi-module projects. The plugin has to be applied in all subprojects which should be
processed with PIT. A sample snippet from build.gradle located for the root project:

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

Currently PIT [does not provide](https://code.google.com/p/pitestrunner/issues/detail?id=41) an aggregated report for
multi-module project. A report for each module has to be browsed separately. Alternatively a
[PIT plugin for Sonar](https://docs.codehaus.org/display/SONAR/Pitest) can be used to get aggregated results.


## Versions

Every gradle-pitest-plugin version by default uses a predefined PIT version. Usually this a the latest released version
of PIT available at the time of releasing a plugin version. It can be overridden by using `pitestVersion` parameter
in a pitest configuration closure.

Note. There could be some issues when using non default PIT versions.

gradle-pitest-plugin 1.1.x by default uses PIT 1.1.x, 1.0.x uses PIT 1.0.x, etc.

Note. PIT 0.27 is not supported due to [issue 47](https://code.google.com/p/pitestrunner/issues/detail?id=47).

Note. Due to internal refactoring in PIT versions >=0.32 require gradle-pitest-plugin >=0.32.x and PIT versions <=0.31 gradle-pitest-plugin <=0.30.x.

gradle-pitest-plugin 1.1.1 requires Gradle 1.6+ and was tested with Gradle 1.6 to 1.12 and Gradle 2.0 to 2.1 under OpenJDK 8, Oracle JDK 8, OpenJDK 7 and Sun 1.6.

See [changelog file](https://github.com/szpak/gradle-pitest-plugin/blob/master/CHANGELOG.md) for more detailed list of changes in the plugin itself.


## FAQ

1. Why have I got `java.lang.VerifyError: Expecting a stackmap frame...` when using Java 7?

    It should be fixed in PIT 0.29.
    As a workaround in older versions add `jvmArgs = '-XX:-UseSplitVerifier'` to a pitest configuration block

        pitest {
            ...
            //jvmArgs = '-XX:-UseSplitVerifier'     //<0.33.0
            jvmArgs = ['-XX:-UseSplitVerifier']     //>=0.33.0
        }

2. Why have I got `GroovyCastException: Cannot cast object '-Xmx1024', '-Xms512m' with class 'java.lang.String' to class 'java.util.List'`
after upgrade to version 0.33.0?

    To keep consistency with the new `mainProcessJvmArgs` configuration parameter and make an input format more predictable
    `jvmArgs` parameter type was changed from `String` to `List<String>` in gradle-pitest-plugin 0.33.0. The migration is trivial,
    but unfortunately I am not aware of the way to keep both parameter types active at the same time.

        pitest {
            ...
            //jvmArgs = '-Xmx1024 -Xms512m'     //old format
            jvmArgs = ['-Xmx1024', '-Xms512m']  //new format

        }

3. Why my Spring Boot application doesn't work correctly with gradle-pitest-plugin 0.33.0 applied?

**Update**. Spring Boot 1.1.0 is fully compatible with gradle-pitest-plugin.

There ~~is~~ was an [issue](https://github.com/spring-projects/spring-boot/issues/721) with the way how spring-boot-gradle-plugin (<1.1.0) handles JavaExec tasks
(including pitest task which in the version 0.33.0 became JavaExec task to resolve classpath issue with configured non default PIT version - see
[issue #7](https://github.com/szpak/gradle-pitest-plugin/issues/7)).

Luckily there is a workaround which allows to run PIT 0.33 (with Java 8 support) with gradle-pitest-plugin 0.32.0:

    buildscript {
        (...)
        dependencies {
            classpath("info.solidsoft.gradle.pitest:gradle-pitest-plugin:0.32.0") {
              exclude group: "org.pitest"
            }
            classpath "org.pitest:pitest-command-line:0.33"
        }
    }

    pitest {
        pitestVersion = "0.33"
    }

4. How can I override plugin configuration from command line/system properties?

Gradle does not provide a built-in way to override plugin configuration via command line, but [gradle-override-plugin](https://github.com/nebula-plugins/gradle-override-plugin)
can be used to do that.

After [applied](https://github.com/nebula-plugins/gradle-override-plugin) gradle-override-plugin in **your** project it is possible to do following:

    ./gradlew pitest -Doverride.pitest.reportDir=build/pitReport -Doverride.pitest.threads=8

Note. The mechanism should work fine for String and numeric properties, but the are limitations with support of
[Lists/Sets/Maps](https://github.com/nebula-plugins/gradle-override-plugin/issues/3) and [Boolean values](https://github.com/nebula-plugins/gradle-override-plugin/issues/1).

For more information see project [web page](https://github.com/nebula-plugins/gradle-override-plugin).


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

[gradle-pitest-plugin](http://gradle-pitest-plugin.solidsoft.info/) was written by Marcin Zajączkowski.
The author can be contacted directly via email: mszpak ATT wp DOTT pl.
There is also Marcin's blog available: [Solid Soft](http://blog.solidsoft.info) - working code is not enough.

The plugin surely has some bugs and missing features. They can be reported using an [issue tracker](https://github.com/szpak/gradle-pitest-plugin/issues).
However it is often a better idea to send a questions to the [PIT mailing list](http://groups.google.com/group/pitusers) first.

The plugin is licensed under the terms of [the Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).

![Stat Counter stats](https://c.statcounter.com/9394072/0/db9b06ab/0/)
