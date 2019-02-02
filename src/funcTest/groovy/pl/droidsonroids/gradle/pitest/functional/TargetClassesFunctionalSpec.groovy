package pl.droidsonroids.gradle.pitest.functional

import nebula.test.functional.ExecutionResult

class TargetClassesFunctionalSpec extends AbstractPitestFunctionalSpec {

    def "report error when no targetClasses parameter is defined"() {
        given:
            buildFile << """
                apply plugin: 'com.android.library'
                apply plugin: 'pl.droidsonroids.pitest'

                repositories {
                    google()
                }
                
                android {
                    compileSdkVersion 28
                    defaultConfig {
                        minSdkVersion 10
                        targetSdkVersion 28
                    }
                }
            """.stripIndent()
        and:
            writeHelloWorld('gradle.pitest.test.hello')
        when:
            ExecutionResult result = runTasksWithFailure('pitestRelease')
        then:
            assertStdOutOrStdErrContainsGivenText(result,"No value has been specified for property 'targetClasses'")
    }
}
