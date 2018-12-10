package pl.droidsonroids.gradle.pitest

import groovy.transform.SelfType

@SelfType(BasicProjectBuilderSpec)
//@CompileStatic  //Fails with: Error:(13, 16) Groovyc: [Static type checking] - Non static method BasicProjectBuilderSpec#getJustOnePitestTaskOrFail cannot be called from static context
trait WithPitestTaskInitialization {

    PitestTask task

    def setup() {
        task = getJustOnePitestTaskOrFail()
    }
}