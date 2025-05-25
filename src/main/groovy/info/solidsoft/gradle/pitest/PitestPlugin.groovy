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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import info.solidsoft.gradle.pitest.internal.GradleVersionEnforcer
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.result.ResolutionResult
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.util.GradleVersion

import java.util.concurrent.Callable

import static org.gradle.api.plugins.JavaPlugin.TEST_TASK_NAME
import static org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP

/**
 * The main class for Pitest plugin.
 */
@CompileStatic
class PitestPlugin implements Plugin<Project> {

    public static final String PLUGIN_ID = "info.solidsoft.pitest"
    public final static String PITEST_TASK_GROUP = VERIFICATION_GROUP
    public final static String PITEST_TASK_NAME = "pitest"
    public final static String PITEST_REPORT_DIRECTORY_NAME = 'pitest'
    public final static String PITEST_CONFIGURATION_NAME = 'pitest'

    public final static String DEFAULT_PITEST_VERSION = '1.17.0'
    @Internal   //8.x just to be more ready for 9.x, could work with lower versions at runtime
    public static final GradleVersion MINIMAL_SUPPORTED_GRADLE_VERSION = GradleVersion.version("8.0") //public as used also in regression tests

    private static final String PITEST_JUNIT5_PLUGIN_NAME = "junit5"
    private final static List<String> DYNAMIC_LIBRARY_EXTENSIONS = ['so', 'dll', 'dylib']
    private final static List<String> DEFAULT_FILE_EXTENSIONS_TO_FILTER_FROM_CLASSPATH = ['pom'] + DYNAMIC_LIBRARY_EXTENSIONS
    private final static String PIT_ADDITIONAL_CLASSPATH_DEFAULT_FILE_NAME = "pitClasspath"
    @PackageScope   //visible for testing
    final static String PIT_HISTORY_DEFAULT_FILE_NAME = 'pitHistory.txt'

    @SuppressWarnings("FieldName")
    private final static Logger log = Logging.getLogger(PitestPlugin)

    private final GradleVersionEnforcer gradleVersionEnforcer

    private Project project
    private PitestPluginExtension extension

    PitestPlugin() {
        this.gradleVersionEnforcer = GradleVersionEnforcer.defaultEnforcer(MINIMAL_SUPPORTED_GRADLE_VERSION)
    }

    void apply(Project project) {
        this.project = project
        gradleVersionEnforcer.failBuildWithMeaningfulErrorIfAppliedOnTooOldGradleVersion(project)
        Configuration pitestConfiguration = createConfiguration()

        project.plugins.withType(JavaPlugin).configureEach {
            setupExtensionWithDefaults()
            addPitDependencies(pitestConfiguration)
            project.tasks.register(PITEST_TASK_NAME, PitestTask) { t ->
                failWithMeaningfulErrorMessageOnUnsupportedConfigurationInRootProjectBuildScript()
                t.description = "Run PIT analysis for java classes"
                t.group = PITEST_TASK_GROUP
                configureTaskDefault(t)
                t.dependsOn(calculateTasksToDependOn())
                t.shouldRunAfter(project.tasks.named(TEST_TASK_NAME))
                suppressPassingDeprecatedTestPluginForNewerPitVersions(t)
            }
        }
    }

    private Configuration createConfiguration() {
        return project.configurations.maybeCreate(PITEST_CONFIGURATION_NAME).with { configuration ->
            visible = false
            description = "The PIT libraries to be used for this project."
            return configuration
        }
    }

    private void setupExtensionWithDefaults() {
        extension = project.extensions.create("pitest", PitestPluginExtension, project)
        setupReportDirInExtensionWithProblematicTypeForGradle5()
        extension.pitestVersion.set(DEFAULT_PITEST_VERSION)
        SourceSetContainer javaSourceSets = project.extensions.getByType(SourceSetContainer)
        extension.testSourceSets.set(javaSourceSets.named(SourceSet.TEST_SOURCE_SET_NAME).map { SourceSet ss -> List.of(ss) })
        extension.mainSourceSets.set(javaSourceSets.named(SourceSet.MAIN_SOURCE_SET_NAME).map { SourceSet ss -> List.of(ss) })
        extension.fileExtensionsToFilter.set(DEFAULT_FILE_EXTENSIONS_TO_FILTER_FROM_CLASSPATH)
        extension.useClasspathFile.set(false)
        extension.verbosity.set("NO_SPINNER")
        extension.addJUnitPlatformLauncher.set(true)
    }

