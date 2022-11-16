package info.solidsoft.gradle.pitest

import org.gradle.api.Incubating
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters

import java.nio.charset.Charset

@Incubating
interface AggregateReportWorkParameters extends WorkParameters {

    DirectoryProperty getReportDir()
    RegularFileProperty getReportFile()
    ConfigurableFileCollection getSourceDirs()
    ConfigurableFileCollection getAdditionalClasspath()
    ConfigurableFileCollection getMutationFiles()
    ConfigurableFileCollection getLineCoverageFiles()
    Property<Charset> getInputCharset()
    Property<Charset> getOutputCharset()
    Property<Integer> getAggregatedTestStrengthThreshold()
    Property<Integer> getAggregatedMutationThreshold()
    Property<Integer> getAggregatedMaxSurviving()

}
