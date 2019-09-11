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

import spock.lang.Issue

class PitestTaskConfigurationSpec extends BasicProjectBuilderSpec implements WithPitestTaskInitialization {

    //public to be used also in functional tests
    public static final List<String> PIT_PARAMETERS_NAMES_NOT_SET_BY_DEFAULT = ['classPathFile',
                                                                                'features',
                                                                                'excludedTestClasses',
                                                                                'testPlugin',
                                                                                'dependencyDistance',
                                                                                'threads',
                                                                                'mutateStaticInits',
                                                                                'includeJarFiles',
                                                                                "mutators",
                                                                                'excludedMethods',
                                                                                'excludedClasses',
                                                                                'excludedTestClasses',
                                                                                'avoidCallsTo',
                                                                                'verbose',
                                                                                'timeoutFactor',
                                                                                'timeoutConst',
                                                                                'maxMutationsPerClass',
                                                                                'jvmArgs',
                                                                                'outputFormats',
                                                                                'failWhenNoMutations',
                                                                                'skipFailingTests',
                                                                                'includedGroups',
                                                                                'excludedGroups',
                                                                                'includedTestMethods',
                                                                                'detectInlinedCode',
                                                                                'timestampedReports',
                                                                                'mutationThreshold',
                                                                                'coverageThreshold',
                                                                                'mutationEngine',
                                                                                'exportLineCoverage',
                                                                                'jvmPath',
                                                                                'maxSurviving',
                                                                                'useClasspathJar',
                                                                                'features',
                                                                                'historyInputLocation',
                                                                                'historyOutputLocation',
                                                                                'pluginConfiguration'
    ]

    def "should pass additional classpath to PIT using classPathFile parameter instead of classPath if configured"() {
        given:
            project.pitest.useClasspathFile = true
        and:
            new File(project.buildDir.absolutePath).mkdir() //in ProjectBuilder "build" directory is not created by default
        expect:
            task.createTaskArgumentMap()['classPathFile'] == new File(project.buildDir, "pitClasspath").absolutePath
            !task.createTaskArgumentMap()['classPath']
    }

    def "should pass features configuration to PIT"() {
        given:
            project.pitest.features = ["-FOO", "+BAR(a[1] a[2])"]
        expect:
            task.createTaskArgumentMap()['features'] == "-FOO,+BAR(a[1] a[2])"
    }

    def "should pass additional features alone if features not set in configuration"() {
        given:
            getJustOnePitestTaskOrFail().additionalFeatures = ['+XYZ', '-ABC']
        expect:
            task.createTaskArgumentMap()['features'] == "+XYZ,-ABC"
    }

    def "should add additional features to those defined in configuration"() {
        given:
            project.pitest.features = ["-FOO", "+BAR"]
            getJustOnePitestTaskOrFail().additionalFeatures = ['+XYZ', '-ABC']
        expect:
            task.createTaskArgumentMap()['features'] == "-FOO,+BAR,+XYZ,-ABC"
    }

    def "should not pass features configuration to PIT if not set in configuration or via option"() {
        //Intentional duplication with generic parametrized tests to emphasis requirement
        expect:
            task.createTaskArgumentMap()['featues'] == null
    }

    def "should not pass to PIT parameter '#paramName' by default if not set explicitly"() {
        expect:
            !task.createTaskArgumentMap().containsKey(paramName)
        where:
            //It would be best to have it generated automatically based. However, mapping between task parameters and map passed to PIT is not 1-to-1
            paramName << PIT_PARAMETERS_NAMES_NOT_SET_BY_DEFAULT
    }

