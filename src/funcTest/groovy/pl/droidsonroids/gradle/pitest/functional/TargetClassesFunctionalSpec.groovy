package pl.droidsonroids.gradle.pitest.functional

import groovy.transform.CompileDynamic
import nebula.test.functional.ExecutionResult

@CompileDynamic
class TargetClassesFunctionalSpec extends AbstractPitestFunctionalSpec {

    void "report error when no targetClasses parameter is defined"() {
        given:
        buildFile << """
                buildscript {
                    repositories {
                        google()
                        mavenCentral()
                    }
                }
                apply plugin: 'com.android.library'
                apply plugin: 'pl.droidsonroids.pitest'

                repositories {
                    google()
                    mavenCentral()
                }

                android {
                    compileSdkVersion 30
                    defaultConfig {
                        minSdkVersion 10
                        targetSdkVersion 30
                    }
                }
            """.stripIndent()
        and:
        writeHelloWorld('gradle.pitest.test.hello')
        when:
        ExecutionResult result = runTasks('pitestRelease')
        then:
        assertStdOutOrStdErrContainsGivenText(result, "Assign a value to 'targetClasses'.")
    }

}
