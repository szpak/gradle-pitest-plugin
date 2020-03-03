package info.solidsoft.gradle.pitest

import groovy.transform.CompileDynamic
import groovy.transform.SelfType

@SelfType(BasicProjectBuilderSpec)
//@CompileStatic  //Fails with: Error:(13, 16) Groovyc: [Static type checking] - Non static method info.solidsoft.gradle.pitest.BasicProjectBuilderSpec#getJustOnePitestTaskOrFail cannot be called from static context
@CompileDynamic
trait WithPitestTaskInitialization {

    PitestTask task

    void setup() {
        task = getJustOnePitestTaskOrFail()
    }

}
