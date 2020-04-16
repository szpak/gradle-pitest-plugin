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

import static org.gradle.api.plugins.JavaPlugin.TEST_TASK_NAME
import static org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import info.solidsoft.gradle.pitest.internal.GradleVersionEnforcer
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.SourceSet
import org.gradle.util.GradleVersion

/**
 * The main class for Pitest plugin.
 */
@CompileDynamic
class PitestPlugin implements Plugin<Project> {

    public final static String DEFAULT_PITEST_VERSION = '1.5.1'
    public final static String PITEST_TASK_GROUP = VERIFICATION_GROUP
    public final static String PITEST_TASK_NAME = "pitest"
    public final static String PITEST_CONFIGURATION_NAME = 'pitest'

    private static final String PITEST_JUNIT5_PLUGIN_NAME = "junit5"

    @Internal
    public static final GradleVersion MINIMAL_SUPPORTED_GRADLE_VERSION = GradleVersion.version("5.6") //public as used also in regression tests
    public static final String PLUGIN_ID = "info.solidsoft.pitest"

    private final static List<String> DYNAMIC_LIBRARY_EXTENSIONS = ['so', 'dll', 'dylib']
    private final static List<String> DEFAULT_FILE_EXTENSIONS_TO_FILTER_FROM_CLASSPATH = ['pom'] + DYNAMIC_LIBRARY_EXTENSIONS

    @SuppressWarnings("FieldName")
    private final static Logger log = Logging.getLogger(PitestPlugin)

    @PackageScope   //visible for testing
    final static String PIT_HISTORY_DEFAULT_FILE_NAME = 'pitHistory.txt'
    private final static String PIT_ADDITIONAL_CLASSPATH_DEFAULT_FILE_NAME = "pitClasspath"

    private final GradleVersionEnforcer gradleVersionEnforcer

    private Project project
    private PitestPluginExtension extension

    PitestPlugin() {
        this.gradleVersionEnforcer = GradleVersionEnforcer.defaultEnforcer(MINIMAL_SUPPORTED_GRADLE_VERSION)
    }

    void apply(Project project) {
        this.project = project
        gradleVersionEnforcer.failBuildWithMeaningfulErrorIfAppliedOnTooOldGradleVersion(project)
        project.plugins.withType(JavaPlugin).configureEach {
            setupExtensionWithDefaults()
            project.tasks.register(PITEST_TASK_NAME, PitestTask) { t ->
                t.description = "Run PIT analysis for java classes"
                t.group = PITEST_TASK_GROUP
                configureTaskDefault(t)
                t.dependsOn(calculateTasksToDependOn())
                t.shouldRunAfter(project.tasks.named(TEST_TASK_NAME))
                addPitDependencies(configuration())
            }
        }
    }

    private Configuration configuration() {
        return project.rootProject.buildscript.configurations.maybeCreate(PITEST_CONFIGURATION_NAME).with { configuration ->
            visible = false
            description = "The Pitest libraries to be used for this project."
            return configuration
        }
    }

    private void setupExtensionWithDefaults() {
        extension = project.extensions.create("pitest", PitestPluginExtension, project)
        extension.reportDir.set(new File(project.reporting.baseDir, "pitest"))
        extension.pitestVersion.set(DEFAULT_PITEST_VERSION)
        extension.testSourceSets.set([project.sourceSets.test] as Set)
        extension.mainSourceSets.set([project.sourceSets.main] as Set)
        extension.fileExtensionsToFilter.set(DEFAULT_FILE_EXTENSIONS_TO_FILTER_FROM_CLASSPATH)
        extension.useClasspathFile.set(false)
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
                return [project.getGroup() + ".*"] as Set
            }
            return null
        })
        task.targetTests.set(project.providers.provider {   //unless explicitly configured use targetClasses - https://github.com/szpak/gradle-pitest-plugin/issues/144
            if (extension.targetTests.isPresent()) {    //getOrElseGet() is not available - https://github.com/gradle/gradle/issues/10520
                return extension.targetTests.get()
            } else {
                return task.targetClasses.getOrNull()
            }
        })
        task.dependencyDistance.set(extension.dependencyDistance)
        task.threads.set(extension.threads)
        task.mutateStaticInits.set(extension.mutateStaticInits)
        task.includeJarFiles.set(extension.includeJarFiles)
        task.mutators.set(extension.mutators)
        task.excludedMethods.set(extension.excludedMethods)
        task.excludedClasses.set(extension.excludedClasses)
        task.excludedTestClasses.set(extension.excludedTestClasses)
        task.avoidCallsTo.set(extension.avoidCallsTo)
        task.verbose.set(extension.verbose)
        task.timeoutFactor.set(extension.timeoutFactor)
        task.timeoutConstInMillis.set(extension.timeoutConstInMillis)
        task.maxMutationsPerClass.set(extension.maxMutationsPerClass)
        task.childProcessJvmArgs.set(extension.jvmArgs)
        task.outputFormats.set(extension.outputFormats)
        task.failWhenNoMutations.set(extension.failWhenNoMutations)
        task.skipFailingTests.set(extension.skipFailingTests)
        task.includedGroups.set(extension.includedGroups)
        task.excludedGroups.set(extension.excludedGroups)
        task.fullMutationMatrix.set(extension.fullMutationMatrix)
        task.includedTestMethods.set(extension.includedTestMethods)
        task.sourceDirs.setFrom(extension.mainSourceSets.get()*.allSource)
        task.detectInlinedCode.set(extension.detectInlinedCode)
        task.timestampedReports.set(extension.timestampedReports)
        task.enableDefaultIncrementalAnalysis.set(extension.enableDefaultIncrementalAnalysis)
        task.mutationThreshold.set(extension.mutationThreshold)
        task.coverageThreshold.set(extension.coverageThreshold)
        task.mutationEngine.set(extension.mutationEngine)
        task.exportLineCoverage.set(extension.exportLineCoverage)
        task.defaultFileForHistoryData.set(new File(project.buildDir, PIT_HISTORY_DEFAULT_FILE_NAME))
        task.jvmPath.set(extension.jvmPath)
        task.mainProcessJvmArgs.set(extension.mainProcessJvmArgs)
