package info.solidsoft.gradle.pitest.functional

import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult

abstract class AbstractPitestFunctionalSpec extends IntegrationSpec {

    void setup() {
        fork = true //to make stdout assertion work with Gradle 2.x - http://forums.gradle.org/gradle/topics/unable-to-catch-stdout-stderr-when-using-tooling-api-i-gradle-2-x#reply_15357743
        memorySafeMode = true   //shutdown Daemon after a few seconds of inactivity
    }

    protected static String getBasicGradlePitestConfig() {
        return """
                apply plugin: 'java'
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
                    testImplementation 'junit:junit:4.12'
                }
        """.stripIndent()
    }

    protected void writeHelloPitClass(String packageDotted = 'gradle.pitest.test.hello', File baseDir = getProjectDir()) {
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

    protected void writeHelloPitTest(String packageDotted = 'gradle.pitest.test.hello', File baseDir = getProjectDir()) {
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

    protected void assertStdOutOrStdErrContainsGivenText(ExecutionResult result, String textToContain) {
        //TODO: Simplify if possible - standardOutput for Gradle <5 and standardError for Gradle 5+
        assert result.standardOutput.contains(textToContain) || result.standardError.contains(textToContain)
    }

    //TODO: Switch to Gradle mechanism once upgraded to 6.x
    protected boolean isJava13Compatible() {
        return System.getProperty("java.version").startsWith("13") || isJava14Compatible()
    }

    protected boolean isJava14Compatible() {
        return System.getProperty("java.version").startsWith("14") || System.getProperty("java.version").startsWith("15")
    }
}
