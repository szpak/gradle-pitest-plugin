package pl.droidsonroids.gradle.pitest.functional

import spock.lang.Ignore
import nebula.test.functional.ExecutionResult

class RobolectricFunctionalSpec extends AbstractPitestFunctionalSpec {

    def "should not fail with tests using Robolectric"() {
        given:
            copyResources("testProjects/robolectric", "")
        when:
            ExecutionResult result = runTasksSuccessfully('pitestRelease')
        then:
            result.wasExecuted('pitestRelease')
            result.standardOutput.contains('Generated 3 Killed 0 (0%)')
    }
}
