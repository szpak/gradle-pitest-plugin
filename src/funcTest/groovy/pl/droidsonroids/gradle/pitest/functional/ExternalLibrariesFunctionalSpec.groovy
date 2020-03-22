package pl.droidsonroids.gradle.pitest.functional

import nebula.test.functional.ExecutionResult

class ExternalLibrariesFunctionalSpec extends AbstractPitestFunctionalSpec {

    def "should work with kotlin and junit5"() {
        given:
            copyResources("testProjects/junit5", "")
        and:
            writeManifestFile()
        when:
            ExecutionResult result = runTasksSuccessfully('pitestRelease')
        then:
            result.wasExecuted('pitestRelease')
            result.standardOutput.contains('Generated 2 mutations Killed 2 (100%)')
    }

    def "should not fail with tests using Robolectric"() {
        given:
        copyResources("testProjects/robolectric", "")
        when:
        ExecutionResult result = runTasksSuccessfully('pitestRelease')
        then:
        result.wasExecuted('pitestRelease')
        result.standardOutput.contains('Generated 3 Killed 0 (0%)')
    }

    def "should not fail with tests using mockk"() {
        given:
        copyResources("testProjects/mockk", "")
        when:
        ExecutionResult result = runTasksSuccessfully('pitestRelease')
        then:
        result.wasExecuted('pitestRelease')
        result.standardOutput.contains('Generated 3 mutations Killed 0 (0%)')
    }
}
