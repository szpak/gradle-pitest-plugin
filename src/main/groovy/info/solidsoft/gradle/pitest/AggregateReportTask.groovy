package info.solidsoft.gradle.pitest

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.Incubating
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject
import java.nio.charset.Charset

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

    @Input
    @Optional
    final Property<Charset> inputCharset

    @Input
    @Optional
    final Property<Charset> outputCharset

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
        inputCharset = of.property(Charset)
        outputCharset = of.property(Charset)
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
            parameters.inputCharset.set(this.inputCharset)
            parameters.outputCharset.set(this.outputCharset)
        }
    }

}
