package info.solidsoft.gradle.pitest.functional

import groovy.transform.CompileDynamic
import info.solidsoft.gradle.pitest.PitestPlugin
import nebula.test.functional.ExecutionResult

@SuppressWarnings("GrMethodMayBeStatic")
@CompileDynamic
class PitestPluginPitVersionFunctionalSpec extends AbstractPitestFunctionalSpec {

    private static final String MINIMAL_SUPPORTED_PIT_VERSION = "1.7.1"  //minimal PIT version that includes verbosity flag - Sep 2021

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
        return MINIMAL_SUPPORTED_PIT_VERSION
    }

}
