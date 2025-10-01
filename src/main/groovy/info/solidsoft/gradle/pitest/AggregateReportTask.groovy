package info.solidsoft.gradle.pitest

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.Incubating
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
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
import org.gradle.work.DisableCachingByDefault
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
@DisableCachingByDefault(because = "TODO")  //TODO: Issue detected by "validatePlugins" task after upgrade to Gradle 7 - TODO: Report issue or implement
abstract class AggregateReportTask extends DefaultTask {

    @OutputDirectory
    abstract DirectoryProperty getReportDir()

    @OutputFile
    abstract RegularFileProperty getReportFile()

    @SkipWhenEmpty
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract ConfigurableFileCollection getSourceDirs()

    @SkipWhenEmpty
    @InputFiles
    @Classpath
    abstract ConfigurableFileCollection getAdditionalClasspath()

    @SkipWhenEmpty
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract ConfigurableFileCollection getMutationFiles()

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract ConfigurableFileCollection getLineCoverageFiles()

    //Stricter isolation level - https://docs.gradle.org/nightly/userguide/worker_api.html#changing_the_isolation_mode
    @InputFiles
    @Classpath
    abstract ConfigurableFileCollection getPitestReportClasspath()

    @Input
    @Optional
    abstract Property<Charset> getInputCharset()

    @Input
    @Optional
    abstract Property<Charset> getOutputCharset()

    @Input
    @Optional
    abstract Property<Integer> getTestStrengthThreshold()

    @Input
    @Optional
    abstract Property<Integer> getMutationThreshold()

    @Input
    @Optional
    abstract Property<Integer> getMaxSurviving()

    @Inject
    abstract WorkerExecutor getWorkerExecutor()

    @TaskAction
    void aggregate() {
        logger.info("Aggregating pitest reports (mutationFiles: {}, lineCoverageFiles: {})", mutationFiles.elements.getOrNull(), lineCoverageFiles.elements.getOrNull())

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
            parameters.testStrengthThreshold.set(this.testStrengthThreshold)
            parameters.mutationThreshold.set(this.mutationThreshold)
            parameters.maxSurviving.set(this.maxSurviving)
        }
    }

}
