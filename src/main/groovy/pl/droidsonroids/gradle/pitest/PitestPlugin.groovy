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
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Plugin
import org.gradle.api.Project
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
class PitestPlugin implements Plugin<Project> {

    public final static String DEFAULT_PITEST_VERSION = '1.5.1'
    public final static String PITEST_TASK_GROUP = VERIFICATION_GROUP
    public final static String PITEST_TASK_NAME = "pitest"
    public final static String PITEST_CONFIGURATION_NAME = 'pitest'
    public final static PITEST_TEST_COMPILE_CONFIGURATION_NAME = 'pitestTestCompile'

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
    private PitestPluginExtension extension

    void apply(Project project) {
        this.project = project
        createConfigurations()

        extension = project.extensions.create("pitest", PitestPluginExtension, project)
        extension.pitestVersion.set(DEFAULT_PITEST_VERSION)
        extension.fileExtensionsToFilter.set(DEFAULT_FILE_EXTENSIONS_TO_FILTER_FROM_CLASSPATH)
        extension.useClasspathFile.set(false)

        project.pluginManager.apply(BasePlugin)
        extension.reportDir.set(new File(project.extensions.getByType(ReportingExtension).baseDir, "pitest"))

        if (extension.mainSourceSets.empty()) {
            extension.mainSourceSets.set(project.android.sourceSets.main as Set<AndroidSourceSet>)
        }
        if (extension.testSourceSets.empty()) {
            extension.testSourceSets.set(project.android.sourceSets.test as Set<AndroidSourceSet>)
        }
        project.afterEvaluate {
            project.plugins.withType(AppPlugin) { createPitestTasks(project.android.applicationVariants) }
            project.plugins.withType(LibraryPlugin) { createPitestTasks(project.android.libraryVariants) }
            project.plugins.withType(TestPlugin) { createPitestTasks(project.android.testVariants) }
            addPitDependencies()
        }
    }

