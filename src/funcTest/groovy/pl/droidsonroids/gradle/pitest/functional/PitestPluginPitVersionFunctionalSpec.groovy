package pl.droidsonroids.gradle.pitest.functional

import groovy.transform.CompileDynamic
import nebula.test.functional.ExecutionResult
import org.gradle.internal.jvm.Jvm
import pl.droidsonroids.gradle.pitest.PitestPlugin

@SuppressWarnings("GrMethodMayBeStatic")
@CompileDynamic
class PitestPluginPitVersionFunctionalSpec extends AbstractPitestFunctionalSpec {

    private static final String MINIMAL_SUPPORTED_PIT_VERSION = "1.4.1"
    //minimal PIT version that works with Java 11 - Aug 2018
    private static final String MINIMAL_JAVA17_COMPATIBLE_PIT_VERSION = "1.6.8"

    void "setup and run pitest task with PIT #pitVersion"() {
        given:
            buildFile << getBasicGradlePitestConfig()
        and:
            buildFile << """
                pitest {
                    pitestVersion = '$pitVersion'
                }
            """.stripIndent()
        and:
            writeHelloPitClass()
            writeHelloPitTest()
        when:
            ExecutionResult result = runTasks('pitest')
        then:
            !result.standardError.contains("Build failed with an exception")
            !result.failure
            result.wasExecuted(':pitest')
        and:
            result.standardOutput.contains("Using PIT: ${pitVersion}")
            result.standardOutput.contains("pitest-${pitVersion}.jar")
        and:
            result.standardOutput.contains('Generated 2 mutations Killed 1 (50%)')
            result.standardOutput.contains('Ran 2 tests (1 tests per mutation)')
        where:
            pitVersion << getPitVersionsCompatibleWithCurrentJavaVersion().unique()
    }

    private List<String> getPitVersionsCompatibleWithCurrentJavaVersion() {
        return [getMinimalPitVersionCompatibleWithCurrentJavaVersion(), PitestPlugin.DEFAULT_PITEST_VERSION]
    }

    private String getMinimalPitVersionCompatibleWithCurrentJavaVersion() {
        if (Jvm.current().javaVersion.isJava12Compatible()) {
            return MINIMAL_JAVA17_COMPATIBLE_PIT_VERSION    //safe modern version
        } else {
            return MINIMAL_SUPPORTED_PIT_VERSION
            //minimal supported PIT version only for testing with Java <12, due to JVM compatibility issues
        }
    }

}
