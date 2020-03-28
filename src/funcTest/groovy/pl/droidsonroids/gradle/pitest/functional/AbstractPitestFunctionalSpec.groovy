package pl.droidsonroids.gradle.pitest.functional

import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult

abstract class AbstractPitestFunctionalSpec extends IntegrationSpec {

    void setup() {
        fork = true
        //to make stdout assertion work with Gradle 2.x - http://forums.gradle.org/gradle/topics/unable-to-catch-stdout-stderr-when-using-tooling-api-i-gradle-2-x#reply_15357743
        memorySafeMode = true   //shutdown Daemon after a few seconds of inactivity
        copyResources('AndroidManifest.xml', 'src/main/AndroidManifest.xml')
    }

    def writeManifestFile() {
        def manifestFile = new File(projectDir, 'src/main/AndroidManifest.xml')
        manifestFile.parentFile.mkdirs()
        manifestFile.write('<?xml version="1.0" encoding="utf-8"?><manifest package="pl.droidsonroids.pitest.hello"/>')
    }

    protected static String getBasicGradlePitestConfig() {
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

    //TODO: Switch to Gradle mechanism once upgrade to 6.x
    protected boolean isJava13Compatible() {
        return System.getProperty("java.version").startsWith("13") || isJava14Compatible()
    }

    protected boolean isJava14Compatible() {
        return System.getProperty("java.version").startsWith("14") || System.getProperty("java.version").startsWith("15")
    }
}
