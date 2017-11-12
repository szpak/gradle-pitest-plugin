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
import info.solidsoft.gradle.pitest.extension.PitestPluginExtension
import info.solidsoft.gradle.pitest.extension.ScmPitestPluginExtension
import info.solidsoft.gradle.pitest.task.AbstractPitestTask
import info.solidsoft.gradle.pitest.task.PitestTask
import info.solidsoft.gradle.pitest.task.ScmPitestTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.UnionFileCollection
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaBasePlugin

/**
 * The main class for Pitest plugin.
 */
class PitestPlugin implements Plugin<Project> {
    public final static String DEFAULT_PITEST_VERSION = '1.2.2'
    public final static String PITEST_TASK_GROUP = "Report"
    public final static String PITEST_TASK_NAME = "pitest"

    private final static List<String> DYNAMIC_LIBRARY_EXTENSIONS = ['so', 'dll', 'dylib']
    private final static List<String> FILE_EXTENSIONS_TO_FILTER_FROM_CLASSPATH = ['pom'] + DYNAMIC_LIBRARY_EXTENSIONS

    private final static Logger log = Logging.getLogger(PitestPlugin)

    @PackageScope
    //visible for testing
    final static String PIT_HISTORY_DEFAULT_FILE_NAME = 'pitHistory.txt'
    private final static String PIT_ADDITIONAL_CLASSPATH_DEFAULT_FILE_NAME = "pitClasspath"

    private Project project
    private PitestPluginExtension pitestExtension
    private ScmPitestPluginExtension scmPitestExtension

    private PitestTask pitestTask
    private ScmPitestTask scmPitestTask

    void apply(Project project) {
        this.project = project
        applyRequiredJavaPlugin()
        createPitestConfiguration()
        pitestExtension = project.extensions.create(PluginConstants.PITEST_EXTENSION_NAME, PitestPluginExtension, project)
        scmPitestExtension = project.extensions.create(PluginConstants.SCM_PITEST_EXTENSION_NAME, ScmPitestPluginExtension, project)
        project.plugins.withType(JavaBasePlugin) {
            pitestTask = project.tasks.create(PluginConstants.PITEST_TASK_NAME, PitestTask)
            scmPitestTask = project.tasks.create(PluginConstants.SCM_PITEST_TASK_NAME, ScmPitestTask)
            configurePitestTaskFromExtension(pitestTask, pitestExtension)
            scmPitestTask.addValidator(new GoalValidator(scmPitestTask))
            scmPitestTask.addValidator(new ConnectionTypeValidator(scmPitestTask))
            scmPitestTask.addValidator(new CustomStrategyValidator(scmPitestTask))
            configureScmTaskFromExtension(scmPitestTask, scmPitestExtension)
        }
        project.afterEvaluate {
            pitestTask.dependsOn(calculateTasksToDependOn())
            scmPitestTask.dependsOn(calculateTasksToDependOn())
            addPitestCommandLineToConfiguration()
        }
    }

    private void applyRequiredJavaPlugin() {
        //The new Gradle plugin mechanism requires all mandatory plugins to be applied explicit
        //See: https://github.com/szpak/gradle-pitest-plugin/issues/21
        project.apply(plugin: 'java')
    }

    private void createPitestConfiguration() {
        project.rootProject.buildscript.configurations.maybeCreate(PluginConstants.PITEST_CONFIGURATION_NAME).with {
            visible = false
            description = "The Pitest libraries to be used for this project."
        }
    }

    private void configureScmTaskFromExtension(ScmPitestTask task, ScmPitestPluginExtension extension) {
        configurePitestTaskFromExtension(task, extension)
        task.conventionMapping.with {
            scm = { extension.scm }
            connectionType = { extension.connectionType }
            goal = {extension.goal}
            scmRoot = { extension.scmRoot }
            startVersion = { extension.startVersion }
            startVersionType = { extension.startVersionType }
            endVersion = { extension.endVersion }
            endVersionType = { extension.endVersionType }
            includes = {extension.includes}
            managerClasspath = { extension.managerClasspath }
        }
    }

