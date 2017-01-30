# Gradle plugin for PIT Mutation Testing

The plugin provides an ability to perform a [mutation testing](https://en.wikipedia.org/wiki/Mutation_testing) and
calculate a mutation coverage of a [Gradle](http://gradle.org/)-based projects with [PIT](http://pitest.org/).

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/info.solidsoft.gradle.pitest/gradle-pitest-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/info.solidsoft.gradle.pitest/gradle-pitest-plugin) [![Build Status Travis](https://travis-ci.org/szpak/gradle-pitest-plugin.svg?branch=master)](https://travis-ci.org/szpak/gradle-pitest-plugin) [![Build Status Jenkins](https://solidsoft.ci.cloudbees.com/buildStatus/icon?job=pitest-gradle-plugin)](https://solidsoft.ci.cloudbees.com/job/pitest-gradle-plugin/)

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
            classpath 'info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.1.11'
        }
    }

Apply the plugin:

    apply plugin: "info.solidsoft.pitest"

Call Gradle with pitest task:

    gradle pitest

After the measurements a report created by PIT will be placed in `${PROJECT_DIR}/build/reports/pitest` directory.

Optionally make it depend on build:

    build.dependsOn "pitest"

Note that when making `pitest` depend on another task, it must be referred to by name. Otherwise Gradle will resolve `pitest` to the configuration and not the task.

### Older gradle-pitest-plugin versions (<1.1.0)

For versions <1.1.0 the plugin can be applied with:

    apply plugin: "pitest"

### New plugin mechanism introduced in Gradle 2.1

The plugin upload mechanism to Gradle Plugins Repository has changed once again and gradle-pitest-plugin 1.1.6+ artifacts are
available only from Maven Central.

## Plugin configuration

The Pitest plugin has to be configured. All the [command line options](http://pitest.org/quickstart/commandline/) are
supported. To make life easier `taskClasspath`, `mutableCodePaths`, `sourceDirs`, `reportDir` and `pitestVersion` are
automatically set by a plugin. In addition `sourceDirs`, `reportDir` and `pitestVersion` can be overridden by an user.

In the past there was one mandatory parameter - `targetClasses` - which points to the classes which should be mutated.
Starting from 0.32.0 it is only required if a [group](http://www.gradle.org/docs/current/userguide/writing_build_scripts.html#N10A34)
for the project is not set. Otherwise value `"${project.group}.*"` is set by default (which can be overridden using `pitest.targetClasses` parameter).

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
 - additionalMutableCodePaths - additional classes to mutate (useful for integration tests with production code in a different module - since 1.1.4 -
see [#25](https://github.com/szpak/gradle-pitest-plugin/issues/25))

For example:

    pitest {
        ...
        enableDefaultIncrementalAnalysis = true
        testSourceSets = [sourceSets.test, sourceSets.integrationTest]
        mainSourceSets = [sourceSets.main, sourceSets.additionalMain]
        jvmArgs = ['-Xmx1024m']
    }


## Multi-module projects support

gradle-pitest-plugin can be used in multi-module projects. The gradle-pitest-plugin dependency should be added to the buildscript configuration in
the root project while the plugin has to be applied in all subprojects which should be processed with PIT. A sample snippet from build.gradle located
for the root project:

    //in root project configuration
    buildscript {
        repositories {
            mavenCentral()
        }
        dependencies {
            classpath 'info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.1.11'
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

Currently PIT [does not provide](https://code.google.com/p/pitestrunner/issues/detail?id=41) an aggregated report for
multi-module project. A report for each module has to be browsed separately. Alternatively a
[PIT plugin for Sonar](https://github.com/SonarCommunity/sonar-pitest) can be used to get aggregated results.

## Integration tests in separate subproject

Since gradle-pitest-plugin 1.1.4 it is possible to mutate code located in different subproject. Gradle internally does not rely on
output directory from other subproject, but builds JAR and uses classes from it. For PIT those are two different sets of class files, so
to make it work it is required to define both `mainSourceSets` and `additionalMutableCodePaths`. For example:

    configure(project(':itest')) {
        apply plugin: "info.solidsoft.pitest"
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

The above is the way recommended by the [Gradle team](http://forums.gradle.org/gradle/topics/how-to-get-file-path-to-binary-jar-produced-by-subproject#reply_15315782),
but in specific cases the simpler solution should also work:

    configure(project(':itest')) {
        apply plugin: "info.solidsoft.pitest"
        dependencies {
            compile project(':shared')
        }

        pitest {
            mainSourceSets = [project.sourceSets.main, project(':shared').sourceSets.main]
            additionalMutableCodePaths = project(':shared').jar.outputs.files.getFiles()
        }
    }

Minimal working multi-project build is available in
[functional tests suite](https://github.com/szpak/gradle-pitest-plugin/tree/master/src/test/resources/testProjects/multiproject).

## PIT plugins support

PIT plugins are officially supported since gradle-pitest-plugin 1.1.4 (although it was possible to use it since 1.1.0).

To enable PIT plugin it is enough to add it to pitest configuration in buildscript closure. For example:

    buildscript {
       repositories {
           mavenCentral()
       }
       configurations.maybeCreate("pitest")
       dependencies {
           classpath 'info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.1.11'
           pitest 'org.pitest.plugins:pitest-fancy-plugin:0.0.1'
       }
    }

The minimal working example is available in [functional tests suite](https://github.com/szpak/gradle-pitest-plugin/blob/master/src/funcTest/groovy/info/solidsoft/gradle/pitest/functional/PitestPluginFunctional1Spec.groovy#L69-91).

## Versions

Every gradle-pitest-plugin version by default uses a predefined PIT version. Usually this a the latest released version
of PIT available at the time of releasing a plugin version. It can be overridden by using `pitestVersion` parameter
in a pitest configuration closure.

Note. There could be some issues when using non default PIT versions.

gradle-pitest-plugin 1.1.x by default uses PIT 1.1.x, 1.0.x uses PIT 1.0.x, etc.

Note. Due to internal refactoring in PIT versions >=0.32 require gradle-pitest-plugin >=0.32.x and PIT versions <=0.31 gradle-pitest-plugin <=0.30.x.

Starting since version 1.1.6 gradle-pitest-plugin requires Gradle 2.0+. The current version was automatically smoke tested with Gradle 2.0 to 2.14.1 and 3.0-rc-2 under Java 7 and 8.
The latest version which supports older Gradle 1.x (1.6+) is gradle-pitest-plugin 1.1.4.

See [changelog file](https://github.com/szpak/gradle-pitest-plugin/blob/master/CHANGELOG.md) for more detailed list of changes in the plugin itself.


## FAQ

### 1. Why have I got `java.lang.VerifyError: Expecting a stackmap frame...` when using Java 7?

    It should be fixed in PIT 0.29.
    As a workaround in older versions add `jvmArgs = '-XX:-UseSplitVerifier'` to a pitest configuration block

        pitest {
            ...
            //jvmArgs = '-XX:-UseSplitVerifier'     //<0.33.0
            jvmArgs = ['-XX:-UseSplitVerifier']     //>=0.33.0
        }

### 2. Why have I got `GroovyCastException: Cannot cast object '-Xmx1024', '-Xms512m' with class 'java.lang.String' to class 'java.util.List'` after upgrade to version 0.33.0?

To keep consistency with the new `mainProcessJvmArgs` configuration parameter and make an input format more predictable `jvmArgs` parameter type was changed from `String` to `List<String>` in gradle-pitest-plugin 0.33.0. The migration is trivial, but unfortunately I am not aware of the way to keep both parameter types active at the same time.

        pitest {
            ...
            //jvmArgs = '-Xmx1024 -Xms512m'     //old format
            jvmArgs = ['-Xmx1024', '-Xms512m']  //new format

        }

### 3. Why my Spring Boot application doesn't work correctly with gradle-pitest-plugin 0.33.0 applied?

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

    pitest {
        pitestVersion = "1.2.9-the.greatest.one"
    }

In case of errors detected when the latest available version of the plugin is used with newer PIT version please raise an [issue](https://github.com/szpak/gradle-pitest-plugin/issues).

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

<div id="statcounter" style="width:100%; text-align:center">
<!-- StatCounter -->
<script type="text/javascript">
var sc_project=9394072;
var sc_invisible=0;
var sc_security="db9b06ab";
var scJsHost = (("https:" == document.location.protocol) ?
"https://secure." : "http://www.");
document.write("<sc"+"ript type='text/javascript' src='" +
scJsHost+
"statcounter.com/counter/counter.js'></"+"script>");
</script>
<noscript><div class="statcounter"><a title="create counter"
href="http://statcounter.com/free-hit-counter/"
target="_blank"><img class="statcounter"
src="https://c.statcounter.com/9394072/0/db9b06ab/0/"
alt="create counter"></a></div></noscript>
</div>
<br />

<div id="cloudbees" style="width:100%; text-align:center">
<a href="http://www.cloudbees.com/foss/foss-dev.cb" target="_blank"><img src="images/cloudbees-badge.png"/></a>
</div>

<!-- Google Analytics -->
<script>
(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
})(window,document,'script','//www.google-analytics.com/analytics.js','ga');
ga('create', 'UA-46065645-1', 'solidsoft.info');
ga('send', 'pageview');
</script>
