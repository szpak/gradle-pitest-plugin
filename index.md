# Gradle plugin for PIT Mutation Testing

The plugin provides an ability to perform a [mutation testing](https://en.wikipedia.org/wiki/Mutation_testing) and
calculate a mutation coverage of a [Gradle](https://gradle.org/)-based projects with [PIT](http://pitest.org/).

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/info.solidsoft.gradle.pitest/gradle-pitest-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/info.solidsoft.gradle.pitest/gradle-pitest-plugin)
[![Plugin Portal](https://img.shields.io/maven-metadata/v?label=Plugin&color=blue&metadataUrl=https://plugins.gradle.org/m2/info/solidsoft/gradle/pitest/gradle-pitest-plugin/maven-metadata.xml)](https://plugins.gradle.org/plugin/info.solidsoft.pitest)
[![Build Status Travis](https://app.travis-ci.com/szpak/gradle-pitest-plugin.svg?branch=master)](https://app.travis-ci.com/szpak/gradle-pitest-plugin)
[![Windows Build Status](https://ci.appveyor.com/api/projects/status/github/szpak/gradle-pitest-plugin?branch=master&svg=true)](https://ci.appveyor.com/project/szpak/gradle-pitest-plugin/)
[![Dependabot Status](https://api.dependabot.com/badges/status?host=github&repo=szpak/gradle-pitest-plugin)](https://dependabot.com)

## Quick start

### The simplest way

Add gradle-pitest-plugin to the `plugins` configuration in your `build.gradle` file:

```groovy
plugins {
    id 'java' //or 'java-library' - depending on your needs
    id 'info.solidsoft.pitest' version '1.15.0'
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
        classpath 'info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.15.0'
    }
}
```

Apply the plugin:

```groovy
apply plugin: 'java' //or 'java-library' - depending on your needs
apply plugin: 'info.solidsoft.pitest'
```


## Plugin configuration

The Pitest plugin does not need to be additionally configured if you use JUnit 4. Customization is done in the `pitest` block:


```groovy
pitest {
    targetClasses = ['our.base.package.*']  //by default "${project.group}.*"
    pitestVersion = '1.15.0' //not needed when a default PIT version should be used
    threads = 4
    outputFormats = ['XML', 'HTML']
    timestampedReports = false
}
```

The configuration in Gradle is the real Groovy code which makes all assignments very intuitive. All values expected by
PIT should be passed as a corresponding types. There is only one important difference. For the parameters where PIT expects
a coma separated list of strings in a Gradle configuration a list of strings should be used (see `outputFormats` in the
example above).

Check PIT documentation for a [list](https://pitest.org/quickstart/commandline/) of all available command line parameters.
The expected parameter format in a plugin configuration can be taken from
[PitestPluginExtension](https://github.com/szpak/gradle-pitest-plugin/blob/master/src/main/groovy/info/solidsoft/gradle/pitest/PitestPluginExtension.groovy).

To make life easier `taskClasspath`, `mutableCodePaths`, `sourceDirs`, `reportDir`, `verbosity` and `pitestVersion` are
automatically set by the plugin. In addition `sourceDirs`, `reportDir`, `verbosity` and `pitestVersion` can be overridden by a user.

There are a few parameters specific for Gradle plugin:

 - `testSourceSets` - defines test source sets which should be used by PIT (by default sourceSets.test, but allows
to add integration tests located in a different source set)
 - `mainSourceSets` - defines main source sets which should be used by PIT (by default sourceSets.main)
 - `mainProcessJvmArgs` - JVM arguments to be used when launching the main PIT process; make a note that PIT itself launches
another Java processes for mutation testing execution and usually `jvmArgs` should be used to for example increase maximum memory size
(see [#7](https://github.com/szpak/gradle-pitest-plugin/issues/7));
 - `additionalMutableCodePaths` - additional classes to mutate (useful for integration tests with production code in a different module - see [#25](https://github.com/szpak/gradle-pitest-plugin/issues/25))
 - `useClasspathFile` - enables passing additional classpath as a file content (useful for Windows users with lots of classpath elements, disabled by default)
 - `fileExtensionsToFilter` - provides ability to filter additional file extensions from PIT classpath (see [#53](https://github.com/szpak/gradle-pitest-plugin/issues/53))

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

### Test system properties

PIT executes tests in a JVM independent of the JVM used by Gradle to execute tests. If your tests require some system properties, you have to pass them to PIT as the plugin won't do it for you:

```groovy
test {
    systemProperty 'spring.test.constructor.autowire.mode', 'all'
}

pitest {
    jvmArgs = ['-Dspring.test.constructor.autowire.mode=all']
}
```

### Eliminate warning in Idea

As reported in [#170](https://github.com/szpak/gradle-pitest-plugin/pull/170) IntelliJ IDEA displays warnings about setting final fields (of [lazy configuration](https://docs.gradle.org/current/userguide/lazy_configuration.html)) in `build.gradle`. It is not a real problem as Gradle internally intercepts those calls and use a setter instead . Nevertheless, people which prefer to have no (less) warnings at the cost of less readable code can use setters instead, e.g:

```groovy
    testSourceSets.set([sourceSets.test, sourceSets.integrationTest])
    mainSourceSets.set([sourceSets.main, sourceSets.additionalMain])
    jvmArgs.set(['-Xmx1024m'])
    useClasspathFile.set(true)     //useful with bigger projects on Windows
    fileExtensionsToFilter.addAll('xml', 'orbit')
```

Similar syntax can be used also for Kotlin configuration (`build.gradle.kts`).

## Multi-module projects support

gradle-pitest-plugin can be used in [multi-module projects](src/funcTest/resources/testProjects/multiproject/build.gradle).
The gradle-pitest-plugin dependency should be added to the buildscript configuration in the root project while the plugin has to be applied in
all subprojects which should be processed with PIT. A sample snippet from build.gradle located for the root project:

```groovy
//in root project configuration
plugins {
    id 'info.solidsoft.pitest' version '1.15.0' apply false
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'info.solidsoft.pitest'

    pitest {
        threads = 4

        if (project.name in ['module-without-any-test']) {
            failWhenNoMutations = false
        }
    }
}
```
It is possible to aggregate pitest report for multi-module project using plugin `info.solidsoft.pitest.aggregator` and
task `pitestReportAggregate`. Root project must be properly configured to use `pitestReportAggregate` :


```groovy
//in root project configuration
plugins {
    id 'info.solidsoft.pitest' version '1.15.0' apply false
}

apply plugin: 'info.solidsoft.pitest.aggregator' // to 'pitestReportAggregate' appear

subprojects {
    apply plugin: 'java'
    apply plugin: 'info.solidsoft.pitest'

    pitest {
        // export mutations.xml and line coverage for aggregation
        outputFormats = ["XML"]
        exportLineCoverage = true
        timestampedReports = false
        ...
        reportAggregator {  //since 1.9.11 - extra results validation, if needed
            testStrengthThreshold.set(50)   //simpler Groovy syntax (testStrengthThreshold = 50) does not seem to be supported for nested properties
            mutationThreshold.set(40)
            maxSurviving.set(3)
        }
    }
}
```

After the `pitest pitestReportAggregate` tasks execution, the aggregated report will be placed in the `${PROJECT_DIR}/build/reports/pitest` directory.

## Integration tests in separate subproject

It is possible to mutate code located in different subproject. Gradle internally does not rely on
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

Test plugins are used to support different test frameworks than JUnit4.

### JUnit 5 plugin for PIT support (gradle-pitest-plugin 1.4.7+)

Starting with this release the configuration required to use PIT with JUnit 5 has been simplified to the following:

```groovy
plugins {
    id 'java'
    id 'info.solidsoft.pitest' version '1.15.0'
}

pitest {
    //adds dependency to org.pitest:pitest-junit5-plugin and sets "testPlugin" to "junit5"
    junit5PluginVersion = '1.0.0'    //or 0.15 for PIT <1.9.0
    // ...
}
```

**Please note**. PIT 1.9.0 requires pitest-junit5-plugin 1.0.0+. JUnit Jupiter 5.8 (JUnit Platform 1.8) requires pitest-junit5-plugin 0.15+, while 5.7 (1.7) requires 0.14. Set right plugin version for JUnit 5 version used in your project to avoid runtime errors (such as [`NoSuchMethodError: 'java.util.Optional org.junit.platform.commons.util.AnnotationUtils.findAnnotation(java.lang.Class, java.lang.Class, boolean)'](https://github.com/szpak/gradle-pitest-plugin/issues/300))).

The minimal working example for JUnit 5 is available in the [functional tests suite](https://github.com/szpak/gradle-pitest-plugin/blob/master/src/funcTest/resources/testProjects/junit5simple/build.gradle).

For mixing JUnit 5 with other PIT plugins, you can read [this section](https://blog.solidsoft.pl/2020/02/27/pit-junit-5-and-gradle-with-just-one-extra-line-of-configuration/#modern-approach-with-plugins-br-with-older-gradle-pitest-plugin) in my blog post.

### Generic plugin support (also JUnit 5 in gradle-pitest-plugin <1.4.7)

To enable PIT plugins, it is enough to add it to the pitest configuration in the buildscript closure. For example:

```groovy
plugins {
    id 'java'
    id 'info.solidsoft.pitest' version '1.15.0'
}

repositories {
    mavenCentral()
}

dependencies {
    pitest 'org.example.pit.plugins:pitest-custom-plugin:0.42'
}
```

The minimal working example is available in the [functional tests suite](https://github.com/szpak/gradle-pitest-plugin/blob/master/src/funcTest/groovy/info/solidsoft/gradle/pitest/functional/PitestPluginFunctional1Spec.groovy#L69-91).

Please note. In gradle-pitest-plugin <1.5.0 the `pitest` configuration had to be created in the `buildscript` scope for the root project.
Please note. Starting with PIT 1.6.7 it is no longer needed to set `testPlugin` configuration parameter. It is also deprecated in the Gradle plugin.

## Versions

Every gradle-pitest-plugin version by default uses a predefined PIT version. Usually this the latest released version
of PIT available at the time of releasing a plugin version. It can be overridden by using `pitestVersion` parameter
in a `pitest` configuration closure.

Please be aware that in some cases there could be some issues when using non default PIT versions.

If not stated otherwise, gradle-pitest-plugin 1.9.x by default uses PIT 1.9.x, 1.7.x uses PIT 1.7.x, etc. The minimal supported PIT version is 1.7.1.

Starting with version 1.7.0 gradle-pitest-plugin requires Gradle 6.4. The latest version with the Gradle 5.x (5.6+) support is 1.6.0.
The current version was automatically smoke tested with Gradle 6.4, 6.9.1 and 7.4.2 under Java 11.
Tests with Java 11+ are limited to the compatible versions of Gradle and PIT.

The experimental support for Java 17 can be tested with 1.7.0+.

Starting with the version 1.3.0 the produced binaries [require](https://github.com/szpak/gradle-pitest-plugin/issues/70#issuecomment-360989155) Java 8
(as a JDK used for running a Gradle build). However, having Java 17 LTS released in September 2021, starting with gradle-pitest-plugin 1.9.0, support for JDK <11 is deprecated (see [#299](https://github.com/szpak/gradle-pitest-plugin/issues/299)).

See the [changelog file](https://github.com/szpak/gradle-pitest-plugin/blob/master/CHANGELOG.md) for more detailed list of changes in the plugin itself.


## FAQ

### How can I override plugin configuration from command line/system properties?

Gradle does not provide a built-in way to override plugin configuration via command line, but [gradle-override-plugin](https://github.com/nebula-plugins/gradle-override-plugin)
can be used to do that.

After [applied](https://github.com/nebula-plugins/gradle-override-plugin) gradle-override-plugin in **your** project it is possible to do following:

    ./gradlew pitest -Doverride.pitest.reportDir=build/pitReport -Doverride.pitest.threads=8

Note. The mechanism should work fine for String and numeric properties, but there are limitations with support of
[Lists/Sets/Maps](https://github.com/nebula-plugins/gradle-override-plugin/issues/3) and [Boolean values](https://github.com/nebula-plugins/gradle-override-plugin/issues/1).

For more information see project [web page](https://github.com/nebula-plugins/gradle-override-plugin).

### How can I change PIT version from default to just released the newest one?

gradle-pitest-plugin by default uses a corresponding PIT version (with the same number). The plugin is released only if there are internal changes or
there is a need to adjust to changes in newer PIT version. There is a dedicated mechanism to allow to use the latest PIT version (e.g, a bugfix release)
or to downgrade PIT in case of detected issues. To override a default version it is enough to set `pitestVersion` property in the `pitest` configuration
closure.

```groovy
pitest {
    pitestVersion = '2.8.1-the.greatest.one'
}
```

In case of errors detected when the latest available version of the plugin is used with newer PIT version please raise an [issue](https://github.com/szpak/gradle-pitest-plugin/issues).

### How to disable placing PIT reports in time-based subfolders?

Placing PIT reports directly in `${PROJECT_DIR}/build/reports/pitest` can be enabled with `timestampedReports` configuration property:

```groovy
pitest {
    timestampedReports = false
}
```

### How can I debug a gradle-pitest-plugin execution or a PIT process execution itself in a Gradle build?

Occasionally, it may be useful to debug a gradle-pitest-plugin execution or a PIT execution itself (e.g. [NPE in PIT](https://github.com/hcoles/pitest/issues/345)) to provide sensible error report.

The gradle-pitest-plugin execution can be remotely debugged with adding `-Dorg.gradle.debug=true` to the command line.

However, as PIT is started as a separate process to debug its execution the following arguments need to be added to the plugin configuration:

```groovy
pitest {
    mainProcessJvmArgs = ['-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005']
}
```

### Can I use gradle-pitest-plugin with my Android application?

Short answer is: not directly. Due to some [incompatibilities](https://github.com/szpak/gradle-pitest-plugin/issues/31) between "standard" Java applications and Android Java applications in Gradle the plugin does not support the later. Luckily, there is an Android [fork](https://github.com/koral--/gradle-pitest-plugin/) of the plugin maintained by [Karol Wrótniak](https://github.com/koral--) which provides a modified version supporting Android applications (but on the other hand it doesn't work with standard Java applications).

### I have JUnit 5 plugin and the execution fails after migration to 1.5.0+, why?

gradle-pitest plugin [1.5.0](https://github.com/szpak/gradle-pitest-plugin/releases/tag/release%2F1.5.0) finally relaxed the way how (where) the `pitest` configuration has been placed ([#62](https://github.com/szpak/gradle-pitest-plugin/issues/62)) which also was generating deprecation warnings in Gradle 6+. This change is not backward compatible and as a result manual migration has to be made - see the [release notes](https://github.com/szpak/gradle-pitest-plugin/releases/tag/release%2F1.5.0). This affects only project with external custom plugins.

**Important**. As the JUnit 5 plugin for PIT is definitely the most popular, starting with 1.4.7 there is a simplified way how it could be configured with `junit5PluginVersion` (which is definitely **recommended**). See [my blog post](https://blog.solidsoft.pl/2020/02/27/pit-junit-5-and-gradle-with-just-one-extra-line-of-configuration/#modern-improved-approach-with-plugins-br-and-gradle-pitest-plugin-147) to find out how to migrate (it also solves the compatibility issue with 1.5.0+).


## Known issues

 - too verbose output from PIT (also see `verbosity` option introduced with PIT 1.7.1)

## Development

gradle-pitest-plugin cloned from the repository can be built using Gradle command:

    ./gradlew build

The easiest way to make a JAR with local changes visible in another project is to install it into the local Maven repository:

    ./gradlew install

There are also basic functional tests written using [nebula-test](https://github.com/nebula-plugins/nebula-test/) which can be run with:

    ./gradlew funcTest


## Support

[gradle-pitest-plugin](https://gradle-pitest-plugin.solidsoft.info/) has been written by Marcin Zajączkowski with a help from [contributors](https://github.com/szpak/gradle-pitest-plugin/graphs/contributors).
The author can be contacted directly via email: mszpak ATT wp DOTT pl.
There is also Marcin's blog available: [Solid Soft](https://blog.solidsoft.pl) - Working code is not enough.

The plugin surely has some bugs and missing features. They can be reported using an [issue tracker](https://github.com/szpak/gradle-pitest-plugin/issues).
However, it is often a better idea to send a questions to the [PIT mailing list](https://groups.google.com/group/pitusers) first.

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

<!-- Google Analytics -->
<script>
(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
})(window,document,'script','//www.google-analytics.com/analytics.js','ga');
ga('create', 'UA-46065645-1', 'solidsoft.info');
ga('send', 'pageview');
</script>
