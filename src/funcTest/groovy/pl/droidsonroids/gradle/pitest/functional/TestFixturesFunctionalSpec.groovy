package pl.droidsonroids.gradle.pitest.functional

import groovy.transform.CompileDynamic
import nebula.test.functional.ExecutionResult

@CompileDynamic
class TestFixturesFunctionalSpec extends AbstractPitestFunctionalSpec {

    void "should work with java-test-fixtures plugin"() {
        given:
            copyResources("testProjects/testFixtures", "")
        when:
            ExecutionResult result = runTasks('pitestRelease')
        then:
            !result.standardError.contains("Build failed with an exception")
            !result.failure
            result.wasExecuted('pitestRelease')
            result.standardOutput.contains('Generated 1 mutations Killed 1 (100%)')
    }

}
