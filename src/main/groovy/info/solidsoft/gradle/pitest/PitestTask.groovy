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
import org.gradle.api.Incubating
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.options.Option

/**
 * Gradle task implementation for Pitest.
 */
@CompileStatic
@SuppressWarnings("UnstableApiUsage")   //@Option
class PitestTask extends JavaExec {

    @Input
    @Optional
    final Property<String> testPlugin

    //ClassNotFoundException: org.gradle.api.file.FileSystemLocationProperty in Gradle <5.6 due to super interface of RegularFileProperty
    //See: https://github.com/gradle/gradle/issues/10953 - on the other hand deprecation warnings in Gradle 6.x with regular File
    @OutputDirectory
    final DirectoryProperty reportDir

    @Input
    final SetProperty<String> targetClasses

    @Input
    @Optional
    final SetProperty<String> targetTests

    @Input
    @Optional
    final Property<Integer> dependencyDistance

    @Input
    @Optional
    final Property<Integer> threads

    @Input
    @Optional
    final Property<Boolean> mutateStaticInits

    @Input
    @Optional
    final Property<Boolean> includeJarFiles

    @Input
    @Optional
    final SetProperty<String> mutators

    @Input
    @Optional
    final SetProperty<String> excludedMethods

    @Input
    @Optional
    final SetProperty<String> excludedClasses

    @Input
    @Optional
    final SetProperty<String> excludedTestClasses

    @Input
    @Optional
    final SetProperty<String> avoidCallsTo

    @Input
    @Optional
    final Property<Boolean> verbose

    @Input
    @Optional
    final Property<BigDecimal> timeoutFactor

    @Input
    @Optional
    final Property<Integer> timeoutConstInMillis

    @Input
    @Optional
    final Property<Integer> maxMutationsPerClass

    @Input
    @Optional
    final ListProperty<String> childProcessJvmArgs

    @Input
    @Optional
    final SetProperty<String> outputFormats

    @Input
    @Optional
    final Property<Boolean> failWhenNoMutations

    @Input
    @Optional
    final Property<Boolean> skipFailingTests

    @Input
    @Optional
    final SetProperty<String> includedGroups

    @Input
    @Optional
    final SetProperty<String> excludedGroups

    @Input
    @Optional
    final SetProperty<String> includedTestMethods

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    Set<File> sourceDirs

    @Input
    @Optional
    final Property<Boolean> detectInlinedCode

    @Input
    @Optional
    final Property<Boolean> timestampedReports

    @InputFiles
    @Classpath
    FileCollection additionalClasspath    //"classpath" is already defined internally in ExecTask

    @Input
    final Property<Boolean> useAdditionalClasspathFile

    //Workaround with @Internal for "Unable to store input properties for task" - https://github.com/gradle/gradle/issues/12351
    @Internal
    final RegularFileProperty additionalClasspathFile

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    Set<File> mutableCodePaths

    //Workaround with getter - see https://github.com/gradle/gradle/issues/12351
    @Internal
    final RegularFileProperty historyInputLocation

    @OutputFile
    @Optional
    final RegularFileProperty historyOutputLocation

    @Input
    @Optional
    final Property<Boolean> enableDefaultIncrementalAnalysis

    //Workaround with getter - see https://github.com/gradle/gradle/issues/12351
    @Internal
    final RegularFileProperty defaultFileForHistoryData

    @Input
    @Optional
    final Property<Integer> mutationThreshold

    @Input
    @Optional
    final Property<Integer> coverageThreshold

    @Input
    @Optional
    final Property<String> mutationEngine

    @Input
    @Optional
    final Property<Boolean> exportLineCoverage

    //Workaround with getter - see https://github.com/gradle/gradle/issues/12351
    @Internal
    final RegularFileProperty jvmPath

    @Input
    @Optional
    final ListProperty<String> mainProcessJvmArgs

    @InputFiles
    @Classpath
    FileCollection launchClasspath

    @Input
    @Optional
    final MapProperty<String, String> pluginConfiguration

    @Input
    @Optional
    final Property<Integer> maxSurviving

    @Input
    @Optional
    final Property<Boolean> useClasspathJar

    @Input
    @Optional
    final ListProperty<String> features

    @Incubating
    @Option(option = "additionalFeatures", description = "Additional PIT features to be appended to those placed in configuration")
    @Input
    @Optional
    List<String> additionalFeatures //ListProperty<String> cannot be used with @Option - https://github.com/gradle/gradle/issues/10517

    @Incubating
    @Option(option = "targetTests", description = "Tests classes to use. Overrides 'testClasses' defined in configuration")
    @Input
    @Optional
    List<String> overriddenTargetTests  //should be Set<String> or SetProperty but it's not supported in Gradle as of 5.6.1

