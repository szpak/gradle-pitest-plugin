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
package pl.droidsonroids.gradle.pitest

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestPlugin
import com.android.build.gradle.api.AndroidSourceSet
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.TestedVariant
import com.android.builder.model.AndroidProject
import groovy.transform.CompileDynamic
import groovy.transform.PackageScope
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.DefaultDomainObjectSet
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.util.VersionNumber

import java.util.concurrent.Callable

import static com.android.builder.model.Version.ANDROID_GRADLE_PLUGIN_VERSION
import static org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP

/**
 * The main class for Pitest plugin.
 */
@CompileDynamic
class PitestPlugin implements Plugin<Project> {

    public final static String DEFAULT_PITEST_VERSION = '1.5.1'
    public final static String PITEST_TASK_GROUP = VERIFICATION_GROUP
    public final static String PITEST_TASK_NAME = "pitest"
    public final static String PITEST_CONFIGURATION_NAME = 'pitest'
    public final static String PITEST_TEST_COMPILE_CONFIGURATION_NAME = 'pitestTestCompile'

    private static final String PITEST_JUNIT5_PLUGIN_NAME = "junit5"
    private final static List<String> DYNAMIC_LIBRARY_EXTENSIONS = ['so', 'dll', 'dylib']
    private final static List<String> DEFAULT_FILE_EXTENSIONS_TO_FILTER_FROM_CLASSPATH = ['pom'] + DYNAMIC_LIBRARY_EXTENSIONS

    @SuppressWarnings("FieldName")
    private final static Logger log = Logging.getLogger(PitestPlugin)
    private final static VersionNumber ANDROID_GRADLE_PLUGIN_VERSION_NUMBER = VersionNumber.parse(ANDROID_GRADLE_PLUGIN_VERSION)

    @PackageScope
    //visible for testing
    final static String PIT_HISTORY_DEFAULT_FILE_NAME = 'pitHistory.txt'
    private final static String PIT_ADDITIONAL_CLASSPATH_DEFAULT_FILE_NAME = "pitClasspath"

    private Project project
    private PitestPluginExtension pitestExtension

    static String sanitizeSdkVersion(String version) {
        return version.replaceAll('[^\\p{Alnum}.-]', '-')
    }

    static JavaCompile getJavaCompileTask(BaseVariant variant) {
        if (ANDROID_GRADLE_PLUGIN_VERSION_NUMBER >= VersionNumber.parse("3.3")) {
            return variant.javaCompileProvider.get()
        } else {
            return variant.javaCompile
        }
    }

    void apply(Project project) {
        this.project = project
        createConfigurations()

        pitestExtension = project.extensions.create("pitest", PitestPluginExtension, project)
        pitestExtension.pitestVersion.set(DEFAULT_PITEST_VERSION)
        pitestExtension.fileExtensionsToFilter.set(DEFAULT_FILE_EXTENSIONS_TO_FILTER_FROM_CLASSPATH)
        pitestExtension.useClasspathFile.set(false)

        project.pluginManager.apply(BasePlugin)

        project.plugins.whenPluginAdded {
            ReportingExtension reportingExtension = project.extensions.findByType(ReportingExtension)
            if (reportingExtension != null) {
                pitestExtension.reportDir.set(new File(reportingExtension.baseDir, "pitest"))
            }
        }

        project.afterEvaluate {
            if (pitestExtension.mainSourceSets.empty()) {
                pitestExtension.mainSourceSets.set(project.android.sourceSets.main as Set<AndroidSourceSet>)
            }
            if (pitestExtension.testSourceSets.empty()) {
                pitestExtension.testSourceSets.set(project.android.sourceSets.test as Set<AndroidSourceSet>)
            }

            project.plugins.withType(AppPlugin) { createPitestTasks(project.android.applicationVariants) }
            project.plugins.withType(LibraryPlugin) { createPitestTasks(project.android.libraryVariants) }
            project.plugins.withType(TestPlugin) { createPitestTasks(project.android.testVariants) }
            addPitDependencies()
        }
    }

