package info.solidsoft.gradle.pitest.functional

import nebula.test.functional.ExecutionResult

class RobolectricFunctionalSpec extends AbstractPitestFunctionalSpec {

    def "should not fail with tests using Robolectric"() {
        given:
            copyResources("testProjects/robolectric", "")
        when:
            ExecutionResult result = runTasks('pitestRelease')
        then:
            result.wasExecuted('pitestRelease')
            result.standardOutput.contains('Generated 3 Killed 0 (0%)')
    }
}
