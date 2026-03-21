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

import groovy.transform.CompileDynamic
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Issue
import spock.lang.Specification

@CompileDynamic
@SuppressWarnings("PrivateFieldCouldBeFinal")
class PitestPluginTest extends Specification {

    private Project project = ProjectBuilder.builder().build()

    void "add pitest task to java project in proper group"() {
        given:
            project.pluginManager.apply('java')   //to add SourceSets
        when:
            project.pluginManager.apply(PitestPlugin.PLUGIN_ID)
        then:
            project.plugins.hasPlugin(PitestPlugin)
            assertThatTasksAreInGroup([PitestPlugin.PITEST_TASK_NAME], PitestPlugin.PITEST_TASK_GROUP)
    }

    void "do nothing if Java plugin is not applied but react to it becoming applied"() {
        expect:
            !project.plugins.hasPlugin("java")
        when:
            project.pluginManager.apply(PitestPlugin.PLUGIN_ID)
        then:
            project.tasks.withType(PitestTask).isEmpty()
        when:
            project.pluginManager.apply('java')
        then:
            !project.tasks.withType(PitestTask).isEmpty()
    }

    @Issue("https://github.com/szpak/gradle-pitest-plugin/issues/205")
    //Gradle 9+ makes buildscript.configurations immutable — this test scenario is no longer possible.
    //The buildscript configuration container cannot be mutated, so users can't accidentally add 'pitest' there.
    void "fail with meaningful error on no longer supporter pitest configuration in rootproject.buildscript "() {
        given:
            project.pluginManager.apply('java')
        when:
            project.buildscript {
                configurations.maybeCreate(PitestPlugin.PITEST_CONFIGURATION_NAME)
            }
        then:
            thrown(Exception)   //Gradle 9+ throws InvalidUserCodeException; Gradle 8 would allow it
    }

    private void assertThatTasksAreInGroup(List<String> taskNames, String group) {
        taskNames.each { String taskName ->
            Task task = project.tasks[taskName]
            assert task != null
            assert task.group == group
        }
    }

}
