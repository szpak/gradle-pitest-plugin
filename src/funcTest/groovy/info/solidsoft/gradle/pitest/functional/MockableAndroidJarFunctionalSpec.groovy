package info.solidsoft.gradle.pitest.functional

import nebula.test.functional.ExecutionResult

class MockableAndroidJarFunctionalSpec extends AbstractPitestFunctionalSpec {

    def "should mutate production code using mockable Android JAR and AGP #requestedAndroidGradlePluginVersion"() {
        given:
            buildFile << """
                buildscript {
                    repositories {
                        google()
                        jcenter()
                    }
                    dependencies {
                        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.2.51"
                        classpath 'com.android.tools.build:gradle:$requestedAndroidGradlePluginVersion'
                    }
                }
                
                apply plugin: 'pl.droidsonroids.pitest'
                apply plugin: 'com.android.application'
                
                android {
                    compileSdkVersion 28
                    defaultConfig {
                        minSdkVersion 10
                        targetSdkVersion 28
                    }
                    testOptions {
                        unitTests.returnDefaultValues = true
                    }
                }
                
                buildscript {
                    repositories {
                        google()
                        mavenCentral()
                    }
                }
                
                repositories {
                    google()
                    mavenCentral()
                }
                
                group = "pitest.test"
                
                dependencies {
                    testImplementation 'junit:junit:4.12'
                }
                """.stripIndent()
        and:
            copyResources("testProjects/mockableAndroidJar", "")
        when:
            ExecutionResult result = runTasksSuccessfully('pitestRelease')
        then:
            result.wasExecuted('pitestRelease')
            result.standardOutput.contains('Generated 1 mutations Killed 1 (100%)')
        where:
            requestedAndroidGradlePluginVersion << resolveRequestedAndroidGradlePluginVersion()
    }
}
