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
        and:
            task = getJustOnePitestTaskOrFail()
        expect:
            !task.createTaskArgumentMap()['classPath'].contains(libFile.path)
        where:
            libFileName << ['lib.so', 'win.dll', 'dyn.dylib', "my.xml", "strange.orbit"]   //TODO: Add test with more than one element
    }

    def "should filter .pom file"() {
        given:
            File pomFile = addFileWithFileNameAsCompileDependencyAndReturnAsFile('foo.pom')
        and:
            task = getJustOnePitestTaskOrFail()
        expect:
            !task.createTaskArgumentMap()['classPath'].contains(pomFile.path)
    }

    def "should not filer regular dependency '#depFileName'"() {
        given:
            File depFile = addFileWithFileNameAsCompileDependencyAndReturnAsFile(depFileName)
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
            File depFile = addFileWithFileNameAsCompileDependencyAndReturnAsFile('foo.jar')
        and:
            File libDepFile = addFileWithFileNameAsCompileDependencyAndReturnAsFile('bar.so')
        and:
            task = getJustOnePitestTaskOrFail()
        expect:
            task.createTaskArgumentMap()['classPath'].contains(depFile.path)
            !task.createTaskArgumentMap()['classPath'].contains(libDepFile.path)
    }

    private File addFileWithFileNameAsCompileDependencyAndReturnAsFile(String depFileName) {
        File depFile = new File(tmpProjectDir.root, depFileName)
        project.dependencies.add('compile', project.files(depFile))
        return depFile
    }
}
