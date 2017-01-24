package info.solidsoft.gradle.pitest

import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static info.solidsoft.gradle.pitest.PitestPlugin.PITEST_TASK_NAME

@PackageScope
abstract class BasicProjectBuilderSpec extends Specification {

    @Rule
    public TemporaryFolder tmpProjectDir = new TemporaryFolder()

    protected Project project
    protected PitestPluginExtension pitestConfig

    //TODO: There is a regression in 2.14.1 with API jar regeneration for every test - https://discuss.gradle.org/t/performance-regression-in-projectbuilder-in-2-14-and-3-0/18956
    //https://github.com/gradle/gradle/commit/3216f07b3acb4cbbb8241d8a1d50b8db9940f37e
    def setup() {
        project = ProjectBuilder.builder().withProjectDir(tmpProjectDir.root).build()

        project.apply(plugin: "java")   //to add SourceSets
        project.apply(plugin: "info.solidsoft.pitest")

        pitestConfig = project.getExtensions().getByType(PitestPluginExtension)

        project.group = 'test.group'
    }

    protected PitestTask getJustOnePitestTaskOrFail() {
        Set<Task> tasks = project.getTasksByName(PITEST_TASK_NAME, false) //forces "afterEvaluate"
        assert tasks?.size() == 1 : "Expected tasks: '$PITEST_TASK_NAME', All tasks: ${project.tasks}"
        assert tasks[0] instanceof PitestTask
        return (PitestTask)tasks[0]
    }
}
