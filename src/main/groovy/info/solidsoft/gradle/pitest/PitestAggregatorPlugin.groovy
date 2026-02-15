package info.solidsoft.gradle.pitest

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import info.solidsoft.gradle.pitest.internal.GradleVersionEnforcer
import org.gradle.api.Incubating
import org.gradle.api.NamedDomainObjectProvider
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

    PitestAggregatorPlugin() {
        this.gradleVersionEnforcer = GradleVersionEnforcer.defaultEnforcer(PitestPlugin.MINIMAL_SUPPORTED_GRADLE_VERSION)
    }

    @Override
    void apply(Project project) {
        gradleVersionEnforcer.failBuildWithMeaningfulErrorIfAppliedOnTooOldGradleVersion(project)

        NamedDomainObjectProvider<Configuration> pitestReportConfiguration = registerPitestReportConfiguration(project)
        addPitAggregateReportDependency(project, pitestReportConfiguration)

        NamedDomainObjectProvider<Configuration> pitestAggregation = registerPitestAggregationConfiguration(project)
        configureAggregateReportTask(project, pitestReportConfiguration, pitestAggregation)
    }

    private static NamedDomainObjectProvider<Configuration> registerPitestReportConfiguration(Project project) {
        return project.configurations.register(PITEST_REPORT_AGGREGATE_CONFIGURATION_NAME) { Configuration configuration ->
            configuration.attributes.attribute(Usage.USAGE_ATTRIBUTE,
                (Usage) project.objects.named(Usage, Usage.JAVA_RUNTIME))
            configuration.visible = false
            configuration.canBeConsumed = false
            configuration.canBeResolved = true
        }
    }

    private static void configureAggregateReportTask(Project project,
                                                     NamedDomainObjectProvider<Configuration> pitestReportConfiguration,
                                                     NamedDomainObjectProvider<Configuration> pitestAggregation) {
        project.tasks.register(PITEST_REPORT_AGGREGATE_TASK_NAME, AggregateReportTask) { t ->
            t.description = "Aggregate PIT reports"
            t.group = PitestPlugin.PITEST_TASK_GROUP
            t.pitestReportClasspath.from(pitestReportConfiguration)
            configureTaskDefaults(project, t, pitestAggregation)
        }
    }

    private static NamedDomainObjectProvider<Configuration> registerPitestAggregationConfiguration(Project project) {
        NamedDomainObjectProvider<Configuration> configurationProvider = project.configurations.register(
                PITEST_AGGREGATION_CONFIGURATION_NAME) { Configuration configuration ->
            configuration.visible = false
            configuration.canBeConsumed = false
            configuration.canBeResolved = true
            configuration.transitive = false
        }

        configurationProvider.configure { Configuration configuration ->
            configuration.defaultDependencies { dependencies ->
                // Include root project if it has both pitest and java plugins applied
                if (project.pluginManager.hasPlugin(PitestPlugin.PLUGIN_ID)
                        && project.pluginManager.hasPlugin("java")) {
                    dependencies.add(project.dependencies.create(project))
                }

                // Include all subprojects with both pitest and java plugins applied
                project.subprojects.each { Project subproject ->
                    if (subproject.pluginManager.hasPlugin(PitestPlugin.PLUGIN_ID)
                            && subproject.pluginManager.hasPlugin("java")) {
                        dependencies.add(project.dependencies.create(subproject))
                    }
                }
            }
        }
        return configurationProvider
    }

    private static void configureTaskDefaults(Project project, AggregateReportTask task,
                                              NamedDomainObjectProvider<Configuration> pitestAggregation) {
        task.reportDir.convention(getReportBaseDirectory(project).map { Directory d ->
            d.dir(PitestPlugin.PITEST_REPORT_DIRECTORY_NAME)
        })
        task.reportFile.convention(task.reportDir.file("index.html"))
        task.sourceDirs.from(getArtifactFiles(project, pitestAggregation.get(), PitestAttributes.SOURCES))

        FileCollection reportFiles = getArtifactFiles(project, pitestAggregation.get(), PitestAttributes.REPORT)
        task.mutationFiles.from(filterMutationFiles(reportFiles))
        task.lineCoverageFiles.from(filterLineCoverageFiles(reportFiles))
        task.additionalClasspath.from(getArtifactFiles(project, pitestAggregation.get(), PitestAttributes.CLASSES))

        project.pluginManager.withPlugin(PitestPlugin.PLUGIN_ID) {
            PitestPluginExtension extension = project.extensions.findByType(PitestPluginExtension)
            if (extension != null) {
                task.inputCharset.set(extension.inputCharset)
                task.outputCharset.set(extension.outputCharset)
                task.testStrengthThreshold.set(extension.reportAggregatorProperties.testStrengthThreshold)
                task.mutationThreshold.set(extension.reportAggregatorProperties.mutationThreshold)
                task.maxSurviving.set(extension.reportAggregatorProperties.maxSurviving)
            }
        }
    }

    private static FileCollection getArtifactFiles(Project project, Configuration configuration, String artifactType) {
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

    private static void addPitAggregateReportDependency(Project project,
                                                        NamedDomainObjectProvider<Configuration> pitestReportConfiguration) {
        pitestReportConfiguration.configure { Configuration configuration ->
            configuration.withDependencies { dependencies ->
                PitestPluginExtension extension = project.extensions.findByType(PitestPluginExtension)
                String pitestVersion = extension != null ? extension.pitestVersion.get() : PitestPlugin.DEFAULT_PITEST_VERSION

                dependencies.add(project.dependencies.create("org.pitest:pitest-aggregator:$pitestVersion"))
                dependencies.add(project.dependencies.create("org.pitest:pitest-html-report:$pitestVersion"))
            }
        }
    }

    private static Provider<Directory> getReportBaseDirectory(Project project) {
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
