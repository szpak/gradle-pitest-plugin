package info.solidsoft.gradle.pitest.functional

import nebula.test.functional.ExecutionResult

class AcceptanceTestsInSeparateSubprojectFunctionalSpec extends AbstractPitestFunctionalSpec {

    def "should mutate production code in another subproject"() {
        given:
            buildFile << """
                apply plugin: 'com.android.library'
                apply plugin: 'pl.droidsonroids.pitest'

                android {
                    buildToolsVersion '26.0.1'
                    compileSdkVersion 26
                    defaultConfig {
                        minSdkVersion 10
                        targetSdkVersion 26
                    }
                }
                buildscript {
                    repositories { mavenCentral() }
                }
                pitest {
                    targetClasses = ['**']
                }
            """
            copyResources("testProjects/multiproject", "")
        when:
            ExecutionResult result = runTasksSuccessfully('pitestRelease')
        then:
            result.wasExecuted(':itest:pitestRelease')
            result.getStandardOutput().contains('Generated 2 mutations Killed 2 (100%)')
            result.getStandardOutput().contains('Generated 2 mutations Killed 1 (50%)')
    }
}
