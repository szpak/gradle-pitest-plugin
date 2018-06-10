package info.solidsoft.gradle.pitest.functional

class TargetClassesFunctionalSpec extends AbstractPitestFunctionalSpec {

    def "report error when no project group and no targetClasses parameter are defined"() {
        given:
            buildFile << """
                apply plugin: 'com.android.library'
                apply plugin: 'pl.droidsonroids.pitest'

                repositories {
                    google()
                }
                
                android {
                    compileSdkVersion 27
                    defaultConfig {
                        minSdkVersion 10
                        targetSdkVersion 27
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
