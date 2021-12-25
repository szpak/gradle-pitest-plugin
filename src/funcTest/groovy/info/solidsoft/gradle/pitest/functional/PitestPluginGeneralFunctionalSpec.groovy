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
            copyResources("testRepos", "")  //Custom artifacts due to: https://github.com/hcoles/pitest-plugins/pull/4
            buildFile << """
                repositories {
                    maven { url "./customPluginRepo/" }
                }
                dependencies {
                    pitest 'org.pitest.plugins:pitest-plugin-configuration-reporter-plugin:0.0.2'
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

    void "report URI is not printed if test suite is not green"() {
        given:
            buildFile << getBasicGradlePitestConfig()
        and:
            writeHelloPitClass()
            writeFailingPitTest()
        when:
            ExecutionResult result = runTasksWithFailure('pitest')
        then:
            result.wasExecuted(':pitest')
            !result.getStandardOutput().contains('index.html')
    }

    void "timestamped report URI is printed if pitest run fails due to unmet thresholds"() {
        given:
            buildFile << getBasicGradlePitestConfig()
            buildFile << """
                pitest {
                    mutationThreshold = 101
                }
            """.stripIndent()
        and:
            writeHelloPitClass()
            writeHelloPitTest()
        when:
            ExecutionResult result = runTasksWithFailure('pitest')
        then:
            result.wasExecuted(':pitest')
            result.getStandardOutput().find(/pitest.[0-9]{12}.index\.html/)
    }

    void "timestamped report URI is not printed if turned off and pitest run fails due to unmet thresholds"() {
        given:
            buildFile << getBasicGradlePitestConfig()
            buildFile << """
                pitest {
                    mutationThreshold = 101
                    printReportUri = 'never'
                }
            """.stripIndent()
        and:
            writeHelloPitClass()
            writeHelloPitTest()
        when:
            ExecutionResult result = runTasksWithFailure('pitest')
        then:
            result.wasExecuted(':pitest')
            !result.getStandardOutput().find(/pitest.[0-9]{12}.index\.html/)
    }

    void "timestamped report URI is printed if always requested and pitest run passes"() {
        given:
            buildFile << getBasicGradlePitestConfig()
            buildFile << """
                pitest {
                    printReportUri = 'always'
                }
            """.stripIndent()
        and:
            writeHelloPitClass()
            writeHelloPitTest()
        when:
            ExecutionResult result = runTasksSuccessfully('pitest')
        then:
            result.wasExecuted(':pitest')
            result.getStandardOutput().find(/pitest.[0-9]{12}.index\.html/)
    }

    void "non-timestamped report URI is printed if always requested and pitest run passes"() {
        given:
            buildFile << getBasicGradlePitestConfig()
            buildFile << """
                pitest {
                    printReportUri = 'always'
                    timestampedReports = false
                }
            """.stripIndent()
        and:
            writeHelloPitClass()
            writeHelloPitTest()
        when:
            ExecutionResult result = runTasksSuccessfully('pitest')
        then:
            result.wasExecuted(':pitest')
            result.getStandardOutput().contains(quoteBackslashesInWindowsPath(new File('reports/pitest/index.html')))
    }

    void "pass additional configured parameters that cannot be test with ProjectBuilder"() {
        given:
            buildFile << getBasicGradlePitestConfig()
            buildFile << """
                pitest {
                    timestampedReports = false  //to do not mess with file path on source code in report verification
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
        and:
            result.standardOutput.contains('Generated 2 mutations Killed 1 (50%)')
            result.standardOutput.contains('Ran 2 tests (1 tests per mutation)')

        and: "use file to pass additional classpath to PIT if enabled"  //Needed? Already tested with ProjectBuilder in PitestTaskConfigurationSpec
            result.getStandardOutput().contains(
                "--classPathFile=${new File(projectDir, "build//${PitestPlugin.PIT_ADDITIONAL_CLASSPATH_DEFAULT_FILE_NAME}").absolutePath}")
            !result.getStandardOutput().find("--classPath=")

        and: "use defined mainProcessJvmArgs to run PIT main process"
            result.getStandardOutput().matches(/(?m)[\s\S]*java(.exe)* -XX:\+UnlockExperimentalVMOptions[\s\S]*/)

        and: "has source code available in report"
            File htmlFileWithReportForHelloPit = new File(projectDir, "build//reports//pitest//gradle.pitest.test.hello//HelloPit.java.html")
            htmlFileWithReportForHelloPit.text.contains("System.out.println(&#34;Mutation to survive&#34;);")
    }

    @Issue("https://github.com/szpak/gradle-pitest-plugin/issues/67")
    void "reuses cached output"() {
        given:
            buildFile << getBasicGradlePitestConfig()
        and:
            writeHelloPitClass()
            writeHelloPitTest()
        when:
            ExecutionResult result = runTasksSuccessfully('pitest', '--build-cache')
            ExecutionResult result2 = runTasksSuccessfully('clean', 'pitest', '--build-cache')
        then:
            result.wasExecuted(':pitest')
            result.getStandardOutput().contains("Build cache key for task ':pitest' is")
//            //TODO: It's flaky - build cache for TestKit executions seems to be also cached
//            //      Tests in Gradle itself have similar problem: https://github.com/gradle/gradle/blob/5ec3f672ed600a86280be490395d70b7bc634862/subprojects/core/src/integTest/groovy/org/gradle/api/tasks/CachedTaskIntegrationTest.groovy#L118-L132
//            result.getStandardOutput().contains("Stored cache entry for task ':pitest'")
        and:
            result2.wasExecuted(':pitest')
            result2.getStandardOutput().contains("Task :pitest FROM-CACHE")
    }

    private String quoteBackslashesInWindowsPath(File file) {
        //There is problem with backslash within '' or "" while running this test on Windows: "unexpected char"
        return file.toString().replaceAll('\\\\', '\\\\\\\\')
    }

}
