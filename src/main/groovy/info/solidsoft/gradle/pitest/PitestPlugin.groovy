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
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.UnionFileCollection
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaBasePlugin

import static org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP

/**
 * The main class for Pitest plugin.
 */
class PitestPlugin implements Plugin<Project> {
    public final static String DEFAULT_PITEST_VERSION = '1.4.6'
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
        applyRequiredJavaPlugin()
        createConfigurations()
        createExtension(project)
        project.plugins.withType(JavaBasePlugin) {
            PitestTask task = project.tasks.create(PITEST_TASK_NAME, PitestTask)
            task.with {
                description = "Run PIT analysis for java classes"
                group = PITEST_TASK_GROUP
            }
            configureTaskDefault(task)
        }
    }

    private void applyRequiredJavaPlugin() {
        //The new Gradle plugin mechanism requires all mandatory plugins to be applied explicit
        //See: https://github.com/szpak/gradle-pitest-plugin/issues/21
        project.apply(plugin: 'java')
    }

    private void createConfigurations() {
        project.rootProject.buildscript.configurations.maybeCreate(PITEST_CONFIGURATION_NAME).with {
            visible = false
            description = "The Pitest libraries to be used for this project."
        }
    }

    //TODO: MZA: Maybe move it to the constructor of an extension class?
    private void createExtension(Project project) {
        extension = project.extensions.create("pitest", PitestPluginExtension)
        extension.reportDir = new File("${project.reporting.baseDir.path}/pitest")
        extension.pitestVersion = DEFAULT_PITEST_VERSION
        extension.testSourceSets = [project.sourceSets.test] as Set
        extension.mainSourceSets = [project.sourceSets.main] as Set
        extension.fileExtensionsToFilter = DEFAULT_FILE_EXTENSIONS_TO_FILTER_FROM_CLASSPATH
    }

    private void configureTaskDefault(PitestTask task) {
        task.conventionMapping.with {
            additionalClasspath = {
                List<FileCollection> testRuntimeClasspath = extension.testSourceSets*.runtimeClasspath

                FileCollection combinedTaskClasspath = new UnionFileCollection(testRuntimeClasspath)
                FileCollection filteredCombinedTaskClasspath = combinedTaskClasspath.filter { File file ->
                    !extension.fileExtensionsToFilter.find { file.name.endsWith(".$it") }
                }

                return filteredCombinedTaskClasspath
            }
            useAdditionalClasspathFile = { extension.useClasspathFile }
            additionalClasspathFile = { new File(project.buildDir, PIT_ADDITIONAL_CLASSPATH_DEFAULT_FILE_NAME) }
            launchClasspath = {
                project.rootProject.buildscript.configurations[PITEST_CONFIGURATION_NAME]
            }
            mutableCodePaths = { calculateBaseMutableCodePaths() + (extension.additionalMutableCodePaths ?: []) }
            sourceDirs = { extension.mainSourceSets*.allSource.srcDirs.flatten() as Set }

            testPlugin = { extension.testPlugin }
            reportDir = { extension.reportDir }
            targetClasses = {
                log.debug("Setting targetClasses. project.getGroup: {}, class: {}", project.getGroup(), project.getGroup()?.class)
                if (extension.targetClasses) {
                    return extension.targetClasses
                }
                if (project.getGroup()) {   //Assuming it is always a String class instance
                    return [project.getGroup() + ".*"] as Set
                }
                return null
            }
            targetTests = { extension.targetTests }
            dependencyDistance = { extension.dependencyDistance }
            threads = { extension.threads }
            mutateStaticInits = { extension.mutateStaticInits }
            includeJarFiles = { extension.includeJarFiles }
            mutators = { extension.mutators }
            excludedMethods = { extension.excludedMethods }
            excludedClasses = { extension.excludedClasses }
            excludedTestClasses = { extension.excludedTestClasses }
            avoidCallsTo = { extension.avoidCallsTo }
            verbose = { extension.verbose }
            timeoutFactor = { extension.timeoutFactor }
            timeoutConstInMillis = { extension.timeoutConstInMillis }
            maxMutationsPerClass = { extension.maxMutationsPerClass }
            childProcessJvmArgs = { extension.jvmArgs }
            outputFormats = { extension.outputFormats }
            failWhenNoMutations = { extension.failWhenNoMutations }
            includedGroups = { extension.includedGroups }
            excludedGroups = { extension.excludedGroups }
            detectInlinedCode = { extension.detectInlinedCode }
            timestampedReports = { extension.timestampedReports }
            historyInputLocation = { extension.historyInputLocation }
            historyOutputLocation = { extension.historyOutputLocation }
            enableDefaultIncrementalAnalysis = { extension.enableDefaultIncrementalAnalysis }
            defaultFileForHistoryData = { new File(project.buildDir, PIT_HISTORY_DEFAULT_FILE_NAME) }
            mutationThreshold = { extension.mutationThreshold }
            mutationEngine = { extension.mutationEngine }
            coverageThreshold = { extension.coverageThreshold }
            exportLineCoverage = { extension.exportLineCoverage }
            jvmPath = { extension.jvmPath }
            mainProcessJvmArgs = { extension.mainProcessJvmArgs }
            pluginConfiguration = { extension.pluginConfiguration }
            maxSurviving = { extension.maxSurviving }
            features = { extension.features }
        }

        project.afterEvaluate {
            task.dependsOn(calculateTasksToDependOn())

            addPitDependencies()
        }
    }

    @CompileStatic
    private Set<File> calculateBaseMutableCodePaths() {
        return extension.mainSourceSets*.output.classesDirs.files.flatten() as Set<File>
    }

    @CompileStatic
    private boolean isGradleVersionBefore4() {
        String gradleVersionAsString = project.gradle.gradleVersion
        return gradleVersionAsString.startsWith("2.") || gradleVersionAsString.startsWith("3.")
    }

    @CompileStatic
    private Set<String> calculateTasksToDependOn() {
//        //Fails with: NoSuchMethodError: org.codehaus.groovy.runtime.DefaultGroovyMethods.collect(Ljava/lang/Iterable;Lgroovy/lang/Closure;)Ljava/util/List;
//        //when compiled with Groovy 2.5 (Gradle 5+) and executed with Groovy 2.4 (Gradle <5). Explicit coercion doesn't help.
//        //TODO: Workaround with DefaultGroovyMethods.collect. Remove once Gradle 4 support is dropped
//        Set<String> tasksToDependOn = extension.testSourceSets.collect { it.name + "Classes" } as Set
        Set<String> tasksToDependOn = DefaultGroovyMethods.collect(extension.testSourceSets) { it.name + "Classes" } as Set
        log.debug("pitest tasksToDependOn: $tasksToDependOn")
        return tasksToDependOn
    }

    @CompileStatic
    private void addPitDependencies() {
        log.info("Using PIT: $extension.pitestVersion")
        project.rootProject.buildscript.dependencies.add(PITEST_CONFIGURATION_NAME, "org.pitest:pitest-command-line:$extension.pitestVersion")
    }
}
