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

import spock.lang.Issue

class PitestPluginClasspathFilteringSpec extends BasicProjectBuilderSpec {

    private PitestTask task

    @Issue('https://github.com/szpak/gradle-pitest-plugin/issues/52')
    def "should filter dynamic library '#libFileName'"() {
        given:
            File libFile = new File(tmpProjectDir.root, libFileName)
            project.dependencies.add('compile', project.files(libFile))
        and:
            task = getJustOnePitestTaskOrFail()
        expect:
            !task.createTaskArgumentMap()['classPath'].contains(libFileName)
        where:
            libFileName << ['lib.so', 'win.dll', 'dyn.dylib']   //TODO: Add test with more than one element
    }

    def "should filter .pom file"() {
        given:
            File pomFile = new File(tmpProjectDir.root, 'foo.pom')
            project.dependencies.add('compile', project.files(pomFile))
        and:
            task = getJustOnePitestTaskOrFail()
        expect:
            !task.createTaskArgumentMap()['classPath'].contains(pomFile.path)
    }

    def "should not filer regular dependency '#depFileName'"() {
        given:
            File depFile = new File(tmpProjectDir.root, depFileName)
            project.dependencies.add('compile', project.files(depFile))
        and:
            task = getJustOnePitestTaskOrFail()
        expect:
            task.createTaskArgumentMap()['classPath'].contains(depFile.path)
        where:
            depFileName << ['foo.jar', 'foo.zip']
    }

    def "should not filer source set directory"() {
        given:
            File testClassesDir = new File(new File(new File(new File(tmpProjectDir.root, 'build'), 'classes'), 'java'), 'test')
        and:
            task = getJustOnePitestTaskOrFail()
        expect:
            task.createTaskArgumentMap()['classPath'].contains(testClassesDir.path)
    }

    def "should filter excluded dependencies remaining regular ones"() {
        given:
            File depFile = new File(tmpProjectDir.root, 'foo.jar')
            project.dependencies.add('compile', project.files(depFile))
        and:
            File libDepFile = new File(tmpProjectDir.root, 'bar.so')
            project.dependencies.add('compile', project.files(libDepFile))
        and:
            task = getJustOnePitestTaskOrFail()
        expect:
            task.createTaskArgumentMap()['classPath'].contains(depFile.path)
            !task.createTaskArgumentMap()['classPath'].contains(libDepFile.path)
    }
}
