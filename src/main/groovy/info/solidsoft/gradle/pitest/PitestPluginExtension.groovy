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

import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskInstantiationException

/**
 * Extension class with configurable parameters for Pitest plugin.
 *
 * Note: taskClasspath, mutableCodePaths, sourceDirs, reportDir and pitestVersion are automatically set using project
 *   configuration. sourceDirs, reportDir and pitestVersion can be overridden by an user.
 */
class PitestPluginExtension {
    String pitestVersion
//    Set<File> sourceDirs  //Removed in 0.30.1 - use mainSourceSets

    File reportDir
    Set<String> targetClasses
    Set<String> targetTests
    Integer dependencyDistance
    Integer threads
    Boolean mutateStaticInits
    Boolean includeJarFiles
    Set<String> mutators
    Set<String> excludedMethods
    Set<String> excludedClasses
    Set<String> avoidCallsTo
    Boolean verbose
    String timeoutFactor    //TODO: MZA: BigDecimal?
    Integer timeoutConstInMillis
    Integer maxMutationsPerClass
    /**
     * JVM arguments to use when PIT launches child processes
     */
    String jvmArgs
    Set<String> outputFormats
    Boolean failWhenNoMutations
    Set<String> includedTestNGGroups
    Set<String> excludedTestNGGroups
    File configFile
    Boolean detectInlinedCode   //new in PIT 0.28
    Boolean timestampedReports
    File historyInputLocation   //new in PIT 0.29
    File historyOutputLocation
    Boolean enableDefaultIncrementalAnalysis    //specific for Gradle plugin - since 0.29.0
    Integer mutationThreshold   //new in PIT 0.30
    Integer coverageThreshold   //new in PIT 0.32
    String mutationEngine
    Set<SourceSet> testSourceSets   //specific for Gradle plugin - since 0.30.1
    Set<SourceSet> mainSourceSets   //specific for Gradle plugin - since 0.30.1
    Boolean exportLineCoverage  //new in PIT 0.32 - for debugging usage only
    File jvmPath    //new in PIT 0.32
    /**
     * JVM arguments to use when Gradle plugin launches the main PIT process.
     *
     * @since 0.33.0 (specific for Gradle plugin)
     */
    List<String> mainProcessJvmArgs

    void setReportDir(String reportDirAsString) {
        this.reportDir = new File(reportDirAsString)
    }

    void setSourceDirsAsFiles(Set<File> sourceDirs) {
        throwExceptionAboutRemovedManualSettingOfSourceDirs()
    }

    void setSourceDirs(Set<String> sourceDirs) {
        throwExceptionAboutRemovedManualSettingOfSourceDirs()
    }

    private throwExceptionAboutRemovedManualSettingOfSourceDirs() {
        throw new TaskInstantiationException("Manual setting of sourceDirs was removed in version 0.30.1. " +
                "Use mainSourceSets property to select source sets which would be used to get source directories. " +
                "Feel free to raise an issue if you need removed feature.")
    }

    void setConfigFile(String configFile) {
        this.configFile = new File(configFile)
    }

    void setHistoryInputLocation(String historyInputLocationPath) {
        this.historyInputLocation = new File(historyInputLocationPath)
    }

    void setHistoryOutputLocation(String historyOutputLocationPath) {
        this.historyOutputLocation = new File(historyOutputLocationPath)
    }

    void setJvmPath(String jvmPathAsString) {
        this.jvmPath = new File(jvmPathAsString)
    }
}