    PitestTask() {
        ObjectFactory of = project.objects

        testPlugin = of.property(String)
        reportDir = of.directoryProperty()
        targetClasses = of.setProperty(String)
        targetTests = of.setProperty(String)
        dependencyDistance = of.property(Integer)
        threads = of.property(Integer)
        mutateStaticInits = of.property(Boolean)
        includeJarFiles = of.property(Boolean)
        mutators = of.setProperty(String)
        excludedMethods = of.setProperty(String)
        excludedClasses = of.setProperty(String)
        excludedTestClasses = of.setProperty(String)
        avoidCallsTo = of.setProperty(String)
        verbose = of.property(Boolean)
        timeoutFactor = of.property(BigDecimal)
        timeoutConstInMillis = of.property(Integer)
        maxMutationsPerClass = of.property(Integer)
        childProcessJvmArgs = of.listProperty(String)
        outputFormats = of.setProperty(String)
        failWhenNoMutations = of.property(Boolean)
        skipFailingTests = of.property(Boolean)
        includedGroups = of.setProperty(String)
        excludedGroups = of.setProperty(String)
        includedTestMethods = of.setProperty(String)
//        sourceDirs = of.fileProperty()
        detectInlinedCode = of.property(Boolean)
        timestampedReports = of.property(Boolean)
        historyInputLocation = of.fileProperty()
        historyOutputLocation = of.fileProperty()
        enableDefaultIncrementalAnalysis = of.property(Boolean)
        defaultFileForHistoryData = of.fileProperty()
        mutationThreshold = of.property(Integer)
        coverageThreshold = of.property(Integer)
        mutationEngine = of.property(String)
        exportLineCoverage = of.property(Boolean)
        jvmPath = of.fileProperty()
        mainProcessJvmArgs = of.listProperty(String)
//        mutableCodePaths = of.setProperty(File)
        pluginConfiguration = of.mapProperty(String, String)
        maxSurviving = of.property(Integer)
        useClasspathJar = of.property(Boolean)
        useAdditionalClasspathFile = of.property(Boolean)
        additionalClasspathFile = of.fileProperty()
        features = of.listProperty(String)
    }

    @Input
    String getAdditionalClasspathFilePath() {
        return additionalClasspathFile.asFile.get().absolutePath
    }

    @Input
    @Optional
    String getHistoryInputLocationPath() {
        //?. operator doesn't work with Gradle Providers
        return historyInputLocation.isPresent() ? historyInputLocation.asFile.get().absolutePath : null
    }

    @Input
    String getDefaultFileForHistoryDataPath() {
        return defaultFileForHistoryData.asFile.get().absolutePath
    }

    @Input
    @Optional
    String getJvmPathPath() {
        return jvmPath.isPresent() ? jvmPath.asFile.get().absolutePath : null
    }

    @Override
    void exec() {
        //Workaround for compatibility with Gradle <4.0 due to setArgs(List) and setJvmArgs(List) added in Gradle 4.0
        args = argumentsForPit()
        jvmArgs = ((List<String>) getMainProcessJvmArgs().getOrNull() ?: getJvmArgs())
        main = "org.pitest.mutationtest.commandline.MutationCoverageReport"
        classpath = getLaunchClasspath()
        super.exec()
    }

    private List<String> argumentsForPit() {
        Map<String, String> taskArgumentsMap = taskArgumentMap()
        List<String> argsAsList = argumentsListFromMap(taskArgumentsMap)
        List<String> multiValueArgsAsList = multiValueArgsAsList()
        return argsAsList + multiValueArgsAsList
    }

