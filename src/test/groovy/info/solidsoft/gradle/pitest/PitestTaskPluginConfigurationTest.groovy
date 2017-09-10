/* Copyright (c) 2015 Marcin Zajączkowski
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
import spock.lang.Specification

class PitestTaskPluginConfigurationTest extends Specification {

    private Project project
    private PitestTask task

    def setup() {
        project = AndroidUtils.createSampleLibraryProject()
        project.evaluate()
        task = project.tasks[AndroidUtils.PITEST_RELEASE_TASK_NAME] as PitestTask
        task.targetClasses = []
    }

    def "should not create pluginConfiguration command line argument when no parameters"() {
        given:
            project.pitest.pluginConfiguration = null
        when:
            List<String> multiValueArgList = task.createMultiValueArgsAsList()
        then:
            multiValueArgList['pluginConfiguration'] == []
    }

    def "should split parameters into separate pluginConfiguration arguments"() {
        given:
            project.pitest.pluginConfiguration = ["plugin1.foo": "one", "plugin1.bar": "2"]
        when:
            List<String> multiValueArgList = task.createMultiValueArgsAsList()
        then:
            multiValueArgList == ['--pluginConfiguration=plugin1.foo=one', '--pluginConfiguration=plugin1.bar=2']
    }
}