    @SuppressWarnings("BuilderMethodWithSideEffects")
    private void createPitestTasks(DefaultDomainObjectSet<? extends BaseVariant> variants) {
        Task globalTask = project.tasks.create(PITEST_TASK_NAME)
        globalTask.with {
            description = "Run PIT analysis for java classes, for all build variants"
            group = PITEST_TASK_GROUP
            shouldRunAfter("test")
        }

        variants.all { BaseVariant variant ->
            PitestTask variantTask = project.tasks.create("${PITEST_TASK_NAME}${variant.name.capitalize()}", PitestTask)

            Task mockableAndroidJarTask
            if (ANDROID_GRADLE_PLUGIN_VERSION_NUMBER < new VersionNumber(3, 2, 0, null)) {
                mockableAndroidJarTask = project.tasks.findByName("mockableAndroidJar")
                configureTaskDefault(variantTask, variant, getMockableAndroidJar(project.android))
            } else {
                mockableAndroidJarTask = project.tasks.maybeCreate("pitestMockableAndroidJar", PitestMockableAndroidJarTask)
                configureTaskDefault(variantTask, variant, mockableAndroidJarTask.outputJar)
            }

            if (!pitestExtension.excludeMockableAndroidJar.getOrElse(false)) {
                variantTask.dependsOn mockableAndroidJarTask
            }

            variantTask.with {
                description = "Run PIT analysis for java classes, for ${variant.name} build variant"
                group = PITEST_TASK_GROUP
                shouldRunAfter("test${variant.name.capitalize()}UnitTest")
            }
            variantTask.dependsOn "compile${variant.name.capitalize()}UnitTestSources"
            globalTask.dependsOn variantTask
        }
    }

    @SuppressWarnings("BuilderMethodWithSideEffects")
    private void createConfigurations() {
        [PITEST_CONFIGURATION_NAME, PITEST_TEST_COMPILE_CONFIGURATION_NAME].each { configuration ->
            project.buildscript.configurations.maybeCreate(configuration).with {
                visible = false
                description = "The PIT libraries to be used for this project."
            }
        }
        project.configurations {
            pitestRuntimeOnly.extendsFrom testRuntimeOnly
        }
    }

