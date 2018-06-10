package info.solidsoft.gradle.pitest.functional

import nebula.test.functional.ExecutionResult
import spock.lang.Ignore

class MockableAndroidJarFunctionalSpec extends AbstractPitestFunctionalSpec {

    def "should mutate production code using mockable Android JAR"() {
        given:
            copyResources("testProjects/mockableAndroidJar", "")
        when:
            ExecutionResult result = runTasksSuccessfully('pitestRelease')
        then:
            result.wasExecuted('pitestRelease')
            result.getStandardOutput().contains('Generated 1 mutations Killed 1 (100%)')
    }
}
