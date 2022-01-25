package pl.droidsonroids.gradle.pitest

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.Incubating
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.*
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject

/**
 * Task to aggregate pitest report
 *
 * @since 1.6.0
 */
@Incubating
@CompileStatic
abstract class AggregateReportTask extends DefaultTask {

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

    //Stricter isolation level - https://docs.gradle.org/nightly/userguide/worker_api.html#changing_the_isolation_mode
    @InputFiles
    @Classpath
    abstract ConfigurableFileCollection getPitestReportClasspath()

    @Inject
    abstract WorkerExecutor getWorkerExecutor()

    AggregateReportTask() {
        ObjectFactory of = project.objects
        reportDir = of.directoryProperty()
        reportFile = of.fileProperty()
        sourceDirs = of.fileCollection()
        additionalClasspath = of.fileCollection()
        mutationFiles = of.fileCollection()
        lineCoverageFiles = of.fileCollection()
    }

    @TaskAction
    void aggregate() {
        logger.info("Aggregating pitest reports")

        WorkQueue workQueue = getWorkerExecutor().classLoaderIsolation { workerSpec ->
            workerSpec.getClasspath().from(getPitestReportClasspath())
        }
        workQueue.submit(AggregateReportGenerator) { parameters ->
            parameters.reportDir.set(reportDir)
            parameters.reportFile.set(reportFile)
            parameters.sourceDirs.from(sourceDirs)
            parameters.additionalClasspath.from(additionalClasspath)
            parameters.mutationFiles.from(mutationFiles)
            parameters.lineCoverageFiles.from(lineCoverageFiles)
        }
    }

}
