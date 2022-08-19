/* Copyright (c) 2017 Marcin Zajączkowski
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
import spock.lang.Issue

import java.nio.charset.Charset

//TODO: Think if task initialization with WithPitestTaskInitialization is not performed to early
//      (see PitestTaskTestPluginConfigurationSpec for corner case with login in PitestPlugin)
@CompileDynamic
class PitestTaskConfigurationSpec extends BasicProjectBuilderSpec implements WithPitestTaskInitialization {

    @SuppressWarnings("JUnitPublicField")   //public to be used also in functional tests
    public static final List<String> PIT_PARAMETERS_NAMES_NOT_SET_BY_DEFAULT = ['classPathFile',
                                                                                'features',
                                                                                'excludedTestClasses',
                                                                                'testPlugin',
                                                                                'dependencyDistance',
                                                                                'threads',
                                                                                "mutators",
                                                                                'excludedMethods',
                                                                                'excludedClasses',
                                                                                'excludedTestClasses',
                                                                                'avoidCallsTo',
                                                                                'verbose',
                                                                                'timeoutFactor',
                                                                                'timeoutConst',
                                                                                'jvmArgs',
                                                                                'outputFormats',
                                                                                'failWhenNoMutations',
                                                                                'skipFailingTests',
                                                                                'includedGroups',
                                                                                'excludedGroups',
                                                                                'fullMutationMatrix',
                                                                                'includedTestMethods',
                                                                                'detectInlinedCode',
                                                                                'timestampedReports',
                                                                                'mutationThreshold',
                                                                                'coverageThreshold',
                                                                                'testStrengthThreshold',
                                                                                'mutationEngine',
                                                                                'exportLineCoverage',
                                                                                'jvmPath',
                                                                                'maxSurviving',
                                                                                'useClasspathJar',
                                                                                'inputCharset',
                                                                                'outputCharset',
                                                                                'inputEncoding',
                                                                                'outputEncoding',
                                                                                'features',
                                                                                'historyInputLocation',
                                                                                'historyOutputLocation',
                                                                                'pluginConfiguration',
    ]

    void "should pass additional classpath to PIT using classPathFile parameter instead of classPath if configured"() {
        given:
            project.pitest.useClasspathFile = true
        and:
            new File(project.buildDir.absolutePath).mkdir() //in ProjectBuilder "build" directory is not created by default
        expect:
            File createClasspathFile = new File(project.buildDir, "pitClasspath")
            task.taskArgumentMap()['classPathFile'] == createClasspathFile.absolutePath
            !task.taskArgumentMap()['classPath']
        and:
            //TODO
            createClasspathFile.readLines().size() == 4
            createClasspathFile.readLines() as Set ==
                [
                    sourceSetBuiltJavaClasses("main"),
                    sourceSetBuiltResources("main"),
                    sourceSetBuiltJavaClasses("test"),
                    sourceSetBuiltResources("test")
                ] as Set
    }

    void "should pass features configuration to PIT"() {
        given:
            project.pitest.features = ["-FOO", "+BAR(a[1] a[2])"]
        expect:
            task.taskArgumentMap()['features'] == "-FOO,+BAR(a[1] a[2])"
    }

    void "should pass additional features alone if features not set in configuration"() {
        given:
            getJustOnePitestTaskOrFail().additionalFeatures = ['+XYZ', '-ABC']
        expect:
            task.taskArgumentMap()['features'] == "+XYZ,-ABC"
    }

    void "should add additional features to those defined in configuration"() {
        given:
            project.pitest.features = ["-FOO", "+BAR"]
            getJustOnePitestTaskOrFail().additionalFeatures = ['+XYZ', '-ABC']
        expect:
            task.taskArgumentMap()['features'] == "-FOO,+BAR,+XYZ,-ABC"
    }

    void "should not pass features configuration to PIT if not set in configuration or via option"() {
        //Intentional duplication with generic parametrized tests to emphasis requirement
        expect:
            task.taskArgumentMap()['featues'] == null
    }

    void "should not pass to PIT parameter '#paramName' by default if not set explicitly"() {
        expect:
            !task.taskArgumentMap().containsKey(paramName)
        where:
            //It would be best to have it generated automatically based. However, mapping between task parameters and map passed to PIT is not 1-to-1
            paramName << PIT_PARAMETERS_NAMES_NOT_SET_BY_DEFAULT
    }

    //TODO: Run PIT with those values to detect removed properties and typos
    void "should pass plugin configuration (#configParamName) from Gradle to PIT"() {
        given:
            project.pitest."${configParamName}" = gradleConfigValue
        expect:
            task.taskArgumentMap()[configParamName] == expectedPitConfigValue
            // TODO: Move timeoutConst to separate test
        where:
            //pitConfigParamName value taken from gradleConfigParamName if set to null
            configParamName          | gradleConfigValue                            || expectedPitConfigValue
            //testPlugin and junit5PluginVersion tested separately
            "reportDir"              | new File("//tmp//foo")                       || new File("//tmp//foo").path    //due to issues on Windows
            "targetClasses"          | ["a", "b"]                                   || "a,b"
            "targetTests"            | ["t1", "t2"]                                 || "t1,t2"
            "dependencyDistance"     | 42                                           || "42"
            "threads"                | 42                                           || "42"
            "mutators"               | ["MUTATOR_X", "MUTATOR_Y", "-MUTATOR_Z"]     || "MUTATOR_X,MUTATOR_Y,-MUTATOR_Z"
            "excludedMethods"        | ["methodX", "methodY"]                       || "methodX,methodY"
            "excludedClasses"        | ["classX", "foo.classY"]                     || "classX,foo.classY"
            "excludedTestClasses"    | ["classX", "foo.classY"]                     || "classX,foo.classY"
            "avoidCallsTo"           | ["callX", "foo.callY"]                       || "callX,foo.callY"
            "verbose"                | true                                         || "true"
            "timeoutFactor"          | 1.25                                         || "1.25"
            "jvmArgs"                | ["-Xmx250m", "-Xms100m"]                     || "-Xmx250m,-Xms100m"
            "outputFormats"          | ["HTML", "CSV"]                              || "HTML,CSV"
            "failWhenNoMutations"    | false                                        || "false"
            "skipFailingTests"       | true                                         || "true"
            "includedGroups"         | ["Group1", "Group2"]                         || "Group1,Group2"
            "excludedGroups"         | ["Group1", "Group2"]                         || "Group1,Group2"
            "fullMutationMatrix"     | true                                         || "true"
            "includedTestMethods"    | ["method1", "method2"]                       || "method1,method2"
            //mainSourceSets and testSourceSets tested separately
            "detectInlinedCode"      | true                                         || "true"
            "timestampedReports"     | true                                         || "true"
            //useClasspathFile tested separately
            //additionalMutableCodePaths tested separately
            "historyInputLocation"   | new File("//tmp//hi")                        || new File("//tmp//hi").path
            "historyOutputLocation"  | new File("//tmp//ho")                        || new File("//tmp//ho").path
            //enableDefaultIncrementalAnalysis tested separately
            "mutationThreshold"      | 90                                           || "90"
            "coverageThreshold"      | 95                                           || "95"
            "testStrengthThreshold"  | 95                                           || "95"
            "mutationEngine"         | "gregor2"                                    || "gregor2"
            "exportLineCoverage"     | true                                         || "true"
            "jvmPath"                | new File("//opt//jvm15//")                   || new File("//opt//jvm15//").path
            //mainProcessJvmArgs tested separately
            //pluginConfiguration tested separately
            "maxSurviving"           | 20                                           || "20"
            "useClasspathJar"        | true                                         || "true"
            //inputCharset and outputCharset tested separately - they set inputEncoding and outputEncoding in PIT
            "inputEncoding"          | Charset.forName("ISO-8859-2")                || "ISO-8859-2"
            "outputEncoding"         | Charset.forName("ISO-8859-1")                || "ISO-8859-1"
            "features"               | ["-FOO", "+BAR(a[1] a[2])"]                  || "-FOO,+BAR(a[1] a[2])"
            //fileExtensionsToFilter not passed to PIT, tested separately
    }

    void "should pass plugin configuration (#gradleConfigParamName) from Gradle to PIT (overridden name)"() {
        given:
            project.pitest."${gradleConfigParamName}" = gradleConfigValue
        expect:
            task.taskArgumentMap()[pitConfigParamName ?: gradleConfigParamName] == expectedPitConfigValue
        where:
            //pitConfigParamName value taken from gradleConfigParamName if set to null
            gradleConfigParamName  | gradleConfigValue | pitConfigParamName || expectedPitConfigValue
            "timeoutConstInMillis" | 100               | "timeoutConst"     || "100"
//            "useClasspathFile" | true               | "classPathFile"     || "?"    //tested separately
    }

    void "should pass plugin configuration (mutableCodePaths) from Gradle to PIT"() {
        given:
            project.pitest.additionalMutableCodePaths = [new File("//tmp//p1"), new File("//tmp//p2")]
        expect:
            task.taskArgumentMap()["mutableCodePaths"].contains("${new File("//tmp//p1").path},${new File("//tmp//p2").path}")
    }

    @Issue("https://github.com/szpak/gradle-pitest-plugin/issues/144")
    void "should set targetTests to targetClasses by default if not defined in configuration"() {
        when:
            project.pitest.targetClasses = ["myClasses.*"]
        then:
            task.taskArgumentMap()['targetTests'] == "myClasses.*"
    }

    @Issue("https://github.com/szpak/gradle-pitest-plugin/issues/144")
    void "should set targetTests to configuration defined value"() {
        when:
            project.pitest.targetClasses = ["myClasses.*"]
            project.pitest.targetTests = ["myClasses.tests.*"]
        then:
            task.taskArgumentMap()['targetTests'] == "myClasses.tests.*"
    }

    @Issue("https://github.com/szpak/gradle-pitest-plugin/issues/143")
    void "should override explicitly defined in configuration targetTests from command line"() {
        given:
            project.pitest.targetTests = ["com.foobar.*"]
            getJustOnePitestTaskOrFail().overriddenTargetTests = ["com.example.a.*", "com.example.b.*"]
        expect:
            task.taskArgumentMap()['targetTests'] == "com.example.a.*,com.example.b.*"
    }

    @Issue("https://github.com/szpak/gradle-pitest-plugin/issues/143")
    void "should override targetTests inferred from targetClasses from command line"() {
        given:
            project.pitest.targetClasses = ["com.foobar.*"]
            getJustOnePitestTaskOrFail().overriddenTargetTests = ["com.example.a.*", "com.example.b.*"]
        expect:
            task.taskArgumentMap()['targetTests'] == "com.example.a.*,com.example.b.*"
    }

    @Issue("https://github.com/szpak/gradle-pitest-plugin/issues/198")
    void "should pass configured mainSourceSets to PIT"() {
        given:
            project.pitest.mainSourceSets = [project.sourceSets.main]
        when:
            String sourceDirs = task.taskArgumentMap()['sourceDirs']
        then:
            sourceDirs == assembleMainSourceDirAsStringSet().join(",")
    }

    void "should consider testSourceSets in (additional) classpath"() {
        given:
            project.sourceSets { intTest }
            project.pitest.testSourceSets = [project.sourceSets.intTest]
        expect:
            task.taskArgumentMap()['classPath'].split(",") as Set ==
                [
                    sourceSetBuiltJavaClasses("intTest"),
                    sourceSetBuiltResources("intTest"),
                    sourceSetBuiltJavaClasses("main")
                ] as Set
    }

    void "should set input/output encoding in PIT for input/output charset"() {
        given:
            String inputEncodingAsString = "ISO-8859-2"
            String outputEncodingAsString = "ISO-8859-1"
        and:
            project.pitest.inputCharset = Charset.forName(inputEncodingAsString)
            project.pitest.outputCharset = Charset.forName(outputEncodingAsString)
        expect:
            task.taskArgumentMap()['inputEncoding'] == inputEncodingAsString
            task.taskArgumentMap()['outputEncoding'] == outputEncodingAsString
    }

    private Set<String> assembleMainSourceDirAsStringSet() {
        return ["resources", "java"].collect { String dirName ->
            new File(project.projectDir, "src/main/${dirName}")
        }*.absolutePath
    }

    private String sourceSetBuiltJavaClasses(String sourceSetName) {
        return new File(project.buildDir, "classes/java/${sourceSetName}").absolutePath
    }

    private String sourceSetBuiltResources(String sourceSetName) {
        return new File(project.buildDir, "resources/${sourceSetName}").absolutePath
    }

}
