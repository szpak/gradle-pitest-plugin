package info.solidsoft.gradle.pitest.functional

import groovy.transform.CompileDynamic
import nebula.test.functional.ExecutionResult
import spock.util.environment.RestoreSystemProperties

@CompileDynamic
class AcceptanceTestsInSeparateSubprojectFunctionalSpec extends AbstractPitestFunctionalSpec {

    @RestoreSystemProperties
    void "should mutate production code in another subproject"() {
        given:
            copyResources("testProjects/multiproject", "")
        and:
            //For Gradle 6+, until fixed: https://github.com/szpak/gradle-pitest-plugin/issues/62
            System.setProperty("ignoreDeprecations", "true")
        when:
            ExecutionResult result = runTasksSuccessfully('pitest')
        then:
            result.wasExecuted(':itest:pitest')
            result.getStandardOutput().contains('Generated 4 mutations Killed 3 (75%)')
    }

}
