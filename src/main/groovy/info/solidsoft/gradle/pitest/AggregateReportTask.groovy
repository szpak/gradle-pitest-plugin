package info.solidsoft.gradle.pitest

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.pitest.aggregate.ReportAggregator
import org.pitest.mutationtest.config.DirectoryResultOutputStrategy
import org.pitest.mutationtest.config.UndatedReportDirCreationStrategy

import java.util.stream.Collectors

/**
 * Task to aggregate pitest report
 */
@CompileStatic
class AggregateReportTask extends DefaultTask {

    private static final String MUTATION_FILE_NAME = "mutations.xml";
    private static final String LINE_COVERAGE_FILE_NAME = "linecoverage.xml";

    @OutputDirectory
    final DirectoryProperty reportDir

    @OutputFile
    final RegularFileProperty reportFile

    @SkipWhenEmpty
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    final ConfigurableFileCollection sourceDirs

    @SkipWhenEmpty
    @InputFiles
    @Classpath
    final ConfigurableFileCollection additionalClasspath

    @SkipWhenEmpty
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    final ConfigurableFileCollection mutationFiles

    @SkipWhenEmpty
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    final ConfigurableFileCollection lineCoverageFiles

    AggregateReportTask() {
        reportDir = project.objects.directoryProperty()
        reportDir.set(new File(getReportBaseDirectory(), PitestPlugin.PITEST_REPORT_DIRECTORY_NAME))

        reportFile = project.objects.fileProperty()
        reportFile.set(reportDir.file("index.html"))

        Set<PitestTask> pitestTasks = getAllPitestTasks()

        sourceDirs = project.objects.fileCollection()
        sourceDirs.from = collectSourceDirs(pitestTasks)

        additionalClasspath = project.objects.fileCollection()
        additionalClasspath.from = collectClasspathDirs(pitestTasks)

        Set<Project> projectsWithPitest = getProjectsWithPitestPlugin()

        mutationFiles = project.objects.fileCollection()
        mutationFiles.from = collectMutationFiles(projectsWithPitest)

        lineCoverageFiles = project.objects.fileCollection()
        lineCoverageFiles.from = collectLineCoverageFiles(projectsWithPitest)
    }

    @TaskAction
    void aggregate() {
        logger.info("Aggregating pitest reports")

        ReportAggregator.Builder builder = ReportAggregator.builder()

        lineCoverageFiles.each { file -> builder.addLineCoverageFile(file) }
        mutationFiles.each { file -> builder.addMutationResultsFile(file) }
        sourceDirs.each { file -> builder.addSourceCodeDirectory(file) }
        additionalClasspath.each { file -> builder.addCompiledCodeDirectory(file) }

        ReportAggregator aggregator = builder.resultOutputStrategy(new DirectoryResultOutputStrategy(
            reportDir.asFile.get().absolutePath,
            new UndatedReportDirCreationStrategy()))
            .build()
        aggregator.aggregateReport()

        logger.info("Aggregated report ${reportFile.asFile.get().absolutePath}")
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

    DirectoryProperty getReportDir() {
        return reportDir
    }

    ConfigurableFileCollection getAdditionalClasspath() {
        return additionalClasspath
    }

    ConfigurableFileCollection getMutationFiles() {
        return mutationFiles
    }

    ConfigurableFileCollection getLineCoverageFiles() {
        return lineCoverageFiles
    }

    ConfigurableFileCollection getSourceDirs() {
        return sourceDirs
    }

    private File getReportBaseDirectory() {
        // if Java plugin configured on root project
        if (project.extensions.findByType(ReportingExtension)) {
            return project.extensions.getByType(ReportingExtension).baseDir
        }
        return new File(project.buildDir, "reports")
    }

    private Set<Project> getProjectsWithPitestPlugin() {
        Set<Project> projects = [] as Set
        if (isRootPitestConfigured()) {
            projects.add(project)
        }
        projects.addAll(getSubprojectsWithPitest())
        return projects
    }

    private boolean isRootPitestConfigured() {
        return project.plugins.hasPlugin(PitestPlugin.PLUGIN_ID) && project.extensions.findByType(PitestPluginExtension)
    }

    private Set<Project> getSubprojectsWithPitest() {
        return project.subprojects.findAll { prj -> prj.plugins.hasPlugin(PitestPlugin.PLUGIN_ID) }
    }

    private Set<PitestTask> getAllPitestTasks() {
        return project.getTasksByName(PitestPlugin.PITEST_TASK_NAME, true) as Set<PitestTask>
    }

    private Set<Provider<Set<File>>> collectSourceDirs(Set<PitestTask> pitestTasks) {
        return pitestTasks.stream()
            .map { task -> task.sourceDirs }
            .map { cfc -> project.provider { cfc.files.findAll { f -> f.isDirectory() } } }
            .collect(Collectors.toSet())
    }

    private Set<Provider<Set<File>>> collectClasspathDirs(Set<PitestTask> pitestTasks) {
        return pitestTasks.stream()
            .map { task -> task.additionalClasspath }
            .map { cfc -> project.provider { cfc.files.findAll { f -> f.isDirectory() } } }
            .collect(Collectors.toSet())
    }

}