//        task.mutableCodePaths.set(extension.additionalMutableCodePaths)
        task.historyInputLocation.set(extension.historyInputLocation)
        task.historyOutputLocation.set(extension.historyOutputLocation)
        task.pluginConfiguration.set(extension.pluginConfiguration)
        task.maxSurviving.set(extension.maxSurviving)
        task.useClasspathJar.set(extension.useClasspathJar)
        task.useAdditionalClasspathFile.set(extension.useClasspathFile)
        task.additionalClasspathFile.set(new File(project.buildDir, PIT_ADDITIONAL_CLASSPATH_DEFAULT_FILE_NAME))
        task.features.set(extension.features)

        //Temporarily for types not supported in Gradle 4.x
        task.conventionMapping.with {
            additionalClasspath = {
                List<FileCollection> testRuntimeClasspath = extension.testSourceSets.get()*.runtimeClasspath
                FileCollection combinedTaskClasspath = project.objects.fileCollection().from(testRuntimeClasspath)
                FileCollection filteredCombinedTaskClasspath = combinedTaskClasspath.filter { File file ->
                    !extension.fileExtensionsToFilter.getOrNull().find { extension -> file.name.endsWith(".$extension") }
                }

                return filteredCombinedTaskClasspath
            }
            launchClasspath = {
                project.rootProject.buildscript.configurations[PITEST_CONFIGURATION_NAME]
            }
            mutableCodePaths = { calculateBaseMutableCodePaths() + (extension.additionalMutableCodePaths ?: []) }
        }
    }

    @CompileStatic
    private Set<File> calculateBaseMutableCodePaths() {
        Set<SourceSet> sourceSets = extension.mainSourceSets.get()
        return sourceSets*.output.classesDirs.files.flatten() as Set<File>
    }

    @CompileStatic
    private Set<String> calculateTasksToDependOn() {
//        //Fails with: NoSuchMethodError: org.codehaus.groovy.runtime.DefaultGroovyMethods.collect(Ljava/lang/Iterable;Lgroovy/lang/Closure;)Ljava/util/List;
//        //when compiled with Groovy 2.5 (Gradle 5+) and executed with Groovy 2.4 (Gradle <5). Explicit coercion doesn't help.
//        //TODO: Workaround with DefaultGroovyMethods.collect. Remove once Gradle 4 support is dropped
//        Set<String> tasksToDependOn = extension.testSourceSets.collect { it.name + "Classes" } as Set
        Set<SourceSet> testSourceSets = extension.testSourceSets.get()
        Set<String> tasksToDependOn = DefaultGroovyMethods.collect(testSourceSets) { sourceSet -> sourceSet.name + "Classes" } as Set
        log.debug("pitest tasksToDependOn: $tasksToDependOn")
        return tasksToDependOn
    }

    @CompileStatic
    private void addPitDependencies(Configuration pitestConfiguration) {
        log.info("Using PIT: ${extension.pitestVersion.get()}")
        pitestConfiguration.dependencies.add(project.dependencies.create("org.pitest:pitest-command-line:${extension.pitestVersion.get()}"))

        addPitJUnit5PluginIfRequested(pitestConfiguration)
    }

    private void addPitJUnit5PluginIfRequested(Configuration pitestConfiguration) {
        if (extension.junit5PluginVersion.isPresent()) {
            if (!extension.testPlugin.isPresent()) {
                log.info("Implicitly using JUnit 5 plugin for PIT with version defined in 'junit5PluginVersion'")
                extension.testPlugin.set(PITEST_JUNIT5_PLUGIN_NAME)
            }
            if (extension.testPlugin.isPresent() && extension.testPlugin.get() != PITEST_JUNIT5_PLUGIN_NAME) {
                log.warn("Specified 'junit5PluginVersion', but other plugin is configured in 'testPlugin' for PIT: '${extension.testPlugin.get()}'")
            }

            String junit5PluginDependencyAsString = "org.pitest:pitest-junit5-plugin:${extension.junit5PluginVersion.get()}"
            log.info("Adding dependency: ${junit5PluginDependencyAsString}")
            pitestConfiguration.dependencies.add(project.dependencies.create(junit5PluginDependencyAsString))
        }
    }

}
