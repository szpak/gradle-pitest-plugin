package info.solidsoft.gradle.pitest.functional

import info.solidsoft.gradle.pitest.PitestPlugin
import nebula.test.functional.ExecutionResult
import org.gradle.internal.jvm.Jvm

@SuppressWarnings("GrMethodMayBeStatic")
class PitestPluginFunctional1Spec extends AbstractPitestFunctionalSpec {

    private static final String PIT_1_3_VERSION = "1.3.1"
    private static final String MINIMAL_JAVA9_COMPATIBLE_PIT_VERSION = "1.2.3"  //https://github.com/hcoles/pitest/issues/380
    private static final String MINIMAL_JAVA10_COMPATIBLE_PIT_VERSION = "1.4.0"

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

    def "enable PIT plugin when on classpath and pass plugin configuration to PIT"() {
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
            result.getStandardOutput().contains('with the following plugin configuration')
        and: 'plugin parameters passed'
            result.getStandardOutput().contains('pitest-plugin-configuration-reporter-plugin.key1=value1')
            result.getStandardOutput().contains('pitest-plugin-configuration-reporter-plugin.key2=value2')
        and: 'built-in features passed'
            result.getStandardOutput().contains("-FANN")
            result.getStandardOutput().contains("+FINFIT")
        and: 'verbose output enabled'
            result.getStandardOutput().contains("PIT >> FINE")
            //TODO: Add plugin features once available - https://github.com/hcoles/pitest-plugins/issues/2
    }

    def "use file to pass additional classpath to PIT if enabled"() {   //Needed? Already tested with ProjectBuilder in PitestTaskConfigurationSpec
        given:
            buildFile << getBasicGradlePitestConfig()
            buildFile << """
                pitest {
                    useClasspathFile = true
                }
            """.stripIndent()
        and:
            writeHelloPitClass()
            writeHelloPitTest()
        when:
            ExecutionResult result = runTasksSuccessfully('pitest')
        then:
            result.wasExecuted(':pitest')
            result.getStandardOutput().contains('--classPathFile=')
            //TODO: Verify file name with regex
            !result.getStandardOutput().find("--classPath=")
    }

    private static String getBasicGradlePitestConfig() {
        return """
                apply plugin: 'info.solidsoft.pitest'
                group = 'gradle.pitest.test'

                repositories {
                    mavenCentral()
                }
                buildscript {
                    repositories {
                        mavenCentral()
                    }
//                    //Local/current version of the plugin should be put on a classpath anyway
//                    //That cannot be also used to override the plugin version as the current version is earlier on a classpath
//                    dependencies {
//                        classpath 'info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.1.9'
//                    }
                }
                dependencies {
                    testCompile 'junit:junit:4.12'
                }
        """.stripIndent()
    }

    private void writeHelloPitClass(String packageDotted = 'gradle.pitest.test.hello', File baseDir = getProjectDir()) {
        def path = 'src/main/java/' + packageDotted.replace('.', '/') + '/HelloPit.java'
        def javaFile = createFile(path, baseDir)
        javaFile << """package ${packageDotted};

            public class HelloPit {
                public int returnInputNumber(int inputNumber) {
                    System.out.println("Mutation to survive");
                    return inputNumber;
                }
            }
        """.stripIndent()
    }

    private void writeHelloPitTest(String packageDotted = 'gradle.pitest.test.hello', File baseDir = getProjectDir()) {
        def path = 'src/test/java/' + packageDotted.replace('.', '/') + '/HelloPitTest.java'
        def javaFile = createFile(path, baseDir)
        javaFile << """package ${packageDotted};
            import org.junit.Test;
            import static org.junit.Assert.assertEquals;

            public class HelloPitTest {
                @Test public void shouldReturnInputNumber() {
                    assertEquals(5, new HelloPit().returnInputNumber(5)); 
                }
            }
        """.stripIndent()
    }

    private List<String> getPitVersionsCompoatibleWithCurrentJavaVersion() {
        //Gradle plugin should be compatible with at least PIT 1.0.0, but this test fails on Windows
        //due to https://github.com/hcoles/pitest/issues/179 which was fixed in 1.1.5
        //PIT before 1.2.3 is not compatible with Java 9
        //PIT before 1.4.0 is not compatible with Java 10
        if (Jvm.current().javaVersion.isJava10Compatible()) {
            return [PitestPlugin.DEFAULT_PITEST_VERSION, MINIMAL_JAVA10_COMPATIBLE_PIT_VERSION]
        }
        if (Jvm.current().javaVersion.isJava9Compatible()) {
            return [PitestPlugin.DEFAULT_PITEST_VERSION, MINIMAL_JAVA9_COMPATIBLE_PIT_VERSION, PIT_1_3_VERSION]
        }
        return [PitestPlugin.DEFAULT_PITEST_VERSION, "1.1.5", "1.2.0", PIT_1_3_VERSION, "1.4.0"]
    }
}
