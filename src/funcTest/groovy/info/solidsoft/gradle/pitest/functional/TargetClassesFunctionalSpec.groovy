package info.solidsoft.gradle.pitest.functional

class TargetClassesFunctionalSpec extends AbstractPitestFunctionalSpec {

    def "report error when no project group and no targetClasses parameter are defined"() {
        given:
            buildFile << """
                apply plugin: 'com.android.library'
                apply plugin: 'pl.droidsonroids.pitest'

                android {
                    buildToolsVersion '25.0.2'
                    compileSdkVersion 25
                    defaultConfig {
                        minSdkVersion 10
                        targetSdkVersion 25
                    }
                }
            """.stripIndent()
        and:
            writeHelloWorld('gradle.pitest.test.hello')
        when:
            def result = runTasksWithFailure('pitestRelease')
        then:
            result.standardError.contains("No value has been specified for property 'targetClasses'")
    }
}