    //TODO: Run PIT with those values to detect removed properties and typos
    def "should pass plugin configuration (#configParamName) from Gradle to PIT"() {
        given:
            project.pitest."${configParamName}" = gradleConfigValue
        expect:
            task.createTaskArgumentMap()[configParamName] == expectedPitConfigValue
            // TODO: Move timeoutConst to separate test
        where:
            //pitConfigParamName value taken from gradleConfigParamName if set to null
            configParamName          | gradleConfigValue                            || expectedPitConfigValue
            "testPlugin"             | "testng"                                     || "testng"
            "reportDir"              | new File("/tmp/foo")                         || new File("/tmp/foo").path    //due to issues on Windows
            "targetClasses"          | ["a", "b"]                                   || "a,b"
            "targetTests"            | ["t1", "t2"]                                 || "t1,t2"
            "dependencyDistance"     | 42                                           || "42"
            "threads"                | 42                                           || "42"
            "mutateStaticInits"      | true                                         || "true" //???
            "includeJarFiles"        | false                                        || "false"
            "mutators"               | ["MUTATOR_X", "MUTATOR_Y"]                   || "MUTATOR_X,MUTATOR_Y"
            "excludedMethods"        | ["methodX", "methodY"]                       || "methodX,methodY"
            "excludedClasses"        | ["classX", "foo.classY"]                     || "classX,foo.classY"
            "excludedTestClasses"    | ["classX", "foo.classY"]                     || "classX,foo.classY"
            "avoidCallsTo"           | ["callX", "foo.callY"]                       || "callX,foo.callY"
            "verbose"                | true                                         || "true"
            "timeoutFactor"          | 1.25                                         || "1.25"
            "maxMutationsPerClass"   | 25                                           || "25"
            "jvmArgs"                | ["-Xmx250m", "-Xms100m"]                     || "-Xmx250m,-Xms100m"
            "outputFormats"          | ["HTML", "CSV"]                              || "HTML,CSV"
            "failWhenNoMutations"    | false                                        || "false"
            "skipFailingTests"       | true                                         || "true"
            "includedGroups"         | ["Group1", "Group2"]                         || "Group1,Group2"
            "excludedGroups"         | ["Group1", "Group2"]                         || "Group1,Group2"
            "includedTestMethods"    | ["method1", "method2"]                       || "method1,method2"
            "detectInlinedCode"      | true                                         || "true"
            "timestampedReports"     | true                                         || "true"
            "mutationThreshold"      | 90                                           || "90"
            "coverageThreshold"      | 95                                           || "95"
            "mutationEngine"         | "gregor2"                                    || "gregor2"
            //sourceSet x2
            "exportLineCoverage"     | true                                         || "true"
            "jvmPath"                | new File("/opt/jvm15/")                      || new File("/opt/jvm15/").path
            //mainProcessJvmArgs?
//            "pluginConfiguration"    | ["plugin1.key1": "v1", "plugin1.key2": "v2"] || "?"   //Tested separately
            "maxSurviving"           | 20                                           || "20"
            "useClasspathJar"        | true                                         || "true"
//            "useClasspathFile"       | true                                         || "false"    //TODO
            "features"               | ["-FOO", "+BAR(a[1] a[2])"]                  || "-FOO,+BAR(a[1] a[2])"
//            "fileExtensionsToFilter" | ["zip", "xxx"]                               || "*.zip,*.xxx"  //not passed to PIT
            "historyInputLocation"   | new File("/tmp/hi")                          || new File("/tmp/hi").path
            "historyOutputLocation"  | new File("/tmp/ho")                          || new File("/tmp/ho").path
    }

    def "should pass plugin configuration (#gradleConfigParamName) from Gradle to PIT (overidden name)"() {
        given:
            project.pitest."${gradleConfigParamName}" = gradleConfigValue
        expect:
            task.createTaskArgumentMap()[pitConfigParamName ?: gradleConfigParamName] == expectedPitConfigValue
        where:
            //pitConfigParamName value taken from gradleConfigParamName if set to null
            gradleConfigParamName  | gradleConfigValue | pitConfigParamName || expectedPitConfigValue
            "timeoutConstInMillis" | 100               | "timeoutConst"     || "100"
//            "useClasspathFile" | true               | "classPathFile"     || "?"    //tested separately
    }

    def "should pass plugin configuration (mutableCodePaths) from Gradle to PIT"() {
        given:
            project.pitest.additionalMutableCodePaths = [new File("/tmp/p1"), new File("/tmp/p2")]
        expect:
            task.createTaskArgumentMap()["mutableCodePaths"].contains("${new File("/tmp/p1").path},${new File("/tmp/p2").path}")
    }

    @Issue("https://github.com/szpak/gradle-pitest-plugin/issues/144")
    def "should set targetTest to targetClasses by default if not defined in configuration"() {
        when:
            project.pitest.targetClasses = ["myClasses.*"]
        then:
            task.createTaskArgumentMap()['targetTests'] == "myClasses.*"
    }

    @Issue("https://github.com/szpak/gradle-pitest-plugin/issues/144")
    def "should set targetTest to configuration defined value"() {
        when:
            project.pitest.targetClasses = ["myClasses.*"]
            project.pitest.targetTests = ["myClasses.tests.*"]
        then:
            task.createTaskArgumentMap()['targetTests'] == "myClasses.tests.*"
    }
}
