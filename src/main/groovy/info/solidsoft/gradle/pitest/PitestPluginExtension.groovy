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
import org.gradle.api.Incubating
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.SourceSet

/**
 * Extension class with configurable parameters for Pitest plugin.
 *
 * Note: additionalClasspath, mutableCodePaths, sourceDirs, reportDir and pitestVersion are automatically set using project
 *   configuration. sourceDirs, reportDir and pitestVersion can be overridden by an user.
 */
@CompileStatic
class PitestPluginExtension {

    final Property<String> pitestVersion

    /**
     * Specifies what test plugin to use.
     *
     * Prior to 1.3.0 this was autodetected, now it has to be specified. The junit plugin is used by default.
     *
     * @since 1.3.0
     */
    final Property<String> testPlugin

//    //ClassNotFoundException: org.gradle.api.file.FileSystemLocationProperty in Gradle <5.6 due to super interface of RegularFileProperty
//    final RegularFileProperty reportDir
    private File reportDir

    final SetProperty<String> targetClasses
    final SetProperty<String> targetTests
    final Property<Integer> dependencyDistance
    final Property<Integer> threads
    @Deprecated //PIT doesn't know it
    final Property<Boolean> mutateStaticInits
    @Deprecated //removed in PIT 0.33
    final Property<Boolean> includeJarFiles
    final SetProperty<String> mutators
    final SetProperty<String> excludedMethods
    final SetProperty<String> excludedClasses

    /**
     * A list of test classes which should be excluded when mutating.
     *
     * @since 1.3.0
     * @see #excludedClasses
     * @see #excludedMethods
     */
    @Incubating
    final SetProperty<String> excludedTestClasses
    final SetProperty<String> avoidCallsTo
    final Property<Boolean> verbose
    final Property<BigDecimal> timeoutFactor
    final Property<Integer> timeoutConstInMillis
    final Property<Integer> maxMutationsPerClass
    /**
     * JVM arguments to use when PIT launches child processes
     *
     * Note. This parameter type was changed from String to List<String> in 0.33.0.
     */
    final ListProperty<String> jvmArgs
    final SetProperty<String> outputFormats
    final Property<Boolean> failWhenNoMutations
    final SetProperty<String> includedGroups  //renamed from includedTestNGGroups in 1.0.0 - to adjust to changes in PIT
    final SetProperty<String> excludedGroups  //renamed from excludedTestNGGroups in 1.0.0 - to adjust to changes in PIT
    final Property<Boolean> detectInlinedCode   //new in PIT 0.28
    final Property<Boolean> timestampedReports
    File historyInputLocation   //new in PIT 0.29
    File historyOutputLocation
    final Property<Boolean> enableDefaultIncrementalAnalysis    //specific for Gradle plugin - since 0.29.0
    final Property<Integer> mutationThreshold   //new in PIT 0.30
    final Property<Integer> coverageThreshold   //new in PIT 0.32
    final Property<String> mutationEngine
    final SetProperty<SourceSet> testSourceSets   //specific for Gradle plugin - since 0.30.1
    final SetProperty<SourceSet> mainSourceSets   //specific for Gradle plugin - since 0.30.1
    final Property<Boolean> exportLineCoverage  //new in PIT 0.32 - for debugging usage only
    File jvmPath    //new in PIT 0.32

    /**
     * JVM arguments to use when Gradle plugin launches the main PIT process.
     *
     * @since 0.33.0 (specific for Gradle plugin)
     */
    final ListProperty<String> mainProcessJvmArgs

    /**
     * Additional mutableCodePaths (paths with production classes which should be mutated).<p/>
     *
     * By default all classes produced by default sourceSets (or defined via mainSourceSets property) are used as production code to mutate.
     * In some rare cases it is required to pass additional classes, e.g. from JAR produced by another subproject. Issue #25.
     *
     * Samples usage ("itest" project depends on "shared" project):
     * <pre>
     * configure(project(':itest')) {
     *     dependencies {
     *         compile project(':shared')
     *     }
     *
     *     apply plugin: "info.solidsoft.pitest"
     *     //mutableCodeBase - additional configuration to resolve :shared project JAR as mutable code path for PIT
     *     configurations { mutableCodeBase { transitive false } }
     *     dependencies { mutableCodeBase project(':shared') }
     *     pitest {
     *         mainSourceSets = [project.sourceSets.main, project(':shared').sourceSets.main]
     *         additionalMutableCodePaths = [configurations.mutableCodeBase.singleFile]
     *     }
     * }
     * </pre>
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
    MapProperty<String, String> pluginConfiguration

    final Property<Integer> maxSurviving    //new in PIT 1.1.10

    /**
     * Use classpath file instead of passing classpath in a command line
     *
     * Useful with very long classpath and Windows - see https://github.com/hcoles/pitest/issues/276
     * Disabled by default.
     *
     * @since 1.2.0
     */
    @Incubating
    final Property<Boolean> useClasspathFile

    /**
     * Turnes on/off features in PIT itself and its plugins.
     *
     * Some details: https://github.com/hcoles/pitest/releases/tag/pitest-parent-1.2.1
     *
     * @since 1.2.1
     */
    @Incubating
    final ListProperty<String> features

    /**
     * File extensions which should be filtered from a classpath.
     *
     * PIT fails on not Java specific file passed on a classpath (e.g. native libraries). Native libraries ('*.so', '*.dll', '*.dylib')
     * and '*.pom' files are filtered by default, but a developer can add extra extensions to the list:
     * <pre>
     * pitest {
     *     fileExtensionsToFilter += ['xml', 'orbit']
     * }
     * </pre>
     *
     * Rationale: https://github.com/szpak/gradle-pitest-plugin/issues/53
     *
     * This feature is specific to the Gradle plugin.
     *
     * <b>Please note</b>. Starting with 1.4.5 due to Gradle limitations only the new syntax with addFileExtensionsToFilter(['xml'[) is available.
     * More information: https://github.com/gradle/gradle/issues/10475
     *
     * @since 1.2.4
     */
    @Incubating
    final ListProperty<String> fileExtensionsToFilter

