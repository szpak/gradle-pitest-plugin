package info.solidsoft.gradle.pitest.task

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*

abstract class AbstractPitestTask extends JavaExec {
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

    protected List<String> createListOfAllArgumentsForPit() {
        Map<String, String> taskArgumentsMap = createTaskArgumentMap()
        List<String> argsAsList = createArgumentsListFromMap(taskArgumentsMap)
        List<String> multiValueArgsAsList = createMultiValueArgsAsList()
        return concatenateTwoLists(argsAsList, multiValueArgsAsList)
    }

    @PackageScope   //visible for testing
    Map<String, String> createTaskArgumentMap() {
        Map<String, String> map = [:]
        map['sourceDirs'] = (getSourceDirs()*.path)?.join(',')
        map['reportDir'] = getReportDir().toString()
        map['targetClasses'] = getTargetClasses().join(',')
        map['targetTests'] = getTargetTests()?.join(',')
        map['dependencyDistance'] = getDependencyDistance()?.toString()
        map['threads'] = getThreads()?.toString()
        map['mutateStaticInits'] = getMutateStaticInits()?.toString()
        map['includeJarFiles'] = getIncludeJarFiles()?.toString()
        map["mutators"] = getMutators()?.join(',')
        map['excludedMethods'] = getExcludedMethods()?.join(',')
        map['excludedClasses'] = getExcludedClasses()?.join(',')
        map['avoidCallsTo'] = getAvoidCallsTo()?.join(',')
        map['verbose'] = getVerbose()?.toString()
        map['timeoutFactor'] = getTimeoutFactor()?.toString()
        map['timeoutConst'] = getTimeoutConstInMillis()?.toString()
        map['maxMutationsPerClass'] = getMaxMutationsPerClass()?.toString()
        map['jvmArgs'] = getChildProcessJvmArgs()?.join(',')
        map['outputFormats'] = getOutputFormats()?.join(',')
        map['failWhenNoMutations'] = getFailWhenNoMutations()?.toString()
        map['mutableCodePaths'] = (getMutableCodePaths()*.path)?.join(',')
        map['includedGroups'] = getIncludedGroups()?.join(',')
        map['excludedGroups'] = getExcludedGroups()?.join(',')
        map['detectInlinedCode'] = getDetectInlinedCode()?.toString()
        map['timestampedReports'] = getTimestampedReports()?.toString()
        map['mutationThreshold'] = getMutationThreshold()?.toString()
        map['coverageThreshold'] = getCoverageThreshold()?.toString()
        map['mutationEngine'] = getMutationEngine()
        map['exportLineCoverage'] = getExportLineCoverage()?.toString()
        map['includeLaunchClasspath'] = Boolean.FALSE.toString()   //code to analyse is passed via classPath
        map['jvmPath'] = getJvmPath()?.path
        map['maxSurviving'] = getMaxSurviving()?.toString()
        map['features'] = getFeatures()?.join(',')
        map.putAll(prepareMapWithClasspathConfiguration())
        map.putAll(prepareMapWithIncrementalAnalysisConfiguration())

        return removeEntriesWithNullValue(map)
    }

    protected Map<String, String> prepareMapWithClasspathConfiguration() {
        if (getUseAdditionalClasspathFile()) {
            fillAdditionalClasspathFileWithClasspathElements()
            return [classPathFile: getAdditionalClasspathFile().absolutePath]
        } else {
            return [classPath: getAdditionalClasspath().files.join(',')]
        }
    }

    protected void fillAdditionalClasspathFileWithClasspathElements() {
        String classpathElementsAsFileContent = getAdditionalClasspath().files.collect { it.getAbsolutePath() }.join(System.lineSeparator())
        //"withWriter" as "file << content" works in append mode (instead of overwrite one)
        getAdditionalClasspathFile().withWriter() {
            it << classpathElementsAsFileContent
        }
    }

    protected Map<String, String> prepareMapWithIncrementalAnalysisConfiguration() {
        if (getEnableDefaultIncrementalAnalysis()) {
            return [historyInputLocation : getHistoryInputLocation()?.path ?: getDefaultFileForHistoryData().path,
                    historyOutputLocation: getHistoryOutputLocation()?.path ?: getDefaultFileForHistoryData().path]
        } else {
            return [historyInputLocation: getHistoryInputLocation()?.path,
                    historyOutputLocation: getHistoryOutputLocation()?.path]
        }
    }

    protected Map removeEntriesWithNullValue(Map map) {
        return map.findAll { it.value != null }
    }

    protected List<String> createArgumentsListFromMap(Map<String, String> taskArgumentsMap) {
        return taskArgumentsMap.collect { k, v ->
            "--$k=$v".toString()
        }
    }

    @PackageScope   //visible for testing
    List<String> createMultiValueArgsAsList() {
        //It is a duplication/special case handling, but a PoC implementation with emulated multimap was also quite ugly and in addition error prone
        return getPluginConfiguration()?.collect { k, v ->
            "$k=$v".toString()
        }?.collect {
            "--pluginConfiguration=$it".toString()
        } ?: [] as List<String>
    }

    //Workaround to keep compatibility with Gradle <2.8
    //[] + [] is compiled in Groovy 2.4.x as "List<T> plus(List<T> left, Collection<T> right)" which is unavailable in Groovy 2.3 and fails with Gradle <2.8
    protected List<String> concatenateTwoLists(List<String> argsAsList, List<String> multiValueArgsAsList) {
        List<String> allArgs = []
        allArgs.addAll(argsAsList)
        allArgs.addAll(multiValueArgsAsList)
        return allArgs
    }
}
