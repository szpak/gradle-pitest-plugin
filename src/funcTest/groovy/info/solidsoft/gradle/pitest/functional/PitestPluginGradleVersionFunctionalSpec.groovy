package info.solidsoft.gradle.pitest.functional

import groovy.util.logging.Slf4j
import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult

@Slf4j
@SuppressWarnings("GrMethodMayBeStatic")
class PitestPluginGradleVersionFunctionalSpec extends IntegrationSpec {

    void setup() {
        fork = true //to make stdout assertion work with Gradle 2.x - http://forums.gradle.org/gradle/topics/unable-to-catch-stdout-stderr-when-using-tooling-api-i-gradle-2-x#reply_15357743
        memorySafeMode = true   //shutdown Daemon after a few seconds of inactivity
        daemonMaxIdleTimeInSecondsInMemorySafeMode = 1  //trying to mitigate "Gradle killed" issues with Travis
    }

    def "should run mutation analysis with Gradle #requestedGradleVersion"() {
        given:
            gradleVersion = requestedGradleVersion
        when:
            copyResources("testProjects/simple1", "")
        then:
            fileExists('build.gradle')
        when:
            ExecutionResult result = runTasksSuccessfully('pitest')
        then:
            result.wasExecuted(':pitest')
        where:
            requestedGradleVersion << ["4.10.2", "5.5.1", "5.6.1"]
    }
}
