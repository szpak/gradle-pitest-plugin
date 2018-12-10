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
package pl.droidsonroids.gradle.pitest

import spock.lang.Issue

class PitestPluginClasspathFilteringSpec extends BasicProjectBuilderSpec {

    @Issue('https://github.com/szpak/gradle-pitest-plugin/issues/52')
    def "should filter dynamic library '#libFileName' by default"() {
        given:
            File libFile = new File(tmpProjectDir.root, libFileName)
        and:
            PitestTask task = getJustOnePitestTaskOrFail()
        expect:
            !forceClasspathResolutionAndReturnIt(task).contains(libFile.path)
        where:
            libFileName << ['lib.so', 'win.dll', 'dyn.dylib']   //TODO: Add test with more than one element
    }

    def "should filter .pom file by default"() {
        given:
            File pomFile = addFileWithFileNameAsCompileDependencyAndReturnAsFile('foo.pom')
        and:
            PitestTask task = getJustOnePitestTaskOrFail()
        expect:
            !forceClasspathResolutionAndReturnIt(task).contains(pomFile.path)
    }

    def "should not filter regular dependency '#depFileName' by default"() {
        given:
            File depFile = addFileWithFileNameAsCompileDependencyAndReturnAsFile(depFileName)
        and:
            PitestTask task = getJustOnePitestTaskOrFail()
        expect:
            forceClasspathResolutionAndReturnIt(task).contains(depFile.path)
        where:
            depFileName << ['foo.jar', 'foo.zip']
    }

    def "should not filter source set directory by default"() {
        given:
            File testClassesDir = new File(tmpProjectDir.root, 'build/intermediates/javac/release/compileReleaseJavaWithJavac/classes')
        and:
            PitestTask task = getJustOnePitestTaskOrFail()
        expect:
            forceClasspathResolutionAndReturnIt(task).contains(testClassesDir.path)
    }

    def "should filter excluded dependencies remaining regular ones"() {
        given:
            File depFile = addFileWithFileNameAsCompileDependencyAndReturnAsFile('foo.jar')
        and:
            File libDepFile = addFileWithFileNameAsCompileDependencyAndReturnAsFile('bar.so')
        and:
            PitestTask task = getJustOnePitestTaskOrFail()
        expect:
            forceClasspathResolutionAndReturnIt(task).contains(depFile.path)
            !forceClasspathResolutionAndReturnIt(task).contains(libDepFile.path)
    }

    @Issue('https://github.com/szpak/gradle-pitest-plugin/issues/53')
    def "should filter user defined extensions"() {
        given:
            File depFile = addFileWithFileNameAsCompileDependencyAndReturnAsFile('file.extra')
        and:
            project.pitest.fileExtensionsToFilter += ['extra']
        and:
            PitestTask task = getJustOnePitestTaskOrFail()
        expect:
            !forceClasspathResolutionAndReturnIt(task).contains(depFile.path)
    }

    @Issue('https://github.com/szpak/gradle-pitest-plugin/issues/53')
    def "should allow to override extensions filtered by default"() {
        given:
            File depFile = addFileWithFileNameAsCompileDependencyAndReturnAsFile('needed.so')
        and:
            project.pitest.fileExtensionsToFilter = ['extra']
        and:
            PitestTask task = getJustOnePitestTaskOrFail()
        expect:
            forceClasspathResolutionAndReturnIt(task).contains(depFile.path)
    }

    @Issue('https://github.com/szpak/gradle-pitest-plugin/issues/53')
    def "should allow to provide extra extensions in addition to default ones"() {
        given:
            File libDepFile = addFileWithFileNameAsCompileDependencyAndReturnAsFile('default.so')
            File extraDepFile = addFileWithFileNameAsCompileDependencyAndReturnAsFile('file.extra')
        and:
            project.pitest.fileExtensionsToFilter += ['extra']
        and:
            PitestTask task = getJustOnePitestTaskOrFail()
        expect:
            String resolvedPitClasspath = forceClasspathResolutionAndReturnIt(task)
            !resolvedPitClasspath.contains(libDepFile.path)
            !resolvedPitClasspath.contains(extraDepFile.path)
    }

    @Issue('https://github.com/szpak/gradle-pitest-plugin/issues/53')
    def "should not fail on fileExtensionsToFilter set to null"() {
        given:
            project.pitest.fileExtensionsToFilter = null
        and:
            PitestTask task = getJustOnePitestTaskOrFail()
        when:
            String resolvedPitClasspath = forceClasspathResolutionAndReturnIt(task)
        then:
            noExceptionThrown()
    }

    private String forceClasspathResolutionAndReturnIt(PitestTask task) {
        return task.createTaskArgumentMap()['classPath']
    }

    private File addFileWithFileNameAsCompileDependencyAndReturnAsFile(String depFileName) {
        File depFile = new File(tmpProjectDir.root, depFileName)
        depFile.createNewFile()
        project.dependencies.add('compile', project.files(depFile))
        return depFile
    }
}
