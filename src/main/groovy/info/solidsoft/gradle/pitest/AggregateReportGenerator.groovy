package info.solidsoft.gradle.pitest

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.gradle.api.Incubating
import org.gradle.workers.WorkAction
import org.pitest.aggregate.ReportAggregator
import org.pitest.mutationtest.config.DirectoryResultOutputStrategy
import org.pitest.mutationtest.config.UndatedReportDirCreationStrategy

@Slf4j
@Incubating
@CompileStatic
@SuppressWarnings('UnstableApiUsage')
abstract class AggregateReportGenerator implements WorkAction<AggregateReportWorkParameters> {

    @Override
    void execute() {
        ReportAggregator.Builder builder = ReportAggregator.builder()

        parameters.lineCoverageFiles.each { file -> builder.addLineCoverageFile(file) }
        parameters.mutationFiles.each { file -> builder.addMutationResultsFile(file) }
        parameters.sourceDirs.each { file -> builder.addSourceCodeDirectory(file) }
        parameters.additionalClasspath.each { file -> builder.addCompiledCodeDirectory(file) }

        ReportAggregator aggregator = builder.resultOutputStrategy(new DirectoryResultOutputStrategy(
            parameters.reportDir.asFile.get().absolutePath,
            new UndatedReportDirCreationStrategy()))
            .build()
        aggregator.aggregateReport()

        log.info("Aggregated report ${parameters.reportFile.asFile.get().absolutePath}")
    }

}
