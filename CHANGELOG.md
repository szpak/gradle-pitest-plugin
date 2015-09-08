1.1.6 - 2015-08-??

 - support for plugin configuration parameters - #29
 - remove project file configuration functionality from the plugin (already removed upstream) - #28
 - PIT 1.1.6 by default
 - simplify internal release process and switch to Gradle 2.x - minimal required Gradle version is 2.0 - #30

1.1.5 - SKIPPED

1.1.4 - 2015-02-12

 - official support for PIT plugins - #17
 - support for integration tests in separate modules - #25
 - PIT and its runtime dependencies no longer put on additional classPath (with tests and code to mutate)
 - PIT 1.1.4 by default

1.1.2 - 2014-12-26

 - better workaround for issue with not applied Java plugin when using new Gradle plugin mechanism - #21
 - PIT 1.1.3 by default

1.1.1 - 2014-10-10

 - fix incompatibility with the new Gradle plugin mechanism - #21

1.1.0 - 2014-10-08

 - change plugin id to 'info.solidsoft.pitest' to be complaint with [Gradle plugin portal](http://plugins.gradle.org/) [**breaking change**]
 - make plugin available through [Gradle plugin portal](http://plugins.gradle.org/) - #19
 - add base automatic functional tests - #22
 - make it easier to manually override PIT dependencies - #20
 - PIT 1.1.0 by default

1.0.0 - 2014-05-21

 - follow new PIT version numbering scheme
 - fix: pitest task fails on dependencies with parent packaged as pom - #15
 - fix: wrong includedGroups/excludedGroups parameter names - #13 (contributed by @gvsmirnov)
 - move PIT dependencies from project scope to rootProject.buildscript scope - #12
 - adjust to PIT 1.0.0
 - upgrade Gradle Wrapper to 1.12

0.33.0 - 2014-04-25

 - fix: broken PIT version override mechanism with configuration parameter - #7
 - PIT is started as separate Java process with its own classpath - new `mainProcessJvmArgs` configuration parameter
 - adjust to PIT 0.33
 - change `jvmArgs` configuration parameter type from `String` to `List<String>` [potential breaking change]
 - upgrade Gradle Wrapper to 1.11

0.32.0 - 2013-12-28

 - adjust to PIT 0.32
 - break compatibility with PIT <0.32 (due to internal refactoring in PIT)
 - make targetClass configuration parameter optional when project group is defined - #5
 - remove deprecation warnings - minimal required Gradle version is 1.6
 - less verbose debug logging
 - upgrade Gradle Wrapper to 1.10

0.31.0 - SKIPPED

0.30.1 - 2013-08-04

 - fix: task no longer always up-to-date for empty java source directory set - #2
 - add support for additional test source sets - #3
 - add support for additional main source sets
 - remove `sourceDirs` configuration property - `mainSourceSets` should be used instead [potential breaking change]
 - add dynamic task dependencies based on selected test source sets
 - better interoperability with Windows
 - upgrade Gradle Wrapper version to 1.6

0.30.0 - 2013-05-10

 - adjust to PIT 0.30
 - artifacts availability in Maven Central Repository

0.29.0 - 2012-11-15

 - adjust to PIT 0.29
 - add default mode for incrementalAnalysis
 - add Gradle Wrapper

0.28.0 - 2012-08-14

 - initial release
