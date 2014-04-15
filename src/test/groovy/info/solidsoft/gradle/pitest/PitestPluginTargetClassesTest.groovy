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

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Ignore
import spock.lang.Specification

class PitestPluginTargetClassesTest extends Specification {

    def "take target classes from pitest configuration closure"() {
        given:
            Project project = ProjectBuilder.builder().build()
            project.apply(plugin: "java")
            project.apply(plugin: "pitest")
            project.pitest.targetClasses = ["foo"]
        when:
            def tasks = project.getTasksByName(PitestPlugin.PITEST_TASK_NAME, false)
        then:
            assertOnePitestTaskWithGivenTargetClasses(tasks, ["foo"] as Set)
    }

    def "set target classes to project group if defined"() {
        given:
            Project project = ProjectBuilder.builder().build()
            project.group = "group"
            project.apply(plugin: "java")
            project.apply(plugin: "pitest")
        when:
            def tasks = project.getTasksByName(PitestPlugin.PITEST_TASK_NAME, false)
        then:
            assertOnePitestTaskWithGivenTargetClasses(tasks, ["group.*"] as Set)
    }

    def "override default target classes from project group if defined by user"() {
        given:
            Project project = ProjectBuilder.builder().build()
            project.group = "group"
            project.apply(plugin: "java")
            project.apply(plugin: "pitest")
            project.pitest.targetClasses = ["target.classes"]
        when:
            def tasks = project.getTasksByName(PitestPlugin.PITEST_TASK_NAME, false)
        then:
            assertOnePitestTaskWithGivenTargetClasses(tasks, ["target.classes"] as Set)
    }

    @Ignore("Is it possible to call task validation on a project created with ProjectBuilder?")
    def "throw Gradle exception if project group not defined and not explicit set targetClasses parameter"() {
    }

    //Only imitation of testing Gradle validation exception
    def "keep classes to mutate by PIT not set if project group not defined and not explicit set targetClasses parameter"() {
        given:
            Project project = ProjectBuilder.builder().build()
            project.apply(plugin: "java")
            project.apply(plugin: "pitest")
        when:
            def tasks = project.getTasksByName(PitestPlugin.PITEST_TASK_NAME, false)
        then:
            assertOnePitestTaskWithGivenTargetClasses(tasks, null)
    }

    private static assertOnePitestTaskWithGivenTargetClasses(Set<Task> tasks, Set<String> expectedTargetClasses) {
        tasks.size() == 1
        PitestTask pitestTask = tasks.iterator().next()
        pitestTask.getTargetClasses() == expectedTargetClasses
    }
}
