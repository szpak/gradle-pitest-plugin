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
/**
 * Gradle task implementation for Pitest.
 */
@CompileStatic
class PitestTask extends AbstractPitestTask {

    @Override
    void exec() {
        //Workaround for compatibility with Gradle <4.0 due to setArgs(List) and setJvmArgs(List) added in Gradle 4.0
        args = createListOfAllArgumentsForPit()
        jvmArgs = (getMainProcessJvmArgs() ?: getJvmArgs())
        main = "org.pitest.mutationtest.commandline.MutationCoverageReport"
        classpath = getLaunchClasspath()
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

    private Map<String, String> prepareMapWithClasspathConfiguration() {
        if (getUseAdditionalClasspathFile()) {
            fillAdditionalClasspathFileWithClasspathElements()
            return [classPathFile: getAdditionalClasspathFile().absolutePath]
        } else {
            return [classPath: getAdditionalClasspath().files.join(',')]
        }
    }

    private void fillAdditionalClasspathFileWithClasspathElements() {
        String classpathElementsAsFileContent = getAdditionalClasspath().files.collect { it.getAbsolutePath() }.join(System.lineSeparator())
        //"withWriter" as "file << content" works in append mode (instead of overwrite one)
        getAdditionalClasspathFile().withWriter() {
            it << classpathElementsAsFileContent
        }
    }

    private Map<String, String> prepareMapWithIncrementalAnalysisConfiguration() {
        if (getEnableDefaultIncrementalAnalysis()) {
            return [historyInputLocation : getHistoryInputLocation()?.path ?: getDefaultFileForHistoryData().path,
                    historyOutputLocation: getHistoryOutputLocation()?.path ?: getDefaultFileForHistoryData().path]
        } else {
            return [historyInputLocation: getHistoryInputLocation()?.path,
                    historyOutputLocation: getHistoryOutputLocation()?.path]
        }
    }

    private Map removeEntriesWithNullValue(Map map) {
        return map.findAll { it.value != null }
    }

    private List<String> createArgumentsListFromMap(Map<String, String> taskArgumentsMap) {
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
    private List<String> concatenateTwoLists(List<String> argsAsList, List<String> multiValueArgsAsList) {
        List<String> allArgs = []
        allArgs.addAll(argsAsList)
        allArgs.addAll(multiValueArgsAsList)
        return allArgs
    }
}
