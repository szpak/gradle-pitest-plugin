package info.solidsoft.gradle.pitest

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.gradle.api.GradleException
import org.gradle.api.Incubating
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.pitest.aggregate.AggregationResult
import org.pitest.aggregate.ReportAggregator
import org.pitest.mutationtest.config.DirectoryResultOutputStrategy
import org.pitest.mutationtest.config.UndatedReportDirCreationStrategy

import java.util.function.Consumer

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

        consumeIfPropertyIsSet(parameters.inputCharset) { charset -> builder.inputCharSet(charset) }
        consumeIfPropertyIsSet(parameters.outputCharset) { charset -> builder.outputCharset(charset) }

        ReportAggregator aggregator = builder.resultOutputStrategy(new DirectoryResultOutputStrategy(
            parameters.reportDir.asFile.get().absolutePath,
            new UndatedReportDirCreationStrategy()))
            .build()
        AggregationResult aggregationResult = aggregator.aggregateReport()

        log.info("Aggregated report ${parameters.reportFile.asFile.get().absolutePath}")

        consumeIfPropertyIsSet(parameters.aggregatedTestStrengthThreshold) { threshold ->
            if (aggregationResult.testStrength < threshold) {
                throw new GradleException(
                    "Aggregated test strength score of ${aggregationResult.testStrength} " +
                        "is below threshold of $threshold"
                )
            }
        }

        consumeIfPropertyIsSet(parameters.aggregatedMutationThreshold) { threshold ->
            if (aggregationResult.mutationCoverage < threshold) {
                throw new GradleException(
                    "Aggregated mutation score of ${aggregationResult.mutationCoverage} " +
                        "is below threshold of $threshold"
                )
            }
        }

        consumeIfPropertyIsSet(parameters.aggregatedMaxSurviving) { threshold ->
            if (aggregationResult.mutationsSurvived > threshold) {
                throw new GradleException(
                    "Had ${aggregationResult.mutationsSurvived} " +
                        "surviving mutants, but only $threshold survivors allowed"
                )
            }
        }
    }

    private static <T> void consumeIfPropertyIsSet(Property<T> property, Consumer<T> applyPropertyCode) {
        if (property.isPresent()) {
            applyPropertyCode.accept(property.get())
        }
    }

}
