package pl.droidsonroids.gradle.pitest.functional

import nebula.test.functional.ExecutionResult
import org.gradle.api.GradleException
import spock.lang.PendingFeature
import spock.util.environment.RestoreSystemProperties

class AcceptanceTestsInSeparateSubprojectFunctionalSpec extends AbstractPitestFunctionalSpec {

    @RestoreSystemProperties
    @PendingFeature(exceptions = GradleException, reason = "To investigate")
    def "should mutate production code in another subproject"() {
        given:
            copyResources("testProjects/multiproject", "")
        and:
            //For Gradle 6+, until fixed: https://github.com/szpak/gradle-pitest-plugin/issues/62
            System.setProperty("ignoreDeprecations", "true")
        when:
            ExecutionResult result = runTasksSuccessfully(':itest:pitestRelease')
        then:
            result.wasExecuted(':itest:pitestRelease')
            result.standardOutput.contains('Generated 2 mutations Killed 2 (100%)')
    }
}