    private void failWithMeaningfulErrorMessageOnUnsupportedConfigurationInRootProjectBuildScript() {
        //TODO: findByName() is suboptimal, but "named(...).isPreset()" triggers too early initialization with "Configuration with name 'pitest' not found"
        if (project.rootProject.buildscript.configurations.findByName(PITEST_CONFIGURATION_NAME) != null) {
            throw new GradleException("The '${PITEST_CONFIGURATION_NAME}' buildscript configuration found in the root project. " +
                "This is no longer supported in 1.5.0+ and has to be changed to the regular (sub)project configuration. " +
                "See the project FAQ for migration details.")
        }
    }

    @CompileDynamic //To keep Gradle <6.0 compatibility - see https://github.com/gradle/gradle/issues/10953
    private void setupReportDirInExtensionWithProblematicTypeForGradle5() {
        extension.reportDir.set(new File(project.extensions.getByType(ReportingExtension).baseDirectory.asFile.get(), PITEST_REPORT_DIRECTORY_NAME))
    }

    @SuppressWarnings("UnnecessarySetter")  //Due to: task.sourceDirs.setFrom() in CodeNarc
    private void configureTaskDefault(PitestTask task) {
        task.testPlugin.set(extension.testPlugin)
        task.reportDir.set(extension.reportDir)
        task.targetClasses.set(project.providers.provider {
            log.debug("Setting targetClasses. project.getGroup: {}, class: {}", project.getGroup(), project.getGroup()?.class)
            if (extension.targetClasses.isPresent()) {
                return extension.targetClasses.get()
            }
            if (project.getGroup()) {   //Assuming it is always a String class instance
                return [project.getGroup().toString() + ".*"] as Set
            }
            return null
        } as Provider<Iterable<String>>)
        task.targetTests.set(project.providers.provider {   //unless explicitly configured use targetClasses - https://github.com/szpak/gradle-pitest-plugin/issues/144
            if (extension.targetTests.isPresent()) {    //getOrElseGet() is not available - https://github.com/gradle/gradle/issues/10520
                return extension.targetTests.get()
            } else {
                return task.targetClasses.getOrNull()
            }
        } as Provider<Iterable<String>>)
        task.threads.set(extension.threads)
        task.mutators.set(extension.mutators)
        task.excludedMethods.set(extension.excludedMethods)
        task.excludedClasses.set(extension.excludedClasses)
        task.excludedTestClasses.set(extension.excludedTestClasses)
        task.avoidCallsTo.set(extension.avoidCallsTo)
        task.verbose.set(extension.verbose)
        task.verbosity.set(extension.verbosity)
        task.timeoutFactor.set(extension.timeoutFactor)
        task.timeoutConstInMillis.set(extension.timeoutConstInMillis)
        task.childProcessJvmArgs.set(extension.jvmArgs)
        task.outputFormats.set(extension.outputFormats)
        task.failWhenNoMutations.set(extension.failWhenNoMutations)
        task.skipFailingTests.set(extension.skipFailingTests)
        task.includedGroups.set(extension.includedGroups)
        task.excludedGroups.set(extension.excludedGroups)
        task.fullMutationMatrix.set(extension.fullMutationMatrix)
        task.includedTestMethods.set(extension.includedTestMethods)
        task.sourceDirs.setFrom(extension.mainSourceSets.map { mainSourceSet -> mainSourceSet*.allSource*.srcDirs })
        task.detectInlinedCode.set(extension.detectInlinedCode)
        task.timestampedReports.set(extension.timestampedReports)
        Callable<Set<File>> allMutableCodePaths = {
            calculateBaseMutableCodePaths() + (extension.additionalMutableCodePaths.getOrElse([] as Set) as Set<File>)
        }
        task.additionalClasspath.setFrom({
            List<FileCollection> testRuntimeClasspath = (extension.testSourceSets.get() as Set<SourceSet>)*.runtimeClasspath
            FileCollection combinedTaskClasspath = project.objects.fileCollection().from(testRuntimeClasspath)
            List<String> fileExtensionsToFilter = extension.fileExtensionsToFilter.getOrElse([])
            FileCollection filteredCombinedTaskClasspath = combinedTaskClasspath.filter { File file ->
                !fileExtensionsToFilter.find { extension -> file.name.endsWith(".$extension") }
            } + project.files(allMutableCodePaths)
            return filteredCombinedTaskClasspath
        } as Callable<FileCollection>)
        task.useAdditionalClasspathFile.set(extension.useClasspathFile)
        task.additionalClasspathFile.set(new File(project.buildDir, PIT_ADDITIONAL_CLASSPATH_DEFAULT_FILE_NAME))
        task.mutableCodePaths.setFrom(allMutableCodePaths)
        task.historyInputLocation.set(extension.historyInputLocation)
        task.historyOutputLocation.set(extension.historyOutputLocation)
        task.enableDefaultIncrementalAnalysis.set(extension.enableDefaultIncrementalAnalysis)
        task.defaultFileForHistoryData.set(new File(project.buildDir, PIT_HISTORY_DEFAULT_FILE_NAME))
        task.mutationThreshold.set(extension.mutationThreshold)
        task.coverageThreshold.set(extension.coverageThreshold)
        task.testStrengthThreshold.set(extension.testStrengthThreshold)
        task.mutationEngine.set(extension.mutationEngine)
        task.exportLineCoverage.set(extension.exportLineCoverage)
        task.jvmPath.set(extension.jvmPath)
        task.mainProcessJvmArgs.set(extension.mainProcessJvmArgs)
        task.launchClasspath.setFrom({
            project.configurations[PITEST_CONFIGURATION_NAME]
        } as Callable<Configuration>)
        task.pluginConfiguration.set(extension.pluginConfiguration)
        task.maxSurviving.set(extension.maxSurviving)
        task.useClasspathJar.set(extension.useClasspathJar)
        task.inputEncoding.set(extension.inputCharset)
        task.outputEncoding.set(extension.outputCharset)
        task.features.set(extension.features)
    }

