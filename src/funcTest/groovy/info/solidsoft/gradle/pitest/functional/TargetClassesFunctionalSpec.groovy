package info.solidsoft.gradle.pitest.functional

import groovy.transform.CompileDynamic
import nebula.test.functional.ExecutionResult

@CompileDynamic
class TargetClassesFunctionalSpec extends AbstractPitestFunctionalSpec {

    void "report error when no project group and no targetClasses parameter are defined"() {
        given:
            buildFile << """
                apply plugin: 'java'
                apply plugin: 'info.solidsoft.pitest'

                repositories {
                    mavenCentral()
                }
            """.stripIndent()
        and:
            writeHelloWorld('gradle.pitest.test.hello')
        when:
            ExecutionResult result = runTasksWithFailure('pitest')
        then:
            assertStdOutOrStdErrContainsGivenText(result, "property 'targetClasses' doesn't have a configured value")
    }

}
