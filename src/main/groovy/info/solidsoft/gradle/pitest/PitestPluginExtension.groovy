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

/**
 * Extension class with configurable parameters for Pitest plugin.
 *
 * Note: taskClasspath, mutableCodePaths, sourceDirs, reportDir and pitestVersion are automatically set using project
 *   configuration. sourceDirs, reportDir and pitestVersion can be overridden by an user.
 */
class PitestPluginExtension {
    String pitestVersion
    Set<File> sourceDirs

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
    String mutationEngine
    Set<SourceSet> testSourceSets   //specific for Gradle plugin - since 0.30.1

    void setReportDir(String reportDir) {
        this.reportDir = new File(reportDir)
    }

    void setSourceDirsAsFiles(Set<File> sourceDirs) {
        this.sourceDirs = sourceDirs
    }

    void setSourceDirs(Set<String> sourceDirs) {
        Set<File> sourceDirsAsFiles = [] as Set
        sourceDirs.each { sourceDirsAsFiles.add(new File(it))}
        this.sourceDirs = sourceDirsAsFiles
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
}
