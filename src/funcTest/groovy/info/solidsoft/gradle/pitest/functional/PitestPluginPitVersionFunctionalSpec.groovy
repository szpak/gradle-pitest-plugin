package info.solidsoft.gradle.pitest.functional

import groovy.transform.CompileDynamic
import info.solidsoft.gradle.pitest.PitestPlugin
import nebula.test.functional.ExecutionResult
import org.gradle.internal.jvm.Jvm

@SuppressWarnings("GrMethodMayBeStatic")
@CompileDynamic
class PitestPluginPitVersionFunctionalSpec extends AbstractPitestFunctionalSpec {

    private static final String PIT_1_3_VERSION = "1.3.1"
    private static final String MINIMAL_JAVA9_COMPATIBLE_PIT_VERSION = "1.2.3"  //https://github.com/hcoles/pitest/issues/380
    private static final String MINIMAL_JAVA10_COMPATIBLE_PIT_VERSION = "1.4.0"
    private static final String MINIMAL_JAVA11_COMPATIBLE_PIT_VERSION = "1.4.2" //in fact 1.4.1, but 1.4.2 is also Java 12 compatible
    private static final String MINIMAL_JAVA13_COMPATIBLE_PIT_VERSION = "1.4.6" //not officially, but at least simple case works

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
            ExecutionResult result = runTasksSuccessfully('pitest')
        then:
            result.wasExecuted(':pitest')
        and:
            result.standardOutput.contains("Using PIT: ${pitVersion}")
        and:
            result.standardOutput.contains('Generated 2 mutations Killed 1 (50%)')
            result.standardOutput.contains('Ran 2 tests (1 tests per mutation)')
        where:
            pitVersion << getPitVersionsCompatibleWithCurrentJavaVersion().unique() //be aware that unique() is available since Groovy 2.4.0
    }

    @SuppressWarnings("IfStatementCouldBeTernary")
    private List<String> getPitVersionsCompatibleWithCurrentJavaVersion() {
        if (isJava13Compatible()) {
            return [PitestPlugin.DEFAULT_PITEST_VERSION, MINIMAL_JAVA13_COMPATIBLE_PIT_VERSION]
        }

        if (Jvm.current().javaVersion.isJava11Compatible()) {
            return [PitestPlugin.DEFAULT_PITEST_VERSION, MINIMAL_JAVA11_COMPATIBLE_PIT_VERSION]
        }
        if (Jvm.current().javaVersion.isJava10Compatible()) {
            return [PitestPlugin.DEFAULT_PITEST_VERSION, MINIMAL_JAVA10_COMPATIBLE_PIT_VERSION]
        }
        if (Jvm.current().javaVersion.isJava9Compatible()) {
            return [PitestPlugin.DEFAULT_PITEST_VERSION, MINIMAL_JAVA9_COMPATIBLE_PIT_VERSION, PIT_1_3_VERSION]
        }
        return [PitestPlugin.DEFAULT_PITEST_VERSION, "1.1.5", "1.2.0", PIT_1_3_VERSION, "1.4.0"]
    }

}
