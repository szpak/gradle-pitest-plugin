/* Copyright (c) 2014 Marcin Zajączkowski
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
import spock.lang.Specification

class PitestPluginTypesConversionTest extends Specification {

    private Project project

    void setup() {
        project = ProjectBuilder.builder().build()
        project.apply(plugin: "java")
        project.apply(plugin: "pitest")
    }

    def "accept BigDecimal as timeoutFactor configuration parameter"() {
        given:
            project.pitest.timeoutFactor = 1.23
        when:
            def tasks = project.getTasksByName(PitestPlugin.PITEST_TASK_NAME, false)
        then:
            PitestTask task = assertOnePitestTaskAndReturnIt(tasks)
            task.timeoutFactor == 1.23
    }

    def "accept String as timeoutFactor configuration parameter"() {
        given:
            project.pitest.timeoutFactor = "1.23"
        when:
            def tasks = project.getTasksByName(PitestPlugin.PITEST_TASK_NAME, false)
        then:
            PitestTask task = assertOnePitestTaskAndReturnIt(tasks)
            task.timeoutFactor == 1.23
    }

    private static PitestTask assertOnePitestTaskAndReturnIt(Set<Task> tasks) {
        assert tasks.size() == 1
        tasks.iterator().next()
    }
}
