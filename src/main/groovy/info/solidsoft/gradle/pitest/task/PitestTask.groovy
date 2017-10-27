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
package info.solidsoft.gradle.pitest.task

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import info.solidsoft.gradle.pitest.PitestPlugin

import java.util.logging.Logger

/**
 * Gradle task implementation for Pitest.
 */
@CompileStatic
class PitestTask extends AbstractPitestTask {

    static final Logger LOG = Logger.getLogger(PitestTask.class.typeName)
    public static final String NAME = "pitest"

    PitestTask() {
        LOG.info("Pitest task registered")
        description = "Run PIT analysis for java classes"
        group = PitestPlugin.PITEST_TASK_GROUP
    }

    @Override
    void exec() {
        //Workaround for compatibility with Gradle <4.0 due to setArgs(List) and setJvmArgs(List) added in Gradle 4.0
        args = createListOfAllArgumentsForPit()
        jvmArgs = (getMainProcessJvmArgs() ?: getJvmArgs())
        main = "org.pitest.mutationtest.commandline.MutationCoverageReport"
        classpath = getLaunchClasspath()
        super.exec()
    }
}
