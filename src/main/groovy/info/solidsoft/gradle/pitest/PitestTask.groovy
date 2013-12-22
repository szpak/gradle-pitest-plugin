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
package info.solidsoft.gradle.pitest;

import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.*;

import org.pitest.mutationtest.commandline.MutationCoverageReport
import com.google.common.annotations.VisibleForTesting

/**
 * Gradle task implementation for Pitest.
 */
class PitestTask extends SourceTask {

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
    String timeoutFactor

    @Input
    @Optional
    Integer timeoutConstInMillis

    @Input
    @Optional
    Integer maxMutationsPerClass

    @Input
    @Optional
    String jvmArgs

    @Input
    @Optional
    Set<String> outputFormats

    @Input
    @Optional
    Boolean failWhenNoMutations

    @Input
    @Optional
    Set<String> includedTestNGGroups

    @Input
    @Optional
    Set<String> excludedTestNGGroups

    @InputFiles
    Set<File> sourceDirs

    @Input
    @Optional
    File configFile

    @Input
    @Optional
    Boolean detectInlinedCode

    @Input
    @Optional
    Boolean timestampedReports

    //TODO: MZA: Not used but leave due to a problem with resolving pitest version. See a comment in PitestTask.
    @Deprecated
    @InputFiles
    FileCollection pitestClasspath

    @Input  //TODO: MZA: Why "Could not determine the dependencies of task ':pitest'" with @InputFiles?
    FileCollection taskClasspath

    @Input
    Set<File> mutableCodePaths

    @Input
    @Optional
    File historyInputLocation

    @Input
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

    @TaskAction
    void run() {
        Map<String, String> taskArgumentsMap = createTaskArgumentMap()
        String[] arg = createArgumentsArrayFromMap(taskArgumentsMap)
        println arg   //TODO: MZA: Change to a debug log
        MutationCoverageReport.main(arg)
    }

    @VisibleForTesting
    Map<String, String> createTaskArgumentMap() {
        Map<String, String> map = [:]
        map['sourceDirs'] = (getSourceDirs()*.path)?.join(',')
        map['reportDir'] = getReportDir()
        map['targetClasses'] = getTargetClasses()?.join(',')
        map['targetTests'] = getTargetTests()?.join(',')
        map['dependencyDistance'] = getDependencyDistance()
        map['threads'] = getThreads()
        map['mutateStaticInits'] = getMutateStaticInits()
        map['includeJarFiles'] = getIncludeJarFiles()
        map["mutators"] = getMutators()?.join(',')
        map['excludedMethods'] = getExcludedMethods()?.join(',')
        map['excludedClasses'] = getExcludedClasses()?.join(',')
        map['avoidCallsTo'] = getAvoidCallsTo()?.join(',')
        map['verbose'] = getVerbose()
        map['timeoutFactor'] = getTimeoutFactor()
        map['timeoutConst'] = getTimeoutConstInMillis()
        map['maxMutationsPerClass'] = getMaxMutationsPerClass()
        map['jvmArgs'] = getJvmArgs()
        map['outputFormats'] = getOutputFormats()?.join(',')
        map['failWhenNoMutations'] = getFailWhenNoMutations()
        map['classPath'] = getTaskClasspath()?.files?.join(',')
        map['mutableCodePaths'] = (getMutableCodePaths()*.path)?.join(',')
        map['includedTestNGGroups'] = getIncludedTestNGGroups()?.join(',')
        map['excludedTestNGGroups'] = getExcludedTestNGGroups()?.join(',')
        map['configFile'] = getConfigFile()?.path
        map['detectInlinedCode'] = getDetectInlinedCode()
        map['timestampedReports'] = getTimestampedReports()
        map['mutationThreshold'] = getMutationThreshold()
        map['coverageThreshold'] = getCoverageThreshold()
        map['mutationEngine'] = getMutationEngine()
        map['exportLineCoverage'] = getExportLineCoverage()
        map['jvmPath'] = getJvmPath()
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

    private String[] createArgumentsArrayFromMap(Map<String, String> taskArgumentsMap) {
        List<String> argList = new ArrayList<String>();
        taskArgumentsMap.each { k, v ->
            argList.add("--" + k + "=" + v)
        }
        argList.toArray(String.class)
    }
}
