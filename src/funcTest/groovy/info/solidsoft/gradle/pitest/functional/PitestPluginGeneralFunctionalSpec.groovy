package info.solidsoft.gradle.pitest.functional

import groovy.transform.CompileDynamic
import info.solidsoft.gradle.pitest.PitestPlugin
import nebula.test.functional.ExecutionResult
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Issue

@CompileDynamic
class PitestPluginGeneralFunctionalSpec extends AbstractPitestFunctionalSpec {

    @Rule
    protected TemporaryFolder tmpDir = new TemporaryFolder()

    void "enable PIT plugin when on classpath and pass plugin configuration to PIT"() {
        given:
            buildFile << getBasicGradlePitestConfig()
            buildFile << """
                buildscript {
                    repositories {
                        maven { url "https://dl.bintray.com/szpak/pitest-plugins/" }
                    }
                    configurations.maybeCreate("pitest")
                    dependencies {
                        pitest 'org.pitest.plugins:pitest-plugin-configuration-reporter-plugin:0.0.2'
                    }
                }
                pitest {
                    excludedClasses = []
                    verbose = true
                    pluginConfiguration = ['pitest-plugin-configuration-reporter-plugin.key1': 'value1',
                                           'pitest-plugin-configuration-reporter-plugin.key2': 'value2']
                    features = ["-FANN", "+FINFIT(a[1] a[2])"]
                    outputFormats = ['pluginConfigurationReporter']
                }
            """.stripIndent()
        and:
            writeHelloPitClass()
            writeHelloPitTest()
        when:
            ExecutionResult result = runTasksSuccessfully('pitest')
        then:
            result.wasExecuted(':pitest')
        and: 'plugin enabled'
            assertStdOutOrStdErrContainsGivenText(result, 'with the following plugin configuration')
        and: 'plugin parameters passed'
            result.getStandardOutput().contains('pitest-plugin-configuration-reporter-plugin.key1=value1')
            result.getStandardOutput().contains('pitest-plugin-configuration-reporter-plugin.key2=value2')
        and: 'built-in features passed'
            result.getStandardOutput().contains("-FANN")
            result.getStandardOutput().contains("+FINFIT")
        and: 'verbose output enabled'
            assertStdOutOrStdErrContainsGivenText(result, "PIT >> FINE")
            //TODO: Add plugin features once available - https://github.com/hcoles/pitest-plugins/issues/2
    }

    @Issue(["https://github.com/gradle/gradle/issues/12351", "https://github.com/szpak/gradle-pitest-plugin/issues/189"])
    void "allow to use RegularFileProperty @Input and @Output fields in task"() {
        given:
            File historyInputLocation = tmpDir.newFile()
            File historyOutputLocation = tmpDir.newFile()
        and:
            buildFile << getBasicGradlePitestConfig()
            buildFile << """
                pitest {
                    historyInputLocation = "${quoteBackslashesInWindowsPath(historyInputLocation)}"
                    historyOutputLocation = "${quoteBackslashesInWindowsPath(historyOutputLocation)}"
                }
            """.stripIndent()
        and:
            writeHelloPitClass()
            writeHelloPitTest()
        when:
            ExecutionResult result = runTasksSuccessfully('pitest')
        then:
            result.wasExecuted(':pitest')
            result.getStandardOutput().contains("--historyInputLocation=${historyInputLocation.absolutePath}")
        and:    //it works with @OutputFile by default, but just in case
            result.getStandardOutput().contains("--historyOutputLocation=${historyOutputLocation.absolutePath}")
            historyOutputLocation.size()
    }

    void "pass additional configured parameters that cannot be test with ProjectBuilder"() {
        given:
            buildFile << getBasicGradlePitestConfig()
            buildFile << """
                pitest {
                    useClasspathFile = true
                    mainProcessJvmArgs = ["-XX:+UnlockExperimentalVMOptions"]
                }
            """.stripIndent()
        and:
            writeHelloPitClass()
            writeHelloPitTest()
        when:
            ExecutionResult result = runTasksSuccessfully('pitest')
        then:
            result.wasExecuted(':pitest')

        and: "use file to pass additional classpath to PIT if enabled"  //Needed? Already tested with ProjectBuilder in PitestTaskConfigurationSpec
            result.getStandardOutput().contains(
                "--classPathFile=${new File(projectDir, "build//${PitestPlugin.PIT_ADDITIONAL_CLASSPATH_DEFAULT_FILE_NAME}").absolutePath}")
            !result.getStandardOutput().find("--classPath=")

        and: "use defined mainProcessJvmArgs to run PIT main process"
            result.getStandardOutput().matches(/(?m)[\s\S]*java(.exe)* -XX:\+UnlockExperimentalVMOptions[\s\S]*/)
    }

    private String quoteBackslashesInWindowsPath(File file) {
        //There is problem with backslash within '' or "" while running this test on Windows: "unexpected char"
        return file.absolutePath.replaceAll('\\\\', '\\\\\\\\')
    }

}
