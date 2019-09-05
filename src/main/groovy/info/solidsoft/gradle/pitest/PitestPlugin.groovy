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
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.UnionFileCollection
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider

import static org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP

/**
 * The main class for Pitest plugin.
 */
class PitestPlugin implements Plugin<Project> {
    public final static String DEFAULT_PITEST_VERSION = '1.4.10'
    public final static String PITEST_TASK_GROUP = VERIFICATION_GROUP
    public final static String PITEST_TASK_NAME = "pitest"
    public final static String PITEST_CONFIGURATION_NAME = 'pitest'

    private final static List<String> DYNAMIC_LIBRARY_EXTENSIONS = ['so', 'dll', 'dylib']
    private final static List<String> DEFAULT_FILE_EXTENSIONS_TO_FILTER_FROM_CLASSPATH = ['pom'] + DYNAMIC_LIBRARY_EXTENSIONS

    private final static Logger log =  Logging.getLogger(PitestPlugin)

    @PackageScope   //visible for testing
    final static String PIT_HISTORY_DEFAULT_FILE_NAME = 'pitHistory.txt'
    private final static String PIT_ADDITIONAL_CLASSPATH_DEFAULT_FILE_NAME = "pitClasspath"

    private Project project
    private PitestPluginExtension extension

    void apply(Project project) {
        this.project = project
        project.plugins.withType(JavaPlugin).configureEach {
            createExtensionAndSetDefaultValues()
            project.tasks.register(PITEST_TASK_NAME, PitestTask) { t ->
                t.description = "Run PIT analysis for java classes"
                t.group = PITEST_TASK_GROUP
                configureTaskDefault(t)
                t.dependsOn(calculateTasksToDependOn())
                addPitDependencies(createConfigurations())
            }
        }
    }

    private Configuration createConfigurations() {
        return project.rootProject.buildscript.configurations.maybeCreate(PITEST_CONFIGURATION_NAME).with {
            visible = false
            description = "The Pitest libraries to be used for this project."
            return it
        }
    }

    private void createExtensionAndSetDefaultValues() {
        extension = project.extensions.create("pitest", PitestPluginExtension, project)
        extension.reportDir = new File("${project.reporting.baseDir.path}/pitest")
        extension.pitestVersion.set(DEFAULT_PITEST_VERSION)
        extension.testSourceSets.set([project.sourceSets.test] as Set)
        extension.mainSourceSets.set([project.sourceSets.main] as Set)
        extension.fileExtensionsToFilter.set(DEFAULT_FILE_EXTENSIONS_TO_FILTER_FROM_CLASSPATH)
        extension.useClasspathFile.set(false)
    }

    private void configureTaskDefault(PitestTask task) {

        task.testPlugin.set(extension.testPlugin)
//        task.reportDir.set(extension.reportDir)
        task.targetClasses.set(project.providers.provider() {
                log.debug("Setting targetClasses. project.getGroup: {}, class: {}", project.getGroup(), project.getGroup()?.class)
                if (extension.targetClasses.isPresent()) {
                    return extension.targetClasses.get()
                }
                if (project.getGroup()) {   //Assuming it is always a String class instance
                    return [project.getGroup() + ".*"] as Set
                }
                return null
            }
        )
        task.targetTests.set(extension.targetTests)
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
        task.includedGroups.set(extension.includedGroups)
        task.excludedGroups.set(extension.excludedGroups)
//        task.sourceDirs.set(project.providers.provider() { extension.mainSourceSets*.allSource.srcDirs.flatten() as Set })
        task.detectInlinedCode.set(extension.detectInlinedCode)
        task.timestampedReports.set(extension.timestampedReports)
        task.enableDefaultIncrementalAnalysis.set(extension.enableDefaultIncrementalAnalysis)
        task.mutationThreshold.set(extension.mutationThreshold)
        task.coverageThreshold.set(extension.coverageThreshold)
        task.mutationEngine.set(extension.mutationEngine)
        task.exportLineCoverage.set(extension.exportLineCoverage)
//        task.jvmPath.set(extension.jvmPath)
        task.mainProcessJvmArgs.set(extension.mainProcessJvmArgs)
//        task.mutableCodePaths.set(extension.additionalMutableCodePaths)
//        task.pluginConfiguration.set(extension.pluginConfiguration)
        task.maxSurviving.set(extension.maxSurviving)
        task.useAdditionalClasspathFile.set(extension.useClasspathFile)
        task.features.set(extension.features)

        //Temporarily for types not supported in Gradle 4.x
        task.conventionMapping.with {
            additionalClasspath = {
                List<FileCollection> testRuntimeClasspath = extension.testSourceSets.get()*.runtimeClasspath

                FileCollection combinedTaskClasspath = new UnionFileCollection(testRuntimeClasspath)
                FileCollection filteredCombinedTaskClasspath = combinedTaskClasspath.filter { File file ->
                    !extension.fileExtensionsToFilter.getOrNull().find { file.name.endsWith(".$it") }
                }

                return filteredCombinedTaskClasspath
            }
            additionalClasspathFile = { new File(project.buildDir, PIT_ADDITIONAL_CLASSPATH_DEFAULT_FILE_NAME) }
            launchClasspath = {
                project.rootProject.buildscript.configurations[PITEST_CONFIGURATION_NAME]
            }
            mutableCodePaths = { calculateBaseMutableCodePaths() + (extension.additionalMutableCodePaths ?: []) }
            sourceDirs = { extension.mainSourceSets.get()*.allSource.srcDirs.flatten() as Set }
            reportDir = { extension.reportDir }
            historyInputLocation = { extension.historyInputLocation }
            historyOutputLocation = { extension.historyOutputLocation }
            defaultFileForHistoryData = { new File(project.buildDir, PIT_HISTORY_DEFAULT_FILE_NAME) }
            jvmPath = { extension.jvmPath }
            pluginConfiguration = { extension.pluginConfiguration }
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
        Set<String> tasksToDependOn = DefaultGroovyMethods.collect(testSourceSets) { SourceSet it -> it.name + "Classes" } as Set
        log.debug("pitest tasksToDependOn: $tasksToDependOn")
        return tasksToDependOn
    }

    @CompileStatic
    private void addPitDependencies(Configuration pitestConfiguration) {
        log.info("Using PIT: ${extension.pitestVersion.get()}")
        pitestConfiguration.dependencies.add(project.dependencies.create("org.pitest:pitest-command-line:${extension.pitestVersion.get()}"))
    }
}