    private void createPitestTasks(DefaultDomainObjectSet<? extends BaseVariant> variants) {
        def globalTask = project.tasks.create(PITEST_TASK_NAME)
        globalTask.with {
            description = "Run PIT analysis for java classes, for all build variants"
            group = PITEST_TASK_GROUP
            shouldRunAfter("test")
        }

        variants.all { BaseVariant variant ->
            PitestTask variantTask = project.tasks.create("${PITEST_TASK_NAME}${variant.name.capitalize()}", PitestTask)

            def mockableAndroidJarTask
            if (ANDROID_GRADLE_PLUGIN_VERSION_NUMBER < new VersionNumber(3, 2, 0, null)) {
                mockableAndroidJarTask = project.tasks.findByName("mockableAndroidJar")
                configureTaskDefault(variantTask, variant, getMockableAndroidJar(project.android))
            } else {
                mockableAndroidJarTask = project.tasks.maybeCreate("pitestMockableAndroidJar", PitestMockableAndroidJarTask.class)
                configureTaskDefault(variantTask, variant, mockableAndroidJarTask.outputJar)
            }

            if (!extension.excludeMockableAndroidJar.getOrElse(false)) {
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

    private void createConfigurations() {
        [PITEST_CONFIGURATION_NAME, PITEST_TEST_COMPILE_CONFIGURATION_NAME].each {
            project.rootProject.buildscript.configurations.maybeCreate(it).with {
                visible = false
                description = "The PIT libraries to be used for this project."
            }
        }
    }

    private void configureTaskDefault(PitestTask task, BaseVariant variant, File mockableAndroidJar) {
        FileCollection combinedTaskClasspath = project.files()

        combinedTaskClasspath.from(project.rootProject.buildscript.configurations[PITEST_TEST_COMPILE_CONFIGURATION_NAME])
        if (!extension.excludeMockableAndroidJar.getOrElse(false)) {
            combinedTaskClasspath.from(mockableAndroidJar)
        }

        if (ANDROID_GRADLE_PLUGIN_VERSION_NUMBER.major >= 3) {
            if (ANDROID_GRADLE_PLUGIN_VERSION_NUMBER.minor < 3) {
                combinedTaskClasspath.from(project.configurations["${variant.name}CompileClasspath"].copyRecursive {
                    it.properties.dependencyProject == null
                })
                combinedTaskClasspath.from(project.configurations["${variant.name}UnitTestCompileClasspath"].copyRecursive {
                    it.properties.dependencyProject == null
                })
            } else if (ANDROID_GRADLE_PLUGIN_VERSION_NUMBER.minor < 4) {
                combinedTaskClasspath.from(project.configurations["${variant.name}CompileClasspath"])
                combinedTaskClasspath.from(project.configurations["${variant.name}UnitTestCompileClasspath"])
            }
        } else {
            combinedTaskClasspath.from(project.configurations["compile"])
            combinedTaskClasspath.from(project.configurations["testCompile"])
        }
        combinedTaskClasspath.from(project.files("${project.buildDir}/intermediates/sourceFolderJavaResources/${variant.dirName}"))
        combinedTaskClasspath.from(project.files("${project.buildDir}/intermediates/sourceFolderJavaResources/test/${variant.dirName}"))
        combinedTaskClasspath.from(project.files("${project.buildDir}/intermediates/java_res/${variant.dirName}/out"))
        combinedTaskClasspath.from(project.files("${project.buildDir}/intermediates/java_res/${variant.dirName}UnitTest/out"))
        combinedTaskClasspath.from(project.files("${project.buildDir}/intermediates/unitTestConfig/test/${variant.dirName}"))
        if (variant instanceof TestedVariant) {
            combinedTaskClasspath.from(getJavaCompileTask(variant.unitTestVariant).classpath)
            combinedTaskClasspath.from(project.files(getJavaCompileTask(variant.unitTestVariant).destinationDir))
        }
        combinedTaskClasspath.from(getJavaCompileTask(variant).classpath)
        combinedTaskClasspath.from(project.files(getJavaCompileTask(variant).destinationDir))

        task.with {
            defaultFileForHistoryData.set(new File(project.buildDir, PIT_HISTORY_DEFAULT_FILE_NAME))
            testPlugin.set(extension.testPlugin)
            reportDir.set(extension.reportDir)
            targetClasses.set(project.providers.provider {
                log.debug("Setting targetClasses. project.getGroup: {}, class: {}", project.getGroup(), project.getGroup()?.class)
                if (extension.targetClasses.isPresent()) {
                    return extension.targetClasses.get()
                }
                if (project.getGroup()) {   //Assuming it is always a String class instance
                    return [project.getGroup() + ".*"] as Set
                }
                return null
            } as Provider<Iterable<String>>)
            targetTests.set(project.providers.provider {
                //unless explicitly configured use targetClasses - https://github.com/szpak/gradle-pitest-plugin/issues/144
                if (extension.targetTests.isPresent()) {
                    //getOrElseGet() is not available - https://github.com/gradle/gradle/issues/10520
                    return extension.targetTests.get()
                } else {
                    return targetClasses.getOrNull()
                }
            } as Provider<Iterable<String>>)
            dependencyDistance.set(extension.dependencyDistance)
            threads.set(extension.threads)
            mutateStaticInits.set(extension.mutateStaticInits)
            includeJarFiles.set(extension.includeJarFiles)
            mutators.set(extension.mutators)
            excludedMethods.set(extension.excludedMethods)
            excludedClasses.set(extension.excludedClasses)
            excludedTestClasses.set(extension.excludedTestClasses)
            avoidCallsTo.set(extension.avoidCallsTo)
            verbose.set(extension.verbose)
            timeoutFactor.set(extension.timeoutFactor)
            timeoutConstInMillis.set(extension.timeoutConstInMillis)
            maxMutationsPerClass.set(extension.maxMutationsPerClass)
            childProcessJvmArgs.set(extension.jvmArgs)
            outputFormats.set(extension.outputFormats)
            failWhenNoMutations.set(extension.failWhenNoMutations)
            skipFailingTests.set(extension.skipFailingTests)
            includedGroups.set(extension.includedGroups)
            excludedGroups.set(extension.excludedGroups)
            fullMutationMatrix.set(extension.fullMutationMatrix)
            includedTestMethods.set(extension.includedTestMethods)
            def javaSourceSet = extension.mainSourceSets.get()*.java.srcDirs.flatten() as Set
            def resourcesSourceSet = extension.mainSourceSets.get()*.resources.srcDirs.flatten() as Set
            sourceDirs.setFrom(javaSourceSet + resourcesSourceSet)
            detectInlinedCode.set(extension.detectInlinedCode)
            timestampedReports.set(extension.timestampedReports)
            additionalClasspath.setFrom({
                FileCollection filteredCombinedTaskClasspath = combinedTaskClasspath.filter { File file ->
                    !extension.fileExtensionsToFilter.getOrElse([]).find { extension -> file.name.endsWith(".$extension") }
                }

                return filteredCombinedTaskClasspath
            } as Callable<FileCollection>, extension.testSourceSets.get()*.java.srcDirs.flatten(), extension.testSourceSets.get()*.resources.srcDirs.flatten())
            useAdditionalClasspathFile.set(extension.useClasspathFile)
            additionalClasspathFile.set(new File(project.buildDir, PIT_ADDITIONAL_CLASSPATH_DEFAULT_FILE_NAME))
            mutableCodePaths.setFrom({
                def additionalMutableCodePaths = extension.additionalMutableCodePaths ?: [] as Set
                additionalMutableCodePaths.add(getJavaCompileTask(variant).destinationDir)
                def kotlinCompileTask = project.tasks.findByName("compile${variant.name.capitalize()}Kotlin")
                if (kotlinCompileTask != null) {
                    additionalMutableCodePaths.add(kotlinCompileTask.destinationDir)
                }
                additionalMutableCodePaths
            } as Callable<Set<File>>)
            historyInputLocation.set(extension.historyInputLocation)
            historyOutputLocation.set(extension.historyOutputLocation)
            enableDefaultIncrementalAnalysis.set(extension.enableDefaultIncrementalAnalysis)
            mutationThreshold.set(extension.mutationThreshold)
            coverageThreshold.set(extension.coverageThreshold)
            mutationEngine.set(extension.mutationEngine)
            exportLineCoverage.set(extension.exportLineCoverage)
            jvmPath.set(extension.jvmPath)
            mainProcessJvmArgs.set(extension.mainProcessJvmArgs)
            launchClasspath.setFrom({
                project.rootProject.buildscript.configurations[PITEST_CONFIGURATION_NAME]
            } as Callable<Configuration>)
            pluginConfiguration.set(extension.pluginConfiguration)
            maxSurviving.set(extension.maxSurviving)
            useClasspathJar.set(extension.useClasspathJar)
            features.set(extension.features)
        }
    }

    private void addPitDependencies() {
        project.rootProject.buildscript.dependencies {
            def pitestVersion = extension.pitestVersion.get()
            log.info("Using PIT: $pitestVersion")
            pitest "org.pitest:pitest-command-line:$pitestVersion"
        }
    }

    private File getMockableAndroidJar(BaseExtension android) {
        def returnDefaultValues = android.testOptions.unitTests.returnDefaultValues

        String mockableAndroidJarFilename = "mockable-"
        mockableAndroidJarFilename += sanitizeSdkVersion(android.compileSdkVersion)
        if (returnDefaultValues) {
            mockableAndroidJarFilename += '.default-values'
        }

        File mockableJarDirectory = new File(project.rootProject.buildDir, AndroidProject.FD_GENERATED)
        if (ANDROID_GRADLE_PLUGIN_VERSION_NUMBER.major >= 3) {
            mockableAndroidJarFilename += '.v3'
            mockableJarDirectory = new File(project.buildDir, AndroidProject.FD_GENERATED)
        }
        mockableAndroidJarFilename += '.jar'

        return new File(mockableJarDirectory, mockableAndroidJarFilename)
    }

    static def sanitizeSdkVersion(def version) {
        return version.replaceAll('[^\\p{Alnum}.-]', '-')
    }

    static JavaCompile getJavaCompileTask(BaseVariant variant) {
        if (ANDROID_GRADLE_PLUGIN_VERSION_NUMBER >= VersionNumber.parse("3.3")) {
            return variant.javaCompileProvider.get()
        } else {
            return variant.javaCompile
        }
    }
}
