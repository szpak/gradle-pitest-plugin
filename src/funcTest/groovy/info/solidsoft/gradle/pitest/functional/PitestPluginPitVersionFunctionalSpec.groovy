package info.solidsoft.gradle.pitest.functional

import info.solidsoft.gradle.pitest.PitestPlugin
import nebula.test.functional.ExecutionResult
import org.gradle.internal.jvm.Jvm

@SuppressWarnings("GrMethodMayBeStatic")
class PitestPluginPitVersionFunctionalSpec extends AbstractPitestFunctionalSpec {

    private static final String PIT_1_3_VERSION = "1.3.1"
    private static final String MINIMAL_JAVA9_COMPATIBLE_PIT_VERSION = "1.2.3"  //https://github.com/hcoles/pitest/issues/380
    private static final String MINIMAL_JAVA10_COMPATIBLE_PIT_VERSION = "1.4.0"
    private static final String MINIMAL_JAVA11_COMPATIBLE_PIT_VERSION = "1.4.2" //in fact 1.4.1, but 1.4.2 is also Java 12 compatible
    private static final String MINIMAL_JAVA13_COMPATIBLE_PIT_VERSION = "1.4.6" //not officially, but at least simple case works

    def "setup and run pitest task with PIT #pitVersion"() {
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
            result.getStandardOutput().contains('Generated 2 mutations Killed 1 (50%)')
            result.getStandardOutput().contains('Ran 2 tests (1 tests per mutation)')
        where:
            pitVersion << getPitVersionsCompoatibleWithCurrentJavaVersion().unique() //be aware that unique() is available since Groovy 2.4.0
    }

    private List<String> getPitVersionsCompoatibleWithCurrentJavaVersion() {
        //Gradle plugin should be compatible with at least PIT 1.0.0, but this test fails on Windows
        //due to https://github.com/hcoles/pitest/issues/179 which was fixed in 1.1.5
        //PIT before 1.2.3 is not compatible with Java 9
        //PIT before 1.4.0 is not compatible with Java 10
        //PIT before 1.4.1 is not compatible with Java 11
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

    //TODO: Switch to Gradle mechanism once upgrade to 5.x
    private boolean isJava13Compatible() {
        return System.getProperty("java.version").startsWith("13") || System.getProperty("java.version").startsWith("14")
    }
}