    private Set<File> calculateBaseMutableCodePaths() {
        Set<SourceSet> sourceSets = extension.mainSourceSets.get()
        return sourceSets*.output.classesDirs.files.flatten() as Set<File>
    }

    private Set<String> calculateTasksToDependOn() {
        Set<SourceSet> testSourceSets = extension.testSourceSets.get()
        Set<String> tasksToDependOn = testSourceSets.collect { sourceSet -> sourceSet.name + "Classes" } as Set
        log.debug("pitest tasksToDependOn: $tasksToDependOn")
        return tasksToDependOn
    }

    private void addPitDependencies(Configuration pitestConfiguration) {
        pitestConfiguration.withDependencies { dependencies ->
            log.info("Using PIT: ${extension.pitestVersion.get()}")
            dependencies.add(project.dependencies.create("org.pitest:pitest-command-line:${extension.pitestVersion.get()}"))
        }

        addPitJUnit5PluginIfRequested(pitestConfiguration)
    }

    private void addPitJUnit5PluginIfRequested(Configuration pitestConfiguration) {
        pitestConfiguration.withDependencies { dependencies ->
            if (extension.junit5PluginVersion.isPresent()) {
                if (extension.testPlugin.isPresent() && extension.testPlugin.get() != PITEST_JUNIT5_PLUGIN_NAME) {
                    log.warn("Specified 'junit5PluginVersion', but other plugin is configured in 'testPlugin' for PIT: '${extension.testPlugin.get()}'")
                }

                String junit5PluginDependencyAsString = "org.pitest:pitest-junit5-plugin:${extension.junit5PluginVersion.get()}"
                log.info("Adding JUnit 5 plugin for PIT as dependency: ${junit5PluginDependencyAsString}")
                dependencies.add(project.dependencies.create(junit5PluginDependencyAsString))
            }
        }

        addJUnitPlatformLauncherDependencyIfNeeded()
    }

