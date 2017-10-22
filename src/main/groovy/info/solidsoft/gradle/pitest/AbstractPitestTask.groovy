package info.solidsoft.gradle.pitest;

import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AbstractPitestTask extends JavaExec {
    @OutputDirectory
    File reportDir

    @Input
    Set<String> targetClasses

    @Input
    @Optional
    Set<String> targetTests

    @Input
    @Optional
    Integer dependencyDistance

    @Input
    @Optional
    Integer threads

    @Input
    @Optional
    Boolean mutateStaticInits

    @Input
    @Optional
    Boolean includeJarFiles

    @Input
    @Optional
    Set<String> mutators

    @Input
    @Optional
    Set<String> excludedMethods

    @Input
    @Optional
    Set<String> excludedClasses

    @Input
    @Optional
    Set<String> avoidCallsTo

    @Input
    @Optional
    Boolean verbose

    @Input
    @Optional
    BigDecimal timeoutFactor

    @Input
    @Optional
    Integer timeoutConstInMillis

    @Input
    @Optional
    Integer maxMutationsPerClass

    @Input
    @Optional
    List<String> childProcessJvmArgs

    @Input
    @Optional
    Set<String> outputFormats

    @Input
    @Optional
    Boolean failWhenNoMutations

    @Input
    @Optional
    Set<String> includedGroups

    @Input
    @Optional
    Set<String> excludedGroups

    @InputFiles
    Set<File> sourceDirs

    @Input
    @Optional
    Boolean detectInlinedCode

    @Input
    @Optional
    Boolean timestampedReports

    @InputFiles
    FileCollection additionalClasspath    //"classpath" is already defined internally in ExecTask

    @Input
    Boolean useAdditionalClasspathFile

    @Input
    @OutputFile
    File additionalClasspathFile

    @InputFiles
    Set<File> mutableCodePaths

    @Input
    @Optional
    File historyInputLocation

    @OutputFile
    @Optional
    File historyOutputLocation

    @Input
    @Optional
    Boolean enableDefaultIncrementalAnalysis

    @Input
    File defaultFileForHistoryData

    @Input
    @Optional
    Integer mutationThreshold

    @Input
    @Optional
    Integer coverageThreshold

    @Input
    @Optional
    String mutationEngine

    @Input
    @Optional
    Boolean exportLineCoverage

    @Input
    @Optional
    File jvmPath

    @Input
    @Optional
    List<String> mainProcessJvmArgs

    @Input
    FileCollection launchClasspath

    @Input
    @Optional
    Map<String, String> pluginConfiguration

    @Input
    @Optional
    Integer maxSurviving

    @Input
    @Optional
    List<String> features
}
