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

@CompileDynamic
class  PitestTaskIncrementalAnalysisTest extends BasicProjectBuilderSpec implements WithPitestTaskInitialization {

    void "default analysis mode disabled by default"() {
        when:
        Map<String, String> createdMap = task.taskArgumentsMap()

        then:
        createdMap.get('enableDefaultIncrementalAnalysis') == null
    }

    void "files for history location not set by default"() {
        when:
        Map<String, String> createdMap = task.taskArgumentsMap()

        then:
        createdMap.get('historyInputLocation') == null
        createdMap.get('historyOutputLocation') == null
    }

    void "set default files for history location in default incremental analysis mode ('#propertyName')"() {
        given:
        project.pitest."$propertyName" = true

        and:
        String pitHistoryDefaultFile = new File(project.buildDir, PitestPlugin.PIT_HISTORY_DEFAULT_FILE_NAME).path

        when:
        Map<String, String> createdMap = task.taskArgumentsMap()

        then:
        createdMap.get('historyInputLocation') == pitHistoryDefaultFile
        createdMap.get('historyOutputLocation') == pitHistoryDefaultFile

        where:
        propertyName << ['enableDefaultIncrementalAnalysis', 'withHistory']
    }

    void "override files for history location when set explicit in configuration also default incremental analysis mode"() {
        given:
        project.pitest  {
            enableDefaultIncrementalAnalysis = true
            historyInputLocation = new File('input')
            historyOutputLocation = new File('output')
        }

        when:
        Map<String, String> createdMap = task.taskArgumentsMap()

        then:
        createdMap.get('historyInputLocation') == task.historyInputLocation.asFile.get().path
        createdMap.get('historyOutputLocation') == task.historyOutputLocation.asFile.get().path
    }

    void "given in configuration files for history location are used also not in default incremental analysis mode"() {
        given:
        project.pitest {
            enableDefaultIncrementalAnalysis = false
            historyInputLocation = new File('input')
            historyOutputLocation = new File('output')
        }

        and:
        task = getJustOnePitestTaskOrFail()

        when:
        Map<String, String> createdMap = task.taskArgumentsMap()
        then:
        createdMap.get('historyInputLocation') == task.historyInputLocation.asFile.get().path
        createdMap.get('historyOutputLocation') == task.historyOutputLocation.asFile.get().path
    }

}