    @SuppressWarnings(["Instanceof", "UnnecessarySetter", "DuplicateNumberLiteral"])
    private void configureTaskDefault(PitestTask task, BaseVariant variant, File mockableAndroidJar) {
        FileCollection combinedTaskClasspath = project.files()

        combinedTaskClasspath.with {
            from(project.buildscript.configurations[PITEST_TEST_COMPILE_CONFIGURATION_NAME])
            if (!pitestExtension.excludeMockableAndroidJar.getOrElse(false)) {
                from(mockableAndroidJar)
            }

            if (ANDROID_GRADLE_PLUGIN_VERSION_NUMBER.major == 3) {
                if (ANDROID_GRADLE_PLUGIN_VERSION_NUMBER.minor < 3) {
                    from(project.configurations["${variant.name}CompileClasspath"].copyRecursive { configuration ->
                        configuration.properties.dependencyProject == null
                    })
                    from(project.configurations["${variant.name}UnitTestCompileClasspath"].copyRecursive { configuration ->
                        configuration.properties.dependencyProject == null
                    })
                } else if (ANDROID_GRADLE_PLUGIN_VERSION_NUMBER.minor < 4) {
                    from(project.configurations["${variant.name}CompileClasspath"])
                    from(project.configurations["${variant.name}UnitTestCompileClasspath"])
                }
            } else if (ANDROID_GRADLE_PLUGIN_VERSION_NUMBER.major == 4) {
                from(project.configurations["compile"])
                from(project.configurations["testCompile"])
            }
            from(project.configurations["pitestRuntimeOnly"])
            from(project.files("${project.buildDir}/intermediates/sourceFolderJavaResources/${variant.dirName}"))
            from(project.files("${project.buildDir}/intermediates/sourceFolderJavaResources/test/${variant.dirName}"))
            from(project.files("${project.buildDir}/intermediates/java_res/${variant.dirName}/out"))
            from(project.files("${project.buildDir}/intermediates/java_res/${variant.dirName}UnitTest/out"))
            from(project.files("${project.buildDir}/intermediates/unitTestConfig/test/${variant.dirName}"))
            if (variant instanceof TestedVariant) {
                variant.unitTestVariant?.with { unitTestVariant ->
                    from(getJavaCompileTask(unitTestVariant).classpath)
                    from(project.files(getJavaCompileTask(unitTestVariant).destinationDir))
                }
            }
            from(getJavaCompileTask(variant).classpath)
            from(project.files(getJavaCompileTask(variant).destinationDir))
        }

        task.with {
            defaultFileForHistoryData.set(new File(project.buildDir, PIT_HISTORY_DEFAULT_FILE_NAME))
            testPlugin.set(pitestExtension.testPlugin)
            reportDir.set(pitestExtension.reportDir)
            targetClasses.set(project.providers.provider {
                log.debug("Setting targetClasses. project.getGroup: {}, class: {}", project.getGroup(), project.getGroup()?.class)
                if (pitestExtension.targetClasses.isPresent()) {
                    return pitestExtension.targetClasses.get()
                }
                if (project.getGroup()) {   //Assuming it is always a String class instance
                    return [project.getGroup() + ".*"] as Set
                }
                return null
            } as Provider<Iterable<String>>)
            targetTests.set(project.providers.provider {
                //unless explicitly configured use targetClasses - https://github.com/szpak/gradle-pitest-plugin/issues/144
                if (pitestExtension.targetTests.isPresent()) {
                    //getOrElseGet() is not available - https://github.com/gradle/gradle/issues/10520
                    return pitestExtension.targetTests.get()
                } else {
                    return targetClasses.getOrNull()
                }
            } as Provider<Iterable<String>>)
            dependencyDistance.set(pitestExtension.dependencyDistance)
            threads.set(pitestExtension.threads)
            mutateStaticInits.set(pitestExtension.mutateStaticInits)
            includeJarFiles.set(pitestExtension.includeJarFiles)
            mutators.set(pitestExtension.mutators)
            excludedMethods.set(pitestExtension.excludedMethods)
            excludedClasses.set(pitestExtension.excludedClasses)
            excludedTestClasses.set(pitestExtension.excludedTestClasses)
            avoidCallsTo.set(pitestExtension.avoidCallsTo)
            verbose.set(pitestExtension.verbose)
            timeoutFactor.set(pitestExtension.timeoutFactor)
            timeoutConstInMillis.set(pitestExtension.timeoutConstInMillis)
            maxMutationsPerClass.set(pitestExtension.maxMutationsPerClass)
            childProcessJvmArgs.set(pitestExtension.jvmArgs)
            outputFormats.set(pitestExtension.outputFormats)
            failWhenNoMutations.set(pitestExtension.failWhenNoMutations)
            skipFailingTests.set(pitestExtension.skipFailingTests)
            includedGroups.set(pitestExtension.includedGroups)
            excludedGroups.set(pitestExtension.excludedGroups)
            fullMutationMatrix.set(pitestExtension.fullMutationMatrix)
            includedTestMethods.set(pitestExtension.includedTestMethods)
            Set javaSourceSet = pitestExtension.mainSourceSets.get()*.java.srcDirs.flatten() as Set
            Set resourcesSourceSet = pitestExtension.mainSourceSets.get()*.resources.srcDirs.flatten() as Set
            sourceDirs.setFrom(javaSourceSet + resourcesSourceSet)
            detectInlinedCode.set(pitestExtension.detectInlinedCode)
            timestampedReports.set(pitestExtension.timestampedReports)
            additionalClasspath.setFrom({
                FileCollection filteredCombinedTaskClasspath = combinedTaskClasspath.filter { File file ->
                    !pitestExtension.fileExtensionsToFilter.getOrElse([]).find { extension -> file.name.endsWith(".$extension") }
                }

                return filteredCombinedTaskClasspath
            } as Callable<FileCollection>, pitestExtension.testSourceSets.get()*.java.srcDirs.flatten(), pitestExtension.testSourceSets.get()*.resources.srcDirs.flatten())
            useAdditionalClasspathFile.set(pitestExtension.useClasspathFile)
            additionalClasspathFile.set(new File(project.buildDir, PIT_ADDITIONAL_CLASSPATH_DEFAULT_FILE_NAME))
            mutableCodePaths.setFrom({
                Object additionalMutableCodePaths = pitestExtension.additionalMutableCodePaths ?: [] as Set
                additionalMutableCodePaths.add(getJavaCompileTask(variant).destinationDir)
                Task kotlinCompileTask = project.tasks.findByName("compile${variant.name.capitalize()}Kotlin")
                if (kotlinCompileTask != null) {
                    additionalMutableCodePaths.add(kotlinCompileTask.destinationDir)
                }
                additionalMutableCodePaths
            } as Callable<Set<File>>)
            historyInputLocation.set(pitestExtension.historyInputLocation)
            historyOutputLocation.set(pitestExtension.historyOutputLocation)
            enableDefaultIncrementalAnalysis.set(pitestExtension.enableDefaultIncrementalAnalysis)
            mutationThreshold.set(pitestExtension.mutationThreshold)
            coverageThreshold.set(pitestExtension.coverageThreshold)
            mutationEngine.set(pitestExtension.mutationEngine)
            exportLineCoverage.set(pitestExtension.exportLineCoverage)
            jvmPath.set(pitestExtension.jvmPath)
            mainProcessJvmArgs.set(pitestExtension.mainProcessJvmArgs)
            launchClasspath.setFrom({
                project.buildscript.configurations[PITEST_CONFIGURATION_NAME]
            } as Callable<Configuration>)
            pluginConfiguration.set(pitestExtension.pluginConfiguration)
            maxSurviving.set(pitestExtension.maxSurviving)
            useClasspathJar.set(pitestExtension.useClasspathJar)
            features.set(pitestExtension.features)
        }
    }