    private void addJUnitPlatformLauncherDependencyIfNeeded() {
        //Starting with Gradle 8.8.0, Configuration implements "Named" which generates runtime error on "testConfiguration.name" for plugin compiled
        //with 8.8.0+ and executed with lower versions. Keep constant name as workaround
        //Related commit: https://github.com/gradle/gradle/commit/61220ea4fdb30b5c7265dd41e7ac4d70896c957b
        final String testImplementationConfigurationName = "testImplementation"

        project.configurations.named(testImplementationConfigurationName).configure { testImplementation ->
            testImplementation.withDependencies { directDependencies ->
                if (!extension.addJUnitPlatformLauncher.isPresent() || !extension.addJUnitPlatformLauncher.get()) {
                    log.info("'addJUnitPlatformLauncher' feature explicitly disabled in configuration. " +
                        "Add junit-platform-launcher manually or expect 'Minion exited abnormally due to UNKNOWN_ERROR' or 'NoClassDefFoundError'")
                    return
                }

                //Note: For simplicity, adding also for older pitest-junit5-plugin versions (<1.2.0), which is not needed

                final String orgJUnitPlatformGroup = "org.junit.platform"

                log.debug("Direct ${testImplementationConfigurationName} dependencies (${directDependencies.size()}): ${directDependencies}")

                //copy() seems to copy also something that refers to original configuration and generates StackOverflow on getting components
                Configuration tmpTestImplementation = project.configurations.maybeCreate("tmpTestImplementation")
                directDependencies.each { directDependency ->
                    tmpTestImplementation.dependencies.add(directDependency)
                }

                ResolutionResult resolutionResult = tmpTestImplementation.incoming.resolutionResult
                Set<ResolvedComponentResult> allResolvedComponents = resolutionResult.allComponents
                log.debug("All resolved components ${testImplementationConfigurationName} (${allResolvedComponents.size()}): ${allResolvedComponents}")

                ResolvedComponentResult foundJunitPlatformComponent = allResolvedComponents.find { ResolvedComponentResult componentResult ->
                    ModuleVersionIdentifier moduleVersion = componentResult.moduleVersion
                    return moduleVersion.group == orgJUnitPlatformGroup &&
                        (moduleVersion.name == "junit-platform-engine" || moduleVersion.name == "junit-platform-commons")
                }

                if (!foundJunitPlatformComponent) {
                    log.info("No ${orgJUnitPlatformGroup} components founds in ${testImplementationConfigurationName}, junit-platform-launcher will not be added")
                    return
                }

                String junitPlatformLauncherDependencyAsString = "${orgJUnitPlatformGroup}:junit-platform-launcher:${foundJunitPlatformComponent.moduleVersion.version}"
                log.info("${orgJUnitPlatformGroup} component (${foundJunitPlatformComponent}) found in ${testImplementationConfigurationName}, " +
                    "adding junit-platform-launcher (${junitPlatformLauncherDependencyAsString}) to testRuntimeOnly")
                project.configurations.named("testRuntimeOnly").configure({ Configuration testRuntimeOnly ->
                    testRuntimeOnly.dependencies.add(project.dependencies.create(junitPlatformLauncherDependencyAsString))
                } as Action<Configuration>)
            }
        }
    }

    private void suppressPassingDeprecatedTestPluginForNewerPitVersions(PitestTask pitestTask) {
        if (extension.testPlugin.isPresent()) {
            log.warn("DEPRECATION WARNING. `testPlugin` is deprecated starting with GPP 1.7.4. It is also not used starting with PIT 1.6.7 (to be removed in 1.8.0).")
            String configuredPitVersion = extension.pitestVersion.get()
            try {
                final GradleVersion minimalPitVersionNotNeedingTestPluginProperty = GradleVersion.version("1.6.7")
                if (GradleVersion.version(configuredPitVersion) >= minimalPitVersionNotNeedingTestPluginProperty) {
                    log.info("Passing '--testPlugin' to PIT disabled for PIT 1.6.7+. See https://github.com/szpak/gradle-pitest-plugin/issues/277")
                    pitestTask.testPlugin.set((String)null)
                }
            } catch (IllegalArgumentException e) {
                log.warn("Error during PIT versions comparison. Is '$configuredPitVersion' really valid? If yes, please report that case. " +
                    "Assuming PIT version is newer than 1.6.7.")
                log.warn("Original exception: ${e.class.name}:${e.message}")
            }
        }
    }

}
