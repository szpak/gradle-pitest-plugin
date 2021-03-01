package info.solidsoft.gradle.pitest

import org.gradle.api.Incubating
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.workers.WorkParameters

@Incubating
interface AggregateReportWorkParameters extends WorkParameters {

    DirectoryProperty getReportDir()
    RegularFileProperty getReportFile()
    ConfigurableFileCollection getSourceDirs()
    ConfigurableFileCollection getAdditionalClasspath()
    ConfigurableFileCollection getMutationFiles()
    ConfigurableFileCollection getLineCoverageFiles()

}
