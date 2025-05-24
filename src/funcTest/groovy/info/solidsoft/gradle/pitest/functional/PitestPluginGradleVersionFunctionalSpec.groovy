package info.solidsoft.gradle.pitest.functional

import groovy.transform.CompileDynamic
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import info.solidsoft.gradle.pitest.PitestPlugin
import nebula.test.functional.ExecutionResult
import nebula.test.functional.GradleRunner
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.internal.jvm.Jvm
import org.gradle.util.GradleVersion
import org.spockframework.runtime.extension.builtin.PreconditionContext
import spock.lang.IgnoreIf
import spock.util.Exceptions
import spock.util.environment.RestoreSystemProperties

import java.util.function.Predicate
import java.util.regex.Pattern

import static info.solidsoft.gradle.pitest.PitestTaskConfigurationSpec.PIT_PARAMETERS_NAMES_NOT_SET_BY_DEFAULT

/**
 * TODO: Possible extensions:
 *  - Move functional tests to a separate sourceSet and not run them in every build - DONE
 *  - Add nice gradle.build builder
 *  - Add Connector clean up in tear down in IntegrationSpec - DONE
 *  - Add testing against latest nightly Gradle version?
 */
@Slf4j
@SuppressWarnings("GrMethodMayBeStatic")
@CompileDynamic
class PitestPluginGradleVersionFunctionalSpec extends AbstractPitestFunctionalSpec {

    private static final GradleVersion LATEST_KNOWN_GRADLE_VERSION = GradleVersion.version("8.14.1")

    //Based on https://docs.gradle.org/current/userguide/compatibility.html
    private static final Map<JavaVersion, GradleVersion> MINIMAL_GRADLE_VERSION_FOR_JAVA_VERSION = [
        (JavaVersion.VERSION_15): GradleVersion.version("6.7"),
        (JavaVersion.VERSION_16): GradleVersion.version("7.0.2"),
        (JavaVersion.VERSION_17): GradleVersion.version("7.0.2"),   //TODO: 7.2? Determine once testing with Java 17 using toolchain is unlocked - https://github.com/szpak/gradle-pitest-plugin/issues/298
    ].withDefault { requestedVersion ->
        return requestedVersion < JavaVersion.VERSION_15
            ? PitestPlugin.MINIMAL_SUPPORTED_GRADLE_VERSION
            : LATEST_KNOWN_GRADLE_VERSION
    }

    void setup() {
        daemonMaxIdleTimeInSecondsInMemorySafeMode = 1  //trying to mitigate "Gradle killed" issues with Travis
    }

    @RestoreSystemProperties
    void "should run mutation analysis with Gradle #requestedGradleVersion"() {
        given:
            gradleVersion = requestedGradleVersion
            classpathFilter = GradleRunner.CLASSPATH_DEFAULT & FILTER_SPOCK_JAR
        and:
            if (requestedGradleVersion.startsWith("8.")) {
                //TODO: Eliminate:
                // - Disabling Gradle user home cache cleanup with the 'org.gradle.cache.cleanup' property has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/8.14.1/userguide/upgrading_version_8.html#disabling_user_home_cache_cleanup
                // - The ReportingExtension.getBaseDir() method has been deprecated. This is scheduled to be removed in Gradle 9.0. Please use the getBaseDirectory() property method instead. Consult the upgrading guide for further information: https://docs.gradle.org/8.14.1/userguide/upgrading_version_8.html#reporting-base-dir
                System.setProperty("ignoreDeprecations", "true")
            }
        when:
            copyResources("testProjects/simple1", "")
        then:
            fileExists('build.gradle')
        when:
            ExecutionResult result = runTasksSuccessfully('pitest', '--warning-mode', 'all')
        then:
            result.wasExecuted(':pitest')
            result.standardOutput.contains('Generated 1 mutations Killed 1 (100%)')
        and:    //issue with Gradle <5.0 where Integer/Boolean property had 0/false provided by default
            //TODO: verifyAll would be great, but it's broken with explicit "assert" - https://github.com/spockframework/spock/issues/855#issuecomment-528411874
            PIT_PARAMETERS_NAMES_NOT_SET_BY_DEFAULT.each { parameterName ->
                assert !result.standardOutput.contains("${parameterName}=")
            }
        where:
            requestedGradleVersion << applyJavaCompatibilityAdjustment(resolveRequestedGradleVersions()).unique()
    }

