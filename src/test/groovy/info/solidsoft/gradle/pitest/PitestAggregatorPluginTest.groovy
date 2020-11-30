package info.solidsoft.gradle.pitest

import groovy.transform.CompileDynamic
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

@CompileDynamic
@SuppressWarnings("PrivateFieldCouldBeFinal")
class PitestAggregatorPluginTest extends Specification {

    private Project project = ProjectBuilder.builder().build()

    void "add aggregate report task to project in proper group"() {
        when:
            project.pluginManager.apply(PitestAggregatorPlugin.PLUGIN_ID)
        then:
            project.plugins.hasPlugin(PitestAggregatorPlugin)
            assertThatTasksAreInGroup([PitestAggregatorPlugin.PITEST_REPORT_AGGREGATE_TASK_NAME], PitestPlugin.PITEST_TASK_GROUP)
    }

    private void assertThatTasksAreInGroup(List<String> taskNames, String group) {
        taskNames.each { String taskName ->
            Task task = project.tasks[taskName]
            assert task.group == group
        }
    }

}
