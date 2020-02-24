package info.solidsoft.gradle.pitest.functional

import nebula.test.functional.ExecutionResult
import spock.lang.Issue

class Junit5FunctionalSpec extends AbstractPitestFunctionalSpec {

    def "should work with kotlin and junit5"() {
        given:
            copyResources("testProjects/junit5kotlin", "")
        when:
            ExecutionResult result = runTasksSuccessfully('pitest')
        then:
            result.wasExecuted('pitest')
            result.standardOutput.contains('Generated 2 mutations Killed 2 (100%)')
    }

    @Issue("https://github.com/szpak/gradle-pitest-plugin/issues/177")
    def "should work with junit5 - WIP"() {
        given:
            copyResources("testProjects/junit5simple", "")
        when:
            ExecutionResult result = runTasksSuccessfully('pitest')
        then:
            result.wasExecuted('pitest')
        and:
            result.standardOutput.contains('--testPlugin=junit5')
            result.standardOutput.contains('Generated 1 mutations Killed 1 (100%)')
    }
}
