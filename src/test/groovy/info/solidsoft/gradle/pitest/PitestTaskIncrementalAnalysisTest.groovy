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

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project

class PitestTaskIncrementalAnalysisTest extends Specification {

    private Project project
    private PitestTask task

    def setup() {
        project = ProjectBuilder.builder().build()
        project.apply(plugin: "java")   //to add SourceSets
        project.apply(plugin: "pitest")
        task = project.tasks[PitestPlugin.PITEST_TASK_NAME] as PitestTask
    }

    def "default analysis mode disabled by default"() {
        given:

        when:
        def createdMap = task.createTaskArgumentMap()

        then:
        assert createdMap.get("enableDefaultIncrementalAnalysis") == null
    }

    def "files for history location not set by default"() {
        given:

        when:
        def createdMap = task.createTaskArgumentMap()

        then:
        assert createdMap.get('historyInputLocation') == null
        assert createdMap.get('historyOutputLocation') == null
    }

    def "set default files for history location in default incremental analysis mode"() {
        given:
        task.enableDefaultIncrementalAnalysis = true

        when:
        def createdMap = task.createTaskArgumentMap()

        then:
        String pitHistoryDefaultFile = new File(project.buildDir, PitestPlugin.PIT_HISTORY_DEFAULT_FILE_NAME).path
        assert createdMap.get('historyInputLocation') == pitHistoryDefaultFile
        assert createdMap.get('historyOutputLocation') == pitHistoryDefaultFile
    }

    def "override files for history location when set explicit in configuration also default incremental analysis mode"() {
        given:
        //TODO: How to set a configuration (e.g. enableDefaultIncrementalAnalysis) in a test project?
        task.enableDefaultIncrementalAnalysis = true
        task.historyInputLocation = new File('input')
        task.historyOutputLocation = new File('output')

        when:
        def createdMap = task.createTaskArgumentMap()

        then:
        assert createdMap.get('historyInputLocation') == task.historyInputLocation.path
        assert createdMap.get('historyOutputLocation') == task.historyOutputLocation.path
    }

    def "given in configuration files for history location are used also not in default incremental analysis mode"() {
        given:
        task.enableDefaultIncrementalAnalysis = false
        task.historyInputLocation = new File('input')
        task.historyOutputLocation = new File('output')

        when:
        def createdMap = task.createTaskArgumentMap()

        then:
        assert createdMap.get('historyInputLocation') == task.historyInputLocation.path
        assert createdMap.get('historyOutputLocation') == task.historyOutputLocation.path
    }
}
