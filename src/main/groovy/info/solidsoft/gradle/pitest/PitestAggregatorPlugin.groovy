package info.solidsoft.gradle.pitest

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import info.solidsoft.gradle.pitest.internal.GradleVersionEnforcer
import org.gradle.api.Incubating
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.Usage
import org.gradle.api.file.FileCollection
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.ReportingExtension

import java.util.function.Consumer

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
    public static final String PITEST_REPORT_AGGREGATION_CONFIGURATION_NAME = "pitestReportAggregation"
    public static final String PITEST_SOURCES_AGGREGATION_CONFIGURATION_NAME = "pitestSourcesAggregation"
    public static final String PITEST_CLASSES_AGGREGATION_CONFIGURATION_NAME = "pitestClassesAggregation"
    //visibility for testing
    @PackageScope static final String PITEST_REPORT_AGGREGATE_CONFIGURATION_NAME = "pitestReport"

    private final GradleVersionEnforcer gradleVersionEnforcer
    private Project project

    PitestAggregatorPlugin() {
        this.gradleVersionEnforcer = GradleVersionEnforcer.defaultEnforcer(PitestPlugin.MINIMAL_SUPPORTED_GRADLE_VERSION)
    }

    @Override
    void apply(Project project) {
        this.project = project
        gradleVersionEnforcer.failBuildWithMeaningfulErrorIfAppliedOnTooOldGradleVersion(project)

        Configuration pitestReportConfiguration = createPitestReportConfiguration()
        addPitAggregateReportDependency(pitestReportConfiguration)

        Configuration pitestReportAggregation = createAggregationConfiguration(
            PITEST_REPORT_AGGREGATION_CONFIGURATION_NAME, PitestAttributes.REPORT
        )
        Configuration pitestSourcesAggregation = createAggregationConfiguration(
            PITEST_SOURCES_AGGREGATION_CONFIGURATION_NAME, PitestAttributes.SOURCES
        )
        Configuration pitestClassesAggregation = createAggregationConfiguration(
            PITEST_CLASSES_AGGREGATION_CONFIGURATION_NAME, PitestAttributes.CLASSES
        )

        configureAggregateReportTask(
            pitestReportConfiguration, pitestReportAggregation, pitestSourcesAggregation, pitestClassesAggregation
        )
    }

    private Configuration createPitestReportConfiguration() {
        return project.configurations.create(PITEST_REPORT_AGGREGATE_CONFIGURATION_NAME).with { configuration ->
            attributes.attribute(Usage.USAGE_ATTRIBUTE, (Usage) project.objects.named(Usage, Usage.JAVA_RUNTIME))
            visible = false
            canBeConsumed = false
            canBeResolved = true
            return configuration
        }
    }

    private void configureAggregateReportTask(Configuration pitestReportConfiguration, Configuration pitestReportAggregation,
                                              Configuration pitestSourcesAggregation, Configuration pitestClassesAggregation) {
        project.tasks.register(PITEST_REPORT_AGGREGATE_TASK_NAME, AggregateReportTask) { t ->
            t.description = "Aggregate PIT reports"
            t.group = PitestPlugin.PITEST_TASK_GROUP
            t.pitestReportClasspath.from(pitestReportConfiguration)

            configureTaskDefaults(t, pitestReportAggregation, pitestSourcesAggregation, pitestClassesAggregation)
        }
    }

    private Configuration createAggregationConfiguration(String name, String artifactType) {
        Configuration configuration = project.configurations.create(name).with { configuration ->
             visible = false
             canBeConsumed = false
             canBeResolved = true
             configuration.attributes.attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category, Category.VERIFICATION))
             configuration.attributes.attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage, "pitest"))
             configuration.attributes.attribute(PitestAttributes.ARTIFACT_TYPE, artifactType)
             return configuration
        }

        project.subprojects.each { subproject ->
            project.dependencies.add(name, subproject)
        }
        return configuration
    }

    private void configureTaskDefaults(AggregateReportTask task, Configuration pitestReportAggregation,
                                       Configuration pitestSourcesAggregation, Configuration pitestClassesAggregation) {
        task.reportDir.convention(getReportBaseDirectory().map { Directory d ->
            d.dir(PitestPlugin.PITEST_REPORT_DIRECTORY_NAME)
        })
        task.reportFile.convention(task.reportDir.file("index.html"))
        task.sourceDirs.from = getLenientArtifactFiles(pitestSourcesAggregation)

        FileCollection reportFiles = getLenientArtifactFiles(pitestReportAggregation)
        task.mutationFiles.from = filterMutationFiles(reportFiles)
        task.lineCoverageFiles.from = filterLineCoverageFiles(reportFiles)
        task.additionalClasspath.from = getLenientArtifactFiles(pitestClassesAggregation)

        findPluginExtension().ifPresent({ PitestPluginExtension extension ->
            task.inputCharset.set(extension.inputCharset)
            task.outputCharset.set(extension.outputCharset)
            task.testStrengthThreshold.set(extension.reportAggregatorProperties.testStrengthThreshold)
            task.mutationThreshold.set(extension.reportAggregatorProperties.mutationThreshold)
            task.maxSurviving.set(extension.reportAggregatorProperties.maxSurviving)
        } as Consumer<PitestPluginExtension>)   //Simplify with Groovy 3+
    }

    private static FileCollection getLenientArtifactFiles(Configuration configuration) {
        return configuration.incoming.artifactView { view ->
            // Lenient is required to ignore dependencies (subprojects) that do not have the Pitest plugin applied
            // and thus do not match the requested attributes (Category/Usage).
            // Without this, the build would fail if any subproject is not configured for Pitest.
            view.lenient(true)
        }.files
    }

    private void addPitAggregateReportDependency(Configuration pitestReportConfiguration) {
        pitestReportConfiguration.withDependencies { dependencies ->
            String pitestVersion = findPluginExtension()
                .map { extension -> extension.pitestVersion.get() }
                .orElse(PitestPlugin.DEFAULT_PITEST_VERSION)

            dependencies.add(project.dependencies.create("org.pitest:pitest-aggregator:$pitestVersion"))
            dependencies.add(project.dependencies.create("org.pitest:pitest-html-report:$pitestVersion"))
        }
    }

    private Optional<PitestPluginExtension> findPluginExtension() {
        return Optional.ofNullable(project.extensions.findByType(PitestPluginExtension))
    }

    private Provider<Directory> getReportBaseDirectory() {
        ReportingExtension reportingExtension = project.extensions.findByType(ReportingExtension)
        if (reportingExtension) {
            return reportingExtension.baseDirectory
        }
        return project.layout.buildDirectory.dir("reports")
    }

    private static FileCollection filterMutationFiles(FileCollection reportFiles) {
        return reportFiles.filter { File f -> f.name == PitestAttributes.MUTATION_FILE_NAME }
    }

    private static FileCollection filterLineCoverageFiles(FileCollection reportFiles) {
       return reportFiles.filter { File f -> f.name == PitestAttributes.LINE_COVERAGE_FILE_NAME }
    }

}
