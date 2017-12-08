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
package info.solidsoft.gradle.pitest.integration

class PitestTaskConfigurationSpec extends BasicProjectBuilderSpec implements WithPitestTaskInitialization {

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

    def "should not pass to PIT parameter '#paramName' by default if not set explicitly"() {
        expect:
            !task.createTaskArgumentMap().containsKey(paramName)
        where:
            paramName << ['classPathFile', 'features']
    }
}
