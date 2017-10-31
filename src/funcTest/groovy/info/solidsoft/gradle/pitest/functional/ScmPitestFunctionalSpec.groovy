package info.solidsoft.gradle.pitest.functional

import org.gradle.api.GradleException

class ScmPitestFunctionalSpec extends AbstractPitestFunctionalSpec {

    def "should throw exception if no '#missingProperty' is specified" () {
        given:
            buildFile << """
                apply plugin: 'info.solidsoft.pitest'
                group = 'gradle.pitest.test'

                repositories {
                    mavenCentral()
                }
                buildscript {
                    repositories {
                        mavenCentral()
                    }
                }
                dependencies {
                    testCompile 'junit:junit:4.12'
                }
        """.stripIndent()
        when:
            runTasksSuccessfully("scmPitest")
        then:
            thrown(GradleException)
        where:
            missingProperty << ['scmRoot','goal','scm','connectionType']
    }

}