    @PackageScope   //visible for testing
    Map<String, String> taskArgumentMap() {
        Map<String, String> map = [:]
        map['testPlugin'] = testPlugin.getOrNull()
        map['reportDir'] = reportDir.getOrNull()?.toString()
        map['targetClasses'] = targetClasses.get().join(',')
        map['targetTests'] = overriddenTargetTests ? overriddenTargetTests.join(',') : optionalCollectionAsString(targetTests)
        map['dependencyDistance'] = optionalPropertyAsString(dependencyDistance)
        map['threads'] = optionalPropertyAsString(threads)
        map['mutateStaticInits'] = optionalPropertyAsString(mutateStaticInits)
        map['includeJarFiles'] = optionalPropertyAsString(includeJarFiles)
        map["mutators"] = optionalCollectionAsString(mutators)
        map['excludedMethods'] = optionalCollectionAsString(excludedMethods)
        map['excludedClasses'] = optionalCollectionAsString(excludedClasses)
        map['excludedTestClasses'] = optionalCollectionAsString(excludedTestClasses)
        map['avoidCallsTo'] = optionalCollectionAsString(avoidCallsTo)
        map['verbose'] = optionalPropertyAsString(verbose)
        map['timeoutFactor'] = optionalPropertyAsString(timeoutFactor)
        map['timeoutConst'] = optionalPropertyAsString(timeoutConstInMillis)
        map['maxMutationsPerClass'] = optionalPropertyAsString(maxMutationsPerClass)
        map['jvmArgs'] = optionalCollectionAsString(childProcessJvmArgs)
        map['outputFormats'] = optionalCollectionAsString(outputFormats)
        map['failWhenNoMutations'] = optionalPropertyAsString(failWhenNoMutations)
        map['skipFailingTests'] = optionalPropertyAsString(skipFailingTests)
        map['includedGroups'] = optionalCollectionAsString(includedGroups)
        map['excludedGroups'] = optionalCollectionAsString(excludedGroups)
        map['includedTestMethods'] = optionalCollectionAsString(includedTestMethods)
        map['sourceDirs'] = (getSourceDirs()*.absolutePath)?.join(',')
        map['detectInlinedCode'] = optionalPropertyAsString(detectInlinedCode)
        map['timestampedReports'] = optionalPropertyAsString(timestampedReports)
        map['mutableCodePaths'] = (getMutableCodePaths()*.absolutePath)?.join(',')
        map['mutationThreshold'] = optionalPropertyAsString(mutationThreshold)
        map['coverageThreshold'] = optionalPropertyAsString(coverageThreshold)
        map['mutationEngine'] = mutationEngine.getOrNull()
        map['exportLineCoverage'] = optionalPropertyAsString(exportLineCoverage)
        map['includeLaunchClasspath'] = Boolean.FALSE.toString()   //code to analyse is passed via classPath
        map['jvmPath'] = getJvmPath()?.getOrNull()?.asFile?.absolutePath
        map['maxSurviving'] = optionalPropertyAsString(maxSurviving)
        map['useClasspathJar'] = optionalPropertyAsString(useClasspathJar)
        map['features'] = (features.getOrElse([]) + (additionalFeatures ?: []))?.join(',')
        map.putAll(prepareMapWithClasspathConfiguration())
        map.putAll(prepareMapWithIncrementalAnalysisConfiguration())

        return removeEntriesWithNullOrEmptyValue(map)
    }

    private Map<String, String> prepareMapWithClasspathConfiguration() {
        if (useAdditionalClasspathFile.get()) {
            fillAdditionalClasspathFileWithClasspathElements()
            return [classPathFile: getAdditionalClasspathFile().asFile.get().absolutePath]
        }
        return [classPath: getAdditionalClasspath().files.join(',')]
    }

    private void fillAdditionalClasspathFileWithClasspathElements() {
        String classpathElementsAsFileContent = getAdditionalClasspath().files*.getAbsolutePath().join(System.lineSeparator())
        //"withWriter" as "file << content" works in append mode (instead of overwrite one)
        getAdditionalClasspathFile().asFile.get().withWriter { writer ->
            writer << classpathElementsAsFileContent
        }
    }

    private Map<String, String> prepareMapWithIncrementalAnalysisConfiguration() {
        if (enableDefaultIncrementalAnalysis.getOrNull()) {
            return [historyInputLocation: getHistoryInputLocation()?.getOrNull()?.asFile?.absolutePath ?: getDefaultFileForHistoryData().asFile.get().absolutePath,
                    historyOutputLocation: getHistoryOutputLocation()?.getOrNull()?.asFile?.absolutePath ?: getDefaultFileForHistoryData().asFile.get().absolutePath,]
        } else {
            return [historyInputLocation: getHistoryInputLocation()?.getOrNull()?.asFile?.absolutePath,
                    historyOutputLocation: getHistoryOutputLocation()?.getOrNull()?.asFile?.absolutePath,]
        }
    }

    private Map<String, String> removeEntriesWithNullOrEmptyValue(Map<String, String> map) {
        return map.findAll { entry -> entry.value != null && entry.value != "" }
    }

    private List<String> argumentsListFromMap(Map<String, String> taskArgumentsMap) {
        return taskArgumentsMap.collect { k, v ->
            "--$k=$v".toString()
        }
    }

    private <T> String optionalPropertyAsString(Provider<T> optionalSetProperty) {
        return optionalSetProperty.getOrNull()?.toString()
    }

    private String optionalCollectionAsString(SetProperty<String> optionalSetProperty) {
        return optionalSetProperty.getOrNull()?.join(',')
    }

    private String optionalCollectionAsString(ListProperty<String> optionalListProperty) {
        return optionalListProperty.getOrNull()?.join(',')
    }

    @PackageScope   //visible for testing
    List<String> multiValueArgsAsList() {
        //It is a duplication/special case handling, but a PoC implementation with emulated multimap was also quite ugly and in addition error prone
        return pluginConfiguration.getOrNull()?.collect { k, v ->
            "$k=$v".toString()
        }?.collect { configString ->
            "--pluginConfiguration=$configString".toString()
        } ?: []
    }

}
