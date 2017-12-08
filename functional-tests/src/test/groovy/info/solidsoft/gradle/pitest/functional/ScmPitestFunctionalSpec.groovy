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
                
                scmPitest {
                    scmRoot = "."
                    goal = "localChanges"
                    scm {
                        connection = "scm:git:git@github.com/spriadka/gradle-pitest-plugin"
                    }
                    connectionType = "connection"
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
