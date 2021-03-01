package info.solidsoft.gradle.pitest

import groovy.transform.CompileStatic
import info.solidsoft.gradle.pitest.internal.GradleVersionEnforcer
import org.gradle.api.Incubating
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Usage
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.TaskCollection

import java.util.stream.Collectors

/**
 * The plugin to aggregate pitest subprojects reports
 *
 * @since 1.6.0
 */
@Incubating
@CompileStatic
class PitestAggregatorPlugin implements Plugin<Project> {

    public static final String PLUGIN_ID = "info.solidsoft.pitest.aggregator"
    public static final String PITEST_REPORT_AGGREGATE_TASK_NAME = "pitestReportAggregate"

    private static final String MUTATION_FILE_NAME = "mutations.xml"
    private static final String LINE_COVERAGE_FILE_NAME = "linecoverage.xml"

    private final GradleVersionEnforcer gradleVersionEnforcer
    private Project project

    PitestAggregatorPlugin() {
        this.gradleVersionEnforcer = GradleVersionEnforcer.defaultEnforcer(PitestPlugin.MINIMAL_SUPPORTED_GRADLE_VERSION)
    }

    @Override
    void apply(Project project) {
        this.project = project
        gradleVersionEnforcer.failBuildWithMeaningfulErrorIfAppliedOnTooOldGradleVersion(project)

        Configuration pitestReportConfiguration = project.configurations.create('pitestReport').with { configuration ->
            attributes.attribute(Usage.USAGE_ATTRIBUTE, (Usage) project.objects.named(Usage, Usage.JAVA_RUNTIME))
            visible = false
            canBeConsumed = false
            canBeResolved = true
            return configuration
        }

        project.tasks.register(PITEST_REPORT_AGGREGATE_TASK_NAME, AggregateReportTask) { t ->
            t.description = "Aggregate PIT reports"
            t.group = PitestPlugin.PITEST_TASK_GROUP
            configureTaskDefaults(t)
            //shouldRunAfter should be enough, but it fails in functional tests as :pitestReportAggregate is executed before :pitest tasks from subprojects
            t.mustRunAfter(project.allprojects.collect { Project p -> p.tasks.withType(PitestTask) })
            addPitAggregateReportDependency(pitestReportConfiguration)
            t.pitestReportClasspath.from(pitestReportConfiguration)
        }
    }

    private void configureTaskDefaults(AggregateReportTask aggregateReportTask) {
        aggregateReportTask.with {
            reportDir.set(new File(getReportBaseDirectory(), PitestPlugin.PITEST_REPORT_DIRECTORY_NAME))
            reportFile.set(reportDir.file("index.html"))

            List<TaskCollection<PitestTask>> pitestTasks = getAllPitestTasks()
            sourceDirs.from = collectSourceDirs(pitestTasks)
            additionalClasspath.from = collectClasspathDirs(pitestTasks)

            Set<Project> projectsWithPitest = getProjectsWithPitestPlugin()
            mutationFiles.from = collectMutationFiles(projectsWithPitest)
            lineCoverageFiles.from = collectLineCoverageFiles(projectsWithPitest)
        }
    }

    private void addPitAggregateReportDependency(Configuration pitestReportConfiguration) {
        Optional<PitestPluginExtension> maybeExtension = Optional.ofNullable(project.extensions.findByType(PitestPluginExtension))
            .map { extension -> Optional.of(extension) }   //Optional::of with Groovy 3
            .orElseGet { findPitestExtensionInSubprojects(project) }
        String pitestVersion = maybeExtension
            .map { extension -> extension.pitestVersion.get() }
            .orElse(PitestPlugin.DEFAULT_PITEST_VERSION)

        pitestReportConfiguration.dependencies.add(project.dependencies.create("org.pitest:pitest-aggregator:$pitestVersion"))
    }

    private File getReportBaseDirectory() {
        if (project.extensions.findByType(ReportingExtension)) {
            return project.extensions.getByType(ReportingExtension).baseDir
        }
        return new File(project.buildDir, "reports")
    }

    private Set<Project> getProjectsWithPitestPlugin() {
        return project.allprojects.findAll { prj -> prj.plugins.hasPlugin(PitestPlugin.PLUGIN_ID) }
    }

    private List<TaskCollection<PitestTask>> getAllPitestTasks() {
        return project.allprojects.collect { p -> p.tasks.withType(PitestTask) }
    }

    private static List<ConfigurableFileCollection> collectSourceDirs(List<TaskCollection<PitestTask>> pitestTasks) {
        return pitestTasks.stream()
            .flatMap { tc ->
                tc.stream()
                    .map { task -> task.sourceDirs }
            }.collect(Collectors.toList())
    }

    private static List<ConfigurableFileCollection> collectClasspathDirs(List<TaskCollection<PitestTask>> pitestTasks) {
        return pitestTasks.stream()
            .flatMap { tc ->
                tc.stream()
                    .map { task -> task.additionalClasspath }
                    .map { cfc -> cfc.filter { File f -> f.isDirectory() } }
            }.collect(Collectors.toList())
    }

    private static Set<Provider<File>> collectMutationFiles(Set<Project> pitestProjects) {
        return pitestProjects.stream()
            .map { prj -> prj.extensions.getByType(PitestPluginExtension) }
            .map { extension -> extension.reportDir.file(MUTATION_FILE_NAME) }
            .collect(Collectors.toSet())
    }

    private static Set<Provider<File>> collectLineCoverageFiles(Set<Project> pitestProjects) {
        return pitestProjects.stream()
            .map { prj -> prj.extensions.getByType(PitestPluginExtension) }
            .map { extension -> extension.reportDir.file(LINE_COVERAGE_FILE_NAME) }
            .collect(Collectors.toSet())
    }

    private static Optional<PitestPluginExtension> findPitestExtensionInSubprojects(Project project) {
        return project.subprojects.stream()
            .map { subproject -> subproject.extensions.findByType(PitestPluginExtension) }
            .filter { extension -> extension != null }
            .findFirst()
    }

}