    //TODO: Could be restored minimal version upgrade
    @IgnoreIf({ new PreconditionContext().javaVersion >= 13 })   //There is no unsupported version of Gradle which can be used with Java 13
    void "should fail with meaningful error message with too old Gradle version"() {
        given:
            gradleVersion = "6.0"   //TODO: With 5.5.1 it fails with "ClassNotFoundException: org.gradle.api.file.FileSystemLocationProperty" anyway after removing compatibility hack
        and:
            assert PitestPlugin.MINIMAL_SUPPORTED_GRADLE_VERSION > GradleVersion.version(gradleVersion)
        when:
            copyResources("testProjects/simple1", "")
        then:
            fileExists('build.gradle')
        when:
            ExecutionResult result = runTasksWithFailure('tasks')
        then:
            verifyAll {
                Throwable root = Exceptions.getRootCause(result.failure)
                root.class.name == GradleException.name //name to mitigate differences on classloader
                root.message.contains("'info.solidsoft.pitest' requires")
                result.standardOutput.contains("WARNING. The 'info.solidsoft.pitest' plugin requires")
            }
    }

    //To prevent failure when Spock for Groovy 2.5 is run with Groovy 3.0 delivered with Gradle 7+
    //Spock is not needed in this artificial project - just the test classpath leaks to Gradle instance started by Nebula
    private static final Pattern SPOCK_JAR_PATTERN = Pattern.compile(".*spock-core-2\\..*.jar")
    @SuppressWarnings('JUnitPublicProperty')
    @PackageScope
    static final Predicate<URL> FILTER_SPOCK_JAR = { URL url ->
        return !url.toExternalForm().matches(SPOCK_JAR_PATTERN)
    } as Predicate<URL>

    //TODO: Extract regression tests control mechanism to a separate class (or even better trait) when needed in some other place
    private static final String REGRESSION_TESTS_ENV_NAME = "PITEST_REGRESSION_TESTS"
    private static final List<String> GRADLE6_VERSIONS = ["6.9.2", "6.8.3", "6.7", "6.6", "6.5",
                                                          PitestPlugin.MINIMAL_SUPPORTED_GRADLE_VERSION.version]
    private static final List<String> GRADLE7_VERSIONS = ["7.6.3", "7.5.1", "7.4.1", "7.3.3", "7.2", "7.1.1", "7.0.2"]
    private static final List<String> GRADLE8_VERSIONS = [LATEST_KNOWN_GRADLE_VERSION.version, "8.9", "8.8", "8.7", "8.6.4", "8.5", "8.4", "8.3",
                                                          "8.2.1", "8.1.1", "8.0.2"]
    private static final List<String> GRADLE_LATEST_VERSIONS = [/*GRADLE6_VERSIONS.first(),*/ GRADLE7_VERSIONS.first(), GRADLE8_VERSIONS.first(),
                                                                /*PitestPlugin.MINIMAL_SUPPORTED_GRADLE_VERSION.version*/]

    @SuppressWarnings('GroovyFallthrough')
    private List<String> resolveRequestedGradleVersions() {
        String regressionTestsLevel = System.getenv(REGRESSION_TESTS_ENV_NAME)
        log.debug("$REGRESSION_TESTS_ENV_NAME set to '${regressionTestsLevel}'")
        switch (regressionTestsLevel) {
            case "latestOnly":
            case null:
                return GRADLE_LATEST_VERSIONS
            case "quick":
                return GRADLE_LATEST_VERSIONS
            case "full":
                return GRADLE6_VERSIONS + GRADLE7_VERSIONS + GRADLE8_VERSIONS
            default:
                log.warn("Unsupported $REGRESSION_TESTS_ENV_NAME value '`$regressionTestsLevel`' (expected 'latestOnly', 'quick' or 'full'). " +
                    "Assuming 'latestOnly'.")
                return GRADLE_LATEST_VERSIONS
        }
    }

    @SuppressWarnings("ConfusingTernary")
    private List<String> applyJavaCompatibilityAdjustment(List<String> requestedGradleVersions) {
        GradleVersion minimalCompatibleGradleVersion = MINIMAL_GRADLE_VERSION_FOR_JAVA_VERSION.get(Jvm.current().javaVersion)
        return leaveJavaXCompatibleGradleVersionsOnly(requestedGradleVersions, minimalCompatibleGradleVersion)
    }

    private List<String> leaveJavaXCompatibleGradleVersionsOnly(List<String> requestedGradleVersions, GradleVersion minimalCompatibleJavaVersion) {
        List<String> javaXCompatibleGradleVersions = requestedGradleVersions.findAll { version ->
            GradleVersion.version(version) >= minimalCompatibleJavaVersion
        }
        if (javaXCompatibleGradleVersions.size() < 2) {
            javaXCompatibleGradleVersions.add(minimalCompatibleJavaVersion.version)
        }
        return javaXCompatibleGradleVersions
    }

}
