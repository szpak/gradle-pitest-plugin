package info.solidsoft.gradle.pitest.functional

import groovy.transform.CompileDynamic
import nebula.test.functional.ExecutionResult

@CompileDynamic
class AcceptanceTestsInSeparateSubprojectFunctionalSpec extends AbstractPitestFunctionalSpec {

    void "should mutate production code in another subproject"() {
        given:
            copyResources("testProjects/multiproject", "")
        when:
            ExecutionResult result = runTasksSuccessfully('pitest')
        then:
            result.wasExecuted(':itest:pitest')
            result.getStandardOutput().contains('Generated 4 mutations Killed 3 (75%)')
    }

}
