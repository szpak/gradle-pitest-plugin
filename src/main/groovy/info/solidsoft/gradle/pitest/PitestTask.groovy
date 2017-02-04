/* Copyright (c) 2012 Marcin Zajączkowski
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.solidsoft.gradle.pitest

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile

/**
 * Gradle task implementation for Pitest.
 */
@CompileStatic
class PitestTask extends JavaExec {

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
    FileCollection taskClasspath

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
    File defaultFileForHistoryDate

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

    @InputFile
    @Optional
    File classPathFile

    @Override
    void exec() {
        setArgs(createListOfAllArgumentsForPit())
        setMain("org.pitest.mutationtest.commandline.MutationCoverageReport")
        setJvmArgs(getMainProcessJvmArgs() ?: getJvmArgs())
        setClasspath(getLaunchClasspath())
        super.exec()
    }

    private List<String> createListOfAllArgumentsForPit() {
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
        map['classPath'] = getTaskClasspath()?.files?.join(',')
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
        map['classPathFile'] = getClassPathFile()?.path
        map.putAll(prepareMapForIncrementalAnalysis())

        return removeEntriesWithNullValue(map)
    }

    private Map<String, String> prepareMapForIncrementalAnalysis() {
        Map<String, String> map = [:]
        if (getEnableDefaultIncrementalAnalysis()) {
            map['historyInputLocation'] = getHistoryInputLocation()?.path ?: getDefaultFileForHistoryDate().path
            map['historyOutputLocation'] = getHistoryOutputLocation()?.path ?: getDefaultFileForHistoryDate().path
        } else {
            map['historyInputLocation'] = getHistoryInputLocation()?.path
            map['historyOutputLocation'] = getHistoryOutputLocation()?.path
        }
        map
    }

    private Map removeEntriesWithNullValue(Map map) {
        map.findAll { it.value != null }
    }

    private List<String> createArgumentsListFromMap(Map<String, String> taskArgumentsMap) {
        taskArgumentsMap.collect { k, v ->
            "--$k=$v".toString()
        }
    }

    @PackageScope   //visible for testing
    List<String> createMultiValueArgsAsList() {
        //It is a duplication/special case handling, but a PoC implementation with emulated multimap was also quite ugly and in addition error prone
        getPluginConfiguration()?.collect { k, v ->
            "$k=$v".toString()
        }?.collect {
            "--pluginConfiguration=$it".toString()
        } ?: [] as List<String>
    }

    //Workaround to keep compatibility with Gradle <2.8
    //[] + [] is compiled in Groovy 2.4.x as "List<T> plus(List<T> left, Collection<T> right)" which is unavailable in Groovy 2.3 and fails with Gradle <2.8
    private List<String> concatenateTwoLists(List<String> argsAsList, List<String> multiValueArgsAsList) {
        List<String> allArgs = []
        allArgs.addAll(argsAsList)
        allArgs.addAll(multiValueArgsAsList)
        return allArgs
    }
}
