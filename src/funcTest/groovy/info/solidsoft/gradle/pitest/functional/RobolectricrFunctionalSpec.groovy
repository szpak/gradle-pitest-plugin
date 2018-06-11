package info.solidsoft.gradle.pitest.functional

import nebula.test.functional.ExecutionResult

class RobolectricrFunctionalSpec extends AbstractPitestFunctionalSpec {

    def "should not fail with tests using Robolectric"() {
        given:
        copyResources("testProjects/robolectric", "")
        when:
        ExecutionResult result = runTasksSuccessfully('pitestRelease')
        then:
        result.wasExecuted('pitestRelease')
        result.getStandardOutput().contains('Generated 3 Killed 0 (0%)')
    }
}
