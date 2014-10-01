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

import spock.lang.Specification
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Task

import org.gradle.api.tasks.TaskInstantiationException

class PitestPluginTest extends Specification {

    def "add pitest task to java project in proper group"() {
        given:
            Project project = ProjectBuilder.builder().build()
            project.apply(plugin: "java")   //to add SourceSets
        when:
            project.apply(plugin: "info.solidsoft.pitest")
        then:
            project.plugins.hasPlugin(PitestPlugin)
            assertThatTasksAreInGroup(project, [PitestPlugin.PITEST_TASK_NAME], PitestPlugin.PITEST_TASK_GROUP)
    }

    def "throw exception when java plugin not already applied on project"() {
        given:
            Project project = ProjectBuilder.builder().build()
            //TODO: MZA: assert java is not already applied
        when:
            project.apply(plugin: "info.solidsoft.pitest");
        then:
            thrown(TaskInstantiationException)
    }

    void assertThatTasksAreInGroup(Project project, List<String> taskNames, String group) {
        taskNames.each { String taskName ->
            Task task = project.tasks[taskName]
            assert task != null
            assert task.group == group
        }
    }
}
