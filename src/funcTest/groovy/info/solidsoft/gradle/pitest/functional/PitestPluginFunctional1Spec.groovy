package info.solidsoft.gradle.pitest.functional

import info.solidsoft.gradle.pitest.PitestPlugin
import nebula.test.functional.ExecutionResult
import org.gradle.internal.jvm.Jvm

@SuppressWarnings("GrMethodMayBeStatic")
class PitestPluginFunctional1Spec extends AbstractPitestFunctionalSpec {
    private static final String PIT_1_3_VERSION = "1.3.1"
    private static final String MINIMAL_JAVA9_COMPATIBLE_PIT_VERSION = "1.2.3"  //https://github.com/hcoles/pitest/issues/380
    private static final String MINIMAL_JAVA10_COMPATIBLE_PIT_VERSION = "1.4.0"

    def "setup and run simple build on pitest infrastructure with AGP: #requestedAndroidGradlePluginVersion"() {
        given:
            buildFile << """
                buildscript {
                    repositories {
                        google()
                        jcenter()
                    }
                    dependencies {
                        classpath 'com.android.tools.build:gradle:$requestedAndroidGradlePluginVersion'
                    }
                }
                
                apply plugin: 'pl.droidsonroids.pitest'
                apply plugin: 'com.android.library'

                android {
                    compileSdkVersion 28
                    defaultConfig {
                        minSdkVersion 10
                        targetSdkVersion 28
                    }
                    lintOptions {
                        //ignore missing lint database
                        abortOnError false
                    }
                }
                repositories {
                    google()
                    mavenCentral()
                    jcenter()
                }
                dependencies {
                    testCompile 'junit:junit:4.12'
                }
            """.stripIndent()
        and:
            writeManifestFile()
        when:
            writeHelloWorld('gradle.pitest.test.hello')
        then:
            fileExists('src/main/java/gradle/pitest/test/hello/HelloWorld.java')
        when:
            writeTest('src/test/java/', 'gradle.pitest.test.hello', false)
        then:
            fileExists('src/test/java/gradle/pitest/test/hello/HelloWorldTest.java')
        when:
            ExecutionResult result = runTasksSuccessfully('build')
        then:
            if (requestedAndroidGradlePluginVersion.startsWith("3.2")) {
                fileExists('build/intermediates/javac/release/compileReleaseJavaWithJavac/classes/gradle/pitest/test/hello/HelloWorld.class')
            } else {
                fileExists('build/intermediates/classes/release/gradle/pitest/test/hello/HelloWorld.class')
            }
            result.wasExecuted(':test')
        where:
            requestedAndroidGradlePluginVersion << resolveRequestedAndroidGradlePluginVersion()
    }

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
            writeManifestFile()
            writeHelloPitClass()
            writeHelloPitTest()
        when:
            ExecutionResult result = runTasksSuccessfully('pitestRelease')
        then:
            result.wasExecuted(':pitestRelease')
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
            ExecutionResult result = runTasksSuccessfully('pitestRelease')
        then:
            result.wasExecuted(':pitestRelease')
        and: 'plugin enabled'
            result.standardOutput.contains('with the following plugin configuration') ||
                result.standardError.contains('with the following plugin configuration')
        and: 'plugin parameters passed'
            result.standardOutput.contains('pitest-plugin-configuration-reporter-plugin.key1=value1') ||
                result.standardError.contains('pitest-plugin-configuration-reporter-plugin.key1=value1')
            result.standardOutput.contains('pitest-plugin-configuration-reporter-plugin.key2=value2') ||
                result.standardError.contains('pitest-plugin-configuration-reporter-plugin.key2=value2')
        and: 'built-in features passed'
            result.standardOutput.contains("-FANN")   ||
                result.standardError.contains("-FANN")
            result.standardOutput.contains("+FINFIT") ||
                result.standardError.contains("+FINFIT")
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
            ExecutionResult result = runTasksSuccessfully('pitestRelease')
        then:
            result.wasExecuted(':pitestRelease')
            result.standardOutput.contains('--classPathFile=')
            //TODO: Verify file name with regex
            !result.standardOutput.find("--classPath=")
    }

    private static String getBasicGradlePitestConfig() {
        return """
                apply plugin: 'pl.droidsonroids.pitest'
                apply plugin: 'com.android.library'

                android {
                    compileSdkVersion 28
                    defaultConfig {
                        minSdkVersion 10
                        targetSdkVersion 28
                    }
                }
                group = 'gradle.pitest.test'

                repositories {
                    google()
                    mavenCentral()
                    jcenter()
                }
                buildscript {
                    repositories {
                        google()
                        mavenCentral()
                        jcenter()
                    }
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
