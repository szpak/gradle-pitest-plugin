# Experimental Gradle plugin for PIT Mutation Testing in Android projects
This is a fork of [gradle-pitest-plugin](https://github.com/szpak/gradle-pitest-plugin)
which supports Android gradle projects. 

# Applying plugin in `build.gradle`
## With [Gradle plugin portal](https://plugins.gradle.org/plugin/pl.droidsonroids.pitest)

```groovy
plugins {
  id "pl.droidsonroids.pitest" version "0.2.0"
}
```

## With Maven central repository
```groovy
buildscript {
  repositories {
    mavenCentral()
    google()
    // If you're using a version of Gradle lower than 4.1, you must instead use:
    // maven {
    //     url 'https://maven.google.com'
    // }
    // An alternative URL is 'https://dl.google.com/dl/android/maven2/'    
  }
  dependencies {
    classpath 'pl.droidsonroids.gradle:gradle-pitest-plugin:0.2.0'
  }
}

apply plugin: 'com.android.application'
//or apply plugin: 'com.android.library'
//or apply plugin: 'com.android.test'

apply plugin: 'pl.droidsonroids.pitest'
```

# Usage
`pitest<variant>` tasks will be created for each build variant 
(eg. `pitestProDebug` for `pro` product flavor and `debug` build type).
Additionally `pitest` task will run tasks for all variants.

After the measurements a report created by PIT will be placed in `${PROJECT_DIR}/build/reports/pitest/<variant>` directory.

For more information see [README of source project](https://github.com/szpak/gradle-pitest-plugin/blob/master/README.md)

# <a name="troubleshooting"></a> Troubleshooting
## Tests fail when run under pitest but pass without it
Issue occurs when using [Android API](https://developer.android.com/reference/packages.html)
without mocking it.
Pitest verbose logs may list exceptions like `ExceptionInitializerError`.

The fastest solution is to set `android.testOptions.unitTests.returnDefaultValues = true`.
See [Local unit testing documentation](https://developer.android.com/training/testing/unit-testing/local-unit-tests.html#error-not-mocked)
to see other consequences of this change.
