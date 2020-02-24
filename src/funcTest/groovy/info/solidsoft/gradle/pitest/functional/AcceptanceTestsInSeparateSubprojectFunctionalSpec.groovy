package info.solidsoft.gradle.pitest.functional

import nebula.test.functional.ExecutionResult
import spock.util.environment.RestoreSystemProperties

class AcceptanceTestsInSeparateSubprojectFunctionalSpec extends AbstractPitestFunctionalSpec {

    @RestoreSystemProperties
    def "should mutate production code in another subproject"() {
        given:
            copyResources("testProjects/multiproject", "")
        and:
            //Until fixed: https://github.com/szpak/gradle-pitest-plugin/issues/155
            System.setProperty("ignoreDeprecations", "true")
        when:
            ExecutionResult result = runTasksSuccessfully('pitest')
        then:
            result.wasExecuted(':itest:pitest')
            result.getStandardOutput().contains('Generated 4 mutations Killed 3 (75%)')
    }
}
