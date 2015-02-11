package info.solidsoft.gradle.pitest.functional

import nebula.test.IntegrationSpec

class AcceptanceTestsInSeparateSubprojectFunctionalSpec extends IntegrationSpec {

    def "should mutate production code in another subproject"() {
        given:
            copyResources("testProjects/multiproject", "")
        when:
            def result = runTasksSuccessfully('pitest')
        then:
            result.wasExecuted(':itest:pitest')
            result.getStandardOutput().contains('Generated 4 mutations Killed 3 (75%)')
    }
}
