package info.solidsoft.gradle.pitest

import groovy.transform.CompileStatic
import info.solidsoft.gradle.pitest.internal.GradleVersionEnforcer
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * The plugin to aggregate pitest subprojects reports
 */
@CompileStatic
class PitestAggregatorPlugin implements Plugin<Project> {

    public static final String PLUGIN_ID = "info.solidsoft.pitest.aggregator"
    public static final String PITEST_REPORT_AGGREGATE_TASK_NAME = "pitestReportAggregate"

    private final GradleVersionEnforcer gradleVersionEnforcer
    private Project project

    PitestAggregatorPlugin() {
        this.gradleVersionEnforcer = GradleVersionEnforcer.defaultEnforcer(PitestPlugin.MINIMAL_SUPPORTED_GRADLE_VERSION)
    }

    @Override
    void apply(Project project) {
        this.project = project
        gradleVersionEnforcer.failBuildWithMeaningfulErrorIfAppliedOnTooOldGradleVersion(project)

        project.tasks.register(PITEST_REPORT_AGGREGATE_TASK_NAME, AggregateReportTask) { t ->
            t.description = "Aggregate PIT reports"
            t.group = PitestPlugin.PITEST_TASK_GROUP
            //shouldRunAfter should be enough, but it fails in functional tests as :pitestReportAggregate is executed before :pitest tasks from subprojects
            t.mustRunAfter(project.allprojects.collect { Project p -> p.tasks.withType(PitestTask) })
        }
    }

}
