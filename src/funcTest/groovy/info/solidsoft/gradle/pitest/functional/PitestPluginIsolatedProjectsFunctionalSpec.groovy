package info.solidsoft.gradle.pitest.functional

import groovy.transform.CompileDynamic
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir

@CompileDynamic
class PitestPluginIsolatedProjectsFunctionalSpec extends Specification {

    private static final String ISOLATED_PROJECTS_GRADLE_VERSION = "9.7.1"
    private static final String CROSS_PROJECT_BUILDSCRIPT_VIOLATION_MESSAGE =
        "cannot access 'Project.buildscript' functionality on another project"

    @SuppressWarnings("JUnitPublicProperty")   //required to be non-private for @TempDir injection
    @TempDir
    File projectDir

    void setup() {
        new File(projectDir, 'settings.gradle').text = """
            rootProject.name = 'isolated-projects-repro'
            include 'sub'
        """.stripIndent()
        //Intentionally minimal - the root project does not apply the pitest plugin and does not
        //touch any subproject state, so the only remaining candidate for a cross-project access
        //violation is the plugin itself when applied to ':sub'.
        new File(projectDir, 'build.gradle').text = ''
        new File(projectDir, 'gradle.properties').text = 'org.gradle.configuration-cache=true\n'
        File subDir = new File(projectDir, 'sub')
        subDir.mkdirs()
        new File(subDir, 'build.gradle').text = """
            plugins {
                id 'java'
                id 'info.solidsoft.pitest'
            }
            repositories { mavenCentral() }
            group = 'pitest.test'
            dependencies { testImplementation 'junit:junit:4.12' }
        """.stripIndent()
        writeHelloPitClass(subDir)
        writeHelloPitTest(subDir)
    }

    void "applying the plugin only to a subproject no longer violates Isolated Projects by reading rootProject.buildscript"() {
        when:
            BuildResult result = gradleRunnerFor(':sub:pitest', '--isolated-projects', '--stacktrace').build()
        then:
            result.task(':sub:pitest').outcome == TaskOutcome.SUCCESS
        and:
            !result.output.contains(CROSS_PROJECT_BUILDSCRIPT_VIOLATION_MESSAGE)
    }

    private GradleRunner gradleRunnerFor(String... arguments) {
        return GradleRunner.create()
            .withGradleVersion(ISOLATED_PROJECTS_GRADLE_VERSION)
            .withProjectDir(projectDir)
            .withPluginClasspath(readPluginUnderTestClasspath())
            .withArguments(arguments)
    }

    private static void writeHelloPitClass(File subDir) {
        File javaFile = new File(subDir, 'src/main/java/pitest/test/HelloPit.java')
        javaFile.parentFile.mkdirs()
        javaFile.text = """
            package pitest.test;

            public class HelloPit {
                public int returnInputNumber(int inputNumber) {
                    System.out.println("Mutation to survive");
                    return inputNumber;
                }
            }
        """.stripIndent()
    }

    private static void writeHelloPitTest(File subDir) {
        File javaFile = new File(subDir, 'src/test/java/pitest/test/HelloPitTest.java')
        javaFile.parentFile.mkdirs()
        javaFile.text = """
            package pitest.test;

            import org.junit.Test;
            import static org.junit.Assert.assertEquals;

            public class HelloPitTest {
                @Test public void shouldReturnInputNumber() {
                    assertEquals(5, new HelloPit().returnInputNumber(5));
                }
            }
        """.stripIndent()
    }

    //Deliberately not relying on GradleRunner#withPluginClasspath()'s no-arg auto-detection (which
    //depends on the plugin-under-test-metadata file being exposed as a classpath *resource*) - read the
    //file the 'pluginUnderTestMetadata' task produces directly instead, so this test has no implicit
    //dependency on how the funcTest source set wires its runtime classpath.
    private static List<File> readPluginUnderTestClasspath() {
        File metadataFile = new File('build/pluginUnderTestMetadata/plugin-under-test-metadata.properties')
        assert metadataFile.exists(): "'pluginUnderTestMetadata' task output not found at ${metadataFile.absolutePath}"
        Properties properties = new Properties()
        metadataFile.withInputStream { InputStream is -> properties.load(is) }
        return properties.getProperty('implementation-classpath').split(File.pathSeparator).collect { String path -> new File(path) }
    }

}
