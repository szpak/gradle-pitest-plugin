package info.solidsoft.gradle.pitest.functional

import com.google.common.base.Predicate
import com.google.common.base.Predicates
import groovy.util.logging.Slf4j
import nebula.test.functional.GradleRunner
import spock.lang.Unroll

/**
 * TODO: Possible extensions:
 *  - Move functional tests to a separate sourceSet and not run them in every build - DONE
 *  - Add nice gradle.build builder
 *  - Add Connector clean up in tear down in IntegrationSpec - DONE
 *  - Add testing against latest nightly Gradle version?
 */
@Slf4j
class PitestPluginFunctional2Spec extends AbstractPitestFunctionalSpec {

    @Unroll
    def "should run mutation analysis with Gradle #requestedGradleVersion"() {
        given:
            gradleVersion = requestedGradleVersion
            classpathFilter = Predicates.and(GradleRunner.CLASSPATH_DEFAULT, FILTER_SPOCK_JAR)
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

    //To prevent failure when Spock for Groovy 2.4 is run with Groovy 2.3 delivered with Gradle <2.8
    //Spock is not needed in this artificial project - just the test classpath leaks to Gradle instance started by Nebula
    private static final Predicate<URL> FILTER_SPOCK_JAR = { URL url ->
        return !url.toExternalForm().contains("spock-core-1.0-groovy-2.4.jar")
    } as Predicate<URL>

    //TODO: Extract regression tests control mechanism to a separate class (or even better trait) when needed in some other place
    private static final String REGRESSION_TESTS_ENV_NAME = "PITEST_REGRESSION_TESTS"
    private static final List<String> GRADLE_LATEST_VERSIONS = ["2.14.1"]
    private static final Range<Integer> GRADLE2_MINOR_RANGE = (14..0)

    private static final Closure gradle2AdditionalVersionModifications = { List<String> versions ->
        versions - "2.2" + "2.2.1" - "2.14" + "2.14.1" + "3.0-rc-2"
    }

    private static List<String> resolveRequestedGradleVersions() {
        String regressionTestsLevel = System.getenv(REGRESSION_TESTS_ENV_NAME)
        log.debug("$REGRESSION_TESTS_ENV_NAME set to '${regressionTestsLevel}'")
        switch (regressionTestsLevel) {
            case "latestOnly":
            case null:
                GRADLE_LATEST_VERSIONS
                break
            case "quick":
                GRADLE_LATEST_VERSIONS + "2.0" + "3.0-rc-2"
                break
            case "full":
                gradle2AdditionalVersionModifications(GRADLE2_MINOR_RANGE.collect { "2.$it" })
                break
            default:
                log.warn("Unsupported $REGRESSION_TESTS_ENV_NAME value `$regressionTestsLevel` (expected 'latestOnly', 'quick' or 'full'). " +
                        "Assuming 'latest'.")
                GRADLE_LATEST_VERSIONS
        }
    }
}
