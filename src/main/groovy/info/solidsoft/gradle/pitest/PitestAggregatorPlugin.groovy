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
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.file.FileCollection
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.plugins.JavaPlugin

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
    public static final String PITEST_AGGREGATION_CONFIGURATION_NAME = "pitestAggregation"
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

        Configuration pitestAggregation = createPitestAggregationConfiguration()
        configureAggregateReportTask(pitestReportConfiguration, pitestAggregation)
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

    private void configureAggregateReportTask(Configuration pitestReportConfiguration, Configuration pitestAggregation) {
        project.tasks.register(PITEST_REPORT_AGGREGATE_TASK_NAME, AggregateReportTask) { t ->
            t.description = "Aggregate PIT reports"
            t.group = PitestPlugin.PITEST_TASK_GROUP
            t.pitestReportClasspath.from(pitestReportConfiguration)
            configureTaskDefaults(t, pitestAggregation)
        }
    }

    private Configuration createPitestAggregationConfiguration() {
        Configuration configuration = project.configurations.create(PITEST_AGGREGATION_CONFIGURATION_NAME).with { configuration ->
            visible = false
            canBeConsumed = false
            canBeResolved = true
            transitive = false
            return configuration
        }

        configuration.defaultDependencies { dependencies ->
            // Include root project if it has both pitest and java plugins
            if (project.plugins.hasPlugin(PitestPlugin.PLUGIN_ID) && project.plugins.hasPlugin(JavaPlugin)) {
                dependencies.add(project.dependencies.create(project))
            }

            // Include all subprojects with both pitest and java plugins
            project.subprojects.each { subproject ->
                if (subproject.plugins.hasPlugin(PitestPlugin.PLUGIN_ID) && subproject.plugins.hasPlugin(JavaPlugin)) {
                    dependencies.add(project.dependencies.create(subproject))
                }
            }
        }
        return configuration
    }

    private void configureTaskDefaults(AggregateReportTask task, Configuration pitestAggregation) {
        task.reportDir.convention(getReportBaseDirectory().map { Directory d ->
            d.dir(PitestPlugin.PITEST_REPORT_DIRECTORY_NAME)
        })
        task.reportFile.convention(task.reportDir.file("index.html"))
        task.sourceDirs.from = getArtifactFiles(pitestAggregation, PitestAttributes.SOURCES)

        FileCollection reportFiles = getArtifactFiles(pitestAggregation, PitestAttributes.REPORT)
        task.mutationFiles.from = filterMutationFiles(reportFiles)
        task.lineCoverageFiles.from = filterLineCoverageFiles(reportFiles)
        task.additionalClasspath.from = getArtifactFiles(pitestAggregation, PitestAttributes.CLASSES)

        findPluginExtension().ifPresent({ PitestPluginExtension extension ->
            task.inputCharset.set(extension.inputCharset)
            task.outputCharset.set(extension.outputCharset)
            task.testStrengthThreshold.set(extension.reportAggregatorProperties.testStrengthThreshold)
            task.mutationThreshold.set(extension.reportAggregatorProperties.mutationThreshold)
            task.maxSurviving.set(extension.reportAggregatorProperties.maxSurviving)
        } as Consumer<PitestPluginExtension>)   //Simplify with Groovy 3+
    }

    private FileCollection getArtifactFiles(Configuration configuration, String artifactType) {
        return configuration.incoming.artifactView { view ->
            view.withVariantReselection()
            view.componentFilter { id -> id in ProjectComponentIdentifier }
            view.attributes { attributes ->
                attributes.attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category, Category.VERIFICATION))
                attributes.attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage, "pitest"))
                attributes.attribute(PitestAttributes.ARTIFACT_TYPE, artifactType)
            }
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
