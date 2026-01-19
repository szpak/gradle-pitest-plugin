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

import groovy.transform.CompileDynamic
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.tasks.TaskExecutionOutcome
import org.gradle.api.internal.tasks.TaskStateInternal
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.TempDir

import static info.solidsoft.gradle.pitest.PitestPlugin.PITEST_TASK_NAME

/**
 * @see WithPitestTaskInitialization
 */
@PackageScope
@CompileDynamic
class BasicProjectBuilderSpec extends Specification {

    @TempDir
    protected File tmpProjectDir

    protected Project project
    protected PitestPluginExtension pitestConfig

    //TODO: There is a regression in 2.14.1 with API jar regeneration for every test - https://discuss.gradle.org/t/performance-regression-in-projectbuilder-in-2-14-and-3-0/18956
    //https://github.com/gradle/gradle/commit/3216f07b3acb4cbbb8241d8a1d50b8db9940f37e
    void setup() {
        project = ProjectBuilder.builder().withProjectDir(tmpProjectDir).build()

        project.pluginManager.apply('java')   //to add SourceSets
        project.pluginManager.apply('info.solidsoft.pitest')

        pitestConfig = project.getExtensions().getByType(PitestPluginExtension)

        project.group = 'test.group'

        rouchEmptyPitClasspathFileWorkaround(project)

        // trick the "Querying the mapped value of ... before task '...' has completed is not supported" check
        // as here in the unit tests, the tasks will never be executed when resolving these providers
        project.tasks.configureEach {
            state.outcome = TaskExecutionOutcome.EXECUTED
        }
    }

    protected void configureTask(String taskName, Closure block) {
        project.tasks.named { name -> name == taskName }.configureEach { task ->
            TaskStateInternal
                .getDeclaredField("outcome")
                .tap { field -> field.accessible = true }
                .set(task.state, null)
        }
        block()
        project.tasks.named { name -> name == taskName }.configureEach { task ->
            task.state.outcome = TaskExecutionOutcome.EXECUTED
        }
    }

    protected PitestTask getJustOnePitestTaskOrFail() {
        Set<Task> tasks = project.getTasksByName(PITEST_TASK_NAME, false) //forces "afterEvaluate"
        assert tasks?.size() == 1 : "Expected tasks: '$PITEST_TASK_NAME', All tasks: ${project.tasks}"
        assert tasks[0] instanceof PitestTask
        return (PitestTask)tasks[0]
    }

    //as "useClasspathFile" is enabled by default (#237) the tests with ProjectBuilder would file on missing file
    //(alternatively "project.pitest.useClasspathFile = false" could be used)
    private static void rouchEmptyPitClasspathFileWorkaround(Project project) {
        DirectoryProperty buildDirectoryProperty = project.layout.buildDirectory
        buildDirectoryProperty.get().asFile.mkdirs()
        buildDirectoryProperty.dir("pitClasspath").get().asFile.createNewFile()
    }

}
