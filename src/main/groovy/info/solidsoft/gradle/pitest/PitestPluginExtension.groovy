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
import org.gradle.api.Action
import org.gradle.api.Incubating
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.SourceSet

import java.nio.charset.Charset

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
     * Prior to 1.3.0 this was auto detected. Up to 1.6.8 it has to be specified. With 1.6.7+ is not needed anymore (test plugins are auto detected)
     * and it is deprecated in PIT. To be removed in 1.8.0.
     *
     * For using with JUnit 5 please see: junit5PluginVersion
     *
     * Related issue: https://github.com/szpak/gradle-pitest-plugin/issues/277
     *
     * @since 1.3.0
     * @deprecated Not needed with PIT 1.6.7+, to be removed in PIT 1.8.0.
     */
    @Deprecated
    final Property<String> testPlugin

    /**
     * Specifies JUnit 5 plugin for PIT which should be added as a dependency.
     *
     * It sets also 'testPlugin' to 'junit5' - unless not explicitly set.
     *
     * Related issue: https://github.com/szpak/gradle-pitest-plugin/issues/177
     *
     * @since 1.4.7
     */
    final Property<String> junit5PluginVersion

    //ClassNotFoundException: org.gradle.api.file.FileSystemLocationProperty in Gradle <5.6 due to super interface of RegularFileProperty
    //See: https://github.com/gradle/gradle/issues/10953 - on the other hand deprecation warnings in Gradle 6.x with regular File
    final DirectoryProperty reportDir

    final SetProperty<String> targetClasses
    final SetProperty<String> targetTests
    final Property<Integer> threads
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
    /**
     * <p>If true, verbose logging is enabled (capture minion output, show the spinner, only log at detailed level).</p>
     * <p>If false, the value of verbosity is used to determine the verbosity of the output.</p>
     * <p>Disabled by default.</p>
     *
     * <p><b>Note:</b> This property is marked as deprecated as - in the majority of cases - it doesn't make sense to use it, if PIT is called using the Gradle
     * plugin. Use #verbosity instead.</p>
     *
     * @see #verbosity
     */
    @Deprecated //since GPP 1.9.11
    final Property<Boolean> verbose

    /**
     * Determines the verbosity of the output. Possible values:
     * <ul>
     * <li>QUIET (no capture of minion output, no spinner, only log severe errors)</li>
     * <li>QUIET_WITH_PROGRESS (no capture of minion output, show the spinner, only log severe errors)</li>
     * <li>DEFAULT (no capture of minion output, show the spinner, only log at info level)</li>
     * <li>NO_SPINNER (no capture of minion output, no spinner, only log at info level)</li>
     * <li>VERBOSE_NO_SPINNER (capture minion output, no spinner, only log at detailed level)</li>
     * <li>VERBOSE (capture minion output, show the spinner, only log at detailed level)</li>
     * </ul>
     * <p>The default value used by the plugin is NO_SPINNER (not DEFAULT, despite the name).</p>
     * <p><b>Note:</b> verbose must be set to false (which is the default) for this to have an effect.</p>
     *
     * @see #verbose
     * @since 1.9.11
     */
    final Property<String> verbosity //new in PIT 1.7.1 (GPP 1.9.11)
    final Property<BigDecimal> timeoutFactor
    final Property<Integer> timeoutConstInMillis
    /**
     * JVM arguments to use when PIT launches child processes
     */
    final ListProperty<String> jvmArgs
    final SetProperty<String> outputFormats
    final Property<Boolean> failWhenNoMutations
    final Property<Boolean> skipFailingTests    //new in PIT 1.4.4 (GPP 1.4.6)
    final SetProperty<String> includedGroups
    final SetProperty<String> excludedGroups
    final Property<Boolean> fullMutationMatrix  //new in PIT 1.4.3
    final SetProperty<String> includedTestMethods   //new in PIT 1.3.2 (GPP 1.4.6)
    final SetProperty<SourceSet> testSourceSets   //specific for Gradle plugin
    final SetProperty<SourceSet> mainSourceSets   //specific for Gradle plugin
    final Property<Boolean> detectInlinedCode
    final Property<Boolean> timestampedReports

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
    final SetProperty<File> additionalMutableCodePaths

    final RegularFileProperty historyInputLocation
    final RegularFileProperty historyOutputLocation
    final Property<Boolean> enableDefaultIncrementalAnalysis    //specific for Gradle plugin
    final Property<Integer> mutationThreshold
    final Property<Integer> coverageThreshold
    final Property<Integer> testStrengthThreshold
    final Property<String> mutationEngine
    final Property<Boolean> exportLineCoverage  //for debugging usage only
    final RegularFileProperty jvmPath

    /**
     * JVM arguments to use when Gradle plugin launches the main PIT process.
     */
    final ListProperty<String> mainProcessJvmArgs

    /**
     * Plugin configuration parameters.
     *
     * Should be defined a map:
     * <pre>
     * pitest {
     *     pluginConfiguration = ["plugin1.key1": "value1", "plugin1.key2": "value2"]
     * }
     * </pre>
     */
    MapProperty<String, String> pluginConfiguration

    final Property<Integer> maxSurviving

    @Incubating
    final Property<Boolean> useClasspathJar //new in PIT 1.4.2 (GPP 1.4.6)

    final Property<Charset> inputCharset    //new in PIT 1.8.1 as inputEncoding (GPP 1.9.0)
    final Property<Charset> outputCharset   //new in PIT 1.8.1 as outputEncoding (GPP 1.9.0)

    /**
     * Turns on/off features in PIT itself and its plugins.
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
     * <b>Please note</b>. Starting with 1.4.6 due to Gradle limitations only the new syntax with addAll()/addAll([] is possible (instead of "+="):
     *
     * pitest {
     *     fileExtensionsToFilter.addAll('xml', 'orbit')
     * }
     *
     * More information: https://github.com/gradle/gradle/issues/10475
     *
     * @since 1.2.4
     */
    @Incubating
    final ListProperty<String> fileExtensionsToFilter

    /**
     * Adds 'junit-platform-launcher' automatically to the 'testRuntimeOnly' configuration.
     *
     * Starting with PIT 1.14.0 (with pitest-junit-plugin 1.2.0+) that dependency is no longer shaded and has to be explicitly added to avoid:
     * "Minion exited abnormally due to UNKNOWN_ERROR" or "NoClassDefFoundError: org.junit.platform.launcher.core.LauncherFactory".
     * This feature is enabled by default if junit-platform is found on the testImplementation classes.
     *
     * PLEASE NOTE. This feature is experimental and might not work as expected in some corner cases. In that situation, just disable it and add
     * required dependency 'junit-platform-launcher' in a proper version to 'testRuntimeOnly' manually.
     *
     * More information: https://github.com/szpak/gradle-pitest-plugin/issues/337
     *
     * @since 1.14.0
     */
    @Incubating
    final Property<Boolean> addJUnitPlatformLauncher

    final ReportAggregatorProperties reportAggregatorProperties

    PitestPluginExtension(Project project) {
        ObjectFactory of = project.objects
        Project p = project

        pitestVersion = of.property(String)
        testPlugin = of.property(String)
        junit5PluginVersion = of.property(String)
        reportDir = of.directoryProperty()
        targetClasses = nullSetPropertyOf(p, String)    //null instead of empty collection to distinguish on optional parameters
        targetTests = nullSetPropertyOf(p, String)
        threads = of.property(Integer)
        mutators = nullSetPropertyOf(p, String)
        excludedMethods = nullSetPropertyOf(p, String)
        excludedClasses = nullSetPropertyOf(p, String)
        excludedTestClasses = nullSetPropertyOf(p, String)
        avoidCallsTo = nullSetPropertyOf(p, String)
        verbose = of.property(Boolean)
        verbosity = of.property(String)
        timeoutFactor = of.property(BigDecimal)
        timeoutConstInMillis = of.property(Integer)
        jvmArgs = nullListPropertyOf(p, String)
        outputFormats = nullSetPropertyOf(p, String)
        failWhenNoMutations = of.property(Boolean)
        skipFailingTests = of.property(Boolean)
        includedGroups = nullSetPropertyOf(p, String)
        excludedGroups = nullSetPropertyOf(p, String)
        fullMutationMatrix = of.property(Boolean)
        includedTestMethods = nullSetPropertyOf(p, String)
        testSourceSets = nullSetPropertyOf(p, SourceSet)
        mainSourceSets = nullSetPropertyOf(p, SourceSet)
        detectInlinedCode = of.property(Boolean)
        timestampedReports = of.property(Boolean)
        useClasspathFile = of.property(Boolean)
        additionalMutableCodePaths = of.setProperty(File)   //the value is not used directly in task and can be notPresent instead of null
        historyInputLocation = of.fileProperty()
        historyOutputLocation = of.fileProperty()
        enableDefaultIncrementalAnalysis = of.property(Boolean)
        mutationThreshold = of.property(Integer)
        coverageThreshold = of.property(Integer)
        testStrengthThreshold = of.property(Integer)
        mutationEngine = of.property(String)
        exportLineCoverage = of.property(Boolean)
        jvmPath = of.fileProperty()
        mainProcessJvmArgs = nullListPropertyOf(p, String)
        pluginConfiguration = nullMapPropertyOf(p, String, String)
        maxSurviving = of.property(Integer)
        useClasspathJar = of.property(Boolean)
        inputCharset = of.property(Charset)
        outputCharset = of.property(Charset)
        features = nullListPropertyOf(p, String)
        fileExtensionsToFilter = nullListPropertyOf(p, String)
        addJUnitPlatformLauncher = of.property(Boolean)
        reportAggregatorProperties = new ReportAggregatorProperties(of)
    }

    void reportAggregator(Action<? super ReportAggregatorProperties> action) {
        action.execute(reportAggregatorProperties)
    }

    void setReportDir(File reportDir) {
        this.reportDir.set(reportDir)
    }

    void setHistoryInputLocation(String historyInputLocationPath) {
        this.historyInputLocation.set(new File(historyInputLocationPath))
    }

    void setHistoryOutputLocation(String historyOutputLocationPath) {
        this.historyOutputLocation.set(new File(historyOutputLocationPath))
    }

    void setJvmPath(String jvmPathAsString) {
        this.jvmPath.set(new File(jvmPathAsString))
    }

    void setTimeoutFactor(String timeoutFactor) {
        this.timeoutFactor.set(new BigDecimal(timeoutFactor))
    }

    /**
     * Alias for enableDefaultIncrementalAnalysis.
     *
     * To make migration from PIT Maven plugin to PIT Gradle plugin easier.
     */
    void setWithHistory(Boolean withHistory) {
        this.enableDefaultIncrementalAnalysis.set(withHistory)
    }

    @Deprecated //Alias for inputCharset to keep naming compatibility with PIT
    void setInputEncoding(Charset inputCharset) {
        this.inputCharset.set(inputCharset)
    }

    @Deprecated //Alias for outputCharset to keep naming compatibility with PIT
    void setOutputEncoding(Charset outputCharset) {
        this.outputCharset.set(outputCharset)
    }

    private static <T> SetProperty<T> nullSetPropertyOf(Project p, Class<T> clazz) {
        return p.objects.setProperty(clazz).convention(p.providers.provider { null } as Provider)   //coercion due to "red" warning in Idea
    }

    private static <T> ListProperty<T> nullListPropertyOf(Project p, Class<T> clazz) {
        return p.objects.listProperty(clazz).convention(p.providers.provider { null } as Provider)
    }

    private static <K, V> MapProperty<K, V> nullMapPropertyOf(Project p, Class<K> keyClazz, Class<V> valueClazz) {
        return p.objects.mapProperty(keyClazz, valueClazz).convention(p.providers.provider { null } as Provider)
    }

}