    private void configurePitestTaskFromExtension(AbstractPitestTask task, PitestPluginExtension pitestExtension) {
        task.conventionMapping.with {
            additionalClasspath = {
                List<FileCollection> testRuntimeClasspath = pitestExtension.testSourceSets*.runtimeClasspath

                FileCollection combinedTaskClasspath = new UnionFileCollection(testRuntimeClasspath)
                FileCollection filteredCombinedTaskClasspath = combinedTaskClasspath.filter { File file ->
                    !FILE_EXTENSIONS_TO_FILTER_FROM_CLASSPATH.find { file.name.endsWith(".$it") }
                }

                return filteredCombinedTaskClasspath
            }
            pitestVersion = {pitestExtension.pitestVersion}
            useAdditionalClasspathFile = { pitestExtension.useClasspathFile }
            additionalClasspathFile = { new File(project.buildDir, PIT_ADDITIONAL_CLASSPATH_DEFAULT_FILE_NAME) }
            launchClasspath = {
                project.rootProject.buildscript.configurations[PluginConstants.PITEST_CONFIGURATION_NAME]
            }
            mutableCodePaths = { calculateBaseMutableCodePaths() + (pitestExtension.additionalMutableCodePaths ?: []) }
            sourceDirs = { pitestExtension.mainSourceSets*.allSource.srcDirs.flatten() as Set }

            reportDir = { pitestExtension.reportDir }
            targetClasses = {
                log.debug("Setting targetClasses. project.getGroup: {}, class: {}", project.getGroup(), project.getGroup()?.class)
                if (pitestExtension.targetClasses) {
                    return pitestExtension.targetClasses
                }
                if (project.getGroup()) {   //Assuming it is always a String class instance
                    return [project.getGroup() + ".*"] as Set
                }
                return null
            }
            targetTests = { pitestExtension.targetTests }
            dependencyDistance = { pitestExtension.dependencyDistance }
            threads = { pitestExtension.threads }
            mutateStaticInits = { pitestExtension.mutateStaticInits }
            includeJarFiles = { pitestExtension.includeJarFiles }
            mutators = { pitestExtension.mutators }
            excludedMethods = { pitestExtension.excludedMethods }
            excludedClasses = { pitestExtension.excludedClasses }
            avoidCallsTo = { pitestExtension.avoidCallsTo }
            verbose = { pitestExtension.verbose }
            timeoutFactor = { pitestExtension.timeoutFactor }
            timeoutConstInMillis = { pitestExtension.timeoutConstInMillis }
            maxMutationsPerClass = { pitestExtension.maxMutationsPerClass }
            childProcessJvmArgs = { pitestExtension.jvmArgs }
            outputFormats = { pitestExtension.outputFormats }
            failWhenNoMutations = { pitestExtension.failWhenNoMutations }
            includedGroups = { pitestExtension.includedGroups }
            excludedGroups = { pitestExtension.excludedGroups }
            detectInlinedCode = { pitestExtension.detectInlinedCode }
            timestampedReports = { pitestExtension.timestampedReports }
            historyInputLocation = { pitestExtension.historyInputLocation }
            historyOutputLocation = { pitestExtension.historyOutputLocation }
            enableDefaultIncrementalAnalysis = { pitestExtension.enableDefaultIncrementalAnalysis }
            defaultFileForHistoryData = { new File(project.buildDir, PIT_HISTORY_DEFAULT_FILE_NAME) }
            mutationThreshold = { pitestExtension.mutationThreshold }
            mutationEngine = { pitestExtension.mutationEngine }
            coverageThreshold = { pitestExtension.coverageThreshold }
            exportLineCoverage = { pitestExtension.exportLineCoverage }
            jvmPath = { pitestExtension.jvmPath }
            mainProcessJvmArgs = { pitestExtension.mainProcessJvmArgs }
            pluginConfiguration = { pitestExtension.pluginConfiguration }
            maxSurviving = { pitestExtension.maxSurviving }
            features = { pitestExtension.features }
        }
    }

    @CompileStatic
    private Set<File> calculateBaseMutableCodePaths() {
        if (isGradleVersionBefore4()) {
            log.warn("WARNING. Support for Gradle <4.0 in gradle-pitest-plugin is deprecated (due to incompatible changes in Gradle itself).")
            //Casting to Iterable to eliminate "NoSuchMethodError: org.codehaus.groovy.runtime.DefaultGroovyMethods.flatten(Ljava/util/List;)Ljava/util/List;"
            //while compiling code with Groovy 2.4.11 (Gradle 4.1) and running with Groovy 2.3.2 (Gradle 2.0)
            return ((Iterable<File>) pitestExtension.mainSourceSets*.output.classesDir).flatten() as Set<File>
        } else {
            return pitestExtension.mainSourceSets*.output.classesDirs.files.flatten() as Set<File>
        }
    }

    @CompileStatic
    private boolean isGradleVersionBefore4() {
        String gradleVersionAsString = project.gradle.gradleVersion
        return gradleVersionAsString.startsWith("2.") || gradleVersionAsString.startsWith("3.")
    }

    @CompileStatic
    private Set<String> calculateTasksToDependOn() {
        Set<String> tasksToDependOn = pitestExtension.testSourceSets.collect { it.name + "Classes" } as Set
        log.debug("pitest tasksToDependOn: $tasksToDependOn")
        return tasksToDependOn
    }

    @CompileStatic
    private void addPitestCommandLineToConfiguration() {
        log.info("Using PIT: $pitestExtension.pitestVersion")
        project.rootProject.buildscript.dependencies.add(PluginConstants.PITEST_CONFIGURATION_NAME, "org.pitest:pitest-command-line:$pitestExtension.pitestVersion")
    }
}
