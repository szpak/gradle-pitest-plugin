package pl.droidsonroids.gradle.pitest.functional

import nebula.test.functional.ExecutionResult

class AcceptanceTestsInSeparateSubprojectFunctionalSpec extends AbstractPitestFunctionalSpec {

    def "should mutate production code in another subproject"() {
        given:
            copyResources("testProjects/multiproject", "")
        when:
            ExecutionResult result = runTasksSuccessfully(':itest:pitestRelease')
        then:
            result.wasExecuted(':itest:pitestRelease')
            result.standardOutput.contains('Generated 2 mutations Killed 2 (100%)')
    }
}
