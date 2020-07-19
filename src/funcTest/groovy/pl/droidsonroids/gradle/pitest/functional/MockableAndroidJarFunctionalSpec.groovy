package pl.droidsonroids.gradle.pitest.functional

import nebula.test.functional.ExecutionResult

class MockableAndroidJarFunctionalSpec extends AbstractPitestFunctionalSpec {

    def "should mutate production code using mockable Android JAR"() {
        given:
            buildFile << """
                buildscript {
                    repositories {
                        google()
                        jcenter()
                    }
                    dependencies {
                        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72"
                        classpath 'com.android.tools.build:gradle:4.0.1'
                    }
                }
                
                apply plugin: 'pl.droidsonroids.pitest'
                apply plugin: 'com.android.application'
                
                android {
                    compileSdkVersion 29
                    defaultConfig {
                        minSdkVersion 10
                        targetSdkVersion 29
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
    }
}
