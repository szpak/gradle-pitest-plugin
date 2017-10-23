package info.solidsoft.gradle.pitest

class ScmPitestTaskScmConnectionTest extends BasicProjectBuilderSpec implements WithScmPitestTaskInitialization {
    def "should map connectionType '#connectionType' correctly" () {
        given:
            project.scmPitest.connectionType = connectionType
        expect:
            scmPitestTask.connectionType == connectionType
        where:
            connectionType << ["connection","developerConnection"]
    }
}
