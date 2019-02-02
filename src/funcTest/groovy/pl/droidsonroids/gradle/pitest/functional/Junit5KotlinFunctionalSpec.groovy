package pl.droidsonroids.gradle.pitest.functional

import nebula.test.functional.ExecutionResult

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
            result.standardOutput.contains('Generated 2 mutations Killed 2 (100%)')
    }
}