    private void addPitDependencies() {
        project.buildscript.dependencies {
            String pitestVersion = pitestExtension.pitestVersion.get()
            log.info("Using PIT: $pitestVersion")
            pitest "org.pitest:pitest-command-line:$pitestVersion"
            if (pitestExtension.junit5PluginVersion.isPresent()) {
                if (!pitestExtension.testPlugin.isPresent()) {
                    log.info("Implicitly using JUnit 5 plugin for PIT with version defined in 'junit5PluginVersion'")
                    pitestExtension.testPlugin.set(PITEST_JUNIT5_PLUGIN_NAME)
                }
                if (pitestExtension.testPlugin.isPresent() && pitestExtension.testPlugin.get() != PITEST_JUNIT5_PLUGIN_NAME) {
                    log.warn("Specified 'junit5PluginVersion', but other plugin is configured in 'testPlugin' for PIT: '${pitestExtension.testPlugin.get()}'")
                }

                String junit5PluginDependencyAsString = "org.pitest:pitest-junit5-plugin:${pitestExtension.junit5PluginVersion.get()}"
                log.info("Adding dependency: ${junit5PluginDependencyAsString}")
                pitest project.dependencies.create(junit5PluginDependencyAsString)
            }
        }
    }

    @SuppressWarnings("DuplicateNumberLiteral")
    private File getMockableAndroidJar(BaseExtension android) {
        boolean returnDefaultValues = android.testOptions.unitTests.returnDefaultValues

        String mockableAndroidJarFilename = "mockable-"
        mockableAndroidJarFilename += sanitizeSdkVersion(android.compileSdkVersion)
        if (returnDefaultValues) {
            mockableAndroidJarFilename += '.default-values'
        }

        File mockableJarDirectory
        if (ANDROID_GRADLE_PLUGIN_VERSION_NUMBER.major >= 3) {
            mockableAndroidJarFilename += '.v3'
            mockableJarDirectory = new File(project.buildDir, AndroidProject.FD_GENERATED)
        } else {
            mockableJarDirectory = new File(project.rootProject.buildDir, AndroidProject.FD_GENERATED)
        }
        mockableAndroidJarFilename += '.jar'

        return new File(mockableJarDirectory, mockableAndroidJarFilename)
    }

}
