package info.solidsoft.gradle.pitest

import groovy.transform.SelfType

@SelfType(BasicProjectBuilderSpec)
trait WithPitestTaskInitialization {

    PitestTask task

    def setup() {
        task = getJustOnePitestTaskOrFail()
    }
}
