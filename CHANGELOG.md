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
