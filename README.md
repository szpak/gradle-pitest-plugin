# Experimental Gradle plugin for PIT Mutation Testing in Android projects
This is a fork of [gradle-pitest-plugin](https://github.com/szpak/gradle-pitest-plugin)
which supports Android gradle projects. 

#Applying plugin
`build.gradle`:
```groovy
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath 'pl.droidsonroids.gradle:gradle-pitest-plugin:0.0.3'
  }
}

apply plugin: 'com.android.application'
//or apply plugin: 'com.android.library'
//or apply plugin: 'com.android.test'

apply plugin: 'pl.droidsonroids.pitest'
```
This plugin has to be applied _after_ android plugins.

Plugin is also available on [Gradle plugin portal](https://plugins.gradle.org/plugin/pl.droidsonroids.pitest). Note that only classic plugin applying syntax is supported since plugin has to be applied last:
```groovy
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.pl.droidsonroids.gradle:gradle-pitest-plugin:0.0.2"
  }
}

//apply android plugin
apply plugin: "pl.droidsonroids.pitest"
```

#Usage
`pitest<variant>` tasks will be created for each build variant 
(eg. `pitestProDebug` for `pro` product flavor and `debug` build type).
Additionally `pitest` task will run tasks for all variants.

After the measurements a report created by PIT will be placed in `${PROJECT_DIR}/build/reports/pitest/<variant>` directory.

`pitest` configuration extension contains additionally `androidRuntimeDependency` property 
which defaults to `org.robolectric:android-all:6.0.0_r1-robolectric-0` but may be overridden.   

For more information see [README of source project](https://github.com/szpak/gradle-pitest-plugin/blob/master/README.md)