    PitestPluginExtension(Project project) {
        ObjectFactory of = project.objects
        pitestVersion = of.property(String)

        testPlugin = of.property(String)
//        reportDir = of.fileProperty()
        targetClasses = setSetPropertyToNull(of.setProperty(String))    //empty collection (default value) is resolved as set to target classes
        targetTests = of.setProperty(String)
        dependencyDistance = setPropertyToNull(of.property(Integer))
        threads = setPropertyToNull(of.property(Integer))
        mutateStaticInits = setPropertyToNull(of.property(Boolean))
        includeJarFiles = setPropertyToNull(of.property(Boolean))
        mutators = of.setProperty(String)
        excludedMethods = of.setProperty(String)
        excludedClasses = of.setProperty(String)
        excludedTestClasses = of.setProperty(String)
        avoidCallsTo = of.setProperty(String)
        verbose = setPropertyToNull(of.property(Boolean))
        timeoutFactor = of.property(BigDecimal)
        timeoutConstInMillis = setPropertyToNull(of.property(Integer))
        maxMutationsPerClass = setPropertyToNull(of.property(Integer))

        jvmArgs = setListPropertyToNull(of.listProperty(String))
        outputFormats = of.setProperty(String)
        failWhenNoMutations = setPropertyToNull(of.property(Boolean))
        includedGroups = of.setProperty(String)
        excludedGroups = of.setProperty(String)
        detectInlinedCode = setPropertyToNull(of.property(Boolean))
        timestampedReports = setPropertyToNull(of.property(Boolean))
//        historyInputLocation = of.fileProperty()
//        historyOutputLocation = of.fileProperty()
        enableDefaultIncrementalAnalysis = setPropertyToNull(of.property(Boolean))
        mutationThreshold = setPropertyToNull(of.property(Integer))
        coverageThreshold = setPropertyToNull(of.property(Integer))
        mutationEngine = of.property(String)
        testSourceSets = setSetPropertyToNull(of.setProperty(SourceSet))
        mainSourceSets = setSetPropertyToNull(of.setProperty(SourceSet))
        exportLineCoverage = setPropertyToNull(of.property(Boolean))
//        jvmPath = of.fileProperty()

        mainProcessJvmArgs = of.listProperty(String)
//        additionalMutableCodePaths = setSetPropertyToNull(of.setProperty(File))
        pluginConfiguration = setMapPropertyToNull(of.mapProperty(String, String))
        maxSurviving = setPropertyToNull(of.property(Integer))

        useClasspathFile = setPropertyToNull(of.property(Boolean))
        features = of.listProperty(String)

        fileExtensionsToFilter = of.listProperty(String)
    }

    void setReportDir(File reportDir) {
        this.reportDir = reportDir
    }

    File getReportDir() {
        return reportDir
    }

    void setReportDir(String reportDirAsString) {
        this.reportDir = new File(reportDirAsString)
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
        this.timeoutFactor.set(new BigDecimal(timeoutFactor))
    }

    /**
     * File extensions which should be filtered from a classpath (ListProperty edition).
     *
     * PIT fails on not Java specific file passed on a classpath (e.g. native libraries). Native libraries ('*.so', '*.dll', '*.dylib')
     * and '*.pom' files are filtered by default, but a developer can add extra extensions to the list:
     * <pre>
     * pitest {
     *     addFileExtensionsToFilter(['xml', 'orbit'])
     * }
     * </pre>
     *
     * Rationale: https://github.com/szpak/gradle-pitest-plugin/issues/53
     *
     * This syntax is a workaround caused by a lack of support for "+=" with ListProperty: https://github.com/gradle/gradle/issues/10475
     *
     * This feature is specific to the Gradle plugin.
     *
     * @since 1.4.5
     */
    @Incubating
    void addFileExtensionsToFilter(List<String> fileExtensionsToFilterToAdd) {
        this.fileExtensionsToFilter.set(this.fileExtensionsToFilter.get() + fileExtensionsToFilterToAdd)
    }

    /**
     * Alias for enableDefaultIncrementalAnalysis.
     *
     * To make migration from PIT Maven plugin to PIT Gradle plugin easier.
     *
     * @since 1.1.10
     */
    void setWithHistory(Boolean withHistory) {
        this.enableDefaultIncrementalAnalysis.set(withHistory)
    }

    private <T> SetProperty<T> setSetPropertyToNull(SetProperty<T> setProperty) {
        // TODO: Switch to value() once 5.6+ is minimal required Gradle version
        setProperty.set(null as Set)
        return setProperty
    }

    private <T> ListProperty<T> setListPropertyToNull(ListProperty<T> listProperty) {
        //TODO: Switch to value() once 5.6+ is minimal required Gradle version
        listProperty.set(null as List)
        return listProperty
    }

    private <K, V> MapProperty<K, V> setMapPropertyToNull(MapProperty<K, V> mapProperty) {
        //TODO: Switch to value() once 5.6+ is minimal required Gradle version
        mapProperty.set(null as Map)
        return mapProperty
    }

    //Workaround on issue with Gradle <5.0 where Integer/Boolean property had 0/false provided by default - changed in 5.0+
    private <T> Property<T> setPropertyToNull(Property<T> property) {
        property.set(null as T)
        return property
    }
}
