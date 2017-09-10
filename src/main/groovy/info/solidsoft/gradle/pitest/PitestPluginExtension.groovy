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

import com.android.build.gradle.api.AndroidSourceSet
import groovy.transform.CompileStatic
import org.gradle.api.Incubating
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskInstantiationException

/**
 * Extension class with configurable parameters for Pitest plugin.
 *
 * Note: additionalClasspath, mutableCodePaths, sourceDirs, reportDir and pitestVersion are automatically set using project
 *   configuration. sourceDirs, reportDir and pitestVersion can be overridden by an user.
 */
@CompileStatic
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
    BigDecimal timeoutFactor
    Integer timeoutConstInMillis
    Integer maxMutationsPerClass
    /**
     * JVM arguments to use when PIT launches child processes
     *
     * Note. This parameter type was changed from String to List<String> in 0.33.0.
     */
    List<String> jvmArgs
    Set<String> outputFormats
    Boolean failWhenNoMutations
    Set<String> includedGroups  //renamed from includedTestNGGroups in 1.0.0 - to adjust to changes in PIT
    Set<String> excludedGroups  //renamed from excludedTestNGGroups in 1.0.0 - to adjust to changes in PIT
//    File configFile           //removed in 1.1.6 to adjust to changes in PIT
    Boolean detectInlinedCode   //new in PIT 0.28
    Boolean timestampedReports
    File historyInputLocation   //new in PIT 0.29
    File historyOutputLocation
    Boolean enableDefaultIncrementalAnalysis    //specific for Gradle plugin - since 0.29.0
    Integer mutationThreshold   //new in PIT 0.30
    Integer coverageThreshold   //new in PIT 0.32
    String mutationEngine
    Set<AndroidSourceSet> mainSourceSets   //specific for Gradle plugin - since 0.30.1
    Boolean exportLineCoverage  //new in PIT 0.32 - for debugging usage only
    File jvmPath    //new in PIT 0.32

    /**
     * JVM arguments to use when Gradle plugin launches the main PIT process.
     *
     * @since 0.33.0 (specific for Gradle plugin)
     */
    List<String> mainProcessJvmArgs

    /**
     * Additional mutableCodePaths (paths with production classes which should be mutated).<p/>
     *
     * By default all classes produced by default sourceSets (or defined via mainSourceSets property) are used as production code to mutate.
     * In some rare cases it is required to pass additional classes, e.g. from JAR produced by another subproject. Issue #25.
     *
     * Samples usage ("itest" project depends on "shared" project):
     * <pre>
     * configure(project(':itest')) {*     dependencies {*         compile project(':shared')
     *}*
     *     apply plugin: "pl.droidsonroids.pitest"
     *     //mutableCodeBase - additional configuration to resolve :shared project JAR as mutable code path for PIT
     *     configurations { mutableCodeBase { transitive false }}*     dependencies { mutableCodeBase project(':shared') }*     pitest {*         mainSourceSets = [project.sourceSets.main, project(':shared').sourceSets.main]
     *         additionalMutableCodePaths = [configurations.mutableCodeBase.singleFile]
     *}*}* </pre>
     *
     * @since 1.1.3 (specific for Gradle plugin)
     */
    Set<File> additionalMutableCodePaths

    /**
     * Plugin configuration parameters.
     *
     * Should be defined a map:
     * <pre>
     * pitest {
     *     pluginConfiguration = ["plugin1.key1": "value1", "plugin1.key2": "value2"]
     * }
     * </pre>
     *
     * @since 1.1.6
     */
    Map<String, String> pluginConfiguration

    Integer maxSurviving    //new in PIT 1.1.10

    /**
     * Use classpath file instead of passing classpath in a command line
     *
     * Useful with very long classpath and Windows - see https://github.com/hcoles/pitest/issues/276
     * Disabled by default.
     *
     * @since 1.2.0
     */
    @Incubating
    boolean useClasspathFile = false

    /**
     * Turned on/off features in PIT itself and its plugins.
     *
     * Some details: https://github.com/hcoles/pitest/releases/tag/pitest-parent-1.2.1
     *
     * @since 1.2.1
     */
    @Incubating
    List<String> features

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

    void setHistoryInputLocation(String historyInputLocationPath) {
        this.historyInputLocation = new File(historyInputLocationPath)
    }

    void setHistoryOutputLocation(String historyOutputLocationPath) {
        this.historyOutputLocation = new File(historyOutputLocationPath)
    }

    void setJvmPath(String jvmPathAsString) {
        this.jvmPath = new File(jvmPathAsString)
    }

    void setTimeoutFactor(String timeoutFactor) {
        this.timeoutFactor = new BigDecimal(timeoutFactor)
    }

    /**
     * Alias for enableDefaultIncrementalAnalysis.
     *
     * To make migration from PIT Maven plugin to PIT Gradle plugin easier.
     *
     * @since 1.1.10
     */
    void setWithHistory(Boolean withHistory) {
        this.enableDefaultIncrementalAnalysis = withHistory
    }

    /**
     * The first (broken) implementation of using a file to pass additional classpath to PIT.
     * Use "useClasspathFile" property instead.
     *
     * @since 1.1.11
     */
    @Deprecated //as of 1.2.0
    void setClassPathFile(File classPathFile) {
        throw new TaskInstantiationException("Passing 'classPathFile' manually was broken and it is no longer available. Use 'useClasspathFile' " +
            "property to enable passing classpath to PIT as file. ")
    }
}
