package info.solidsoft.gradle.pitest

import groovy.transform.CompileDynamic
import groovy.transform.SelfType

@SelfType(BasicProjectBuilderSpec)
@CompileDynamic
trait WithPitestTaskInitialization {

    PitestTask task

    void setup() {
        task = getJustOnePitestTaskOrFail()
    }

}
