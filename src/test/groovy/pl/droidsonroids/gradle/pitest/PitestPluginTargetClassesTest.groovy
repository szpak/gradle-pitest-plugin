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

import org.gradle.api.Project
import org.gradle.api.Task
import spock.lang.Specification

class PitestPluginTargetClassesTest extends Specification {

    private Project project

    def setup() {
        project = AndroidUtils.createSampleLibraryProject()
    }

    def "take target classes from pitest configuration closure"() {
        given:
            project.pitest.targetClasses = ["foo"]
        when:
            Set<Task> tasks = project.getTasksByName(AndroidUtils.PITEST_RELEASE_TASK_NAME, false)
        then:
            assertOnePitestTaskWithGivenTargetClasses(tasks, ["foo"] as Set)
    }

    def "set target classes to project group if defined"() {
        given:
            project.group = "group"
        when:
            Set<Task> tasks = project.getTasksByName(AndroidUtils.PITEST_RELEASE_TASK_NAME, false)
        then:
            assertOnePitestTaskWithGivenTargetClasses(tasks, ["group.*"] as Set)
    }

    def "override default target classes from project group if defined by user"() {
        given:
            project.group = "group"
            project.pitest.targetClasses = ["target.classes"]
        when:
            Set<Task> tasks = project.getTasksByName(AndroidUtils.PITEST_RELEASE_TASK_NAME, false)
        then:
            assertOnePitestTaskWithGivenTargetClasses(tasks, ["target.classes"] as Set)
    }

    //Only imitation of testing Gradle validation exception
    def "keep classes to mutate by PIT not set if project group not defined and not explicit set targetClasses parameter"() {
        when:
            Set<Task> tasks = project.getTasksByName(AndroidUtils.PITEST_RELEASE_TASK_NAME, false)
        then:
            assertOnePitestTaskWithGivenTargetClasses(tasks, null)
    }

    //Test case "throw Gradle exception if project group not defined and not explicit set targetClasses parameter" implemented as functional test
    // in TargetClassesFunctionalSpec

    private static assertOnePitestTaskWithGivenTargetClasses(Set<Task> tasks, Set<String> expectedTargetClasses) {
        tasks.size() == 1
        PitestTask pitestTask = (PitestTask) tasks.first()
        pitestTask.getTargetClasses() == expectedTargetClasses
    }
}
