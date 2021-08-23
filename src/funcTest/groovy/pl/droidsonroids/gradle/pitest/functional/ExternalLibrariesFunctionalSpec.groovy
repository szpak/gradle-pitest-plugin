package pl.droidsonroids.gradle.pitest.functional

import groovy.transform.CompileDynamic
import nebula.test.functional.ExecutionResult

@CompileDynamic
class ExternalLibrariesFunctionalSpec extends AbstractPitestFunctionalSpec {

    void "should work with kotlin and junit5"() {
        given:
        copyResources("testProjects/junit5kotlin", "")
        and:
        writeManifestFile()
        when:
        ExecutionResult result = runTasksSuccessfully('pitestRelease')
        then:
        result.wasExecuted('pitestRelease')
        result.standardOutput.contains('Generated 2 mutations Killed 2 (100%)')
    }

    void "should not fail with tests using Robolectric"() {
        given:
        copyResources("testProjects/robolectric", "")
        when:
        ExecutionResult result = runTasksSuccessfully('pitestRelease')
        then:
        result.wasExecuted('pitestRelease')
        result.standardOutput.contains('Generated 3 Killed 0 (0%)')
    }

    void "should not fail with tests using mockk"() {
        given:
        copyResources("testProjects/mockk", "")
        when:
        ExecutionResult result = runTasksSuccessfully('pitestRelease')
        then:
        result.wasExecuted('pitestRelease')
        result.standardOutput.contains('Generated 5 mutations Killed 0 (0%)')
    }

    void "should work with Spock 2 using JUnit 5 internally"() {
        given:
        copyResources("testProjects/junit5spock2", "")
        when:
        ExecutionResult result = runTasksSuccessfully('pitest')
        then:
        result.wasExecuted('pitest')
        and:
        result.standardOutput.contains('--testPlugin=junit5')
        result.standardOutput.contains('Generated 1 mutations Killed 1 (100%)')
    }

}
