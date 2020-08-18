# gradle-pitest-plugin changelog

## 1.5.2 - Unreleased

 - Support java-test-fixtures plugin - [#223](https://github.com/szpak/gradle-pitest-plugin/pull/223) - PR by [Piotr Kubowicz](https://github.com/pkubowicz)
 - PIT 1.5.2 by default
 - Add functional test with Spock 2 (using JUnit Platform)
 - Bump minimal supported PIT version to 1.4.0 - the first version which required Java 8 (May 2018)

## 1.5.1 - 2020-05-06

 - Fail with meaningful error message on no longer supported `pitest` configuration in `rootProject.buildscript` - [#205](https://github.com/szpak/gradle-pitest-plugin/issues/205)

The only change helps people not reading the release notes to get know why their projects with the JUnit 5 PIT plugin stopped working after migration to 1.5.0+.

## 1.5.0 - 2020-04-28

 - Move `pitest` configuration from root project to current project to eliminate Gradle 6+ warning - [#62](https://github.com/szpak/gradle-pitest-plugin/issues/62)
 - Upgrade Gradle wrapper to 6.3 (ability to build with Java 14)

**Compatibility changes**. This version finally relaxes the need to create `pitest` configuration in the root project. This was problematic especially with Android projects and also started to generate deprecation warnings in Gradle 6.

The migration steps are required only in project manually adding custom PIT plugins. For example:

```groovy
buildscript {   //only in gradle-pitest-plugin <1.5.0
    //...
    configurations.maybeCreate('pitest')
    dependencies {
        pitest 'org.example.pit.plugins:pitest-custom-plugin:0.42'
    }
}

pitest {
    testPlugin = 'custom'
    //...
}
```

should be replaced with:

```groovy
//only in gradle-pitest-plugin 1.5.0+

//in project (not buildscript) dependencies and without needto create "pitest" configuration manually
dependencies {
    pitest 'org.example.pit.plugins:pitest-custom-plugin:0.42'
}

pitest {
    testPlugin = 'custom'
    //...
}
```

Please also note that the users of the new JUnit 5 PIT plugin [configuration mechanism](https://blog.solidsoft.pl/2020/02/27/pit-junit-5-and-gradle-with-just-one-extra-line-of-configuration/#modern-improved-approach-with-plugins-br-and-gradle-pitest-plugin-147) with `junit5PluginVersion` are not affected.

## 1.4.9 - 2020-04-22

 - Fix regression in 1.4.8 related to missing source listing in PIT reports - [#198](https://github.com/szpak/gradle-pitest-plugin/issues/198)
 - migrate remaining configuration properties to [Lazy Configuration API](https://docs.gradle.org/current/userguide/lazy_configuration.html)
 - more corner cases tested

## 1.4.8 - 2020-04-01

 - Fix problem with setting `historyInputLocation` and `jvmPath` - [#189](https://github.com/szpak/gradle-pitest-plugin/issues/189)
 - Less noisy "Adding dependency" logging - [#182](https://github.com/szpak/gradle-pitest-plugin/issues/182)
 - PIT 1.5.1 by default (better Java 14 support)
 - Basic functional testing with Java 14
 - Validation with Java Gradle plugin - [#106](https://github.com/szpak/gradle-pitest-plugin/issues/106) - PR by [Matthew Haughton](https://github.com/3flex)
 - CodeNarc code checks - [#184](https://github.com/szpak/gradle-pitest-plugin/pull/184) - PR by [Matthew Haughton](https://github.com/3flex)

## 1.4.7 - 2020-02-27

 - Simpler usage with JUnit 5 with `junit5PluginVersion` configuration parameter - [#177](https://github.com/szpak/gradle-pitest-plugin/issues/177) - idea by [John Scancella](https://github.com/jscancella)
 - Remove deprecation warnings in Gradle 6 - [#155](https://github.com/gradle/gradle/issues/155)
 - `additionalClasspathFile` no longer marked as `@OutputFile` for better Gradle 6+ compatibility
 - PIT 1.5.0 by default
 - Bump minimal Gradle version to 5.6 - required to fix deprecation warnings due to [#10953](https://github.com/gradle/gradle/issues/10953)
 - Upgrade Gradle wrapper to 5.6.4

Starting with this release the configuration required to use PIT with JUnit 5 has been simplified to the following:

```
plugins {
    id 'java'
    id 'info.solidsoft.pitest' version '...'
}

pitest {
    //adds dependency to org.pitest:pitest-junit5-plugin and sets "testPlugin" to "junit5"
    junit5PluginVersion = '0.12'
}
```

See this [blog post](https://blog.solidsoft.pl/2020/02/27/pit-junit-5-and-gradle-with-just-one-extra-line-of-configuration/) for more details.

**Compatibility changes**. A new set of the configuration parameters (`jvmPath`, `historyInputLocation` and `historyOutputLocation`) has been converted from `File` to `RegularFileProperty` to prevent deprecation warnings in Gradle 6+. Gradle itself should handle those changes for configuration in `build.gradle`, however, there could be some corner cases if those fields were used directly.


## 1.4.6 - 2019-12-15

 - Support for includedTestMethods (PIT 1.3.2) - [#79](https://github.com/szpak/gradle-pitest-plugin/issues/79)
 - Support for useClasspathJar (PIT 1.4.2) - [#92](https://github.com/szpak/gradle-pitest-plugin/issues/92)
 - Support for skipFailingTests (PIT 1.4.4) - [#113](https://github.com/szpak/gradle-pitest-plugin/issues/113)
 - Pass additional PIT features from command line with `--additionalFeatures` - [#139](https://github.com/szpak/gradle-pitest-plugin/issues/139)
 - Set `targetTests` explicitly - [#144](https://github.com/szpak/gradle-pitest-plugin/issues/144)
 - Ability to override `targetTests` from command line `--targetTests` - [#143](https://github.com/szpak/gradle-pitest-plugin/issues/143)
 - Run `pitest` after `test` if both scheduled to run - [#141](https://github.com/szpak/gradle-pitest-plugin/pull/141) - PR by [Björn Kautler](https://github.com/Vampire)
 - Remove incubating `addFileExtensionsToFilter()` method added in 1.4.5 - suggestion by [Björn Kautler](https://github.com/Vampire)
 - Travis build with OpenJ9 11 - [#112](https://github.com/szpak/gradle-pitest-plugin/issues/112)

**Compatibility changes**. The incubating `addFileExtensionsToFilter()` method added in 1.4.5 was removed as it is possible to achieve the same effect
(while waiting for [improvement in Gradle](https://github.com/gradle/gradle/issues/10475)) with:

```
pitest {
    fileExtensionsToFilter.addAll('xml', 'orbit')
}
```

## 1.4.5 - 2019-09-08

 - Rework internal plugin implementation to Gradle 5+ standards
 - PIT 1.4.10 by default
 - Basic Java 12 support tested by CI build
 - Move `pitest` task to `verification` group - [#136](https://github.com/szpak/gradle-pitest-plugin/issues/136) - PR by [Björn Kautler](https://github.com/Vampire)
 - Remove deprecation warnings in Gradle 6.0
 - Bump minimal Gradle version to 5.1
 - Meaningful error message on running unsupported Gradle version
 - Switch build to Gradle 5.6.1

**Known limitations**. This is a technical release to cope with the changes in Gradle 5 and 6. PIT 1.4.10 is used by default, but not all
new features of PIT 1.4.0+ have been implemented yet.

**Breaking changes**. This release changes internal implementation of the plugin configuration. The Gradle team worked hard to keep it as compatible
as possible, but not everything is supported. The new syntax can be [required](https://github.com/gradle/gradle/issues/10475) for adding elements to
the file extensions to filter (`fileExtensionsToFilter`):

```
pitest {
    addFileExtensionsToFilter(['xml', 'orbit'])
}
```
There could be also some issues for people interacting with the plugin from custom code (instead of from `build.gradle`). As a side effect Gradle 5.1
(released in I 2019) is required.

## 1.4.0 - 2019-01-26

 - Basic Java 11 support verified with CI build (requires PIT 1.4.1+)- [#86](https://github.com/gradle/gradle/issues/86), [#81](hhttps://github.com/gradle/gradle/issues/81)
 - Improve `pitest` task caching with @Classpath - [#93](https://github.com/gradle/gradle/issues/93)
 - PIT 1.4.3 by default
 - Switch build to Gradle 4.10.2
 - Remove support for Gradle <4.0 (for better Gradle 5 compatibility)
 - Remove deprecation warnings in Gradle 5.0
 - Enable automatic dependency bumping with [Dependabot](https://dependabot.com/)

**Known limitations**. To reduce confusion on Java 11 support, this version provides PIT 1.4.3 by default which supports Java 11. However, not all
new features of PIT 1.4.0 to 1.4.3 have been implemented in this release of the Gradle plugin. They are planned to be added in the future versions.

## 1.3.0 - 2018-01-27

 - Support for test plugin selection in PIT (e.g. JUnit 5) - #76 - PR by [Christoph Sturm](https://github.com/christophsturm)
 - Support for excludedTestClasses parameter in PIT - #75
 - PIT 1.3.1 by default
 - Drop Java 7 support - #70
 - Basic Java 9 compatibility verified with CI build (preliminary support for Java 9 has been available since PIT 1.2.3) - #68
 - Switch build to Gradle 4.5

**Breaking change**. Starting with 1.3.0 binary artifacts require at least Java 8.

## 1.2.4 - 2017-11-14

 - Make dependency exclusion on classpath configurable - #53
 - PIT 1.2.4 by default
 - Switch build to Gradle 4.3.1 - nicer executed tasks displaying with `--console=verbose`

## 1.2.3 - Skipped

## 1.2.2 - 2017-09-06

 - ClassPathFile should not use exposed in configuration - [#56](https://github.com/szpak/gradle-pitest-plugin/issues/56)
 - Support for PIT features configuration - [#65](https://github.com/szpak/gradle-pitest-plugin/issues/65)
 - PIT 1.2.2 by default
 - Switch releasing to Continuous Delivery with [CDeliveryBoy](https://travis-ci.org/szpak/CDeliveryBoy) - [#66](https://github.com/szpak/gradle-pitest-plugin/issues/66)
 - Automatic `CHANGELOG.md` synchronization with GitHub releases - [#61](https://github.com/szpak/gradle-pitest-plugin/issues/61)
 - Improve functional tests reliability
 - Switch build to Gradle 4.1 (should be still compatible with 2.0+) - [#64](https://github.com/szpak/gradle-pitest-plugin/issues/64)
 - Remove ugly deprecation warning in Gradle 4.0+
 - Make contribution for developers using Windows easier - AppVeyor CI build - [#58](https://github.com/szpak/gradle-pitest-plugin/issues/58)

## 1.2.1 - Skipped

## 1.2.0 - Skipped

## 1.1.11 - 2017-01-30

 - support for new `classPathFile` parameter in PIT 1.1.11 - #50
 - filter dynamic libraries - #52
 - PIT 1.1.11 by default
 - downgrade Gradle wrapper to 2.13 due to performance [regression](https://discuss.gradle.org/t/performance-regression-in-projectbuilder-in-2-14-and-3-0/18956)
   in tests with ProjectBuilder

## 1.1.10 - 2016-08-15

 - support for the new `maxSurviving` parameter in PIT 1.1.10 - #45
 - add `withHistory` alias (new thing in PIT Maven plugin)  for long established `enableDefaultIncrementalAnalysis` parameter in Gradle plugin
 - PIT 1.1.10 by default - #49
 - bring full Gradle 2.0 - 2.14.1 range of functional tests back (with custom changes to nebula-test)
 - upgrade Gradle wrapper to 2.14.1 - #48

## 1.1.9 - 2016-02-15

 - PIT 1.1.9 by default - fixed process hangs in 1.1.6 - #39

## 1.1.6 - 2015-09-08

 - support for plugin configuration parameters - #29
 - remove project file configuration functionality from the plugin (already removed upstream) - #28
 - PIT 1.1.6 by default
 - simplify internal release process and switch to Gradle 2.x - minimal required Gradle version is 2.0 - #30

## 1.1.5 - SKIPPED

## 1.1.4 - 2015-02-12

 - official support for PIT plugins - #17
 - support for integration tests in separate modules - #25
 - PIT and its runtime dependencies no longer put on additional classPath (with tests and code to mutate)
 - PIT 1.1.4 by default

## 1.1.2 - 2014-12-26

 - better workaround for issue with not applied Java plugin when using new Gradle plugin mechanism - #21
 - PIT 1.1.3 by default

## 1.1.1 - 2014-10-10

 - fix incompatibility with the new Gradle plugin mechanism - #21

## 1.1.0 - 2014-10-08

 - change plugin id to 'info.solidsoft.pitest' to be complaint with [Gradle plugin portal](http://plugins.gradle.org/) [**breaking change**]
 - make plugin available through [Gradle plugin portal](http://plugins.gradle.org/) - #19
 - add base automatic functional tests - #22
 - make it easier to manually override PIT dependencies - #20
 - PIT 1.1.0 by default

## 1.0.0 - 2014-05-21

 - follow new PIT version numbering scheme
 - fix: pitest task fails on dependencies with parent packaged as pom - #15
 - fix: wrong includedGroups/excludedGroups parameter names - #13 (contributed by @gvsmirnov)
 - move PIT dependencies from project scope to rootProject.buildscript scope - #12
 - adjust to PIT 1.0.0
 - upgrade Gradle Wrapper to 1.12

## 0.33.0 - 2014-04-25

 - fix: broken PIT version override mechanism with configuration parameter - #7
 - PIT is started as separate Java process with its own classpath - new `mainProcessJvmArgs` configuration parameter
 - adjust to PIT 0.33
 - change `jvmArgs` configuration parameter type from `String` to `List<String>` [potential breaking change]
 - upgrade Gradle Wrapper to 1.11

## 0.32.0 - 2013-12-28

 - adjust to PIT 0.32
 - break compatibility with PIT <0.32 (due to internal refactoring in PIT)
 - make targetClass configuration parameter optional when project group is defined - #5
 - remove deprecation warnings - minimal required Gradle version is 1.6
 - less verbose debug logging
 - upgrade Gradle Wrapper to 1.10

## 0.31.0 - SKIPPED

## 0.30.1 - 2013-08-04

 - fix: task no longer always up-to-date for empty java source directory set - #2
 - add support for additional test source sets - #3
 - add support for additional main source sets
 - remove `sourceDirs` configuration property - `mainSourceSets` should be used instead [potential breaking change]
 - add dynamic task dependencies based on selected test source sets
 - better interoperability with Windows
 - upgrade Gradle Wrapper version to 1.6

## 0.30.0 - 2013-05-10

 - adjust to PIT 0.30
 - artifacts availability in Maven Central Repository

## 0.29.0 - 2012-11-15

 - adjust to PIT 0.29
 - add default mode for incrementalAnalysis
 - add Gradle Wrapper

## 0.28.0 - 2012-08-14

 - initial release
