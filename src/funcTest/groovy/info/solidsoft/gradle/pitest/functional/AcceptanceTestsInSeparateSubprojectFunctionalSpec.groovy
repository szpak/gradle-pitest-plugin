package info.solidsoft.gradle.pitest.functional

import nebula.test.functional.ExecutionResult

class AcceptanceTestsInSeparateSubprojectFunctionalSpec extends AbstractPitestFunctionalSpec {

    def "should mutate production code in another subproject"() {
        given:
            copyResources("testProjects/multiproject", "")
        when:
            ExecutionResult result = runTasksSuccessfully('pitest')
        then:
            result.wasExecuted(':itest:pitest')
            result.getStandardOutput().contains('Generated 4 mutations Killed 3 (75%)')
    }
}
