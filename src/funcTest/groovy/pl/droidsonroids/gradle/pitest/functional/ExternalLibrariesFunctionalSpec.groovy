package pl.droidsonroids.gradle.pitest.functional

import groovy.transform.CompileDynamic
import nebula.test.functional.ExecutionResult
import org.gradle.api.GradleException
import spock.lang.PendingFeature

@CompileDynamic
class ExternalLibrariesFunctionalSpec extends AbstractPitestFunctionalSpec {

    @PendingFeature(exceptions = GradleException, reason = "To investigate")
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
        result.standardOutput.contains('Generated 3 mutations Killed 0 (0%)')
    }

}
