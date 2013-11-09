# Gradle plugin for PIT Mutation Testing

The plugin provides an ability to perform a [mutation testing](https://en.wikipedia.org/wiki/Mutation_testing) and
calculate a mutation coverage of a [Gradle](http://gradle.org/)-based project with [PIT](http://pitest.org/).

## Quick start

Add gradle-pitest-plugin and pitest itself to the buildscript dependencies in your build.gradle file:

    buildscript {
        repositories {
            mavenCentral()
            mavenLocal()
            //Needed only for SNAPSHOT versions
            //maven { url "http://oss.sonatype.org/content/repositories/snapshots/" }
        }
        dependencies {
            classpath 'info.solidsoft.gradle.pitest:gradle-pitest-plugin:0.30.1'
            classpath "org.pitest:pitest:0.30"
        }
    }

Apply plugin:

    apply plugin: "pitest"

Add a pitest configuration closure:

    pitest {
        targetClasses = ['our.base.package.*']
    }

Call Gradle with pitest task:

    gradle pitest

After the measurements a report created by PIT will be placed in ${PROJECT_DIR}/build/reports/pitest directory.

## Plugin configuration

The Pitest plugin has to be configured. All the [command line options](http://pitest.org/quickstart/commandline/) are
supported. To make life easier taskClasspath, mutableCodePaths, sourceDirs, reportDir and pitestVersion are
automatically set by a plugin. In addition sourceDirs, reportDir and pitestVersion can be overridden by an user.

There is one mandatory parameter - targetClasses - which points to the classes which should be mutated. In case of using
not default PIT version the pitestVersion parameter should be used to override it.

The configuration in Gradle is the real Groovy code which makes all assignments very intuitive. All values expected by
PIT should be passed as a corresponding types. There is only one important difference. For the parameters where PIT expects
a coma separated list of strings in a Gradle configuration a list of strings should be used (see outputFormats in the 
following example).

    pitest {
        targetClasses = ['our.base.package.*']
        pitestVersion = "0.31" //not needed when a default PIT version should be used
        threads = 4
        outputFormats = ['XML', 'HTML']
    }

Check PIT documentation for a [list](http://pitest.org/quickstart/commandline/) of all available command line parameters.

There are a few parameters specific for Gradle plugin:
 - enableDefaultIncrementalAnalysis - enables incremental analysis in PIT using the default settings (build/pitHistory.txt
file for both input and output locations) (since 0.29.0)
 - testSourceSets - defines test source sets which should be used by PIT (by default sourceSets.test, but allows
to add integration tests located in a different source set) (since 0.30.1)
 - mainSourceSets - defines main source sets which should be used by PIT (by default sourceSets.main) (since 0.30.1)

For example:

    pitest {
        ...
        enableDefaultIncrementalAnalysis = true
        testSourceSets = [sourceSets.test, sourceSets.integrationTest]
        mainSourceSets = [sourceSets.main, sourceSets.additionalMain]
    }

## Multi-module projects support

gradle-pitest-plugin can be used in multi-module projects. The plugin has to be applied in all subprojects which should be
processed with PIT. A sample snippet from build.gradle located for the root project:

    subprojects {
        ...
        apply plugin: 'pitest'

        pitest {
            targetClasses = ['our.base.package.*']
            threads = 4

            if (project.name in ['module-without-any-test'] {
                failWhenNoMutation = false
            }
        }
    }

Currently PIT [does not provide](https://code.google.com/p/pitestrunner/issues/detail?id=41) an aggregated report for
multi-module project. A report for each module has to be browsed separately. Alternatively a
[PIT plugin for Sonar](https://docs.codehaus.org/display/SONAR/Pitest) can be used to get aggregated results.

## Versions

Every gradle-pitest-plugin version by default uses a predefined PIT version. Usually this a the latest released version
of PIT available at the time of releasing a plugin version. It can be overridden by using pitestVersion parameter
in a pitest configuration closure *and* specifying *the same* version as a buildscript dependency.

Note. There could be some issues when using non default PIT versions.

gradle-pitest-plugin 0.30.x uses PIT 0.30, 0.29.0 uses PIT 0.29. etc.

Note. 0.27 is not supported due to [issue 47](https://code.google.com/p/pitestrunner/issues/detail?id=47).

gradle-pitest-plugin 0.30.0 was tested with Gradle 1.5 and 1.6 under OpenJDK 7 and Sun 1.6.

## FAQ

1. Why have I got "java.lang.VerifyError: Expecting a stackmap frame..." when using Java 7?

    It should be fixed in PIT 0.29.
    As a workaround in older versions add "jvmArgs = '-XX:-UseSplitVerifier'" to a pitest configuration block

        pitest {
            ...
            jvmArgs = '-XX:-UseSplitVerifier'
        }

## Known issues

 - too verbose output from PIT

## Development

gradle-pitest-plugin cloned from the repository can be built using Gradle command:

    ./gradlew build

The easiest way to make a JAR with local changes visible in another project is to install it into the local Maven repository

    ./gradlew install

## Support

[gradle-pitest-plugin](http://gradle-pitest-plugin.solidsoft.info/) was written by Marcin Zajączkowski.
The author can be contacted directly via email: mszpak ATT wp DOTT pl.
There is also Marcin's blog available: [Solid Soft](http://blog.solidsoft.info) - working code is not enough.

The plugin surely has some bugs and missing features. They can be reported using an [issue tracker](https://github.com/szpak/gradle-pitest-plugin/issues).
However it is often a better idea to send a questions to the [PIT mailing list](http://groups.google.com/group/pitusers) first.

The plugin is licensed under the terms of [the Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).
