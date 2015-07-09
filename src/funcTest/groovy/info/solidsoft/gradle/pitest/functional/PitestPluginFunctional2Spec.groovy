package info.solidsoft.gradle.pitest.functional

import groovy.util.logging.Slf4j
import spock.lang.Unroll

/**
 * TODO: Possible extensions:
 *  - Move functional tests to a separate sourceSet and not run them in every build - DONE
 *  - Add nice gradle.build builder
 *  - Add Connector clean up in tear down in IntegrationSpec
 *  - Add testing against latest nightly Gradle version?
 *
 *  - Allow to test with Gradle 2.x a plugin built with Gradle 1.x - classpath problem - https://github.com/nebula-plugins/nebula-test/issues/13 - ugly hacked locally
 */
@Slf4j
class PitestPluginFunctional2Spec extends AbstractPitestFunctionalSpec {

    @Unroll
    def "should run mutation analysis with Gradle #requestedGradleVersion"() {
        given:
            gradleVersion = requestedGradleVersion
        when:
            copyResources("testProjects/simple1", "")
        then:
            fileExists('build.gradle')
        when:
            def result = runTasksSuccessfully('pitest')
        then:
            result.wasExecuted(':pitest')
            result.getStandardOutput().contains('Generated 1 mutations Killed 1 (100%)')
        where:
            requestedGradleVersion << resolveRequestedGradleVersions()
    }

    //TODO: Extract regression tests control mechanism to a separate class (or even better trait) when needed in some other place
    private static final String REGRESSION_TESTS_ENV_NAME = "PITEST_REGRESSION_TESTS"
    private static final List<String> GRADLE_LATEST_VERSIONS = ["2.5"]
    private static final Range<Integer> GRADLE2_MINOR_RANGE = (5..0)

    private static def resolveRequestedGradleVersions() {
        String regressionTestsLevel = System.getenv(REGRESSION_TESTS_ENV_NAME)
        log.debug("$REGRESSION_TESTS_ENV_NAME set to '${regressionTestsLevel}'")
        switch (regressionTestsLevel) {
            case "latestOnly":
            case null:
                GRADLE_LATEST_VERSIONS
                break
            case "quick":
                GRADLE_LATEST_VERSIONS + ["2.0"]
                break
            case "full":
                GRADLE2_MINOR_RANGE.collect { "2.$it" } - ["2.2"] + ["2.2.1"]
                break
            default:
                log.warn("Unsupported $REGRESSION_TESTS_ENV_NAME value `$regressionTestsLevel` (expected 'latestOnly', 'quick' or 'full'). " +
                        "Assuming 'latest'.")
                GRADLE_LATEST_VERSIONS
        }
    }
}
