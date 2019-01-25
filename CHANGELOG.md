# gradle-pitest-plugin changelog

## 1.4.0 - Unreleased

 - Basic Java 11 support verified with CI build (requires PIT 1.4.1)- [#86](https://github.com/szpak/CDeliveryBoy/issues/#86), [#81](https://github.com/szpak/CDeliveryBoy/issues/#81)
 - Improve `pitest` task caching with @Classpath - [#93](https://github.com/szpak/CDeliveryBoy/issues/#93)
 - PIT 1.4.3 by default
 - Switch build to Gradle 4.10.2
 - Enable automatic dependency bumping with [Dependabot](https://dependabot.com/)

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
