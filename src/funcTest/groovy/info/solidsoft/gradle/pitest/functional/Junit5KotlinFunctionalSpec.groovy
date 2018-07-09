package info.solidsoft.gradle.pitest.functional

import nebula.test.functional.ExecutionResult
import org.junit.Ignore

@Ignore("pitest-junit5 not compatible with Android: NoClassDefFoundError: org/junit/platform/engine/support/filter/ExclusionReasonConsumingFilter")
class Junit5KotlinFunctionalSpec extends AbstractPitestFunctionalSpec {

    def "should work with kotlin and junit5"() {
        given:
            copyResources("testProjects/junit5", "")
        and:
            writeManifestFile()
        when:
            ExecutionResult result = runTasksSuccessfully('pitestRelease')
        then:
            result.wasExecuted('pitestRelease')
            result.getStandardOutput().contains('Generated 2 mutations Killed 2 (100%)')
    }
}
