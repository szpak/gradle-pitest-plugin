package info.solidsoft.gradle.pitest.functional

import com.google.common.base.Predicate
import com.google.common.base.Predicates
import groovy.util.logging.Slf4j
import nebula.test.functional.ExecutionResult
import nebula.test.functional.GradleRunner
import org.gradle.internal.jvm.Jvm
import org.gradle.util.GradleVersion

import java.util.regex.Pattern

/**
 * TODO: Possible extensions:
 *  - Move functional tests to a separate sourceSet and not run them in every build - DONE
 *  - Add nice gradle.build builder
 *  - Add Connector clean up in tear down in IntegrationSpec - DONE
 *  - Add testing against latest nightly Gradle version?
 */
@Slf4j
@SuppressWarnings("GrMethodMayBeStatic")
class PitestPluginGradleVersionFunctionalSpec extends AbstractPitestFunctionalSpec {

    //https://github.com/gradle/gradle/issues/2992#issuecomment-332869508
    private static final GradleVersion MINIMAL_STABLE_JAVA9_COMPATIBLE_GRADLE_VERSION = GradleVersion.version("4.2.1")
    //To do not fail on "NoSuchMethodError: sun.misc.Unsafe.defineClass()"
    private static final GradleVersion MINIMAL_STABLE_JAVA12_COMPATIBLE_GRADLE_VERSION = GradleVersion.version("4.8")
    //https://github.com/gradle/gradle/issues/8681#issuecomment-522951112
    private static final GradleVersion MINIMAL_STABLE_JAVA13_COMPATIBLE_GRADLE_VERSION = GradleVersion.version("6.0-20190902220030+0000")

    void setup() {
        daemonMaxIdleTimeInSecondsInMemorySafeMode = 1  //trying to mitigate "Gradle killed" issues with Travis
    }

    def "should run mutation analysis with Gradle #requestedGradleVersion"() {
        given:
            gradleVersion = requestedGradleVersion
            classpathFilter = Predicates.and(GradleRunner.CLASSPATH_DEFAULT, FILTER_SPOCK_JAR)
        when:
            copyResources("testProjects/simple1", "")
        then:
            fileExists('build.gradle')
        when:
            ExecutionResult result = runTasksSuccessfully('pitest')
        then:
            result.wasExecuted(':pitest')
            result.getStandardOutput().contains('Generated 1 mutations Killed 1 (100%)')
        where:
            requestedGradleVersion << applyJavaCompatibilityAdjustment(resolveRequestedGradleVersions()).unique()
    }

    //To prevent failure when Spock for Groovy 2.4 is run with Groovy 2.3 delivered with Gradle <2.8
    //Spock is not needed in this artificial project - just the test classpath leaks to Gradle instance started by Nebula
    private static final Pattern SPOCK_JAR_PATTERN = Pattern.compile(".*spock-core-1\\..*.jar")
    private static final Predicate<URL> FILTER_SPOCK_JAR = { URL url ->
        return !url.toExternalForm().matches(SPOCK_JAR_PATTERN)
    } as Predicate<URL>

    //TODO: Extract regression tests control mechanism to a separate class (or even better trait) when needed in some other place
    private static final String REGRESSION_TESTS_ENV_NAME = "PITEST_REGRESSION_TESTS"
    private static final List<String> GRADLE4_VERSIONS = ["4.10.2", "4.9", "4.8.1", "4.7", "4.6", "4.5", "4.4.1", "4.3.1", "4.2.1", "4.1", "4.0.1"]
    private static final List<String> GRADLE5_VERSIONS = ["5.6.1", "5.5.1", "5.4.1", "5.3.1", "5.2.1", "5.1.1", "5.0"]
    private static final List<String> GRADLE6_VERSIONS = [MINIMAL_STABLE_JAVA13_COMPATIBLE_GRADLE_VERSION.version] //for Java 13 compatibility
    private static final List<String> GRADLE_LATEST_VERSIONS = [GRADLE4_VERSIONS.first(), GRADLE5_VERSIONS.first(), GRADLE6_VERSIONS.first()]

    private List<String> resolveRequestedGradleVersions() {
        String regressionTestsLevel = System.getenv(REGRESSION_TESTS_ENV_NAME)
        log.debug("$REGRESSION_TESTS_ENV_NAME set to '${regressionTestsLevel}'")
        switch (regressionTestsLevel) {
            case "latestOnly":
            case null:
                return GRADLE_LATEST_VERSIONS
            case "quick":
                return GRADLE_LATEST_VERSIONS + GRADLE4_VERSIONS.last() + GRADLE5_VERSIONS.last()
            case "full":
                return GRADLE5_VERSIONS + GRADLE4_VERSIONS
            default:
                log.warn("Unsupported $REGRESSION_TESTS_ENV_NAME value '`$regressionTestsLevel`' (expected 'latestOnly', 'quick' or 'full'). " +
                        "Assuming 'latestOnly'.")
                return GRADLE_LATEST_VERSIONS
        }
    }

    //Jvm class from Spock doesn't work with Java 9 stable releases - otherwise @IgnoreIf could be used - TODO: check with Spock 1.2
    private List<String> applyJavaCompatibilityAdjustment(List<String> requestedGradleVersions) {
        if (!Jvm.current().javaVersion.isJava9Compatible()) {
            //All supported versions should be Java 8 compatible
            return requestedGradleVersions
        }
        GradleVersion minimalCompatibleGradleVersion = !Jvm.current().javaVersion.isJava10Compatible() ? MINIMAL_STABLE_JAVA9_COMPATIBLE_GRADLE_VERSION :
            (!(Integer.valueOf(Jvm.current().javaVersion.majorVersion) > 12) ? MINIMAL_STABLE_JAVA12_COMPATIBLE_GRADLE_VERSION :
                MINIMAL_STABLE_JAVA13_COMPATIBLE_GRADLE_VERSION)
        return leaveJavaXCompatibleGradleVersionsOnly(requestedGradleVersions, minimalCompatibleGradleVersion)
    }

    private List<String> leaveJavaXCompatibleGradleVersionsOnly(List<String> requestedGradleVersions, GradleVersion minimalCompatibleJavaVersion) {
        List<String> javaXCompatibleGradleVersions = requestedGradleVersions.findAll {
            GradleVersion.version(it) >= minimalCompatibleJavaVersion
        }
        if (javaXCompatibleGradleVersions.size() < 2) {
            javaXCompatibleGradleVersions.add(minimalCompatibleJavaVersion.version)
        }
        return javaXCompatibleGradleVersions
    }
}
